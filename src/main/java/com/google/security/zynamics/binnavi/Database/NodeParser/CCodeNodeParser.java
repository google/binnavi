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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.cache.InstructionCache;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionReplacement;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.binnavi.disassembly.CStringReplacement;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviReplacement;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The code node parser is used to build code nodes of an entire function.
 */
public final class CCodeNodeParser {
  /**
   * Maps module IDs to their module objects.
   */
  private final Map<Integer, INaviModule> modules = new HashMap<Integer, INaviModule>();

  /**
   * Stores a mapping from local comment id to code node for comment bulk loading.
   */
  final Map<Integer, INaviCodeNode> localCommentIdToCodeNode =
      new HashMap<Integer, INaviCodeNode>();

  /**
   * Stores a mapping from global comment id to code node for comment bulk loading
   */
  final Map<Integer, INaviCodeNode> globalCommentIdToCodeNode =
      new HashMap<Integer, INaviCodeNode>();

  /**
   * Stores a mapping from local comment id to instruction for comment bulk loading.
   */
  final Map<Integer, Pair<INaviInstruction, INaviCodeNode>> localCommentIdToInstruction =
      new HashMap<Integer, Pair<INaviInstruction, INaviCodeNode>>();

  /**
   * Stores a mapping from global comment if to instruction for comment bulk loading.
   */
  final Map<Integer, INaviInstruction> globalCommentIdToInstruction =
      new HashMap<Integer, INaviInstruction>();

  /**
   * Provides the raw instruction data.
   */
  private final ICodeNodeProvider dataProvider;

  /**
   * The connection to the database.
   */
  private final SQLProvider sqlProvider;

  /**
   * Nodes created by the parser.
   */
  private final List<CCodeNode> nodes = new ArrayList<CCodeNode>();

  /**
   * Currently parsed node.
   */
  private CCodeNode currentNode;

  /**
   * Currently parsed instruction.
   */
  private InstructionLine currentLine;

  /**
   * Creates a new instruction parser object.
   *
   * @param modules The modules the loaded function belongs to.
   * @param dataProvider The code node provider that provides the raw data from which the nodes are
   *        built.
   * @param sqlProvider The SQL provider that is passed to all objects created by the parser.
   */
  public CCodeNodeParser(final ICodeNodeProvider dataProvider,
      final List<? extends INaviModule> modules, final SQLProvider sqlProvider) {
    this.dataProvider =
        Preconditions.checkNotNull(dataProvider, "IE00645: Data provider argument can't be null");
    this.sqlProvider =
        Preconditions.checkNotNull(sqlProvider, "IE00646: SQL provider argument can't be null");
    Preconditions.checkNotNull(modules, "IE01664: modules argument can not be null");

    for (final INaviModule module : modules) {
      this.modules.put(module.getConfiguration().getId(), module);
    }
  }

  /**
   * Creates a new operand tree node object from data from a code node provider.
   *
   * @param module The module the loaded function belongs to.
   * @param dataset Provides the operand data.
   *
   * @return The created operand tree node object.
   *
   * @throws ParserException Thrown if not all data for the operand tree node object could be read.
   */
  private static OperandTreeNode createNewOperand(
      final INaviModule module, final ICodeNodeProvider dataset) throws ParserException {
    final int expressionId = dataset.getExpressionTreeId();
    final int type = dataset.getExpressionTreeType();
    final String value = getValue(dataset, type);
    final Integer parentId = dataset.getParentId();
    final String replacementString = dataset.getReplacement();
    final IAddress functionAddress = dataset.getFunctionAddress();
    final Integer typeId = dataset.getSubstitutionTypeId();
    RawTypeSubstitution substitution = null;

    if (typeId != null) {
      substitution = new RawTypeSubstitution(
          dataset.getInstructionAddress(), dataset.getSubstitutionPosition(), expressionId, typeId,
          dataset.getSubstitutionPath(), dataset.getSubstitutionOffset());
    }
    final Integer instanceId =
        dataset.getTypeInstanceId() == null ? null : dataset.getTypeInstanceId();
    final int operandPosition = dataset.getOperandPosition();
    final IAddress address = dataset.getInstructionAddress();

    // The function parse references moves the dataset around quite heavily therefore all direct
    // access to the dataset must be done before.
    final List<CReference> references = parseReferences(expressionId, dataset);

    final INaviReplacement replacement =
        lookupReplacement(replacementString, module, functionAddress);

    return new OperandTreeNode(expressionId, type, value, getParentId(parentId), replacement,
        references, substitution, instanceId, operandPosition, address);
  }

  /**
   * Normalizes a parent ID for object creation.
   *
   * @param parentId The parent ID to normalize.
   *
   * @return The normalized parent ID or null if there is no parent.
   */
  private static Integer getParentId(final Integer parentId) {
    return parentId == 0 ? null : parentId;
  }

  /**
   * Returns the value of an operand expression.
   *
   * @param dataset Provides the raw operand data.
   * @param type The raw operand type.
   *
   * @return The value of the operand.
   *
   * @throws ParserException Thrown if the value of the operand could not be determined.
   */
  private static String getValue(final ICodeNodeProvider dataset, final int type)
      throws ParserException {
    return type == 2 ? dataset.getImmediate() : dataset.getSymbol();
  }

  /**
   * Parses the outgoing references of an operand expression.
   *
   * @param expressionId The expression ID of the operand expression.
   * @param dataset Provides the reference data.
   *
   * @return The outgoing references of the operand expression.
   *
   * @throws ParserException Thrown if the reference data could not be read.
   */
  private static List<CReference> parseReferences(
      final int expressionId, final ICodeNodeProvider dataset) throws ParserException {

    final List<CReference> references = new ArrayList<CReference>();
    boolean hasReferences = false;

    do {
      final CReference reference = dataset.getReference();

      if (reference == null) {
        if (hasReferences) {
          dataset.prev();
        }

        break;
      }

      hasReferences = true;

      final int currentExpressionId = dataset.getExpressionTreeId();

      if (expressionId != currentExpressionId) {
        dataset.prev();
        break;
      }

      references.add(reference);

    } while (dataset.next());

    return references;
  }

  /**
   * Creates an {@link INaviReplacement replacement} to be displayed instead of a raw number in a
   * {@link INaviOperandTreeNode operand node}.
   *
   * @param replacementString The replacement string.
   * @param module The module the loaded view belongs to.
   * @param functionAddress Address of the function the replacement refers to.
   * @return The replacement object or null.
   */
  // TODO(timkornau): This code is legacy. As soon as the support for
  // function prototypes reaches a usable state everything but the string replacement should be
  // removed from this function.
  private static INaviReplacement lookupReplacement(
      final String replacementString, final INaviModule module, final IAddress functionAddress) {

    if (functionAddress != null) {
      final INaviFunction function =
          module.getContent().getFunctionContainer().getFunction(functionAddress);

      if (function != null) {
        return new CFunctionReplacement(function);
      }
    }

    if (replacementString != null) {
      return new CStringReplacement(replacementString);
    } else {
      return null;
    }
  }

  /**
   * Adds an instruction to a code node.
   *
   * @param node The code node where the instruction is added.
   * @param line The raw instruction data to add.
   */
  private void addInstruction(final CCodeNode node, final InstructionLine line) {

    final CInstruction instruction = InstructionConverter.createInstruction(line, sqlProvider);

    InstructionCache.get(sqlProvider).addInstruction(instruction);

    localCommentIdToInstruction.put(line.getLocalInstructionCommentId(),
        new Pair<INaviInstruction, INaviCodeNode>(instruction, node));
    globalCommentIdToInstruction.put(line.getGlobalInstructionComment(), instruction);
    localCommentIdToCodeNode.put(line.getLocalNodeCommentId(), node);
    globalCommentIdToCodeNode.put(line.getGlobalNodeCommentId(), node);

    node.addInstruction(instruction, null);
  }

  /**
   * Creates a new code node.
   *
   * @param resultSet Provides the data for the code node.
   *
   * @return The created code node object.
   *
   * @throws ParserException Thrown if the node data could not be read.
   * @throws CPartialLoadException Thrown if not all required modules are loaded.
   */
  private CCodeNode createCurrentNode(final ICodeNodeProvider resultSet)
      throws ParserException, CPartialLoadException {
    final int nodeId = resultSet.getNodeId();

    final int moduleId = resultSet.getModule();
    final IAddress parentFunction = resultSet.getParentFunction();

    final INaviModule module = modules.get(moduleId);

    if (module == null) {
      throw new ParserException(
          String.format("Node with ID %d has unknown parent module with ID %d", nodeId, moduleId));
    }

    if (!module.isLoaded()) {
      try {
        module.load();
      } catch (final CouldntLoadDataException e) {
        throw new CPartialLoadException(
            "E00066: The view could not be loaded because not all modules that form the view could be loaded",
            module);
      } catch (final LoadCancelledException e) {
        throw new CPartialLoadException(
            "E00067: The view could not be loaded because it was cancelled", module);
      }
    }

    final INaviFunction function = parentFunction == null ? null
        : module.getContent().getFunctionContainer().getFunction(parentFunction);

    if ((parentFunction != null) && (function == null)) {
      throw new ParserException(String.format(
          "Node with ID %d has unknown parent function with address %s", nodeId,
          parentFunction.toHexString()));
    }

    final double x = resultSet.getX();
    final double y = resultSet.getY();
    final double width = resultSet.getWidth();
    final double height = resultSet.getHeight();
    final Color color = new Color(resultSet.getColor());
    final Color bordercolor = new Color(resultSet.getBorderColor());
    final boolean selected = resultSet.isSelected();
    final boolean visible = resultSet.isVisible();
    final Integer localCodeNodeCommentId = resultSet.getLocalNodeCommentId();
    final Integer globalCodeNodeCommentId = resultSet.getGlobalNodeCommentId();

    // TODO(timkornau): final new Set<CTag>! must replaced by a set which
    // contains the loaded node
    // tags from the DB
    final CCodeNode codeNode = new CCodeNode(
        nodeId, x, y, width, height, color, bordercolor, selected, visible, null, function,
        new HashSet<CTag>(), sqlProvider);

    if (localCodeNodeCommentId != null) {
      localCommentIdToCodeNode.put(localCodeNodeCommentId, codeNode);
    }

    if (globalCodeNodeCommentId != null) {
      globalCommentIdToCodeNode.put(globalCodeNodeCommentId, codeNode);
    }

    return codeNode;
  }

  /**
   * Creates raw instruction data from a code node provider.
   *
   * @param instructionSet Provides the raw data.
   *
   * @return The generated raw data object.
   *
   * @throws ParserException Thrown if not all instruction data could be read.
   */
  private InstructionLine createLine(final ICodeNodeProvider instructionSet)
      throws ParserException {
    final InstructionLine row = new InstructionLine();

    row.setBasicBlock(instructionSet.getNodeId());
    row.setAddress(instructionSet.getInstructionAddress());
    row.setMnemonic(instructionSet.getMnemonic());
    row.setArchitecture(instructionSet.getInstructionArchitecture());

    final int moduleId = instructionSet.getModule();
    final IAddress parentFunction = instructionSet.getParentFunction();

    final INaviModule module = modules.get(moduleId);

    if (module == null) {
      throw new ParserException(String.format(
          "Instruction with ID %d has unknown module with ID %d", row.getId(), moduleId));
    }

    final INaviFunction function = parentFunction == null ? null
        : module.getContent().getFunctionContainer().getFunction(parentFunction);
    row.setParentFunction(function);

    if ((parentFunction != null) && (function == null)) {
      throw new ParserException(String.format(
          "Instruction with ID %d has unknown parent function with address %s", row.getId(),
          parentFunction.toHexString()));
    }

    row.setX(instructionSet.getX());
    row.setY(instructionSet.getY());
    row.setColor(new Color(instructionSet.getColor()));
    row.setBorderColor(new Color(instructionSet.getBorderColor()));
    row.setSelected(instructionSet.isSelected());
    row.setVisible(instructionSet.isVisible());
    row.setLocalNodeCommentId(instructionSet.getLocalNodeCommentId());
    row.setGlobalNodeComment(instructionSet.getGlobalNodeCommentId());
    row.setGlobalInstructionComment(instructionSet.getGlobalInstructionCommentId());
    row.setLocalInstructionComment(instructionSet.getLocalInstructionCommentId());
    row.setData(instructionSet.getData());
    row.setModule(module);

    return row;
  }

  /**
   * Extracts a single line from the dataset.
   *
   * @param dataset The dataset which provides the information about the instructions.
   *
   * @return The generated instruction line. Null is returned when there are no more lines left in
   *         the dataset.
   *
   * @throws ParserException
   */
  private InstructionLine extractLine(final ICodeNodeProvider dataset) throws ParserException {
    // Assumption: At this point the SQL result set points to the first
    // row of an instruction.

    if (dataset.isAfterLast()) {
      // We're done. There is no more data to parse.

      return null;
    }

    final InstructionLine row = createLine(dataset);

    OperandTree tree = new OperandTree(dataset.getExpressionTreeId());

    int operandPositionCounter = 0;

    do {
      final IAddress currentAddress = dataset.getInstructionAddress();

      if (!row.getAddress().equals(currentAddress)
          || (row.getBasicBlock() != dataset.getNodeId())) {
        break;
      }

      final Integer position = dataset.getOperandPosition();

      // It is possible that the position is null because the database
      // is set up in a way to return null for non-existing operands.
      // Note that both checks in the if statement are needed because
      // it is possible that more than one instruction without
      // an operand appears in a row.

      if ((position == null) || (position != operandPositionCounter)) {
        // New operand found => Save the old operand
        if (tree.getNodes().size() != 0) {
          row.getOperands().add(tree);
        }

        // Create a new tree for the new operand.
        tree = new OperandTree(dataset.getExpressionTreeId());

        operandPositionCounter = position == null ? 0 : position;
      }

      // The following if is not an else-if to the if before
      // because it is possible that two operands with different
      // but non-null positions appear in a row.

      if (position != null) {
        // No matter what happened before this if, at this
        // point the right operand tree is stored in the tree
        // variable. That means we can simply create the new
        // operand tree node and put it into the tree.

        // Note that at this point we assume that IDs of 0 are invalid. This
        // might not be the case at other databases and we need to look into
        // it.

        final int moduleId = dataset.getModule();

        final INaviModule module = modules.get(moduleId);

        tree.getNodes().add(createNewOperand(module, dataset));
      }

    } while (dataset.next());

    // Make sure not to add empty operand trees for instructions
    // without operands.
    if (tree.getNodes().size() != 0) {
      row.getOperands().add(tree);
    }

    return row;
  }

  /**
   * Builds individual nodes from the given dataset.
   *
   * @param dataset The dataset that provides information about nodes and instructions.
   * @return The node generated by that iteration.
   *
   * @throws ParserException Thrown if the node data could not be read.
   * @throws CPartialLoadException Thrown if not all required modules are loaded.
   */
  private CCodeNode extractNode(final ICodeNodeProvider dataset)
      throws ParserException, CPartialLoadException {
    // Create the first node if necessary.
    if (currentNode == null) {
      currentNode = createCurrentNode(dataset);
    }

    final CCodeNode nodeInProcess = currentNode;

    // We have to get all instructions that belong to the node.
    while ((currentLine = extractLine(dataset)) != null) {
      if (currentLine.getBasicBlock() == currentNode.getId()) {
        // The instruction belongs to the current block => just add it.
        addInstruction(currentNode, currentLine);
      } else {
        // We found an instruction that belongs to the next block
        // => Create a new block and add the instruction there.

        currentNode = new CCodeNode(currentLine.getBasicBlock(), currentLine.getX(),
            currentLine.getY(), currentLine.getWidth(), currentLine.getHeight(),
            currentLine.getColor(), currentLine.getBorderColor(), currentLine.isSelected(),
            currentLine.isVisible(), null, currentLine.getParentFunction(), new HashSet<CTag>(),
            sqlProvider);
        addInstruction(currentNode, currentLine);

        return nodeInProcess;
      }
    }

    // We have reached the end of the data set.
    currentNode = null;

    return nodeInProcess;
  }

  /**
   * Takes the information from the components passed into the constructor and creates a list of
   * nodes from that information.
   *
   * @return The list of nodes created by the parser.
   *
   * @throws ParserException Thrown if the instruction data could not be loaded.
   * @throws CPartialLoadException Thrown if not all necessary modules are loaded.
   */
  public List<CCodeNode> parse() throws ParserException, CPartialLoadException {
    // At the beginning of the parsing process, the data provider
    // is set to before the data set. The parser needs to tell
    // it to point to the proper data.
    if (!dataProvider.next()) {
      return new ArrayList<CCodeNode>();
    }

    // Generate the nodes from the raw data.
    while (true) {
      if (dataProvider.isAfterLast()) {
        if (currentNode != null) {
          nodes.add(currentNode);
        }

        break;
      }

      nodes.add(extractNode(dataProvider));
    }

    final HashSet<Integer> allComments = Sets.newHashSet();
    allComments.addAll(localCommentIdToCodeNode.keySet());
    allComments.addAll(globalCommentIdToCodeNode.keySet());
    allComments.addAll(globalCommentIdToInstruction.keySet());
    allComments.addAll(localCommentIdToInstruction.keySet());

    try {
      final HashMap<Integer, ArrayList<IComment>> commentIdToComments =
          sqlProvider.loadMultipleCommentsById(allComments);

      for (final Entry<Integer, ArrayList<IComment>> commentIdToComment :
          commentIdToComments.entrySet()) {
        if (localCommentIdToCodeNode.containsKey(commentIdToComment.getKey())) {
          localCommentIdToCodeNode.get(commentIdToComment.getKey())
              .getComments().initializeLocalCodeNodeComment(commentIdToComment.getValue());
        }
        if (globalCommentIdToCodeNode.containsKey(commentIdToComment.getKey())) {
          globalCommentIdToCodeNode.get(commentIdToComment.getKey())
              .getComments().initializeGlobalCodeNodeComment(commentIdToComment.getValue());
        }
        if (localCommentIdToInstruction.containsKey(commentIdToComment.getKey())) {
          final Pair<INaviInstruction, INaviCodeNode> instructionToCodeNode =
              localCommentIdToInstruction.get(commentIdToComment.getKey());

          instructionToCodeNode.second().getComments().initializeLocalInstructionComment(
              instructionToCodeNode.first(), commentIdToComment.getValue());
        }
        if (globalCommentIdToInstruction.containsKey(commentIdToComment.getKey())) {
          globalCommentIdToInstruction.get(commentIdToComment.getKey())
              .initializeGlobalComment(commentIdToComment.getValue());
        }
      }


    } catch (final CouldntLoadDataException exception) {
      throw new CPartialLoadException("Error: Comments could not be loaded.", null);
    }

    return nodes;
  }
}
