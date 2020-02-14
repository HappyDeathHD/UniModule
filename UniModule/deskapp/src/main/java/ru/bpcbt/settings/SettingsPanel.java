package ru.bpcbt.settings;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.misc.ColoredButton;
import ru.bpcbt.utils.GlobalUtils;
import ru.bpcbt.utils.Style;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class SettingsPanel extends JPanel {
    private final JTextField inputDirTF;
    private final JTextField modulesDirTF;
    private final JTextField outputDirTF;
    private final JTextField reserveDirTF;
    private JComboBox fontNameCB;
    private JComboBox styleCB;
    private JSpinner fontSizeS;
    private JTextField coreUrlTF;
    private JTextField usernameTF;
    private JCheckBox debugFlag;

    private final JPasswordField passwordPF = new JPasswordField();

    private final GridBagConstraints gridBag;
    private final String[] fonts;

    public SettingsPanel() {
        gridBag = new GridBagConstraints();
        gridBag.fill = GridBagConstraints.BOTH;
        gridBag.insets = new Insets(3, 3, 3, 3);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        fonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        addOptionDir(Settings.INPUT_DIR, inputDirTF = new JTextField());
        addOptionDir(Settings.MODULE_DIR, modulesDirTF = new JTextField());
        addOptionDir(Settings.OUTPUT_DIR, outputDirTF = new JTextField());
        addOptionDir(Settings.RESERVE_DIR, reserveDirTF = new JTextField());
        addFontSelector();
        addLookAndFeelSelector();
        addApiBlock();
        addSaveButton();
        addDebugFlag();
    }

    private void addApiBlock() {
        final Dimension perfectTFSize = new Dimension(0, 26);
        final Dimension perfectLSize = new Dimension(0, 13);

        final JPanel apiPanel = new JPanel(new GridBagLayout());
        apiPanel.setBorder(BorderFactory.createTitledBorder("Данные для подключения к API"));
        final GridBagConstraints apiGridBag = new GridBagConstraints();
        apiGridBag.fill = GridBagConstraints.BOTH;
        apiGridBag.insets = new Insets(3, 3, 3, 3);
        apiGridBag.weightx = 1;

        final JLabel coreUrlL = new JLabel(Settings.CORE_URL.getDescription());
        apiGridBag.gridwidth = 2;
        apiGridBag.gridy = 0;
        apiGridBag.gridx = 0;
        apiPanel.add(coreUrlL, apiGridBag);
        coreUrlTF = new JTextField();
        coreUrlTF.setPreferredSize(perfectTFSize);
        apiGridBag.gridy = 1;
        apiPanel.add(coreUrlTF, apiGridBag);

        final JLabel loginL = new JLabel(Settings.USERNAME.getDescription());
        loginL.setPreferredSize(perfectLSize);
        apiGridBag.gridwidth = 1;
        apiGridBag.gridy = 2;
        apiPanel.add(loginL, apiGridBag);
        usernameTF = new JTextField();
        usernameTF.setPreferredSize(perfectTFSize);
        apiGridBag.gridy = 3;
        apiPanel.add(usernameTF, apiGridBag);

        final JLabel passwordL = new JLabel(Settings.PASSWORD.getDescription());
        passwordL.setPreferredSize(perfectLSize);
        apiGridBag.gridx = 1;
        apiGridBag.gridy = 2;
        apiPanel.add(passwordL, apiGridBag);
        apiGridBag.gridy = 3;
        passwordPF.setPreferredSize(perfectTFSize);
        apiPanel.add(passwordPF, apiGridBag);

        gridBag.gridy++;
        gridBag.gridx = 0;
        gridBag.gridwidth = 2;
        add(apiPanel, gridBag);
        gridBag.gridwidth = 1;
    }

    @SuppressWarnings("unchecked")
    private void addFontSelector() {
        final JLabel fontNameL = new JLabel(Settings.FONT_NAME.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(fontNameL, gridBag);

        fontNameCB = new JComboBox(fonts);
        gridBag.gridwidth = 1;
        gridBag.gridy++;
        add(fontNameCB, gridBag);

        fontSizeS = new JSpinner();
        gridBag.gridx = 1;
        add(fontSizeS, gridBag);
    }

    @SuppressWarnings("unchecked")
    private void addLookAndFeelSelector() {
        final JLabel styleL = new JLabel(Settings.STYLE.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(styleL, gridBag);

        final Object[] lafNames = Arrays.stream(Style.getLafs()).map(UIManager.LookAndFeelInfo::getName).toArray();
        styleCB = new JComboBox(lafNames);
        gridBag.gridy++;
        add(styleCB, gridBag);

        gridBag.gridwidth = 1;
    }

    private void addOptionDir(Settings property, JTextField field) {
        final JLabel workingDirL = new JLabel(property.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(workingDirL, gridBag);
        gridBag.gridwidth = 1;

        field.setColumns(40);
        gridBag.gridx = 0;
        gridBag.gridy++;
        add(field, gridBag);

        final ColoredButton button = getCommonSelectFileButton();
        button.addActionListener(new SelectDirActionListener(this, field, property));
        gridBag.gridx = 1;
        add(button, gridBag);
    }

    private void addSaveButton() {
        final ColoredButton saveB = new ColoredButton("Сохрани и Примени", Style.GREEN);
        saveB.addActionListener(e -> {
            try {
                Map<Settings, String> properties = Program.getProperties();
                properties.put(Settings.INPUT_DIR, inputDirTF.getText());
                properties.put(Settings.MODULE_DIR, modulesDirTF.getText());
                properties.put(Settings.OUTPUT_DIR, outputDirTF.getText());
                properties.put(Settings.RESERVE_DIR, reserveDirTF.getText());
                properties.put(Settings.FONT_NAME, String.valueOf(fontNameCB.getSelectedIndex()));
                properties.put(Settings.FONT_SIZE, String.valueOf(fontSizeS.getValue()));
                properties.put(Settings.CORE_URL, coreUrlTF.getText());
                properties.put(Settings.USERNAME, usernameTF.getText());
                properties.put(Settings.STYLE, String.valueOf(styleCB.getSelectedIndex()));
                FileUtils.setProperties(properties);

                if (fontNameCB.getSelectedItem() != null) {
                    Font font = new Font(fontNameCB.getSelectedItem().toString(), Font.PLAIN, (int) fontSizeS.getValue());
                    GlobalUtils.setNavigatorsFont(font);
                }
                UIManager.setLookAndFeel(Style.getLafs()[styleCB.getSelectedIndex()].getClassName());
                SwingUtilities.updateComponentTreeUI(Program.getMainFrame());
                Narrator.success("Схоронил!");
                GlobalUtils.refreshAllFiles();
                Program.getMainFrame().setPaneTab(MainFrame.INPUTS_TAB);
            } catch (Exception ex) {
                Narrator.yell("Не удалось сохранить настройки", ex);
            }
        });
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(saveB, gridBag);
        gridBag.gridwidth = 1;
    }

    private void addDebugFlag() {
        debugFlag = new JCheckBox(Settings.DEBUG.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(debugFlag, gridBag);
    }

    private ColoredButton getCommonSelectFileButton() {
        final ColoredButton button = new ColoredButton("🔍", Style.GREEN);
        button.setToolTipText("Выбрать директорию");
        return button;
    }

    public void loadConfigurations() {
        boolean allMandatoryParamsExist = true;
        final Map<Settings, String> properties = Program.getProperties();
        if (properties.containsKey(Settings.INPUT_DIR)) {
            inputDirTF.setText(properties.get(Settings.INPUT_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(Settings.MODULE_DIR)) {
            modulesDirTF.setText(properties.get(Settings.MODULE_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(Settings.OUTPUT_DIR)) {
            outputDirTF.setText(properties.get(Settings.OUTPUT_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(Settings.RESERVE_DIR)) {
            reserveDirTF.setText(properties.get(Settings.RESERVE_DIR));
        }
        if (properties.containsKey(Settings.FONT_NAME) && properties.containsKey(Settings.FONT_SIZE)) {
            int selectedFont = Integer.parseInt(properties.get(Settings.FONT_NAME));
            fontNameCB.setSelectedIndex(selectedFont);
            int size = Integer.parseInt(properties.get(Settings.FONT_SIZE));
            fontSizeS.setValue(size);
            Font font = new Font(fonts[selectedFont], Font.PLAIN, (int) fontSizeS.getValue());
            GlobalUtils.setNavigatorsFont(font);
        } else {
            fontSizeS.setValue(14);
        }
        if (properties.containsKey(Settings.CORE_URL)) {
            coreUrlTF.setText(properties.get(Settings.CORE_URL));
        } else {
            String defaultCoreUrl = Program.getSysProperty("default.core.url");
            properties.put(Settings.CORE_URL, defaultCoreUrl);
            coreUrlTF.setText(defaultCoreUrl);
        }
        if (properties.containsKey(Settings.USERNAME)) {
            usernameTF.setText(properties.get(Settings.USERNAME));
        }
        if (properties.containsKey(Settings.STYLE)) {
            int lafIndex = Integer.parseInt(properties.get(Settings.STYLE));
            styleCB.setSelectedIndex(lafIndex);
        }
        if (allMandatoryParamsExist) {
            Narrator.normal("С возвращением!");
            Program.getMainFrame().getInputFilesPanel().refreshFiles();
        } else {
            Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
        }
    }

    public String getPassword() {
        return String.valueOf(passwordPF.getPassword());
    }

    public void setPassword(String password) {
        passwordPF.setText(password);
    }

    public boolean isDebug() {
        return debugFlag.isSelected();
    }
}
