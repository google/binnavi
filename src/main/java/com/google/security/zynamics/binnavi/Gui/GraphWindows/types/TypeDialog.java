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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Shows a dialog which allows the user to create a new type or edit an existing one.
 */
public class TypeDialog extends JDialog {

  private final TypeManager typeManager;
  private TypeDialogPanel currentPanel;
  private JTabbedPane tabbedPane;
  private final List<TypeDialogPanel> panels = Lists.newArrayList();

  private AtomicTypePanel atomicTypePanel;
  private PointerTypePanel pointerTypePanel;
  private ArrayTypePanel arrayTypePanel;
  private UnionTypePanel unionTypePanel;
  private StructureTypePanel structureTypePanel;
  private PrototypeTypePanel functionPrototypePanel;

  /**
   * Constructor for showing the dialog to create a new type.
   *
   * @param owner The owner of this dialog.
   * @param typeManager The type typeManager that is used to hold the type system.
   */
  private TypeDialog(final JFrame owner, final TypeManager typeManager) {
    super(owner, "Create new type", true);
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "IE02852: Type typeManager can not be null.");
    createControls(null);
    new CDialogEscaper(this);
  }

  /**
   * Constructor for showing the dialog to edit an existing type.
   *
   * @param owner The owner of this dialog.
   * @param typeManager The type typeManager that is used to hold the type system.
   * @param baseType The type that should be edited in the dialog.
   */
  private TypeDialog(final JFrame owner, final TypeManager typeManager, final BaseType baseType) {
    super(owner, "Edit type", true);
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "IE02853: Type typeManager can not be null.");
    createControls(baseType);
    new CDialogEscaper(this);
  }

  /**
   * Create dialog to build a new type.
   */
  public static TypeDialog createBuildNewTypeDialog(final JFrame owner, final TypeManager manager) {
    return new TypeDialog(owner, manager);
  }

  /**
   * Create dialog to edit an existing type.
   */
  public static TypeDialog createEditTypeDialog(final JFrame owner, final TypeManager manager,
      final BaseType baseType) {
    return new TypeDialog(owner, manager, baseType);
  }

  private void createButtons() {
    final JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    {
      final JButton okButton = new JButton("Ok");
      okButton.addActionListener(new OkActionListener());
      buttonPane.add(okButton);
      getRootPane().setDefaultButton(okButton);
    }
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dispose();
      }
    });
    buttonPane.add(cancelButton);
  }

  private void createControls(final BaseType baseType) {
    if (baseType == null) {
      atomicTypePanel = new AtomicTypePanel(this, typeManager);
      pointerTypePanel = new PointerTypePanel(this, typeManager);
      arrayTypePanel = new ArrayTypePanel(this, typeManager);
      structureTypePanel = new StructureTypePanel(this, typeManager);
      unionTypePanel = new UnionTypePanel(this, typeManager);
      functionPrototypePanel = new PrototypeTypePanel(this, typeManager);
    } else {
      switch (baseType.getCategory()) {
        case ATOMIC:
          atomicTypePanel = new AtomicTypePanel(this, typeManager, baseType);
          break;
        case POINTER:
          pointerTypePanel = new PointerTypePanel(this, typeManager, baseType);
          break;
        case ARRAY:
          arrayTypePanel = new ArrayTypePanel(this, typeManager, baseType);
          break;
        case STRUCT:
          structureTypePanel = new StructureTypePanel(this, typeManager, baseType);
          break;
        case UNION:
          unionTypePanel = new UnionTypePanel(this, typeManager, baseType);
          break;
        case FUNCTION_PROTOTYPE:
          functionPrototypePanel = new PrototypeTypePanel(this, typeManager, baseType);
          break;
        default:
          throw new IllegalStateException("IE02854: Unknown type category.");
      }
    }

    tabbedPane = new JTabbedPane();
    addTab(atomicTypePanel, "Atomic type");
    addTab(pointerTypePanel, "Pointer type");
    addTab(arrayTypePanel, "Array type");
    addTab(structureTypePanel, "Structure type");
    addTab(unionTypePanel, "Union type");
    addTab(functionPrototypePanel, "Function prototype");

    tabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final JTabbedPane pane = (JTabbedPane) e.getSource();
        TypeDialog.this.currentPanel = panels.get(pane.getSelectedIndex());
      }
    });
    setActivePanel(baseType);
    setBounds(100, 100, 516, 325);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(tabbedPane);
    createButtons();
  }

  private void addTab(final TypeDialogPanel panel, final String caption) {
    if (panel != null) {
      tabbedPane.add(panel, caption);
      panels.add(panel);
    }
  }

  private void setActivePanel(final BaseType baseType) {
    if (baseType == null) {
      currentPanel = atomicTypePanel;
      return;
    }
    switch (baseType.getCategory()) {
      case ATOMIC:
        currentPanel = atomicTypePanel;
        break;
      case POINTER:
        currentPanel = pointerTypePanel;
        break;
      case ARRAY:
        currentPanel = arrayTypePanel;
        break;
      case STRUCT:
        currentPanel = structureTypePanel;
        break;
      case UNION:
        currentPanel = unionTypePanel;
        break;
      case FUNCTION_PROTOTYPE:
        currentPanel = functionPrototypePanel;
        break;
      default:
        throw new IllegalStateException("Error: Base type: " + baseType.getCategory().toString()
            + " has no panel type associated to it.");
    }
    tabbedPane.setSelectedComponent(currentPanel);
  }

  private class OkActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      try {
        if (currentPanel.createOrUpdateType()) {
          dispose();
        }
      } catch (CouldntSaveDataException | CouldntDeleteException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
