package ru.bpcbt.misc;

import javax.swing.*;
import java.awt.*;

public class ColoredButton extends JButton {

    private Color hoverBackgroundColor;
    private Color pressedBackgroundColor;

    public ColoredButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
    }

    public ColoredButton(Icon icon) {
        super(icon);
        super.setContentAreaFilled(false);
        super.setPreferredSize(new Dimension(45,45));
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

    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    public void setPressedBackgroundColor(Color pressedBackgroundColor) {
        this.pressedBackgroundColor = pressedBackgroundColor;
    }
}