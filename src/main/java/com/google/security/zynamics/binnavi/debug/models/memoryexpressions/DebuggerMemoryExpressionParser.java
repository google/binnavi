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
package com.google.security.zynamics.binnavi.debug.models.memoryexpressions;

import com.google.security.zynamics.binnavi.parsers.gotomem.MemoryExpressionLexer;
import com.google.security.zynamics.binnavi.parsers.gotomem.MemoryExpressionParser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses memory expression strings into syntax trees.
 */
public final class DebuggerMemoryExpressionParser {
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
  private DebuggerMemoryExpressionParser() {}

  /**
   * Converts an ANTRL tree into a memory expression tree.
   *
   * @param ast The ANTLR tree.
   *
   * @return The converted memory tree.
   */
  private static MemoryExpressionElement convert(final CommonTree ast) {
    if (ast.getType() == MemoryExpressionParser.REGISTER) {
      return new Register(ast.getText());
    } else if (ast.getType() == MemoryExpressionParser.NUMBER) {
      return new NumericalValue(new BigInteger(ast.getText()));
    } else if (ast.getType() == MemoryExpressionParser.HEX_NUMBER) {
      return new NumericalValue(new BigInteger(ast.getText().substring(2), 16));
    } else if (ast.getType() == MemoryExpressionParser.MEM_EXPRESSION) {
      return new MemoryExpression(convert((CommonTree) ast.getChild(0)));
    } else if (ast.getType() == MemoryExpressionParser.OPERAND_PLUS) {
      return new PlusExpression(convertChildren(ast));
    } else if (ast.getType() == MemoryExpressionParser.OPERAND_MINUS) {
      return new MinusExpression(convertChildren(ast));
    } else if (ast.getType() == MemoryExpressionParser.OPERAND_MULT) {
      return new MultiplicationExpression(convertChildren(ast));
    } else if (ast.getType() == MemoryExpressionParser.SUB_EXPRESSION) {
      return new SubExpression(convert((CommonTree) ast.getChild(0)));
    } else if (ast.getType() == 0) {
      return convert((CommonTree) ast.getChild(0));
    }

    throw new IllegalStateException("IE00360: Not yet implemented (" + ast.getType() + ")");
  }

  private static List<MemoryExpressionElement> convertChildren(final CommonTree ast) {
    final List<MemoryExpressionElement> children = new ArrayList<>();
    for (final Object childObject : ast.getChildren()) {
      children.add(convert((CommonTree) childObject));
    }
    return children;
  }

  /**
   * Parses a single memory expression string.
   *
   * @param memoryExpression The memory expression string to parse.
   *
   * @return The parsed memory expression tree.
   *
   * @throws RecognitionException Thrown if parsing failed.
   */
  public static MemoryExpressionElement parse(final String memoryExpression)
      throws RecognitionException {
    final CharStream charStream = new ANTLRStringStream(memoryExpression);
    final MemoryExpressionLexer lexer = new MemoryExpressionLexer(charStream);
    final CommonTokenStream tokens = new CommonTokenStream();
    tokens.setTokenSource(lexer);
    final MemoryExpressionParser parser = new MemoryExpressionParser(tokens) {
      @Override
      public void recover(final IntStream input, final RecognitionException exception) {
        // Nothing to do
      }

      @Override
      public Object recoverFromMismatchedToken(final IntStream input, final int ttype,
          final org.antlr.runtime.BitSet follow) throws RecognitionException {
        throw new MismatchedTokenException(ttype, input);
      }

      @Override
      public void reportError(final RecognitionException exception) {
        // Nothing to do
      }
    };
    parser.setTreeAdaptor(adaptor);
    final MemoryExpressionParser.prog_return parserResult = parser.prog();
    final CommonTree ast = (CommonTree) parserResult.getTree();
    return convert(ast);
  }
}
