//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/bekoenig/getdown/blob/master/LICENSE

package io.github.bekoenig.getdown.launcher;

import io.github.bekoenig.getdown.launcher.swing.GroupLayout;
import io.github.bekoenig.getdown.launcher.swing.Spacer;
import io.github.bekoenig.getdown.launcher.swing.VGroupLayout;
import io.github.bekoenig.getdown.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Displays a confirmation that the user wants to abort installation.
 */
public final class AbortPanel extends JFrame
    implements ActionListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AbortPanel(Getdown getdown, ResourceBundle msgs) {
        _getdown = getdown;
        _msgs = msgs;

        setLayout(new VGroupLayout());
        setResizable(false);
        setTitle(get("m.abort_title"));

        JLabel message = new JLabel(get("m.abort_confirm"));
        message.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(message);
        add(new Spacer(5, 5));

        JPanel row = GroupLayout.makeButtonBox(GroupLayout.CENTER);
        JButton button;
        row.add(button = new JButton(get("m.abort_ok")));
        button.setActionCommand("ok");
        button.addActionListener(this);
        row.add(button = new JButton(get("m.abort_cancel")));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        getRootPane().setDefaultButton(button);
        add(row);
    }

    // documentation inherited
    @Override
    public Dimension getPreferredSize() {
        // this is annoyingly hardcoded, but we can't just force the width
        // or the JLabel will claim a bogus height thinking it can lay its
        // text out all on one line which will booch the whole UI's
        // preferred size
        return new Dimension(300, 200);
    }

    // documentation inherited from interface
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("ok".equals(cmd)) {
            System.exit(0);
        } else {
            setVisible(false);
        }
    }

    /**
     * Used to look up localized messages.
     */
    private String get(String key) {
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (MessageUtil.isTainted(key)) {
            return MessageUtil.untaint(key);
        }
        try {
            return _msgs.getString(key);
        } catch (MissingResourceException mre) {
            logger.warn("Missing translation message '{}'.", key);
            return key;
        }
    }

    private final Getdown _getdown;
    private final ResourceBundle _msgs;
}
