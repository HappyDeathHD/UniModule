package ru.bpcbt.utils;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Style {
    public static final Color RED = new Color(217, 83, 79);
    public static final Color RED_B = new Color(255, 118, 112);
    public static final Color GREEN = new Color(92, 184, 92);
    public static final Color GREEN_B = new Color(131, 255, 131);
    public static final Color BLUE = new Color(2, 117, 216);
    public static final Color BLUE_B = new Color(4, 167, 255);
    public static final Color YELLOW = new Color(240, 173, 78);
    public static final Color YELLOW_B = new Color(255, 247, 111);
    public static final Color GRAY = new Color(238, 238, 238);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);

    private static final UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();

    private static final SimpleAttributeSet error;
    private static final SimpleAttributeSet success;
    private static final SimpleAttributeSet warning;
    private static final SimpleAttributeSet fine;
    private static final SimpleAttributeSet mark;

    static {
        error = new SimpleAttributeSet();
        StyleConstants.setBackground(error, RED_B);
        StyleConstants.setBold(error, true);

        success = new SimpleAttributeSet();
        StyleConstants.setBackground(success, GREEN);
        StyleConstants.setBold(success, true);

        warning = new SimpleAttributeSet();
        StyleConstants.setBackground(warning, YELLOW_B);

        fine = new SimpleAttributeSet();
        StyleConstants.setBackground(fine, GREEN_B);

        mark = new SimpleAttributeSet();
        StyleConstants.setBackground(mark, BLUE_B);
    }

    private Style() {// Utils class
    }

    public static UIManager.LookAndFeelInfo[] getLafs() {
        return lafs;
    }

    /*Getters & Setters*/
    public static SimpleAttributeSet getError() {
        return error;
    }

    public static SimpleAttributeSet getSuccess() {
        return success;
    }

    public static SimpleAttributeSet getWarning() {
        return warning;
    }

    public static SimpleAttributeSet getFine() {
        return fine;
    }

    public static SimpleAttributeSet getMark() {
        return mark;
    }
}
