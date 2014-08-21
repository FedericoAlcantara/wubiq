/**
 * 
 */
package net.sf.wubiq.tests;

import java.awt.Component;
import java.io.File;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.wubiq.utils.InstallerProperties;

import org.uispec4j.Button;
import org.uispec4j.CheckBox;
import org.uispec4j.ComboBox;
import org.uispec4j.Spinner;
import org.uispec4j.Table;
import org.uispec4j.TextBox;
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
	private final String BUTTON_ADD_PREFIX = "btnAdd";
	private final String BUTTON_DELETE_PREFIX = "btnDelete";
	
	private final String ADDRESS_FLD_NAME = "Address";
	private final String GROUPS_FLD_NAME = "Group";
	private final String PHOTO_PRINTERS_FLD_NAME = "PhotoPrinter";
	private final String DM_PRINTERS_FLD_NAME = "DmPrinter";
	private final String DM_HQ_PRINTERS_FLD_NAME = "DmHqPrinter";
	
	private final String ADDRESS_POPUP_TITLE = "Enter Wubiq Server's Address";
	private final String GROUP_POPUP_TITLE = "Enter Group Name";
	private final String PHOTO_PRINTERS_POPUP_TITLE = "Enter Photo Printer";
	private final String DM_PRINTERS_POPUP_TITLE = "Enter Dot Matrix Printer";
	private final String DM_HQ_PRINTERS_POPUP_TITLE = "Enter Dot Matrix Printer (HQ)";
	
	private final String OK_RESPONSE = "OK";
	private final String NO_RESPONSE = "NO";
	private final String YES_RESPONSE = "YES";
	
	private CheckBox changedChkBox;
	private Button enLangButton;
	private Button esLangButton;
	private Button resetButton;
	private Button saveButton;

	private JLabel lblUuid;
	private JTextField uuid;

	// General
	private Table connectionTable;
	private Table groupsTable;
	private Button connectionAddButton;
	private Button connectionDeleteButton;
	private Button groupsAddButton;
	private Button groupsDeleteButton;
	
	// Parameters
	private TextBox applicationName;
	private TextBox servletName;
    private CheckBox enableVerbosedLog;
    private Spinner logLevel;
	
    // Printers
	private Table photoPrintersTable;
	private Table dmPrintersTable;
	private Table dmHqPrintersTable;
	private Button photoPrintersAddButton;
	private Button photoPrintersDeleteButton;
	private Button dmPrintersAddButton;
	private Button dmPrintersDeleteButton;
	private Button dmHqPrintersAddButton;
	private Button dmHqPrintersDeleteButton;
	private ComboBox defaultDmFont;
	private CheckBox forceLogicalFonts;

	// Advanced
    private TextBox pollInterval;
    private TextBox printJobWait;
    private TextBox additionalJvmParameters;
    
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setAdapter(new TestAdapter());
	}
	
	public void testLanguageChange() {
		clearAll();
		esLangButton.click();
		assertEquals("Id Único", lblUuid.getText());
		enLangButton.click();
		assertEquals("Unique Id", lblUuid.getText());
	}
	
	/**
	 * Test if groups are properly added and deleted.
	 */
	public void testGroups() {
		clearAll();
		textTable(groupsTable, groupsAddButton, groupsDeleteButton, GROUP_POPUP_TITLE);
	}
	
	public void testAddresses() {
		clearAll();

		assertEquals(0, connectionTable.getRowCount());
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "localhost", OK_RESPONSE);
		assertEquals(1, connectionTable.getRowCount());
		// This is 8080 because the wubiq-server MUST be running
		assertEquals("http://localhost:8080", connectionTable.getJTable().getValueAt(0, 0));
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "http://sicflex.com:8090", OK_RESPONSE);
		assertEquals(2, connectionTable.getRowCount());
		assertEquals("http://sicflex.com:8090", connectionTable.getJTable().getValueAt(1, 0));
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "sicflex.com:8090", OK_RESPONSE);
		assertEquals(2, connectionTable.getRowCount());
		// Test delete
		connectionTable.selectRowSpan(0, 1);
		connectionDeleteButton.click();
		assertEquals(0, connectionTable.getRowCount());
	}
	
	public void testPhotoPrinters() {
		clearAll();
		textTable(photoPrintersTable, photoPrintersAddButton, photoPrintersDeleteButton, PHOTO_PRINTERS_POPUP_TITLE);
	}
	
	public void testDotMatrixPrinters() {
		clearAll();
		textTable(dmPrintersTable, dmPrintersAddButton, dmPrintersDeleteButton, DM_PRINTERS_POPUP_TITLE);
	}
	
	public void testDotMatrixHqPrinters() {
		clearAll();
		textTable(dmHqPrintersTable, dmHqPrintersAddButton, dmHqPrintersDeleteButton, DM_HQ_PRINTERS_POPUP_TITLE);
	}
	
	public void testSave() {
		clearAll();
		
		validateInfoErrorPopup(saveButton, "Unique id must be specified");
		
		uuid.setText("unique_id_01");
		
		validateWarningPopup(saveButton, "You have to specify at least one valid connection. This client will not work without connections.", NO_RESPONSE);
		// add an invalid address
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "htxddd://localhost", OK_RESPONSE);

		validateWarningPopup(saveButton, "You have to specify at least one valid connection. This client will not work without connections.", NO_RESPONSE);
		// add a valid address
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "localhost:8080", OK_RESPONSE);
		validateInfoErrorPopup(saveButton, "The file wubiq-installer.properties was successfully created");
		writeData();
		// Save again
		validateInfoErrorPopup(saveButton, "The file wubiq-installer.properties was successfully updated");
		assertEquals("UUID", "unique_id_01", uuid.getText());

		afterSave();
	}
	
	private void writeData() {
		// General
		addText(groupsAddButton, GROUP_POPUP_TITLE, "GROUP_ONE", OK_RESPONSE);
		addText(groupsAddButton, GROUP_POPUP_TITLE, "GROUP_TWO", OK_RESPONSE);
		
		addText(connectionAddButton, ADDRESS_POPUP_TITLE, "http://127.0.0.1:8090", OK_RESPONSE);
		
		// Parameters
		applicationName.setText("jSicflexMobile");
		servletName.setText("wubiqspe.do");
		enableVerbosedLog.select();
		logLevel.setValue(5);
		
		// Printers
		addText(photoPrintersAddButton, PHOTO_PRINTERS_POPUP_TITLE, "SHARP", OK_RESPONSE);
		addText(photoPrintersAddButton, PHOTO_PRINTERS_POPUP_TITLE, "2050CS", OK_RESPONSE);
		addText(photoPrintersAddButton, PHOTO_PRINTERS_POPUP_TITLE, "2010CS", OK_RESPONSE);
		
		addText(dmPrintersAddButton, DM_PRINTERS_POPUP_TITLE, "lx-", OK_RESPONSE);
		addText(dmPrintersAddButton, DM_PRINTERS_POPUP_TITLE, "mx-", OK_RESPONSE);
		addText(dmPrintersAddButton, DM_PRINTERS_POPUP_TITLE, "rx-", OK_RESPONSE);
		
		addText(dmHqPrintersAddButton, DM_HQ_PRINTERS_POPUP_TITLE, "lq", OK_RESPONSE);
		defaultDmFont.setText("Sans Serif");
		forceLogicalFonts.select();
		
		// Advanced
		((JFormattedTextField)pollInterval.getAwtComponent()).setValue(400);
		((JFormattedTextField)printJobWait.getAwtComponent()).setValue(1000);;
		additionalJvmParameters.setText("-Xmx128m");
		assertTrue("Changed Box", changedChkBox.isSelected());
		
	}
	
	private void afterSave() {
		// Refresh
		validateWarningPopup(resetButton, "You are going to reset all values, any changes will be lost. Are you sure?", YES_RESPONSE);
		assertFalse("Changed Box", changedChkBox.isSelected());
		assertEquals("UUID", "unique_id_01", uuid.getText());
		
		// General
		assertEquals("Groups", 2, groupsTable.getRowCount());
		assertTableContent(groupsTable, "GROUP_ONE", "GROUP_TWO");
		assertEquals("Connections", 2, connectionTable.getRowCount());
		assertTableContent(connectionTable, "http://localhost:8080", "http://127.0.0.1:8090");
		
		// Parameters
		assertEquals("applicationName", "jSicflexMobile", applicationName.getText());
		assertEquals("servletName", "wubiqspe.do", servletName.getText());
		assertTrue("enableVerbosed", enableVerbosedLog.isSelected());
		assertEquals("logLevel", 5, logLevel.getAwtComponent().getValue());
		
		// Printers
		assertEquals("photoPrinters", 3, photoPrintersTable.getRowCount());
		assertTableContent(photoPrintersTable, "SHARP", "2050CS", "2010CS");
		
		assertEquals("dmPrinters", 3, dmPrintersTable.getRowCount());
		assertTableContent(dmPrintersTable, "lx-", "mx-", "rx-");
		
		assertEquals("dmHqPrinters", 1, dmHqPrintersTable.getRowCount());
		assertTableContent(dmHqPrintersTable, "lq");
		
		assertEquals("defaultFonts", "Sans Serif", defaultDmFont.getAwtComponent().getSelectedItem());
		assertTrue("logicalFonts", forceLogicalFonts.isSelected());

		// Advanced
		assertEquals("pollInterval", "400", pollInterval.getText());
		assertEquals("printJobWait", "1,000", printJobWait.getText());
	}
	
	/**
	 * Tests text tables.
	 * @param table Table to test.
	 * @param addButton Add button associated with the table.
	 * @param deleteButton Delete button associated with the table.
	 * @param optionPaneTitle Title of the pane.
	 */
	private void textTable(Table table,
			Button addButton,
			Button deleteButton,
			String optionPaneTitle) {
		assertEquals(0, table.getRowCount());
		addText(addButton, optionPaneTitle, addButton.getName() + " One", OK_RESPONSE);
		// validate if added.
		assertEquals(1, table.getRowCount());
		assertEquals(addButton.getName() + "_One", table.getJTable().getValueAt(0, 0));
		// Should not be duplicates
		addText(addButton, optionPaneTitle, addButton.getName() + " One", OK_RESPONSE);
		assertEquals(1, table.getRowCount());
		assertEquals(addButton.getName() + "_One", table.getJTable().getValueAt(0, 0));
		// Add another with strange characters and leading space trailing space
		addText(addButton, optionPaneTitle, " () " + addButton.getName() + " Two/*&^22 ", OK_RESPONSE);
		assertEquals(2, table.getRowCount());
		assertEquals(addButton.getName() + "_Two22", table.getJTable().getValueAt(1, 0));
		// Add a third one
		addText(addButton, optionPaneTitle, "Third " + addButton.getName(), OK_RESPONSE);
		// Now delete the group, testing multiple selection
		table.selectRowSpan(0, 2);
		deleteButton.click();
		assertEquals(0, table.getRowCount());
	}
	
	private void assertTableContent(Table table, String... contents) {
		for (int row = 0; row < contents.length; row++) {
			assertEquals("Table:" + table.getName() + ", row:" + row,
					contents[row], table.getContentAt(row, 0));
		}
	}
	
	/**
	 * Adds a text to  given element.
	 * @param button Button that triggers the dialog.
	 * @param expectedTitle Expected title of the dialog.
	 * @param textToAdd Text to add.
	 */
	private void addText(final Button button, 
			final String expectedTitle, 
			final String textToAdd,
			final String responseButton) {
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
				return window.getButton(responseButton).triggerClick();
			}
		}).run();
	}
	
	private void validateInfoErrorPopup(final Button button, final String popText) {
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
	
	private void validateWarningPopup(final Button button, final String popText, final String responseButton) {
		WindowInterceptor.init(new Trigger(){
			@Override
			public void run() throws Exception {
				button.click();
			}
			
		}).process(new WindowHandler("first popup") {
			@Override
			public Trigger process(Window window) throws Exception {
				JLabel label = null;
				for (Component component : window.getSwingComponents(JLabel.class)) {
					if (((JLabel)component).getText() != null) {
						label = (JLabel)component;
						break;
					}
				}
				if (label == null) {
					fail("No label found for message:" + popText);
				}
				assertEquals(popText, label.getText());
				return window.getButton(responseButton).triggerClick();
			}
			
		}).run();
	}

	private Window getEnglishWindow() {
		Window mainWindow = getMainWindow();
		Button enLang = mainWindow.getButton("btnEnFlag");
		enLang.click();
		return mainWindow;
	}
	
	private void createComponents(Window mainWindow) {
		changedChkBox = mainWindow.getCheckBox("chkChangedState");
		enLangButton = mainWindow.getButton("btnEnFlag");
		esLangButton = mainWindow.getButton("btnEsFlag");
		resetButton = mainWindow.getButton("btnReset");
		saveButton = mainWindow.getButton("btnSave");
		lblUuid = (JLabel) mainWindow.findSwingComponent(new NameMatcher("lblUuid"));
		uuid = (JTextField)mainWindow.findSwingComponent(new NameMatcher("fldUuid"));

		// General
		connectionTable = mainWindow.getTable("fldInternetAddresses");
		groupsTable = mainWindow.getTable(GROUPS_FLD_NAME);
		connectionAddButton = mainWindow.getButton(BUTTON_ADD_PREFIX + ADDRESS_FLD_NAME);
		connectionDeleteButton = mainWindow.getButton(BUTTON_DELETE_PREFIX + ADDRESS_FLD_NAME);
		groupsAddButton = mainWindow.getButton(BUTTON_ADD_PREFIX + GROUPS_FLD_NAME);
		groupsDeleteButton = mainWindow.getButton(BUTTON_DELETE_PREFIX + GROUPS_FLD_NAME);

		// Parameters
		applicationName = mainWindow.getTextBox("fldApplicationName");
		servletName = mainWindow.getTextBox("fldServletName");
		enableVerbosedLog = mainWindow.getCheckBox("fldEnableVerbosedLog");
		logLevel = mainWindow.getSpinner("fldLogLevel");

		// Printers
		photoPrintersTable = mainWindow.getTable(PHOTO_PRINTERS_FLD_NAME);
		dmPrintersTable = mainWindow.getTable(DM_PRINTERS_FLD_NAME);
		dmHqPrintersTable = mainWindow.getTable(DM_HQ_PRINTERS_FLD_NAME);
		photoPrintersAddButton = mainWindow.getButton(BUTTON_ADD_PREFIX + PHOTO_PRINTERS_FLD_NAME);
		photoPrintersDeleteButton = mainWindow.getButton(BUTTON_DELETE_PREFIX + PHOTO_PRINTERS_FLD_NAME);
		dmPrintersAddButton = mainWindow.getButton(BUTTON_ADD_PREFIX + DM_PRINTERS_FLD_NAME);
		dmPrintersDeleteButton = mainWindow.getButton(BUTTON_DELETE_PREFIX + DM_PRINTERS_FLD_NAME);
		dmHqPrintersAddButton = mainWindow.getButton(BUTTON_ADD_PREFIX + DM_HQ_PRINTERS_FLD_NAME);
		dmHqPrintersDeleteButton = mainWindow.getButton(BUTTON_DELETE_PREFIX + DM_HQ_PRINTERS_FLD_NAME);
		defaultDmFont = mainWindow.getComboBox("fldDmDefaultFont");
		forceLogicalFonts = mainWindow.getCheckBox("fldForceLogicalFonts");
		
		// Advanced
		pollInterval = mainWindow.getTextBox("fldPollInterval");
		printJobWait = mainWindow.getTextBox("fldPrintJobWait");
		additionalJvmParameters = mainWindow.getTextBox("fldAdditionalJvmParameters");
	}
	
	private void clearAll() {
		// First delete the properties file.
		File propertiesFile = null;
		boolean deleted = true;
		do {
			propertiesFile = InstallerProperties.INSTANCE("").getPropertiesFile();
			if (propertiesFile != null &&
					propertiesFile.exists()) {
				propertiesFile.delete();
			} else {
				deleted = false;
			}
		}
		while(deleted);

		// Now start the test
		Window mainWindow = getEnglishWindow();
		createComponents(mainWindow);
	}
}
