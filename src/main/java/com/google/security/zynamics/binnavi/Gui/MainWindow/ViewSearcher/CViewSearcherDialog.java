/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ViewSearcher;



import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.GraphSelectionDialog.CGraphSelectionTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Dialog class that is used to search for functions and views by offset.
 *
 * Justification: The user wants to find functions with certain offsets. It is also necessary to let
 * the user choose from one of many functions in case the user needs to choose where to continue
 * (for example during debugging).
 */
public final class CViewSearcherDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6689975974705708788L;

  /**
   * Contains the views to search through.
   */
  private final IViewContainer m_viewContainer;

  /**
   * The function or view that was selected by the user.
   */
  private INaviView m_selectionResult = null;

  /**
   * The table that is used to display functions and views.
   */
  private JTable m_table;

  /**
   * Table model that is used to display the retrieved views.
   */
  private final CGraphSelectionTableModel tableModel = new CGraphSelectionTableModel(
      new ArrayList<INaviView>());

  /**
   * The text input field that is used by the user to enter offsets.
   */
  private final JFormattedTextField m_offsetField = new JFormattedTextField(
      new DefaultFormatterFactory(new CHexFormatter(8)));

  /**
   * Listener that handles user input.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Creates a new dialog.
   *
   * @param owner Parent frame of the dialog.
   * @param viewContainer View container that contains the views to search through.
   * @param address The initial address to search for. This argument can be null.
   */
  public CViewSearcherDialog(final Window owner, final IViewContainer viewContainer,
      final IAddress address) {
    super(owner, "Select a graph", ModalityType.APPLICATION_MODAL);

    Preconditions.checkNotNull(viewContainer, "IE02057: View container can't be null");

    m_viewContainer = viewContainer;

    createGui();

    new CDialogEscaper(this);

    GuiHelper.centerChildToParent(owner, this, true);

    if (address != null) {
      m_offsetField.setText(address.toHexString());
      search(address.toLong());
    }
  }

  /**
   * Cleans up the dialog and hides it.
   */
  private void closeDialog() {
    m_table.removeMouseListener(m_listener);

    dispose();
  }

  /**
   * Creates the GUI of the dialog.
   */
  private void createGui() {
    setLayout(new BorderLayout());

    final JPanel panel = new JPanel(new BorderLayout());

    final JLabel lbl = new JLabel("Address" + ":");

    lbl.setBorder(new EmptyBorder(5, 5, 5, 5));

    panel.add(lbl, BorderLayout.WEST);

    m_offsetField.setSize(400, 20);

    final ActionListener listener = new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        search();
      }
    };

    m_offsetField.addActionListener(listener);

    panel.add(m_offsetField, BorderLayout.CENTER);

    panel.add(new JButton(CActionProxy.proxy(new SearchAction(this))), BorderLayout.EAST);

    add(panel, BorderLayout.NORTH);

    m_table = new JTable(tableModel);

    m_table.addMouseListener(m_listener);

    add(new JScrollPane(m_table), BorderLayout.CENTER);

    add(new CPanelTwoButtons(CActionProxy.proxy(new InternalActionListener()), "OK", "Cancel"),
        BorderLayout.SOUTH);

    setSize(500, 300);
  }

  /**
   * Searches for views with the given offset.
   *
   * @param offset The offset to search for.
   */
  private void search(final long offset) {
    try {
      tableModel.setViews(m_viewContainer.getViewsWithAddresses(
          Lists.newArrayList(new UnrelocatedAddress(new CAddress(offset))), true));
    } catch (final CouldntLoadDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00190: " + "Could not search views";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not determine what views contain the offset '%s'.", new CAddress(
                  offset).toHexString()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The views with the given address can not be shown."});

      NaviErrorDialog.show(null, innerMessage, innerDescription, e);
    }
  }

  /**
   * Sets the return value of the dialog to the selected view.
   */
  private void setSelectedElement() {
    final int selectedRow = m_table.getSelectedRow();


    if (selectedRow != -1) {
      final CGraphSelectionTableModel model = (CGraphSelectionTableModel) m_table.getModel();

      m_selectionResult = model.getViews().get(selectedRow);
    }
  }

  /**
   * Returns the view that was selected by the user. This value can be null if no view was selected.
   *
   * @return The view that was selected by the user.
   */
  public INaviView getSelectionResult() {
    return m_selectionResult;
  }

  /**
   * Searches for the view that contain the offset entered by the user.
   */
  public void search() {
    final long offset = Long.valueOf(m_offsetField.getText(), 16);

    search(offset);
  }

  /**
   * Action handler for the buttons of the dialog.
   */
  private class InternalActionListener extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -3385023274674137932L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      // Updates the selected element in case the user hits the OK button.

      if (event.getActionCommand().equals("OK")) {
        setSelectedElement();
      }

      closeDialog();
    }
  }

  /**
   * Listener used to handle clicks on the views table.
   */
  private class InternalListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      // Updates the selected element in case the user double clicks on
      // a view in the table.

      if ((event.getButton() == 1) && (event.getClickCount() == 2)) {
        setSelectedElement();
      }

      if (m_selectionResult != null) {
        closeDialog();
      }
    }
  }
}
