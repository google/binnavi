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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations;

import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Contains methods for tagging nodes.
 */
public final class CTaggingFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTaggingFunctions() {
  }

  /**
   * Returns the currently selected tag of a tags tree.
   *
   * @param tagsTree The tags tree.
   *
   * @return The currently selected tag.
   *
   * @throws MaybeNullException Thrown if no tag is selected.
   */
  public static CTag getSelectedTag(final JTree tagsTree) throws MaybeNullException {
    final TreePath selectionPath = tagsTree.getSelectionPath();

    if (selectionPath != null && selectionPath.getLastPathComponent() instanceof CTagTreeNode
        && selectionPath.getLastPathComponent() instanceof CTagTreeNode) {
      final CTagTreeNode ttn = (CTagTreeNode) selectionPath.getLastPathComponent();

      return ttn.getTag().getObject();
    }

    throw new MaybeNullException();
  }

  /**
   * Tags a node with a given tag.
   *
   * @param parent Parent window used for dialogs.
   * @param node The node to tag.
   * @param tag Tag the node is tagged with.
   */
  public static void tagNode(final JFrame parent, final NaviNode node, final CTag tag) {
    try {
      node.getRawNode().tagNode(tag);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00131: " + "Could not tag node";
      final String innerDescription = CUtilityFunctions.createDescription(
          String.format("The selected node could not be tagged with the tag '%s'.", tag.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The node remains untagged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Tags a node with the currently selected tag of a tags tree.
   *
   * @param parent Parent window used for dialogs.
   * @param tagsTree Tags tree that provides the tag used for tagging.
   * @param node Node to be tagged.
   */
  public static void tagNode(final JFrame parent, final JTree tagsTree, final NaviNode node) {
    try {
      tagNode(parent, node, getSelectedTag(tagsTree));
    } catch (final MaybeNullException exception) {
      // If there is no selected tag, then we can not tag a node with it.
    }
  }

  /**
   * Tags all selected nodes with the currently selected tag of a tags tree.
   *
   * @param parent Parent window used for dialogs.
   * @param tagsTree Provides the tag for tagging.
   * @param graph Graph whose selected nodes are tagged.
   */
  public static void tagSelectedNodes(
      final JFrame parent, final JTree tagsTree, final ZyGraph graph) {
    try {
      tagSelectedNodes(parent, graph, getSelectedTag(tagsTree));
    } catch (final MaybeNullException exception) {
      // There is no selected tag.
    }
  }

  /**
   * Tags all selected nodes of a graph with a given tag.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph whose nodes are tagged.
   * @param tag The tag to add to all selected nodes.
   */
  public static void tagSelectedNodes(final JFrame parent, final ZyGraph graph, final CTag tag) {
    final Collection<NaviNode> selectedNodes = graph.getSelectedNodes();

    try {
      for (final NaviNode node : selectedNodes) {
        node.getRawNode().tagNode(tag);
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00134: " + "Could not tag selected nodes";
      final String innerDescription = CUtilityFunctions.createDescription(
          String.format("The selected nodes could not be tagged with the tag '%s'.", tag.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"Some nodes were tagged while other remain untagged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }
}
