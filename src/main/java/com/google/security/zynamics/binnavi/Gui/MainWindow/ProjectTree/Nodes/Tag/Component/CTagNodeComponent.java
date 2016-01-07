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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component;



import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.Help.CDescriptionHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.Help.CNameHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Tag.Component.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveField;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSavePane;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.Help.CHelpSaveField;
import com.google.security.zynamics.binnavi.Help.CHelpSavePane;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Component that is shown on the right side of the main window when a tag node is selected.
 */
public final class CTagNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6040807788139421729L;

  /**
   * Database that contains the tag to be displayed.
   */
  private final IDatabase m_database;

  /**
   * Tag tree node that contains the information to be displayed.
   */
  private final TreeNode<CTag> m_tagTreeNode;

  /**
   * Field where the tag name can be edited.
   */
  private final CSaveField m_nameTextField;

  /**
   * Field where the tag description can be edited.
   */
  private final CSavePane m_descriptionField;

  /**
   * Table where the child tags of the tag are shown.
   */
  private final CChildTagsTable m_childrenTagTable;

  /**
   * Border where information about the number of child tags is shown.
   */
  private final TitledBorder m_tableBorder;

  /**
   * Button used to save the input back to the module.
   */
  private final JButton m_saveButton = new CHelpButton(CActionProxy.proxy(new SaveAction()),
      new CSaveHelp());

  /**
   * Updates the GUI on relevant changes in the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Updates the GUI on relevant changes in the tag.
   */
  private final InternalTagListener m_tagListener = new InternalTagListener();

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database that contains the tag to be displayed.
   * @param tagTreeNode Tag tree node that contains the information to be displayed.
   */
  public CTagNodeComponent(final JTree projectTree, final IDatabase database,
      final TreeNode<CTag> tagTreeNode) {
    super(new BorderLayout());

    Preconditions.checkNotNull(projectTree, "IE02000: Project tree argument can not be null");

    Preconditions.checkNotNull(tagTreeNode, "IE02001: Tree node argument can not be null");

    m_database = database;

    m_tagTreeNode = tagTreeNode;

    m_nameTextField = new CHelpSaveField(tagTreeNode.getObject().getName(), new CNameHelp());

    m_descriptionField =
        new CHelpSavePane(tagTreeNode.getObject().getDescription(), new CDescriptionHelp());

    m_childrenTagTable = new CChildTagsTable(projectTree, m_tagTreeNode, database);

    m_tableBorder = new TitledBorder(getBorderText());

    createGui();

    m_database.getContent().getViewTagManager().addListener(m_tagManagerListener);

    m_tagTreeNode.getObject().addListener(m_tagListener);

    final UpdateListener updateListener = new UpdateListener();

    m_nameTextField.getDocument().addDocumentListener(updateListener);
    m_descriptionField.getDocument().addDocumentListener(updateListener);

    updateGUI();
  }

  /**
   * Creates the GUI of the component.
   */
  private void createGui() {
    final JPanel outerNamePanel = new JPanel(new BorderLayout());
    outerNamePanel.setBorder(new TitledBorder("Tag"));

    final JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.setBorder(new EmptyBorder(0, 0, 5, 0));
    final JLabel nameLabel = new CHelpLabel("Name" + ":", new CNameHelp());

    nameLabel.setPreferredSize(new Dimension(110, 25));
    namePanel.add(nameLabel, BorderLayout.WEST);
    namePanel.add(m_nameTextField, BorderLayout.CENTER);
    outerNamePanel.add(namePanel, BorderLayout.CENTER);

    final JPanel outerDescriptionPanel = new JPanel(new BorderLayout());
    outerDescriptionPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
    final JPanel descriptionPanel = new JPanel(new BorderLayout());
    descriptionPanel.setBorder(new TitledBorder("Description"));

    descriptionPanel.setMinimumSize(new Dimension(0, 120));
    descriptionPanel.add(new JScrollPane(m_descriptionField));
    outerDescriptionPanel.add(descriptionPanel, BorderLayout.CENTER);

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.add(new JPanel());
    buttonPanel.setBorder(new EmptyBorder(5, 0, 5, 2));
    buttonPanel.add(m_saveButton);

    final JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(outerNamePanel, BorderLayout.NORTH);
    topPanel.add(outerDescriptionPanel, BorderLayout.CENTER);
    topPanel.add(buttonPanel, BorderLayout.SOUTH);

    final JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBorder(m_tableBorder);
    final JScrollPane scrollPane = new JScrollPane(m_childrenTagTable);
    bottomPanel.add(scrollPane, BorderLayout.CENTER);

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
    splitPane.setResizeWeight(0.5);

    add(splitPane);
  }

  /**
   * Creates the border text that gives information about the number of child tags of the tag.
   * 
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d %s", m_tagTreeNode.getChildren().size(), "Child Tags");
  }

  /**
   * Saves the information from the GUI back to the tag object.
   */
  private void save() {
    try {
      m_tagTreeNode.getObject().setName(m_nameTextField.getText());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00182: " + "Could not change tag name";
      final String innerDescription =
          CUtilityFunctions.createDescription(String
              .format("The name of the tag '%s' could not be changed.", m_tagTreeNode.getObject()
                  .getName()), new String[] {"There was a problem with the database connection."},
              new String[] {"The tag name could not be changed."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), innerMessage, innerDescription,
          e);
    }

    try {
      m_tagTreeNode.getObject().setDescription(m_descriptionField.getText());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00183: " + "Could not change tag description";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The description of the tag '%s' could not be changed.", m_tagTreeNode.getObject()
                  .getName()), new String[] {"There was a problem with the database connection."},
              new String[] {"The tag description could not be changed."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), innerMessage, innerDescription,
          e);
    }
  }

  /**
   * Updates the GUI depending on the input state.
   */
  private void updateGUI() {
    final boolean textChanged =
        !m_nameTextField.getText().equals(m_tagTreeNode.getObject().getName());
    final boolean descriptionChanged =
        !m_descriptionField.getText().equals(m_tagTreeNode.getObject().getDescription());

    m_saveButton.setEnabled(textChanged || descriptionChanged);

    m_nameTextField.setModified(textChanged);
    m_descriptionField.setModified(descriptionChanged);
  }

  @Override
  public void dispose() {
    m_database.getContent().getViewTagManager().removeListener(m_tagManagerListener);

    m_tagTreeNode.getObject().removeListener(m_tagListener);

    m_childrenTagTable.dispose();
  }

  /**
   * Updates the GUI on relevant changes in the tag.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      m_descriptionField.setText(tag.getDescription());

      updateGUI();
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      m_nameTextField.setText(tag.getName());

      updateGUI();
    }

    @Override
    public void deletedTag(final CTag tag) {
      // do nothing
    }
  }

  /**
   * Updates the GUI on relevant changes in the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    /**
     * Updates the border text of the component.
     */
    private void updateBorder() {
      m_tableBorder.setTitle(getBorderText());
      updateUI();
    }

    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      updateBorder();
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      updateBorder();
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      updateBorder();
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      updateBorder();
    }
  }

  /**
   * Saves the information from the GUI back into the tag object.
   */
  private class SaveAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2385339704599606246L;

    /**
     * Creates a new action object.
     */
    private SaveAction() {
      super("Save");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      save();
    }
  }

  /**
   * Updates the Save button depending on the input state.
   */
  private class UpdateListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateGUI();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateGUI();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateGUI();
    }
  }
}
