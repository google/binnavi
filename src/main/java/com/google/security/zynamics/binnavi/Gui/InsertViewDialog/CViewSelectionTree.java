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

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;

/**
 * Tree component that can be used to select a view from all views
 * of a given view container.
 */
public final class CViewSelectionTree extends JTree
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = -677028371100260666L;

	/**
	 * Creates a new view selection tree.
	 *
	 * @param dialog Parent window used for dialogs.
	 * @param container The container that provides the views to select from.
	 */
	public CViewSelectionTree(final Window dialog, final IViewContainer container)
	{
		final DefaultTreeModel model = new DefaultTreeModel(null);
		setModel(model);

		setRootVisible(false);

		// Set the root node of the tree.
		model.setRoot(new CRootNode(dialog, container, model));

		Preconditions.checkNotNull(container, "IE01826: Container argument can not be null");

		setRootVisible(false);

		setCellRenderer(new IconNodeRenderer()); // ATTENTION: UNDER NO CIRCUMSTANCES MOVE THIS LINE ABOVE THE SETROOT LINE

		addMouseListener(new InternalMouseListener());
	}

	/**
	 * Passes double-click events to the nodes to handle them.
	 *
	 * @param event The mouse event to handle.
	 */
	private void handleDoubleClick(final MouseEvent event)
	{
		final IViewSelectionTreeNode selectedNode = (IViewSelectionTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

		if (selectedNode == null)
		{
			return;
		}

		selectedNode.doubleClicked();
	}

	/**
	 * Handles double-clicks on nodes.
	 */
	private class InternalMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked(final MouseEvent event)
		{
			if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1)
			{
				handleDoubleClick(event);
			}
		}
	}
}
