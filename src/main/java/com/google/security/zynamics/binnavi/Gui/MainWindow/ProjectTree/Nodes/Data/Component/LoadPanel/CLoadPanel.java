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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.LoadPanel;



import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.LoadPanel.Implementations.CDataFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.CFileChooser;

/**
 * Panel that is used to associate binary data with a module.
 */
public final class CLoadPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3363342903629907902L;

  /**
   * Button used to store module data in the database.
   */
  private final JButton m_storeButton;

  /**
   * Creates a new load panel object.
   * 
   * @param module Module that is updated by the panel.
   */
  public CLoadPanel(final INaviModule module) {
    Preconditions.checkNotNull(module, "IE01961: Module argument can not be null");

    m_storeButton = new JButton(CActionProxy.proxy(new CStoreAction(module)));

    setLayout(new BorderLayout());

    final JPanel innerPanelLeft = new JPanel(new BorderLayout());

    innerPanelLeft.add(new JButton(CActionProxy.proxy(new CLoadDatabaseAction(module))),
        BorderLayout.WEST);

    add(innerPanelLeft, BorderLayout.WEST);

    final JPanel innerPanel = new JPanel(new BorderLayout());

    final JPanel loadButtonPanel = new JPanel(new BorderLayout());
    loadButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 3));
    loadButtonPanel.add(new JButton(CActionProxy.proxy(new CLoadAction(module))),
        BorderLayout.CENTER);

    innerPanel.add(loadButtonPanel, BorderLayout.WEST);
    innerPanel.add(m_storeButton, BorderLayout.EAST);

    add(innerPanel, BorderLayout.EAST);

    setBorder(new TitledBorder("Data Options"));
  }

  /**
   * Asks the user to select an input file from which the binary data is read.
   * 
   * @param parent Parent window used for dialogs.
   * 
   * @return The name of the selected file or null if no file was selected.
   */
  private static String selectAssociatedFile(final Window parent) {
    final CFileChooser chooser = new CFileChooser();
    chooser.setDialogTitle("Select the input file");

    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getAbsolutePath();
    }

    return null;
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    // Nothing to dispose
  }

  /**
   * Action class used for loading data from a file.
   */
  private class CLoadAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 4173682656470933076L;

    /**
     * Module whose data is updated from the file data.
     */
    private final INaviModule m_module;

    /**
     * Creates a new action object.
     * 
     * @param module Module whose data is updated from the file data.
     */
    public CLoadAction(final INaviModule module) {
      super("Load");

      putValue(Action.SMALL_ICON, new ImageIcon(CMain.class.getResource("data/folder.png")));

      m_module = module;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      final String filename =
          selectAssociatedFile(SwingUtilities.getWindowAncestor(CLoadPanel.this));

      if (filename != null) {
        CDataFunctions.loadFromFile(SwingUtilities.getWindowAncestor(CLoadPanel.this), m_module,
            filename);
      }
    }
  }

  /**
   * Action class for loading module data from the database.
   */
  private class CLoadDatabaseAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 6407054355352505148L;

    /**
     * Module whose data is updated from the database data.
     */
    private final INaviModule m_module;

    /**
     * Creates a new action object.
     * 
     * @param module Module whose data is updated from the database data.
     */
    public CLoadDatabaseAction(final INaviModule module) {
      super("Load");

      m_module = module;

      putValue(Action.SMALL_ICON,
          new ImageIcon(CMain.class.getResource("data/load_from_database.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CDataFunctions.loadFromDatabase(SwingUtilities.getWindowAncestor(CLoadPanel.this), m_module);
    }
  }

  /**
   * Action class used for saving data to the database.
   */
  private class CStoreAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -7058938411200766428L;

    /**
     * Module whose data is stored in the database.
     */
    private final INaviModule m_module;

    /**
     * Creates a new action object.
     * 
     * @param module Module whose data is stored in the database.
     */
    public CStoreAction(final INaviModule module) {
      super("Store");

      m_module = module;

      putValue(Action.SMALL_ICON,
          new ImageIcon(CMain.class.getResource("data/save_to_database.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CDataFunctions.storeData(SwingUtilities.getWindowAncestor(CLoadPanel.this), m_module);
    }
  }
}
