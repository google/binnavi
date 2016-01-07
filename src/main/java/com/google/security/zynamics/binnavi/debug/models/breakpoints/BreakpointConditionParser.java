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
package com.google.security.zynamics.binnavi.debug.models.breakpoints;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ExpressionNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.FormulaNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.IdentifierNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.MemoryNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.NumberNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.RelationNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.SubNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.parsers.BreakpointCondition.ConditionLexer;
import com.google.security.zynamics.binnavi.parsers.BreakpointCondition.ConditionParser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses breakpoint conditions.
 */
public final class BreakpointConditionParser {
  /**
   * Creates tree objects for each token of the filter strings.
   */
  private static final TreeAdaptor adaptor = new CommonTreeAdaptor() {
    @Override
    public Object create(final Token payload) {
      return new CommonTree(payload);
    }
  };

  /**
   * You are not supposed to instantiate this class.
   */
  private BreakpointConditionParser() {}

  /**
   * Parses an ANTLR tree into a breakpoint condition tree.
   *
   * @param ast The ANTLR tree to convert.
   *
   * @return The converted breakpoint condition tree.
   *
   * @throws RecognitionException Thrown if the tree could not be converted.
   */
  private static ConditionNode convert(final CommonTree ast) throws RecognitionException {
    if (ast == null) {
      throw new RecognitionException();
    }

    switch (ast.getType()) {
      case 0:
        return convert((CommonTree) ast.getChild(0));
      case ConditionParser.SUB_EXPRESSION:
        return new SubNode(convert((CommonTree) ast.getChild(0)));
      case ConditionParser.MEMORY_EXPRESSION:
        return new MemoryNode(convert((CommonTree) ast.getChild(0)));
      case ConditionParser.NUMBER:
        try {
          return new NumberNode(Long.valueOf(ast.getText()));
        } catch (final NumberFormatException exception) {
          throw new RecognitionException();
        }
      case ConditionParser.HEX_NUMBER:
        try {
          return new NumberNode(Long.valueOf(ast.getText().substring(2), 16));
        } catch (final NumberFormatException exception) {
          throw new RecognitionException();
        }
      case ConditionParser.IDENTIFIER:
        return new IdentifierNode(ast.getText());
      case ConditionParser.EQ_SIGN:
      case ConditionParser.GEQ_SIGN:
      case ConditionParser.LEQ_SIGN:
      case ConditionParser.GT_SIGN:
      case ConditionParser.LT_SIGN:
      case ConditionParser.NEQ_SIGN:
      case ConditionParser.NEQ_SIGN_2:
        return new RelationNode(ast.getText(), convert((CommonTree) ast.getChild(0)),
            convert((CommonTree) ast.getChild(1)));
      case ConditionParser.AND:
      case ConditionParser.OR:
        return new ExpressionNode(ast.getText(), createOperator(ast));
      case ConditionParser.ADD:
      case ConditionParser.SUB:
      case ConditionParser.MULT:
      case ConditionParser.DIV:
      case ConditionParser.MOD:
      case ConditionParser.LSH:
      case ConditionParser.RSH:
      case ConditionParser.B_AND:
      case ConditionParser.B_OR:
      case ConditionParser.B_XOR:
        return new FormulaNode(ast.getText(), createOperator(ast));
    }

    throw new IllegalStateException("IE01140: Not yet implemented (" + ast.getType() + ")");
  }

  /**
   * Converts the children of an n-ary operator node.
   *
   * @param ast The root node of the n-ary operator.
   *
   * @return The converted child nodes.
   *
   * @throws RecognitionException Thrown if any of the children could not be converted.
   */
  private static List<ConditionNode> createOperator(final CommonTree ast)
      throws RecognitionException {
    final List<ConditionNode> children = new ArrayList<>();
    for (final Object child : ast.getChildren()) {
      children.add(convert((CommonTree) child));
    }
    return children;
  }

  /**
   * Turns breakpoint condition formulas into breakpoint condition objects.
   *
   * @param condition The input string.
   *
   * @return The converted formula object.
   *
   * @throws InvalidFormulaException Thrown if the formula string is invalid.
   */
  public static Condition evaluate(final String condition)
      throws InvalidFormulaException {
    try {
      return new BreakpointCondition(condition, parse(condition));
    } catch (final MaybeNullException e) {
      return new BreakpointCondition(condition, null);
    } catch (final RecognitionException e) {
      throw new InvalidFormulaException();
    }
  }

  /**
   * Parses a breakpoint condition string.
   *
   * @param conditionString The condition string to parse.
   *
   * @return The parsed breakpoint condition tree.
   *
   * @throws RecognitionException Thrown if the condition string could not be parsed.
   * @throws MaybeNullException Thrown if an empty condition string is passed to the function.
   */
  public static ConditionNode parse(final String conditionString) throws RecognitionException,
      MaybeNullException {
    if (conditionString.trim().isEmpty()) {
      throw new MaybeNullException();
    }
    final CharStream charStream = new ANTLRStringStream(conditionString);
    final ConditionLexer lexer = new ConditionLexer(charStream);
    final CommonTokenStream tokens = new CommonTokenStream();
    tokens.setTokenSource(lexer);
    final ConditionParser parser = new ConditionParser(tokens);
    parser.setTreeAdaptor(adaptor);
    try {
      final ConditionParser.prog_return parserResult = parser.prog();
      final CommonTree ast = (CommonTree) parserResult.getTree();

      if (parser.input.index() < parser.input.size()) {
        throw new RecognitionException();
      }
      return convert(ast);
    } catch (final IllegalArgumentException e) {
      throw new RecognitionException();
    }
  }
}
