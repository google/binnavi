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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceConfigurationListener;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Class for address space nodes of view selection dialogs.
 */
public final class CAddressSpaceNode extends IconNode implements IViewSelectionTreeNode
{
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = 7984412952756615230L;

	/**
	 * Icon used when the address space is loaded.
	 */
	private static final ImageIcon ICON_ADDRESSSPACE = new ImageIcon(CMain.class.getResource("data/projecttreeicons/addressspace2.png"));

	/**
	 * Icon used when the address space is unloaded.
	 */
	private static final ImageIcon ICON_ADDRESSSPACE_GRAY = new ImageIcon(CMain.class.getResource("data/projecttreeicons/addressspace_gray.png"));

	/**
	 * The address space represented by the node.
	 */
	private final INaviAddressSpace m_addressSpace;

	/**
	 * Parent window for dialogs.
	 */
	private final Window m_parent;

	/**
	 * Tree model of the project tree.
	 */
	private final DefaultTreeModel m_model;

	/**
	 * Updates the node on changes to the address space.
	 */
	private final IAddressSpaceListener m_internalSpaceListener = new InternalSpaceListener();

	private final IAddressSpaceConfigurationListener m_internalSpaceConfigurationListener = new InternalAddressSpaceConfigurationListener();

	/**
	 * Creates a new address space node.
	 *
	 * @param parent Parent window for dialogs.
	 * @param addressSpace The address space represented by the node.
	 * @param model Tree model of the tree the node belongs to.
	 */
	public CAddressSpaceNode(final Window parent, final INaviAddressSpace addressSpace, final DefaultTreeModel model)
	{
		m_addressSpace = Preconditions.checkNotNull(addressSpace, "IE01820: Address space argument can not be null");
		m_parent = Preconditions.checkNotNull(parent, "IE02332: Parent argument can not be null");
		m_model = Preconditions.checkNotNull(model, "IE02333: Model argument can not be null");

		// TODO: Dispose this
		m_addressSpace.addListener(m_internalSpaceListener);
		m_addressSpace.getConfiguration().addListener(m_internalSpaceConfigurationListener);

		createChildren();
	}

	/**
	 * Creates the children of the node.
	 */
	private void createChildren()
	{
		if (m_addressSpace.isLoaded())
		{
			for (final INaviModule module : m_addressSpace.getContent().getModules())
			{
				add(new ViewSelectionModuleNode(m_parent, module, m_model));
			}
		}
	}

	@Override
	public void doubleClicked()
	{
		// No action is associated with double-clicking address spaces.
	}

	@Override
	public Icon getIcon()
	{
		return m_addressSpace.isLoaded() ? ICON_ADDRESSSPACE : ICON_ADDRESSSPACE_GRAY;
	}

	@Override
	public String toString()
	{
		return m_addressSpace.getConfiguration().getName();
	}

	private class InternalAddressSpaceConfigurationListener extends CAddressSpaceConfigurationListenerAdapter
	{
		@Override
		public void changedName(final INaviAddressSpace module, final String name)
		{
			m_model.nodeChanged(CAddressSpaceNode.this);
		}
	}

	/**
	 * Updates the node on changes to the address space.
	 */
	private class InternalSpaceListener extends CAddressSpaceListenerAdapter
	{
		// TODO: Added module / Removed module

		@Override
		public void loaded(final INaviAddressSpace addressSpace)
		{
			createChildren();

			m_model.nodeStructureChanged(CAddressSpaceNode.this);
		}
	}
}