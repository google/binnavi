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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.comboboxes.memorybox.JMemoryBox;

/**
 * Text field where the user can enter an address to go to in the graph.
 */
public final class CGotoAddressField extends JMemoryBox {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -944210249806692638L;

  /**
   * The graph the address field refers to.
   */
  private final ZyGraph m_graph;

  /**
   * The List of modules which build the current graph
   */
  private final List<INaviModule> m_modules;

  /**
   * The parent JFrame object
   */
  private final JFrame m_parent;

  /**
   * Zooms to the address the user entered.
   */
  private final InternalActionListener m_listener = new InternalActionListener();

  /**
   * Editor component of the combo box.
   */
  private final CGotoAddressInputField m_textField = new CGotoAddressInputField();

  /**
   * Creates a new Goto Address field.
   * 
   * @param graph The graph the address field refers to.
   * @param modules The list of modules present in the graph
   * @param parent The parent JFrame
   */
  public CGotoAddressField(final ZyGraph graph, final List<INaviModule> modules, final JFrame parent) {
    super(20);

    m_graph = Preconditions.checkNotNull(graph, "IE01811: Graph argument can't be null");
    m_modules = Preconditions.checkNotNull(modules, "IE01176: Modules argument can not be null");
    m_parent = Preconditions.checkNotNull(parent, "IE02845: parent argument can not be null");

    // Code to fix some kind of combo box GUI issue with formatted text fields
    m_textField.setPreferredSize(getPreferredSize());
    m_textField.setBorder(((JTextField) new JComboBox().getEditor().getEditorComponent())
        .getBorder());

    setEditor(new BasicComboBoxEditor() {
      @Override
      protected JTextField createEditorComponent() {
        return m_textField;
      }
    });

    ((JTextField) getEditor().getEditorComponent()).getInputMap().put(
        HotKeys.GRAPH_SEARCH_NEXT_KEY.getKeyStroke(), "ZOOM");
    ((JTextField) getEditor().getEditorComponent()).getActionMap().put("ZOOM",
        new AbstractAction() {
          private static final long serialVersionUID = 4721578747969744911L;

          @Override
          public void actionPerformed(final ActionEvent event) {
            if (m_modules.size() == 1) {
              zoomToAddress();
            } else {
              buildAddressSelectionPopUp();
            }
          }
        });

    addActionListener(m_listener);
  }

  /**
   * In the case of multiple modules zoom to address does not work therefore the user must select in
   * which module to search.
   */
  private void buildAddressSelectionPopUp() {
    final CAddressSelectionDialog dlg = new CAddressSelectionDialog(m_parent, m_modules);

    dlg.setVisible(true);

    final INaviModule result = dlg.getSelectionResult();

    final IAddress address = new CAddress(Long.parseLong(getText(), 16));

    ZyZoomHelpers.zoomToAddress(m_graph, address, result, true);
  }

  /**
   * Returns the currently entered text.
   * 
   * @return The currently entered text.
   */
  private String getText() {
    return ((JTextField) getEditor().getEditorComponent()).getText();
  }

  /**
   * Zooms to the current address.
   */
  private void zoomToAddress() {
    if (!"".equals(getText())) {
      add(getText());

      final IAddress address = new CAddress(Long.parseLong(getText(), 16));

      m_textField.setSuccessful(ZyZoomHelpers.zoomToAddress(m_graph, address, m_modules.get(0),
          true));
    }
  }

  /**
   * Zooms to the address the user entered.
   */
  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      zoomToAddress();
    }
  }
}
