package ru.bpcbt.navigator;

import ru.bpcbt.rest.UnimessageConductor;
import ru.bpcbt.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ReservePanel extends BaseNavigatorTreePanel {

    private static final String LOADING = "Загрузка...";

    private final JList<String> templates;

    public ReservePanel(Settings workingDirType) {
        super(workingDirType);
        // Схемы с сервера
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        templates = new JList<>();

        final JScrollPane scroll = new JScrollPane(templates);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.add(scroll);
        // Единение!
        addSplittedScrollAndContent(contentPanel);
        splitPane.setResizeWeight(0.7);
    }

    @Override
    public void selectTab() {
        if (navigatorTree.getModel() == null) {
            super.refreshFiles();
        }
        if (templates.getModel().getSize() == 0
                || (templates.getModel().getSize() == 1 && LOADING.equals(templates.getModel().getElementAt(0)))) {
            fillTemplatesInBackground();
        }
    }

    @Override
    public void setFontToElements(Font font) {
        super.setFontToElements(font);
        templates.setFont(font);
    }

    @Override
    public void saveCurrentFile() { // No implementation necessary
    }

    @Override
    public void repaintTextToDisplay() { // No implementation necessary
    }

    @Override
    public void refreshFiles() {
        super.refreshFiles();
        fillTemplatesInBackground();
    }

    private void fillTemplatesInBackground() {
        templates.setListData(new String[]{LOADING});
        SwingUtilities.invokeLater(() -> {
            Map<String, Long> templateIdMap = UnimessageConductor.getTemplateIdMap();
            if (templateIdMap != null && !templateIdMap.isEmpty()) {
                Vector<String> templateNames = new Vector<>(templateIdMap.keySet());
                templateNames.sort(String.CASE_INSENSITIVE_ORDER);
                templates.setListData(templateNames);
            }
        });
    }

    List<String> getSelectedTemplates() {
        return templates.getSelectedValuesList();
    }

    @Override
    public void forceUpdateLater() {
        templates.setModel(new DefaultListModel<>());
        navigatorTree.setModel(null);
    }
}
