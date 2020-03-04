package ru.bpcbt.misc;

import ru.bpcbt.logger.ReportPane;
import ru.bpcbt.navigator.SelectableTab;
import ru.bpcbt.utils.FileUtils;
import ru.bpcbt.utils.Style;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class InfoPanel extends JPanel implements SelectableTab {

    public InfoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        final StringBuilder hints = new StringBuilder("<html><div style=\"padding:20\"><h1>Разделители</h1><table>");
        for (Delimiters delimiter : Delimiters.values()) {
            hints.append("<tr><td>").append(delimiter.getSymbol()).append("</td><td>").append(delimiter.getDescription()).append("</td></tr>");
        }
        hints.append("</table><h1>Маппинг названий шаблонов</h1>")
                .append("В корневой папке со скелетами может быть файл ").append(FileUtils.TEMPLATE_MAPPING_FILE).append(" со структурой:")
                .append("<pre>{<br/> \"НАЗВАНИЕ_ПАПКИ\": {<br/>  \"name\":\"НАЗВАНИЕ_ОБЩЕЙ_СХЕМЫ\",<br/>")
                .append("  \"topics\": {<br/>   \"ЯЗЫК(ru/en/...)\":\"ТЕМА_ПИСЬМА\"<br/>  }<br/> }<br/>}</pre></div></html>");
        JLabel hintL = new JLabel(hints.toString());
        hintL.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(hintL);
        add(Box.createRigidArea(new Dimension(0, 100)));
        add(createButtonWithLink("Основная инструкция", "https://rbs-develop.paymentgate.ru/wiki/pages/viewpage.action?pageId=86973254"));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(createButtonWithLink("Инструкция по загрузке", "https://rbs-develop.paymentgate.ru/wiki/pages/viewpage.action?pageId=96143819"));
    }

    @Override
    public void selectTab() {
    }

    private ColoredButton createButtonWithLink(String title, String url) {
        final ColoredButton linkB = new ColoredButton(title, Style.BLUE_B);
        linkB.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkB.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(url));
            } catch (IOException ex) {
                ex.printStackTrace();
                ReportPane.debug("Не смог открыть ссылку", ex);
            }
        });
        Dimension size = new Dimension(200, 30);
        linkB.setMinimumSize(size);
        linkB.setMaximumSize(size);
        linkB.setPreferredSize(size);
        return linkB;
    }
}
