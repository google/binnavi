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
package com.google.security.zynamics.binnavi.Gui.GraphSettings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Graph settings dialog where the user can configure layout and behavior of graphs.
 */
public final class CGraphSettingsDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3874204308656158839L;

  /**
   * Width of the dialog.
   */
  private static final int FRAME_WIDTH = 650;

  /**
   * Height of the dialog.
   */
  private static final int FRAME_HEIGHT = 275;

  /**
   * List of settings panels shown in the dialog.
   */
  private final List<CAbstractSettingsPanel> m_settingsPanel =
      new ArrayList<CAbstractSettingsPanel>();

  /**
   * Graph settings that are used by the dialog.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Flag that indicates whether the dialog was canceled or closed through the OK button.
   */
  private boolean m_wasCanceled = true;

  /**
   * Flag that indicates whether graph settings need to be changed that require a layouting step.
   */
  private boolean m_needsLayouting = false;

  /**
   * Creates a new settings dialog.
   * 
   * @param parent Parent window of the settings dialog.
   * @param topic Title of the dialog.
   * @param settings Graph settings that are used by the dialog.
   * @param isDefaultSettingsDialog True, to indicate that the settings dialog is a default settings
   *        dialog.
   * @param isCallgraph True, to indicate that the settings dialog is used for call graphs. False,
   *        otherwise.
   */
  public CGraphSettingsDialog(final JFrame parent, final String topic,
      final ZyGraphViewSettings settings, final boolean isDefaultSettingsDialog,
      final boolean isCallgraph) {
    super(parent, topic, true);

    Preconditions.checkNotNull(parent, "IE01586: Parent argument can not be null");
    Preconditions.checkNotNull(topic, "IE01587: Topic argument can not be null");
    Preconditions.checkNotNull(settings, "IE01588: Settings argument can not be null");

    m_settings = settings;

    new CDialogEscaper(this);

    setLayout(new BorderLayout());
    add(createTabbedPane(isDefaultSettingsDialog, isCallgraph), BorderLayout.CENTER);
    add(new CPanelTwoButtons(new InternalListener(), "OK", "Cancel"), BorderLayout.SOUTH);

    pack();
    GuiHelper.centerChildToParent(parent, this, true);
  }

  /**
   * Adds a single tab to the tabbed pane that displays the individual settings.
   * 
   * @param tab The tab the panel is added to.
   * @param tabHeader The header string of the tab.
   * @param panel The panel to add to the tab.
   */
  private void addTab(final JTabbedPane tab, final String tabHeader,
      final CAbstractSettingsPanel panel) {
    final JPanel parentPanel = new JPanel(new BorderLayout());

    parentPanel.add(panel, BorderLayout.NORTH);

    tab.addTab(tabHeader, new JScrollPane(parentPanel));

    m_settingsPanel.add(panel);
  }

  /**
   * Creates the tabbed pane that is used to display the individual settings tabs.
   * 
   * @param isDefaultSettingsDialog True, to indicate that the settings dialog is a default settings
   *        dialog.
   * @param isCallgraph True, to indicate that the settings dialog is used for call graphs. False,
   *        otherwise.
   * 
   * @return The created pane.
   */
  private JTabbedPane createTabbedPane(final boolean isDefaultSettingsDialog,
      final boolean isCallgraph) {
    final JTabbedPane tab = new JTabbedPane();

    addTab(tab, "Automatism", new CAutomatismPanel(m_settings, isDefaultSettingsDialog));
    addTab(tab, "Edges", new CEdgePanel(m_settings));
    addTab(tab, "Hierarchic", new CHierarchicPanel(m_settings));
    addTab(tab, "Orthogonal", new COrthogonalPanel(m_settings));
    addTab(tab, "Circular", new CCircularPanel(m_settings));

    if (!isCallgraph) {
      addTab(tab, "Disassembly", new CDisassemblyPanel(m_settings));
    }

    addTab(tab, "Controls", new CControlsPanel(m_settings));
    addTab(tab, "Miscellaneous",
        new CMiscPanel(m_settings, !isDefaultSettingsDialog || isCallgraph));

    tab.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

    return tab;
  }

  /**
   * Returns whether the graph needs to be layouted again to make the changes happen.
   * 
   * @return True, to force a layout. False, to skip a layout.
   */
  public boolean needsLayouting() {
    return m_needsLayouting;
  }

  /**
   * Returns a flag that tells whether the dialog was closed through the OK button or whether it was
   * canceled.
   * 
   * @return True, if the dialog was canceled. False, otherwise.
   */
  public boolean wasCanceled() {
    return m_wasCanceled;
  }

  /**
   * Keeps track of the OK and Cancel buttons and acts accordingly if either of them is pressed.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        m_wasCanceled = false;

        for (final CAbstractSettingsPanel panel : m_settingsPanel) {
          m_needsLayouting |= panel.updateSettings(m_settings);
        }
      }

      dispose();
    }
  }
}
