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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;



/**
 * Panel shown in the criteria dialog when Tag state criteria nodes are selected.
 */
public final class CTagCriteriumPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2231647585848005777L;

  /**
   * The criterium edited in this panel.
   */
  private final CTagCriterium m_criterium;

  /**
   * Provides the necessary tagging information.
   */
  private final ITagManager m_tagManager;

  /**
   * Shows all available tags.
   */
  private final JTree m_tagTree = new JTree();

  /**
   * Used to select whether nodes with any tag are to be selected or whether nodes with a special
   * tag are to be selected.
   */
  private final JCheckBox m_anyTagBox = new JCheckBox("Any Tag");

  /**
   * Updates the GUI on user input.
   */
  private final InternalTreeSelectionListener m_selectionListener =
      new InternalTreeSelectionListener();

  /**
   * Updates the GUI on user input.
   */
  private final InternalCheckboxChangeListener m_checkboxListener =
      new InternalCheckboxChangeListener();

  /**
   * Updates the tree on changes to the tags.
   */
  private final ITagManagerListener m_listener = new InternalTagManagerListener();

  /**
   * Creates a new panel object.
   *
   * @param tagCriterium The criterium edited in this panel.
   * @param tagManager Provides the necessary tagging information.
   */
  public CTagCriteriumPanel(final CTagCriterium tagCriterium, final ITagManager tagManager) {
    super(new BorderLayout());

    m_criterium = tagCriterium;
    m_tagManager = tagManager;

    m_tagTree.addTreeSelectionListener(m_selectionListener);

    m_anyTagBox.addChangeListener(m_checkboxListener);

    initPanel(tagManager.getRootTag());

    tagManager.addListener(m_listener);
  }

  /**
   * Creates the tree that shows the available tags.
   *
   * @param root Root node of the tag tree to show.
   */
  private void createTree(final ITreeNode<CTag> root) {
    m_tagTree.setRootVisible(false);

    final DefaultTreeModel tagTreeModel = new DefaultTreeModel(new CTagTreeNode(root));

    m_tagTree.setModel(tagTreeModel);

    m_tagTree.setCellRenderer(new IconNodeRenderer());
  }

  /**
   * Initializes the GUI of the tag.
   *
   * @param rootTag Root node of the tag tree to show.
   */
  private void initPanel(final ITreeNode<CTag> rootTag) {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Tag Condition"));

    createTree(rootTag);

    final JScrollPane pane = new JScrollPane(m_tagTree);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    final JPanel anyTagPanel = new JPanel();
    anyTagPanel.add(m_anyTagBox);

    mainPanel.add(pane, BorderLayout.CENTER);
    mainPanel.add(m_anyTagBox, BorderLayout.SOUTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_tagTree.removeTreeSelectionListener(m_selectionListener);
    m_anyTagBox.removeChangeListener(m_checkboxListener);

    m_tagManager.removeListener(m_listener);
  }

  /**
   * Returns the currently selected tag.
   *
   * @return The currently selected tag or null if no tag is selected.
   */
  public CTag getTag() {
    final TreePath path = m_tagTree.getSelectionPath();

    return path == null ? null : ((CTagTreeNode) path.getLastPathComponent()).getTag();
  }

  /**
   * Returns whether any tag should match.
   *
   * @return True, if any tag should match. False, if only the selected tag should match.
   */
  public boolean isAny() {
    return m_anyTagBox.isSelected();
  }

  /**
   * Updates the GUI on user input.
   */
  private class InternalCheckboxChangeListener implements ChangeListener {
    @Override
    public void stateChanged(final ChangeEvent arg0) {
      m_criterium.notifyListeners();
    }
  }

  /**
   * Updates the tree on changes to the tags.
   */
  private class InternalTagManagerListener implements ITagManagerListener {
    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      createTree(manager.getRootTag());
    }

    @Override
    public void deletedTag(
        final CTagManager manager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      createTree(manager.getRootTag());
    }

    @Override
    public void deletedTagSubtree(
        final CTagManager manager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      createTree(manager.getRootTag());
    }

    @Override
    public void insertedTag(
        final CTagManager manager, final ITreeNode<CTag> parent, final ITreeNode<CTag> tag) {
      createTree(manager.getRootTag());
    }
  }

  /**
   * Updates the GUI on user input.
   */
  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      m_criterium.notifyListeners();
    }
  }
}
