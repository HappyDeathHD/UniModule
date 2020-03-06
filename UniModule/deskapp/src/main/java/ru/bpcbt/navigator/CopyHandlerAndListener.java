package ru.bpcbt.navigator;

import ru.bpcbt.misc.Delimiters;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.stream.Collectors;

class CopyHandlerAndListener extends TransferHandler implements KeyListener {

    private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private final JTree navigatorTree;

    CopyHandlerAndListener(JTree navigatorTree) {
        this.navigatorTree = navigatorTree;
    }

    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        JTree tree = (JTree) comp;
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            String placeholder = Delimiters.START_END.getSymbol()
                    + Arrays.stream(path.getPath()).skip(1).map(Object::toString)
                    .collect(Collectors.joining(Delimiters.DELIMITER.getSymbol()))
                    + Delimiters.START_END.getSymbol();
            clip.setContents(new StringSelection(placeholder), null);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // KeyEvent.getModifiers() — в виде маски модификаторов из битов
        if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & MASK) != 0)) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            navigatorTree.getTransferHandler().exportToClipboard(navigatorTree, clipboard, TransferHandler.COPY);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { // No implementation necessary
    }

    @Override
    public void keyReleased(KeyEvent e) { // No implementation necessary
    }
}