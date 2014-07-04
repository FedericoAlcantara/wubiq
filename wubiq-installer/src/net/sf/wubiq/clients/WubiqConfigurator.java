package net.sf.wubiq.clients;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.AddressStatus;
import net.sf.wubiq.utils.InstallerBundle;
import net.sf.wubiq.utils.InstallerProperties;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the configuration of remote fiscal printers clients.
 * @author Federico Alcantara
 *
 */
public class WubiqConfigurator {
	private static final Log LOG = LogFactory.getLog(WubiqConfigurator.class);
	
	private JFrame frame;
	private JPanel contentPane;
	private JLabel lblId;
	private JLabel lblDescription;
	private JLabel lblSchema;
	private JComboBox fldId;
	private JTextField fldDescription;
	private JTextField fldSchema;
	private JButton btnSave;
	private JButton btnInstall;
	private JButton btnEsDo;
	private JButton btnEnUs;
	private JButton btnTestInternetAddresses;
	private JButton btnReset;
	private JScrollPane scrollPane;
	private JButton btnAddAddress;
	private JButton btnDeleteAddress;
	private JTable fldInternetAddresses;
	private JSplitPane saveSplitPane;
	private JSplitPane addressSplitPane;
	private JSplitPane idSplitPane;
	private JButton btnAddPrinter;
	private JButton btnDeletePrinter;
	private boolean changed;
	private Object oldValue;
	private Object newValue;
	private boolean changingId;
	private JCheckBox chkChangedState;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WubiqConfigurator window = new WubiqConfigurator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WubiqConfigurator() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws IOException {
		frame = new JFrame();
		frame.setTitle("Version:" + Labels.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 628, 414);
		contentPane = new JPanel();
		contentPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				idSplitPane.setDividerLocation(0.5d);
				addressSplitPane.setDividerLocation(0.5d);
				saveSplitPane.setDividerLocation(0.5d);
			}
		});
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][grow][][grow]", "[][grow][][][][][][][][][grow][][][][grow][grow][grow][grow]"));
		
		chkChangedState = new JCheckBox("");
		chkChangedState.setEnabled(false);
		contentPane.add(chkChangedState, "cell 0 0,alignx left,aligny center");
		
		btnEsDo = new JButton("");
		btnEsDo.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/es_DO.png")));
		contentPane.add(btnEsDo, "cell 2 0,grow");
		
		btnEsDo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Locale.setDefault(new Locale("es", "DO"));
				localize();
			}
			
		});
		
		btnEnUs = new JButton("");
		btnEnUs.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/us_EN.png")));
		contentPane.add(btnEnUs, "cell 3 0,grow");
		
		btnEnUs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Locale.setDefault(Locale.ENGLISH);
				localize();
			}
			
		});
		
		lblId = new JLabel();
		contentPane.add(lblId, "cell 0 1,alignx trailing,aligny baseline");
		
		fldId = new JComboBox();
		lblId.setLabelFor(fldId);
		fldId.setToolTipText("");
		fldId.setEditable(true);
		contentPane.add(fldId, "cell 1 1,growx");
		
		fldId.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent event) {
				onChangeId(event);
			}
		});
		
		idSplitPane = new JSplitPane();
		contentPane.add(idSplitPane, "cell 3 1,growx,aligny center");
		
		btnAddPrinter = new JButton("");
		btnAddPrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/plus.png")));
		btnAddPrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				addNewPrinter(event);
			}
		});
		idSplitPane.setLeftComponent(btnAddPrinter);
		
		btnDeletePrinter = new JButton("");
		btnDeletePrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/minus.png")));
		btnDeletePrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				deletePrinter(event);
			}
		});
		idSplitPane.setRightComponent(btnDeletePrinter);
		
		btnReset = new JButton("Reset");
		contentPane.add(btnReset, "cell 2 1");
		btnReset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				resetData(event);
			}
		});
		lblDescription = new JLabel("Description");
		contentPane.add(lblDescription, "cell 0 3,alignx trailing");
		
		fldDescription = new JTextField();
		lblDescription.setLabelFor(fldDescription);
		contentPane.add(fldDescription, "cell 1 3,growx");
		fldDescription.setColumns(10);
		fldDescription.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent event) {
				setChangedState(true);
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				setChangedState(true);
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				setChangedState(true);
			}
		});
		
		lblSchema = new JLabel("Group Id (Schema)");
		contentPane.add(lblSchema, "cell 0 5,alignx trailing");
		
		fldSchema = new JTextField();
		lblSchema.setLabelFor(fldSchema);
		contentPane.add(fldSchema, "cell 1 5,growx");
		fldSchema.setColumns(10);
		fldSchema.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent event) {
				setChangedState(true);
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				setChangedState(true);
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				setChangedState(true);
			}
		});
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 6 2 7,grow");
		
		fldInternetAddresses = new JTable(new AddressesTableModel());
		fldInternetAddresses.getColumnModel().getColumn(0).setResizable(true);
		fldInternetAddresses.getColumnModel().getColumn(0).setPreferredWidth(300);
		fldInternetAddresses.getColumnModel().getColumn(0).setMaxWidth(12312312);
		fldInternetAddresses.getColumnModel().getColumn(1).setMaxWidth(90);
		fldInternetAddresses.setCellSelectionEnabled(true);
		fldInternetAddresses.getModel().addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent event) {
				if (TableModelEvent.DELETE == event.getType() ||
						TableModelEvent.INSERT == event.getType() ||
						TableModelEvent.UPDATE == event.getType()) {
					setChangedState(true);
				}
			}
		});
		scrollPane.setViewportView(fldInternetAddresses);
		
		addressSplitPane = new JSplitPane();
		contentPane.add(addressSplitPane, "flowx,cell 3 6,growx,aligny center");
		
		btnAddAddress = new JButton("");
		btnAddAddress.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/plus.png")));
		addressSplitPane.setLeftComponent(btnAddAddress);
		btnAddAddress.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				addConnection(event);
			}
		});

		btnDeleteAddress = new JButton("");
		btnDeleteAddress.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/com/sicflex/fiscalprinters/client/minus.png")));
		addressSplitPane.setRightComponent(btnDeleteAddress);
		
		btnDeleteAddress.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				deleteConnection(event);
			}
		});
		
		btnTestInternetAddresses = new JButton("Test");
		contentPane.add(btnTestInternetAddresses, "cell 2 6 1 2,aligny center");
		btnTestInternetAddresses.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				testConnections();
			}
		});
		
		saveSplitPane = new JSplitPane();
		saveSplitPane.setResizeWeight(0.5);
		contentPane.add(saveSplitPane, "cell 0 14 2 1,growx,aligny center");
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save(event);
			}
		});
		saveSplitPane.setLeftComponent(btnSave);
		
		
		btnInstall = new JButton("Install");
		saveSplitPane.setRightComponent(btnInstall);
		
		btnInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				install(event);
			}
		});
		
		// Custom code
		changed = false;
		changingId = true;
		try {
			splashScreen();
			clear();
			localize();
		} finally {
			changingId = false;
		}
	}

	/**
	 * Triggered upon change of id.
	 * @param event Thrown when field id has a change.
	 */
	private boolean onChangeId(ItemEvent event) {
		boolean returnValue = false;
		if (changingId) {
			return returnValue;
		}
		if (ItemEvent.SELECTED == event.getStateChange()) {
			changingId = true;
			try {
				newValue = event.getItem();
				boolean loadNewValue = true;
				if (oldValue != null && !Is.emptyString(oldValue.toString().trim())) {
					if (changed) {
						int ask = JOptionPane.showConfirmDialog(null, InstallerBundle.getMessage("info.fiscal_printer.modified.text"),
								InstallerBundle.getMessage("info.fiscal_printer.modified.title"), 
								JOptionPane.YES_NO_CANCEL_OPTION, 
								JOptionPane.INFORMATION_MESSAGE);
						if (ask == JOptionPane.YES_OPTION) {
							save(oldValue.toString());
						} else if (ask == JOptionPane.CANCEL_OPTION) {
							loadNewValue = false;
							String description = fldDescription.getText();
							String schema = fldSchema.getText();
							TableModel addresses = fldInternetAddresses.getModel();
							fldId.setSelectedItem(oldValue);
							fldDescription.setText(description);
							fldSchema.setText(schema);
							fldInternetAddresses.setModel(addresses);
						}
					}
				}
				returnValue = loadNewValue;
				if (loadNewValue) {
					try {
						loadProperties((String)newValue);
						oldValue = newValue;
					} catch (IOException e) {
						LOG.error(e.getMessage());
						handleException(e);
					}
				}
			} finally {
				changingId = false;
			}
		}
		return returnValue;
	}
	
	/**
	 * Saves current printer data and adds a new printer.
	 * @param event Click event.
	 */
	private void addNewPrinter(ActionEvent event) {
		fldId.setSelectedItem("");
	}
	
	/**
	 * Deletes current printer.
	 * @param event Click event.
	 */
	private void deletePrinter(ActionEvent event) {
		if (!Is.emptyString((String)fldId.getSelectedItem())) {
			int ask = JOptionPane.showConfirmDialog(null, InstallerBundle.getMessage("warning.reset.text"),
					InstallerBundle.getMessage("warning.reset.title"), 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (ask == JOptionPane.YES_OPTION) {
				File propertiesFile = InstallerProperties.getPropertiesFile();
				if (propertiesFile != null && 
						propertiesFile.exists()) {
					propertiesFile.delete();
					JOptionPane.showMessageDialog(null, InstallerBundle.getMessage("info.printer_deleted_successfully"));
					setChangedState(false);
					clear();
				} else {
					JOptionPane.showMessageDialog(null, InstallerBundle.getMessage("info.printer_not_registered_yet"));
				}
			}
		}
	}
	
	/**
	 * Resets the current data.
	 * @param event Click event.
	 */
	private void resetData(ActionEvent event) {
		int ask = JOptionPane.showConfirmDialog(null, InstallerBundle.getMessage("warning.reset.text"),
				InstallerBundle.getMessage("warning.reset.title"), 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if (ask == JOptionPane.YES_OPTION) {
			try {
				loadProperties((String)fldId.getSelectedItem());
			} catch (IOException e) {
				LOG.error(e.getMessage());
				handleException(e);
			}
		}
	}
	
	/**
	 * Adds a new connection.
	 * @param event Click event.
	 */
	private void addConnection(ActionEvent event) {
		String writtenHost = (String)JOptionPane.showInputDialog(frame, 
				InstallerBundle.getLabel("host.text"), 
				InstallerBundle.getLabel("host.title"),
				JOptionPane.PLAIN_MESSAGE, null, null, "");
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		if (!Is.emptyString(writtenHost)) {
			URI uri;
			try {
				uri = new URI(writtenHost);
				if (uri.getScheme() == null) {
					uri = new URI("http://" + writtenHost);
				}
				String host = uri.toASCIIString();
				if (!tryConnections(host)) {
					String plainHost = host.split(":[0-9]").length > 1 ? host.substring(0, host.lastIndexOf(':')) : host;
					if (!tryConnections(plainHost)) {
						if (!tryConnections(plainHost + ":8090")) {
							if (!tryConnections(plainHost + ":8080")) {
								if (!tryConnections(plainHost + ":8181")) {
									JOptionPane.showMessageDialog(null, InstallerBundle.getMessage("warning.address_not_found", writtenHost));
								}
							}
						}
					}
				}
			} catch (URISyntaxException e) {
				LOG.error(e);
				handleException(e);
			}
		} else {
			tableModel.addRow(new Object[]{"http://"});
		}
	}
	
	/**
	 * Validates if the connection is possible and polls the server about
	 * its possible connection.
	 * @param sentHost Connection to test.
	 * @return True if the host was contacted.
	 */
	private boolean tryConnections(String sentHost) {
		boolean returnValue = false;
		try {
			URI uri = new URI(sentHost);
			String host = uri.toASCIIString();
			String serverInfos = readCommandConnection(host);
			if (!Is.emptyString(serverInfos) &&
					serverInfos.contains(ParameterKeys.ATTRIBUTE_SET_SEPARATOR)){
				String[] serverInfo = serverInfos.split("\\" + ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
				returnValue = true;
				AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
				for (String connection : serverInfo[1].split("[,;\n]")) {
					if (!Is.emptyString(connection.trim())) {
						if (testConnection(connection.trim())) {
							tableModel.addRow(new Object[]{connection.trim()});
						}
					}
				}
				if (Is.emptyString(fldSchema.getText())) {
					String[] company = serverInfo[0].split("[.;\n]");
					if (company.length > 0) {
						if (company.length == 0) {
							fldSchema.setText(extractSchema(company[0]));
						} else {
							String selectedCompany = (String)JOptionPane.showInputDialog(null, 
									InstallerBundle.getMessage("info.schemas.select"), 
									InstallerBundle.getLabel("lblSchema.text"),
									JOptionPane.PLAIN_MESSAGE,
									null,
									company,
									"");
							if (selectedCompany != null) {
								fldSchema.setText(extractSchema(selectedCompany));
							}
						}
					}
					
				}
			}
		} catch (URISyntaxException e) {
			LOG.error(e); // just log it.
		}
		return returnValue;
	}
	
	/**
	 * Extract company schema from the company information.
	 * @param company Company information.
	 * @return Company schema.
	 */
	private String extractSchema(String company) {
		String returnValue = company.contains(ParameterKeys.ATTRIBUTE_SET_SEPARATOR)
				? company.substring(0, company.indexOf(ParameterKeys.ATTRIBUTE_SET_SEPARATOR))
				: company;
		return returnValue;
	}
	/**
	 * Deletes a connection.
	 * @param event Click event.
	 */
	private void deleteConnection(ActionEvent event) {
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		for (int index = 1 ; index <= fldInternetAddresses.getSelectedRowCount(); index++) {
			int selectedIndex = fldInternetAddresses.getSelectedRowCount() - index;
			int row = fldInternetAddresses.getSelectedRows()[selectedIndex];
			tableModel.removeRow(row);
		}
	}
		
	/**
	 * Loads the properties according to the current selected item.
	 * @throws IOException
	 */
	private void loadProperties(String fiscalPrinterId) throws IOException {
		if (!Is.emptyString(fiscalPrinterId)) {
			fldSchema.setText(InstallerProperties.getGroups());
			AddressesTableModel tableModel = (AddressesTableModel) fldInternetAddresses.getModel();
			tableModel.removeAll();
			if (!Is.emptyString(InstallerProperties.getConnections())) {
				for (String internetAddress : InstallerProperties.getConnections().split("[,;]")) {
					tableModel.addRow(new Object[]{internetAddress});
				}
			}
		
		} else {
			clear();
		}
		setChangedState(false);
	}
	
	/**
	 * Sets the text for the given locale
	 * @param locale
	 */
	private void localize() {
		InstallerBundle.resetLocale();
		lblId.setText(InstallerBundle.getLabel("lblId.text"));
		lblDescription.setText(InstallerBundle.getLabel("lblDescription.text"));
		lblSchema.setText(InstallerBundle.getLabel("lblSchema.text"));
		btnTestInternetAddresses.setText(InstallerBundle.getLabel("btnTest.text"));
		btnSave.setText(InstallerBundle.getLabel("btnSave.text"));
		btnInstall.setText(InstallerBundle.getLabel("btnInstall.text"));
		btnReset.setText(InstallerBundle.getLabel("btnReset.text"));
		fldInternetAddresses.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblInternetAddresses.text"));
		fldInternetAddresses.getColumnModel().getColumn(1).setHeaderValue(InstallerBundle.getLabel("lblInternetAddressStatus.text"));
	}
	
	/**
	 * Saves the current information, after validation.
	 * @param event Click event.
	 * @return True if it was properly saved.
	 */
	private boolean save(ActionEvent event) {
		if (fldId.getSelectedItem() == null ||
				Is.emptyString(fldId.getSelectedItem().toString().trim())) {
			JOptionPane.showMessageDialog(null, InstallerBundle.getMessage("error.no_id"));
			return false;
		}
		return save(fldId.getSelectedItem().toString());
	}
	
	/**
	 * Saves the current information, after validation.
	 * @return True if it was properly saved.
	 */
	private boolean save(String fiscalPrinterId) {
		if (validate(fiscalPrinterId)) {
			String message = null;
			Properties properties = new Properties();
			// Save description
			String description = fldDescription.getText();
			if (description == null) {
				description = "";
			}
			properties.setProperty(Constants.PROPERTY_DESCRIPTION, description);
			
			// Save schema
			String schema = fldSchema.getText();
			if (schema == null) {
				schema = "";
			}
			properties.setProperty(Constants.PROPERTY_GROUP_ID, schema);
			
			// Save Addresses
			StringBuffer addresses = new StringBuffer("");
			AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
			for (int row = 0; row < tableModel.getRowCount(); row++) {
				String address = (String) tableModel.getValueAt(row, 0);
				if (addresses.length() > 0) {
					addresses.append(';');
				}
				addresses.append(address.trim());
			}
			properties.setProperty(Constants.PROPERTY_INTERNET_ADDRESSES, addresses.toString());
			
			try {
				if (!Is.emptyString(fiscalPrinterId)) {
					properties.setProperty(Constants.PROPERTY_ID, fiscalPrinterId);
					File propertiesFile = FiscalPrinterUtils.INSTANCE.getFiscalPrinterProperty(fiscalPrinterId);
					if (propertiesFile.exists()) {
						message = InstallerBundle.getMessage("info.printer_updated_successfully", fiscalPrinterId);
					} else {
						message = InstallerBundle.getMessage("info.printer_created_successfully", fiscalPrinterId);
					}
					OutputStream outputStream = new FileOutputStream(propertiesFile);
					properties.store(outputStream, new Date().toString());
					outputStream.flush();
					outputStream.close();
					JOptionPane.showMessageDialog(null, message);
					setChangedState(false);
					reloadIds();
				}
			} catch (FileNotFoundException e) {
				LOG.error(e.getMessage());
				handleException(e);
			} catch (IOException e) {
				LOG.error(e.getMessage());
				handleException(e);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Installs the fiscal printer.
	 * @param event Click event.
	 * Saves the values, register the service, and start the service.
	 */
	private void install(ActionEvent event) {
		if (fldId.getSelectedItem() == null ||
				Is.emptyString(fldId.getSelectedItem().toString().trim())) {
			JOptionPane.showMessageDialog(null, InstallerBundle.getMessage("error.no_id"));
		}
		install();
	}

	/**
	 * Installs the fiscal printer.
	 * Saves the values, register the service, and start the service.
	 */
	private void install() {
		if (save(fldId.getSelectedItem().toString())) {
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	/**
	 * Clears the current data.
	 */
	private void clear() {
		fldId.setSelectedItem("");
		fldDescription.setText("");
		fldSchema.setText("");
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		tableModel.removeAll();
	}
	
	/**
	 * Validates the data.
	 * @return True if all pertinent data is valid. False otherwise.
	 */
	private boolean validate(String fiscalPrinterId) {
		String id = fiscalPrinterId != null ? fiscalPrinterId : null;
		if (id == null ||
				Is.emptyString(id.toString())) {
			return false;
		}
		// validates the addresses
		StringBuffer invalidAddresses = new StringBuffer("");
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			String address = (String) tableModel.getValueAt(row, 0);
			if (!FiscalPrinterUtils.INSTANCE.validateAddress(address)) {
				tableModel.setValueAt(AddressStatus.NOT_VALID, row, 1);
				if (invalidAddresses.length() > 0) {
					invalidAddresses.append('\n');
				}
				invalidAddresses.append(InstallerBundle.getMessage("error.internet_address_invalid", address));
			}
		}
		if (invalidAddresses.length() > 0) {
			int ask = JOptionPane.showConfirmDialog(null, invalidAddresses.toString() +
					InstallerBundle.getMessage("warning.continue.text"),
					InstallerBundle.getMessage("warning.continue.title"), 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (ask == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Tests all connections.
	 */
	private void testConnections() {
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			String address = (String) tableModel.getValueAt(row, 0);
			tableModel.setValueAt(AddressStatus.TESTING, row, 1);
			if (!FiscalPrinterUtils.INSTANCE.validateAddress(address)) {
				tableModel.setValueAt(AddressStatus.NOT_VALID, row, 1);
			} else {
				boolean status = testConnection(address);
				tableModel.setValueAt(status ? AddressStatus.OKEY : AddressStatus.FAILED, row, 1);
			}
		}
	}
	
	/**
	 * Test the connectivity to the given address.
	 * @param address Address to test the connection to.
	 * @return True if connection was successful, false otherwise.
	 */
	private boolean testConnection(String address) {
		boolean returnValue = false;
		String testString = readCommandConnection(address, RemoteDataCommand.TEST);
		if (Constants.TEST_STRING.equalsIgnoreCase(testString)) {
			returnValue = true;
		}

		return returnValue;
	}
	
	/**
	 * Reads the connection's content
	 * @param address Address to read the content from.
	 * @param command Command to use for the connection.
	 * @return Webpage content, null value if invalid connection.
	 */
	private String readCommandConnection(String address) {
		String returnValue = null;
		try {
			URL url = new URL(RemoteFiscalPrinterWrapper.getConnectionString(address) 
					+ "?"
					+ RemoteDataCommand.PARAMETER_COMMAND
					+ RemoteDataCommand.PARAMETER_SEPARATOR
					+ command.ordinal());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(1000);
			Object content = connection.getContent();

			if (content != null && content instanceof InputStream) {
				BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)content));
				returnValue = reader.readLine();
			}
		} catch (MalformedURLException e) {
			returnValue = null;
		} catch (IOException e) {
			returnValue = null;
		}
		return returnValue;
	}
	
	/**
	 * Properly handles a give exception.
	 * @param e Exception to handle.
	 */
	private void handleException(Exception e) {
		String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
		JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Changes the state of the current printer definition.
	 * @param state New state to set.
	 */
	private void setChangedState(boolean state) {
		if (!changingId) {
			changed = state;
		}
		chkChangedState.setSelected(changed);
	}
	
	/**
	 * Renders the splash screen.
	 */
	private void splashScreen() {
		final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            System.out.println("SplashScreen.getSplashScreen() returned null");
            return;
        }
        Graphics2D g = splash.createGraphics();
        if (g == null) {
            System.out.println("g is null");
            return;
        }
        g.drawString("Hello World", 0, 0);
        splash.update();
	}
}
