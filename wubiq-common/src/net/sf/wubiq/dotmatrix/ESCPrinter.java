package net.sf.wubiq.dotmatrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import javax.print.PrintException;

import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.enums.TextAlignmentType;
import net.sf.wubiq.wrappers.TextField;

/**
 * Source code from 
 * http://code.google.com/p/escprinter/source/browse/trunk/net/drayah/matrixprinter/ESCPrinter.java
 * It let printing directly to a Dot matrix.
 * 
 * Page sizes are received in Twips (inch * 1440). Text positions are in points (inch / 72).
 * @author Federico Alcantara
 *
 */
public class ESCPrinter {
    /* fields */
    private String printer;
    private boolean escp24pin; //boolean to indicate whether the printer is a 24 pin esc/p2 epson
    private OutputStream ostream;
    private PrintStream pstream;
    private Charset charset;
    private int pageHeight = 396; // In number of minimum line feed. For 11 inch = 396
    private int pageWidth = 1020; // In 1/120 inches. for 8.5 = 1020.
    private float horPosition = 0;
    private int verticalPosition = 0; // In fine line units (1/216 inch, 1/180 for 24 pin) from the top of the page.
    private int lineCount = 0;
    protected static int MAX_ADVANCE_9PIN = 216; //for 24/48 pin esc/p2 printers this should be 180
    protected static int MAX_ADVANCE_24PIN = 180;
    protected static int MAX_UNITS = 127; //for vertical positioning range is between 0 - 255 (0 <= n <= 255) according to epson ref. but 255 gives weird errors at 1.5f, 127 as max (0 - 128) seems to be working
    protected static final float CM_PER_INCH = 2.54f;
    protected static final double BASELINE_9PIN = 7d / 72d; // Inches from the top of the character cell to the baseline.
    protected static final double BASELINE_24PIN = 18d / 180d;
    
    /* decimal ascii values for epson ESC/P commands */
    protected static final char ESC = 27; //escape
    protected static final char SPACE = 32; //space
    protected static final char AT = 64; //@
    protected static final char LINE_FEED = 10; //line feed/new spacing
    protected static final char PARENTHESIS_LEFT = 40;
    protected static final char BACKSLASH = 92;
    protected static final char HYPHEN = 45;
    protected static final char CR = 13; //carriage return
    protected static final char TAB = 9; //horizontal tab
    protected static final char FF = 12; //form feed
    protected static final char ZERO = 48; // ZERO
    protected static final char THREE = 51; // 3
    protected static final char FOUR = 52; // 4: italic on (ESC 4)
    protected static final char FIVE = 53; // 5: italic off (ESC 5)
    protected static final char g = 103; //15cpi pitch
    protected static final char p = 112; //used for choosing proportional mode or fixed-pitch
    protected static final char t = 116; //used for character set assignment/selection
    protected static final char l = 108; //used for setting left margin
    protected static final char x = 120; //used for setting draft or letter quality (LQ) printing
    protected static final char C = 67; //bold font on
    protected static final char E = 69; //bold font on
    protected static final char F = 70; //bold font off
    protected static final char J = 74; //used for advancing paper vertically
    protected static final char M = 77; //Used for 12cpi
    protected static final char P = 80; //10cpi pitch
    protected static final char Q = 81; //used for setting right margin
    protected static final char V = 86; // used for absolute vertical positioning
    protected static final char W = 87; // used for double width
    protected static final char DOLLAR_SIGN = 36; //used for absolute horizontal positioning
    protected static final char ARGUMENT_0 = 0;
    protected static final char ARGUMENT_1 = 1;
    protected static final char ARGUMENT_2 = 2;
    protected static final char ARGUMENT_3 = 3;
    protected static final char ARGUMENT_4 = 4;
    protected static final char ARGUMENT_5 = 5;
    protected static final char ARGUMENT_6 = 6;
    protected static final char ARGUMENT_7 = 7;
    protected static final char ARGUMENT_12 = 12;
    protected static final char ARGUMENT_15 = 15;
    protected static final char ARGUMENT_18 = 18;
    protected static final char ARGUMENT_25 = 25;
    
    /* character sets */
    public static final char USA = ARGUMENT_1;
    public static final char BRAZIL = ARGUMENT_25;
    public static final char LATIN_AMERICA = ARGUMENT_12;
	
    /**
     * Creates a new instance of printer streamer.
     * @param printer Printer url.
     * @param pageWidth Width of the page in twips (inch * 1440).
     * @param pageHeight Height of the page in twips (inch * 1440).
     * @param escp24pin True if the streamer is to print to a 24pin printer.
     */
    public ESCPrinter(String printer, int pageWidth, int pageHeight, boolean escp24pin) throws PrintException {
    	this(printer, pageWidth, pageHeight, escp24pin, false);
    }

    /**
     * Creates a new instance of printer streamer.
     * @param printer Printer url.
     * @param pageWidth Width of the page in twips (inch * 1440).
     * @param pageHeight Height of the page in twips (inch * 1440).
     * @param escp24pin True if the streamer is to print to a 24pin printer.
     * @param append True to add to what the url already holds, false to start it again.
     */
    public ESCPrinter(String printer, int pageWidth, int pageHeight, boolean escp24pin, boolean append) throws PrintException {
        //pre: printer non null String indicating network path to printer
        this.printer = printer;
        this.escp24pin = escp24pin;
        this.charset = textCharset();
        initialize(append);
        setPageHeight(pageHeight);
        setPageWidth(pageWidth);
    }

    public ESCPrinter(String printer, Double pageWidth, Double pageHeight, boolean escp24pin) throws PrintException {
    	this(printer, pageWidth.intValue(), pageHeight.intValue(), escp24pin, false);
    }

    public ESCPrinter(String printer, Double pageWidth, Double pageHeight, boolean escp24pin, boolean append) throws PrintException {
    	this(printer, pageWidth.intValue(), pageHeight.intValue(), escp24pin, append);
    }
    
    /**
     * Closes the streamer and printer.
     */
    public void close() {
        //post: closes the stream, used when printjob ended
        try {
            pstream.close();
            ostream.close();
        } 
        catch (IOException e) { e.printStackTrace(); }
    }
    
    private void initialize(boolean append) throws PrintException {
        //post: returns true if stream to network printer successfully opened, streams for writing to esc/p printer created
        try {
            //create stream objs
        	if (ostream == null) {
        		ostream = new FileOutputStream(printer, append);
        	}
            pstream = new PrintStream(ostream);
            
            //reset default settings
            pstream.print(ESC);
            pstream.print(AT);
            
            //select draft quality printing
            selectDraftPrinting();
            
            setCharacterSet(characterTable());
            
            proportionalMode(false);
            selectMinimumLineSpacing();
            pstream.flush();
        } 
        catch (FileNotFoundException e) {
        	throw new PrintException(e);
        }
    }
    /**
     * Sets the paper width in 1/120 inches.
     * @param width Width in twips (inch * 1440).
     */
    public void setPageWidth(int width) {
    	this.pageWidth = (int)(toInches(width) * 120);
    }
    
    /**
     * Sets the paper size in 5/180 == 6/216 inch.
     * @param height Height to be set. In twips (inch * 1440).
     */
    public void setPageHeight(int height){
    	this.pageHeight = (int)(toInches(height) * 180 / 5);
    	int inches = Math.round(toInches(height));
    	if (inches >= 1 && inches <= 22) { // ESC C 0 n: page length of n inches.
    		pstream.write(ESC);
    		pstream.write(C);
    		pstream.write(0);
    		pstream.write(inches);
    	}
    	pstream.flush();
    }
    
    /**
     * Selects the appropriate CPI
     * @param cpi CPI to be set.
     */
    public void selectCPI(float cpi) {
		condensed(false);
		pstream.flush();
		doubleWidth(false);
		pstream.flush();
		if (cpi == TextField.CPI_5) {
			select10CPI();
			doubleWidth(true);
		} else if (cpi == TextField.CPI_6) {
			select12CPI();
			doubleWidth(true);
		} else if (cpi == TextField.CPI_10) {
    		select10CPI();
    		condensed(false);
    	} else if (cpi == TextField.CPI_12) {
    		select12CPI();
    	} else if (cpi == TextField.CPI_15) {
    		select15CPI();
    	} else if (cpi == TextField.CPI_17) {
    		select10CPI();
    		condensed(true);
    	} else if (cpi == TextField.CPI_20) {
    		select12CPI();
    		condensed(true);
        } else {
    		select12CPI();
    	}
		pstream.flush();
    }
    

    public void select10CPI() { //10 characters per inch (condensed available)
        pstream.print(ESC);
        pstream.print(P);
    }
    
    public void select12CPI() { //12 characters per inch (condensed available)
        pstream.print(ESC);
        pstream.print(M);
    }

    public void select15CPI() { //15 characters per inch (condensed not available)
        pstream.print(ESC);
        pstream.print(g);
    }
    
    public void selectDraftPrinting() { //set draft quality printing
        pstream.print(ESC);
        pstream.print(x);
        pstream.print((char) 48);
    }
    
    public void selectLQPrinting() { //set letter quality printing
        pstream.print(ESC);
        pstream.print(x);
        pstream.print((char) 49);
    }
    
    public void selectOneEigthLineSpacing() {
    	pstream.print(ESC);
    	pstream.print(ZERO);
    }
    
    /**
     * Minimum line spacing is set to 5/180 (24pin) or 6/216 (9pin)
     */
    public void selectMinimumLineSpacing() {
    	pstream.print(ESC);
    	pstream.print(THREE);
    	if (escp24pin) {
    		pstream.print(ARGUMENT_5);
    	} else {
    		pstream.print(ARGUMENT_6);
    	}
    }
    public void setCharacterSet(char charset) {
        //assign character table
        pstream.print(ESC);
        pstream.print(PARENTHESIS_LEFT);
        pstream.print(t);
        pstream.print(ARGUMENT_3); //always 3
        pstream.print(ARGUMENT_0); //always 0
        pstream.print(ARGUMENT_1); //selectable character table 1
        pstream.print(charset); //registered character table
        pstream.print(ARGUMENT_0); //always 0
        
        //select character table
        pstream.print(ESC);
        pstream.print(t);
        pstream.print(ARGUMENT_1); //selectable character table 1
    }
    
    
    public void bold(boolean bold) {
        pstream.print(ESC);
        if (bold) {
            pstream.print(E);
        } else {
            pstream.print(F);
        }
    }
    
    public void italic(boolean italic) {
        pstream.print(ESC);
        if (italic) {
            pstream.print(FOUR);
        } else {
            pstream.print(FIVE);
        }
    }

    public void underline(boolean underline) {
        pstream.print(ESC);
        pstream.print(HYPHEN);
        if (underline) {
            pstream.print(ARGUMENT_1);
        } else {
            pstream.print(ARGUMENT_0);
        }
    }

    public void condensed(boolean condensed){
    	if (condensed) {
        	pstream.print(ESC);
    		pstream.print(ARGUMENT_15);
    	} else {
    		pstream.print(ARGUMENT_18);
    	}
    }
    
    public void proportionalMode(boolean proportional) {
        pstream.print(ESC);
        pstream.print(p);
        if (proportional){
            pstream.print((char) 49);
        } else {
            pstream.print((char) 48);
        }
    }
    
    public void doubleWidth(boolean doubleWidth) { // double width printing.
        pstream.print(ESC);
        pstream.print(W);
        if (doubleWidth) {
        	pstream.print(ARGUMENT_1);
        } else {
        	pstream.print(ARGUMENT_0);
        }
    }

	/**
	 * Moves the print head to where the text goes: down with ESC J (paper only moves forward) so
	 * that the characters' baseline lies on the text's baseline, across with ESC $ (absolute position).
	 * @param textField Holder of the field information.
	 * @throws IOException
	 */
	public void advanceTo(TextField textField) throws IOException {
		selectCPI(textField.getTextFontCPI());
		// Vertical: the head position is the top of the character cell, a few dots above the baseline.
		double headInches = textField.getY() / 72d - (escp24pin ? BASELINE_24PIN : BASELINE_9PIN);
		int target = (int) Math.round(headInches * (escp24pin ? 180d : 216d));
		while (target > verticalPosition) {
			int step = Math.min(255, target - verticalPosition);
			pstream.write(ESC);
			pstream.write(J);
			pstream.write(step);
			verticalPosition += step;
		}
		// Horizontal, in 1/120 inch.
		float fixValue = 120 / textField.getTextFontCPI();
		float toPos = textField.getX() * 120 / 72;
		float textWidth = textField.getWidth() * 120 / 72;
		if (TextAlignmentType.RIGHT.equals(textField.getTextAlignment())) {
			toPos += textWidth - (textField.getText().length() * fixValue);
		} else if (TextAlignmentType.CENTER.equals(textField.getTextAlignment())) {
			toPos += ((textWidth - (textField.getText().length()) * fixValue)) / 2;
		}
		int position = Math.max(0, Math.round(toPos / 2)); // ESC $ counts in 1/60 inch.
		pstream.write(ESC);
		pstream.write(DOLLAR_SIGN);
		pstream.write(position % 256);
		pstream.write(position / 256);
		horPosition = position * 2;
	}
	
	/**
	 * Prints the text to the given printer.
	 * @param textField Text to print.
	 * @return Print head advance performed.
	 * @throws IOException
	 */
	public float print(TextField textField) throws IOException {
		bold(textField.isBold());
		italic(textField.isItalic());
		underline(textField.isUnderline());
		float cpi = textField.getTextFontCPI();
		float returnValue = 0;
		float fixValue = 120 / cpi;
		if (((((float)textField.getText().length()) * fixValue) + horPosition) <= pageWidth) {
			horPosition += (((float)textField.getText().length()) * fixValue);
			writeText(textField.getText());
		} else {
			for (char charValue : textField.getText().toCharArray()) {
				returnValue += fixValue;
				print(charValue, fixValue);
			}
		}
		underline(false);
		return returnValue;
	}
	
	/**
	 * Prints the char.
	 * @param charValue Character to print.
	 * @param charSize Size of the character (1/120 inches). 
	 * If character is to be printed outside pageWidth, then it is not printed. 
	 */
	private void print(char charValue, float charSize) {
		if ((horPosition + charSize) <= pageWidth) {
			writeText(String.valueOf(charValue));
		}
		horPosition += charSize;
	}


    public void setMargins(int columnsLeft, int columnsRight) {
        //pre: columnsLeft > 0 && <= 255, columnsRight > 0 && <= 255
        //post: sets left margin to columnsLeft columns and right margin to columnsRight columns
        //left
        pstream.print(ESC);
        pstream.print(l);
        pstream.print((char) columnsLeft);
        
        //right
        pstream.print(ESC);
        pstream.print(Q);
        pstream.print((char) columnsRight);
    }
    
    public void lineFeed() {
        //post: performs new line
        pstream.print(CR); //according to epson esc/p ref. manual always send carriage return before line feed
        pstream.print(LINE_FEED);
        horPosition = 0;
        lineCount++;
    }
    
    public void formFeed() {
        horPosition = 0;
        //post: ejects single sheet
        if (pageHeight <= 127) {
	        pstream.print(CR); //according to epson esc/p ref. manual it is recommended to send carriage return before form feed
	        pstream.print(FF);
        } else {
        	lineCount += 1;  // one less as the FORM FEED issues a line feed
        	for (int i = lineCount; i < pageHeight; i++) {
                pstream.print(CR); //according to epson esc/p ref. manual always send carriage return before line feed
                pstream.print(LINE_FEED);
        	}
	        pstream.print(CR); //according to epson esc/p ref. manual it is recommended to send carriage return before form feed
	        pstream.print(FF);
    }
        pstream.flush();
        lineCount = 0;
    }
    
    /**
     * Ends the page: the paper is ejected and the print head goes back to the left of the next page.
     */
    public void endPage() {
    	pstream.write(CR);
    	pstream.write(FF);
    	pstream.flush();
    	horPosition = 0;
    	verticalPosition = 0;
    	lineCount = 0;
    }
    
    public String getShare() {
        //post: returns printer share name (Windows network)
        return printer;
    }
    
    public PrintStream getPrintStream() {
        //post: returns printer share name (Windows network)
        return pstream;
    }
    
    /**
     * Writes the text as bytes of the printer's character table. Characters the charset has no
     * byte for are written as a question mark.
     */
    private void writeText(String text) {
    	byte[] bytes = text.getBytes(charset);
    	pstream.write(bytes, 0, bytes.length);
    }
    
    /**
     * @return Charset to encode text with: the one of the {@link PropertyKeys#WUBIQ_DOTMATRIX_CHARSET}
     * property; if none, the system's on Windows (what the application has always sent there) and
     * Cp1252 elsewhere, where the system's would be UTF-8, which no printer table has.
     */
    private static Charset textCharset() {
    	String name = System.getProperty(PropertyKeys.WUBIQ_DOTMATRIX_CHARSET);
    	if (name != null && name.trim().length() > 0) {
    		try {
    			return Charset.forName(name.trim());
    		} catch (IllegalArgumentException e) {
    			// Unknown or illegal name: falls back to the default.
    		}
    	}
    	if (System.getProperty("os.name", "").startsWith("Windows")) {
    		return Charset.defaultCharset();
    	}
    	try {
    		return Charset.forName("Cp1252");
    	} catch (IllegalArgumentException e) {
    		return Charset.defaultCharset();
    	}
    }
    
    /**
     * @return Registered character table to ask the printer for: the number in the
     * {@link PropertyKeys#WUBIQ_DOTMATRIX_TABLE} property (3 is PC850); if none, {@link #LATIN_AMERICA},
     * as the application has always sent.
     */
    private static char characterTable() {
    	String value = System.getProperty(PropertyKeys.WUBIQ_DOTMATRIX_TABLE);
    	if (value != null) {
    		try {
    			int number = Integer.parseInt(value.trim());
    			if (number >= 0 && number <= 255) {
    				return (char) number;
    			}
    		} catch (NumberFormatException e) {
    			// Not a number: falls back to the default.
    		}
    	}
    	return LATIN_AMERICA;
    }
    
    private float toInches(double twipValue) {
    	return (float) (twipValue / 1440d);
    }
    
    public String toString() {
        //post: returns String representation of ESCPrinter e.g. <ESCPrinter[share=...]>
        StringBuilder strb = new StringBuilder();
        strb.append("<ESCPrinter[share=").append(printer).append(", 24pin=").append(escp24pin).append("]>");
        return strb.toString();
    }
    
}