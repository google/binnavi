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
package com.google.security.zynamics.binnavi.Gui.InsertViewDialog;



import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

/**
 * This dialog can be used to select a view from a view container.
 */
public final class CViewSelectionDialog extends JDialog
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = -4076097510286368826L;

	/**
	 * After the user clicked OK, the selected view is stored here.
	 */
	private INaviView m_view;

	/**
	 * The component where all available views are displayed.
	 */
	private final CViewSelectionTree m_tree;

	/**
	 * Creates a new view selection dialog.
	 *
	 * @param parent Parent frame of the dialog.
	 * @param container The view container that provides the views to select from.
	 */
	public CViewSelectionDialog(final JFrame parent, final IViewContainer container)
	{
		super(parent, "Insert View", ModalityType.APPLICATION_MODAL);
		Preconditions.checkNotNull(parent, "IE01824: Parent argument can not be null");

		Preconditions.checkNotNull(container, "IE01825: Container argument can not be null");

		setLayout(new BorderLayout());

		m_tree = new CViewSelectionTree(this, container);

		add(new JScrollPane(m_tree));

		final CPanelTwoButtons buttons = new CPanelTwoButtons(new InternalListener(), "OK", "Cancel");
		add(buttons, BorderLayout.SOUTH);

		pack();
	}

	/**
	 * Determines the selected view of the tree component.
	 *
	 * @return The currently selected view or null if no view is selected.
	 */
	private INaviView getSelectedView()
	{
		final TreePath path = m_tree.getSelectionPath();

		if (path == null)
		{
			return null;
		}

		final Object component = path.getLastPathComponent();

		if (component instanceof CViewIconNode)
		{
			return ((CViewIconNode) component).getView();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the selected view. If the dialog was canceled this method
	 * returns null.
	 *
	 * @return The selected view or null if the dialog was canceled.
	 */
	public INaviView getView()
	{
		return m_view;
	}

	/**
	 * Keeps track of the OK and Cancel buttons and acts accordingly
	 * if either of the buttons is clicked.
	 */
	private class InternalListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent event)
		{
			if (event.getActionCommand().equals("OK"))
			{
				m_view = getSelectedView();
			}

			dispose();
		}
	}
}
