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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.CModuleNodeComponent;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Displays a list of cross references.
 */
public class TypeInstanceReferencesDialog extends JDialog {

  private boolean wasCancelled = true;
  private JTable xrefs;
  private final MouseListener internalMouseListener = new CrossreferenceTableMouseListener();

  public TypeInstanceReferencesDialog(final JFrame owner,
      final List<TypeInstanceReference> references) {
    super(owner, "Cross references", false);
    createControls(references);
    new CDialogEscaper(this);
    pack();
    GuiHelper.centerChildToParent(owner, this, true);
  }

  private void createControls(final List<TypeInstanceReference> references) {
    final JPanel panel = new JPanel();
    getContentPane().add(panel, BorderLayout.SOUTH);
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    final JButton buttonOk = new JButton("OK");
    buttonOk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        wasCancelled = false;
        dispose();
      }
    });
    panel.add(buttonOk);

    final JButton buttonCancel = new JButton("Cancel");
    buttonCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dispose();
      }
    });
    panel.add(buttonCancel);

    xrefs = new JTable(new XRefsTableModel(references));
    xrefs.addMouseListener(internalMouseListener);
    xrefs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    xrefs.setRowSelectionInterval(0, 0);
    final JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
    tablePanel.setBorder(new TitledBorder("Cross references"));
    tablePanel.add(new JScrollPane(xrefs), BorderLayout.CENTER);
    add(tablePanel, BorderLayout.CENTER);
  }

  public TypeInstanceReference getSelectedXRef() {
    if (xrefs.getSelectedRow() != -1) {
      return ((XRefsTableModel) xrefs.getModel()).getXRef(xrefs.getSelectedRow());
    } else {
      return null;
    }
  }

  public boolean wasCancelled() {
    return wasCancelled;
  }

  private class CrossreferenceTableMouseListener implements MouseListener {

    @Override
    public void mouseClicked(final MouseEvent e) {
      final XRefsTableModel model = (XRefsTableModel) xrefs.getModel();
      final TypeInstanceReference reference = model.getXRef(xrefs.getSelectedRow());
      CModuleNodeComponent.focusTypeInstance(reference.getTypeInstance().getModule(),
          reference.getTypeInstance());
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    @Override
    public void mousePressed(final MouseEvent e) {
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }
  }

  private class XRefsTableModel extends AbstractTableModel {

    private final List<TypeInstanceReference> references;
    private final String COLUMN_NAMES[] = {"Source", "Destination", "Section"};
    private static final int SOURCE_COLUMN = 0;
    private static final int DESTINATION_COLUMN = 1;
    private static final int SECTION_COLUMN = 2;

    public XRefsTableModel(final List<TypeInstanceReference> references) {
      this.references = references;
    }

    private String getDestinationAddress(final int rowIndex) {
      return String.format("0x%X", references.get(rowIndex).getTypeInstance().getAddress()
          .getVirtualAddress());
    }

    private String getSection(final int rowIndex) {
      return references.get(rowIndex).getTypeInstance().getSection().getName();
    }

    private String getSourceAddress(final int rowIndex) {
      return references.get(rowIndex).getAddress().toHexString();
    }

    @Override
    public int getColumnCount() {
      return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(final int columnIndex) {
      switch (columnIndex) {
        case SOURCE_COLUMN:
          return "Source address";
        case DESTINATION_COLUMN:
          return "Destination address";
        case SECTION_COLUMN:
          return "Section";
      }
      return "";
    }

    @Override
    public int getRowCount() {
      return references.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
      switch (columnIndex) {
        case SOURCE_COLUMN:
          return getSourceAddress(rowIndex);
        case DESTINATION_COLUMN:
          return getDestinationAddress(rowIndex);
        case SECTION_COLUMN:
          return getSection(rowIndex);
      }
      return null;
    }

    public TypeInstanceReference getXRef(final int rowIndex) {
      return references.get(rowIndex);
    }
  }
}
