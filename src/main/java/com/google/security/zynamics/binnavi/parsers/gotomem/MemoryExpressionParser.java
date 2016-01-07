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
// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Dokumente und
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g
// 2009-10-19 09:57:01

package com.google.security.zynamics.binnavi.parsers.gotomem;

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
public class MemoryExpressionParser extends Parser {
  public static final String[] tokenNames = new String[] {"<invalid>",
      "<EOR>",
      "<DOWN>",
      "<UP>",
      "HEXADECIMAL_NUMBER",
      "DECIMAL_NUMBER",
      "ADD_EXPRESSION",
      "MEM_EXPRESSION",
      "SUB_EXPRESSION",
      "DIGIT",
      "WS",
      "CHARACTER",
      "NUMBER",
      "HEX_NUMBER",
      "REGISTER",
      "OPERAND_PLUS",
      "OPERAND_MINUS",
      "OPERAND_MULT",
      "'['",
      "']'",
      "'('",
      "')'"};
  public static final int OPERAND_MULT = 17;
  public static final int OPERAND_MINUS = 16;
  public static final int T__21 = 21;
  public static final int T__20 = 20;
  public static final int SUB_EXPRESSION = 8;
  public static final int NUMBER = 12;
  public static final int OPERAND_PLUS = 15;
  public static final int ADD_EXPRESSION = 6;
  public static final int HEXADECIMAL_NUMBER = 4;
  public static final int EOF = -1;
  public static final int CHARACTER = 11;
  public static final int T__19 = 19;
  public static final int WS = 10;
  public static final int T__18 = 18;
  public static final int DIGIT = 9;
  public static final int HEX_NUMBER = 13;
  public static final int MEM_EXPRESSION = 7;
  public static final int REGISTER = 14;
  public static final int DECIMAL_NUMBER = 5;

  // delegates
  // delegators


  protected TreeAdaptor adaptor = new CommonTreeAdaptor();
  public static final BitSet FOLLOW_expression_in_prog187 =
      new BitSet(new long[] {0x0000000000000000L});

  public static final BitSet FOLLOW_EOF_in_prog189 = new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_addExpression_in_expression199 =
      new BitSet(new long[] {0x0000000000000002L});
  public static final BitSet FOLLOW_multExpression_in_addExpression211 =
      new BitSet(new long[] {0x0000000000018002L});

  public static final BitSet FOLLOW_OPERAND_PLUS_in_addExpression215 =
      new BitSet(new long[] {0x0000000000147000L});
  public static final BitSet FOLLOW_OPERAND_MINUS_in_addExpression220 =
      new BitSet(new long[] {0x0000000000147000L});


  public static final BitSet FOLLOW_multExpression_in_addExpression224 =
      new BitSet(new long[] {0x0000000000018002L});;

  public static final BitSet FOLLOW_primaryExpression_in_multExpression239 =
      new BitSet(new long[] {0x0000000000020002L});

  public static final BitSet FOLLOW_OPERAND_MULT_in_multExpression242 =
      new BitSet(new long[] {0x0000000000147000L});;

  public static final BitSet FOLLOW_primaryExpression_in_multExpression245 =
      new BitSet(new long[] {0x0000000000020002L});

  public static final BitSet FOLLOW_REGISTER_in_primaryExpression260 =
      new BitSet(new long[] {0x0000000000000002L});;

  public static final BitSet FOLLOW_NUMBER_in_primaryExpression266 =
      new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_HEX_NUMBER_in_primaryExpression272 =
      new BitSet(new long[] {0x0000000000000002L});;

  public static final BitSet FOLLOW_18_in_primaryExpression278 =
      new BitSet(new long[] {0x0000000000147000L});

  public static final BitSet FOLLOW_expression_in_primaryExpression280 =
      new BitSet(new long[] {0x0000000000080000L});;

  public static final BitSet FOLLOW_19_in_primaryExpression282 =
      new BitSet(new long[] {0x0000000000000002L});

  // Delegated rules



  public static final BitSet FOLLOW_20_in_primaryExpression296 =
      new BitSet(new long[] {0x0000000000147000L});
  public static final BitSet FOLLOW_expression_in_primaryExpression298 =
      new BitSet(new long[] {0x0000000000200000L});
  public static final BitSet FOLLOW_21_in_primaryExpression300 =
      new BitSet(new long[] {0x0000000000000002L});

  public MemoryExpressionParser(final TokenStream input) {
    this(input, new RecognizerSharedState());
  }

  public MemoryExpressionParser(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);

  }

  // $ANTLR start "addExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:38:1:
  // addExpression : multExpression ( ( OPERAND_PLUS | OPERAND_MINUS ) multExpression )* ;
  public final MemoryExpressionParser.addExpression_return addExpression()
      throws RecognitionException {
    final MemoryExpressionParser.addExpression_return retval =
        new MemoryExpressionParser.addExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token OPERAND_PLUS5 = null;
    Token OPERAND_MINUS6 = null;
    MemoryExpressionParser.multExpression_return multExpression4 = null;

    MemoryExpressionParser.multExpression_return multExpression7 = null;


    Object OPERAND_PLUS5_tree = null;
    Object OPERAND_MINUS6_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:3:
      // ( multExpression ( ( OPERAND_PLUS | OPERAND_MINUS ) multExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:5:
      // multExpression ( ( OPERAND_PLUS | OPERAND_MINUS ) multExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_multExpression_in_addExpression211);
        multExpression4 = multExpression();

        state._fsp--;

        adaptor.addChild(root_0, multExpression4.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:20:
        // ( ( OPERAND_PLUS | OPERAND_MINUS ) multExpression )*
        loop2: do {
          int alt2 = 2;
          final int LA2_0 = input.LA(1);

          if (((LA2_0 >= OPERAND_PLUS && LA2_0 <= OPERAND_MINUS))) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:21:
            // ( OPERAND_PLUS | OPERAND_MINUS ) multExpression
          {
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:21:
            // ( OPERAND_PLUS | OPERAND_MINUS )
            int alt1 = 2;
            final int LA1_0 = input.LA(1);

            if ((LA1_0 == OPERAND_PLUS)) {
              alt1 = 1;
            } else if ((LA1_0 == OPERAND_MINUS)) {
              alt1 = 2;
            } else {
              final NoViableAltException nvae = new NoViableAltException("", 1, 0, input);

              throw nvae;
            }
            switch (alt1) {
              case 1:
              // C:\\Dokumente und
              // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:22:
              // OPERAND_PLUS
            {
              OPERAND_PLUS5 =
                  (Token) match(input, OPERAND_PLUS, FOLLOW_OPERAND_PLUS_in_addExpression215);
              OPERAND_PLUS5_tree = (Object) adaptor.create(OPERAND_PLUS5);
              root_0 = (Object) adaptor.becomeRoot(OPERAND_PLUS5_tree, root_0);


            }
                break;
              case 2:
              // C:\\Dokumente und
              // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:39:38:
              // OPERAND_MINUS
            {
              OPERAND_MINUS6 =
                  (Token) match(input, OPERAND_MINUS, FOLLOW_OPERAND_MINUS_in_addExpression220);
              OPERAND_MINUS6_tree = (Object) adaptor.create(OPERAND_MINUS6);
              root_0 = (Object) adaptor.becomeRoot(OPERAND_MINUS6_tree, root_0);


            }
                break;

            }

            pushFollow(FOLLOW_multExpression_in_addExpression224);
            multExpression7 = multExpression();

            state._fsp--;

            adaptor.addChild(root_0, multExpression7.getTree());

          }
              break;

            default:
              break loop2;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException e) {
      throw e;
    } finally {
    }
    return retval;
  }

  // $ANTLR end "addExpression"
  // $ANTLR start "expression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:35:1:
  // expression : addExpression ;
  public final MemoryExpressionParser.expression_return expression() throws RecognitionException {
    final MemoryExpressionParser.expression_return retval =
        new MemoryExpressionParser.expression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    MemoryExpressionParser.addExpression_return addExpression3 = null;



    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:36:3:
      // ( addExpression )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:36:5:
      // addExpression
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_addExpression_in_expression199);
        addExpression3 = addExpression();

        state._fsp--;

        adaptor.addChild(root_0, addExpression3.getTree());

      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException e) {
      throw e;
    } finally {
    }
    return retval;
  }

  // $ANTLR end "expression"
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g";
  }

  public String[] getTokenNames() {
    return MemoryExpressionParser.tokenNames;
  }

  public TreeAdaptor getTreeAdaptor() {
    return adaptor;
  }

  // $ANTLR start "multExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:42:1:
  // multExpression : primaryExpression ( OPERAND_MULT primaryExpression )* ;
  public final MemoryExpressionParser.multExpression_return multExpression()
      throws RecognitionException {
    final MemoryExpressionParser.multExpression_return retval =
        new MemoryExpressionParser.multExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token OPERAND_MULT9 = null;
    MemoryExpressionParser.primaryExpression_return primaryExpression8 = null;

    MemoryExpressionParser.primaryExpression_return primaryExpression10 = null;


    Object OPERAND_MULT9_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:43:3:
      // ( primaryExpression ( OPERAND_MULT primaryExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:43:5:
      // primaryExpression ( OPERAND_MULT primaryExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_primaryExpression_in_multExpression239);
        primaryExpression8 = primaryExpression();

        state._fsp--;

        adaptor.addChild(root_0, primaryExpression8.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:43:23:
        // ( OPERAND_MULT primaryExpression )*
        loop3: do {
          int alt3 = 2;
          final int LA3_0 = input.LA(1);

          if ((LA3_0 == OPERAND_MULT)) {
            alt3 = 1;
          }


          switch (alt3) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:43:24:
            // OPERAND_MULT primaryExpression
          {
            OPERAND_MULT9 =
                (Token) match(input, OPERAND_MULT, FOLLOW_OPERAND_MULT_in_multExpression242);
            OPERAND_MULT9_tree = (Object) adaptor.create(OPERAND_MULT9);
            root_0 = (Object) adaptor.becomeRoot(OPERAND_MULT9_tree, root_0);

            pushFollow(FOLLOW_primaryExpression_in_multExpression245);
            primaryExpression10 = primaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, primaryExpression10.getTree());

          }
              break;

            default:
              break loop3;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException e) {
      throw e;
    } finally {
    }
    return retval;
  }

  // $ANTLR end "multExpression"
  // $ANTLR start "primaryExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:46:1:
  // primaryExpression : ( REGISTER | NUMBER | HEX_NUMBER | '[' expression ']' -> ^( MEM_EXPRESSION
  // expression ) | '(' expression ')' -> ^( SUB_EXPRESSION expression ) );
  public final MemoryExpressionParser.primaryExpression_return primaryExpression()
      throws RecognitionException {
    final MemoryExpressionParser.primaryExpression_return retval =
        new MemoryExpressionParser.primaryExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token REGISTER11 = null;
    Token NUMBER12 = null;
    Token HEX_NUMBER13 = null;
    Token char_literal14 = null;
    Token char_literal16 = null;
    Token char_literal17 = null;
    Token char_literal19 = null;
    MemoryExpressionParser.expression_return expression15 = null;

    MemoryExpressionParser.expression_return expression18 = null;


    Object REGISTER11_tree = null;
    Object NUMBER12_tree = null;
    Object HEX_NUMBER13_tree = null;
    final Object char_literal14_tree = null;
    final Object char_literal16_tree = null;
    final Object char_literal17_tree = null;
    final Object char_literal19_tree = null;
    final RewriteRuleTokenStream stream_21 = new RewriteRuleTokenStream(adaptor, "token 21");
    final RewriteRuleTokenStream stream_20 = new RewriteRuleTokenStream(adaptor, "token 20");
    final RewriteRuleTokenStream stream_19 = new RewriteRuleTokenStream(adaptor, "token 19");
    final RewriteRuleTokenStream stream_18 = new RewriteRuleTokenStream(adaptor, "token 18");
    final RewriteRuleSubtreeStream stream_expression =
        new RewriteRuleSubtreeStream(adaptor, "rule expression");
    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:47:3:
      // ( REGISTER | NUMBER | HEX_NUMBER | '[' expression ']' -> ^( MEM_EXPRESSION expression ) |
      // '(' expression ')' -> ^( SUB_EXPRESSION expression ) )
      int alt4 = 5;
      switch (input.LA(1)) {
        case REGISTER: {
        alt4 = 1;
      }
          break;
        case NUMBER: {
        alt4 = 2;
      }
          break;
        case HEX_NUMBER: {
        alt4 = 3;
      }
          break;
        case 18: {
        alt4 = 4;
      }
          break;
        case 20: {
        alt4 = 5;
      }
          break;
        default:
          final NoViableAltException nvae = new NoViableAltException("", 4, 0, input);

          throw nvae;
      }

      switch (alt4) {
        case 1:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:47:5:
        // REGISTER
      {
        root_0 = (Object) adaptor.nil();

        REGISTER11 = (Token) match(input, REGISTER, FOLLOW_REGISTER_in_primaryExpression260);
        REGISTER11_tree = (Object) adaptor.create(REGISTER11);
        adaptor.addChild(root_0, REGISTER11_tree);


      }
          break;
        case 2:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:48:5:
        // NUMBER
      {
        root_0 = (Object) adaptor.nil();

        NUMBER12 = (Token) match(input, NUMBER, FOLLOW_NUMBER_in_primaryExpression266);
        NUMBER12_tree = (Object) adaptor.create(NUMBER12);
        adaptor.addChild(root_0, NUMBER12_tree);


      }
          break;
        case 3:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:49:5:
        // HEX_NUMBER
      {
        root_0 = (Object) adaptor.nil();

        HEX_NUMBER13 = (Token) match(input, HEX_NUMBER, FOLLOW_HEX_NUMBER_in_primaryExpression272);
        HEX_NUMBER13_tree = (Object) adaptor.create(HEX_NUMBER13);
        adaptor.addChild(root_0, HEX_NUMBER13_tree);


      }
          break;
        case 4:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:50:5:
        // '[' expression ']'
      {
        char_literal14 = (Token) match(input, 18, FOLLOW_18_in_primaryExpression278);
        stream_18.add(char_literal14);

        pushFollow(FOLLOW_expression_in_primaryExpression280);
        expression15 = expression();

        state._fsp--;

        stream_expression.add(expression15.getTree());
        char_literal16 = (Token) match(input, 19, FOLLOW_19_in_primaryExpression282);
        stream_19.add(char_literal16);



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

        root_0 = (Object) adaptor.nil();
        // 50:24: -> ^( MEM_EXPRESSION expression )
        {
          // C:\\Dokumente und
          // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:50:27:
          // ^( MEM_EXPRESSION expression )
          {
            Object root_1 = (Object) adaptor.nil();
            root_1 = (Object) adaptor.becomeRoot(
                (Object) adaptor.create(MEM_EXPRESSION, "MEM_EXPRESSION"), root_1);

            adaptor.addChild(root_1, stream_expression.nextTree());

            adaptor.addChild(root_0, root_1);
          }

        }

        retval.tree = root_0;
      }
          break;
        case 5:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:51:5:
        // '(' expression ')'
      {
        char_literal17 = (Token) match(input, 20, FOLLOW_20_in_primaryExpression296);
        stream_20.add(char_literal17);

        pushFollow(FOLLOW_expression_in_primaryExpression298);
        expression18 = expression();

        state._fsp--;

        stream_expression.add(expression18.getTree());
        char_literal19 = (Token) match(input, 21, FOLLOW_21_in_primaryExpression300);
        stream_21.add(char_literal19);



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

        root_0 = (Object) adaptor.nil();
        // 51:24: -> ^( SUB_EXPRESSION expression )
        {
          // C:\\Dokumente und
          // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:51:27:
          // ^( SUB_EXPRESSION expression )
          {
            Object root_1 = (Object) adaptor.nil();
            root_1 = (Object) adaptor.becomeRoot(
                (Object) adaptor.create(SUB_EXPRESSION, "SUB_EXPRESSION"), root_1);

            adaptor.addChild(root_1, stream_expression.nextTree());

            adaptor.addChild(root_0, root_1);
          }

        }

        retval.tree = root_0;
      }
          break;

      }
      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException e) {
      throw e;
    } finally {
    }
    return retval;
  }

  // $ANTLR end "primaryExpression"
  // $ANTLR start "prog"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:33:1:
  // prog : expression EOF ;
  public final MemoryExpressionParser.prog_return prog() throws RecognitionException {
    final MemoryExpressionParser.prog_return retval = new MemoryExpressionParser.prog_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token EOF2 = null;
    MemoryExpressionParser.expression_return expression1 = null;


    Object EOF2_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:33:7:
      // ( expression EOF )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:33:9:
      // expression EOF
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_expression_in_prog187);
        expression1 = expression();

        state._fsp--;

        adaptor.addChild(root_0, expression1.getTree());
        EOF2 = (Token) match(input, EOF, FOLLOW_EOF_in_prog189);
        EOF2_tree = (Object) adaptor.create(EOF2);
        adaptor.addChild(root_0, EOF2_tree);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException e) {
      throw e;
    } finally {
    }
    return retval;
  }

  // $ANTLR end "prog"
  public void setTreeAdaptor(final TreeAdaptor adaptor) {
    this.adaptor = adaptor;
  }

  public static class addExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class expression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class multExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class primaryExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class prog_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }

}
