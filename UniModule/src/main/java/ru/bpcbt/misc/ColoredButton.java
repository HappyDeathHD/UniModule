package ru.bpcbt.misc;

import javax.swing.*;
import java.awt.*;

public class ColoredButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public ColoredButton(String text) {
        super(text);
        setContentAreaFilled(false);
    }

    public ColoredButton(Icon icon, String toolTip, Color bgColor, Color bgColorHover, Color bgColorPressed) {
        super(icon);
        setContentAreaFilled(false);
        setPreferredSize(new Dimension(45, 45));
        setToolTipText(toolTip);
        setBackground(bgColor);
//        setForeground(bgColor);
        setHoverBackgroundColor(bgColorHover);
        setPressedBackgroundColor(bgColorPressed);
        //setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed()) {
            g.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g.setColor(hoverBackgroundColor);
        } else {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    /*Getters & Setters*/
    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public void setPressedBackgroundColor(Color pressedBackgroundColor) {
        this.pressedBackgroundColor = pressedBackgroundColor;
    }
}