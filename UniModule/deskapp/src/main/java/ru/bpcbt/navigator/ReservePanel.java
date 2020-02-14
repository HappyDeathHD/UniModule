package ru.bpcbt.navigator;

import ru.bpcbt.rest.TemplateWorker;
import ru.bpcbt.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ReservePanel extends BaseNavigatorTreePanel {
    private final JList<String> templates;

    public ReservePanel(Settings workingDirType) {
        super(workingDirType);
        //схемы с сервера
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        templates = new JList<>();

        final JScrollPane scroll = new JScrollPane(templates);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        contentPanel.add(scroll);
        //единение!
        addSplittedScrollAndContent(contentPanel);
    }

    @Override
    public void selectTab() {
        super.selectTab();
        Map<String, Long> templateIdMap = TemplateWorker.getTemplateIdMap();
        if (templateIdMap != null && !templateIdMap.isEmpty()) {
            Vector<String> templateNames = new Vector<>(templateIdMap.keySet());
            templateNames.sort(String.CASE_INSENSITIVE_ORDER);
            templates.setListData(templateNames);
            splitPane.setDividerLocation(0.7);
        }
    }

    @Override
    public void setFontToElements(Font font) {
        super.setFontToElements(font);
        templates.setFont(font);
    }

    @Override
    public void saveCurrentFile() {
    }

    @Override
    public void repaintTextToDisplay() {
    }

    List<String> getSelectedTemplates() {
        return templates.getSelectedValuesList();
    }
}
