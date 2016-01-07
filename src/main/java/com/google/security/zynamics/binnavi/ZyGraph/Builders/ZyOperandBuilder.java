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

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.config.ColorsConfigItem;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.debug.helpers.RelocationChecker;
import com.google.security.zynamics.binnavi.disassembly.CFunctionReplacement;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.OperandDisplayStyle;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IReplacement;
import com.google.security.zynamics.zylib.disassembly.OperandOrderIterator;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;

import java.awt.Color;
import java.math.BigInteger;
import java.util.List;

/**
 * Contains code for building operands that are shown in graph windows.
 */
public final class ZyOperandBuilder {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyOperandBuilder() {}

  /**
   * Adds closing delimiters to a line if necessary. This is necessary because for certain opening
   * delimiters, the closing delimiter is not actually part of the operand tree as stored in the
   * database.
   *
   * @param line The closing delimiter is added to this string.
   * @param styleRuns The style run for the closing delimiter is added to this list.
   * @param hasMemderef True, if a memory dereference delimiter should be added.
   * @param hasExpressionList True, if an expression list delimiter should be added.
   * @param hasExclamationMark True, if an exclamation mark delimiter should be added.
   */
  private static void addClosingDelimiters(final StringBuffer line,
      final List<CStyleRunData> styleRuns, final boolean hasMemderef,
      final boolean hasExpressionList, final boolean hasExclamationMark) {
    // TODO(timkornau): Figure out if more than one of the boolean arguments can be true at any
    // given invocation.

    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();
    if (hasMemderef) {
      // Since closing "]" characters are not part of the operand trees,
      // we have to handle memory dereference brackets manually.
      styleRuns.add(new CStyleRunData(line.length(), 1, colors.getMemRefColor()));
      line.append(']');
    }

    if (hasExpressionList) {
      // Since closing "}" characters are not part of the operand trees,
      // we have to handle expression list braces manually.
      styleRuns.add(new CStyleRunData(line.length(), 1, colors.getExpressionListColor()));
      line.append('}');
    }

    if (hasExclamationMark) {
      styleRuns.add(new CStyleRunData(line.length(), 1, colors.getOperatorColor()));
      line.append('!');
    }
  }

  /**
   * Adds a comma separator to the created line if necessary.
   *
   * @param line The comma is added to this string.
   * @param styleRuns The style run for the comma is added to this list.
   * @param operands Number of operands used to determine whether a comma is necessary.
   * @param operandIndex Index of the current operand.
   */
  private static void addCommaSeparator(final StringBuffer line,
      final List<CStyleRunData> styleRuns, final List<? extends INaviOperandTree> operands,
      final int operandIndex) {
    // Separate individual operands with a comma.
    if (operandIndex < operands.size() - 1) {
      styleRuns.add(new CStyleRunData(line.length(), 2,
          ConfigManager.instance().getColorSettings().getOperandSeparatorColor()));
      line.append(", ");
    }
  }

  /**
   * Adds an operand to the instruction line.
   *
   * @param line The operand is added to this string.
   * @param styleRuns The style run for the operand is added to this list.
   * @param treeNode Provides the operand information.
   * @param modifier Calculates the address string (this argument can be null).
   */
  private static void addOperand(final StringBuffer line, final List<CStyleRunData> styleRuns,
      final COperandTreeNode treeNode, final INodeModifier modifier) {

    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();
    final String typeSubstitution = getTypeSubstitution(treeNode);
    if (!typeSubstitution.isEmpty()) {
      // TODO(jannewger): we might want to introduce an additional setting so the user is able to
      // customize the way types are displayed.
      styleRuns.add(new CStyleRunData(line.length(), typeSubstitution.length(),
          colors.getVariableColor(), treeNode));
      line.append(typeSubstitution);
      return;
    }

    final IReplacement replacement =
        treeNode.getDisplayStyle() == OperandDisplayStyle.OFFSET ? treeNode.getReplacement() : null;

    // Colorize the current part of the operand
    if (replacement == null) {
      final Color color = getOperandColor(treeNode.getType());
      final String value = adjustValue(treeNode, modifier);
      styleRuns.add(new CStyleRunData(line.length(), value.length(), color, treeNode));
      line.append(value);
    } else {
      final String replacementString = determineReplacementString(treeNode, replacement);
      if (replacementString.equalsIgnoreCase("")) {
        final Color color = getOperandColor(treeNode.getType());
        final String value = adjustValue(treeNode, modifier);
        styleRuns.add(new CStyleRunData(line.length(), value.length(), color, treeNode));
        line.append(value);
        return;
      }
      if (treeNode.getType() == ExpressionType.IMMEDIATE_INTEGER) {
        if (replacement instanceof CFunctionReplacement) {
          styleRuns.add(new CStyleRunData(line.length(), replacementString.length(),
              colors.getFunctionColor(), treeNode));
        } else {
          styleRuns.add(new CStyleRunData(line.length(), replacementString.length(),
              colors.getVariableColor(), treeNode));
        }
      } else {
        final Color color = getOperandColor(treeNode.getType());
        styleRuns.add(
            new CStyleRunData(line.length(), replacementString.length(), color, treeNode));
      }
      line.append(replacementString);
    }
  }

  /**
   * Depending on the type of the given operand tree node, the displayed value has to be adjusted
   * (for example because of relocation).
   *
   * @param treeNode Provides the value to adjust.
   * @param modifier Modifier used for adjusting the tree node value.
   *
   * @return The adjusted tree node value.
   */
  private static String adjustValue(final COperandTreeNode treeNode, final INodeModifier modifier) {
    if (treeNode.getType() == ExpressionType.IMMEDIATE_INTEGER) {
      return buildIntegerOperand(treeNode, modifier);
    } else if (treeNode.getType() == ExpressionType.SIZE_PREFIX) {
      // Separate the prefixes from the following operand part
      return treeNode.getValue() + " ";
    }
    return treeNode.getValue();
  }

  private static String getUnsignedBigIntegerString(final BigInteger value,
      final COperandTreeNode node, final int radix) {
    // We only need to take care of values which are less than zero.
    if (value.signum() != -1) {
      return value.toString(radix);
    }

    INaviOperandTreeNode currentNode = node.getParent();
    while (currentNode.getType() != ExpressionType.SIZE_PREFIX) {
      if (currentNode.getParent() == null) {
        throw new IllegalStateException("Error: could not determine size of operand.");
      }
      currentNode = currentNode.getParent();
    }

    final BigInteger twosComplement = value.abs().not().add(BigInteger.ONE);
    // TODO(timkornau): The code here uses strings as the current implementation of the operand
    // nodes stores all value information in a string field. To change this the size and value
    // information of an operand node need to be stored in different fields. b/11566525
    switch (currentNode.getValue()) {
      case "byte":
        return twosComplement.and(new BigInteger("FF", 16)).toString(radix);
      case "word":
        return twosComplement.and(new BigInteger("FFFF", 16)).toString(radix);
      case "dword":
        return twosComplement.and(new BigInteger("FFFFFFFF", 16)).toString(radix);
      case "fword":
        return twosComplement.and(new BigInteger("FFFFFFFFFFFF", 16)).toString(radix);
      case "qword":
        return twosComplement.and(new BigInteger("FFFFFFFFFFFFFFFF", 16)).toString(radix);
      case "oword":
        return twosComplement.and(new BigInteger("FFFFFFFFFFFFFFFFFFFF", 16)).toString(radix);
      default:
        throw new IllegalStateException("Error: size of operand tree node is not supported.");
    }
  }

  /**
   * Builds an integer operand.
   *
   * @param operandTreeNode The tree node that provides the integer value to build.
   * @param nodeModifier Used to modify the integer value in case of relocations.
   *
   * @return The built integer operand string.
   */
  private static String buildIntegerOperand(final COperandTreeNode operandTreeNode,
      final INodeModifier nodeModifier) {
    BigInteger treeNodeValue = new BigInteger(operandTreeNode.getValue());

    if ((nodeModifier != null) && RelocationChecker.needsRelocation(operandTreeNode,
        operandTreeNode.getOperand().getInstruction().getModule())) {
      treeNodeValue = relocateIntegerOperand(operandTreeNode, nodeModifier, treeNodeValue);
    }

    return buildIntegerOperandString(operandTreeNode, treeNodeValue);
  }

  /**
   * Relocates an integer operand of a {@link COperandTreeNode operandTreeNode}. For the relocation
   * the passed in {@link INodeModifier modifier} is used.
   *
   * @param operandTreeNode the {@link COperandTreeNode} where the value needs to be relocated.
   * @param nodeModifier The {@link INodeModifier} used for the relocation.
   * @param treeNodeValue The value of the {@link COperandTreeNode}.
   *
   * @return A {@link BigInteger} with the relocated value.
   */
  private static BigInteger relocateIntegerOperand(final COperandTreeNode operandTreeNode,
      final INodeModifier nodeModifier, final BigInteger treeNodeValue) {
    return new BigInteger(nodeModifier.getAddress(
        operandTreeNode.getOperand().getInstruction().getModule(),
        new UnrelocatedAddress(new CAddress(treeNodeValue)), false), 16 /* hex string */);
  }

  /**
   * Generates a {@link String} representation of the {@link COperandTreeNode operandTreeNode}
   * depending on its {@link OperandDisplayStyle display style}.
   *
   * @param operandTreeNode The {@link COperandTreeNode} to determine the {@link String}
   *        representation for.
   * @param treeNodeValue The value of the {@link COperandTreeNode}. This is not determined within
   *        the function as the {@link BigInteger treeNodeValue} has potentially been altered by
   *        relocations.
   * @return A string representation of the integer operand.
   */
  private static String buildIntegerOperandString(final COperandTreeNode operandTreeNode,
      final BigInteger treeNodeValue) {
    switch (operandTreeNode.getDisplayStyle()) {
      case CHAR:
        return bigIntegerToAsciiString(treeNodeValue);
      case OFFSET:
        break;
      case SIGNED_DECIMAL:
        return treeNodeValue.toString(10) + "d";
      case SIGNED_HEXADECIMAL:
        return treeNodeValue.toString(16) + "h";
      case BINARY:
        return treeNodeValue.toString(2) + "b";
      case UNSIGNED_DECIMAL:
        return getUnsignedBigIntegerString(treeNodeValue, operandTreeNode, 10) + "d";
      case UNSIGNED_HEXADECIMAL:
        return getUnsignedBigIntegerString(treeNodeValue, operandTreeNode, 16) + "h";
      case OCTAL:
        return getUnsignedBigIntegerString(treeNodeValue, operandTreeNode, 8) + "o";
      default:
        return operandTreeNode.getValue();
    }
    return operandTreeNode.getValue();
  }

  /**
   * This function is intended to convert operands for display, for example to convert a number such
   * as 0x41424141 into 'ABAA'. Also, given Java's difficulty with unsigned integers, we need to
   * make sure to get an unsigned number.
   *
   * @param bigInteger BigInteger that will be converted
   * @return converted BigInteger numbers into ASCII Code Char-String.
   */
  private static String bigIntegerToAsciiString(final BigInteger bigInteger) {
    final byte[] byteArray = bigInteger.toByteArray();
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < byteArray.length; i++) {
      final char value = (char) (byteArray[i] & 0xFF);
      if (i == 0 && value == 0) {
        continue; // skip a leading null byte from bigintegers minimal representation.
      }
      stringBuffer.append(value);
    }
    return stringBuffer.toString();
  }

  /**
   * Builds a single operand.
   *
   * @param instruction The instruction in question.
   * @param operandTree Provides information about the operand to build.
   * @param graphSettings Provides the graph settings.
   * @param line String buffer where the operands string is added.
   * @param styleRun Style runs list where the formatting information is added.
   * @param modifier Calculates the address string (this argument can be null).
   * @param counter The index of the operand in the operands list of the instruction.
   */
  private static void buildOperand(final INaviInstruction instruction,
      final INaviOperandTree operandTree,
      final ZyGraphViewSettings graphSettings,
      final StringBuffer line,
      final List<CStyleRunData> styleRun,
      final INodeModifier modifier,
      final int counter) {
    // We use an iterator that gives us the individual operands in correct (printing) order.
    final OperandOrderIterator iter = new OperandOrderIterator(operandTree.getRootNode());

    final boolean isVariableAccess =
        graphSettings.getDisplaySettings().getSimplifiedVariableAccess()
        && isVariableAccess(operandTree.getRootNode());

    boolean hasMemderef = false;
    boolean hasExpressionList = false;
    boolean needsComma = false;
    boolean hasExclamationMark = false;

    COperandTreeNode memParent = null;
    final ColorsConfigItem colors = ConfigManager.instance().getColorSettings();

    while (iter.next()) {
      if (isVariableAccess) {
        skipMemoryAccess(iter);
      }

      // Process the next operand part
      final COperandTreeNode treeNode = (COperandTreeNode) iter.current();

      hasExclamationMark = "!".equals(treeNode.getValue());

      if (skipOperand(treeNode, hasExpressionList)) {
        continue;
      }

      if (needsComma) {
        styleRun.add(new CStyleRunData(line.length(), 1, colors.getOperandSeparatorColor()));
        line.append(',');
      }

      if (hasExpressionList) {
        needsComma = true;
      }

      if (hasMemderef && !isAncestor(treeNode, memParent)) {
        // Since closing "]" characters are not part of the operand trees,
        // we have to handle memory dereference brackets manually.
        styleRun.add(new CStyleRunData(line.length(), 1, colors.getMemRefColor()));
        line.append(']');

        hasMemderef = false;
      }

      hasExpressionList =
          (treeNode.getType() == ExpressionType.EXPRESSION_LIST) || hasExpressionList;

      if (treeNode.getType() == ExpressionType.MEMDEREF) {
        memParent = treeNode;
        hasMemderef = true;
      }

      addOperand(line, styleRun, treeNode, modifier);
    }

    addClosingDelimiters(line, styleRun, hasMemderef, hasExpressionList, hasExclamationMark);

    addCommaSeparator(line, styleRun, instruction.getOperands(), counter);
  }

  /**
   * Returns the appropriate replacement string for a given {@link COperandTreeNode node}. The
   * preference is to return a {@link TypeInstanceReference reference} rather than a
   * {@link IReplacement replacement}.
   *
   * @param treeNode The {@link COperandTreeNode} to check for a {@link TypeInstanceReference
   *        reference}.
   * @param replacement The {@link IReplacement} associated with the {@link COperandTreeNode node}.
   * @return A replacement string for the {@link COperandTreeNode node}.
   */
  private static String determineReplacementString(final COperandTreeNode treeNode,
      final IReplacement replacement) {
    if (!treeNode.getTypeInstanceReferences().isEmpty()) {
      return treeNode.getTypeInstanceReferences().get(0).getTypeInstance().getName();
    } else {
      return replacement.toString();
    }
  }

  /**
   * Returns a type substitution string iff the given node is an immediate integer that is part of
   * an expression with a register that in turn has a type substitution.
   *
   *  One difficulty is, that in an expression such as [esp+4], the type is actually attached to
   * "esp" but the string "4" needs to be replaced with the type substitution string. This method
   * determines the string substitution for the given tree node (in this example we would get a
   * substitution for the node corresponding to "4").
   */
  private static String getImmediateSubstitution(final COperandTreeNode treeNode) {
    final INaviOperandTreeNode parent = treeNode.getParent();
    if (parent != null && parent.getValue().equals("+")) {
      // A node with a value of "+" has exactly two children, so the child that does not contain
      // an immediate value might have a type substitution attached to it.
      final INaviOperandTreeNode otherChild = getSibling(treeNode);
      if (otherChild != null && otherChild.getTypeSubstitution() != null) {
        final long operandValue = Long.parseLong(treeNode.getValue());
        return TypeSubstitution.generateTypeString(otherChild.getTypeSubstitution(), operandValue);
      }
    }
    return "";
  }

  /**
   * Returns the memory access node of an operand.
   *
   * @param node The root node of the operand.
   *
   * @return The memory access node of the operand.
   *
   * @throws MaybeNullException Thrown if there is no memory access operand.
   */
  private static INaviOperandTreeNode getMemoryAccessNode(final INaviOperandTreeNode node)
      throws MaybeNullException {
    switch (node.getType()) {
      case SYMBOL:
      case IMMEDIATE_INTEGER:
      case REGISTER:
      case IMMEDIATE_FLOAT:
      case EXPRESSION_LIST:
        throw new MaybeNullException();
      case OPERATOR:
        if (node.getChildren().size() == 1) {
          return getMemoryAccessNode(node.getChildren().get(0));
        } else {
          throw new MaybeNullException();
        }
      case MEMDEREF:
        return node;
      case SIZE_PREFIX:
        return getMemoryAccessNode(node.getChildren().get(0));
      default:
        throw new IllegalArgumentException("IE00702: Unknown node type");
    }
  }

  /**
   * Returns the color to be used to display a given operand type.
   *
   * @param type The operand type whose color is returned.
   *
   * @return The color information for the operand type.
   */
  private static Color getOperandColor(final ExpressionType type) {
    switch (type) {
      case IMMEDIATE_INTEGER:
        return ConfigManager.instance().getColorSettings().getImmediateColor();
      case MEMDEREF:
        return ConfigManager.instance().getColorSettings().getMemRefColor();
      case OPERATOR:
        return ConfigManager.instance().getColorSettings().getOperatorColor();
      case SIZE_PREFIX:
        return ConfigManager.instance().getColorSettings().getPrefixColor();
      case SYMBOL:
        return ConfigManager.instance().getColorSettings().getStringColor();
      case REGISTER:
        return ConfigManager.instance().getColorSettings().getRegisterColor();
      case EXPRESSION_LIST:
        return ConfigManager.instance().getColorSettings().getExpressionListColor();
      case IMMEDIATE_FLOAT:
        return ConfigManager.instance().getColorSettings().getImmediateColor();
      default:
        throw new IllegalStateException("IE02247: Unknown expression type");
    }
  }

  /**
   * Returns a type substitution if the given node represents a register and is not part of an
   * expression containing other operands. The format is: %type_name% %register_name%.
   */
  private static String getRegisterSubstitution(final COperandTreeNode node) {
    if (node.getTypeSubstitution() != null && node.getParent() != null
        && node.getParent().getChildren().size() == 1) {
      return String.format("%s %s",
          TypeSubstitution.generateTypeString(node.getTypeSubstitution(), 0), node.getValue());
    }
    return "";
  }

  private static INaviOperandTreeNode getSibling(final INaviOperandTreeNode node) {
    final INaviOperandTreeNode parent = node.getParent();
    if (parent.getChildren().size() != 2) {
      return null;
    } else {
      return parent.getChildren().get(0) == node ? parent.getChildren().get(1)
          : parent.getChildren().get(0);
    }
  }

  private static String getTypeSubstitution(final COperandTreeNode treeNode) {
    switch (treeNode.getType()) {
      case IMMEDIATE_INTEGER:
        return getImmediateSubstitution(treeNode);
      case REGISTER:
        return getRegisterSubstitution(treeNode);
      default:
        return "";
    }
  }

  /**
   * Determines whether a parent node is an ancestor node of a given child node.
   *
   * @param child The child node to check.
   * @param parent The parent node to check.
   *
   * @return True, if the parent node is an ancestor of the child node.
   */
  private static boolean isAncestor(final COperandTreeNode child, final COperandTreeNode parent) {
    if (child == parent) {
      return true;
    }

    if (child.getParent() == null) {
      return false;
    }

    return isAncestor((COperandTreeNode) child.getParent(), parent);
  }

  /**
   * Determines whether a given node is a variable access node that can be simplified.
   *
   * @param node The node to check.
   *
   * @return True, if the node can be simplified. False, otherwise.
   */
  private static boolean isOperatorVariableAccess(final INaviOperandTreeNode node) {
    final List<INaviOperandTreeNode> children = node.getChildren();

    if (children.size() == 2) {
      // An expression can be simplified if it is of the form 'REGISTER + VARIABLE'

      final INaviOperandTreeNode child0 = children.get(0);
      final INaviOperandTreeNode child1 = children.get(1);

      return isVariable(child0) ^ isVariable(child1);
    } else {
      return false;
    }
  }

  /**
   * Returns whether a given node is a variable.
   *
   * @param node The node to check.
   *
   * @return True, if the node is a variable. False, otherwise.
   */
  private static boolean isVariable(final INaviOperandTreeNode node) {
    return (node.getType() == ExpressionType.IMMEDIATE_INTEGER) && (node.getReplacement() != null)
        && (node.getDisplayStyle() == OperandDisplayStyle.OFFSET);
  }

  /**
   * Determines whether a given node represents variable access.
   *
   * @param node The node to check.
   *
   * @return True, if the node represents an expression that accesses a variable. False, otherwise.
   */
  private static boolean isVariableAccess(final INaviOperandTreeNode node) {
    try {
      final INaviOperandTreeNode memoryAccessNode = getMemoryAccessNode(node);

      final List<INaviOperandTreeNode> children = memoryAccessNode.getChildren();

      if (children.size() == 1) {
        final INaviOperandTreeNode child = children.get(0);

        switch (child.getType()) {
          case SIZE_PREFIX:
            return isVariable(child.getChildren().get(0));
          case IMMEDIATE_INTEGER:
            return isVariable(child);
          case OPERATOR:
            return isOperatorVariableAccess(child);
          default:
            return false;
        }
      } else {
        throw new IllegalStateException("IE00703: Invalid tree shape");
      }
    } catch (final MaybeNullException e) {
      return false;
    }
  }

  /**
   * Skips all the nodes that should not be printed in simplified variable access mode.
   *
   * @param iter Iterator used to skip over the nodes.
   */
  private static void skipMemoryAccess(final OperandOrderIterator iter) {
    do {
      if (iter.current().getType() == ExpressionType.MEMDEREF) {
        iter.next();

        while (iter.current().getType() != ExpressionType.IMMEDIATE_INTEGER) {
          iter.next();
        }

        return;
      }
    } while (iter.next());
  }

  /**
   * Determines whether an operand should be skipped.
   *
   * @param treeNode The next operand to consider.
   * @param hasExpressionList
   *
   * @return True, if the operand should be skipped.
   */
  private static boolean skipOperand(final COperandTreeNode treeNode,
      final boolean hasExpressionList) {
    if ("!".equals(treeNode.getValue())) {
      return true;
    }

    // By convention the prefix "dword" is not shown in the graphs.
    if ("dword".equals(treeNode.getValue())) {
      return true;
    }

    if (hasExpressionList && (treeNode.getType() == ExpressionType.EXPRESSION_LIST)) {
      return true;
    }

    return false;
  }

  /**
   * Builds the operands of an instruction.
   *
   * @param instruction The instruction in question.
   * @param graphSettings Provides the graph settings.
   * @param line String buffer where the operands string is added.
   * @param styleRun Style runs list where the formatting information is added.
   * @param modifier Calculates the address string (this argument can be null).
   */
  public static void buildOperands(final INaviInstruction instruction,
      final ZyGraphViewSettings graphSettings, final StringBuffer line,
      final List<CStyleRunData> styleRun, final INodeModifier modifier) {
    int counter = 0;

    for (final INaviOperandTree operandTree : instruction.getOperands()) {
      buildOperand(instruction, operandTree, graphSettings, line, styleRun, modifier, counter);

      counter++;
    }
  }
}
