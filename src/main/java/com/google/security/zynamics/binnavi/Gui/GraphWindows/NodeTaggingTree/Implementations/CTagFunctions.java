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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CViewCommentDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;



/**
 * Contains methods for modifying node tag hierarchies.
 */
public final class CTagFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTagFunctions() {
  }

  /**
   * Appends a new node tag.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag managed where the new tag is created.
   * @param parentTag Parent tag of the new tag.
   */
  public static void appendTag(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> parentTag) {
    try {
      tagManager.addTag(
          parentTag, parentTag.getObject().getId() == 0 ? "New Root Tag" : "New Child Tag");
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00135: " + "Could not append tag";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "It was not possible to append a tag to the tag '%s'.", parentTag.getObject().getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The tag can not be appended."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Deletes a node tag.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager from which the tag is deleted.
   * @param tag The tag to delete.
   */
  public static void deleteTag(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> tag) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to convert delete the node tag '%s'?", tag.getObject().getName()))
        == JOptionPane.YES_OPTION) {
      try {
        tagManager.deleteTag(tag);
      } catch (final CouldntDeleteException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00124: " + "Could not delete tag";
        final String innerDescription = CUtilityFunctions.createDescription(
            String.format("The tag '%s' could not be deleted.", tag.getObject().getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The tag still exists."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Deletes a tag and all of its child tags.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager Tag manager from which the tag is deleted.
   * @param tag The tag to delete.
   */
  public static void deleteTagSubTree(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> tag) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to convert delete the node tag '%s' and all of its children?",
        tag.getObject().getName())) == JOptionPane.YES_OPTION) {
      try {
        tagManager.deleteTagSubTree(tag);
      } catch (final CouldntDeleteException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00125: " + "Could not delete tags";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The tag '%s' and its children could not be deleted.", tag.getObject().getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The tag and its children still exist."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Used to edit the name and description of a tag.
   *
   * @param parent Parent window used for dialogs.
   * @param tag The tag to delete.
   */
  public static void editTag(final JFrame parent, final CTag tag) {
    final CViewCommentDialog dlg =
        new CViewCommentDialog(parent, "Edit Tag", tag.getName(), tag.getDescription());

    dlg.setVisible(true);

    if (!dlg.wasCancelled()) {
      try {
        tag.setName(dlg.getName());
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00126: " + "Could not change tag name";
        final String innerDescription = CUtilityFunctions.createDescription(
            String.format("The name of the tag '%s' could not be changed.", tag.getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The tag name could not be changed."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }

      try {
        tag.setDescription(dlg.getComment());
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00127: " + "Could not change tag description";
        final String innerDescription = CUtilityFunctions.createDescription(
            String.format("The description of the tag '%s' could not be changed.", tag.getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The tag description could not be changed."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Inserts a new node tag.
   *
   * @param parent Parent window used for dialogs.
   * @param tagManager The tag manager that creates and manages the new tag.
   * @param parentTag The parent tag the new tag is appended to.
   */
  public static void insertTag(
      final JFrame parent, final ITagManager tagManager, final ITreeNode<CTag> parentTag) {
    try {
      tagManager.insertTag(parentTag, "New Tag Node");
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00128: " + "Could not insert tag";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "It was not possible to insert a tag between the tag '%s' and its children.",
          parentTag.getObject().getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The new tag could not be inserted."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Removes a given tag from all nodes of a graph.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph whose nodes are untagged.
   * @param tag The tag to remove from all the nodes of the graph.
   */
  public static void removeTagFromAllNodes(
      final JFrame parent, final ZyGraph graph, final CTag tag) {
    final Collection<NaviNode> nodes = GraphHelpers.getNodes(graph);

    try {
      for (final NaviNode n : nodes) {
        n.getRawNode().removeTag(tag);
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00129: " + "Could not remove tag from nodes";
      final String innerDescription = CUtilityFunctions.createDescription(
          String.format("The tag '%s' could not be appended from all nodes.", tag.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The tag was removed from some nodes but not from all."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Removes a tag from all selected nodes of a graph.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph from which the tag is removed.
   * @param tag The tag to remove from the selected nodes.
   */
  public static void removeTagFromSelectedNodes(
      final JFrame parent, final ZyGraph graph, final CTag tag) {
    final Collection<NaviNode> nodes = graph.getSelectedNodes();

    try {
      for (final NaviNode n : nodes) {
        n.getRawNode().removeTag(tag);
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00130: " + "Could not remove tag from nodes";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The tag '%s' could not be removed from the selected nodes.", tag.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The tag was removed from some nodes but not from all."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }
}
