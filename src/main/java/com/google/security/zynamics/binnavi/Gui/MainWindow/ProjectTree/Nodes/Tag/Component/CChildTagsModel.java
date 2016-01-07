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



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.Tagging.ITagManagerListener;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Table model class used to display information about the child tags of a tag.
 */
public final class CChildTagsModel extends CAbstractTreeTableModel<CTag> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5680955818776214499L;

  /**
   * Index of the column where the child tag names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where the child tag descriptions are shown.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column where the number of child tags are shown.
   */
  private static final int CHILDREN_COUNT_COLUMN = 2;

  /**
   * Names of the columns of the model.
   */
  private final String[] COLUMNS = {"Name", "Description", "Children"};

  /**
   * Database where the tags are stored.
   */
  private final IDatabase m_database;

  /**
   * Root tag of the child tags shown in the table.
   */
  private final ITreeNode<CTag> m_tagTreeNode;

  /**
   * Updates the table model on changes in the tag manager.
   */
  private final InternalTagManagerListener m_tagManagerListener = new InternalTagManagerListener();

  /**
   * Updates the table model on changes in tags.
   */
  private final InternalTagListener m_tagListener = new InternalTagListener();

  /**
   * Creates a new table model object.
   * 
   * @param database Database where the tags are stored.
   * @param tagTreeNode Root tag of the child tags shown in the table.
   */
  public CChildTagsModel(final IDatabase database, final ITreeNode<CTag> tagTreeNode) {
    m_tagTreeNode = Preconditions.checkNotNull(tagTreeNode, "IE01996: Tag argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01997: Database argument can't be null");

    m_database.getContent().getViewTagManager().addListener(m_tagManagerListener);
    m_tagTreeNode.getObject().addListener(m_tagListener);

  }

  @Override
  public void delete() {
    m_database.getContent().getViewTagManager().removeListener(m_tagManagerListener);

    m_tagTreeNode.getObject().removeListener(m_tagListener);
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  @Override
  public int getRowCount() {
    return m_tagTreeNode.getChildren().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(CHILDREN_COUNT_COLUMN, new IntComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final List<? extends ITreeNode<CTag>> children = m_tagTreeNode.getChildren();
    final ITreeNode<CTag> tagNode = children.get(row);

    switch (col) {
      case NAME_COLUMN:
        return tagNode.getObject().getName();
      case DESCRIPTION_COLUMN:
        return tagNode.getObject().getDescription();
      case CHILDREN_COUNT_COLUMN:
        return tagNode.getChildren().size();
      default:
        throw new IllegalStateException("IE01178: Invalid column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return (col == NAME_COLUMN) || (col == DESCRIPTION_COLUMN);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    if ((col != NAME_COLUMN) && (col != DESCRIPTION_COLUMN)) {
      throw new IllegalStateException("IE01179: Column can not be edited");
    }

    final List<? extends ITreeNode<CTag>> children = m_tagTreeNode.getChildren();
    final ITreeNode<CTag> tagNode = children.get(row);

    if (col == NAME_COLUMN) {
      try {
        tagNode.getObject().setName((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00180: " + "Could not change tag name";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The name of the tag '%s' could not be changed.", tagNode.getObject().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The tag name could not be changed."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (col == DESCRIPTION_COLUMN) {
      try {
        tagNode.getObject().setDescription((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00181: " + "Could not change tag description";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The description of the tag '%s' could not be changed.", tagNode.getObject()
                    .getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The tag description could not be changed."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Updates the table model on changes in tags.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      fireTableDataChanged();
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      fireTableDataChanged();
    }

    @Override
    public void deletedTag(final CTag tag) {
      fireTableDataChanged();
    }
  }

  /**
   * Updates the table model on changes in the tag manager.
   */
  private class InternalTagManagerListener implements ITagManagerListener {

    @Override
    public void addedTag(final CTagManager manager, final ITreeNode<CTag> tag) {
      fireTableDataChanged();
    }

    @Override
    public void deletedTag(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      fireTableDataChanged();
    }

    @Override
    public void deletedTagSubtree(final CTagManager manager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      fireTableDataChanged();
    }

    @Override
    public void insertedTag(final CTagManager tagManager, final ITreeNode<CTag> parent,
        final ITreeNode<CTag> tag) {
      fireTableDataChanged();
    }
  }
}
