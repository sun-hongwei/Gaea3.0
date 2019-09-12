package com.wh.control;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class ScrollToolBar {
	public ScrollToolBar(JPanel panel, JPanel scrollButtonPanel,
			JScrollPane toolbarScrollBar,JToolBar toolBar) {
		panel.setOpaque(false);
		panel.setBorder(null);
		toolBar.setFloatable(false);
		toolBar.setOpaque(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolbarScrollBar.setOpaque(false);
		toolbarScrollBar.setBorder(null);
		toolbarScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		toolbarScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		scrollButtonPanel.setLayout(new BoxLayout(scrollButtonPanel, BoxLayout.X_AXIS));

		JButton forwardButton = new JButton("");
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JScrollBar scrollBar = toolbarScrollBar.getHorizontalScrollBar();
				scrollBar.setUnitIncrement(50);
				scrollBar.setValue(scrollBar.getValue() - 50);
			}
		});
		forwardButton.setIcon(new ImageIcon(panel.getClass().getResource("/image/arrowleft.png")));
		forwardButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollButtonPanel.add(forwardButton);

		JButton backwardsButton = new JButton("");
		backwardsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JScrollBar scrollBar = toolbarScrollBar.getHorizontalScrollBar();
				scrollBar.setUnitIncrement(50);
				scrollBar.setValue(scrollBar.getValue() + 50);
			}
		});

		backwardsButton.setIcon(new ImageIcon(panel.getClass().getResource("/image/arrowright.png")));
		backwardsButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollButtonPanel.add(backwardsButton);

		toolBar.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (scrollButtonPanel.getParent() == panel) {
					panel.remove(scrollButtonPanel);
					panel.updateUI();
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							componentResized(e);
						}
					});
					return;
				}
				if (toolBar.getWidth() > toolbarScrollBar.getWidth()) {
					panel.add(scrollButtonPanel, BorderLayout.EAST);
				}
				panel.updateUI();
			}
		});

	}

}
