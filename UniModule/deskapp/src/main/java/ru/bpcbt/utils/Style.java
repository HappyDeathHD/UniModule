package ru.bpcbt.utils;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Style {
    public static final Color RED = new Color(217, 83, 79); // #d9534f
    public static final Color RED_B = new Color(255, 118, 112); // #ff7670
    public static final Color GREEN = new Color(92, 184, 92); // #5cb85c
    public static final Color GREEN_B = new Color(131, 255, 131); // #83ff83
    public static final Color BLUE = new Color(2, 117, 216); // #0275d8
    public static final Color BLUE_B = new Color(4, 167, 255); // #04a7ff
    public static final Color YELLOW = new Color(240, 173, 78); // #f0ad4e
    public static final Color YELLOW_B = new Color(255, 247, 111); // #fff76f
    public static final Color GRAY = new Color(238, 238, 238); // #eeeeee
    public static final Color WHITE = new Color(255, 255, 255); // #ffffff
    public static final Color BLACK = new Color(0, 0, 0); // #000000

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
