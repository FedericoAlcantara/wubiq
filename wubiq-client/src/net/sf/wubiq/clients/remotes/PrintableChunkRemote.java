package net.sf.wubiq.clients.remotes;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.wrappers.CompositeWrapper;
import net.sf.wubiq.wrappers.GlyphVectorWrapper;
import net.sf.wubiq.wrappers.GraphicCommand;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.ImageObserverWrapper;
import net.sf.wubiq.wrappers.ImageWrapper;
import net.sf.wubiq.wrappers.RenderableImageWrapper;
import net.sf.wubiq.wrappers.RenderedImageWrapper;
import net.sf.wubiq.wrappers.RenderingHintWrapper;
import net.sf.wubiq.wrappers.RenderingHintsWrapper;
import net.sf.wubiq.wrappers.ShapeWrapper;
import net.sf.wubiq.wrappers.StrokeWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps a printable object into a serializable class.
 * This object stores the commands sent to a graphic recorder.
 * Because it is serialized, the commands can later be de-serialized
 * into this object.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintableChunkRemote implements Printable, IProxyClient {
	private static final Log LOG = LogFactory.getLog(PrintableChunkRemote.class);
	public static final String[] FILTERED_METHODS = new String[]{
		"print"
	};

	private PrinterType printerType;
	private transient AffineTransform initialTransform;
	private boolean initialTransformApplied = false;
	private boolean noScale = false;
	private int printed = 0;
	private Integer returnValue = -1;
	private static Set<GraphicCommand>graphicCommands = new TreeSet<GraphicCommand>();

	public PrintableChunkRemote() {
		initialize();
	}
	
	/**
	 * This a two stage print method. 
	 * First it records graphics command into itself by using a GraphicRecorder object.
	 * After it is de-serialized, then it sends the previously saved command to the
	 * graphics provided by the local printer object.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics2D graph = (Graphics2D) graphics;
		if (printed <= 0) {
			long startTime = new Date().getTime();
/*
			returnValue = (Integer) manager().readFromRemote(new RemoteCommand(objectUUID(),
					"print",
					new GraphicParameter(PageFormatWrapper.class, pageFormat),
					new GraphicParameter(int.class, pageIndex)));
*/	
			try {
				returnValue = Integer.parseInt(
					(String)manager().directServer(
							jobId().toString(), 
							DirectConnectCommand.EXECUTE_PRINTABLE,
							DirectConnectKeys.DIRECT_CONNECT_DATA
								+ ParameterKeys.PARAMETER_SEPARATOR
								+ DirectConnectUtils.INSTANCE.serialize(objectUUID()),
							DirectConnectKeys.DIRECT_CONNECT_PAGE_FORMAT
								+ ParameterKeys.PARAMETER_SEPARATOR
								+ DirectConnectUtils.INSTANCE.serialize(pageFormat),
							DirectConnectKeys.DIRECT_CONNECT_PAGE_INDEX
								+ ParameterKeys.PARAMETER_SEPARATOR
								+ Integer.toString(pageIndex)));
			} catch (Exception e) {
				LOG.fatal(e.getMessage(), e);
				throw new RuntimeException(e);
			}
					
			manager().doLog("Generating page in server took:" + (new Date().getTime() - startTime) + "ms", 4); 
		}
		if (returnValue != null && Printable.NO_SUCH_PAGE != returnValue) {
			graphicCommands.clear();
			
			try {
				if (printed > 0) {
					graphicCommands.addAll((Set<GraphicCommand>)
							DirectConnectUtils.INSTANCE.deserialize((String)
									manager().directServer(
									jobId().toString(),
									DirectConnectCommand.POLL_PRINTABLE_DATA,
									DirectConnectKeys.DIRECT_CONNECT_DATA
										+ ParameterKeys.PARAMETER_SEPARATOR
										+ DirectConnectUtils.INSTANCE.serialize(objectUUID()),
									DirectConnectKeys.DIRECT_CONNECT_PAGE_INDEX
										+ ParameterKeys.PARAMETER_SEPARATOR
										+ Integer.toString(pageIndex)
									)));
				}
			} catch (Exception e) {
				LOG.fatal(e.getMessage(), e);
				throw new RuntimeException(e);
			}
			if (graphicCommands != null && !graphicCommands.isEmpty()) {
				long startTime = new Date().getTime();
				printerType = PrintServiceUtils.printerType(graph.getDeviceConfiguration().getDevice().getIDstring());
				Point2D scaleValue = GraphicsUtils.INSTANCE.scaleGraphics(graph, pageFormat, noScale);
				executeGraphics(graph, pageFormat, scaleValue.getX(), scaleValue.getY(), graphicCommands);
				manager().doLog("Sending page to printer took:" + (new Date().getTime() - startTime) + "ms", 4); 
			}
		}
		printed++;
		return returnValue;
	}
	
	/**
	 * Iterates through the previously serialized command set.
	 * @param graph Graphics2D object receiving the commands.
	 * @param pageFormat Format of the page to be rendered.
	 * @param xScale new scale to apply horizontally wise.
	 * @param yScale new scale to apply vertically wise.
	 */
	private void executeGraphics(Graphics2D graph, PageFormat pageFormat, double xScale, double yScale,
			Set<GraphicCommand> graphicCommands) {
		initialTransform = graph.getTransform();
		initialTransformApplied = false;
		if (graphicCommands != null) {
			Iterator<GraphicCommand> it = graphicCommands.iterator();
			while (it.hasNext()) {
				GraphicCommand graphicCommand = it.next();
	
				executeMethod(graph, graphicCommand, xScale, yScale);
			}
		}
	}
	
	/**
	 * Validates and perform the graphic command. It unwraps previously wrapped objects
	 * and applies the command to the given Graphics2D object.
	 * @param graph Graphics2D object.
	 * @param graphicCommand Specific graphic command.
	 */
	@SuppressWarnings("rawtypes")
	private void executeMethod(Graphics2D graph, GraphicCommand graphicCommand, double xScale, double yScale) {
		Method method = null;
		Class[] parameterTypes = new Class[]{};
		Object[] parameterValues = new Object[]{};
		if (graphicCommand.getMethodName().equals("setTransform")) {
			if (!initialTransformApplied) {
				graph.setTransform(initialTransform);
				initialTransformApplied = true;
			}
			return;
		}
		
		if ("setFont".equalsIgnoreCase(graphicCommand.getMethodName())) {
			Font originalFont = (Font)graphicCommand.getParameters()[0].getParameterValue();
			Font font = GraphicsUtils.INSTANCE.properFont(originalFont, printerType);
			graphicCommand.setParameters(new GraphicParameter[]{new GraphicParameter(Font.class, font)});
		}
		
		if (graphicCommand.getParameters().length > 0) {
			parameterTypes = new Class[graphicCommand.getParameters().length];
			parameterValues = new Object[graphicCommand.getParameters().length];
			Object renderingHintValue = null;
			for (int index = 0; index < parameterTypes.length; index++) {
				GraphicParameter parameter = graphicCommand.getParameters()[index];
				parameterValues[index] = parameter.getParameterValue();
				parameterTypes[index] = parameter.getParameterType();
				if (parameterTypes[index].equals(ImageWrapper.class)) {
					parameterValues[index] = 
							 ((ImageWrapper)parameter.getParameterValue()).getImage(xScale, yScale);
					parameterTypes[index] = Image.class;
					
				} else if (parameterTypes[index].equals(RenderedImageWrapper.class)) {
					parameterValues[index] = 
							((RenderedImageWrapper)parameter.getParameterValue()).getRenderedImage(xScale, yScale);
					parameterTypes[index] = RenderedImage.class;
					
				} else if (parameterTypes[index].equals(RenderableImageWrapper.class)) {
					parameterValues[index] = 
							((RenderableImageWrapper)parameter.getParameterValue()).getRenderedImage(xScale, yScale);
					parameterTypes[index] = RenderedImage.class;
					
				} else if (parameterTypes[index].equals(ImageObserverWrapper.class)) {
					parameterValues[index] = null;
					parameterTypes[index] = ImageObserver.class;
					
				} else if (parameterTypes[index].equals(ShapeWrapper.class)) {
					parameterTypes[index] = Shape.class;
					parameterValues[index] = ((ShapeWrapper) parameterValues[index]).getShape();
					
				} else if (parameterTypes[index].equals(Color.class)) {
					if (graphicCommand.getMethodName().equals("setPaint")) {
						parameterTypes[index] = Paint.class;
					}
					
				} else if (parameterTypes[index].equals(GlyphVectorWrapper.class)) {
					parameterTypes[index] = GlyphVector.class;
					GlyphVectorWrapper glyphVectorWrapper = (GlyphVectorWrapper)parameterValues[index];
					GlyphVector glyphVector = glyphVectorWrapper.getFont().createGlyphVector(graph.getFontRenderContext(), 
							glyphVectorWrapper.getCodes());
					parameterValues[index] = glyphVector;
					
				} else if (parameterTypes[index].equals(StrokeWrapper.class)) {
					parameterTypes[index] = Stroke.class;
					parameterValues[index] = ((StrokeWrapper) parameterValues[index]).getStroke(xScale, yScale);
					
				} else if (parameterTypes[index].equals(CompositeWrapper.class)) {
					parameterTypes[index] = Composite.class;
					parameterValues[index] = ((CompositeWrapper) parameterValues[index]).getComposite();

				} else if (parameterTypes[index].equals(RenderingHintsWrapper.class)) {
					parameterTypes[index] = Map.class;
					parameterValues[index] = ((RenderingHintsWrapper)parameterValues[index]).getRenderingHints();
					
				} else if (parameterTypes[index].equals(RenderingHintWrapper.class)) {
					RenderingHintWrapper hint = (RenderingHintWrapper)parameterValues[index];
					parameterTypes[index] = RenderingHints.Key.class;
					parameterValues[index] = hint.getKey();
					renderingHintValue = hint.getValue();
					
				} else if (index > 0 && parameterTypes[index - 1].equals(RenderingHints.Key.class)) {
					parameterTypes[index] = Object.class;
					parameterValues[index] = renderingHintValue;
				} 
			}
		}
		method = findMethod(graph.getClass(), graphicCommand.getMethodName(), parameterTypes);
		if (method != null) {
			try {
				method.setAccessible(true);
				method.invoke(graph, parameterValues);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		} else {
			LOG.info("Method not FOUND:" + graphicCommand.getMethodName());
		}
	}
	
	/**
	 * 
	 * @param clazz Class that must contain the given method.
	 * @param name Name of the method.
	 * @param parameterTypes Array of parameter types of the method sought.
	 * @return the method that should be called. Null if the method is not found.
	 */
			
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Method findMethod(Class clazz, String name, Class[] parameterTypes) {
		Method method = null;
		try {
			method = clazz.getDeclaredMethod(name, parameterTypes);
		} catch (SecurityException e) {
			LOG.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			method = null;
		}
		// let's try non declaredMethod
		if (method == null) {
			try {
				method = clazz.getMethod(name, parameterTypes);
			} catch (SecurityException e) {
				LOG.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				method = null;
			}
		}
		
		return method;
	}

	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	@Override
	public void initialize(){
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#jobId()
	 */
	@Override
	public Long jobId() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	@Override
	public UUID objectUUID() {
		return null;
	}

	/* *****************************************
	 * IProxyClient interface implementation
	 * *****************************************
	 */
	@Override
	public DirectPrintManager manager() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
