package net.sf.wubiq.wrappers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PrintableWrapper implements Printable, Serializable {
	private static final long serialVersionUID = 1L;
	private static Log LOG = LogFactory.getLog(PrintableWrapper.class);
	transient private Printable printable;
	private List<GraphicCommand> graphicCommands;
	private int returnValue = 0;
	private int width;
	private int height;
	public PrintableWrapper() {
	}

	public PrintableWrapper(Printable printable) {
		this.printable = printable;
	}
	
	
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (graphicCommands == null) {
			graphicCommands = new ArrayList<GraphicCommand>();
			GraphicsRecorder graphicsRecorder = new GraphicsRecorder(graphicCommands);
			width = new Double(pageFormat.getWidth()).intValue();
			height = new Double(pageFormat.getHeight()).intValue();
			graphicsRecorder.setBackground(Color.white);
			graphicsRecorder.setColor(Color.BLACK);
			graphicsRecorder.setFont(graphics.getFont());
			graphicsRecorder.clearRect(0, 0, width, height);
			returnValue = printable.print(graphicsRecorder, new PageFormatWrapper(pageFormat), pageIndex);
		} else {
			Graphics2D graph = (Graphics2D) graphics;
			for (GraphicCommand graphicCommand : graphicCommands) {
				executeMethod(graph, graphicCommand);
			}
		}
		return returnValue;
	}
	
	@SuppressWarnings("rawtypes")
	private void executeMethod(Graphics2D graph, GraphicCommand graphicCommand) {
		Method method = null;
		Class[] parameterTypes = new Class[]{};
		Object[] parameterValues = new Object[]{};
		if (graphicCommand.getParameters().length > 0) {
			parameterTypes = new Class[graphicCommand.getParameters().length];
			parameterValues = new Object[graphicCommand.getParameters().length];
			for (int index = 0; index < parameterTypes.length; index++) {
				GraphicParameter parameter = graphicCommand.getParameters()[index];
				parameterValues[index] = parameter.getParameterValue();
				parameterTypes[index] = parameter.getParameterType();
					
				if (parameterTypes[index].equals(ImageWrapper.class)) {
					parameterValues[index] = 
							extractImage(parameter.getParameterValue());
					parameterTypes[index] = Image.class;
				} else if (parameterTypes[index].equals(RenderableImageWrapper.class)) {
					parameterValues[index] = 
							extractRenderableImage(parameter.getParameterValue());
					parameterTypes[index] = RenderableImage.class;
				} else if (parameterTypes[index].equals(ImageObserverWrapper.class)) {
					parameterValues[index] = null;
					parameterTypes[index] = ImageObserver.class;
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
		}
	}
	
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
	 * Converts a ImageWrapper to an image.
	 * @param parameter Parameter containing the image wrapper.
	 * @return A BufferedImage or null if error.
	 */
	private Image extractImage(Object parameter) {
		return (Image)extractBufferedImage(parameter);
	}

	/**
	 * Converts a ImageWrapper to an image.
	 * @param parameter Parameter containing the image wrapper.
	 * @return A BufferedImage or null if error.
	 */
	private RenderableImage extractRenderableImage(Object parameter) {
		return (RenderableImage)extractBufferedImage(parameter);
	}
	
	/**
	 * Converts a ImageWrapper to an image.
	 * @param parameter Parameter containing the image wrapper.
	 * @return A BufferedImage or null if error.
	 */
	private BufferedImage extractBufferedImage(Object parameter) {
		ImageWrapper wrapper = (ImageWrapper)parameter;
		try {
			return ImageIO.read(new ByteArrayInputStream(wrapper.getImageData()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
