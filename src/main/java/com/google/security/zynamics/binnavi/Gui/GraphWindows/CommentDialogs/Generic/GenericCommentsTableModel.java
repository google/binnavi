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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Generic;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.zylib.gui.CodeDisplay.BasicCodeDisplayModel;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplayCoordinate;
import com.google.security.zynamics.zylib.gui.CodeDisplay.FormattedCharacterBuffer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the TableModel for displaying comments. Allows moving a cursor around the
 * comments, deleting/editing comments that are owned by the user etc.
 */
public class GenericCommentsTableModel extends BasicCodeDisplayModel {
  private static final JCodeDisplayColumnDescription[] columns = {
      new JCodeDisplayColumnDescription(
          "Username", 20, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Comment", 70, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD)};
  private static final int USER_INDEX = 0;
  private static final int COMMENT_INDEX = 1;

  private final ICommentAccessor commentAccessor;
  private ArrayList<IComment> cachedComments;

  public GenericCommentsTableModel(final ICommentAccessor commentAccessor) {
    this.commentAccessor = Preconditions.checkNotNull(commentAccessor);
    resetCachedComments();
  }

  private void resetCachedComments() {
    cachedComments =
        commentAccessor.getComment() == null
            ? new ArrayList<IComment>() : new ArrayList<>(commentAccessor.getComment());
  }

  public List<IComment> getComments() {
    return cachedComments;
  }

  @Override
  public int getNumberOfRows() {
    return cachedComments.size() + 1;
  }

  @Override
  public int getTotalNumberOfLines() {
    int totalNumberOfLines = 0;
    for (IComment comment : cachedComments) {
      totalNumberOfLines += comment.getNumberOfCommentLines();
    }
    // Return the extra line for the "new comment" field.
    return totalNumberOfLines + 1;
  }

  @Override
  public boolean hasHeaderRow() {
    return true;
  }

  @Override
  public FormattedCharacterBuffer getHeader(int columnIndex) {
    Preconditions.checkElementIndex(columnIndex, columns.length);
    return columns[columnIndex].getHeader();
  }

  @Override
  public int getTotalWidthInCharacters() {
    int total = 0;
    for (int index = 0; index < columns.length; ++index) {
      total += columns[index].getWidth();
    }
    return total;
  }

  @Override
  public int getNumberOfColumns() {
    return columns.length;
  }

  @Override
  public int getColumnWidthInCharacters(int columnIndex) {
    Preconditions.checkElementIndex(columnIndex, columns.length);
    return columns[columnIndex].getWidth();
  }

  @Override
  public String getColumnName(int columnIndex) {
    Preconditions.checkElementIndex(columnIndex, columns.length);
    return columns[columnIndex].getName();
  }

  @Override
  public int getMaximumLinesForRow(int rowIndex) {
    if (rowIndex == cachedComments.size()) {
      return 1; // The placeholder row for new comments only has one line.
    }
    if (rowIndex > cachedComments.size()) {
      return 0;
    }
    IComment comment = cachedComments.get(rowIndex);
    return comment.getNumberOfCommentLines();
  }

  @Override
  public FormattedCharacterBuffer getLineFormatted(int rowIndex, int columnIndex, int lineIndex) {
    // Display a row that invites the user to enter a comment below the existing comments.
    Preconditions.checkElementIndex(columnIndex, columns.length);
    JCodeDisplayColumnDescription column = columns[columnIndex];
    if (rowIndex >= cachedComments.size()) {
      switch (columnIndex) {
        case USER_INDEX:
          String username = CodeDisplay.padRight("Your username ...", column.getWidth());
          return new FormattedCharacterBuffer(username, STANDARD_FONT, column.getDefaultFontColor(),
              column.getDefaultBackgroundColor());
        case COMMENT_INDEX:
          String comment = CodeDisplay.padRight("Enter new comment ...", column.getWidth());
          return new FormattedCharacterBuffer(comment, STANDARD_FONT, column.getDefaultFontColor(),
              column.getDefaultBackgroundColor());
        default:
          // Not useful, but required for style guide conformance.
          break;
      }
    }

    switch (columnIndex) {
      case USER_INDEX:
        String username = "";
        if (lineIndex == 0) {
          username = cachedComments.get(rowIndex).getUser().getUserName();
        }
        username = CodeDisplay.padRight(username, column.getWidth());
        return new FormattedCharacterBuffer(username, STANDARD_FONT, column.getDefaultFontColor(),
            column.getDefaultBackgroundColor());
      case COMMENT_INDEX:
        // Get the comment for the row in question.
        String commentLine = cachedComments.get(rowIndex).getCommentLine(lineIndex);
        commentLine = CodeDisplay.padRight(commentLine, columns[columnIndex].getWidth());
        return new FormattedCharacterBuffer(commentLine, STANDARD_FONT,
            column.getDefaultFontColor(), column.getDefaultBackgroundColor());
    }
    return null;
  }

  @Override
  public boolean canHaveCaret(CodeDisplayCoordinate coordinate) {
    return coordinate.getColumn() == 1;
  }

  @Override
  public boolean isEditable(CodeDisplayCoordinate coordinate) {
    if (coordinate.getColumn() != 1){
      return false;
    }
    if (coordinate.getRow() >= cachedComments.size()) {
      // Adding a new comment.
      return true;
    }
    IComment comment = cachedComments.get(coordinate.getRow());
    // Empty comments are editable. For non-empty comments, only allow editing of comments that are
    // owned by this user.
    return comment.getNumberOfCommentLines() == 0
        || commentAccessor.isOwner(comment);
  }

  private static int lineAndIndexToCommentIndex(IComment comment, int lineNumber, int index) {
    final String[] lines = comment.getCommentLines();
    // Returns -1 if the index is further out than the end of the line.
    if (index > lines[lineNumber].length()) {
      return -1;
    }
    int temporaryIndex = 0;
    // This loop only iterates up to lineNumber - therefore a counter would be needed even if the
    // code used a for-each loop. To keep things simple, a "classical" loop with counter is used.
    for (int line = 0; line < lineNumber; line++) {
      temporaryIndex += lines[line].length() + 1; // Take newline char into account.
    }
    temporaryIndex += index;
    return temporaryIndex;
  }

  private IComment editComment(IComment comment, String newComment) {
    IComment newlySetComment = null;
    try {
      if (newComment.isEmpty()) {
        commentAccessor.deleteComment(comment);
      } else {
        newlySetComment = commentAccessor.editComment(comment, newComment);
      }
    } catch (CouldntSaveDataException | CouldntDeleteException e) {
      CUtilityFunctions.logException(e);
    }
    resetCachedComments();
    return newlySetComment;
  }

  private IComment deleteCharacterFromComment(IComment comment, int lineNumber, int index) {
    int commentIndex = lineAndIndexToCommentIndex(comment, lineNumber, index);
    String commentString = comment.getComment();
    // Do nothing if an invalid index was specified.
    if (commentIndex == -1) {
      return comment;
    }
    // If a character that is in the middle of the string is removed, build a new comment without
    // this character.
    if (commentIndex < commentString.length()) {
      String newComment =
          commentString.substring(0, commentIndex) + commentString.substring(commentIndex + 1);
      return editComment(comment, newComment);
    }
    // If the last character is to be removed, do that.
    return editComment(comment, commentString.substring(0, commentIndex));
  }

  private IComment handleBackspaceKeyInComment(IComment comment,
      CodeDisplayCoordinate coordinate) {
    int lineNumber = coordinate.getLine();
    int index = coordinate.getFieldIndex();
    int commentIndex = lineAndIndexToCommentIndex(comment, lineNumber, index);
    if ((coordinate.getFieldIndex() == 0) && (coordinate.getLine() > 0)) {
      // A newline character needs to be removed and two lines are joined.
    } else if ((coordinate.getFieldIndex() == 0) && (coordinate.getLine() == 0)) {
      // The backspace needs to be ignored - we are at the beginning of a field, and we don't want
      // the cursor to leave the field.
    } else {
      // "Regular" backspacing.
      coordinate.setFieldIndex(Math.max(coordinate.getFieldIndex() - 1, 0));
      if (commentIndex >= 0) {
        // Is there anything to delete? If no, don't delete anything.
        return deleteCharacterFromComment(comment, coordinate.getLine(),
            coordinate.getFieldIndex());
      }
    }
    // Nothing changed, so return.
    return comment;
  }

  private IComment handleDeleteKeyInComment(IComment comment, CodeDisplayCoordinate coordinate) {
    int line = coordinate.getLine();
    int field = coordinate.getFieldIndex();
    int commentIndex =
        lineAndIndexToCommentIndex(comment, coordinate.getLine(), coordinate.getFieldIndex());
    if (commentIndex >= 0) {
      return deleteCharacterFromComment(comment, line, field);
    }
    // The cursor is past the end of the text in the line in question. Pad the comment with spaces
    // before deleting the character under the cursor.
    StringBuilder newString = new StringBuilder();
    for (int lineCounter = 0; lineCounter < line; lineCounter++) {
      newString.append(comment.getCommentLine(lineCounter));
      newString.append("\n");
    }
    // Now add the padded line.
    newString.append(CodeDisplay.padRight(comment.getCommentLine(line), field));
    // Re-assemble the string with newlines separating the individual lines.
    for (int lineIndex = line + 1; lineIndex < comment.getNumberOfCommentLines(); lineIndex++) {
      newString.append(comment.getCommentLine(lineIndex));
      newString.append((lineIndex == comment.getNumberOfCommentLines() - 1 ? "" : "\n"));
    }
    return editComment(comment, newString.toString());
  }

  private IComment handleRegularKeyInComment(IComment comment, CodeDisplayCoordinate coordinate,
      KeyEvent event) {
    int line = coordinate.getLine();
    int field = coordinate.getFieldIndex();
    int commentIndex = lineAndIndexToCommentIndex(comment, line, field);
    if (commentIndex >= 0) {
      comment = insertStringIntoComment(comment, line, field, "" + event.getKeyChar());
    } else {
      String lineString = comment.getCommentLine(coordinate.getLine());
      String paddingAndKey =
          CodeDisplay.padRight("", coordinate.getFieldIndex() - lineString.length())
          + event.getKeyChar();
      comment = insertStringIntoComment(comment, coordinate.getLine(), lineString.length(),
          paddingAndKey);
    }
    return comment;
  }

  private IComment insertStringIntoComment(IComment comment, int lineNumber, int index,
      String string) {
    int commentIndex = lineAndIndexToCommentIndex(comment, lineNumber, index);
    if (commentIndex < 0) {
      return null;
    }
    String commentString = comment.getComment();
    String newComment =
        commentString.substring(0, commentIndex) + string + commentString.substring(commentIndex);
    return editComment(comment, newComment);
  }

  @Override
  /**
   * Please read the comment in the base class regarding this method - it is somewhat counter-
   * intuitive for people used to the Swing keyboard handling model.
   */
  public void keyPressedOrTyped(CodeDisplayCoordinate coordinate, KeyEvent event) {
    int commentRow = coordinate.getRow();
    IComment comment = null;
    if (commentRow < cachedComments.size()) {
      comment = cachedComments.get(commentRow);
    }

    if (!isEditable(coordinate)) {
      return;
    }

    switch (event.getKeyCode()) {
      // VK_UNDEFINED implies that this was a KEY_TYPED event.
      case KeyEvent.VK_UNDEFINED:
        if (commentRow >= cachedComments.size()) {
          // Create a new comment.
          try {
            commentAccessor.appendComment(String.format("%c", event.getKeyChar()));
            coordinate.setFieldIndex(1);
          } catch (CouldntSaveDataException | CouldntLoadDataException e) {
            CUtilityFunctions.logException(e);
          }
          resetCachedComments();
          // The new comment has been created with the key. Stop handling this keycode and skip
          // the handling code below.
          break;
        }
        switch (event.getKeyChar()) {
          case KeyEvent.VK_ENTER:
            comment = handleRegularKeyInComment(comment, coordinate, event);
            coordinate.setLine(coordinate.getLine() < (comment.getNumberOfCommentLines() - 1)
                ? coordinate.getLine() + 1
                : comment.getNumberOfCommentLines() - 1);
            coordinate.setFieldIndex(0);
            break;
          case KeyEvent.VK_BACK_SPACE:
            comment = handleBackspaceKeyInComment(comment, coordinate);
            break;
          case KeyEvent.VK_DELETE:
            comment = handleDeleteKeyInComment(comment, coordinate);
            break;
          default:
            comment = handleRegularKeyInComment(comment, coordinate, event);
            coordinate.setFieldIndex(coordinate.getFieldIndex() + 1);
            break;
        }
        break;
      case KeyEvent.VK_HOME:
        coordinate.setFieldIndex(0);
        break;
      case KeyEvent.VK_END:
        coordinate.setFieldIndex(columns[coordinate.getColumn()].getWidth());
        break;
      default:
        throw new IllegalArgumentException();
    }
  }
}
