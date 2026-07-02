package com.dungeonrpg.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * EndScreenPanel is one reusable class for BOTH victory and defeat —
 * they're the same shape (big title, short subtitle, two buttons) with
 * different text and colors, so building two nearly-identical panel
 * classes would just be duplication with extra steps. Victory and
 * Game Over are constructed by passing different arguments into the
 * same constructor.
 */
public class EndScreenPanel extends JPanel {

    public EndScreenPanel(String title, String subtitle, Color accentColor,
                           Runnable onRestart, Runnable onQuit) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UiTheme.BG_DARKEST);

        add(Box.createVerticalGlue());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UiTheme.FONT_BODY);
        subtitleLabel.setForeground(UiTheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(subtitleLabel);

        add(Box.createVerticalStrut(30));

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton restartButton = new RoundedButton("Play Again");
        restartButton.setPreferredSize(new Dimension(130, 40));
        restartButton.addActionListener(e -> onRestart.run());

        RoundedButton quitButton = new RoundedButton("Quit");
        quitButton.setPreferredSize(new Dimension(100, 40));
        quitButton.addActionListener(e -> onQuit.run());

        buttonRow.add(restartButton);
        buttonRow.add(Box.createHorizontalStrut(14));
        buttonRow.add(quitButton);
        add(buttonRow);

        add(Box.createVerticalGlue());
    }
}
