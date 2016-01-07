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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CAbstractAndExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CAbstractOrExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CPredicateExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.IAbstractNode;
import com.google.security.zynamics.binnavi.parsers.filter.FilterLexer;
import com.google.security.zynamics.binnavi.parsers.filter.FilterParser;


/**
 * Parses filter strings.
 */
public final class CFilterRuleParser {
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
  private CFilterRuleParser() {
  }

  /**
   * Converts an ANTLR AST into a filter AST.
   * 
   * @param ast The ANTRL AST to convert.
   * 
   * @return The converted AST.
   * 
   * @throws RecognitionException Thrown if the AST could not be converted.
   */
  private static IAbstractNode convert(final CommonTree ast) throws RecognitionException {
    if (ast.getType() == FilterParser.PREDICATE) {
      return new CPredicateExpression(ast.getText());
    } else if (ast.getType() == FilterParser.AND) {
      return convertAnd(ast);
    } else if (ast.getType() == FilterParser.OR) {
      return convertOr(ast);
    } else if (ast.getType() == 0) {
      throw new RecognitionException();
    } else if (ast.getType() == FilterParser.SUB_EXPRESSION) {
      return convert((CommonTree) ast.getChild(0));
    }

    throw new IllegalStateException("IE00960: Not yet implemented (" + ast.getType() + ")");
  }

  /**
   * Converts an ANTRL AND AST into a filter AND AST.
   * 
   * @param ast The AST to convert.
   * 
   * @return The converted AST.
   * 
   * @throws RecognitionException Thrown if the AST could not be converted.
   */
  private static IAbstractNode convertAnd(final CommonTree ast) throws RecognitionException {
    final List<IAbstractNode> children = new ArrayList<IAbstractNode>();

    for (final Object childObject : ast.getChildren()) {
      children.add(convert((CommonTree) childObject));
    }

    return new CAbstractAndExpression(children);
  }

  /**
   * Converts an ANTRL OR AST into a filter OR AST.
   * 
   * @param ast The AST to convert.
   * 
   * @return The converted AST.
   * 
   * @throws RecognitionException Thrown if the AST could not be converted.
   */
  private static IAbstractNode convertOr(final CommonTree ast) throws RecognitionException {
    final List<IAbstractNode> children = new ArrayList<IAbstractNode>();

    for (final Object childObject : ast.getChildren()) {
      children.add(convert((CommonTree) childObject));
    }

    return new CAbstractOrExpression(children);
  }

  /**
   * Parses a filter string.
   * 
   * @param filterString The filter string to parse.
   * 
   * @return The root node of the parsed AST.
   * 
   * @throws RecognitionException Thrown if parsing the filter string failed.
   */
  public static IAbstractNode parse(final String filterString) throws RecognitionException {
    final CharStream charStream = new ANTLRStringStream(filterString);

    final FilterLexer lexer = new FilterLexer(charStream);

    final CommonTokenStream tokens = new CommonTokenStream();
    tokens.setTokenSource(lexer);

    final FilterParser parser = new FilterParser(tokens);

    parser.setTreeAdaptor(adaptor);

    final FilterParser.prog_return parserResult = parser.prog();
    final CommonTree ast = (CommonTree) parserResult.getTree();

    return convert(ast);
  }
}
