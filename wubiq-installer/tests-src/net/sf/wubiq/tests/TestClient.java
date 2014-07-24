/**
 * 
 */
package net.sf.wubiq.tests;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.wubiq.utils.InstallerProperties;

import org.uispec4j.Button;
import org.uispec4j.Table;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

/**
 * @author Federico Alcantara
 *
 */
public class TestClient extends UISpecTestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setAdapter(new TestAdapter());
	}
	
	public void testLanguageChange() {
		Window mainWindow = getMainWindow();
		Button enLang = mainWindow.getButton("btnEnFlag");
		Button esLang = mainWindow.getButton("btnEsFlag");
		esLang.click();
		JLabel lblUuid = (JLabel) mainWindow.findSwingComponent(new NameMatcher("lblUuid"));
		assertEquals("Id Único", lblUuid.getText());
		enLang.click();
		assertEquals("Unique Id", lblUuid.getText());
	}
	
	/**
	 * Test if groups are properly added and deleted.
	 */
	public void testGroups() {
		textTable("fldGroups", "Group", "Enter Group Name");
	}
	
	public void testAddresses() {
		Window mainWindow = getEnglishWindow();
		String buttonName = "Address";
		String optionPaneTitle = "Enter Wubiq Server's Address";
		Table table = mainWindow.getTable("fldInternetAddresses");
		assertEquals(0, table.getRowCount());
		Button addButton = mainWindow.getButton("btnAdd" + buttonName);
		addText(addButton, optionPaneTitle, "localhost");
		assertEquals(1, table.getRowCount());
		assertEquals("http://localhost", table.getJTable().getValueAt(0, 0));
		addText(addButton, optionPaneTitle, "http://sicflex.com:8090");
		assertEquals(2, table.getRowCount());
		assertEquals("http://sicflex.com:8090", table.getJTable().getValueAt(1, 0));
		addText(addButton, optionPaneTitle, "sicflex.com:8090");
		assertEquals(2, table.getRowCount());
		// Test delete
		table.selectRowSpan(0, 1);

		Button deleteButton = mainWindow.getButton("btnDelete" + buttonName);
		deleteButton.click();
		assertEquals(0, table.getRowCount());
	}
	
	public void testPhotoPrinters() {
		textTable("fldPhotoPrinters", "PhotoPrinter", "Enter Photo Printer");
	}
	
	public void testDotMatrixPrinters() {
		textTable("fldDmPrinters", "DmPrinter", "Enter Dot Matrix Printer");
	}
	
	public void testDotMatrixHqPrinters() {
		textTable("fldDmHqPrinters", "DmHq", "Enter Dot Matrix Printer (HQ)");
	}
	
	public void testSave() {
		Window mainWindow = getEnglishWindow();
		File propertiesFile = InstallerProperties.INSTANCE.getPropertiesFile();
		if (propertiesFile != null &&
				propertiesFile.exists()) {
			propertiesFile.delete();
		}
		Button saveButton = mainWindow.getButton("btnSave");
		validatePopUp(saveButton, "Unique id must be specified");
		
		JTextField uuid = (JTextField) mainWindow.findSwingComponent(new NameMatcher("fldUuid"));
		uuid.setText(" unique id&^_)01");
		assertEquals("unique_id_01", uuid.getText());
		validatePopUp(saveButton, "The file wubiq-installer.properties was successfully created");
		validatePopUp(saveButton, "The file wubiq-installer.properties was successfully updated");
	}
	
	/**
	 * Tests the functionality of a test table.
	 * @param tableName Name of the table.
	 * @param buttonName Button generic name.
	 * @param optionPaneTitle Title for the option pane.
	 */
	private void textTable(String tableName,
			String buttonName,
			String optionPaneTitle) {
		Window mainWindow = getEnglishWindow();
		Table table = mainWindow.getTable(tableName);
		Button addButton = mainWindow.getButton("btnAdd" + buttonName);
		assertEquals(0, table.getRowCount());
		addText(addButton, optionPaneTitle, buttonName + " One");
		// validate if added.
		assertEquals(1, table.getRowCount());
		assertEquals(buttonName + "_One", table.getJTable().getValueAt(0, 0));
		// Should not be duplicates
		addText(addButton, optionPaneTitle, buttonName + " One");
		assertEquals(1, table.getRowCount());
		assertEquals(buttonName + "_One", table.getJTable().getValueAt(0, 0));
		// Add another with strange characters and leading space trailing space
		addText(addButton, optionPaneTitle, " () " + buttonName + " Two/*&^22 ");
		assertEquals(2, table.getRowCount());
		assertEquals(buttonName + "_Two22", table.getJTable().getValueAt(1, 0));
		// Add a third one
		addText(addButton, optionPaneTitle, "Third " + buttonName);
		// Now delete the group, testing multiple selection
		table.selectRowSpan(0, 2);
		Button deleteButton = mainWindow.getButton("btnDelete" + buttonName);
		deleteButton.click();
		assertEquals(0, table.getRowCount());
	}
	
	/**
	 * Adds a text to  given element.
	 * @param button Button that triggers the dialog.
	 * @param expectedTitle Expected title of the dialog.
	 * @param textToAdd Text to add.
	 */
	private void addText(final Button button, 
			final String expectedTitle, 
			final String textToAdd) {
		WindowInterceptor.init(new Trigger(){
			@Override
			public void run() throws Exception {
				button.click();
			}
			
		}).process(new WindowHandler("ok") {
			@Override
			public Trigger process(Window window) throws Exception {
				assertEquals(expectedTitle, window.getTitle());
				JTextField field = (JTextField) window.findSwingComponent(new NameMatcher("OptionPane.textField"));
				field.setText(textToAdd);
				return window.getButton("OK").triggerClick();
			}
		}).run();
	}
	
	private void validatePopUp(final Button button, final String popText) {
		WindowInterceptor.init(new Trigger(){
			@Override
			public void run() throws Exception {
				button.click();
			}
			
		}).process(new WindowHandler("first popup") {
			@Override
			public Trigger process(Window window) throws Exception {
				JLabel label = (JLabel) window.findSwingComponent(new NameMatcher("OptionPane.label"));
				assertEquals(popText, label.getText());
				return window.getButton("OK").triggerClick();
			}
			
		}).run();
	}
	
	private Window getEnglishWindow() {
		Window mainWindow = getMainWindow();
		Button enLang = mainWindow.getButton("btnEnFlag");
		enLang.click();
		return mainWindow;
	}
}
