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
package com.google.security.zynamics.binnavi.Gui.plugins;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Plugins.IPluginRegistry;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.PluginConfigItem;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Dialog that is used to configure which plugins to load and in what order.
 *
 * @param <T> The plugin interface which is used by plugins to interface with com.google.security.zynamics.binnavi.
 */
public final class CPluginDialog<T> extends JDialog {
  /**
   * The list box where all available plugins are shown.
   */
  private final PluginListBox<T> pluginList;

  /**
   * Shows the plugin description of the selected plugin.
   */
  private final JTextArea pluginDescriptionTextArea;

  /**
   * Creates a new plugin dialog.
   *
   * @param parent Parent window of the dialog.
   * @param registry Plugin registry object from where the plugin information is taken.
   * @param configFile Provides information about which plugins to load and which plugins to skip.
   */
  public CPluginDialog(final JFrame parent,
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> registry, final ConfigManager configFile) {
    super(parent, "Plugins", true);

    Preconditions.checkNotNull(registry, "IE02058: Plugin Registry can't be null");
    Preconditions.checkNotNull(configFile, "IE02059: Config file can't be null");

    new CDialogEscaper(this);
    setLayout(new BorderLayout());
    final JPanel mainPanel = new JPanel(new BorderLayout());
    final JPanel sortPanel = new JPanel(new GridBagLayout());
    final JButton upButton = new JButton(CActionProxy.proxy(new MoveUpAction()));
    final JButton downButton = new JButton(CActionProxy.proxy(new MoveDownAction()));
    final InternalListener<T> listener = new InternalListener<T>(configFile);
    final GridBagConstraints gbConstraints = new GridBagConstraints();
    gbConstraints.insets = new Insets(3, 3, 3, 3);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 0;
    gbConstraints.weighty = 1.0;
    gbConstraints.weightx = 1.0;
    gbConstraints.gridheight = 1;
    gbConstraints.gridwidth = 1;

    sortPanel.add(upButton, gbConstraints);

    gbConstraints.gridy = 1;

    sortPanel.add(downButton, gbConstraints);

    final JPanel leftPanel = new JPanel(new GridBagLayout());
    final JPanel rightPanel = new JPanel(new BorderLayout());

    pluginDescriptionTextArea = new JTextArea();
    pluginDescriptionTextArea.setEditable(false);
    pluginDescriptionTextArea.setLineWrap(true);
    pluginDescriptionTextArea.setWrapStyleWord(true);

    rightPanel.add(new JScrollPane(pluginDescriptionTextArea), BorderLayout.CENTER);

    pluginList = new PluginListBox<T>(registry, configFile);
    pluginList.addListSelectionListener(listener);

    final GridBagConstraints gbConstraints2 = new GridBagConstraints();

    gbConstraints2.insets = new Insets(3, 3, 3, 3);
    gbConstraints2.gridx = 0;
    gbConstraints2.gridy = 0;
    gbConstraints2.gridheight = 1;
    gbConstraints2.gridwidth = 1;
    gbConstraints2.anchor = GridBagConstraints.NORTH;

    leftPanel.add(sortPanel, gbConstraints2);

    gbConstraints2.weighty = 1.0;
    gbConstraints2.weightx = 1.0;
    gbConstraints2.insets = new Insets(3, 3, 3, 3);
    gbConstraints2.fill = GridBagConstraints.BOTH;
    gbConstraints2.gridx = 1;
    gbConstraints2.gridy = 0;
    gbConstraints2.gridheight = 1;
    gbConstraints2.gridwidth = 2;
    leftPanel.add(new JScrollPane(pluginList), gbConstraints2);

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, rightPanel);

    mainPanel.add(splitPane);

    final GridBagConstraints gbConstraints3 = new GridBagConstraints();

    gbConstraints3.insets = new Insets(3, 3, 3, 3);
    gbConstraints3.gridx = 1;
    gbConstraints3.gridy = 0;
    gbConstraints3.ipadx = 400;
    gbConstraints3.ipady = 200;
    gbConstraints3.gridheight = 1;
    gbConstraints3.gridwidth = 1;
    gbConstraints3.anchor = GridBagConstraints.NORTH;
    gbConstraints3.fill = GridBagConstraints.BOTH;

    add(mainPanel, BorderLayout.CENTER);

    final CPanelTwoButtons panel = new CPanelTwoButtons(listener, "OK", "Cancel");

    getRootPane().setDefaultButton(panel.getFirstButton());

    add(panel, BorderLayout.SOUTH);

    setSize(600, 400);

    setLocationRelativeTo(parent);
  }

  /**
   * Listener that updates the dialog on changes in the GUI.
   */
  private class InternalListener<T> implements ListSelectionListener, ActionListener {
    /**
     * Provides information about which plugins to load and which plugins to skip.
     */
    private final ConfigManager configFile;

    /**
     * Creates a new listener object.
     *
     * @param configFile Provides information about which plugins to load and which plugins to skip.
     */
    public InternalListener(final ConfigManager configFile) {
      this.configFile = configFile;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        // When the OK button is clicked, the new plugin
        // selection is written back to the config file.

        configFile.getGeneralSettings().getPlugins().clear();

        final DefaultListModel model = (DefaultListModel) pluginList.getModel();

        for (int i = 0; i < model.getSize(); i++) {
          @SuppressWarnings("unchecked")
          final PluginItem<T> pluginItem = (PluginItem<T>) model.get(i);

          final PluginConfigItem pluginType = new PluginConfigItem();

          pluginType.setName(pluginItem.getObject().getName());
          pluginType.setGUID(pluginItem.getObject().getGuid());
          pluginType.setLoad(pluginList.isChecked(i));

          configFile.getGeneralSettings().getPlugins().add(pluginType);
        }

        setVisible(false);
        dispose();
      } else if (event.getActionCommand().equals("Cancel")) {
        // Don't write the new plugin settings if the user
        // hits the Cancel button.

        setVisible(false);
        dispose();
      }
    }

    @Override
    public void valueChanged(final ListSelectionEvent event) {
      // Every time a plugin is selected, its description
      // must be shown in the right part of the dialog.

      if (!event.getValueIsAdjusting()) {
        @SuppressWarnings("unchecked")
        final PluginItem<T> pluginItem = (PluginItem<T>) pluginList.getSelectedValue();

        if (pluginItem == null) {
          return;
        }

        pluginDescriptionTextArea.setText(pluginItem.getObject().getDescription());
      }
    }
  }

  /**
   * Action class used by the Down button.
   */
  private class MoveDownAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 7778701107890773452L;

    /**
     * Creates a new action handler for the Down button.
     */
    public MoveDownAction() {
      putValue(SMALL_ICON, new ImageIcon(CMain.class.getResource("data/arrow_down.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      // When the user hits the Down buttin, the currently
      // selected plugin is moved down one position in the plugin list.

      final int selected = pluginList.getSelectedIndex();

      if ((selected == -1) || (selected == (pluginList.getModel().getSize() - 1))) {
        return;
      }

      pluginList.switchElements(selected, selected + 1);
    }
  }

  /**
   * Action class used by the Up button.
   */
  private class MoveUpAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -8853973865690704629L;

    /**
     * Creates a new action handler for the Up button.
     */
    public MoveUpAction() {
      super();

      putValue(SMALL_ICON, new ImageIcon(CMain.class.getResource("data/arrow_up.png")));
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      // When the user hits the Up button, the currently selected
      // plugin is moved up one position in the plugin list.

      final int selected = pluginList.getSelectedIndex();

      if ((selected == -1) || (selected == 0)) {
        return;
      }

      pluginList.switchElements(selected, selected - 1);
    }
  }
}
