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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.CommentContainer;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.CodeDisplay.BasicCodeDisplayModel.JCodeDisplayColumnDescription;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplayCoordinate;
import com.google.security.zynamics.zylib.gui.CodeDisplay.FormattedCharacterBuffer;
import com.google.security.zynamics.zylib.gui.CodeDisplay.ICodeDisplayModel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation for the data model to display the list of types in BinNavi. This includes
 * the address of the type, the name, of the type, the C-style declaration of it, a list of
 * cross-references, and finally a comment.
 */
public class TypeInstanceTableDatamodel implements ICodeDisplayModel {
  private static final Font HEADER_FONT_BOLD =
      GuiHelper.MONOSPACED_FONT.deriveFont(java.awt.Font.BOLD);
  private static final Font STANDARD_FONT = GuiHelper.MONOSPACED_FONT;
  private static final JCodeDisplayColumnDescription[] columns = {
      new JCodeDisplayColumnDescription(
          "Address", 20, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Name", 35, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Type Declaration", 50, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "XRefs", 30, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD),
      new JCodeDisplayColumnDescription(
          "Comments", 50, Color.BLACK, Color.LIGHT_GRAY.brighter(), HEADER_FONT_BOLD)};
  // These are intentionally package-level, so the data section component can decide which columns
  // it should display a context-sensitive menu over.
  static final int ADDRESS_INDEX = 0;
  static final int NAME_INDEX = 1;
  static final int TYPE_INDEX = 2;
  static final int XREFS_INDEX = 3;
  private static final int COMMENTS_INDEX = 4;

  TypeInstanceContainer typeContainer;
  ArrayList<TypeInstance> typesToDisplay = new ArrayList<>();
  ArrayList<FormattedCharacterBuffer> typeDeclarations = new ArrayList<>();
  ArrayList<CodeDisplay> displaysToUpdate = new ArrayList<>();

  Section section;

  int currentNumberOfRows;
  int currentNumberOfLines;

  TypeInstanceTableDatamodel(final TypeInstanceContainer instanceContainer,
      final Section currentSection) {
    Preconditions.checkNotNull(currentSection);
    Preconditions.checkNotNull(instanceContainer);
    setSection(currentSection);
    setTypeInstanceContainer(instanceContainer);
    rebuildAndCalculateSize();
  }

  TypeInstanceTableDatamodel() {
    rebuildAndCalculateSize();
  }

  private Color getBackgroundColor(int rowIndex, int columnIndex) {
    if (displaysToUpdate.size() > 0) {
      if (rowIndex == displaysToUpdate.get(0).getCaretPosition().getRow()) {
        return columns[columnIndex].getDefaultBackgroundColor().darker();
      }
    }
    return columns[columnIndex].getDefaultBackgroundColor();
  }

  private FormattedCharacterBuffer stringToFormattedCharacterBuffer(String string, int rowIndex,
      int columnIndex) {
    return new FormattedCharacterBuffer(
        Strings.padEnd(string, columns[columnIndex].getWidth(), ' '), STANDARD_FONT,
        columns[columnIndex].getDefaultFontColor(), getBackgroundColor(rowIndex, columnIndex));
  }

  public void registerCodeDisplayToUpdate(CodeDisplay display) {
    displaysToUpdate.add(display);
  }

  public void deRegisterCodeDisplayToUpdate(CodeDisplay display) {
    displaysToUpdate.remove(display);
  }

  /** If you call this you have to call recalculateSize() thereafter. This doesn't happen here
   * as it would lead to duplication of work (the usual setup is to set both the type instance
   * container and the section in close succession, and the section then recalculates the size. */
  public void setTypeInstanceContainer(TypeInstanceContainer instanceContainer) {
    typeContainer = instanceContainer;
  }

  public TypeInstanceContainer getTypeInstanceContainer() {
    return typeContainer;
  }

  public void setSection(Section currentSection) {
    section = currentSection;
    rebuildAndCalculateSize();
  }

  public TypeInstance getTypeAtRow(int index) {
    return typesToDisplay.get(index);
  }

  private void addTypeToDisplay(TypeInstance instance) {
    // Add the instance to be displayed.
    typesToDisplay.add(instance);
    // Add the rendering of the type declaration.
    FormattedCharacterBuffer typeRendering = BaseTypeTableCellRenderer.renderType(instance,
        STANDARD_FONT, columns[TYPE_INDEX].getWidth(), false);
    typeDeclarations.add(typeRendering);
    // Calculate how many lines this type occupies.
    int typeRenderingHeight = typeRendering.getNumberOfLines();
    int numberOfReferences = typeContainer.getReferenceCount(instance);
    currentNumberOfLines += Math.max(typeRenderingHeight, numberOfReferences);
  }

  private void rebuildAndCalculateSize() {
    if (typeContainer == null) {
      return;
    }
    typesToDisplay.clear();
    typeDeclarations.clear();
    currentNumberOfRows = 0;
    currentNumberOfLines = 0;
    for (TypeInstance instance : typeContainer.getTypeInstances(section)) {
      // New type means extra row.
      currentNumberOfRows++;
      addTypeToDisplay(instance);
    }
  }

  private static FormattedCharacterBuffer generateFormattedComment(final List<IComment> comments) {
    // Calculate the number of rows that will be needed.
    int numberOfRows = 0;
    for (final IComment comment : comments) {
      final CommentContainer commentContainer = new CommentContainer(comment);
      final List<String> commentFragments = commentContainer.getCommentingString();
      numberOfRows += commentFragments.size();
    }
    // Generate and fill the buffer.
    FormattedCharacterBuffer result =
        new FormattedCharacterBuffer(numberOfRows + 1, columns[COMMENTS_INDEX].getWidth());
    int lineIndex = 0;
    int columnIndex = 0;
    for (final IComment comment : comments) {
      final CommentContainer commentContainer = new CommentContainer(comment);
      final List<String> commentFragments = commentContainer.getCommentingString();
      for (final String commentFragment : commentFragments) {
        for (int i = 0; i < commentFragment.length(); i++) {
          result.setAt(lineIndex,
              columnIndex,
              commentFragment.charAt(i),
              STANDARD_FONT,
              Color.BLACK,
              Color.WHITE);
          columnIndex++;
        }
        columnIndex = 0;
        lineIndex++;
      }
    }
    return result;
  }

  /*
   * Each type instance gets a separate row - which can then have multiple lines (type declaration
   * etc.).
   */
  @Override
  public int getNumberOfRows() {
    return typesToDisplay.size();
  }

  @Override
  public int getTotalNumberOfLines() {
    return currentNumberOfLines;
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
    return columns[columnIndex].getWidth();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columns[columnIndex].getName();
  }

  @Override
  public int getMaximumLinesForRow(int rowIndex) {
    FormattedCharacterBuffer typeRendering = BaseTypeTableCellRenderer.renderType(
        typesToDisplay.get(rowIndex), STANDARD_FONT, columns[TYPE_INDEX].getWidth(), false);
    return Math.max(typeRendering.getNumberOfLines(),
        typeContainer.getReferenceCount(typesToDisplay.get(rowIndex)));
  }

  @Override
  public FormattedCharacterBuffer getLineFormatted(int rowIndex, int columnIndex, int lineIndex) {
    Color backgroundColor = getBackgroundColor(rowIndex, columnIndex);
    switch (columnIndex) {
      case ADDRESS_INDEX:
        TypeInstance type = typesToDisplay.get(rowIndex);
        String addressString = "";
        if (lineIndex == 0) {
          addressString =
              TypeInstanceAddressTableCellRenderer.getStringToDisplay(false, type.getAddress());
        }
        return stringToFormattedCharacterBuffer(addressString, rowIndex, columnIndex);
      case NAME_INDEX:
        String typeName = "";
        if (lineIndex == 0) {
          typeName = typesToDisplay.get(rowIndex).getName();
        }
        return new FormattedCharacterBuffer(
            Strings.padEnd(typeName, columns[columnIndex].getWidth(), ' '), STANDARD_FONT,
            columns[columnIndex].getDefaultFontColor(), backgroundColor);
      case TYPE_INDEX:
        FormattedCharacterBuffer typeDeclaration = typeDeclarations.get(rowIndex);
        if (typeDeclaration.getNumberOfLines() > lineIndex) {
          return typeDeclarations.get(rowIndex).getLine(lineIndex)
              .setBackgroundColor(backgroundColor);
        }
        return stringToFormattedCharacterBuffer("", rowIndex, columnIndex);
      case XREFS_INDEX:
        TypeInstanceReference reference = getTypeInstanceReference(rowIndex, lineIndex);
        String xrefString = "";
        if (reference != null) {
          xrefString = TypeInstanceCrossReferenceRenderer.renderText(reference);
        }
        return stringToFormattedCharacterBuffer(xrefString, rowIndex, columnIndex);
      case COMMENTS_INDEX:
        FormattedCharacterBuffer comment =
            generateFormattedComment(typeContainer.getComments(typesToDisplay.get(rowIndex)));
        if (lineIndex < comment.getNumberOfLines()) {
          // Now only return the line that was requested. TODO(thomasdullien): This is pretty
          // wasteful, clean it up eventually.
          return comment.getLine(lineIndex).setBackgroundColor(backgroundColor);
        }
        // Return empty buffers for empty lines.
        return stringToFormattedCharacterBuffer("", rowIndex, columnIndex);
      default:
        Logger.warning("Invalid column index, investigate.");
        break;
    }
    return null;
  }

  public TypeInstanceReference getTypeInstanceReference(int row, int line) {
    TypeInstance typeInstance = typesToDisplay.get(row);
    List<TypeInstanceReference> references = typeContainer.getReferences(typeInstance);
    if (line < references.size()) {
      return references.get(line);
    }
    return null;
  }

  @Override
  public boolean canHaveCaret(CodeDisplayCoordinate coordinate) {
    return true;
  }

  @Override
  public boolean isEditable(CodeDisplayCoordinate coordinate) {
    switch (coordinate.getColumn()) {
      case ADDRESS_INDEX:
        return false;
      case NAME_INDEX:
        return true;
      case TYPE_INDEX:
        return false;
      case XREFS_INDEX:
        return false;
      case COMMENTS_INDEX:
        return true;
      default:
        return false;
    }
  }

  @Override
  public void keyPressedOrTyped(CodeDisplayCoordinate coordinate, KeyEvent event) {
    if (!isEditable(coordinate)) {
      return;
    }
    // Deal with key events in the name or comments fields. These have to be handled differently.

    if (coordinate.getColumn() == COMMENTS_INDEX) {
    } else if (coordinate.getColumn() == NAME_INDEX) {
      if (coordinate.getLine() != 0) {
        // There are no multi-line names, so just bare essential keys are supported here.
        switch (event.getKeyCode()) {
          case 0:
          case KeyEvent.VK_HOME:
            coordinate.setFieldIndex(0);
            break;
          case KeyEvent.VK_END:
            coordinate.setFieldIndex(columns[coordinate.getColumn()].getWidth() - 1);
            break;
          default:
            // No special handling in the default case.
            break;
        }
      } else {
        // In the first line, the name can be changed. Allow this to occur if the caret is next
        // to an existing letter of the name.
        // TODO(thomasdullien): Implement.
      }
    }
  }

  @Override
  public boolean hasHeaderRow() {
    return true;
  }

  @Override
  public FormattedCharacterBuffer getHeader(int columnIndex) {
    return columns[columnIndex].getHeader();
  }
}
