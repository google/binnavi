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

import com.google.common.base.Strings;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeHelpers;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.CodeDisplay.FormattedCharacterBuffer;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A renderer that converts a {@link TypeInstance type instance} and the corresponding
 * {@link BaseType base type} to a text representation for display in the
 * {@link TypeInstanceTableDataModel} component.
 *
 */
class BaseTypeTableCellRenderer {
  private static final int THRESHOLD = 30;
  private final boolean renderInstanceData;

  public BaseTypeTableCellRenderer(final boolean renderInstanceData) {
    this.renderInstanceData = renderInstanceData;
  }

  private static void appendString(
      final StyledDocument document, final String string, final Style style)
          throws BadLocationException {
    document.insertString(document.getLength(), string, style);
  }

  private static Style createDataStyle(final StyledDocument document) {
    final Style declStyle = document.addStyle("DATA_STYLE", null);
    StyleConstants.setBackground(declStyle, Color.WHITE);
    StyleConstants.setForeground(declStyle, Color.BLUE);
    StyleConstants.setFontFamily(declStyle, GuiHelper.getMonospaceFont());
    StyleConstants.setFontSize(declStyle, 11);
    return declStyle;
  }

  private static Style createDeclarationStyle(final StyledDocument document) {
    final Style declStyle = document.addStyle("DECL_STYLE", null);
    StyleConstants.setBackground(declStyle, Color.WHITE);
    StyleConstants.setForeground(declStyle, Color.BLACK);
    StyleConstants.setFontFamily(declStyle, GuiHelper.getMonospaceFont());
    StyleConstants.setFontSize(declStyle, 11);
    return declStyle;
  }

  private static void generateDocument(
      final TypeInstance instance, final boolean renderData, final StyledDocument document) {
    switch (instance.getBaseType().getCategory()) {
      case ARRAY:
        renderArray(instance, document, renderData);
        break;
      case ATOMIC:
        renderAtomic(instance, document, renderData);
        break;
      case POINTER:
        renderPointer(instance, document);
        break;
      case STRUCT:
        renderStruct(instance, document, renderData);
        break;
      default:
        break;
    }
  }

  private static int getRenderDataLength(final BaseType baseType) {
    final int size = baseType.getByteSize();
    return (size > THRESHOLD) ? THRESHOLD : size;
  }

  private static void renderArray(
      final TypeInstance instance, final StyledDocument document, final boolean renderData) {
    final Style arrayStyle = createDeclarationStyle(document);
    try {
      document.remove(0, document.getLength());
      final BaseType baseType = instance.getBaseType();
      appendString(document, baseType.getName(), arrayStyle);
      if (renderData) {
        appendString(document,
            renderInstanceData(baseType, instance.getAddress().getOffset(), instance.getSection()),
            createDataStyle(document));
      }
    } catch (final BadLocationException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  private static void renderAtomic(
      final TypeInstance instance, final StyledDocument document, final boolean renderData) {
    final Style atomicStyle = createDeclarationStyle(document);
    try {
      document.remove(0, document.getLength());
      final BaseType baseType = instance.getBaseType();
      appendString(document, baseType.getName(), atomicStyle);
      if (renderData) {
        appendString(document,
            renderInstanceData(baseType, instance.getAddress().getOffset(), instance.getSection()),
            createDataStyle(document));
      }
    } catch (final BadLocationException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Renders the data corresponding to the given base type which is located at the given offset in
   * the section.
   */
  private static String renderInstanceData(
      final BaseType baseType, final long offset, final Section section) {
    final int size = getRenderDataLength(baseType);
    final StringBuilder builder = new StringBuilder();
    builder.append("[");
    final byte[] data = section.getData();
    final int strideLength = (baseType.getCategory() == BaseTypeCategory.ARRAY)
        ? BaseTypeHelpers.getArrayElementByteSize(baseType) : size;
    for (long currentOffset = offset; currentOffset < offset + size; ++currentOffset) {
      if ((currentOffset - offset) != 0 && ((currentOffset - offset) % strideLength == 0)) {
        builder.append(", ");
      }
      builder.append(String.format("%02X", data[(int) currentOffset]));
    }
    if (size == THRESHOLD) {
      builder.append("...");
    }
    builder.append(']');
    return builder.toString();
  }

  private static void renderPointer(final TypeInstance instance, final StyledDocument document) {
    final Style pointerStyle = createDeclarationStyle(document);
    try {
      document.remove(0, document.getLength());
      appendString(document, instance.getBaseType().getName(), pointerStyle);
    } catch (final BadLocationException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  private static void renderStruct(
      final TypeInstance instance, final StyledDocument document, final boolean renderData) {
    final Style structNameStyle = createDeclarationStyle(document);
    final Style structMemberStyle = document.addStyle("STRUCTMEMBERSTYLE", structNameStyle);
    StyleConstants.setForeground(structMemberStyle, Color.GRAY);
    final Style structContentStyle = document.addStyle("STRUCTCONTENTSTYLE", structNameStyle);
    StyleConstants.setForeground(structContentStyle, Color.BLUE);
    StyleConstants.setAlignment(structNameStyle, StyleConstants.ALIGN_RIGHT);

    final BaseType baseType = instance.getBaseType();
    int maxMemberLength = 0;
    for (final TypeMember member : baseType) {
      if (member.getBaseType().getName().length() > maxMemberLength) {
        maxMemberLength = member.getBaseType().getName().length();
      }
    }
    int maxNameLength = 0;
    for (final TypeMember member : baseType) {
      if (member.getName().length() > maxNameLength) {
        maxNameLength = member.getName().length();
      }
    }

    /* Renders type information for structures - construct a string such as:
     *
     * struct STRUCT_NAME { BASE_TYPE_NAME
     */
    try {
      document.remove(0, document.getLength());
      appendString(document, "struct " + baseType.getName() + " {\n", structNameStyle);

      long memberOffset = 0;
      for (final TypeMember member : baseType) {
        appendString(document, "  " + member.getBaseType().getName(), structNameStyle);
        final String separator =
            Strings.repeat(" ", maxMemberLength - member.getBaseType().getName().length() + 1);
        appendString(document, separator + member.getName(), structMemberStyle);
        appendString(document, ";", structMemberStyle);

        if (renderData) {
          final String dataSeperator =
              Strings.repeat(".", maxNameLength - member.getName().length() + 1);
          appendString(document, dataSeperator, structNameStyle);
          appendString(document, renderInstanceData(member.getBaseType(),
              instance.getAddress().getOffset() + memberOffset, instance.getSection()),
              createDataStyle(document));
          memberOffset += member.getBaseType().getByteSize();
        }
        appendString(document, "\n", structMemberStyle);
      }
      appendString(document, "};", structNameStyle);
    } catch (final BadLocationException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  public static String renderText(final TypeInstance instance) {
    final StyledDocument document = new DefaultStyledDocument();
    generateDocument(instance, false, document);
    try {
      return document.getText(0, document.getLength());
    } catch (final BadLocationException exception) {
      CUtilityFunctions.logException(exception);
    }
    return "";
  }

  protected void generateDocument(final StyledDocument document, final Object value) {
    generateDocument((TypeInstance) value, renderInstanceData, document);
  }

  public static FormattedCharacterBuffer convertDocumentToFormattedCharacterBuffer(
      final StyledDocument document, Font font, int desiredWidth) {
    // The following code calculates the number of rows and the number of columns required for the
    // FormattedCharacterBuffer.
    int width = desiredWidth, height = 0;
    String text = "";
    try {
      text = document.getText(0, document.getLength());
    } catch (BadLocationException e) {
      // Cannot happen.
    }
    String[] chunks = text.split("\n");
    height = chunks.length;
    for (String line : chunks) {
      // The +1 is necessary because we wish to store the newline characters in the
      // FormattedCharacterBuffer.
      width = Math.max(width, line.length() + 1);
    }
    // Height & width is calculated, now create the buffer.
    FormattedCharacterBuffer result = new FormattedCharacterBuffer(height, width);
    int lineindex = 0, columnindex = 0;
    for (int index = 0; index < document.getLength(); ++index) {
      if (text.charAt(index) != '\n') {
        AttributeSet attributes = document.getCharacterElement(index).getAttributes();
        Color foreground =
            document.getForeground(attributes);
        Color background =
            document.getBackground(attributes);

        columnindex++;
        result.setAt(lineindex, columnindex, text.charAt(index), font, foreground, background);
      } else {
        columnindex = 0;
        lineindex++;
      }
    }
    return result;
  }

  public static FormattedCharacterBuffer renderType(final TypeInstance instance, Font font,
      int desiredWidth, final boolean renderData) {
    DefaultStyledDocument document = new DefaultStyledDocument();

    generateDocument(instance, renderData, document);
    return convertDocumentToFormattedCharacterBuffer(document, font, desiredWidth);
  }

  public static FormattedCharacterBuffer renderTypeLine(final TypeInstance instance,
      Font font, int desiredWidth, final boolean renderData, final int line) {
    DefaultStyledDocument document = new DefaultStyledDocument();

    generateDocument(instance, renderData, document);
    return convertDocumentToFormattedCharacterBuffer(document, font, desiredWidth);
  }
}
