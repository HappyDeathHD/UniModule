package ru.bpcbt.settings;

import ru.bpcbt.MainFrame;
import ru.bpcbt.Program;
import ru.bpcbt.misc.ColoredButton;
import ru.bpcbt.utils.Style;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.logger.Narrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

import static ru.bpcbt.settings.Settings.*;

public class SettingsPanel extends JPanel {
    private JTextField inputDirTF;
    private JTextField modulesDirTF;
    private JTextField outputDirTF;
    private JComboBox fontNameCB;
    private JSpinner fontSizeS;
    private JTextField coreUrlTF;
    private JTextField usernameTF;

    private final JPasswordField passwordPF = new JPasswordField();

    private Map<Settings, String> properties;
    private GridBagConstraints gridBag;
    private String[] fonts;

    public SettingsPanel() {
        gridBag = new GridBagConstraints();
        gridBag.fill = GridBagConstraints.BOTH;
        gridBag.insets = new Insets(3, 3, 3, 3);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        addOptionDir(INPUT_DIR, inputDirTF = new JTextField());
        addOptionDir(MODULE_DIR, modulesDirTF = new JTextField());
        addOptionDir(OUTPUT_DIR, outputDirTF = new JTextField());
        fonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        addFontSelector(fonts);
        addApiBlock();
        addSaveButton();
    }

    private void addApiBlock() {
        Dimension perfectTFSize = new Dimension(0, 26);
        Dimension perfectLSize = new Dimension(0, 13);

        JPanel apiPanel = new JPanel(new GridBagLayout());
        apiPanel.setBorder(BorderFactory.createTitledBorder("Данные для подключения к API"));
        GridBagConstraints apiGridBag = new GridBagConstraints();
        apiGridBag.fill = GridBagConstraints.BOTH;
        apiGridBag.insets = new Insets(3, 3, 3, 3);
        apiGridBag.weightx = 1;

        JLabel coreUrlL = new JLabel(CORE_URL.getDescription());
        apiGridBag.gridwidth = 2;
        apiGridBag.gridy = 0;
        apiGridBag.gridx = 0;
        apiPanel.add(coreUrlL, apiGridBag);
        coreUrlTF = new JTextField();
        coreUrlTF.setPreferredSize(perfectTFSize);
        apiGridBag.gridy = 1;
        apiPanel.add(coreUrlTF, apiGridBag);

        JLabel loginL = new JLabel(USERNAME.getDescription());
        loginL.setPreferredSize(perfectLSize);
        apiGridBag.gridwidth = 1;
        apiGridBag.gridy = 2;
        apiPanel.add(loginL, apiGridBag);
        usernameTF = new JTextField();
        usernameTF.setPreferredSize(perfectTFSize);
        apiGridBag.gridy = 3;
        apiPanel.add(usernameTF, apiGridBag);

        JLabel passwordL = new JLabel(PASSWORD.getDescription());
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

    private void addFontSelector(String[] fonts) {
        JLabel workingDirL = new JLabel(FONT_NAME.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(workingDirL, gridBag);

        fontNameCB = new JComboBox(fonts);
        gridBag.gridwidth = 1;
        gridBag.gridy++;
        add(fontNameCB, gridBag);

        fontSizeS = new JSpinner();
        gridBag.gridx = 1;
        add(fontSizeS, gridBag);
    }

    private void addOptionDir(Settings property, JTextField field) {
        JLabel workingDirL = new JLabel(property.getDescription());
        gridBag.gridx = 0;
        gridBag.gridy++;
        gridBag.gridwidth = 2;
        add(workingDirL, gridBag);
        gridBag.gridwidth = 1;

        field.setColumns(40);
        gridBag.gridx = 0;
        gridBag.gridy++;
        add(field, gridBag);

        ColoredButton button = getCommonSelectFileButton();
        button.addActionListener(new SelectDirActionListener(this, field, property));
        gridBag.gridx = 1;
        add(button, gridBag);
    }

    private void addSaveButton() {
        ColoredButton saveB = new ColoredButton("Сохрани и Примени");
        saveB.setBackground(Style.GREEN);
        saveB.setHoverBackgroundColor(Style.GREEN_B);
        saveB.setPressedBackgroundColor(Style.YELLOW);
        saveB.addActionListener(e -> {
            try {
                properties.put(INPUT_DIR, inputDirTF.getText());
                properties.put(MODULE_DIR, modulesDirTF.getText());
                properties.put(OUTPUT_DIR, outputDirTF.getText());
                properties.put(FONT_NAME, String.valueOf(fontNameCB.getSelectedIndex()));
                properties.put(FONT_SIZE, String.valueOf(fontSizeS.getValue()));
                properties.put(CORE_URL, coreUrlTF.getText());
                properties.put(USERNAME, usernameTF.getText());
                Font font = new Font(fontNameCB.getSelectedItem().toString(), Font.PLAIN, (int) fontSizeS.getValue());
                Program.setNavigatorsFont(font);
                FileUtils.setProperties(properties);
                Narrator.success("Схоронил!");
                Program.refreshAllFiles();
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

    private ColoredButton getCommonSelectFileButton() {
        ColoredButton button = new ColoredButton("🔍");
        button.setBackground(Style.GREEN);
        button.setHoverBackgroundColor(Style.GREEN_B);
        button.setPressedBackgroundColor(Style.YELLOW);
        button.setToolTipText("Выбрать директорию");
        return button;
    }

    public void loadConfigurations() {
        boolean allMandatoryParamsExist = true;
        properties = FileUtils.getProperties();
        if (properties.containsKey(INPUT_DIR)) {
            inputDirTF.setText(properties.get(INPUT_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(MODULE_DIR)) {
            modulesDirTF.setText(properties.get(MODULE_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(OUTPUT_DIR)) {
            outputDirTF.setText(properties.get(OUTPUT_DIR));
        } else {
            allMandatoryParamsExist = false;
        }
        if (properties.containsKey(FONT_NAME) && properties.containsKey(FONT_SIZE)) {
            int selectedFont = Integer.parseInt(properties.get(FONT_NAME));
            fontNameCB.setSelectedIndex(selectedFont);
            int size = Integer.parseInt(properties.get(FONT_SIZE));
            fontSizeS.setValue(size);
            Font font = new Font(fonts[selectedFont], Font.PLAIN, (int) fontSizeS.getValue());
            Program.setNavigatorsFont(font);
        }
        if (properties.containsKey(CORE_URL)) {
            coreUrlTF.setText(properties.get(CORE_URL));
        } else {
            properties.put(CORE_URL, "https://pay.test.aeroflot.ru/unimessage-core");
            coreUrlTF.setText("https://pay.test.aeroflot.ru/unimessage-core");
        }
        if (properties.containsKey(USERNAME)) {
            usernameTF.setText(properties.get(USERNAME));
        }
        if (allMandatoryParamsExist) {
            Narrator.normal("С возвращением!");
        } else {
            Program.getMainFrame().setPaneTab(MainFrame.SETTINGS_TAB);
        }
    }

    public Map<Settings, String> getProperties() {
        return properties;
    }

    public String getPassword() {
        return String.valueOf(passwordPF.getPassword());
    }

    public void setPassword(String password) {
        passwordPF.setText(password);
    }
}
