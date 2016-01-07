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
package com.google.security.zynamics.binnavi.parsers.BreakpointCondition;

// $ANTLR 3.1.2 C:\\Dokumente und
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g
// 2009-12-08 13:28:46

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings("all")
public class ConditionLexer extends Lexer {
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
  public static final int MEMORY_EXPRESSION = 5;
  public static final int T__31 = 31;
  public static final int T__32 = 32;
  public static final int WS = 7;
  public static final int T__33 = 33;
  public static final int T__34 = 34;
  public static final int B_AND = 22;
  public static final int LEQ_SIGN = 14;
  public static final int IDENTIFIER = 11;
  public static final int RSH = 25;
  public static final int NEQ_SIGN_2 = 18;
  public static final int OR = 19;
  public static final int DIGIT = 6;
  public static final int DIV = 27;
  public static final int HEX_NUMBER = 10;
  public static final int ADD = 29;

  protected DFA4 dfa4 = new DFA4(this);


  // delegates
  // delegators

  static final String DFA4_eotS = "\6\uffff\1\25\1\10\2\uffff\1\32\1\36\1\uffff\1\40\1\42\24\uffff";
  static final String DFA4_eofS = "\43\uffff";
  static final String DFA4_minS =
      "\1\11\5\uffff\1\60\1\170\2\uffff\1\75\1\74\1\uffff\1\174\1\46\24" + "\uffff";
  static final String DFA4_maxS =
      "\1\174\5\uffff\1\172\1\170\2\uffff\2\76\1\uffff\1\174\1\46\24\uffff";

  // $ANTLR start "T__31"
  static final String DFA4_acceptS =
      "\1\uffff\1\1\1\2\1\3\1\4\1\5\2\uffff\1\7\1\12\2\uffff\1\17\2\uffff"
          + "\1\25\1\30\1\31\1\32\1\33\1\34\1\6\1\11\1\10\1\13\1\27\1\16\1\14"
          + "\1\20\1\26\1\15\1\21\1\23\1\22\1\24";
  // $ANTLR end "T__31"

  // $ANTLR start "T__32"
  static final String DFA4_specialS = "\43\uffff}>";
  // $ANTLR end "T__32"

  // $ANTLR start "T__33"
  static final String[] DFA4_transitionS = {
      "\2\5\1\uffff\2\5\22\uffff\1\5\1\14\3\uffff\1\22\1\16\1\uffff"
          + "\1\3\1\4\1\20\1\23\1\uffff\1\24\1\uffff\1\21\1\7\11\10\2\uffff"
          + "\1\13\1\11\1\12\2\uffff\32\6\1\1\1\uffff\1\2\1\17\2\uffff\32" + "\6\1\uffff\1\15", "",
      "", "", "", "", "\12\26\7\uffff\32\26\6\uffff\32\26", "\1\27", "", "", "\1\30\1\31",
      "\1\35\1\33\1\34", "", "\1\37", "\1\41", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", ""};
  // $ANTLR end "T__33"

  // $ANTLR start "T__34"
  static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
  // $ANTLR end "T__34"

  // $ANTLR start "DIGIT"
  static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
  // $ANTLR end "DIGIT"

  // $ANTLR start "WS"
  static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
  // $ANTLR end "WS"

  // $ANTLR start "CHARACTER"
  static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
  // $ANTLR end "CHARACTER"

  // $ANTLR start "NUMBER"
  static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
  // $ANTLR end "NUMBER"

  // $ANTLR start "HEX_NUMBER"
  static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
  // $ANTLR end "HEX_NUMBER"

  // $ANTLR start "IDENTIFIER"
  static final short[][] DFA4_transition;
  // $ANTLR end "IDENTIFIER"

  // $ANTLR start "EQ_SIGN"
  static {
    final int numStates = DFA4_transitionS.length;
    DFA4_transition = new short[numStates][];
    for (int i = 0; i < numStates; i++) {
      DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
    }
  }

  // $ANTLR end "EQ_SIGN"

  // $ANTLR start "GEQ_SIGN"
  public ConditionLexer() {
  }

  // $ANTLR end "GEQ_SIGN"

  // $ANTLR start "LEQ_SIGN"
  public ConditionLexer(final CharStream input) {
    this(input, new RecognizerSharedState());
  }

  // $ANTLR end "LEQ_SIGN"

  // $ANTLR start "LT_SIGN"
  public ConditionLexer(final CharStream input, final RecognizerSharedState state) {
    super(input, state);

  }

  // $ANTLR end "LT_SIGN"

  // $ANTLR start "GT_SIGN"
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g";
  }

  // $ANTLR end "GT_SIGN"

  // $ANTLR start "NEQ_SIGN"
  public final void mADD() throws RecognitionException {
    try {
      final int _type = ADD;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:52:6:
      // ( '+' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:52:8:
      // '+'
      {
        match('+');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "NEQ_SIGN"

  // $ANTLR start "NEQ_SIGN_2"
  public final void mAND() throws RecognitionException {
    try {
      final int _type = AND;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:43:6:
      // ( '&&' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:43:8:
      // '&&'
      {
        match("&&");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "NEQ_SIGN_2"

  // $ANTLR start "OR"
  public final void mB_AND() throws RecognitionException {
    try {
      final int _type = B_AND;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:45:8:
      // ( '&' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:45:10:
      // '&'
      {
        match('&');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "OR"

  // $ANTLR start "AND"
  public final void mB_OR() throws RecognitionException {
    try {
      final int _type = B_OR;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:44:7:
      // ( '|' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:44:9:
      // '|'
      {
        match('|');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "AND"

  // $ANTLR start "B_OR"
  public final void mB_XOR() throws RecognitionException {
    try {
      final int _type = B_XOR;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:46:8:
      // ( '^' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:46:10:
      // '^'
      {
        match('^');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "B_OR"

  // $ANTLR start "B_AND"
  public final void mCHARACTER() throws RecognitionException {
    try {
      final int _type = CHARACTER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:31:11:
      // ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:31:14:
      // ( 'a' .. 'z' | 'A' .. 'Z' )
      {
        if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z')
            || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
          input.consume();

        } else {
          final MismatchedSetException mse = new MismatchedSetException(null, input);
          recover(mse);
          throw mse;
        }


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "B_AND"

  // $ANTLR start "B_XOR"
  public final void mDIGIT() throws RecognitionException {
    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:28:17:
      // ( '0' .. '9' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:28:19:
      // '0' .. '9'
      {
        matchRange('0', '9');

      }

    } finally {
    }
  }

  // $ANTLR end "B_XOR"

  // $ANTLR start "LSH"
  public final void mDIV() throws RecognitionException {
    try {
      final int _type = DIV;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:50:6:
      // ( '/' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:50:8:
      // '/'
      {
        match('/');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "LSH"

  // $ANTLR start "RSH"
  public final void mEQ_SIGN() throws RecognitionException {
    try {
      final int _type = EQ_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:35:10:
      // ( '==' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:35:13:
      // '=='
      {
        match("==");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "RSH"

  // $ANTLR start "MULT"
  public final void mGEQ_SIGN() throws RecognitionException {
    try {
      final int _type = GEQ_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:36:11:
      // ( '>=' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:36:14:
      // '>='
      {
        match(">=");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "MULT"

  // $ANTLR start "DIV"
  public final void mGT_SIGN() throws RecognitionException {
    try {
      final int _type = GT_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:39:10:
      // ( '>' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:39:13:
      // '>'
      {
        match('>');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "DIV"

  // $ANTLR start "MOD"
  public final void mHEX_NUMBER() throws RecognitionException {
    try {
      final int _type = HEX_NUMBER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:33:12:
      // ( '0x' ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:33:14:
      // '0x' ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
      {
        match("0x");

        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:33:19:
        // ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
        int cnt2 = 0;
        loop2: do {
          int alt2 = 2;
          final int LA2_0 = input.LA(1);

          if (((LA2_0 >= '0' && LA2_0 <= '9') || (LA2_0 >= 'A' && LA2_0 <= 'F') || (LA2_0 >= 'a' && LA2_0 <= 'f'))) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:
            {
              if ((input.LA(1) >= '0' && input.LA(1) <= '9')
                  || (input.LA(1) >= 'A' && input.LA(1) <= 'F')
                  || (input.LA(1) >= 'a' && input.LA(1) <= 'f')) {
                input.consume();

              } else {
                final MismatchedSetException mse = new MismatchedSetException(null, input);
                recover(mse);
                throw mse;
              }


            }
              break;

            default:
              if (cnt2 >= 1) {
                break loop2;
              }
              final EarlyExitException eee = new EarlyExitException(2, input);
              throw eee;
          }
          cnt2++;
        } while (true);


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "MOD"

  // $ANTLR start "ADD"
  public final void mIDENTIFIER() throws RecognitionException {
    try {
      final int _type = IDENTIFIER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:34:12:
      // ( CHARACTER ( CHARACTER | DIGIT )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:34:14:
      // CHARACTER ( CHARACTER | DIGIT )+
      {
        mCHARACTER();
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:34:24:
        // ( CHARACTER | DIGIT )+
        int cnt3 = 0;
        loop3: do {
          int alt3 = 2;
          final int LA3_0 = input.LA(1);

          if (((LA3_0 >= '0' && LA3_0 <= '9') || (LA3_0 >= 'A' && LA3_0 <= 'Z') || (LA3_0 >= 'a' && LA3_0 <= 'z'))) {
            alt3 = 1;
          }


          switch (alt3) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:
            {
              if ((input.LA(1) >= '0' && input.LA(1) <= '9')
                  || (input.LA(1) >= 'A' && input.LA(1) <= 'Z')
                  || (input.LA(1) >= 'a' && input.LA(1) <= 'z')) {
                input.consume();

              } else {
                final MismatchedSetException mse = new MismatchedSetException(null, input);
                recover(mse);
                throw mse;
              }


            }
              break;

            default:
              if (cnt3 >= 1) {
                break loop3;
              }
              final EarlyExitException eee = new EarlyExitException(3, input);
              throw eee;
          }
          cnt3++;
        } while (true);


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "ADD"

  // $ANTLR start "SUB"
  public final void mLEQ_SIGN() throws RecognitionException {
    try {
      final int _type = LEQ_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:37:11:
      // ( '<=' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:37:14:
      // '<='
      {
        match("<=");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "SUB"

  public final void mLSH() throws RecognitionException {
    try {
      final int _type = LSH;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:47:6:
      // ( '<<' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:47:8:
      // '<<'
      {
        match("<<");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }


  public final void mLT_SIGN() throws RecognitionException {
    try {
      final int _type = LT_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:38:10:
      // ( '<' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:38:13:
      // '<'
      {
        match('<');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mMOD() throws RecognitionException {
    try {
      final int _type = MOD;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:51:6:
      // ( '%' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:51:8:
      // '%'
      {
        match('%');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mMULT() throws RecognitionException {
    try {
      final int _type = MULT;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:49:7:
      // ( '*' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:49:9:
      // '*'
      {
        match('*');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mNEQ_SIGN() throws RecognitionException {
    try {
      final int _type = NEQ_SIGN;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:40:11:
      // ( '!=' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:40:14:
      // '!='
      {
        match("!=");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mNEQ_SIGN_2() throws RecognitionException {
    try {
      final int _type = NEQ_SIGN_2;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:41:13:
      // ( '<>' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:41:16:
      // '<>'
      {
        match("<>");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mNUMBER() throws RecognitionException {
    try {
      final int _type = NUMBER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:32:9:
      // ( ( DIGIT )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:32:11:
      // ( DIGIT )+
      {
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:32:11:
        // ( DIGIT )+
        int cnt1 = 0;
        loop1: do {
          int alt1 = 2;
          final int LA1_0 = input.LA(1);

          if (((LA1_0 >= '0' && LA1_0 <= '9'))) {
            alt1 = 1;
          }


          switch (alt1) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:32:11:
            // DIGIT
            {
              mDIGIT();

            }
              break;

            default:
              if (cnt1 >= 1) {
                break loop1;
              }
              final EarlyExitException eee = new EarlyExitException(1, input);
              throw eee;
          }
          cnt1++;
        } while (true);


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mOR() throws RecognitionException {
    try {
      final int _type = OR;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:42:5:
      // ( '||' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:42:7:
      // '||'
      {
        match("||");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mRSH() throws RecognitionException {
    try {
      final int _type = RSH;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:48:6:
      // ( '>>' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:48:8:
      // '>>'
      {
        match(">>");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mSUB() throws RecognitionException {
    try {
      final int _type = SUB;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:53:6:
      // ( '-' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:53:8:
      // '-'
      {
        match('-');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mT__31() throws RecognitionException {
    try {
      final int _type = T__31;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:10:7:
      // ( '[' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:10:9:
      // '['
      {
        match('[');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mT__32() throws RecognitionException {
    try {
      final int _type = T__32;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:11:7:
      // ( ']' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:11:9:
      // ']'
      {
        match(']');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mT__33() throws RecognitionException {
    try {
      final int _type = T__33;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:12:7:
      // ( '(' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:12:9:
      // '('
      {
        match('(');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public final void mT__34() throws RecognitionException {
    try {
      final int _type = T__34;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:13:7:
      // ( ')' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:13:9:
      // ')'
      {
        match(')');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  public void mTokens() throws RecognitionException {
    // C:\\Dokumente und
    // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:8:
    // ( T__31 | T__32 | T__33 | T__34 | WS | CHARACTER | NUMBER | HEX_NUMBER | IDENTIFIER | EQ_SIGN
    // | GEQ_SIGN | LEQ_SIGN | LT_SIGN | GT_SIGN | NEQ_SIGN | NEQ_SIGN_2 | OR | AND | B_OR | B_AND |
    // B_XOR | LSH | RSH | MULT | DIV | MOD | ADD | SUB )
    int alt4 = 28;
    alt4 = dfa4.predict(input);
    switch (alt4) {
      case 1:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:10:
      // T__31
      {
        mT__31();

      }
        break;
      case 2:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:16:
      // T__32
      {
        mT__32();

      }
        break;
      case 3:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:22:
      // T__33
      {
        mT__33();

      }
        break;
      case 4:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:28:
      // T__34
      {
        mT__34();

      }
        break;
      case 5:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:34:
      // WS
      {
        mWS();

      }
        break;
      case 6:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:37:
      // CHARACTER
      {
        mCHARACTER();

      }
        break;
      case 7:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:47:
      // NUMBER
      {
        mNUMBER();

      }
        break;
      case 8:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:54:
      // HEX_NUMBER
      {
        mHEX_NUMBER();

      }
        break;
      case 9:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:65:
      // IDENTIFIER
      {
        mIDENTIFIER();

      }
        break;
      case 10:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:76:
      // EQ_SIGN
      {
        mEQ_SIGN();

      }
        break;
      case 11:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:84:
      // GEQ_SIGN
      {
        mGEQ_SIGN();

      }
        break;
      case 12:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:93:
      // LEQ_SIGN
      {
        mLEQ_SIGN();

      }
        break;
      case 13:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:102:
      // LT_SIGN
      {
        mLT_SIGN();

      }
        break;
      case 14:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:110:
      // GT_SIGN
      {
        mGT_SIGN();

      }
        break;
      case 15:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:118:
      // NEQ_SIGN
      {
        mNEQ_SIGN();

      }
        break;
      case 16:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:127:
      // NEQ_SIGN_2
      {
        mNEQ_SIGN_2();

      }
        break;
      case 17:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:138:
      // OR
      {
        mOR();

      }
        break;
      case 18:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:141:
      // AND
      {
        mAND();

      }
        break;
      case 19:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:145:
      // B_OR
      {
        mB_OR();

      }
        break;
      case 20:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:150:
      // B_AND
      {
        mB_AND();

      }
        break;
      case 21:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:156:
      // B_XOR
      {
        mB_XOR();

      }
        break;
      case 22:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:162:
      // LSH
      {
        mLSH();

      }
        break;
      case 23:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:166:
      // RSH
      {
        mRSH();

      }
        break;
      case 24:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:170:
      // MULT
      {
        mMULT();

      }
        break;
      case 25:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:175:
      // DIV
      {
        mDIV();

      }
        break;
      case 26:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:179:
      // MOD
      {
        mMOD();

      }
        break;
      case 27:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:183:
      // ADD
      {
        mADD();

      }
        break;
      case 28:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:1:187:
      // SUB
      {
        mSUB();

      }
        break;

    }

  }

  public final void mWS() throws RecognitionException {
    try {
      final int _type = WS;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:30:5:
      // ( ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' ) )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\BreakpointCondition\\Condition.g:30:8:
      // ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' )
      {
        if ((input.LA(1) >= '\t' && input.LA(1) <= '\n')
            || (input.LA(1) >= '\f' && input.LA(1) <= '\r') || input.LA(1) == ' ') {
          input.consume();

        } else {
          final MismatchedSetException mse = new MismatchedSetException(null, input);
          recover(mse);
          throw mse;
        }

        _channel = HIDDEN;

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  @Override
  public void reportError(final RecognitionException e) {
    throw new IllegalArgumentException(e);
  }

  class DFA4 extends DFA {

    public DFA4(final BaseRecognizer recognizer) {
      this.recognizer = recognizer;
      decisionNumber = 4;
      eot = DFA4_eot;
      eof = DFA4_eof;
      min = DFA4_min;
      max = DFA4_max;
      accept = DFA4_accept;
      special = DFA4_special;
      transition = DFA4_transition;
    }

    public String getDescription() {
      return "1:1: Tokens : ( T__31 | T__32 | T__33 | T__34 | WS | CHARACTER | NUMBER | HEX_NUMBER | IDENTIFIER | EQ_SIGN | GEQ_SIGN | LEQ_SIGN | LT_SIGN | GT_SIGN | NEQ_SIGN | NEQ_SIGN_2 | OR | AND | B_OR | B_AND | B_XOR | LSH | RSH | MULT | DIV | MOD | ADD | SUB );";
    }
  }


}
