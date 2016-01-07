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
package com.google.security.zynamics.binnavi.parsers.gotomem;

// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Dokumente und
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g
// 2009-10-19 09:57:01

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings("all")
public class MemoryExpressionLexer extends Lexer {
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
  public static final int REGISTER = 14;
  public static final int MEM_EXPRESSION = 7;
  public static final int DECIMAL_NUMBER = 5;

  // delegates
  // delegators

  protected DFA4 dfa4 = new DFA4(this);
  static final String DFA4_eotS = "\6\uffff\1\14\1\10\7\uffff";
  static final String DFA4_eofS = "\17\uffff";
  static final String DFA4_minS = "\1\11\5\uffff\1\60\1\170\7\uffff";

  static final String DFA4_maxS = "\1\172\5\uffff\1\172\1\170\7\uffff";

  static final String DFA4_acceptS =
      "\1\uffff\1\1\1\2\1\3\1\4\1\5\2\uffff\1\7\1\12\1\13\1\14\1\6\1\11" + "\1\10";

  static final String DFA4_specialS = "\17\uffff}>";

  static final String[] DFA4_transitionS = {
      "\2\5\1\uffff\2\5\22\uffff\1\5\7\uffff\1\3\1\4\1\13\1\11\1\uffff"
          + "\1\12\2\uffff\1\7\11\10\7\uffff\32\6\1\1\1\uffff\1\2\3\uffff" + "\32\6", "", "", "",
      "", "", "\12\15\7\uffff\32\15\6\uffff\32\15", "\1\16", "", "", "", "", "", "", ""};

  static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);

  static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);

  static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);

  static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);

  static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);

  static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);

  static final short[][] DFA4_transition;

  static {
    final int numStates = DFA4_transitionS.length;
    DFA4_transition = new short[numStates][];
    for (int i = 0; i < numStates; i++) {
      DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
    }
  }

  public MemoryExpressionLexer() {
  }

  public MemoryExpressionLexer(final CharStream input) {
    this(input, new RecognizerSharedState());
  }


  public MemoryExpressionLexer(final CharStream input, final RecognizerSharedState state) {
    super(input, state);

  }

  @Override
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g";
  }

  // $ANTLR start "CHARACTER"
  public final void mCHARACTER() throws RecognitionException {
    try {
      final int _type = CHARACTER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:24:11:
      // ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:24:14:
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

  // $ANTLR end "CHARACTER"
  // $ANTLR start "DIGIT"
  public final void mDIGIT() throws RecognitionException {
    try {
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:21:17:
      // ( '0' .. '9' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:21:19:
      // '0' .. '9'
      {
        matchRange('0', '9');

      }

    } finally {
    }
  }

  // $ANTLR end "DIGIT"
  // $ANTLR start "HEX_NUMBER"
  public final void mHEX_NUMBER() throws RecognitionException {
    try {
      final int _type = HEX_NUMBER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:26:12:
      // ( '0x' ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:26:14:
      // '0x' ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
      {
        match("0x");

        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:26:19:
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
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:
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

  // $ANTLR end "HEX_NUMBER"
  // $ANTLR start "NUMBER"
  public final void mNUMBER() throws RecognitionException {
    try {
      final int _type = NUMBER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:25:9:
      // ( ( DIGIT )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:25:11:
      // ( DIGIT )+
      {
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:25:11:
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
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:25:11:
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

  // $ANTLR end "NUMBER"
  // $ANTLR start "OPERAND_MINUS"
  public final void mOPERAND_MINUS() throws RecognitionException {
    try {
      final int _type = OPERAND_MINUS;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:29:15:
      // ( '-' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:29:17:
      // '-'
      {
        match('-');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "OPERAND_MINUS"
  // $ANTLR start "OPERAND_MULT"
  public final void mOPERAND_MULT() throws RecognitionException {
    try {
      final int _type = OPERAND_MULT;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:30:14:
      // ( '*' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:30:16:
      // '*'
      {
        match('*');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "OPERAND_MULT"

  // $ANTLR start "OPERAND_PLUS"
  public final void mOPERAND_PLUS() throws RecognitionException {
    try {
      final int _type = OPERAND_PLUS;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:28:14:
      // ( '+' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:28:16:
      // '+'
      {
        match('+');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "OPERAND_PLUS"
  // $ANTLR start "REGISTER"
  public final void mREGISTER() throws RecognitionException {
    try {
      final int _type = REGISTER;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:27:10:
      // ( CHARACTER ( CHARACTER | DIGIT )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:27:12:
      // CHARACTER ( CHARACTER | DIGIT )+
      {
        mCHARACTER();
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:27:22:
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
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:
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

  // $ANTLR end "REGISTER"
  // $ANTLR start "T__18"
  public final void mT__18() throws RecognitionException {
    try {
      final int _type = T__18;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:3:7:
      // ( '[' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:3:9:
      // '['
      {
        match('[');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__18"
  // $ANTLR start "T__19"
  public final void mT__19() throws RecognitionException {
    try {
      final int _type = T__19;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:4:7:
      // ( ']' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:4:9:
      // ']'
      {
        match(']');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__19"
  // $ANTLR start "T__20"
  public final void mT__20() throws RecognitionException {
    try {
      final int _type = T__20;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:5:7:
      // ( '(' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:5:9:
      // '('
      {
        match('(');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__20"
  // $ANTLR start "T__21"
  public final void mT__21() throws RecognitionException {
    try {
      final int _type = T__21;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:6:7:
      // ( ')' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:6:9:
      // ')'
      {
        match(')');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__21"
  @Override
  public void mTokens() throws RecognitionException {
    // C:\\Dokumente und
    // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:8:
    // ( T__18 | T__19 | T__20 | T__21 | WS | CHARACTER | NUMBER | HEX_NUMBER | REGISTER |
    // OPERAND_PLUS | OPERAND_MINUS | OPERAND_MULT )
    int alt4 = 12;
    alt4 = dfa4.predict(input);
    switch (alt4) {
      case 1:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:10:
      // T__18
      {
        mT__18();

      }
        break;
      case 2:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:16:
      // T__19
      {
        mT__19();

      }
        break;
      case 3:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:22:
      // T__20
      {
        mT__20();

      }
        break;
      case 4:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:28:
      // T__21
      {
        mT__21();

      }
        break;
      case 5:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:34:
      // WS
      {
        mWS();

      }
        break;
      case 6:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:37:
      // CHARACTER
      {
        mCHARACTER();

      }
        break;
      case 7:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:47:
      // NUMBER
      {
        mNUMBER();

      }
        break;
      case 8:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:54:
      // HEX_NUMBER
      {
        mHEX_NUMBER();

      }
        break;
      case 9:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:65:
      // REGISTER
      {
        mREGISTER();

      }
        break;
      case 10:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:74:
      // OPERAND_PLUS
      {
        mOPERAND_PLUS();

      }
        break;
      case 11:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:87:
      // OPERAND_MINUS
      {
        mOPERAND_MINUS();

      }
        break;
      case 12:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:1:101:
      // OPERAND_MULT
      {
        mOPERAND_MULT();

      }
        break;

    }

  }

  // $ANTLR start "WS"
  public final void mWS() throws RecognitionException {
    try {
      final int _type = WS;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:23:6:
      // ( ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' ) )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\gotomem\\MemoryExpression.g:23:9:
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

  // $ANTLR end "WS"

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

    @Override
    public String getDescription() {
      return "1:1: Tokens : ( T__18 | T__19 | T__20 | T__21 | WS | CHARACTER | NUMBER | HEX_NUMBER | REGISTER | OPERAND_PLUS | OPERAND_MINUS | OPERAND_MULT );";
    }
  }


}
