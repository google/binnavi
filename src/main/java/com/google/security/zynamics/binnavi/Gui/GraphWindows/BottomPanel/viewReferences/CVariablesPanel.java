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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.BaseTypeTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeMemberTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.EditMemberAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions.EditTypeAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.EditTypeInstanceAction;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode.TypeSubstitutionAction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Panel that shows the variables used by a view.
 */
public final class CVariablesPanel extends JPanel implements IGraphPanelExtension {

  private CGraphModel graphModel;

  private ViewReferencesTable referencesTable;

  /**
   * Highlights instructions in the tree depending on the current selection by the user. Given only
   * a single instruction is selected it zooms the graph to this instruction.
   */
  private final TreeSelectionListener treeSelectionListener = new InternalTreeSelectionListener();

  public CVariablesPanel() {
    super(new BorderLayout());
  }

  private TypeManager getTypeManager() {
    return graphModel.getGraph().getRawView().getConfiguration().getModule().getTypeManager();
  }

  private TypeInstanceContainer getTypeInstanceContainer() {
    return graphModel
        .getGraph()
        .getRawView()
        .getConfiguration()
        .getModule()
        .getContent()
        .getTypeInstanceContainer();
  }

  /**
   * Creates the popup menu for a given node.
   *
   * @param selectedNode The node for which the popup menu is created.
   *
   * @return The created popup menu.
   */
  private JPopupMenu createPopupMenu(final IconNode selectedNode) {
    final JPopupMenu menu = new JPopupMenu();
    if (selectedNode instanceof TypeMemberTreeNode) {
      menu.add(new EditMemberAction(graphModel.getParent(), getTypeManager(),
          ((TypeMemberTreeNode) selectedNode).getTypeMember()));
      return menu;
    }
    if (selectedNode instanceof BaseTypeTreeNode) {
      menu.add(new EditTypeAction(graphModel.getParent(), getTypeManager(),
          ((BaseTypeTreeNode) selectedNode).getBaseType()));
      return menu;
    }
    if (selectedNode instanceof InstructionNode) {
      final ViewReferencesTableModel model = (ViewReferencesTableModel) referencesTable.getModel();
      if (model.getTypeInstanceReference((InstructionNode) selectedNode) != null) {
        menu.add(new EditTypeInstanceAction(graphModel.getParent(), getTypeManager(),
            model.getTypeInstanceReference((InstructionNode) selectedNode).getTypeInstance(),
            getTypeInstanceContainer()));
      }
      if (model.getTypeSubstitution((InstructionNode) selectedNode) != null) {
        try {
          menu.add(TypeSubstitutionAction.instantiateEditTypeSubstitution(graphModel.getParent(),
              getTypeManager(), graphModel
                  .getGraph()
                  .getRawView()
                  .getConfiguration()
                  .getModule()
                  .getContent()
                  .getFunctionContainer()
                  .getFunction(graphModel.getGraph().getRawView().getConfiguration().getName())
                  .getStackFrame(),
              model.getTypeSubstitution((InstructionNode) selectedNode).getOperandTreeNode()));
        } catch (MaybeNullException exception) {
          exception.printStackTrace();
        }
      }
      return menu;
    }
    return null;
  }

  /**
   * Shows a popup menu that depends on the node that was clicked.
   *
   * @param event The event to handle.
   */
  private void showPopupMenu(final MouseEvent event) {
    final IconNode selectedNode =
        (IconNode) TreeHelpers.getNodeAt(referencesTable, event.getX(), event.getY());

    if (selectedNode != null) {
      final JPopupMenu menu = createPopupMenu(selectedNode);

      if (menu != null) {
        menu.show(referencesTable, event.getX(), event.getY());
      }
    }
  }

  @Override
  public void dispose() {
    if (referencesTable != null) {
      referencesTable.dispose();
    }
  }

  @Override
  public void visit(final CGraphModel model, final IGraphPanelExtender extender) {
    graphModel = model;
    if (graphModel.getGraph().getRawView().getConfiguration().getModule() == null) {
      return; // TODO(timkornau): There is no type system support in projects.
    }

    extender.addTab("Variables", this);

    final ViewReferencesTableModel treeModel =
        new ViewReferencesTableModel(model.getGraph().getRawView());

    referencesTable = new ViewReferencesTable(treeModel);

    add(new JScrollPane(referencesTable));

    referencesTable.addTreeSelectionListener(treeSelectionListener);

    // Initialize the handler for showing popup context menus
    referencesTable.addMouseListener(new VariablesPanelMouseAdapter());
  }

  /**
   * Mouse adapter which either triggers zoom on selection of an Instruction node, or creates
   * context specific pop up menus.
   */
  private class VariablesPanelMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      final Object node = referencesTable.getLastSelectedPathComponent();

      if (node instanceof InstructionNode) {
        final InstructionNode vnode = (InstructionNode) node;
        ZyZoomHelpers.zoomToInstruction(graphModel.getGraph(), vnode.getInstruction());
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }

  /**
   * Tree selection listener which handles the selection of single and multiple nodes within the
   * tree. It has support for multiple node selection and single node selection. It will always
   * highlight all Instructions in the selection. If only a single instruction is in the selected
   * tree path the graph is zoomed to this instruction.
   */
  private class InternalTreeSelectionListener implements TreeSelectionListener {

    /**
     * Given a {@link TreePath path} finds all instructions which are below all nodes in all
     * selected paths
     *
     * @param paths Selected paths in the tree.
     * @return The {@link INaviInstruction instructions} which are below all of the currently
     *         selected paths.
     */
    private Set<INaviInstruction> getSelectedVariables(final TreePath[] paths) {
      final Set<INaviInstruction> instructions = Sets.newHashSet();
      if (paths == null) {
        return instructions;
      }

      for (final TreePath treePath : paths) {
        final DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (node instanceof InstructionNode) {
          instructions.add(((InstructionNode) node).getInstruction());
        } else {
          instructions.addAll(findInstructions(node));
        }
      }
      return instructions;
    }

    /**
     * Given a node in the {@link JTree tree} finds all instructions below the given node.
     *
     * @param node The {@link DefaultMutableTreeNode node} which is the root of the sub tree where
     *        to collect all {@link INaviInstruction instructions}.
     * @return The {@link INaviInstruction instructions} below the given
     *         {@link DefaultMutableTreeNode node}.
     */
    private Set<INaviInstruction> findInstructions(final DefaultMutableTreeNode node) {
      final Set<INaviInstruction> instructions = Sets.newHashSet();
      Enumeration<?> enumeration = node.preorderEnumeration();
      while (enumeration.hasMoreElements()) {
        final DefaultMutableTreeNode currentNode =
            (DefaultMutableTreeNode) enumeration.nextElement();
        if (currentNode.isLeaf() && currentNode instanceof InstructionNode) {
          instructions.add(((InstructionNode) currentNode).getInstruction());
        }
      }
      return instructions;
    }

    private void handleSingleNodeSelection(final TreePath[] paths) {
      final DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
      if (node instanceof InstructionNode) {
        CVariableHighlighter.highlightInstructions(graphModel.getGraph(),
            Lists.newArrayList(((InstructionNode) node).getInstruction()));
        return;
      }
      if (node instanceof BaseTypeTreeNode || node instanceof TypeMemberTreeNode) {
        CVariableHighlighter.highlightInstructions(graphModel.getGraph(), findInstructions(node));
        return;
      }
    }

    private void handleMultipleNodeSelection(final TreePath[] paths) {
      CVariableHighlighter.highlightInstructions(graphModel.getGraph(),
          getSelectedVariables(paths));
    }

    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final TreePath[] paths = referencesTable.getSelectionPaths();
      if (paths != null && paths.length == 1) {
        handleSingleNodeSelection(paths);
      } else {
        handleMultipleNodeSelection(paths);
      }
    }
  }
}
