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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;



import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;



/**
 * Contains helper functions for working with tags.
 */
public final class CTagFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTagFunctions() {
  }

  /**
   * Adds a new tag to the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager The tag manager where the tag is stored.
   * @param parentTag The parent tag of the tag. This parameter can be null for root tags.
   * @param name The name of the tag.
   */
  public static void addTag(final JFrame parent, final ITagManager tagManager,
      final ITreeNode<CTag> parentTag, final String name) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, false);
          operation.getProgressPanel().setMaximum(1);

          operation.getProgressPanel().setText("Creating new tag" + ": " + name);

          tagManager.addTag(parentTag, name);

          operation.getProgressPanel().next();
          operation.stop();
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00144: " + "Could not create tag";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  String.format("The new tag '%s' could not be created.", name),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The tag was not created."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        }
      }
    }.start();
  }

  /**
   * Deletes a tag from the database. Child tags of the tag are connected to the parent tag of the
   * tag.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager The tag manager that manages the tag.
   * @param tag The tag to be deleted.
   */
  public static void deleteTag(final JFrame parent, final ITagManager tagManager,
      final TreeNode<CTag> tag) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the tag '%s' from the database?", tag.getObject().getName())) == JOptionPane.YES_OPTION) {
      new Thread() {
        @Override
        public void run() {
          try {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", false, false);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText("Deleting tag" + ": " + tag.getObject().getName());

            tagManager.deleteTag(tag);

            operation.getProgressPanel().next();
            operation.stop();
          } catch (final CouldntDeleteException e) {
            CUtilityFunctions.logException(e);

            final String innerMessage = "E00145: " + "Could not delete tag";
            final String innerDescription =
                CUtilityFunctions.createDescription(
                    String.format("The tag '%s' could not be deleted.", tag.getObject().getName()),
                    new String[] {"There was a problem with the database connection."},
                    new String[] {"The tag was not deleted and can still be used."});

            NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
          }
        }
      }.start();
    }
  }

  /**
   * Deletes a tag and all of its child tags from the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager The tag manager that manages the tag.
   * @param tag The tag to be deleted.
   */
  public static void deleteTagSubtree(final JFrame parent, final ITagManager tagManager,
      final TreeNode<CTag> tag) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the tag '%s' and all of its children from the database?", tag
            .getObject().getName())) == JOptionPane.YES_OPTION) {
      new Thread() {
        @Override
        public void run() {
          try {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", false, false);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText(
                "Deleting tag tree" + ": " + tag.getObject().getName());

            tagManager.deleteTagSubTree(tag);

            operation.getProgressPanel().next();
            operation.stop();
          } catch (final CouldntDeleteException e) {
            CUtilityFunctions.logException(e);

            final String innerMessage = "E00146: " + "Could not delete tag tree";
            final String innerDescription =
                CUtilityFunctions
                    .createDescription(
                        String.format("The tag '%s' and its children could not be deleted.", tag
                            .getObject().getName()),
                        new String[] {"There was a problem with the database connection."},
                        new String[] {"The tag and its children were not deleted and can still be used."});

            NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
          }
        }
      }.start();
    }
  }

  /**
   * Inserts a tag into the tag hierarchy. The new tag is used as the parent tag of the child tags
   * of the old parent tag.
   * 
   * @param parent Parent window used for dialogs.
   * @param tagManager The tag manager that manages the old parent tag and the new tag.
   * @param parentTag The parent tag of the new tag.
   * @param name The name of the new tag.
   */
  public static void insertTag(final JFrame parent, final ITagManager tagManager,
      final TreeNode<CTag> parentTag, final String name) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, false);
          operation.getProgressPanel().setMaximum(1);

          operation.getProgressPanel().setText("Inserting new tag" + ": " + name);

          tagManager.insertTag(parentTag, name);

          operation.getProgressPanel().next();
          operation.stop();
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00147: " + "Could not insert tag";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  String.format("BinNavi could not insert a new tag with the name '%s'.", name),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The tag was not created."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        }
      }
    }.start();
  }
}
