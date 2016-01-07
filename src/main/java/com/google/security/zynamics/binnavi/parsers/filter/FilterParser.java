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
// $ANTLR 3.1.2 C:\\Dokumente und
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g 2009-04-14
// 09:53:58

package com.google.security.zynamics.binnavi.parsers.filter;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;

@SuppressWarnings("all")
public final class FilterParser extends Parser {
  public static final String[] tokenNames = new String[] {"<invalid>",
      "<EOR>",
      "<DOWN>",
      "<UP>",
      "SUB_EXPRESSION",
      "WS",
      "PREDICATE",
      "AND",
      "OR",
      "'('",
      "')'"};
  public static final int WS = 5;
  public static final int SUB_EXPRESSION = 4;
  public static final int OR = 8;
  public static final int T__10 = 10;
  public static final int AND = 7;
  public static final int EOF = -1;
  public static final int T__9 = 9;
  public static final int PREDICATE = 6;

  // delegates
  // delegators


  protected TreeAdaptor adaptor = new CommonTreeAdaptor();
  public static final BitSet FOLLOW_expression_in_prog130 =
      new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_andExpression_in_expression140 =
      new BitSet(new long[] {0x0000000000000102L});

  public static final BitSet FOLLOW_OR_in_expression143 =
      new BitSet(new long[] {0x0000000000000240L});
  public static final BitSet FOLLOW_andExpression_in_expression146 =
      new BitSet(new long[] {0x0000000000000102L});

  public static final BitSet FOLLOW_primaryExpression_in_andExpression161 =
      new BitSet(new long[] {0x0000000000000082L});
  public static final BitSet FOLLOW_AND_in_andExpression164 =
      new BitSet(new long[] {0x0000000000000240L});


  public static final BitSet FOLLOW_primaryExpression_in_andExpression167 =
      new BitSet(new long[] {0x0000000000000082L});;

  public static final BitSet FOLLOW_PREDICATE_in_primaryExpression183 =
      new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_9_in_primaryExpression189 =
      new BitSet(new long[] {0x0000000000000240L});;

  public static final BitSet FOLLOW_expression_in_primaryExpression191 =
      new BitSet(new long[] {0x0000000000000400L});

  public static final BitSet FOLLOW_10_in_primaryExpression193 =
      new BitSet(new long[] {0x0000000000000002L});;

  public FilterParser(final TokenStream input) {
    this(input, new RecognizerSharedState());
  }

  public FilterParser(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);

  };

  // $ANTLR start "andExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:26:1:
  // andExpression : primaryExpression ( AND primaryExpression )* ;
  public final FilterParser.andExpression_return andExpression() throws RecognitionException {
    final FilterParser.andExpression_return retval = new FilterParser.andExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token AND6 = null;
    FilterParser.primaryExpression_return primaryExpression5 = null;

    FilterParser.primaryExpression_return primaryExpression7 = null;


    Object AND6_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:27:3:
      // ( primaryExpression ( AND primaryExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:27:5:
      // primaryExpression ( AND primaryExpression )*
      {
        root_0 = adaptor.nil();

        pushFollow(FOLLOW_primaryExpression_in_andExpression161);
        primaryExpression5 = primaryExpression();

        state._fsp--;

        adaptor.addChild(root_0, primaryExpression5.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:27:23:
        // ( AND primaryExpression )*
        loop2: do {
          int alt2 = 2;
          final int LA2_0 = input.LA(1);

          if ((LA2_0 == AND)) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:27:24:
            // AND primaryExpression
          {
            AND6 = (Token) match(input, AND, FOLLOW_AND_in_andExpression164);
            AND6_tree = adaptor.create(AND6);
            root_0 = adaptor.becomeRoot(AND6_tree, root_0);

            pushFollow(FOLLOW_primaryExpression_in_andExpression167);
            primaryExpression7 = primaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, primaryExpression7.getTree());

          }
              break;

            default:
              break loop2;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "andExpression"

  // Delegated rules



  // $ANTLR start "expression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:22:1:
  // expression : andExpression ( OR andExpression )* ;
  public final FilterParser.expression_return expression() throws RecognitionException {
    final FilterParser.expression_return retval = new FilterParser.expression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token OR3 = null;
    FilterParser.andExpression_return andExpression2 = null;

    FilterParser.andExpression_return andExpression4 = null;


    Object OR3_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:23:3:
      // ( andExpression ( OR andExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:23:5:
      // andExpression ( OR andExpression )*
      {
        root_0 = adaptor.nil();

        pushFollow(FOLLOW_andExpression_in_expression140);
        andExpression2 = andExpression();

        state._fsp--;

        adaptor.addChild(root_0, andExpression2.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:23:19:
        // ( OR andExpression )*
        loop1: do {
          int alt1 = 2;
          final int LA1_0 = input.LA(1);

          if ((LA1_0 == OR)) {
            alt1 = 1;
          }


          switch (alt1) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:23:20:
            // OR andExpression
          {
            OR3 = (Token) match(input, OR, FOLLOW_OR_in_expression143);
            OR3_tree = adaptor.create(OR3);
            root_0 = adaptor.becomeRoot(OR3_tree, root_0);

            pushFollow(FOLLOW_andExpression_in_expression146);
            andExpression4 = andExpression();

            state._fsp--;

            adaptor.addChild(root_0, andExpression4.getTree());

          }
              break;

            default:
              break loop1;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "expression"
  @Override
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g";
  }

  @Override
  public String[] getTokenNames() {
    return FilterParser.tokenNames;
  }

  public TreeAdaptor getTreeAdaptor() {
    return adaptor;
  }

  // $ANTLR start "primaryExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:30:1:
  // primaryExpression : ( PREDICATE | '(' expression ')' -> ^( SUB_EXPRESSION expression ) );
  public final FilterParser.primaryExpression_return primaryExpression()
      throws RecognitionException {
    final FilterParser.primaryExpression_return retval =
        new FilterParser.primaryExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token PREDICATE8 = null;
    Token char_literal9 = null;
    Token char_literal11 = null;
    FilterParser.expression_return expression10 = null;


    Object PREDICATE8_tree = null;
    final Object char_literal9_tree = null;
    final Object char_literal11_tree = null;
    final RewriteRuleTokenStream stream_10 = new RewriteRuleTokenStream(adaptor, "token 10");
    final RewriteRuleTokenStream stream_9 = new RewriteRuleTokenStream(adaptor, "token 9");
    final RewriteRuleSubtreeStream stream_expression =
        new RewriteRuleSubtreeStream(adaptor, "rule expression");
    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:31:3:
      // ( PREDICATE | '(' expression ')' -> ^( SUB_EXPRESSION expression ) )
      int alt3 = 2;
      final int LA3_0 = input.LA(1);

      if ((LA3_0 == PREDICATE)) {
        alt3 = 1;
      } else if ((LA3_0 == 9)) {
        alt3 = 2;
      } else {
        final NoViableAltException nvae = new NoViableAltException("", 3, 0, input);

        throw nvae;
      }
      switch (alt3) {
        case 1:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:31:5:
        // PREDICATE
      {
        root_0 = adaptor.nil();

        PREDICATE8 = (Token) match(input, PREDICATE, FOLLOW_PREDICATE_in_primaryExpression183);
        PREDICATE8_tree = adaptor.create(PREDICATE8);
        adaptor.addChild(root_0, PREDICATE8_tree);


      }
          break;
        case 2:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:32:5:
        // '(' expression ')'
      {
        char_literal9 = (Token) match(input, 9, FOLLOW_9_in_primaryExpression189);
        stream_9.add(char_literal9);

        pushFollow(FOLLOW_expression_in_primaryExpression191);
        expression10 = expression();

        state._fsp--;

        stream_expression.add(expression10.getTree());
        char_literal11 = (Token) match(input, 10, FOLLOW_10_in_primaryExpression193);
        stream_10.add(char_literal11);



        // AST REWRITE
        // elements: expression
        // token labels:
        // rule labels: retval
        // token list labels:
        // rule list labels:
        // wildcard labels:
        retval.tree = root_0;
        final RewriteRuleSubtreeStream stream_retval =
            new RewriteRuleSubtreeStream(adaptor, "rule retval", retval != null
                ? retval.tree : null);

        root_0 = adaptor.nil();
        // 32:24: -> ^( SUB_EXPRESSION expression )
        {
          // C:\\Dokumente und
          // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:32:27:
          // ^( SUB_EXPRESSION expression )
          {
            Object root_1 = adaptor.nil();
            root_1 = adaptor.becomeRoot(adaptor.create(SUB_EXPRESSION, "SUB_EXPRESSION"), root_1);

            adaptor.addChild(root_1, stream_expression.nextTree());

            adaptor.addChild(root_0, root_1);
          }

        }

        retval.tree = root_0;
      }
          break;

      }
      retval.stop = input.LT(-1);

      retval.tree = adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "primaryExpression"
  // $ANTLR start "prog"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:20:1: prog
  // : expression ;
  public final FilterParser.prog_return prog() throws RecognitionException {
    final FilterParser.prog_return retval = new FilterParser.prog_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    FilterParser.expression_return expression1 = null;



    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:20:7:
      // ( expression )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:20:9:
      // expression
      {
        root_0 = adaptor.nil();

        pushFollow(FOLLOW_expression_in_prog130);
        expression1 = expression();

        state._fsp--;

        adaptor.addChild(root_0, expression1.getTree());

      }

      retval.stop = input.LT(-1);

      retval.tree = adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "prog"
  public void setTreeAdaptor(final TreeAdaptor adaptor) {
    this.adaptor = adaptor;
  }

  public static class andExpression_return extends ParserRuleReturnScope {
    Object tree;

    @Override
    public Object getTree() {
      return tree;
    }
  }
  public static class expression_return extends ParserRuleReturnScope {
    Object tree;

    @Override
    public Object getTree() {
      return tree;
    }
  }
  public static class primaryExpression_return extends ParserRuleReturnScope {
    Object tree;

    @Override
    public Object getTree() {
      return tree;
    }
  }
  public static class prog_return extends ParserRuleReturnScope {
    Object tree;

    @Override
    public Object getTree() {
      return tree;
    }
  }

}
