package ru.bpcbt.misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class HoverButton extends JButton implements MouseListener {

    private static final float TRANSPARENCY_STEP = .03f;
    private static final long TRANSPARENCY_DELAY = 10L;
    private static final float MIN_TRANSPARENCY = .7f;

    private float currentTransparency = MIN_TRANSPARENCY;
    private float maxTransparency = 1f;

    public HoverButton(Icon icon, String toolTip) {
        super(icon);
        setToolTipText(toolTip);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        addMouseListener(this);
        setMargin(new Insets(0, 0, 0, 0));
    }

    private void setCurrentTransparency(float currentTransparency) {
        this.currentTransparency = currentTransparency;
        repaint();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentTransparency));
        super.paintComponent(g2);
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        currentTransparency = .5f;
        maxTransparency = b ? 1f : .5f;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        new Thread(() -> {
            for (float i = currentTransparency; i <= maxTransparency; i += TRANSPARENCY_STEP) {
                setCurrentTransparency(i);
                try {
                    Thread.sleep(TRANSPARENCY_DELAY);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        new Thread(() -> {
            for (float i = maxTransparency; i >= MIN_TRANSPARENCY; i -= TRANSPARENCY_STEP) {
                setCurrentTransparency(i);
                try {
                    Thread.sleep(TRANSPARENCY_DELAY);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    @Override
    public void mouseClicked(MouseEvent e) { // No implementation necessary
    }

    @Override
    public void mousePressed(MouseEvent e) { // No implementation necessary
    }

    @Override
    public void mouseReleased(MouseEvent e) { // No implementation necessary
    }
}