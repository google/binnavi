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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Provides search functionality for the TypesTree component.
 */
public class TypeEditorSearchPanel extends JPanel {

  private final JTextField searchField;
  private final TypesTree typesTree;
  /*
   * The index into the list of matches that is currently shown in the types tree. -1 if no matches
   * exist.
   */
  private int currentMatchIndex;
  private ImmutableList<Integer> currentMatches = ImmutableList.of();

  /**
   * Signals a changed search term to the type editor search panel.
   */
  private class TypeSearchDocumentListener implements DocumentListener {

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateFilter(event);
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateFilter(event);
    }

    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateFilter(event);
    }

    private void updateFilter(final DocumentEvent event) {
      try {
        updateTypesTree(event.getDocument().getText(0 /* offset */,
            event.getDocument().getLength()));
      } catch (final BadLocationException exception) {
        // This can never happen since we always retrieve the whole document content.
      }
    }
  }

  private static ImmutableList<Integer> determineMatches(final String filterString,
      final DefaultMutableTreeNode rootNode) {
    if (filterString.isEmpty()) {
      return ImmutableList.<Integer>of();
    }
    final Builder<Integer> builder = ImmutableList.builder();
    final String lowercaseFilter = filterString.toLowerCase();
    for (int i = 0; i < rootNode.getChildCount(); ++i) {
      if (rootNode.getChildAt(i).toString().toLowerCase().contains(lowercaseFilter)) {
        builder.add(i);
      }
    }
    return builder.build();
  }

  private void updateTypesTree(final String filterString) {
    currentMatches =
        determineMatches(filterString, (DefaultMutableTreeNode) typesTree.getModel().getRoot());
    selectMatches(currentMatches, typesTree);
    currentMatchIndex = currentMatches.isEmpty() ? -1 : 0;
    if (currentMatchIndex != -1) {
      scrollToMatch(currentMatchIndex, currentMatches, typesTree);
    }
  }

  private static void selectMatches(final ImmutableList<Integer> matchedRows,
      final TypesTree tree) {
    if (matchedRows.isEmpty()) {
      tree.clearSelection();
    } else {
      final int[] selectRows = new int[matchedRows.size()];
      for (int i = 0; i < matchedRows.size(); i++) {
        selectRows[i] = matchedRows.get(i);
      }
      tree.setSelectionRows(selectRows);
    }
  }

  private static void scrollToMatch(int matchIndex, final ImmutableList<Integer> matches,
      final TypesTree tree) {
    tree.scrollRowToVisible(matches.get(matchIndex));
  }

  public TypeEditorSearchPanel(final TypesTree typesTree) {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    searchField = new JTextField();
    add(searchField);
    searchField.setColumns(10);
    // There are two buttons "<" and ">", resp., to cycle through the result set back and forward.
    final JButton previousResult = new JButton("<");
    add(previousResult);
    previousResult.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (!currentMatches.isEmpty()) {
          currentMatchIndex = ((currentMatchIndex - 1 % currentMatches.size())
              + currentMatches.size())% currentMatches.size();
          // If the user deselected the searched nodes in the mean time, we select them again.
          // Same for the "next result" handler.
          selectMatches(currentMatches, typesTree);
          scrollToMatch(currentMatchIndex, currentMatches, typesTree);
        }
      }
    });
    final JButton nextResult = new JButton(">");
    add(nextResult);
    nextResult.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (!currentMatches.isEmpty()) {
          currentMatchIndex = (currentMatchIndex + 1) % currentMatches.size();
          selectMatches(currentMatches, typesTree);
          scrollToMatch(currentMatchIndex, currentMatches, typesTree);
        }
      }
    });
    searchField.getDocument().addDocumentListener(new TypeSearchDocumentListener());
    this.typesTree = typesTree;
  }
}
