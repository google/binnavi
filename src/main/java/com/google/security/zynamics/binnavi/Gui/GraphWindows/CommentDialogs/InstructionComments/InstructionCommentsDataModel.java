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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CCommentUtilities;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IInstructionCommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CodeDisplay.BasicCodeDisplayModel;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplayCoordinate;
import com.google.security.zynamics.zylib.gui.CodeDisplay.FormattedCharacterBuffer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The data model used for displaying the local instruction comments in the dialog where comments
 * for the local code are edited.
 */
public class InstructionCommentsDataModel extends BasicCodeDisplayModel {
  private static final JCodeDisplayColumnDescription[] columns = {
      new JCodeDisplayColumnDescription(
          "Instruction", 70, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Username", 30, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Comment", 80, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD)};
  private static final int INSTRUCTION_INDEX = 0;
  private static final int USER_INDEX = 1;
  private static final int COMMENT_INDEX = 2;

  final CGraphModel graphModel;

  private final IInstructionCommentAccessor commentAccessor;
  private final CCodeNode node;

  // Internally, this class keeps the data in an ArrayList that contains pairs of
  // Instruction -> List of Comments.
  private final List<Pair<INaviInstruction, ArrayList<IComment>>> internalData = new ArrayList<>();

  public InstructionCommentsDataModel(final CCodeNode codeNode, CGraphModel model,
      CommentManager.CommentScope scope) {
    Preconditions.checkNotNull(codeNode);
    Preconditions.checkNotNull(model);

    if (scope == CommentManager.CommentScope.LOCAL) {
      commentAccessor = new LocalInstructionCommentAccessor(codeNode);
    } else {
      commentAccessor = new CGlobalInstructionCommentAccessor(codeNode);
    }
    graphModel = model;
    node = codeNode;

    updateDataRepresentation();
  }

  private void updateDataRepresentation() {
    internalData.clear();
    for (INaviInstruction instruction : node.getInstructions()) {
      internalData.add(
          new Pair<INaviInstruction, ArrayList<IComment>>(instruction, new ArrayList<IComment>()));
    }

    int currentIndex = 0;
    for (Pair<INaviInstruction, IComment> dataPair : commentAccessor.getAllComments()) {
      // Is this the right instruction?
      while (dataPair.first() != internalData.get(currentIndex).first()) {
        currentIndex++;
      }
      internalData.get(currentIndex).second().add(dataPair.second());
    }
  }

  @Override
  public int getNumberOfRows() {
    return internalData.size();
  }

  @Override
  public int getTotalNumberOfLines() {
    int totalNumberOfLines = 0;
    for (Pair<INaviInstruction, ArrayList<IComment>> dataPair : internalData) {
      for (IComment comment : dataPair.second()) {
        totalNumberOfLines += comment.getNumberOfCommentLines();
      }
    }
    return totalNumberOfLines;
  }

  @Override
  public boolean hasHeaderRow() {
    return true;
  }

  @Override
  public FormattedCharacterBuffer getHeader(int columnIndex) {
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

  /*
   * The comment dialog displays the data in the form: address-instruction-string | username |
   * comment It is clear that it would be nicer to have address and instruction split, and also that
   * syntax-highlighting would be better, but this is not easily accessible from the
   * INaviInstruction interface at the moment.
   */
  @Override
  public int getNumberOfColumns() {
    return columns.length;
  }

  @Override
  public int getColumnWidthInCharacters(int columnIndex) {
    return columns[columnIndex].getWidth();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columns[columnIndex].getName();
  }

  /*
   * The maximum number of lines for a given row is simply the sum of all the comments in that row.
   * There is always only one address / element a sequence of comments is associated with, hence
   * the number of comments (and their respective number of lines) dictates the maximum rows for
   * a given line.
   */
  @Override
  public int getMaximumLinesForRow(int rowIndex) {
    int maximumLines = 0;
    for (IComment comment : internalData.get(rowIndex).second()) {
      maximumLines +=
          (comment.getNumberOfCommentLines() == 0) ? 1 : comment.getNumberOfCommentLines();
    }
    return maximumLines;
  }

  @Override
  public FormattedCharacterBuffer getLineFormatted(int rowIndex, int columnIndex, int lineIndex) {
    // Return the instruction that needs commenting.
    switch (columnIndex) {
      case INSTRUCTION_INDEX:
        return getInstructionCharacterBuffer(rowIndex, columnIndex, lineIndex);
      case USER_INDEX:
        return getCommentUserCharacterBuffer(rowIndex, columnIndex, lineIndex);
      case COMMENT_INDEX:
        return getCommentCharacterBuffer(rowIndex, columnIndex, lineIndex);
      default:
        Logger.warning("getLineFormatted called with invalid columnIndex, investigate.");
        break;
    }
    return null;
  }

  private IComment getCommentAtCoordinate(CodeDisplayCoordinate coordinate) {
    return getCommentAndIndexAtCoordinate(coordinate).first();
  }

  /*
   * The string that IComment contains is a multi-line string; it contains newline characters that
   * make the UI display multiple "lines" of comments for a given row (code address).
   *
   * In order to make it easy for the caller to identify which substring of a IComment is relevant
   * given a particular cursor position, this function returns the beginning and the end index
   * within the larger IComment of the comment indicated by coordinate.
   */
  private Pair<IComment, Pair<Integer, Integer>> getCommentAndIndexAtCoordinate(
      CodeDisplayCoordinate coordinate) {
    // Get the list of comments for the instruction.
    ArrayList<IComment> comments = internalData.get(coordinate.getRow()).second();
    int currentLineIndex = 0;
    // Now iterate over the comments until you reach the right comment at this
    // coordinate.
    for (IComment comment : comments) {
      int numberOfCommentLines = Math.max(comment.getNumberOfCommentLines(), 1);
      // Does the current comment (at currentLineIndex) span over the line field of the coordinate?
      if ((currentLineIndex + numberOfCommentLines - 1) >= coordinate.getLine()) {
        // Yes. Is it an empty comment? Then return now.
        if (comment.getNumberOfCommentLines() == 0) {
          return new Pair<IComment, Pair<Integer, Integer>>(comment,
              new Pair<Integer, Integer>(0, 0));
        }
        // The comment is non-empty and spans the line in the coordinate. Now identify the beginning
        // and end indices of the right line within the comment (remember that comments are strings
        // with newlines - so a given line that is displayed is just a beginning and end index into
        // a longer string).
        String commentString = comment.getComment();
        int beginIndex = 0;
        int endIndex = 0;
        int targetSubstringIndex = coordinate.getLine() - currentLineIndex;
        // The beginIndex is the index of the first character after the Nth newline (where N is
        // targetSubstringIndex), the endIndex is the last character before either end-of-string or
        // the N+1th newline.
        int newlineCounter = 0;
        while (newlineCounter != targetSubstringIndex) {
          if (commentString.charAt(beginIndex++) == '\n') {
            newlineCounter++;
          }
        }
        endIndex = commentString.indexOf('\n', beginIndex);
        endIndex = (endIndex == -1) ? commentString.length() : endIndex;
        return new Pair<IComment, Pair<Integer, Integer>>(comment, new Pair<Integer, Integer>(
            beginIndex, endIndex));
      }
      currentLineIndex += numberOfCommentLines;
    }
    // This can never happen.
    return null;
  }

  private FormattedCharacterBuffer getCommentCharacterBuffer(int rowIndex, int columnIndex,
      int lineIndex) {
    // Return the actual comment.
    CodeDisplayCoordinate coordinate =
        new CodeDisplayCoordinate(rowIndex, lineIndex, columnIndex, 0);

    Pair<IComment, Pair<Integer, Integer>> commentAndIndex =
        getCommentAndIndexAtCoordinate(coordinate);

    String finalComment = "";
    if (commentAndIndex.first().getNumberOfCommentLines() > 0) {
      String commentString = commentAndIndex.first().getComment();
      Pair<Integer, Integer> indices = commentAndIndex.second();

      finalComment = commentString.substring(indices.first(), indices.second());
    }
    finalComment = CodeDisplay.padRight(finalComment, getColumnWidthInCharacters(columnIndex));
    return new FormattedCharacterBuffer(finalComment, STANDARD_FONT,
        columns[COMMENT_INDEX].getDefaultFontColor(),
        columns[COMMENT_INDEX].getDefaultBackgroundColor());
  }

  private FormattedCharacterBuffer getCommentUserCharacterBuffer(int rowIndex, int columnIndex,
      int lineIndex) {
    // Return the user string associated with the comment.
    ArrayList<IComment> comments = internalData.get(rowIndex).second();
    int currentLineIndex = 0;
    String username = "";
    for (IComment comment : comments) {
      if ((currentLineIndex + comment.getNumberOfCommentLines() > lineIndex)
          && (currentLineIndex == lineIndex)) {
        // Found the right comment.
        String temp = comment.getUser().getUserName();
        if (temp != null) {
          username = temp;
        }
        break;
      }
      currentLineIndex += comment.getNumberOfCommentLines();
    }
    username = CodeDisplay.padRight(username, getColumnWidthInCharacters(columnIndex));
    return new FormattedCharacterBuffer(username, STANDARD_FONT,
        columns[USER_INDEX].getDefaultFontColor(), columns[USER_INDEX].getDefaultBackgroundColor());
  }

  private FormattedCharacterBuffer getInstructionCharacterBuffer(int rowIndex, int columnIndex,
      int lineIndex) {
    if (lineIndex == 0) {
      String instruction = CCommentUtilities.createInstructionLine(
          internalData.get(rowIndex).first(), graphModel);
      instruction = CodeDisplay.padRight(instruction, getColumnWidthInCharacters(columnIndex));
      return new FormattedCharacterBuffer(instruction, STANDARD_FONT,
          columns[INSTRUCTION_INDEX].getDefaultFontColor(),
          columns[INSTRUCTION_INDEX].getDefaultBackgroundColor());
    } else {
      String buf = CodeDisplay.padRight("", getColumnWidthInCharacters(columnIndex));
      return new FormattedCharacterBuffer(buf, STANDARD_FONT,
          columns[INSTRUCTION_INDEX].getDefaultFontColor(),
          columns[INSTRUCTION_INDEX].getDefaultBackgroundColor());
    }
  }

  @Override
  public boolean canHaveCaret(CodeDisplayCoordinate coordinate) {
    return (coordinate.getColumn() == COMMENT_INDEX);
  }

  @Override
  public boolean isEditable(CodeDisplayCoordinate coordinate) {
    // Only the comment field is editable.
    if (coordinate.getColumn() == 2) {
      IComment comment = getCommentAtCoordinate(coordinate);
      // Empty comments are editable.
      if (comment.getNumberOfCommentLines() == 0) {
        return true;
      }
      // Only allow editing of comments that are owned by this user.
      if (commentAccessor.isOwner(comment)) {
        return true;
      }
    }
    return false;
  }

  private void deleteCharacterFromComment(INaviInstruction instruction, IComment comment,
      int index) {
    // If the comment is already empty, there's not much to do here.
    if (comment.getNumberOfCommentLines() == 0) {
      return;
    }
    // If the last character of a comment is to be deleted, don't do anything either.
    String oldComment = comment.getComment();
    if (oldComment.length() == index) {
      return;
    }
    String newComment = oldComment.substring(0, index) + oldComment.substring(index + 1);
    try {
      if (newComment.length() == 0) {
        commentAccessor.deleteInstructionComment(instruction, comment);
      } else {
        commentAccessor.editInstructionComment(instruction, comment, newComment);
      }
    } catch (CouldntSaveDataException | CouldntDeleteException e) {
      Logger.logException(e);
    }
    updateDataRepresentation();
  }

  private void handleBackspaceKeyInComment(INaviInstruction instruction, IComment comment,
      Pair<Integer, Integer> indices, CodeDisplayCoordinate coordinate) {
    // Move the cursor back one.
    coordinate.setFieldIndex(Math.max(coordinate.getFieldIndex() - 1, 0));
    if (coordinate.getFieldIndex() > (indices.second() - indices.first())) {
      // Is there anything to delete? If no, don't delete anything.
      return;
    }
    deleteCharacterFromComment(instruction, comment, indices.first() + coordinate.getFieldIndex());
  }

  private void handleDeleteKeyInComment(INaviInstruction instruction, IComment comment,
      Pair<Integer, Integer> indices, CodeDisplayCoordinate coordinate) {
    // Is there anything to delete? If no, don't delete anything.
    if (coordinate.getFieldIndex() > (indices.second() - indices.first())) {
      return;
    }
    deleteCharacterFromComment(instruction, comment, indices.first() + coordinate.getFieldIndex());
  }

  private void handleRegularKeyInComment(INaviInstruction instruction, IComment comment,
      Pair<Integer, Integer> indices, CodeDisplayCoordinate coordinate, KeyEvent event) {
    // Does the comment line need padding?
    if (coordinate.getFieldIndex() > (indices.second() - indices.first())) {
      String newComment = CodeDisplay.padRight("",
          coordinate.getFieldIndex() - (indices.second() - indices.first())) + event.getKeyChar();

      insertStringIntoComment(instruction, comment, indices.second(), newComment);
    } else {
      insertStringIntoComment(instruction, comment, indices.first() + coordinate.getFieldIndex(),
          "" + event.getKeyChar());
    }
  }

  private void insertStringIntoComment(INaviInstruction instruction, IComment comment, int index,
      String string) {
    // Is the existing comment empty?
    if (comment.getNumberOfCommentLines() == 0) {
      String newComment = CodeDisplay.padRight("", index);
      newComment += string;
      try {
        commentAccessor.appendInstructionComment(instruction, newComment);
      } catch (CouldntSaveDataException | CouldntLoadDataException e) {
        Logger.logException(e);
      }
    } else {
      String oldComment = comment.getComment();
      String newComment = oldComment.substring(0, index) + string + oldComment.substring(index);
      try {
        commentAccessor.editInstructionComment(instruction, comment, newComment);
      } catch (CouldntSaveDataException e) {
        Logger.logException(e);
      }
    }
    updateDataRepresentation();
  }

  @Override
  /**
   * Please read the comment in the base class regarding this method - it is somewhat counter-
   * intuitive for people used to the Swing keyboard handling model.
   */
  public void keyPressedOrTyped(CodeDisplayCoordinate coordinate, KeyEvent event) {
    if (!isEditable(coordinate)) {
      return;
    }

    // Are there any existing comments?
    INaviInstruction instruction = internalData.get(coordinate.getRow()).first();
    Pair<IComment, Pair<Integer, Integer>> commentAndIndex =
        getCommentAndIndexAtCoordinate(coordinate);
    IComment comment = commentAndIndex.first();
    Pair<Integer, Integer> indices = commentAndIndex.second();

    switch (event.getKeyCode()) {
      // VK_UNDEFINED implies that this was a KEY_TYPED event.
      case KeyEvent.VK_UNDEFINED:
        switch (event.getKeyChar()) {
          case java.awt.Event.ENTER:
            handleRegularKeyInComment(instruction, comment, indices, coordinate, event);
            coordinate.setLine(coordinate.getLine() + 1);
            coordinate.setFieldIndex(0);
            break;
          case java.awt.Event.BACK_SPACE:
            handleBackspaceKeyInComment(instruction, comment, indices, coordinate);
            break;
          case java.awt.Event.DELETE:
            handleDeleteKeyInComment(instruction, comment, indices, coordinate);
            break;
          default:
            handleRegularKeyInComment(instruction, comment, indices, coordinate, event);
            coordinate.setFieldIndex(coordinate.getFieldIndex() + 1);
            break;
        }
        break;
      case KeyEvent.VK_HOME:
        coordinate.setFieldIndex(0);
        break;
      case KeyEvent.VK_END:
        coordinate.setFieldIndex(indices.second() - indices.first());
        break;
      default:
        Logger.warning("Default case in keyTyped hit, investigate why.");
        break;
    }
  }
}
