/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Help;



import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.zylib.gui.UrlLabel.UrlLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;



/**
 * Dialog where context-sensitive help is shown.
 */
public class CHelpDialog extends JDialog
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = 6879759177756681306L;

	/**
	 * Shows the context-sensitive help.
	 */
	private final JTextArea m_helpArea = new JTextArea();

	/**
	 * Links to the relevant section in the manual.
	 */
	private final UrlLabel m_url = new UrlLabel("Click here to open the manual");

	/**
	 * Creates a new help dialog.
	 */
	public CHelpDialog()
	{
		super(new JFrame(), "Quick Help");

		setLayout(new BorderLayout());
		setUndecorated(true);

		CIconInitializer.initializeWindowIcons(this);

		m_helpArea.setLineWrap(true);
		m_helpArea.setWrapStyleWord(true);
		m_helpArea.setEditable(false);

		final JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(new TitledBorder(""));
		innerPanel.setBackground(Color.BLACK);

		innerPanel.add(new JScrollPane(m_helpArea));

		final JPanel lowerPanel = new JPanel(new BorderLayout());

		m_url.setBorder(new EmptyBorder(5, 0, 5, 5));
		lowerPanel.add(m_url);

		innerPanel.add(lowerPanel, BorderLayout.SOUTH);

		add(innerPanel);

		setAlwaysOnTop(true);

		setSize(500, 200);
		setResizable(false);

		addWindowFocusListener(new WindowAdapter()
		{
			@Override
			public void windowLostFocus(final WindowEvent event)
			{
				setVisible(false);
			}
		});

		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	/**
	 * Sets the information shown in the dialog.
	 *
	 * @param help The information shown in the dialog.
	 */
	public void setInformation(final IHelpInformation help)
	{
		m_helpArea.setText(help.getText());
		m_url.setUrl(help.getUrl());
	}
}
