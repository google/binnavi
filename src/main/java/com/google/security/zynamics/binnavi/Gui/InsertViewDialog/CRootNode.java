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
import java.util.List;

import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Class of root nodes of the view selection tree.
 */
public final class CRootNode extends IconNode implements IViewSelectionTreeNode
{
	// TODO: Dispose nodes

	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = 896018082290216639L;

	/**
	 * Creates a new view selection root node.
	 *
	 * @param dialog Parent window used for dialogs.
	 * @param container The container that provides the views to select from.
	 * @param model Tree model of the tree the node belongs to.
	 */
	public CRootNode(final Window dialog, final IViewContainer container, final DefaultTreeModel model)
	{
		Preconditions.checkNotNull(container, "IE01822: Container argument can not be null");

		final List<INaviAddressSpace> addressSpaces = container.getAddressSpaces();

		if (addressSpaces == null)
		{
			// We are dealing with views from a module here.
			final INaviModule module = container.getModules().get(0);

			add(new ViewSelectionModuleNode(dialog, module, model));
		}
		else
		{
			// We are dealing with views from a project here.

			// TODO: Check whether project views can be selected from this dialog.

			for (final INaviAddressSpace addressSpace : addressSpaces)
			{
				add(new CAddressSpaceNode(dialog, addressSpace, model)); 
			}
		}
	}

	@Override
	public void doubleClicked()
	{
		// No action is associated with double-clicking root nodes
	}

}
