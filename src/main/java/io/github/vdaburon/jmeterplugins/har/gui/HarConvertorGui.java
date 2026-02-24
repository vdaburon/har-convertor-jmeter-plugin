/*
 * Copyright 2024 Vincent DABURON
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package io.github.vdaburon.jmeterplugins.har.gui;

import io.github.vdaburon.jmeter.har.HarForJMeter;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class HarConvertorGui extends AbstractAction implements
        ActionListener, UnsharedComponent, MenuCreator, Serializable {

	private static Set<String> commands = new HashSet<>();

    @SuppressWarnings("unused")
	private static final long serialVersionUID = 2433L;

    private static final Logger log = LoggerFactory.getLogger(HarConvertorGui.class);
    private static final String BROWSE_HAR_IN = "BROWSE_HAR_IN";
    private static final String BROWSE_JMX_OUT = "BROWSE_JMX_OUT";
    private static final String BROWSE_RECORD_OUT = "BROWSE_RECORD_OUT";
    private static final String BROWSE_EXTERNAL_FILE_IN = "BROWSE_EXTERNAL_FILE_IN";
    private static final String ACTION_CONVERT = "ACTION_CONVERT";
    private static final String ACTION_CONVERT_AND_LOAD_SCRIPT = "ACTION_CONVERT_LOAD";
    private static final String ACTION_MENU_TOOL = "ACTION_MENU_TOOL";
    private static final String ACTION_CHECKBOX_WEBSOCKET = "ACTION_CHECKBOX_WEBSOCKET";

    private EscapeDialog messageDialog;
    
    private JTextField fileHarInTextField;
    private JTextField fileJmxOutTextField;
    private JTextField fileRecordOutTextField;
    private JTextField externalFileInfoInField;
    private JTextField pauseBetweenUrlTextField;
    private JTextField pageStartNumberTextField;
    private JTextField samplerStartNumberTextField;
    private JButton fileHarInFileButton;
    private JTextField regexFilterIncludeField;
    private JTextField regexFilterExcludeField;
    private JTextField removeHeadersField;
    private JTextField jacksonParserStringMaxField;
    private JButton fileJmxOutFileButton;
    private JButton fileRecordOutFileButton;
    private JButton externalFileInfoInButton;
    private JCheckBox isAddPauseCheckbox;
    private JCheckBox isRemoveCookieCheckbox;
    private JCheckBox isRemoveCacheRequestHeaderCheckbox;
    private JCheckBox isAddResultTreeRecordCheckbox;
    private JCheckBox isWebSocketPDoornboschCheckbox;

    private JButton btConvert;
    private JButton btConvertAndLoad;
    private String lastJFCDirectory;
    private JTextField labelStatus;

    static {
        commands.add(ACTION_MENU_TOOL);
    }
    public HarConvertorGui() {
        super();
        log.debug("Creating HarConvertorGui");
    }

 
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    public void doAction(ActionEvent e) throws IllegalUserActionException {
    	HarConvertorGui harConvertorGui = new HarConvertorGui();
        JFrame jfMainFrame = GuiPackage.getInstance().getMainFrame();
        harConvertorGui.showInputDialog(jfMainFrame);
    }

    public void showInputDialog(JFrame parent) {
        setupInputDialog(parent);
        launchInputDialog();
    }

    private void launchInputDialog() {
        messageDialog.pack();
        ComponentUtil.centerComponentInWindow(messageDialog);
        messageDialog.setVisible(true);
    }

    public void setupInputDialog(JFrame parent) {
        messageDialog = new EscapeDialog(parent, "vdn@github - HAR CONVERTOR TOOL", false);
        setupContentPane();
    }

    private void setupContentPane() {
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout(0,5));

        JPanel mainPanel = new JPanel(new BorderLayout());
        VerticalPanel vertPanel = new VerticalPanel();
        vertPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "HAR Convertor Configuration"));

        vertPanel.add(setupFileChooserPanel());
        vertPanel.add(createFilterAndPausePanel());
        vertPanel.add(createCheckbox());
        vertPanel.add(createControls());
        
        labelStatus = new JTextField("Waiting configuration ... ");
        labelStatus.setEditable(false);
        vertPanel.add(labelStatus);

        mainPanel.add(vertPanel);
        contentPane.add(mainPanel);

    }


	@Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();
        Exception except = null;
 
//        log.info("command=" + command);
        if (command.equals(ACTION_MENU_TOOL)) {
        	try {
				doAction(action);
			} catch (IllegalUserActionException e) {
				e.printStackTrace();
			}
        }
        
        if (command.equals(ACTION_CONVERT) || command.equals(ACTION_CONVERT_AND_LOAD_SCRIPT)) {
            String fileHarIn= fileHarInTextField.getText();
            
            File fFileIn = new File(fileHarIn);
            if (!fFileIn.canRead()) {
            	labelStatus.setText("Tool HAR Convertor Finished KO, CAN'T READ HAR fileHarIn = " + fileHarIn);
            	labelStatus.setForeground(java.awt.Color.RED);
            	return;
            }

            String externalFileInfoIn= externalFileInfoInField.getText();
            if (!externalFileInfoIn.isEmpty()) {
                File fExternalFileInfoIn = new File(externalFileInfoIn);
                if (!fExternalFileInfoIn.canRead()) {
                    labelStatus.setText("Tool HAR Convertor Finished KO, CAN'T READ CSV externalFileInfoIn = " + externalFileInfoIn);
                    labelStatus.setForeground(java.awt.Color.RED);
                    return;
                }
            }

            String fileJmxOut= fileJmxOutTextField.getText();
            String recordXmlOut= fileRecordOutTextField.getText();
            String regexFilterInclude= regexFilterIncludeField.getText();
            String regexFilterExclude= regexFilterExcludeField.getText();
            String removeHeaders = removeHeadersField.getText();
            String jacksonParserStringMax = jacksonParserStringMaxField.getText();

            if (fileJmxOut.trim().isEmpty()) {
            	fileJmxOut = fileHarIn.substring(0,fileHarIn.lastIndexOf(".")) + ".jmx";
            }
            if (regexFilterInclude.trim().isEmpty()) {
            	regexFilterInclude = "";
            }

            boolean isAddPause = isAddPauseCheckbox.isSelected();

            String timeWaitBetweenUrls = pauseBetweenUrlTextField.getText();
            long createNewTransactionAfterRequestMs = 0;
            if (!timeWaitBetweenUrls.isEmpty()) {
                try {
                    createNewTransactionAfterRequestMs = Integer.parseInt(timeWaitBetweenUrls);
                    if (isAddPause && createNewTransactionAfterRequestMs > 0) {
                        isAddPause = true;
                    }
                } catch (Exception ex) {
                    log.warn("Error parsing long parameter " + ", value = " + timeWaitBetweenUrls + ", set to 0");
                    createNewTransactionAfterRequestMs = 0;
                }
            }

            String sPageStartNumber = pageStartNumberTextField.getText();
            int pageStartNumber = 1;
            if (!sPageStartNumber.isEmpty()) {
                try {
                    pageStartNumber = Integer.parseInt(sPageStartNumber);
                } catch (Exception ex) {
                    log.warn("Error parsing int parameter " + ", value = " + sPageStartNumber + ", set to 1");
                    pageStartNumber = 1;
                }
            }
            if (pageStartNumber <= 0) {
                pageStartNumber  = 1;
            }

            String sSamplerStartNumber = samplerStartNumberTextField.getText();
            int samplerStartNumber = 1;
            if (!sSamplerStartNumber.isEmpty()) {
                try {
                    samplerStartNumber = Integer.parseInt(sSamplerStartNumber);
                } catch (Exception ex) {
                    log.warn("Error parsing int parameter " + ", value = " + sSamplerStartNumber + ", set to 1");
                    samplerStartNumber = 1;
                }
            }
            if (samplerStartNumber <= 0) {
                samplerStartNumber  = 1;
            }

            int iJacksonParserStringMax = HarForJMeter.K_JACKSON_PARSER_STRING_MAX_DEFAULT;
            if (!jacksonParserStringMax.isEmpty()) {
                try {
                    iJacksonParserStringMax = Integer.parseInt(jacksonParserStringMax);
                } catch (Exception ex) {
                    log.warn("Error parsing int parameter " + ", value = " + jacksonParserStringMax + ", set to " + HarForJMeter.K_JACKSON_PARSER_STRING_MAX_DEFAULT + " Default value");
                    iJacksonParserStringMax = HarForJMeter.K_JACKSON_PARSER_STRING_MAX_DEFAULT;
                }
            }
            if (iJacksonParserStringMax <= 0) {
                iJacksonParserStringMax  = HarForJMeter.K_JACKSON_PARSER_STRING_MAX_DEFAULT;
            }
            if (iJacksonParserStringMax != HarForJMeter.K_JACKSON_PARSER_STRING_MAX_DEFAULT && iJacksonParserStringMax > 1024) {
                log.info("Set " + HarForJMeter.K_JACKSON_PARSER_STRING_MAX + "=<" + iJacksonParserStringMax + ">");
            }

            boolean isRemoveCookieHeader = isRemoveCookieCheckbox.isSelected();
            boolean isRemoveCacheRequestHeader = isRemoveCacheRequestHeaderCheckbox.isSelected();
            boolean isAddResultTreeRecord = isAddResultTreeRecordCheckbox.isSelected();
            boolean isWebSocketPDoornbosch = isWebSocketPDoornboschCheckbox.isSelected();

            try {
            	btConvert.setEnabled(false);
            	labelStatus.setText("Tool HAR Convert Running");
            	log.info("Before HarForJMeter.generateJmxAndRecord");
                log.info("************* PARAMETERS ***************");
            	log.info("fileHarIn=<"+ fileHarIn + ">");
                log.info("fileJmxOut=<" + fileJmxOut + ">");
                log.info("recordXmlOut=<" + recordXmlOut + ">");
                log.info("isAddResultTreeRecord=<" + isAddResultTreeRecord + ">");
                log.info("isWebSocketPDoornbosch=<" + isWebSocketPDoornbosch + ">");
                log.info("createNewTransactionAfterRequestMs=<" + createNewTransactionAfterRequestMs + ">");
                log.info("isAddPause=<" + isAddPause + ">");
                log.info("regexFilterInclude=<" + regexFilterInclude + ">");
                log.info("regexFilterExclude=<" + regexFilterExclude + ">");
                log.info("removeHeaders=<" + removeHeaders + ">");
                log.info("jacksonParserStringMax=<" + iJacksonParserStringMax + ">");
                log.info("isRemoveCookieHeader=<" + isRemoveCookieHeader + ">");
                log.info("samplerStartNumber=<" + samplerStartNumber + ">");
                log.info("samplerStartNumber=<" + samplerStartNumber + ">");
                log.info("externalFileInfoIn=<" + externalFileInfoIn + ">");
                log.info("****************************************");

                HarForJMeter.generateJmxAndRecord(fileHarIn, iJacksonParserStringMax, fileJmxOut,createNewTransactionAfterRequestMs,isAddPause, isRemoveCookieHeader, isRemoveCacheRequestHeader,
                                                regexFilterInclude, regexFilterExclude, recordXmlOut, pageStartNumber, samplerStartNumber, externalFileInfoIn,
                                                isAddResultTreeRecord, isWebSocketPDoornbosch,removeHeaders);

                log.info("After HarForJMeter.generateJmxAndRecord");
                btConvert.setEnabled(true);
                if (!recordXmlOut.isEmpty()) {
                    labelStatus.setText("Tool HAR Convertor Finished OK, fileJmxOut=" + fileJmxOut + " AND recordXmlOut=" + recordXmlOut);
                } else {
                    labelStatus.setText("Tool HAR Convertor Finished OK, fileJmxOut=" + fileJmxOut);
                }
     	 		labelStatus.setForeground(java.awt.Color.BLACK);

                if (command.equals(ACTION_CONVERT_AND_LOAD_SCRIPT)) {
                    // open the script generated in current JMeter
                    final HashTree tree = SaveService.loadTree(new File(fileJmxOut));
                    org.apache.jmeter.gui.action.Load.insertLoadedTree(1,tree);
                }
             } catch (Exception e) {
                e.printStackTrace();
                log.warn("Tool HAR Convertor Finished KO, exception = " + e);
                except = e;
                btConvert.setEnabled(true);
                btConvertAndLoad.setEnabled(true);
                labelStatus.setText("Tool HAR Convertor Finished KO, exception = " + e);
                labelStatus.setForeground(java.awt.Color.RED);
            }
            
            if (null == except) {
                 btConvert.setEnabled(true);
            }
        }

        if (command.equals(BROWSE_HAR_IN)) {
        	fileHarInTextField.setText(showFileChooser(fileHarInTextField.getParent(),
                    fileHarInTextField, false, new String[] { ".har" }));
        	labelStatus.setText("Waiting configuration ... ");
        	labelStatus.setForeground(java.awt.Color.BLACK);
        }

        if (command.equals(BROWSE_JMX_OUT)) {
        	fileJmxOutTextField.setText(showFileChooser(fileJmxOutTextField.getParent(),
                    fileJmxOutTextField, false, new String[] { ".jmx" }));
        	labelStatus.setText("Waiting configuration ... ");
        	labelStatus.setForeground(java.awt.Color.BLACK);
        }

        if (command.equals(BROWSE_RECORD_OUT)) {
            fileRecordOutTextField.setText(showFileChooser(fileRecordOutTextField.getParent(),
                    fileRecordOutTextField, false, new String[] { ".xml" }));
            labelStatus.setText("Waiting configuration ... ");
            labelStatus.setForeground(java.awt.Color.BLACK);
        }

        if (command.equals(BROWSE_EXTERNAL_FILE_IN)) {
            externalFileInfoInField.setText(showFileChooser(externalFileInfoInField.getParent(),
                    externalFileInfoInField, false, new String[] { ".csv",".json" }));
            labelStatus.setText("Waiting configuration ... ");
            labelStatus.setForeground(java.awt.Color.BLACK);
        }

        if (command.equals(ACTION_CHECKBOX_WEBSOCKET)) {
            boolean isWSChecked = isWebSocketPDoornboschCheckbox.isSelected();
            if (isWSChecked) {
                try {
                    // check if plugin "WebSocket Samplers by Peter Doornbosch" is present
                    Class.forName("eu.luminis.jmeter.wssampler.WebsocketGeneralSampler");
                    log.info("plugin \"WebSocket Samplers by Peter Doornbosch\" is present");
                } catch (ClassNotFoundException ex) {
                    labelStatus.setText("You must install the plugin 'WebSocket Samplers by Peter Doornbosch' to open the JMeter generated script file");
                    labelStatus.setForeground(java.awt.Color.RED);
                    btConvertAndLoad.setEnabled(false);
                }
            } else {
                labelStatus.setText("Waiting configuration ... ");
                labelStatus.setForeground(java.awt.Color.BLACK);
                btConvertAndLoad.setEnabled(true);
            }
        }
    }

    private JPanel createControls() {
    	btConvert = new JButton("CONVERT");
    	btConvert.addActionListener(this);
    	btConvert.setActionCommand(ACTION_CONVERT);
    	btConvert.setEnabled(true);

        btConvertAndLoad = new JButton("CONVERT AND LOAD GENERATED SCRIPT");
        btConvertAndLoad.addActionListener(this);
        btConvertAndLoad.setActionCommand(ACTION_CONVERT_AND_LOAD_SCRIPT);
        btConvertAndLoad.setEnabled(true);

        JPanel panel = new JPanel();
        panel.add(btConvertAndLoad);
        panel.add(btConvert);
        return panel;
    }


    private JPanel createFilterAndPausePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel regexFilterIncludeLabel = new JLabel("(Optional) Regular expression matches for filter Include URL");
        regexFilterIncludeField = new JTextField("", 80);

        JLabel regexFilterExcludeLabel = new JLabel("(Optional) Regular expression matches for filter Exclude URL");
        regexFilterExcludeField = new JTextField("", 80);

        JLabel removeHeadersFieldLabel = new JLabel("(Optional) Remove Headers (comma separator case insensitive)");
        removeHeadersField = new JTextField("", 80);

        JLabel pauseBetweenUrlLabel = new JLabel("(Optional) Time (ms) between 2 URLs to create a new page (Transaction Controller)");
        pauseBetweenUrlTextField = new JTextField("5000", 80);

        JLabel pageStartNumberLabel = new JLabel("(Optional) Page start number usually for partial recording (default 1)");
        pageStartNumberTextField = new JTextField("", 80);

        JLabel samplerStartNumberLabel = new JLabel("(Optional) Sampler start number usually for partial recording (default 1)");
        samplerStartNumberTextField = new JTextField("", 80);

        JLabel jacksonParserStringMaxLabel = new JLabel("(Optional) Change Jackson String length size (default integer size = 20000000) for very large JSON");
        jacksonParserStringMaxField = new JTextField("", 80);

        panel.add(pauseBetweenUrlLabel);
        panel.add(pauseBetweenUrlTextField);

        panel.add(pageStartNumberLabel);
        panel.add(pageStartNumberTextField);

        panel.add(samplerStartNumberLabel);
        panel.add(samplerStartNumberTextField);

        panel.add(regexFilterIncludeLabel);
        panel.add(regexFilterIncludeField);

        panel.add(regexFilterExcludeLabel);
        panel.add(regexFilterExcludeField);

        panel.add(removeHeadersFieldLabel);
        panel.add(removeHeadersField);

        panel.add(jacksonParserStringMaxLabel);
        panel.add(jacksonParserStringMaxField);

        return panel;
    }

    private JPanel createCheckbox() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JLabel isWebSocketPDoornboschLabel = new JLabel("(Optional) Create WebSocket Sampler if HAR contains WebSocket Connection (default false)");
        isWebSocketPDoornboschCheckbox= new JCheckBox("",false);
        isWebSocketPDoornboschCheckbox.addActionListener(this);
        isWebSocketPDoornboschCheckbox.setActionCommand(ACTION_CHECKBOX_WEBSOCKET);
        isWebSocketPDoornboschCheckbox.setEnabled(true);

        JLabel isAddPauseLabel = new JLabel("(Optional) Add a pause between Transaction Controller (default true), time between 2 URLs must be > 0");
        isAddPauseCheckbox= new JCheckBox("",true);

        JLabel isRemoveCookieLabel = new JLabel("(Optional) Remove cookie in http header (default true because add a Cookie Manager)");
        isRemoveCookieCheckbox= new JCheckBox("",true);

        JLabel isRemoveCacheHeaderLabel = new JLabel("(Optional) Remove cache header in the http request (default true because add a Cache Manager)");
        isRemoveCacheRequestHeaderCheckbox= new JCheckBox("",true);

        JLabel isAddResultTreeRecordLabel = new JLabel("(Optional) Add 'View Result Tree' to view the recording xml file created (default true)");
        isAddResultTreeRecordCheckbox= new JCheckBox("",true);

        panel.add(isWebSocketPDoornboschLabel);
        panel.add(isWebSocketPDoornboschCheckbox);

        panel.add(isAddPauseLabel);
        panel.add(isAddPauseCheckbox);

        panel.add(isRemoveCookieLabel);
        panel.add(isRemoveCookieCheckbox);

        panel.add(isRemoveCacheHeaderLabel);
        panel.add(isRemoveCacheRequestHeaderCheckbox);

        panel.add(isAddResultTreeRecordLabel);
        panel.add(isAddResultTreeRecordCheckbox);

        return panel;
    }

    private JPanel setupFileChooserPanel() {
        JPanel fileChooserPanel = new JPanel(new GridLayout(0, 3));
        fileChooserPanel.add(new JLabel("HAR file in (to read) : "));

        fileHarInTextField = new JTextField();
        fileChooserPanel.add(fileHarInTextField);

        this.fileHarInFileButton = new JButton("Browse ...");
        fileHarInFileButton.setActionCommand(BROWSE_HAR_IN);
        fileHarInFileButton.addActionListener(this);
        fileChooserPanel.add(fileHarInFileButton);

        fileChooserPanel.add(new JLabel("JMeter script out (to write) (if empty default <har_in_no_extension>.jmx) : "));

        fileJmxOutTextField = new JTextField();
        fileChooserPanel.add(fileJmxOutTextField);

        this.fileJmxOutFileButton = new JButton("Browse ...");
        fileJmxOutFileButton.setActionCommand(BROWSE_JMX_OUT);
        fileJmxOutFileButton.addActionListener(this);
        fileChooserPanel.add(fileJmxOutFileButton);


        fileChooserPanel.add(new JLabel("(Optional) Record XML out (to write) : "));
        fileRecordOutTextField = new JTextField();
        fileChooserPanel.add(fileRecordOutTextField);

        this.fileRecordOutFileButton = new JButton("Browse ...");
        fileRecordOutFileButton.setActionCommand(BROWSE_RECORD_OUT);
        fileRecordOutFileButton.addActionListener(this);
        fileChooserPanel.add(fileRecordOutFileButton);

        fileChooserPanel.add(new JLabel("(Optional) External csv file or json file with transaction info (to read) : "));
        externalFileInfoInField= new JTextField();
        fileChooserPanel.add(externalFileInfoInField);

        this.externalFileInfoInButton = new JButton("Browse ...");
        externalFileInfoInButton.setActionCommand(BROWSE_EXTERNAL_FILE_IN);
        externalFileInfoInButton.addActionListener(this);
        fileChooserPanel.add(externalFileInfoInButton);

        return fileChooserPanel;
    }
    
    /**
     * Show a file chooser to the user
     *
     * @param locationTextField
     *            the textField that will receive the path
     * @param onlyDirectory
     *            whether or not the file chooser will only display directories or Files only
     * @param extensions File extensions to filter
     * @return the path the user selected or, if the user cancelled the file
     *         chooser, the previous path
     */
    private String showFileChooser(Component component, JTextField locationTextField, boolean onlyDirectory, String[] extensions) {
        JFileChooser jfc = new JFileChooser();
        if (onlyDirectory) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        if(extensions != null && extensions.length > 0) {
            JMeterFileFilter currentFilter = new JMeterFileFilter(extensions);
            jfc.addChoosableFileFilter(currentFilter);
            jfc.setAcceptAllFileFilterUsed(true);
            jfc.setFileFilter(currentFilter);
        }
        if (lastJFCDirectory != null) {
            jfc.setCurrentDirectory(new File(lastJFCDirectory));
        } else {
            String start = System.getProperty("user.dir", ""); //$NON-NLS-1$//$NON-NLS-2$
            if (!start.isEmpty()) {
                jfc.setCurrentDirectory(new File(start));
            }
        }
        int retVal = jfc.showOpenDialog(component);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
            return jfc.getSelectedFile().getPath();
        } else {
            return locationTextField.getText();
        }
    }

	@Override
	public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
	      if (location != MENU_LOCATION.TOOLS) {
	            return new JMenuItem[0];
	        }

	        JMenuItem menuItem = new JMenuItem("vdn@github - HAR Convertor Tool", null);
	        menuItem.setName("HAR Convertor Tool");
	        menuItem.setActionCommand(ACTION_MENU_TOOL);
	        menuItem.setAccelerator(null);
	        menuItem.addActionListener(this);
	        return new JMenuItem[] { menuItem };
	}

	@Override
	public JMenu[] getTopLevelMenus() {
		return new JMenu[0];
	}

	@Override
	public void localeChanged() {
		// NOOP
		
	}

	@Override
	public boolean localeChanged(MenuElement arg0) {
		return false;
	}


	@Override
	public Set<String> getActionNames() {
		return commands;
	}
}
