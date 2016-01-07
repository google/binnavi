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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CReferenceFinder;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class that builds the content of code nodes.
 */
public final class ZyCodeNodeBuilder {
  /**
   * Default bold font for text content.
   */
  private static final Font BOLD_FONT = new Font(GuiHelper.getMonospaceFont(), Font.BOLD, 12);

  /**
   * Default italic font for comments.
   */
  private static final Font ITALIC_FONT = new Font(GuiHelper.getMonospaceFont(), Font.ITALIC, 12);

  /**
   * Default font for text content.
   */
  private static final Font NORMAL_FONT = new Font(GuiHelper.getMonospaceFont(), Font.PLAIN, 12);

  /**
   * Padding between the function address and the function name.
   */
  private static final String PADDING_AFTER_FUNCTION_ADDRESS = "   ";

  /**
   * You are not supposed to instantiate this class.
   */
  private ZyCodeNodeBuilder() {
  }

  /**
   * Builds the function line of a code node. This line gives information about the function from
   * where the code node originally came from.
   * 
   * @param node The node that provides the raw data.
   * @param content The node content where the function line is added.
   * @param modifier Calculates the address strings. This argument can be null.
   */
  private static void buildFunctionLine(final INaviCodeNode node, final ZyLabelContent content,
      final INodeModifier modifier) {
    try {
      final INaviFunction parentFunction = node.getParentFunction();

      final String address =
          modifier == null ? parentFunction.getAddress().toHexString() : modifier.getAddress(
              parentFunction.getModule(), new UnrelocatedAddress(parentFunction.getAddress()),
              true);
      final String name = parentFunction.getName();

      content.addLineContent(new ZyLineContent(address + PADDING_AFTER_FUNCTION_ADDRESS
          + parentFunction.getModule().getConfiguration().getName() + "::" + name, BOLD_FONT, Lists
          .newArrayList(new CStyleRunData(0, -1, Color.BLACK)), null));
    } catch (final MaybeNullException exception) {
      // If there is no parent function, the parent function is not shown in the code node.
    }
  }

  /**
   * Adds the instruction lines to the content.
   * 
   * @param node The node that provides the instructions.
   * @param content The content where the lines are added.
   * @param graphSettings Provides settings that influence node formatting.
   * @param modifier Calculates the address strings. This argument can be null.
   */
  private static void buildInstructionLines(final INaviCodeNode node, final ZyLabelContent content,
      final ZyGraphViewSettings graphSettings,
      final INodeModifier modifier) {

    // Step I: Create the individual lines
    final List<Pair<String, List<CStyleRunData>>> lines =
        new ArrayList<Pair<String, List<CStyleRunData>>>();
    final HashMap<Pair<String, List<CStyleRunData>>, ArrayList<CommentContainer>> comments =
        new HashMap<Pair<String, List<CStyleRunData>>, ArrayList<CommentContainer>>();

    final int maxLine = createLines(node, lines, comments, graphSettings, modifier);

    // Step II: Insert all the instruction lines to the content
    insertLines(content, lines, comments, maxLine);

    // Step III: Add the lines for the tags
    ZyNodeBuilder.addCommentTagLines(content, node, node.getComments().getLocalCodeNodeComment(),
        node.getComments().getGlobalCodeNodeComment());
  }

  /**
   * Creates the instructions lines for a code node.
   * 
   * @param node The node that provides the instructions.
   * @param lines The created lines will be stored here.
   * @param commentsToLineAssociation Information about the required comments is stored here.
   * @param graphSettings Provides settings that influence node formatting.
   * @param modifier Calculates the address strings. This argument can be null.
   * 
   * @return The maximum size in characters of all lines put into the lines list.
   */
  private static int createLines(
      final INaviCodeNode node,
      final List<Pair<String, List<CStyleRunData>>> lines,
      final HashMap<Pair<String, List<CStyleRunData>>, ArrayList<CommentContainer>> commentsToLineAssociation,
      final ZyGraphViewSettings graphSettings,
      final INodeModifier modifier) {
    int maxLineWidth = 0;

    final Map<INaviInstruction, INaviFunction> instructionToFunctionReferences =
        CReferenceFinder.getCodeReferenceMap(node);

    for (final INaviInstruction instruction : node.getInstructions()) {
      final Pair<String, List<CStyleRunData>> zyLineContent =
          ZyInstructionBuilder.buildInstructionLine(instruction, graphSettings, modifier);

      final ArrayList<CommentContainer> commentLineContainerList =
          new ArrayList<CommentContainer>();

      final List<IComment> localComments =
          node.getComments().getLocalInstructionComment(instruction);
      if (localComments != null) {
        for (final IComment localComment : localComments) {
          commentLineContainerList.add(new CommentContainer(localComment));
        }
      }

      final List<IComment> globalComments = instruction.getGlobalComment();
      if (globalComments != null) {
        for (final IComment globalComment : globalComments) {
          commentLineContainerList.add(new CommentContainer(globalComment));
        }
      }

      final List<IComment> functionComments =
          instructionToFunctionReferences.get(instruction) == null ? null
              : instructionToFunctionReferences.get(instruction).getGlobalComment();
      if (functionComments != null) {
        for (final IComment functionComment : functionComments) {
          commentLineContainerList.add(new CommentContainer(functionComment));
        }
      }

      commentsToLineAssociation.put(zyLineContent, commentLineContainerList);

      final int lineWidth = zyLineContent.first().length();

      if (lineWidth > maxLineWidth) {
        maxLineWidth = lineWidth;
      }

      lines.add(zyLineContent);
    }

    return maxLineWidth;
  }

  /**
   * Inserts the previously created lines into the content with consideration for the comments.
   * 
   * @param content The content to which the lines are added.
   * @param lines The instruction lines to add to the content.
   * @param comments Information about the instruction comments for each line.
   * @param maxLineWidth The maximum line width of all instruction lines in characters.
   */
  private static void insertLines(final ZyLabelContent content,
      final List<Pair<String, List<CStyleRunData>>> lines,
      final HashMap<Pair<String, List<CStyleRunData>>, ArrayList<CommentContainer>> comments,
      final int maxLineWidth) {
    for (final Pair<String, List<CStyleRunData>> lineContent : lines) {
      final ArrayList<CommentContainer> instructionComments = comments.get(lineContent);
      final StringBuilder lineBuilder = new StringBuilder(lineContent.first());
      final List<CStyleRunData> styleRuns = lineContent.second();

      if ((instructionComments == null) || instructionComments.isEmpty()) {
        final ZyLineContent instructionLine =
            new ZyLineContent(lineBuilder.toString(), NORMAL_FONT, styleRuns, null);
        content.addLineContent(instructionLine);
        continue;
      }

      final String instructionFirstCommentLine =
          instructionComments.get(0).getCommentingString() != null ? instructionComments.get(0)
              .getCommentingString().get(0) : null;

      if (instructionFirstCommentLine != null) {
        lineBuilder.append(Strings.repeat(" ", (maxLineWidth - lineBuilder.length()) + 1));
        lineBuilder.append(instructionFirstCommentLine);
      }

      final ZyLineContent instructionLine =
          new ZyLineContent(lineBuilder.toString(), NORMAL_FONT, styleRuns, null);

      if (instructionFirstCommentLine != null) {
        instructionLine
            .setFont(maxLineWidth + 1, instructionFirstCommentLine.length(), ITALIC_FONT);
        instructionLine.setTextColor(maxLineWidth + 1, instructionFirstCommentLine.length(),
            Color.BLACK);
        instructionLine.setTextColor(maxLineWidth + 1, instructionComments.get(0)
            .getCommentUserNameLength(), instructionComments.get(0).getCommentColor());
      }
      content.addLineContent(instructionLine);

      boolean firstCommentContainer = true;
      for (final CommentContainer commentContainer : instructionComments) {
        boolean firstCommentLine = true;
        for (final String partialCommentString : commentContainer.getCommentingString()) {
          if (firstCommentContainer) {
            firstCommentContainer = false;
            continue;
          }
          final ZyLineContent commentLine =
              new ZyLineContent(Strings.repeat(" ", maxLineWidth + 1) + partialCommentString,
                  ITALIC_FONT, null);
          commentLine.setTextColor(Color.BLACK);
          if (firstCommentLine) {
            firstCommentLine = false;
            commentLine.setTextColor(maxLineWidth + 1, commentContainer.getCommentUserNameLength(),
                commentContainer.getCommentColor());
          }
          content.addLineContent(commentLine);
        }
      }
    }
  }

  /**
   * Builds the visible content of a code node.
   * 
   * @param node The code node that provides the raw data.
   * @param graphSettings Provides settings that influence node formatting.
   * @param modifier Calculates the address strings. This argument can be null.
   * 
   * @return The content that was built from the information from the raw node.
   */
  public static ZyLabelContent buildContent(final INaviCodeNode node,
      final ZyGraphViewSettings graphSettings, final INodeModifier modifier) {
    Preconditions.checkNotNull(node, "IE00897: Node argument can not be null");

    final ZyLabelContent content = new ZyLabelContent(new IZyEditableObject() {
      @Override
      public int getEnd() {
        throw new IllegalStateException();
      }

      @Override
      public int getLength() {
        throw new IllegalStateException();
      }

      @Override
      public Object getPersistentModel() {
        throw new IllegalStateException();
      }

      @Override
      public int getStart() {
        throw new IllegalStateException();
      }

      @Override
      public boolean isCommentDelimiter() {
        throw new IllegalStateException();
      }

      @Override
      public boolean isPlaceholder() {
        throw new IllegalStateException();
      }

      @Override
      public boolean update(final String newContent) {
        throw new IllegalStateException();
      }

      @Override
      public boolean updateComment(final String newContent, final ECommentPlacement placement) {
        throw new IllegalStateException();
      }
    }, true, false);

    buildContent(content, node, graphSettings, modifier);

    return content;
  }

  public static void buildContent(final ZyLabelContent content, final INaviCodeNode node,
      final ZyGraphViewSettings graphSettings, final INodeModifier modifier) {
    Preconditions.checkNotNull(node, "IE01533: Node argument can not be null");

    while (content.getLineCount() != 0) {
      content.removeLine(0);
    }

    // In the first line of a flow graph node, the address of the
    // function from which the basic block was taken is displayed in bold font.
    buildFunctionLine(node, content, modifier);

    // Afterwards follow all the instruction lines.
    buildInstructionLines(node, content, graphSettings, modifier);
  }
}
