package net.sf.wubiq.clients;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.enums.AddressStatus;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.InstallerBundle;
import net.sf.wubiq.utils.InstallerProperties;
import net.sf.wubiq.utils.InstallerUtils;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import net.sf.wubiq.utils.WebUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the configuration of remote clients.
 * @author Federico Alcantara
 *
 */
public class WubiqConfigurator {
	private static final Log LOG = LogFactory.getLog(WubiqConfigurator.class);
	
	private String serviceName;
	private JFrame frame;
	private JPanel contentPane;
	private JLabel lblUuid;
	private JTextField fldUuid;
	private JLabel lblKeepAlive;
	private JCheckBox chkKeepAlive;
	private JLabel lblSuppressLogs;
	private JCheckBox chkSuppressLogs;
	private JButton btnSave;
	private JButton btnEsFlag;
	private JButton btnEnFlag;
	private JButton btnTestInternetAddresses;
	private JButton btnReset;
	private JScrollPane scrollPaneAddresses;
	private JButton btnAddAddress;
	private JButton btnDeleteAddress;
	private JTable fldInternetAddresses;
	private JSplitPane addressSplitPane;
	private boolean changed;
	private boolean changingId;
	private JLabel lblChangedState;
	private JCheckBox chkChangedState;
	private final String[] TEST_PORTS = new String[]{"", ":8090", ":8080", ":8181"};
	private final Pattern URI_PORT = Pattern.compile(".+\\:\\d{1,6}.*");
	private TableModelListener tableListener;
	private JTabbedPane tabbedPane;
	private JScrollPane scrollPaneGroups;
	private JPanel general;
	private JTable fldGroups;
	private JSplitPane groupsSplitPane;
	private JButton btnAddGroup;
	private JButton btnDeleteGroup;
	private JPanel clientParameters;
	private JLabel lblApplicationName;
	private JTextField fldApplicationName;
	private JLabel lblServletName;
	private JTextField fldServletName;
	private JCheckBox fldEnableVerbosedLog;
	private JSpinner fldLogLevel;
	private JLabel lblLogLevel;
	private JPanel printers;
	private JScrollPane scrollPanePhotoPrinters;
	private JTable fldPhotoPrinters;
	private JSplitPane photoPrintersSplitPane;
	private JScrollPane scrollPaneDmPrinters;
	private JTable fldDmPrinters;
	private JSplitPane dmPrintersSplitPane;
	private JButton btnAddDmPrinter;
	private JButton btnDeleteDmPrinter;
	private JButton btnAddPhotoPrinter;
	private JButton btnDeletePhotoPrinter;
	private JScrollPane scrollPaneDmHqPrinters;
	private JTable fldDmHqPrinters;
	private JSplitPane dmPrintersHqSplitPane;
	private JButton btnAddDmHq;
	private JButton btnDeleteDmHq;
	private JLabel lblDmDefaultFont;
	private JComboBox fldDmDefaultFont;
	private JSplitPane flagsSplitPane;
	private JPanel advanced;
	private JLabel lblPollInterval;
	private JFormattedTextField fldPollInterval;
	private JLabel lblPrintJobWait;
	private JFormattedTextField fldPrintJobWait;
	private JLabel lblAdditionalJvmParameters;
	private JTextField fldAdditionalJvmParameters;
	private JCheckBox fldForceLogicalFonts;
	private JSplitPane saveSplitPane;
	private JButton btnSaveAndExit;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		final String serviceName;
		if (args.length > 1) {
			serviceName = args[0];
		} else {
			serviceName = "";
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WubiqConfigurator window = new WubiqConfigurator(serviceName);
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
	public WubiqConfigurator(String serviceName) throws IOException {
		this.serviceName = serviceName;
		initialize();
	}

	public JFrame getFrame() {
		return frame;
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws IOException {
		tableListener = 
				new TableModelListener(){
					@Override
					public void tableChanged(TableModelEvent event) {
						if (TableModelEvent.DELETE == event.getType() ||
								TableModelEvent.INSERT == event.getType() ||
								TableModelEvent.UPDATE == event.getType()) {
							setChangedState(true);
						}
					}
				};
		
		frame = new JFrame();
		frame.setName("frame");
		frame.setTitle("Version:" + Labels.VERSION);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setBounds(100, 100, 628, 493);
		contentPane = new JPanel();
		contentPane.setName("contentPane");
		contentPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				addressSplitPane.setDividerLocation(0.5d);
				groupsSplitPane.setDividerLocation(0.5d);
				photoPrintersSplitPane.setDividerLocation(0.5d);
				dmPrintersSplitPane.setDividerLocation(0.5d);
				dmPrintersHqSplitPane.setDividerLocation(0.5d);
				saveSplitPane.setDividerLocation(0.5d);
			}
		});
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][grow][]", "[][grow][grow][grow][grow][grow]"));
		
		lblChangedState = new JLabel("Changed State");
		lblChangedState.setName("lblChangedState");
		lblChangedState.setFont(new Font("Tahoma", Font.ITALIC, 8));

		contentPane.add(lblChangedState);

		chkChangedState = new JCheckBox("");
		chkChangedState.setName("chkChangedState");
		chkChangedState.setEnabled(false);
		lblChangedState.setLabelFor(chkChangedState);

		contentPane.add(chkChangedState, "cell 0 0,alignx left,aligny center");
		
		flagsSplitPane = new JSplitPane();
		flagsSplitPane.setName("flagsSplitPane");
		contentPane.add(flagsSplitPane, "cell 2 0,alignx right,growy");
		
		btnEsFlag = new JButton("");
		btnEsFlag.setName("btnEsFlag");
		flagsSplitPane.setLeftComponent(btnEsFlag);
		btnEsFlag.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/es.png")));
		
		btnEnFlag = new JButton("");
		btnEnFlag.setName("btnEnFlag");
		flagsSplitPane.setRightComponent(btnEnFlag);
		btnEnFlag.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/en.png")));
	
		btnEnFlag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Locale.setDefault(Locale.ENGLISH);
				localize();
			}
			
		});
		
		btnEsFlag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Locale.setDefault(new Locale("es", "DO"));
				localize();
			}
			
		});
		
		lblUuid = new JLabel("Group Id (Schema)");
		lblUuid.setName("lblUuid");
		contentPane.add(lblUuid, "cell 0 1,alignx trailing");
		
		fldUuid = new JTextField();
		fldUuid.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent evt) {
				fldUuid.setText(InstallerUtils.INSTANCE.cleanString(fldUuid.getText()));
			}
		});
		fldUuid.setName("fldUuid");
		lblUuid.setLabelFor(fldUuid);
		contentPane.add(fldUuid, "cell 1 1,growx,alignx,aligny center");
		fldUuid.setColumns(10);
		fldUuid.getDocument().addDocumentListener(new DocumentListener() {
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

		btnReset = new JButton("Reset");
		btnReset.setName("btnReset");
		contentPane.add(btnReset, "cell 2 1,growx,aligny center");
		btnReset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				resetData(event);
			}
		});
		
        lblKeepAlive = new JLabel("Keep Alive");
        lblKeepAlive.setName("lblKeepAlive");
        contentPane.add(lblKeepAlive, "cell 0 2,alignx right,aligny center");

        chkKeepAlive = new JCheckBox();
        chkKeepAlive.setName("chkKeepAlive");
        contentPane.add(chkKeepAlive, "cell 1 2,alignx left,aligny center");

		lblSuppressLogs = new JLabel("Suppress Logs");
        lblSuppressLogs.setName("lblSuppressLogs");
        contentPane.add(lblSuppressLogs, "cell 0 3,alignx right,aligny center");

        chkSuppressLogs = new JCheckBox();
        chkSuppressLogs.setName("chkSuppressLogs");
        contentPane.add(chkSuppressLogs, "cell 1 3,alignx left,aligny center");

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setName("tabbedPane");
		contentPane.add(tabbedPane, "cell 0 4 3 1,growx");
		
		general = new JPanel();
		general.setName("general");
		tabbedPane.addTab("General", null, general, null);
		
		// Tab General
		tabbedPane.setTitleAt(0, InstallerBundle.getLabel("tab.general.text"));
		general.setLayout(new MigLayout("", "[454px][77px]", "[420px][420px][29px]"));
		
		scrollPaneGroups = new JScrollPane();
		scrollPaneGroups.setName("scrollPaneGroups");
		general.add(scrollPaneGroups, "cell 0 0,growx,aligny top");
		
		fldGroups = new JTable(new TextStringTableModel("groups"));
		fldGroups.setName("fldGroups");
		fldGroups.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fldGroups.getColumnModel().getColumn(0).setResizable(true);
		fldGroups.getColumnModel().getColumn(0).setPreferredWidth(400);
		fldGroups.getColumnModel().getColumn(0).setMaxWidth(12312312);
		fldGroups.setCellSelectionEnabled(true);
		fldGroups.getModel().addTableModelListener(tableListener);
		scrollPaneGroups.setViewportView(fldGroups);
		
		groupsSplitPane = new JSplitPane();
		groupsSplitPane.setName("groupsSplitPane");
		general.add(groupsSplitPane, "cell 1 0,alignx left,aligny center");
		
		btnAddGroup = new JButton("");
		btnAddGroup.setName("btnAddGroup");
		btnAddGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText(fldGroups);
			}
		});
		btnAddGroup.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/plus.png")));
		groupsSplitPane.setLeftComponent(btnAddGroup);
		
		btnDeleteGroup = new JButton("");
		btnDeleteGroup.setName("btnDeleteGroup");
		
		btnDeleteGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRow(fldGroups);
			}
		});
		btnDeleteGroup.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/minus.png")));
		groupsSplitPane.setRightComponent(btnDeleteGroup);
		
				scrollPaneAddresses = new JScrollPane();
				scrollPaneAddresses.setName("scrollPaneAddresses");
				general.add(scrollPaneAddresses, "cell 0 1,growx,aligny top");
				
				fldInternetAddresses = new JTable(new AddressesTableModel("host"));
				fldInternetAddresses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				fldInternetAddresses.setName("fldInternetAddresses");
				fldInternetAddresses.getColumnModel().getColumn(0).setResizable(true);
				fldInternetAddresses.getColumnModel().getColumn(0).setPreferredWidth(300);
				fldInternetAddresses.getColumnModel().getColumn(0).setMaxWidth(12312312);
				fldInternetAddresses.getColumnModel().getColumn(1).setMaxWidth(90);
				fldInternetAddresses.setCellSelectionEnabled(true);
				fldInternetAddresses.getModel().addTableModelListener(tableListener);
				scrollPaneAddresses.setViewportView(fldInternetAddresses);
				
				addressSplitPane = new JSplitPane();
				addressSplitPane.setName("addressSplitPane");
				general.add(addressSplitPane, "cell 1 1,alignx left,aligny center");
				
						btnTestInternetAddresses = new JButton("Test");
						btnTestInternetAddresses.setName("btnTestInternetAddresses");
						btnTestInternetAddresses.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								testConnections();
							}
						});
						general.add(btnTestInternetAddresses, "cell 0 2,alignx center,aligny top");
						
								btnAddAddress = new JButton("");
								btnAddAddress.setName("btnAddAddress");
								btnAddAddress.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/plus.png")));
								btnAddAddress.addActionListener(new ActionListener(){
									@Override
									public void actionPerformed(ActionEvent event) {
										addConnection(event);
									}
								});
								addressSplitPane.setLeftComponent(btnAddAddress);
								
										btnDeleteAddress = new JButton("");
										btnDeleteAddress.setName("btnDeleteAddress");
										btnDeleteAddress.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/minus.png")));
										btnDeleteAddress.addActionListener(new ActionListener(){
											@Override
											public void actionPerformed(ActionEvent event) {
												deleteRow(fldInternetAddresses);
											}
										});
										addressSplitPane.setRightComponent(btnDeleteAddress);
		
		clientParameters = new JPanel();
		clientParameters.setName("clientParameters");
		tabbedPane.addTab("Client Parameters", null, clientParameters, null);
		clientParameters.setLayout(new MigLayout("", "[][grow]", "[][][][]"));
		
		lblApplicationName = new JLabel("Application Name");
		lblApplicationName.setName("lblApplicationName");
		clientParameters.add(lblApplicationName, "cell 0 0,alignx right");
		
		fldApplicationName = new JTextField();
		fldApplicationName.setName("fldApplicationName");
		fldApplicationName.setText("wubiq-server");
		clientParameters.add(fldApplicationName, "cell 1 0,growx");
		fldApplicationName.setColumns(10);
		
		lblServletName = new JLabel("Servlet Name");
		lblServletName.setName("lblServletName");
		clientParameters.add(lblServletName, "cell 0 1,alignx trailing");
		
		fldServletName = new JTextField();
		fldServletName.setName("fldServletName");
		fldServletName.setText("wubiq.do");
		clientParameters.add(fldServletName, "cell 1 1,growx");
		fldServletName.setColumns(10);
		
		fldEnableVerbosedLog = new JCheckBox("Enable Verbosed Log");
		fldEnableVerbosedLog.setName("fldEnableVerbosedLog");
		clientParameters.add(fldEnableVerbosedLog, "cell 1 2");
		
		lblLogLevel = new JLabel("Log Level");
		lblLogLevel.setName("lblLogLevel");
		clientParameters.add(lblLogLevel, "cell 0 3,alignx right");
		
		fldLogLevel = new JSpinner();
		fldLogLevel.setName("fldLogLevel");
		fldLogLevel.setModel(new SpinnerNumberModel(0, 0, 5, 1));
		clientParameters.add(fldLogLevel, "cell 1 3");
		
		printers = new JPanel();
		printers.setName("printers");
		tabbedPane.addTab("Printers", null, printers, null);
		printers.setLayout(new MigLayout("", "[grow][grow][grow]", "[grow][grow][grow][grow][][][]"));
		
		scrollPanePhotoPrinters = new JScrollPane();
		scrollPanePhotoPrinters.setName("scrollPanePhotoPrinters");
		printers.add(scrollPanePhotoPrinters, "cell 0 0 2 1,grow");
		
		fldPhotoPrinters = new JTable(new TextStringTableModel("printers.photo"));
		fldPhotoPrinters.setName("fldPhotoPrinters");
		fldPhotoPrinters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fldPhotoPrinters.getColumnModel().getColumn(0).setResizable(true);
		fldPhotoPrinters.getColumnModel().getColumn(0).setPreferredWidth(400);
		fldPhotoPrinters.getColumnModel().getColumn(0).setMaxWidth(12312312);
		fldPhotoPrinters.setCellSelectionEnabled(true);
		fldPhotoPrinters.getModel().addTableModelListener(tableListener);
		scrollPanePhotoPrinters.setViewportView(fldPhotoPrinters);
		
		photoPrintersSplitPane = new JSplitPane();
		photoPrintersSplitPane.setName("photoPrintersSplitPane");
		printers.add(photoPrintersSplitPane, "cell 2 0,alignx center,aligny center");
		
		btnAddPhotoPrinter = new JButton("");
		btnAddPhotoPrinter.setName("btnAddPhotoPrinter");
		btnAddPhotoPrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText(fldPhotoPrinters);
			}
		});
		btnAddPhotoPrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/plus.png")));
		photoPrintersSplitPane.setLeftComponent(btnAddPhotoPrinter);
		
		btnDeletePhotoPrinter = new JButton("");
		btnDeletePhotoPrinter.setName("btnDeletePhotoPrinter");
		btnDeletePhotoPrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRow(fldPhotoPrinters);
			}
		});
		btnDeletePhotoPrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/minus.png")));
		photoPrintersSplitPane.setRightComponent(btnDeletePhotoPrinter);
		
		scrollPaneDmPrinters = new JScrollPane();
		scrollPaneDmPrinters.setName("scrollPaneDmPrinters");
		printers.add(scrollPaneDmPrinters, "cell 0 1 2 1,grow");
		
		fldDmPrinters = new JTable(new TextStringTableModel("printers.dot_matrix"));
		fldDmPrinters.setName("fldDmPrinters");
		fldDmPrinters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fldDmPrinters.getColumnModel().getColumn(0).setResizable(true);
		fldDmPrinters.getColumnModel().getColumn(0).setPreferredWidth(400);
		fldDmPrinters.getColumnModel().getColumn(0).setMaxWidth(12312312);
		fldDmPrinters.setCellSelectionEnabled(true);
		fldDmPrinters.getModel().addTableModelListener(tableListener);
		scrollPaneDmPrinters.setViewportView(fldDmPrinters);

		scrollPaneDmHqPrinters = new JScrollPane();
		scrollPaneDmHqPrinters.setName("scrollPaneDmHqPrinters");
		printers.add(scrollPaneDmHqPrinters, "cell 0 2 2 1,grow");

		dmPrintersSplitPane = new JSplitPane();
		dmPrintersSplitPane.setName("dmPrintersSplitPane");
		printers.add(dmPrintersSplitPane, "cell 2 1,alignx center,aligny center");
		
		btnAddDmPrinter = new JButton("");
		btnAddDmPrinter.setName("btnAddDmPrinter");
		btnAddDmPrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText(fldDmPrinters);
			}
		});
		btnAddDmPrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/plus.png")));
		dmPrintersSplitPane.setLeftComponent(btnAddDmPrinter);
		
		btnDeleteDmPrinter = new JButton("");
		btnDeleteDmPrinter.setName("btnDeleteDmPrinter");
		btnDeleteDmPrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRow(fldDmPrinters);
			}
		});
		btnDeleteDmPrinter.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/minus.png")));
		dmPrintersSplitPane.setRightComponent(btnDeleteDmPrinter);
		
		fldDmHqPrinters = new JTable(new TextStringTableModel("printers.dot_matrix_hq"));
		fldDmHqPrinters.setName("fldDmHqPrinters");
		fldDmHqPrinters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fldDmHqPrinters.getColumnModel().getColumn(0).setResizable(true);
		fldDmHqPrinters.getColumnModel().getColumn(0).setPreferredWidth(400);
		fldDmHqPrinters.getColumnModel().getColumn(0).setMaxWidth(12312312);
		fldDmHqPrinters.setCellSelectionEnabled(true);
		fldDmHqPrinters.getModel().addTableModelListener(tableListener);
		scrollPaneDmHqPrinters.setViewportView(fldDmHqPrinters);
		
		dmPrintersHqSplitPane = new JSplitPane();
		dmPrintersHqSplitPane.setName("dmPrintersHqSplitPane");
		printers.add(dmPrintersHqSplitPane, "cell 2 2,alignx center,aligny center");
		
		btnAddDmHq = new JButton("");
		btnAddDmHq.setName("btnAddDmHqPrinter");
		btnAddDmHq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addText(fldDmHqPrinters);
			}
		});
		btnAddDmHq.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/plus.png")));
		dmPrintersHqSplitPane.setLeftComponent(btnAddDmHq);
		
		btnDeleteDmHq = new JButton("");
		btnDeleteDmHq.setName("btnDeleteDmHqPrinter");
		btnDeleteDmHq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRow(fldDmHqPrinters);
			}
		});
		btnDeleteDmHq.setIcon(new ImageIcon(WubiqConfigurator.class.getResource("/net/sf/wubiq/i18n/minus.png")));
		dmPrintersHqSplitPane.setRightComponent(btnDeleteDmHq);
		
		lblDmDefaultFont = new JLabel("Dot Matrix Default Font");
		lblDmDefaultFont.setName("lblDmDefaultFont");
		printers.add(lblDmDefaultFont, "cell 0 3,alignx trailing");
		
		fldDmDefaultFont = new JComboBox<String>();
		fldDmDefaultFont.setName("fldDmDefaultFont");
		fldDmDefaultFont.setModel(new DefaultComboBoxModel<String>(new String[] {"Times New Roman", "Sans Serif", "Serif"}));
		fldDmDefaultFont.setEditable(true);
		printers.add(fldDmDefaultFont, "cell 1 3,growx");
		
		fldForceLogicalFonts = new JCheckBox("Force Logical Fonts");
		fldForceLogicalFonts.setName("fldForceLogicalFonts");
		printers.add(fldForceLogicalFonts, "cell 1 4");
		
		advanced = new JPanel();
		advanced.setName("advanced");
		tabbedPane.addTab("Advanced", null, advanced, null);
		advanced.setLayout(new MigLayout("", "[][grow]", "[][][][grow]"));
		
		lblPollInterval = new JLabel("Poll Interval");
		lblPollInterval.setName("lblPollInterval");
		advanced.add(lblPollInterval, "cell 0 0,alignx trailing");
		
		fldPollInterval = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fldPollInterval.setName("fldPollInterval");
		advanced.add(fldPollInterval, "cell 1 0,growx");
		
		lblPrintJobWait = new JLabel("Print Job Wait Time");
		lblPrintJobWait.setName("lblPrintJobWait");
		advanced.add(lblPrintJobWait, "cell 0 1,alignx trailing");
		
		fldPrintJobWait = new JFormattedTextField();
		fldPrintJobWait.setName("fldPrintJobWait");
		advanced.add(fldPrintJobWait, "cell 1 1,growx");
		
		lblAdditionalJvmParameters = new JLabel("Additional JVM Parameters");
		lblAdditionalJvmParameters.setName("lblAdditionalJvmParameters");
		advanced.add(lblAdditionalJvmParameters, "cell 0 2,alignx trailing");
		
		fldAdditionalJvmParameters = new JTextField();
		fldAdditionalJvmParameters.setName("fldAdditionalJvmParameters");
		advanced.add(fldAdditionalJvmParameters, "cell 1 2,growx");
		fldAdditionalJvmParameters.setColumns(10);
		
		saveSplitPane = new JSplitPane();
		contentPane.add(saveSplitPane, "cell 0 5 3 1,grow");
		
		btnSave = new JButton("Save");
		saveSplitPane.setLeftComponent(btnSave);
		btnSave.setName("btnSave");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save(false);
			}
		});
		
		btnSaveAndExit = new JButton("SaveAndExit");
		saveSplitPane.setRightComponent(btnSaveAndExit);
		btnSaveAndExit.setName("btnSaveAndExit");
		btnSaveAndExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save(true);
			}
		});
		
		saveSplitPane.setDividerLocation(0.5d);
		
		// Custom code
		changed = false;
		changingId = true;
		try {
			splashScreen();
			clear();
			localize();
			loadProperties();
		} finally {
			changingId = false;
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
				loadProperties();
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
				if (uri.getScheme() == null || !writtenHost.contains("://")) {
					uri = new URI("http://" + writtenHost);
				}
				String host = uri.toASCIIString();
				boolean added = false;
				if (!testConnection(host) && !hasPort(host)) {
					String plainHost = host.split(":[0-9]").length > 1 ? host.substring(0, host.lastIndexOf(':')) : host;
					for (String testPort : TEST_PORTS) {
						String connectionString = plainHost + testPort;
						if (testConnection(connectionString)) {
							addUniqueText(tableModel, connectionString);
							added = true;
							break;
						}
					}
				} 
				if (!added){
					if (InstallerUtils.INSTANCE.validateAddress(host)) {
						addUniqueText(tableModel, host);
					} else {
						JOptionPane.showMessageDialog(null, 
								InstallerUtils.INSTANCE.invalidAddressMessage(writtenHost),
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} catch (URISyntaxException e) {
				JOptionPane.showMessageDialog(null, 
						InstallerUtils.INSTANCE.invalidAddressMessage(writtenHost),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Adds a text to the model.
	 * @param event Originating event.
	 * @param tableModel Associated table model.
	 */
	private void addText(JTable table) {
		TextStringTableModel model = (TextStringTableModel) table.getModel();
		String text = (String)JOptionPane.showInputDialog(frame, 
				model.getTextLabel(), 
				model.getTitleLabel(),
				JOptionPane.PLAIN_MESSAGE, null, null, "");
		if (!Is.emptyString(text)) {
			addUniqueText(model, text);
		}
	}
	
	/**
	 * Checks if the connection string has a port in it.
	 * @param connection Connection string.
	 * @return True if the port is defined in the connection.
	 */
	private boolean hasPort(String connection) {
		Matcher match = URI_PORT.matcher(connection);
		return match.matches();
	}
	
	/**
	 * Adds the connection to the table model. Avoids duplicated connections.
	 * @param tableModel AddressesTableModel instance.
	 * @param sentText Connection to be added.
	 */
	private void addUniqueText(BaseTableModel tableModel, String sentText) {
		String text = tableModel.cleanText(sentText);
		boolean found = false;
		for (int index = 0; index < tableModel.getRowCount(); index++) {
			if (text.equals(tableModel.getValueAt(index, 0))) {
				found = true;
				break;
			}
		}
		if (!found) {
			tableModel.addRow(new Object[]{text});
		}
	}
	
	/**
	 * Deletes the texts.
	 * @param event Click event.
	 */
	private void deleteRow(JTable table) {
		DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
		for (int index = table.getSelectedRowCount() - 1 ; index >= 0; index--) {
			int row = table.getSelectedRows()[index];
			tableModel.removeRow(row);
		}
	}
		
	/**
	 * Loads the properties according to the current selected item.
	 * @throws IOException
	 */
	private void loadProperties() throws IOException {
		InstallerProperties.INSTANCE(serviceName).resetProperties();
		
		// Un-tabbed

		fldUuid.setText(InstallerProperties.INSTANCE(serviceName).getUuid());
		
		chkKeepAlive.setSelected(InstallerProperties.INSTANCE(serviceName).isKeepAlive());
		chkSuppressLogs.setSelected(InstallerProperties.INSTANCE(serviceName).isSuppressLogs());

		// Tab General
		loadTableModel(fldGroups, InstallerProperties.INSTANCE(serviceName).getGroups());
		loadTableModel(fldInternetAddresses, InstallerProperties.INSTANCE(serviceName).getConnections());
		
		// Tab Client Parameters
		String applicationName = InstallerProperties.INSTANCE(serviceName).getApplicationName();
		if (!ConfigurationKeys.DEFAULT_APPLICATION_NAME.equals(applicationName)) {
			fldApplicationName.setText(applicationName);
		}
		
		String servletName = InstallerProperties.INSTANCE(serviceName).getServletName();
		if (!ConfigurationKeys.DEFAULT_SERVLET_NAME.equals(servletName)) {
			fldServletName.setText(servletName);
		}
		
		fldEnableVerbosedLog.setSelected(InstallerProperties.INSTANCE(serviceName).isDebugMode());
		fldLogLevel.setValue(InstallerProperties.INSTANCE(serviceName).getDebugLogLevel());
		
		// Tab Printers
		loadTableModel(fldPhotoPrinters, InstallerProperties.INSTANCE(serviceName).getPhotoPrinters());
		loadTableModel(fldDmPrinters, InstallerProperties.INSTANCE(serviceName).getDmPrinters());
		loadTableModel(fldDmHqPrinters, InstallerProperties.INSTANCE(serviceName).getDmHqPrinters());
		fldDmDefaultFont.setSelectedItem(InstallerProperties.INSTANCE(serviceName).getDefaultDmFont());
		
		// Tab Advanced
		Integer pollInterval = InstallerProperties.INSTANCE(serviceName).getPollInterval();
		fldPollInterval.setValue(pollInterval);

		Integer printJobWait = InstallerProperties.INSTANCE(serviceName).getPrintJobWait();
		fldPrintJobWait.setValue(printJobWait);

		fldAdditionalJvmParameters.setText(InstallerProperties.INSTANCE(serviceName).getJvmParameters());
		setChangedState(false);
	}
	
	/**
	 * Loads the table model with a comma separated list of texts.
	 * @param table Table to be filled.
	 * @param readString String to parse.
	 */
	private void loadTableModel(JTable table, String readString) {
		BaseTableModel textModel = (BaseTableModel) table.getModel();
		textModel.removeAll();
		if (!Is.emptyString(readString)) {
			for (String text : readString.split("[,;]")) {
				if (!Is.emptyString(text)) {
					textModel.addRow(new Object[]{text});
				}
			}
		}
	}
	
	/**
	 * Sets the text for the given locale
	 * @param locale
	 */
	private void localize() {
		InstallerBundle.resetLocale();
		// Untabbed
		lblChangedState.setText(InstallerBundle.getLabel("lblChangedState.text"));
		lblUuid.setText(InstallerBundle.getLabel("lblUuid.text"));
		lblKeepAlive.setText(InstallerBundle.getLabel("lblKeepAlive.text"));
		lblSuppressLogs.setText(InstallerBundle.getLabel("lblSuppressLogs.text"));
		btnSave.setText(InstallerBundle.getLabel("btnSave.text"));
		btnSaveAndExit.setText(InstallerBundle.getLabel("btnSaveAndExit.text"));
		btnReset.setText(InstallerBundle.getLabel("btnReset.text"));
		fldGroups.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblGroups.text"));
		fldInternetAddresses.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblInternetAddresses.text"));
		fldInternetAddresses.getColumnModel().getColumn(1).setHeaderValue(InstallerBundle.getLabel("lblInternetAddressStatus.text"));
		btnTestInternetAddresses.setText(InstallerBundle.getLabel("btnTest.text"));
		
		// Tab Client Parameters
		tabbedPane.setTitleAt(1, InstallerBundle.getLabel("tab.client_parameters.text"));
		lblApplicationName.setText(InstallerBundle.getLabel("lblApplicationName.text"));
		lblServletName.setText(InstallerBundle.getLabel("lblServletName.text"));
		fldEnableVerbosedLog.setText(InstallerBundle.getLabel("lblEnableVerbosedLog.text"));
		lblLogLevel.setText(InstallerBundle.getLabel("lblLogLevel.text"));
		
		// Tab Printers
		tabbedPane.setTitleAt(2, InstallerBundle.getLabel("tab.printers.text"));
		fldPhotoPrinters.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblPhotoPrinters.text"));
		fldDmPrinters.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblDmPrinters.text"));
		fldDmHqPrinters.getColumnModel().getColumn(0).setHeaderValue(InstallerBundle.getLabel("lblDmHqPrinters.text"));
		lblDmDefaultFont.setText(InstallerBundle.getLabel("lblDmDefaultFont.text"));
		fldForceLogicalFonts.setText(InstallerBundle.getLabel("lblForceLogicalFonts"));
		
		// Tab Advanced
		tabbedPane.setTitleAt(3, InstallerBundle.getLabel("tab.advanced.text"));
		lblPollInterval.setText(InstallerBundle.getLabel("lblPollInterval"));
		lblPrintJobWait.setText(InstallerBundle.getLabel("lblPrintJobWait"));
		lblAdditionalJvmParameters.setText(InstallerBundle.getLabel("lblAdditionalJvmParameters"));
	}
	
	/**
	 * Saves the current information, after validation.
	 * @return True if it was properly saved.
	 */
	private boolean save(boolean exit) {
		if (validate()) {
			String message = null;
			Properties properties = new Properties();

			// Untabbed
			// Save schema
			save(properties, ConfigurationKeys.PROPERTY_UUID, fldUuid.getText(), "");
			
			// Tab General
			save(properties, ConfigurationKeys.PROPERTY_KEEP_ALIVE, chkKeepAlive.isSelected());
			save(properties, ConfigurationKeys.PROPERTY_SUPPRESS_LOGS, chkSuppressLogs.isSelected());
			save(properties, ConfigurationKeys.PROPERTY_GROUPS, fldGroups);
			save(properties, ConfigurationKeys.PROPERTY_CONNECTIONS, fldInternetAddresses);
			
			// Tab Client Parameters
			save(properties, ConfigurationKeys.PROPERTY_APPLICATION_NAME, fldApplicationName.getText(), ConfigurationKeys.DEFAULT_APPLICATION_NAME);
			save(properties, ConfigurationKeys.PROPERTY_SERVLET_NAME, fldServletName.getText(), ConfigurationKeys.DEFAULT_SERVLET_NAME);
			save(properties, ConfigurationKeys.PROPERTY_DEBUG_ENABLED, fldEnableVerbosedLog.isSelected());
			save(properties, ConfigurationKeys.PROPERTY_DEBUG_LOG_LEVEL, (Integer)fldLogLevel.getValue(), ConfigurationKeys.DEFAULT_LOG_LEVEL);
			
			// Tab Printers
			save(properties, PropertyKeys.WUBIQ_PRINTERS_PHOTO, fldPhotoPrinters);
			save(properties, PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX, fldDmPrinters);
			save(properties, PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ, fldDmHqPrinters);
			save(properties, PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT, (String)fldDmDefaultFont.getSelectedItem(), "");
			save(properties, PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL, fldForceLogicalFonts.isSelected());
			
			// Tab Advanced
			int pollInterval = fldPollInterval.getValue() != null ? Integer.parseInt(fldPollInterval.getValue().toString()) : ConfigurationKeys.DEFAULT_POLL_INTERVAL;
			int printJobWait = fldPrintJobWait.getValue() != null ? Integer.parseInt(fldPrintJobWait.getValue().toString()) : ConfigurationKeys.DEFAULT_PRINT_JOB_WAIT;
			save(properties, ConfigurationKeys.PROPERTY_POLL_INTERVAL, pollInterval, ConfigurationKeys.DEFAULT_POLL_INTERVAL);
			save(properties, ConfigurationKeys.PROPERTY_PRINT_JOB_WAIT, printJobWait, ConfigurationKeys.DEFAULT_PRINT_JOB_WAIT);
			save(properties, ConfigurationKeys.PROPERTY_JVM_PARAMETERS, fldAdditionalJvmParameters.getText(), "");
			
			try {
				File propertiesFile = InstallerProperties.INSTANCE(serviceName).getPropertiesFile();
				if (propertiesFile != null && propertiesFile.exists()) {
					message = InstallerBundle.getMessage("info.wubiq.updated_successfully");
					propertiesFile.delete();
				} else {
					message = InstallerBundle.getMessage("info.wubiq.created_successfully");
				}
				if (propertiesFile == null) {
					propertiesFile = new File("./" + ConfigurationKeys.INSTALLER_PROPERTIES_FILE_NAME + ".properties");
				}
				OutputStream outputStream = new FileOutputStream(propertiesFile);
				properties.store(outputStream, new Date().toString());
				outputStream.flush();
				outputStream.close();
				if (!exit) {
					JOptionPane.showMessageDialog(null, message);
				}
				setChangedState(false);
				InstallerProperties.INSTANCE(serviceName).resetProperties();
				WubiqLauncher.restart(new String[]{serviceName});
				if (exit) {
					System.exit(0);
				}
			} catch (FileNotFoundException e) {
				LOG.error(e.getMessage());
				handleException(e);
			} catch (IOException e) {
				LOG.error(e.getMessage());
				handleException(e);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				handleException(e);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Save the table rows in the properties object.
	 * @param properties Properties container.
	 * @param propertyName Name of the property name.
	 * @param table Table Table to read the rows from.
	 */
	private void save(Properties properties, String propertyName, JTable table) {
		BaseTableModel tableModel = (BaseTableModel) table.getModel();
		StringBuffer text = new StringBuffer("");
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			String data = (String)tableModel.getValueAt(row, 0);
			if (!Is.emptyString(data)) {
				if (text.length() > 0) {
					text.append(',');
				}
				text.append(data);
			}
		}
		save(properties, propertyName, text.toString(), "");
	}
	
	/**
	 * Saves the state to the properties object.
	 * @param properties Properties container.
	 * @param propertyName Name of the property name.
	 * @param value True or false value.
	 */
	private void save(Properties properties, String propertyName, boolean value) {
		if (value) {
			save(properties, propertyName, "true", "false");
		}
	}

	/**
	 * Saves the state to the properties object.
	 * @param properties Properties container.
	 * @param propertyName Name of the property name.
	 * @param value Integer value to save.
	 */
	private void save(Properties properties, String propertyName, Integer value, Integer defaultValue) {
		if (value != null && !value.equals(defaultValue)) {
			save(properties, propertyName, value.toString(), defaultValue.toString());
		}
	}

	/**
	 * Saves the property value. In order to be saved the value must not be blank and must be different than defaultValue.
	 * @param properties Properties container.
	 * @param propertyName Name of the property name.
	 * @param value Value to be saved.
	 * @param defaultValue Filter for avoiding saving default values in the properties object.
	 */
	private void save(Properties properties, String propertyName, String value, String defaultValue) {
		if (!Is.emptyString(value) && !value.equalsIgnoreCase(defaultValue)) {
			properties.put(propertyName, value);
		}
	}

	/**
	 * Clears the current data.
	 */
	private void clear() {
		// Untabbed
		fldUuid.setText("");
		// Tab General
		((TextStringTableModel)fldGroups.getModel()).removeAll();
		((AddressesTableModel)fldInternetAddresses.getModel()).removeAll();
		
		// Tab Client Parameters
		fldApplicationName.setText("");
		fldServletName.setText("");
		fldEnableVerbosedLog.setSelected(false);
		fldLogLevel.setValue(0);
		
		// Tab Printers
		((TextStringTableModel)fldPhotoPrinters.getModel()).removeAll();
		((TextStringTableModel)fldDmPrinters.getModel()).removeAll();
		((TextStringTableModel)fldDmHqPrinters.getModel()).removeAll();
		fldDmDefaultFont.setSelectedIndex(-1);
		fldForceLogicalFonts.setSelected(false);
		
		// Tab Advanced
		fldPollInterval.setValue(0);
		fldPrintJobWait.setValue(0);
		fldAdditionalJvmParameters.setText("");
	}
	
	/**
	 * Validates the data.
	 * @return True if all pertinent data is valid. False otherwise.
	 */
	private boolean validate() {
		// validates UUID
		if (Is.emptyString(fldUuid.getText())) {
			JOptionPane.showMessageDialog(null, 
					InstallerBundle.getMessage("error.uuid.required"),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// validates the addresses
		StringBuffer invalidAddresses = new StringBuffer("");
		AddressesTableModel tableModel = (AddressesTableModel)fldInternetAddresses.getModel();
		boolean empty = true;
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			String address = (String) tableModel.getValueAt(row, 0);
			if (!InstallerUtils.INSTANCE.validateAddress(address)) {
				tableModel.setValueAt(AddressStatus.NOT_VALID, row, 1);
				if (invalidAddresses.length() > 0) {
					invalidAddresses.append('\n');
				}
				invalidAddresses.append(InstallerUtils.INSTANCE.invalidAddressMessage(address));
			} else {
				empty = false;
			}
		}
		if (empty) {
			int ask = JOptionPane.showConfirmDialog(null, InstallerBundle.getMessage("warning.no_connection") +
					InstallerBundle.getMessage("warning.continue.text"),
					InstallerBundle.getMessage("warning.continue.title"), 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			if (ask == JOptionPane.NO_OPTION) {
				return false;
			}
		} else if (invalidAddresses.length() > 0) {
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
			if (!InstallerUtils.INSTANCE.validateAddress(address)) {
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
		try {
			String testString = readCommandConnection(address, CommandKeys.CONNECTION_TEST);
			if (ParameterKeys.CONNECTION_TEST_STRING.equalsIgnoreCase(testString)) {
				returnValue = true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return returnValue;
	}
	
	/**
	 * Reads the connection's content
	 * @param address Address to read the content from.
	 * @param command Command to use for the connection.
	 * @return Webpage content, null value if invalid connection.
	 */
	private String readCommandConnection(String address, String command) {
		String returnValue = null;
		try {
			URL url = hostServletUrl(address);
			String encodedParameters = encodedParameters(command);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			connection.setRequestMethod("POST");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("ContentType", "text/html");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(encodedParameters.getBytes().length));
			connection.setConnectTimeout(1000);
			ByteArrayInputStream input = new ByteArrayInputStream(encodedParameters.getBytes());
			connection.setUseCaches (false);
			IOUtils.INSTANCE.copy(input, connection.getOutputStream());
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
	 * Properly forms the url and encode the parameters so that servers can receive them correctly.
	 * @param command Command to be encoded as part of the url.
	 * @param parameters Arrays of parameters in the form parameterName=parameterValue that will be appended to the url.
	 * @return Url string with parameterValues encoded.
	 */
	private String encodedParameters(String command, String... parameters) {
		StringBuffer parametersQuery = new StringBuffer("");
		if (!Is.emptyString(command)) {
			parametersQuery
				.append(ParameterKeys.COMMAND)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(command);
		}
		for (String parameter: parameters) {
			String parameterString = parameter;
			if (parameter.contains("=")) {
				String parameterName = parameter.substring(0, parameter.indexOf("="));
				String parameterValue = parameter.substring(parameter.indexOf("=") + 1);
				parameterValue = WebUtils.INSTANCE.encode(parameterValue);
				parameterString = parameterName + "=" + parameterValue;
			}
			parametersQuery.append('&')
					.append(parameterString);
		}
		return parametersQuery.toString();
	}
	
	/**
	 * Creates a valid url with the given connection.
	 * @param connection Connection to encapsulate.
	 * @return Valid URL or null.
	 */
	private URL hostServletUrl(String connection) {
		URL returnValue = null;
		StringBuffer buffer = new StringBuffer("");
		if (!Is.emptyString(connection)) {
			buffer.append(connection);
		}
		if (!Is.emptyString(InstallerProperties.INSTANCE(serviceName).getApplicationName())) {
			appendWebChar(buffer, '/')
					.append(InstallerProperties.INSTANCE(serviceName).getApplicationName());
		}
		if (!Is.emptyString(InstallerProperties.INSTANCE(serviceName).getServletName())) {
			appendWebChar(buffer, '/')
					.append(InstallerProperties.INSTANCE(serviceName).getServletName());
		}
		if (buffer.length() > 0) {
			try {
				returnValue = new URL(buffer.toString());
			} catch (MalformedURLException e) {
				LOG.error(e.getMessage());
			}
		}
		return returnValue;
	}	
	
	/**
	 * Appends a character to the buffer only if the buffer doesn't end with that character.
	 * @param buffer Buffer to be appended.
	 * @param webChar Character to be appended.
	 * @return The StringBuffer ending with the char.
	 */
	private StringBuffer appendWebChar(StringBuffer buffer, char webChar) {
		if (buffer.length() == 0) {
			buffer.append(webChar);
		} else if (buffer.charAt(buffer.length() - 1) != webChar) {
			buffer.append(webChar);
		}
		return buffer;
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
