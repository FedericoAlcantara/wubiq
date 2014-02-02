package net.sf.wubiq.wrappers;

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
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wraps a printable object into a serializable class.
 * This object stores the commands sent to a graphic recorder.
 * Because it is serialized, the commands can later be deserialized
 * into this object.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintableWrapper implements Printable, Serializable {
	private static final long serialVersionUID = 1L;
	private static Log LOG = LogFactory.getLog(PrintableWrapper.class);
	private transient Printable printable;
	private Map<Integer, Set<GraphicCommand>> graphicCommandsList;
	private int returnValue = 0;
	private Map<Integer, Integer> printedPages;
	private boolean noScale = false;
	private transient boolean notSerialized = false;
	private PrinterType printerType;
	private transient AffineTransform initialTransform;
	private boolean initialTransformApplied = false;
	
	public PrintableWrapper() {
	}

	/**
	 * Creates a wrapper by reading printable values. This should be the 
	 * preferred constructor.
	 * @param printable Printable object to be encapsulated.
	 */
	public PrintableWrapper(Printable printable) {
		this();
		this.printable = printable;
		if (printable.getClass().getName().endsWith(".TextPrintable")) {
			noScale = true;
		}
		if (printable instanceof PrintableWrapper) {
			this.notSerialized = ((PrintableWrapper)printable).notSerialized;
		}
	}
	
	/**
	 * This a two stage print method. 
	 * First it records graphics command into itself by using a GraphicRecorder object.
	 * After it is deserialized, then it sends the previously saved command to the
	 * graphics provided by the local printer object.
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics2D graph = (Graphics2D) graphics;

		if (notSerialized) {
			Set<GraphicCommand> graphicCommands = createGraphicCommands(pageIndex);
			GraphicsRecorder graphicsRecorder = new GraphicsRecorder(graphicCommands, graph);
			returnValue = printable.print(graphicsRecorder, new PageFormatWrapper(pageFormat), pageIndex);
			if (returnValue == Printable.PAGE_EXISTS) {
				graphicCommandsList.put(pageIndex, graphicCommands);
				if (printedPages == null) {
					printedPages = new HashMap<Integer, Integer>();
				}
				printedPages.put(pageIndex, 0);
			}
		} else {
			Integer printed = printedPages.get(pageIndex);
			if (printed != null && printed <= 1) { // Normally a page rendering is called twice. One for peek graphics and the actual printing.
				returnValue = Printable.PAGE_EXISTS;
				printerType = PrintServiceUtils.printerType(graph.getDeviceConfiguration().getDevice().getIDstring());
				Point2D scaleValue = GraphicsUtils.INSTANCE.scaleGraphics(graph, pageFormat, noScale);
				executeGraphics(graph, pageFormat, scaleValue.getX(), scaleValue.getY(), pageIndex);
				printed += 1;
				printedPages.put(pageIndex, printed);
			} else {
				returnValue = Printable.NO_SUCH_PAGE;
			}
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
	private void executeGraphics(Graphics2D graph, PageFormat pageFormat, double xScale, double yScale, int pageIndex) {
		initialTransform = graph.getTransform();
		initialTransformApplied = false;
		Set<GraphicCommand> graphicCommands = getGraphicCommands(pageIndex);
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

	/**
	 * @return the notSerialized
	 */
	public boolean isNotSerialized() {
		return notSerialized;
	}

	/**
	 * @param notSerialized the notSerialized to set
	 */
	public void setNotSerialized(boolean notSerialized) {
		this.notSerialized = notSerialized;
	}

	/**
	 * Gets the graphic commands set by its page index.
	 * @param pageIndex Page index to the graphic commands set.
	 * @return Found instance or null if not found.
	 */
	private Set<GraphicCommand> getGraphicCommands(int pageIndex) {
		Set<GraphicCommand> returnValue = null;
		if (graphicCommandsList == null) {
			graphicCommandsList = new HashMap<Integer, Set<GraphicCommand>>();
		}

		returnValue = graphicCommandsList.get(pageIndex);
		return returnValue;
	}
	
	/**
	 * Finds or create a graphic commands set.
	 * @param pageIndex Index of the graphicCommand.
	 * @return An empty set of the graphic command. Never null.
	 */
	private Set<GraphicCommand> createGraphicCommands(int pageIndex) {
		
		Set<GraphicCommand> returnValue = getGraphicCommands(pageIndex);
		if (returnValue == null) {
			returnValue = new TreeSet<GraphicCommand>();
		}
		returnValue.clear();
		return returnValue;
	}
}
