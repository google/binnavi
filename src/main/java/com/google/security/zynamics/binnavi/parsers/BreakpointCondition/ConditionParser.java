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
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g
// 2009-12-08 13:28:46

package com.google.security.zynamics.binnavi.parsers.BreakpointCondition;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
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
public class ConditionParser extends Parser {
  public static final String[] tokenNames = new String[] {"<invalid>",
      "<EOR>",
      "<DOWN>",
      "<UP>",
      "SUB_EXPRESSION",
      "MEMORY_EXPRESSION",
      "DIGIT",
      "WS",
      "CHARACTER",
      "NUMBER",
      "HEX_NUMBER",
      "IDENTIFIER",
      "EQ_SIGN",
      "GEQ_SIGN",
      "LEQ_SIGN",
      "LT_SIGN",
      "GT_SIGN",
      "NEQ_SIGN",
      "NEQ_SIGN_2",
      "OR",
      "AND",
      "B_OR",
      "B_AND",
      "B_XOR",
      "LSH",
      "RSH",
      "MULT",
      "DIV",
      "MOD",
      "ADD",
      "SUB",
      "'['",
      "']'",
      "'('",
      "')'"};
  public static final int GT_SIGN = 16;
  public static final int LT_SIGN = 15;
  public static final int B_OR = 21;
  public static final int LSH = 24;
  public static final int EQ_SIGN = 12;
  public static final int MOD = 28;
  public static final int NEQ_SIGN = 17;
  public static final int SUB_EXPRESSION = 4;
  public static final int NUMBER = 9;
  public static final int B_XOR = 23;
  public static final int SUB = 30;
  public static final int MULT = 26;
  public static final int AND = 20;
  public static final int EOF = -1;
  public static final int CHARACTER = 8;
  public static final int GEQ_SIGN = 13;
  public static final int T__31 = 31;
  public static final int MEMORY_EXPRESSION = 5;
  public static final int T__32 = 32;
  public static final int T__33 = 33;
  public static final int WS = 7;
  public static final int T__34 = 34;
  public static final int B_AND = 22;
  public static final int RSH = 25;
  public static final int IDENTIFIER = 11;
  public static final int LEQ_SIGN = 14;
  public static final int OR = 19;
  public static final int NEQ_SIGN_2 = 18;
  public static final int DIGIT = 6;
  public static final int DIV = 27;
  public static final int HEX_NUMBER = 10;
  public static final int ADD = 29;

  // delegates
  // delegators


  protected TreeAdaptor adaptor = new CommonTreeAdaptor();
  public static final BitSet FOLLOW_conditionChain_in_prog323 =
      new BitSet(new long[] {0x0000000000000000L});

  public static final BitSet FOLLOW_EOF_in_prog325 = new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_andExpression_in_conditionChain335 =
      new BitSet(new long[] {0x0000000000080002L});
  public static final BitSet FOLLOW_OR_in_conditionChain338 =
      new BitSet(new long[] {0x0000000280000E00L});

  public static final BitSet FOLLOW_andExpression_in_conditionChain341 =
      new BitSet(new long[] {0x0000000000080002L});
  public static final BitSet FOLLOW_condition_in_andExpression356 =
      new BitSet(new long[] {0x0000000000100002L});


  public static final BitSet FOLLOW_AND_in_andExpression359 =
      new BitSet(new long[] {0x0000000280000E00L});


  public static final BitSet FOLLOW_condition_in_andExpression362 =
      new BitSet(new long[] {0x0000000000100002L});;

  public static final BitSet FOLLOW_formula_in_condition376 =
      new BitSet(new long[] {0x000000000007F000L});

  public static final BitSet FOLLOW_set_in_condition378 =
      new BitSet(new long[] {0x0000000280000E00L});;

  public static final BitSet FOLLOW_formula_in_condition395 =
      new BitSet(new long[] {0x0000000000000002L});

  public static final BitSet FOLLOW_bxorExpression_in_formula408 =
      new BitSet(new long[] {0x0000000000200002L});;

  public static final BitSet FOLLOW_B_OR_in_formula411 =
      new BitSet(new long[] {0x0000000280000E00L});

  public static final BitSet FOLLOW_bxorExpression_in_formula414 =
      new BitSet(new long[] {0x0000000000200002L});;

  public static final BitSet FOLLOW_bandExpression_in_bxorExpression431 =
      new BitSet(new long[] {0x0000000000800002L});

  public static final BitSet FOLLOW_B_XOR_in_bxorExpression434 =
      new BitSet(new long[] {0x0000000280000E00L});;

  public static final BitSet FOLLOW_bandExpression_in_bxorExpression437 =
      new BitSet(new long[] {0x0000000000800002L});

  public static final BitSet FOLLOW_shiftExpression_in_bandExpression454 =
      new BitSet(new long[] {0x0000000000400002L});;

  public static final BitSet FOLLOW_B_AND_in_bandExpression457 =
      new BitSet(new long[] {0x0000000280000E00L});

  public static final BitSet FOLLOW_shiftExpression_in_bandExpression460 =
      new BitSet(new long[] {0x0000000000400002L});;

  public static final BitSet FOLLOW_addExpression_in_shiftExpression477 =
      new BitSet(new long[] {0x0000000003000002L});

  public static final BitSet FOLLOW_set_in_shiftExpression480 =
      new BitSet(new long[] {0x0000000280000E00L});;

  public static final BitSet FOLLOW_addExpression_in_shiftExpression487 =
      new BitSet(new long[] {0x0000000003000002L});

  public static final BitSet FOLLOW_multExpression_in_addExpression504 =
      new BitSet(new long[] {0x0000000060000002L});;

  public static final BitSet FOLLOW_set_in_addExpression507 =
      new BitSet(new long[] {0x0000000280000E00L});

  public static final BitSet FOLLOW_multExpression_in_addExpression514 =
      new BitSet(new long[] {0x0000000060000002L});;

  public static final BitSet FOLLOW_primaryExpression_in_multExpression531 =
      new BitSet(new long[] {0x000000001C000002L});

  public static final BitSet FOLLOW_set_in_multExpression534 =
      new BitSet(new long[] {0x0000000280000E00L});;

  public static final BitSet FOLLOW_primaryExpression_in_multExpression543 =
      new BitSet(new long[] {0x000000001C000002L});

  // Delegated rules



  public static final BitSet FOLLOW_IDENTIFIER_in_primaryExpression558 =
      new BitSet(new long[] {0x0000000000000002L});
  public static final BitSet FOLLOW_NUMBER_in_primaryExpression564 =
      new BitSet(new long[] {0x0000000000000002L});
  public static final BitSet FOLLOW_HEX_NUMBER_in_primaryExpression570 =
      new BitSet(new long[] {0x0000000000000002L});
  public static final BitSet FOLLOW_31_in_primaryExpression576 =
      new BitSet(new long[] {0x0000000280000E00L});
  public static final BitSet FOLLOW_formula_in_primaryExpression578 =
      new BitSet(new long[] {0x0000000100000000L});
  public static final BitSet FOLLOW_32_in_primaryExpression580 =
      new BitSet(new long[] {0x0000000000000002L});
  public static final BitSet FOLLOW_33_in_primaryExpression594 =
      new BitSet(new long[] {0x0000000280000E00L});
  public static final BitSet FOLLOW_formula_in_primaryExpression596 =
      new BitSet(new long[] {0x0000000400000000L});
  public static final BitSet FOLLOW_34_in_primaryExpression598 =
      new BitSet(new long[] {0x0000000000000002L});

  public ConditionParser(final TokenStream input) {
    this(input, new RecognizerSharedState());
  }

  public ConditionParser(final TokenStream input, final RecognizerSharedState state) {
    super(input, state);

  }

  // $ANTLR start "addExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:84:1:
  // addExpression : multExpression ( ( ADD | SUB ) multExpression )* ;
  public final ConditionParser.addExpression_return addExpression() throws RecognitionException {
    final ConditionParser.addExpression_return retval = new ConditionParser.addExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token set25 = null;
    ConditionParser.multExpression_return multExpression24 = null;

    ConditionParser.multExpression_return multExpression26 = null;


    final Object set25_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:85:3:
      // ( multExpression ( ( ADD | SUB ) multExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:85:5:
      // multExpression ( ( ADD | SUB ) multExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_multExpression_in_addExpression504);
        multExpression24 = multExpression();

        state._fsp--;

        adaptor.addChild(root_0, multExpression24.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:85:20:
        // ( ( ADD | SUB ) multExpression )*
        loop7: do {
          int alt7 = 2;
          final int LA7_0 = input.LA(1);

          if (((LA7_0 >= ADD && LA7_0 <= SUB))) {
            alt7 = 1;
          }


          switch (alt7) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:85:21:
            // ( ADD | SUB ) multExpression
          {
            set25 = (Token) input.LT(1);
            set25 = (Token) input.LT(1);
            if ((input.LA(1) >= ADD && input.LA(1) <= SUB)) {
              input.consume();
              root_0 = (Object) adaptor.becomeRoot((Object) adaptor.create(set25), root_0);
              state.errorRecovery = false;
            } else {
              final MismatchedSetException mse = new MismatchedSetException(null, input);
              throw mse;
            }

            pushFollow(FOLLOW_multExpression_in_addExpression514);
            multExpression26 = multExpression();

            state._fsp--;

            adaptor.addChild(root_0, multExpression26.getTree());

          }
              break;

            default:
              break loop7;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "addExpression"
  // $ANTLR start "andExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:61:1:
  // andExpression : condition ( AND condition )* ;
  public final ConditionParser.andExpression_return andExpression() throws RecognitionException {
    final ConditionParser.andExpression_return retval = new ConditionParser.andExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token AND7 = null;
    ConditionParser.condition_return condition6 = null;

    ConditionParser.condition_return condition8 = null;


    Object AND7_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:62:3:
      // ( condition ( AND condition )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:62:5:
      // condition ( AND condition )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_condition_in_andExpression356);
        condition6 = condition();

        state._fsp--;

        adaptor.addChild(root_0, condition6.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:62:15:
        // ( AND condition )*
        loop2: do {
          int alt2 = 2;
          final int LA2_0 = input.LA(1);

          if ((LA2_0 == AND)) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:62:16:
            // AND condition
          {
            AND7 = (Token) match(input, AND, FOLLOW_AND_in_andExpression359);
            AND7_tree = (Object) adaptor.create(AND7);
            root_0 = (Object) adaptor.becomeRoot(AND7_tree, root_0);

            pushFollow(FOLLOW_condition_in_andExpression362);
            condition8 = condition();

            state._fsp--;

            adaptor.addChild(root_0, condition8.getTree());

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

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "andExpression"
  // $ANTLR start "bandExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:76:1:
  // bandExpression : shiftExpression ( B_AND shiftExpression )* ;
  public final ConditionParser.bandExpression_return bandExpression() throws RecognitionException {
    final ConditionParser.bandExpression_return retval =
        new ConditionParser.bandExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token B_AND19 = null;
    ConditionParser.shiftExpression_return shiftExpression18 = null;

    ConditionParser.shiftExpression_return shiftExpression20 = null;


    Object B_AND19_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:77:3:
      // ( shiftExpression ( B_AND shiftExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:77:5:
      // shiftExpression ( B_AND shiftExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_shiftExpression_in_bandExpression454);
        shiftExpression18 = shiftExpression();

        state._fsp--;

        adaptor.addChild(root_0, shiftExpression18.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:77:21:
        // ( B_AND shiftExpression )*
        loop5: do {
          int alt5 = 2;
          final int LA5_0 = input.LA(1);

          if ((LA5_0 == B_AND)) {
            alt5 = 1;
          }


          switch (alt5) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:77:22:
            // B_AND shiftExpression
          {
            B_AND19 = (Token) match(input, B_AND, FOLLOW_B_AND_in_bandExpression457);
            B_AND19_tree = (Object) adaptor.create(B_AND19);
            root_0 = (Object) adaptor.becomeRoot(B_AND19_tree, root_0);

            pushFollow(FOLLOW_shiftExpression_in_bandExpression460);
            shiftExpression20 = shiftExpression();

            state._fsp--;

            adaptor.addChild(root_0, shiftExpression20.getTree());

          }
              break;

            default:
              break loop5;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "bandExpression"
  // $ANTLR start "bxorExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:72:1:
  // bxorExpression : bandExpression ( B_XOR bandExpression )* ;
  public final ConditionParser.bxorExpression_return bxorExpression() throws RecognitionException {
    final ConditionParser.bxorExpression_return retval =
        new ConditionParser.bxorExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token B_XOR16 = null;
    ConditionParser.bandExpression_return bandExpression15 = null;

    ConditionParser.bandExpression_return bandExpression17 = null;


    Object B_XOR16_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:73:3:
      // ( bandExpression ( B_XOR bandExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:73:5:
      // bandExpression ( B_XOR bandExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_bandExpression_in_bxorExpression431);
        bandExpression15 = bandExpression();

        state._fsp--;

        adaptor.addChild(root_0, bandExpression15.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:73:20:
        // ( B_XOR bandExpression )*
        loop4: do {
          int alt4 = 2;
          final int LA4_0 = input.LA(1);

          if ((LA4_0 == B_XOR)) {
            alt4 = 1;
          }


          switch (alt4) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:73:21:
            // B_XOR bandExpression
          {
            B_XOR16 = (Token) match(input, B_XOR, FOLLOW_B_XOR_in_bxorExpression434);
            B_XOR16_tree = (Object) adaptor.create(B_XOR16);
            root_0 = (Object) adaptor.becomeRoot(B_XOR16_tree, root_0);

            pushFollow(FOLLOW_bandExpression_in_bxorExpression437);
            bandExpression17 = bandExpression();

            state._fsp--;

            adaptor.addChild(root_0, bandExpression17.getTree());

          }
              break;

            default:
              break loop4;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "bxorExpression"
  // $ANTLR start "condition"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:65:1:
  // condition : formula ( EQ_SIGN | GEQ_SIGN | LEQ_SIGN | LT_SIGN | GT_SIGN | NEQ_SIGN | NEQ_SIGN_2
  // ) formula ;
  public final ConditionParser.condition_return condition() throws RecognitionException {
    final ConditionParser.condition_return retval = new ConditionParser.condition_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token set10 = null;
    ConditionParser.formula_return formula9 = null;

    ConditionParser.formula_return formula11 = null;


    final Object set10_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:65:11:
      // ( formula ( EQ_SIGN | GEQ_SIGN | LEQ_SIGN | LT_SIGN | GT_SIGN | NEQ_SIGN | NEQ_SIGN_2 )
      // formula )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:65:14:
      // formula ( EQ_SIGN | GEQ_SIGN | LEQ_SIGN | LT_SIGN | GT_SIGN | NEQ_SIGN | NEQ_SIGN_2 )
      // formula
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_formula_in_condition376);
        formula9 = formula();

        state._fsp--;

        adaptor.addChild(root_0, formula9.getTree());
        set10 = (Token) input.LT(1);
        set10 = (Token) input.LT(1);
        if ((input.LA(1) >= EQ_SIGN && input.LA(1) <= NEQ_SIGN_2)) {
          input.consume();
          root_0 = (Object) adaptor.becomeRoot((Object) adaptor.create(set10), root_0);
          state.errorRecovery = false;
        } else {
          final MismatchedSetException mse = new MismatchedSetException(null, input);
          throw mse;
        }

        pushFollow(FOLLOW_formula_in_condition395);
        formula11 = formula();

        state._fsp--;

        adaptor.addChild(root_0, formula11.getTree());

      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "condition"
  // $ANTLR start "conditionChain"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:57:1:
  // conditionChain : andExpression ( OR andExpression )* ;
  public final ConditionParser.conditionChain_return conditionChain() throws RecognitionException {
    final ConditionParser.conditionChain_return retval =
        new ConditionParser.conditionChain_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token OR4 = null;
    ConditionParser.andExpression_return andExpression3 = null;

    ConditionParser.andExpression_return andExpression5 = null;


    Object OR4_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:58:3:
      // ( andExpression ( OR andExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:58:5:
      // andExpression ( OR andExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_andExpression_in_conditionChain335);
        andExpression3 = andExpression();

        state._fsp--;

        adaptor.addChild(root_0, andExpression3.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:58:19:
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
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:58:20:
            // OR andExpression
          {
            OR4 = (Token) match(input, OR, FOLLOW_OR_in_conditionChain338);
            OR4_tree = (Object) adaptor.create(OR4);
            root_0 = (Object) adaptor.becomeRoot(OR4_tree, root_0);

            pushFollow(FOLLOW_andExpression_in_conditionChain341);
            andExpression5 = andExpression();

            state._fsp--;

            adaptor.addChild(root_0, andExpression5.getTree());

          }
              break;

            default:
              break loop1;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "conditionChain"
  // $ANTLR start "formula"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:68:1:
  // formula : bxorExpression ( B_OR bxorExpression )* ;
  public final ConditionParser.formula_return formula() throws RecognitionException {
    final ConditionParser.formula_return retval = new ConditionParser.formula_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token B_OR13 = null;
    ConditionParser.bxorExpression_return bxorExpression12 = null;

    ConditionParser.bxorExpression_return bxorExpression14 = null;


    Object B_OR13_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:69:3:
      // ( bxorExpression ( B_OR bxorExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:69:5:
      // bxorExpression ( B_OR bxorExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_bxorExpression_in_formula408);
        bxorExpression12 = bxorExpression();

        state._fsp--;

        adaptor.addChild(root_0, bxorExpression12.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:69:20:
        // ( B_OR bxorExpression )*
        loop3: do {
          int alt3 = 2;
          final int LA3_0 = input.LA(1);

          if ((LA3_0 == B_OR)) {
            alt3 = 1;
          }


          switch (alt3) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:69:21:
            // B_OR bxorExpression
          {
            B_OR13 = (Token) match(input, B_OR, FOLLOW_B_OR_in_formula411);
            B_OR13_tree = (Object) adaptor.create(B_OR13);
            root_0 = (Object) adaptor.becomeRoot(B_OR13_tree, root_0);

            pushFollow(FOLLOW_bxorExpression_in_formula414);
            bxorExpression14 = bxorExpression();

            state._fsp--;

            adaptor.addChild(root_0, bxorExpression14.getTree());

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

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "formula"
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g";
  }

  public String[] getTokenNames() {
    return ConditionParser.tokenNames;
  }

  public TreeAdaptor getTreeAdaptor() {
    return adaptor;
  }

  // $ANTLR start "multExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:88:1:
  // multExpression : primaryExpression ( ( MULT | DIV | MOD ) primaryExpression )* ;
  public final ConditionParser.multExpression_return multExpression() throws RecognitionException {
    final ConditionParser.multExpression_return retval =
        new ConditionParser.multExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token set28 = null;
    ConditionParser.primaryExpression_return primaryExpression27 = null;

    ConditionParser.primaryExpression_return primaryExpression29 = null;


    final Object set28_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:89:3:
      // ( primaryExpression ( ( MULT | DIV | MOD ) primaryExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:89:5:
      // primaryExpression ( ( MULT | DIV | MOD ) primaryExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_primaryExpression_in_multExpression531);
        primaryExpression27 = primaryExpression();

        state._fsp--;

        adaptor.addChild(root_0, primaryExpression27.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:89:23:
        // ( ( MULT | DIV | MOD ) primaryExpression )*
        loop8: do {
          int alt8 = 2;
          final int LA8_0 = input.LA(1);

          if (((LA8_0 >= MULT && LA8_0 <= MOD))) {
            alt8 = 1;
          }


          switch (alt8) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:89:24:
            // ( MULT | DIV | MOD ) primaryExpression
          {
            set28 = (Token) input.LT(1);
            set28 = (Token) input.LT(1);
            if ((input.LA(1) >= MULT && input.LA(1) <= MOD)) {
              input.consume();
              root_0 = (Object) adaptor.becomeRoot((Object) adaptor.create(set28), root_0);
              state.errorRecovery = false;
            } else {
              final MismatchedSetException mse = new MismatchedSetException(null, input);
              throw mse;
            }

            pushFollow(FOLLOW_primaryExpression_in_multExpression543);
            primaryExpression29 = primaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, primaryExpression29.getTree());

          }
              break;

            default:
              break loop8;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "multExpression"
  // $ANTLR start "primaryExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:92:1:
  // primaryExpression : ( IDENTIFIER | NUMBER | HEX_NUMBER | '[' formula ']' -> ^(
  // MEMORY_EXPRESSION formula ) | '(' formula ')' -> ^( SUB_EXPRESSION formula ) );
  public final ConditionParser.primaryExpression_return primaryExpression()
      throws RecognitionException {
    final ConditionParser.primaryExpression_return retval =
        new ConditionParser.primaryExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token IDENTIFIER30 = null;
    Token NUMBER31 = null;
    Token HEX_NUMBER32 = null;
    Token char_literal33 = null;
    Token char_literal35 = null;
    Token char_literal36 = null;
    Token char_literal38 = null;
    ConditionParser.formula_return formula34 = null;

    ConditionParser.formula_return formula37 = null;


    Object IDENTIFIER30_tree = null;
    Object NUMBER31_tree = null;
    Object HEX_NUMBER32_tree = null;
    final Object char_literal33_tree = null;
    final Object char_literal35_tree = null;
    final Object char_literal36_tree = null;
    final Object char_literal38_tree = null;
    final RewriteRuleTokenStream stream_32 = new RewriteRuleTokenStream(adaptor, "token 32");
    final RewriteRuleTokenStream stream_31 = new RewriteRuleTokenStream(adaptor, "token 31");
    final RewriteRuleTokenStream stream_33 = new RewriteRuleTokenStream(adaptor, "token 33");
    final RewriteRuleTokenStream stream_34 = new RewriteRuleTokenStream(adaptor, "token 34");
    final RewriteRuleSubtreeStream stream_formula =
        new RewriteRuleSubtreeStream(adaptor, "rule formula");
    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:93:3:
      // ( IDENTIFIER | NUMBER | HEX_NUMBER | '[' formula ']' -> ^( MEMORY_EXPRESSION formula ) |
      // '(' formula ')' -> ^( SUB_EXPRESSION formula ) )
      int alt9 = 5;
      switch (input.LA(1)) {
        case IDENTIFIER: {
        alt9 = 1;
      }
          break;
        case NUMBER: {
        alt9 = 2;
      }
          break;
        case HEX_NUMBER: {
        alt9 = 3;
      }
          break;
        case 31: {
        alt9 = 4;
      }
          break;
        case 33: {
        alt9 = 5;
      }
          break;
        default:
          final NoViableAltException nvae = new NoViableAltException("", 9, 0, input);

          throw nvae;
      }

      switch (alt9) {
        case 1:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:93:5:
        // IDENTIFIER
      {
        root_0 = (Object) adaptor.nil();

        IDENTIFIER30 = (Token) match(input, IDENTIFIER, FOLLOW_IDENTIFIER_in_primaryExpression558);
        IDENTIFIER30_tree = (Object) adaptor.create(IDENTIFIER30);
        adaptor.addChild(root_0, IDENTIFIER30_tree);


      }
          break;
        case 2:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:94:5:
        // NUMBER
      {
        root_0 = (Object) adaptor.nil();

        NUMBER31 = (Token) match(input, NUMBER, FOLLOW_NUMBER_in_primaryExpression564);
        NUMBER31_tree = (Object) adaptor.create(NUMBER31);
        adaptor.addChild(root_0, NUMBER31_tree);


      }
          break;
        case 3:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:95:5:
        // HEX_NUMBER
      {
        root_0 = (Object) adaptor.nil();

        HEX_NUMBER32 = (Token) match(input, HEX_NUMBER, FOLLOW_HEX_NUMBER_in_primaryExpression570);
        HEX_NUMBER32_tree = (Object) adaptor.create(HEX_NUMBER32);
        adaptor.addChild(root_0, HEX_NUMBER32_tree);


      }
          break;
        case 4:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:96:5:
        // '[' formula ']'
      {
        char_literal33 = (Token) match(input, 31, FOLLOW_31_in_primaryExpression576);
        stream_31.add(char_literal33);

        pushFollow(FOLLOW_formula_in_primaryExpression578);
        formula34 = formula();

        state._fsp--;

        stream_formula.add(formula34.getTree());
        char_literal35 = (Token) match(input, 32, FOLLOW_32_in_primaryExpression580);
        stream_32.add(char_literal35);



        // AST REWRITE
        // elements: formula
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
        // 96:21: -> ^( MEMORY_EXPRESSION formula )
        {
          // C:\\Dokumente und
          // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:96:24:
          // ^( MEMORY_EXPRESSION formula )
          {
            Object root_1 = (Object) adaptor.nil();
            root_1 = (Object) adaptor.becomeRoot(
                (Object) adaptor.create(MEMORY_EXPRESSION, "MEMORY_EXPRESSION"), root_1);

            adaptor.addChild(root_1, stream_formula.nextTree());

            adaptor.addChild(root_0, root_1);
          }

        }

        retval.tree = root_0;
      }
          break;
        case 5:
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:97:5:
        // '(' formula ')'
      {
        char_literal36 = (Token) match(input, 33, FOLLOW_33_in_primaryExpression594);
        stream_33.add(char_literal36);

        pushFollow(FOLLOW_formula_in_primaryExpression596);
        formula37 = formula();

        state._fsp--;

        stream_formula.add(formula37.getTree());
        char_literal38 = (Token) match(input, 34, FOLLOW_34_in_primaryExpression598);
        stream_34.add(char_literal38);



        // AST REWRITE
        // elements: formula
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
        // 97:21: -> ^( SUB_EXPRESSION formula )
        {
          // C:\\Dokumente und
          // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:97:24:
          // ^( SUB_EXPRESSION formula )
          {
            Object root_1 = (Object) adaptor.nil();
            root_1 = (Object) adaptor.becomeRoot(
                (Object) adaptor.create(SUB_EXPRESSION, "SUB_EXPRESSION"), root_1);

            adaptor.addChild(root_1, stream_formula.nextTree());

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

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "primaryExpression"
  // $ANTLR start "prog"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:55:1:
  // prog : conditionChain EOF ;
  public final ConditionParser.prog_return prog() throws RecognitionException {
    final ConditionParser.prog_return retval = new ConditionParser.prog_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token EOF2 = null;
    ConditionParser.conditionChain_return conditionChain1 = null;


    Object EOF2_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:55:7:
      // ( conditionChain EOF )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:55:9:
      // conditionChain EOF
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_conditionChain_in_prog323);
        conditionChain1 = conditionChain();

        state._fsp--;

        adaptor.addChild(root_0, conditionChain1.getTree());
        EOF2 = (Token) match(input, EOF, FOLLOW_EOF_in_prog325);
        EOF2_tree = (Object) adaptor.create(EOF2);
        adaptor.addChild(root_0, EOF2_tree);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "prog"
  @Override
  public void reportError(final RecognitionException e) {
    throw new IllegalArgumentException(e);
  }

  public void setTreeAdaptor(final TreeAdaptor adaptor) {
    this.adaptor = adaptor;
  }

  // $ANTLR start "shiftExpression"
  // C:\\Dokumente und
  // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:80:1:
  // shiftExpression : addExpression ( ( LSH | RSH ) addExpression )* ;
  public final ConditionParser.shiftExpression_return shiftExpression()
      throws RecognitionException {
    final ConditionParser.shiftExpression_return retval =
        new ConditionParser.shiftExpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token set22 = null;
    ConditionParser.addExpression_return addExpression21 = null;

    ConditionParser.addExpression_return addExpression23 = null;


    final Object set22_tree = null;

    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:81:3:
      // ( addExpression ( ( LSH | RSH ) addExpression )* )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:81:5:
      // addExpression ( ( LSH | RSH ) addExpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_addExpression_in_shiftExpression477);
        addExpression21 = addExpression();

        state._fsp--;

        adaptor.addChild(root_0, addExpression21.getTree());
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:81:19:
        // ( ( LSH | RSH ) addExpression )*
        loop6: do {
          int alt6 = 2;
          final int LA6_0 = input.LA(1);

          if (((LA6_0 >= LSH && LA6_0 <= RSH))) {
            alt6 = 1;
          }


          switch (alt6) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:81:20:
            // ( LSH | RSH ) addExpression
          {
            set22 = (Token) input.LT(1);
            set22 = (Token) input.LT(1);
            if ((input.LA(1) >= LSH && input.LA(1) <= RSH)) {
              input.consume();
              root_0 = (Object) adaptor.becomeRoot((Object) adaptor.create(set22), root_0);
              state.errorRecovery = false;
            } else {
              final MismatchedSetException mse = new MismatchedSetException(null, input);
              throw mse;
            }

            pushFollow(FOLLOW_addExpression_in_shiftExpression487);
            addExpression23 = addExpression();

            state._fsp--;

            adaptor.addChild(root_0, addExpression23.getTree());

          }
              break;

            default:
              break loop6;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (final RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }

  // $ANTLR end "shiftExpression"
  public static class addExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class andExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class bandExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class bxorExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class condition_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class conditionChain_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }
  public static class formula_return extends ParserRuleReturnScope {
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
  public static class shiftExpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() {
      return tree;
    }
  }

}
