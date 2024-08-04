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
import java.awt.event.ItemEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Displays an interface with which the user can configure their proxy
 * settings.
 */
public final class ProxyPanel extends JPanel implements ActionListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ProxyPanel(Getdown getdown, ResourceBundle msgs, boolean updateAuth) {
        _getdown = getdown;
        _msgs = msgs;
        _updateAuth = updateAuth;

        setLayout(new VGroupLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String title = get(updateAuth ? "m.update_proxy_auth" : "m.configure_proxy");
        add(new SaneLabelField(title));
        add(new Spacer(5, 5));

        JPanel row = new JPanel(new GridLayout());
        row.add(new SaneLabelField(get("m.proxy_host")), BorderLayout.WEST);
        row.add(_host = new SaneTextField());
        add(row);

        row = new JPanel(new GridLayout());
        row.add(new SaneLabelField(get("m.proxy_port")), BorderLayout.WEST);
        row.add(_port = new SaneTextField());
        add(row);

        add(new Spacer(5, 5));

        row = new JPanel(new GridLayout());
        row.add(new SaneLabelField(get("m.proxy_auth_required")), BorderLayout.WEST);
        _useAuth = new JCheckBox();
        row.add(_useAuth);
        _useAuth.setSelected(updateAuth);
        add(row);

        row = new JPanel(new GridLayout());
        row.add(new SaneLabelField(get("m.proxy_username")), BorderLayout.WEST);
        _username = new SaneTextField();
        _username.setEnabled(updateAuth);
        row.add(_username);
        add(row);

        row = new JPanel(new GridLayout());
        row.add(new SaneLabelField(get("m.proxy_password")), BorderLayout.WEST);
        _password = new SanePasswordField();
        _password.setEnabled(updateAuth);
        row.add(_password);
        add(row);

        _useAuth.addItemListener(event -> {
            boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
            _username.setEnabled(selected);
            _password.setEnabled(selected);
        });

        add(new Spacer(5, 5));

        row = GroupLayout.makeButtonBox(GroupLayout.CENTER);
        JButton button;
        row.add(button = new JButton(get("m.proxy_ok")));
        button.setActionCommand("ok");
        button.addActionListener(this);
        row.add(button = new JButton(get("m.proxy_cancel")));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        add(row);
    }

    public void setProxy(String host, String port) {
        if (host != null) {
            _host.setText(host);
        }
        if (port != null) {
            _port.setText(port);
        }
    }

    // documentation inherited
    @Override
    public void addNotify() {
        super.addNotify();
        if (_updateAuth) {
            // we are asking the user to update the credentials for an existing proxy
            // configuration, so focus that instead of the proxy host config
            _username.requestFocusInWindow();
        } else {
            _host.requestFocusInWindow();
        }
    }

    // documentation inherited
    @Override
    public Dimension getPreferredSize() {
        // this is annoyingly hardcoded, but we can't just force the width
        // or the JLabel will claim a bogus height thinking it can lay its
        // text out all on one line which will booch the whole UI's
        // preferred size
        return new Dimension(500, 320);
    }

    // documentation inherited from interface
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("ok".equals(cmd)) {
            String user = null, pass = null;
            if (_useAuth.isSelected()) {
                user = _username.getText();
                // we have to keep the proxy password around for every HTTP request, so having it
                // in a char[] that gets zeroed out after use is not viable for this use case
                pass = new String(_password.getPassword());
            }
            _getdown.configProxy(_host.getText(), _port.getText(), user, pass);
        } else {
            // they canceled, we're outta here
            System.exit(0);
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

    protected static class SaneLabelField extends JLabel {
        public SaneLabelField(String message) {
            super(message);
        }

        @Override
        public Dimension getPreferredSize() {
            return clampWidth(super.getPreferredSize(), 200);
        }
    }

    protected static class SaneTextField extends JTextField {
        @Override
        public Dimension getPreferredSize() {
            return clampWidth(super.getPreferredSize(), 150);
        }
    }

    protected static class SanePasswordField extends JPasswordField {
        @Override
        public Dimension getPreferredSize() {
            return clampWidth(super.getPreferredSize(), 150);
        }
    }

    private static Dimension clampWidth(Dimension dim, int minWidth) {
        dim.width = Math.max(dim.width, minWidth);
        return dim;
    }

    private final Getdown _getdown;
    private final ResourceBundle _msgs;
    private final boolean _updateAuth;

    private final JTextField _host;
    private final JTextField _port;
    private final JCheckBox _useAuth;
    private final JTextField _username;
    private final JPasswordField _password;
}
