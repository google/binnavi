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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.standardplugins.utils.CDialogEscaper;
import com.google.security.zynamics.binnavi.standardplugins.utils.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreePath;

/**
 * The pathfinding dialog is used to let the user select a start block and an end block from the
 * list of all basic blocks of a module.
 */
public final class PathfindingDialog extends JDialog {

  /**
   * The function tree located on the left side of the window.
   */
  private final FunctionTree startBlockTree;

  /**
   * The function tree located on the right side of the window.
   */
  private final FunctionTree endBlockTree;

  /**
   * The function selected in the left function tree.
   */
  private Function startFunction;

  /**
   * The function selected in the right function tree.
   */

  private Function endFunction;
  /**
   * The block selected in the left function tree.
   */
  private BasicBlock startBlock;

  /**
   * The block selected in the right function tree.
   */
  private BasicBlock endBlock;

  /**
   * Flag that indicates how the dialog was closed.
   */
  private boolean wasCancelled = true;

  private final InternalListener m_listener = new InternalListener();

  /**
   * Creates a new pathfinding dialog object.
   *
   * @param module The module that provides the basic blocks to choose from.
   */
  public PathfindingDialog(final JFrame parent, final Module module) {
    super(parent, "Pathfinding", true);

    // Provide ESC key functionality
    new CDialogEscaper(this);

    // Create the GUI
    setLayout(new BorderLayout());

    final JPanel topPanel = new JPanel(new BorderLayout());

    // This tree is used to select the start block.
    startBlockTree = new FunctionTree(this, module);

    // This tree is used to select the end block.
    endBlockTree = new FunctionTree(this, module);

    // This field is used to display the assembler code of
    // the selected start block.
    final JTextArea startBlockAsmField = createAsmField();

    // This field is used to display the assembler code of
    // the selected end block.
    final JTextArea endBlockAsmField = createAsmField();

    final JTextArea searchFieldStart = new JTextArea(1, 10);
    final JTextArea searchFieldEnd = new JTextArea(1, 10);
    searchFieldStart.getDocument()
        .addDocumentListener(new InternalDocumentListener(startBlockTree));
    searchFieldEnd.getDocument().addDocumentListener(new InternalDocumentListener(endBlockTree));

    // Listeners to update the assembler fields when the selection changes.
    startBlockTree.addTreeSelectionListener(new InternalTreeSelectionListener(startBlockAsmField));
    endBlockTree.addTreeSelectionListener(new InternalTreeSelectionListener(endBlockAsmField));

    final JSplitPane splitPaneSearch = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(searchFieldStart),
        new JScrollPane(searchFieldEnd));
    splitPaneSearch.setResizeWeight(0.5);
    final JSplitPane splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(startBlockTree),
        new JScrollPane(endBlockTree));
    splitPane.setResizeWeight(0.5);

    final JPanel upperPanel = new JPanel(new BorderLayout());
    upperPanel.add(splitPane);
    upperPanel.add(splitPaneSearch, BorderLayout.NORTH);

    final JPanel previewPanel = new JPanel(new BorderLayout());
    final JSplitPane splitPane2 = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(startBlockAsmField),
        new JScrollPane(endBlockAsmField));
    splitPane2.setResizeWeight(0.5);
    previewPanel.add(splitPane2);
    upperPanel.add(previewPanel, BorderLayout.SOUTH);
    topPanel.add(upperPanel);
    splitPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(topPanel, BorderLayout.CENTER);
    add(new CPanelTwoButtons(m_listener, "OK", "Cancel"), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(800, 500));
    pack();
  }

  /**
   * Creates a text field that can be used to display assembler code.
   *
   * @return The created text field.
   */
  private JTextArea createAsmField() {
    final JTextArea asmField = new JTextArea(10, 30);

    asmField.setFont(GuiHelper.MONOSPACED_FONT);
    asmField.setEditable(false);

    return asmField;
  }

  /**
   * Returns the end block selected by the user. This can be null.
   *
   * @return The selected end block or null.
   */
  public BasicBlock getEndBlock() {
    return endBlock;
  }

  /**
   * Returns the end function selected by the user. This can be null.
   *
   * @return The selected function or null.
   */
  public Function getEndFunction() {
    return endFunction;
  }

  /**
   * Returns the start block selected by the user. This can be null.
   *
   * @return The selected start block or null.
   */
  public BasicBlock getStartBlock() {
    return startBlock;
  }

  /**
   * Returns the start function selected by the user. This can be null.
   *
   * @return The selected start function or null.
   */
  public Function getStartFunction() {
    return startFunction;
  }

  /**
   * Returns a flag that indicates whether the dialog was closed by clicking on Cancel or not.
   *
   * @return True, if the dialog was canceled.
   */
  public boolean wasCancelled() {
    return wasCancelled;
  }

  /**
   * This class is used to handle updates of the trees as the user is typing a matching function
   * name
   */
  private static class InternalDocumentListener implements DocumentListener {
    private final JTree m_tree;

    public InternalDocumentListener(final JTree tree) {
      m_tree = tree;
    }

    /**
     * Update the tree control according to the given text
     *
     * @param e The document event generated by the corresponding text field
     */
    private void updateTreeModel(final DocumentEvent e) {
      try {
        final String s = e.getDocument().getText(0, e.getDocument().getLength());
        final FilteredTreeModel treeModel = (FilteredTreeModel) m_tree.getModel();
        treeModel.setFilter(new TextPatternFilter(s));
      } catch (final BadLocationException e1) {
        Logger.logException(e1);
      }
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
      updateTreeModel(e);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
      updateTreeModel(e);
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
      updateTreeModel(e);
    }
  }

  /**
   * This class handles clicks on the buttons of the dialog.
   */
  private class InternalListener implements ActionListener {
    /**
     * Returns the selected block of a function tree.
     *
     * @param tree The tree to check.
     *
     * @return The selected basic block or null if no basic block is selected.
     */
    private BasicBlock getSelectedBlock(final FunctionTree tree) {
      final TreePath path = tree.getSelectionPath();

      if (path == null) {
        return null;
      }

      final Object component = path.getLastPathComponent();

      if (component instanceof FunctionTreeBlockNode) {
        return ((FunctionTreeBlockNode) component).getBasicBlock();
      }

      return null;
    }

    /**
     * Returns the selected function of a function tree.
     *
     * @param tree The tree to check.
     *
     * @return The selected function or null if no function is selected.
     */
    private Function getSelectedFunction(final FunctionTree tree) {
      final TreePath path = tree.getSelectionPath();

      if (path == null) {
        return null;
      }

      final Object component = path.getLastPathComponent();

      if (component instanceof FunctionTreeFunctionNode) {
        return ((FunctionTreeFunctionNode) component).getFunction();
      }

      // if (component instanceof FunctionTreeBlockNode) {
      // return ((FunctionTreeFunctionNode) path.getPathComponent(path.getPathCount() - 2))
      // .getFunction();
      // }
      return null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      if (e.getActionCommand().equals("OK")) {
        // If the user clicked the OK button the currently
        // selected nodes are determined and written into
        // the member variables.

        startBlock = getSelectedBlock(startBlockTree);
        endBlock = getSelectedBlock(endBlockTree);
        startFunction = getSelectedFunction(startBlockTree);
        endFunction = getSelectedFunction(endBlockTree);

        wasCancelled = false;
      }

      startBlockTree.dispose();
      endBlockTree.dispose();

      dispose();
    }
  }

  /**
   * This class is used to update the content of the assembler code text fields when the selection
   * in one of the trees changes.
   */
  private static class InternalTreeSelectionListener implements TreeSelectionListener {
    private final JTextArea m_field;

    public InternalTreeSelectionListener(final JTextArea field) {
      m_field = field;
    }

    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      // The tree selection changed. Update the assembler code
      // depending on the new node selection.

      final TreePath path = event.getPath();

      if (path == null) {
        return;
      }

      final Object selectedNode = path.getLastPathComponent();

      // Every selectable node in the tree is either of type FunctionTreeContainerNode
      // or of type FunctionTreeFunctionNode. Only nodes of the first
      // type contain assembler code that is displayed.

      if (selectedNode instanceof FunctionTreeBlockNode) {
        final FunctionTreeBlockNode blockNode = (FunctionTreeBlockNode) selectedNode;

        final BasicBlock basicBlock = blockNode.getBasicBlock();

        // Generate the assembler code string for the instructions of
        // the selected basic block and show them in the text field.

        final StringBuilder text = new StringBuilder("");

        for (final Instruction instruction : basicBlock.getInstructions()) {
          text.append(instruction.toString() + "\n");
        }

        m_field.setText(text.toString());

        m_field.setCaretPosition(0);
      } else {
        // Do not show anything if a function node is selected.

        m_field.setText("");
      }
    }
  }
}
