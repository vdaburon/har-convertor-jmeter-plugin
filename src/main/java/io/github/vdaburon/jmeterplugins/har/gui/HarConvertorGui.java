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
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class HarConvertorGui extends AbstractAction implements
        ActionListener, UnsharedComponent, MenuCreator {
	private static Set<String> commands = new HashSet<>();

    @SuppressWarnings("unused")
	private static final long serialVersionUID = 2433L;

    private static final Logger log = LoggerFactory.getLogger(HarConvertorGui.class);
    private static final String BROWSE_HAR_IN = "BROWSE_HAR_IN";
    private static final String BROWSE_JMX_OUT = "BROWSE_JMX_OUT";
    private static final String BROWSE_RECORD_OUT = "BROWSE_RECORD_OUT";
    private static final String ACTION_CONVERT = "ACTION_CONVERT";
    private static final String ACTION_MENU_TOOL = "ACTION_MENU_TOOL"; 

    private EscapeDialog messageDialog;
    
    private JTextField fileHarInTextField;
    private JTextField fileJmxOutTextField;
    private JTextField fileRecordOutTextField;
    private JTextField pauseBetweenUrlTextField;
    private JButton fileHarInFileButton;
    private JTextField regexFilterIncludeField;
    private JTextField regexFilterExcludeField;
    private JButton fileJmxOutFileButton;
    private JButton fileRecordOutFileButton;
    private JCheckBox isAddPauseCheckbox;
    private JCheckBox isRemoveCookieCheckbox;
    private JCheckBox isRemoveCacheRequestHeaderCheckbox;

    private JButton btConvert;
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
        
        if (command.equals(ACTION_CONVERT)) {
            String fileHarIn= fileHarInTextField.getText();
            
            File fFileIn = new File(fileHarIn);
            if (!fFileIn.canRead()) {
            	labelStatus.setText("Tool HAR Convertor Finished KO, CAN'T READ HAR fileHarIn = " + fileHarIn);
            	labelStatus.setForeground(java.awt.Color.RED);
            	return;
            }
            String fileJmxOut= fileJmxOutTextField.getText();
            String recordXmlOut= fileRecordOutTextField.getText();
            String regexFilterInclude= regexFilterIncludeField.getText();
            String regexFilterExclude= regexFilterExcludeField.getText();
            
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

            boolean isRemoveCookieHeader = isRemoveCookieCheckbox.isSelected();
            boolean isRemoveCacheRequestHeader = isRemoveCacheRequestHeaderCheckbox.isSelected();

            try {
            	btConvert.setEnabled(false);
            	labelStatus.setText("Tool HAR Convert Running");
            	log.info("Before HarForJMeter.generateJmxAndRecord");
                log.info("************* PARAMETERS ***************");
            	log.info("fileHarIn=<"+ fileHarIn + ">");
                log.info("fileJmxOut=<" + fileJmxOut + ">");
                log.info("recordXmlOut=<" + recordXmlOut + ">");
                log.info("createNewTransactionAfterRequestMs=<" + createNewTransactionAfterRequestMs + ">");
                log.info("isAddPause=<" + isAddPause + ">");
                log.info("regexFilterInclude=<" + regexFilterInclude + ">");
                log.info("regexFilterExclude=<" + regexFilterExclude + ">");
                log.info("isRemoveCookieHeader=<" + isRemoveCookieHeader + ">");
                log.info("isRemoveCacheRequestHeader=<" + isRemoveCacheRequestHeader + ">");
                log.info("****************************************");

                HarForJMeter.generateJmxAndRecord(fileHarIn, fileJmxOut,createNewTransactionAfterRequestMs,isAddPause, isRemoveCookieHeader, isRemoveCacheRequestHeader, regexFilterInclude, regexFilterExclude, recordXmlOut);

                log.info("After HarForJMeter.generateJmxAndRecord");
                btConvert.setEnabled(true);
                if (!recordXmlOut.isEmpty()) {
                    labelStatus.setText("Tool HAR Convertor Finished OK, fileJmxOut=" + fileJmxOut + " AND recordXmlOut=" + recordXmlOut);
                } else {
                    labelStatus.setText("Tool HAR Convertor Finished OK, fileJmxOut=" + fileJmxOut);
                }
     	 		labelStatus.setForeground(java.awt.Color.BLACK);
             } catch (Exception e) {
                e.printStackTrace();
                except = e;
                btConvert.setEnabled(true);
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
    }

    private JPanel createControls() {
    	btConvert = new JButton("CONVERT");
    	btConvert.addActionListener(this);
    	btConvert.setActionCommand(ACTION_CONVERT);
    	btConvert.setEnabled(true);

        JPanel panel = new JPanel();
        panel.add(btConvert);
        return panel;
    }


    private JPanel createFilterAndPausePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel regexFilterIncludeLabel = new JLabel("(Optional) Regular expression matches for filter Include URL");
        regexFilterIncludeField = new JTextField("", 80);

        JLabel regexFilterExcludeLabel = new JLabel("(Optional) Regular expression matches for filter Exclude URL");
        regexFilterExcludeField = new JTextField("", 80);

        JLabel pauseBetweenUrlLabel = new JLabel("(Optional) Time (ms) between 2 URLs to create a new page (Transaction Controller)");
        pauseBetweenUrlTextField = new JTextField("5000", 80);

        panel.add(pauseBetweenUrlLabel);
        panel.add(pauseBetweenUrlTextField);

        panel.add(regexFilterIncludeLabel);
        panel.add(regexFilterIncludeField);

        panel.add(regexFilterExcludeLabel);
        panel.add(regexFilterExcludeField);
        return panel;
    }

    private JPanel createCheckbox() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JLabel isAddPauseLabel = new JLabel("(Optional) Add a pause between Transaction Controller (default true), time between 2 URLs must be > 0");
        isAddPauseCheckbox= new JCheckBox("",true);

        JLabel isRemoveCookieLabel = new JLabel("(Optional) Remove cookie in http header (default true because add a Cookie Manager)");
        isRemoveCookieCheckbox= new JCheckBox("",true);

        JLabel isRemoveCacheHeaderLabel = new JLabel("(Optional) Remove cache header in the http request (default true because add a Cache Manager)");
        isRemoveCacheRequestHeaderCheckbox= new JCheckBox("",true);

        panel.add(isAddPauseLabel);
        panel.add(isAddPauseCheckbox);

        panel.add(isRemoveCookieLabel);
        panel.add(isRemoveCookieCheckbox);

        panel.add(isRemoveCacheHeaderLabel);
        panel.add(isRemoveCacheRequestHeaderCheckbox);

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

        return fileChooserPanel;
    }
    
    /**
     * Show a file chooser to the user
     *
     * @param locationTextField
     *            the textField that will receive the path
     * @param onlyDirectory
     *            whether or not the file chooser will only display directories
     * @param extensions File extensions to filter
     * @return the path the user selected or, if the user cancelled the file
     *         chooser, the previous path
     */
    private String showFileChooser(Component component, JTextField locationTextField, boolean onlyDirectory, String[] extensions) {
        JFileChooser jfc = new JFileChooser();
        if (onlyDirectory) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
