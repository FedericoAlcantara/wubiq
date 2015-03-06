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
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.ICompressible;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.wrappers.CompositeWrapper;
import net.sf.wubiq.wrappers.CompressedGraphicsPage;
import net.sf.wubiq.wrappers.GlyphChunkVectorCompressedWrapper;
import net.sf.wubiq.wrappers.GlyphChunkVectorWrapper;
import net.sf.wubiq.wrappers.GraphicCommand;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.ImageObserverWrapper;
import net.sf.wubiq.wrappers.ImageWrapper;
import net.sf.wubiq.wrappers.PageFormatWrapper;
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
		"print",
		"executeGraphics",
		"executeMethod",
		"findMethod",
		"setServerSupportsCompression"
	};

	private PrinterType printerType;
	private int printed = 0;
	private Integer returnValue = -1;
	private String slowerMethod;
	private long slowerTime;
	private boolean serverSupportsCompression;
	private CompressedGraphicsPage compressedGraphicsPage;

	public PrintableChunkRemote() {
		initialize();
	}
	
	public void setServerSupportsCompression(boolean serverSupportsCompression) {
		this.serverSupportsCompression = serverSupportsCompression;
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
		long startTime = new Date().getTime();			
		AffineTransform transform = graph.getTransform();
		Color background = graph.getBackground();
		Font font = graph.getFont();
		PageFormatWrapper remotePageFormat = new PageFormatWrapper(pageFormat);
		
		transform.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		GraphicsUtils.INSTANCE.scaleGraphics(graph, remotePageFormat, true);
		
		manager().readFromRemote(new RemoteCommand(objectUUID(),
				"setClientSupportsCompression",
				new GraphicParameter(boolean.class, true)));
		
		returnValue = (Integer) manager().readFromRemote(new RemoteCommand(objectUUID(),
				"print",
				new GraphicParameter(int.class, pageIndex),
				new GraphicParameter(PageFormatWrapper.class, remotePageFormat),
				new GraphicParameter(AffineTransform.class, transform),
				new GraphicParameter(Color.class, background),
				new GraphicParameter(Font.class, font)));
		Set<GraphicCommand> commands = null;
		
		if (serverSupportsCompression) {
			compressedGraphicsPage = 
					(CompressedGraphicsPage)
					manager().readFromRemote(new RemoteCommand(objectUUID(),
							"compressedGraphicsPage",
							new GraphicParameter(int.class, pageIndex)));
			commands = compressedGraphicsPage.getGraphicCommands();
		} else {
			commands =
				(Set<GraphicCommand>)
				manager().readFromRemote(new RemoteCommand(objectUUID(),
						"graphicCommands",
						new GraphicParameter(int.class, pageIndex)));
		}
		if (commands != null) {
			long startTimePrint = new Date().getTime();
			printerType = PrintServiceUtils.printerType(graph.getDeviceConfiguration().getDevice().getIDstring());
			executeGraphics(graph, remotePageFormat, commands);
			manager().doLog("Sending page " + pageIndex + " to printer took (step " + ++printed + "):" + (new Date().getTime() - startTimePrint) + "ms", 4);		
		}
		manager().doLog("Generating page " + pageIndex + " took:" + (new Date().getTime() - startTime) + "ms", 4);
		if (compressedGraphicsPage != null) {
			compressedGraphicsPage.clearLists();
		}
		return returnValue;
	}
		
	/**
	 * Iterates through the previously serialized command set.
	 * @param graph Graphics2D object receiving the commands.
	 * @param pageFormat Format of the page to be rendered.
	 * @param xScale new scale to apply horizontally wise.
	 * @param yScale new scale to apply vertically wise.
	 */
	protected void executeGraphics(Graphics2D graph, PageFormat pageFormat,
			Set<GraphicCommand> graphicCommands) {
		if (graphicCommands != null && 
				!graphicCommands.isEmpty()) {
			long startTime = new Date().getTime();
			Iterator<GraphicCommand> it = graphicCommands.iterator();
			int count = 0;
			while (it.hasNext()) {
				GraphicCommand graphicCommand = it.next();
				count++;
				try {
					executeMethod(graph, graphicCommand);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			long diff = new Date().getTime() - startTime;
			manager().doLog("Took:" + diff + "ms, processed count:" + count + ", slower method:" + slowerMethod + " took:" + slowerTime, 5);
		}
	}
	
	/**
	 * Validates and perform the graphic command. It unwraps previously wrapped objects
	 * and applies the command to the given Graphics2D object.
	 * @param graph Graphics2D object.
	 * @param graphicCommand Specific graphic command.
	 */
	@SuppressWarnings("rawtypes")
	protected void executeMethod(Graphics2D graph, GraphicCommand graphicCommand) {
		double xScale = 1;
		double yScale = 1;
		Class[] parameterTypes = new Class[]{};
		Object[] parameterValues = new Object[]{};
		long startTime = new Date().getTime();

		if (serverSupportsCompression) {
			Object parameterValue = graphicCommand.getParameters()[0].getParameterValue();
			if (parameterValue != null && parameterValue instanceof ICompressible) {
				GraphicParameter[] newGraphicsParameters = ((ICompressible)
						parameterValue).uncompress(compressedGraphicsPage);
				if (newGraphicsParameters != null) {
					graphicCommand.setParameters(newGraphicsParameters);
				}
			}
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
					
				} else if (parameterTypes[index].equals(GlyphChunkVectorWrapper.class)) {
					parameterTypes[index] = GlyphVector.class;
					GlyphChunkVectorWrapper glyphVectorWrapper = (GlyphChunkVectorWrapper)parameterValues[index];
					Font font = GraphicsUtils.INSTANCE.properFont(glyphVectorWrapper.getFont(), printerType);
					executeMethod(graph, "setFont", new Class[]{Font.class}, new Object[]{font});
					StringBuffer data = new StringBuffer("");
					for (char charAt : glyphVectorWrapper.getCharacters()) {
						data.append(charAt);
					}
					graphicCommand.setMethodName("drawString");
					parameterValues[index] = data.toString();
					parameterTypes[index] = String.class;
				} else if (parameterTypes[index].equals(GlyphChunkVectorCompressedWrapper.class)) {
					parameterTypes[index] = GlyphVector.class;
					GlyphChunkVectorCompressedWrapper glyphVectorWrapper = (GlyphChunkVectorCompressedWrapper)parameterValues[index];
					Font font = GraphicsUtils.INSTANCE.properFont(glyphVectorWrapper.getFont(), printerType);
					executeMethod(graph, "setFont", new Class[]{Font.class}, new Object[]{font});
					StringBuffer data = new StringBuffer("");
					for (char charAt : glyphVectorWrapper.getCharacters()) {
						data.append(charAt);
					}
					graphicCommand.setMethodName("drawString");
					parameterValues[index] = data.toString();
					parameterTypes[index] = String.class;
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
		startTime = new Date().getTime();
		executeMethod(graph, graphicCommand.getMethodName(), parameterTypes, parameterValues);
		long diff = new Date().getTime() - startTime;
		if (diff > slowerTime) {
			slowerTime = diff;
			slowerMethod = graphicCommand.getMethodName();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void executeMethod(Graphics2D graph, String methodName, Class[] parameterTypes, Object[] parameterValues) {
		Method method = null;
		method = findMethod(graph.getClass(), methodName, parameterTypes);
		if (method != null) {
			try {
				method.setAccessible(true);
				method.invoke(graph, parameterValues);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		} else {
			LOG.info("Method not FOUND:" + methodName);
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
		return null;
	}
	
}
