package ru.bpcbt.misc;

import javax.swing.*;
import java.awt.*;

public class ColoredButton extends JButton {

    public ColoredButton(String text, Color color) {
        super(text);
        setBackground(color);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}