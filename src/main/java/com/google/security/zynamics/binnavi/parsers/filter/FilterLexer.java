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
package com.google.security.zynamics.binnavi.parsers.filter;

// $ANTLR 3.1.2 C:\\Dokumente und
// Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g 2009-04-14
// 09:53:58

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings("all")
public final class FilterLexer extends Lexer {
  static final String DFA1_eotS = "\1\1\13\uffff";
  static final String DFA1_eofS = "\14\uffff";
  static final String DFA1_minS = "\1\11\13\uffff";
  static final String DFA1_maxS = "\1\172\13\uffff";
  static final String DFA1_acceptS = "\1\uffff\1\13\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12";
  static final String DFA1_specialS = "\14\uffff}>";
  static final String[] DFA1_transitionS = {
      "\2\13\1\uffff\2\13\22\uffff\1\13\1\12\16\uffff\12\4\2\uffff"
      + "\1\5\1\7\1\6\1\uffff\1\11\32\3\4\uffff\1\10\1\uffff\32\2",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      ""};
  static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);

  // delegates
  // delegators

  static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
  static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
  static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
  static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);

  static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);

  static final short[][] DFA1_transition;

  public static final int WS = 5;

  public static final int SUB_EXPRESSION = 4;

  public static final int OR = 8;

  public static final int T__10 = 10;

  public static final int AND = 7;


  public static final int EOF = -1;
  public static final int T__9 = 9;
  public static final int PREDICATE = 6;
  protected DFA1 dfa1 = new DFA1(this);
  static {
    final int numStates = DFA1_transitionS.length;
    DFA1_transition = new short[numStates][];
    for (int i = 0; i < numStates; i++) {
      DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
    }
  }

  public FilterLexer() {
  }

  public FilterLexer(final CharStream input) {
    this(input, new RecognizerSharedState());
  }

  public FilterLexer(final CharStream input, final RecognizerSharedState state) {
    super(input, state);

  }

  @Override
  public String getGrammarFileName() {
    return "C:\\Dokumente und Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g";
  }

  // $ANTLR start "AND"
  public final void mAND() throws RecognitionException {
    try {
      final int _type = AND;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:17:6:
      // ( '&&' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:17:8:
      // '&&'
      {
        match("&&");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "AND"
  // $ANTLR start "OR"
  public final void mOR() throws RecognitionException {
    try {
      final int _type = OR;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:18:5:
      // ( '||' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:18:7:
      // '||'
      {
        match("||");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "OR"
  // $ANTLR start "PREDICATE"
  public final void mPREDICATE() throws RecognitionException {
    try {
      final int _type = PREDICATE;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:11:
      // ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '<' | '>' | '=' | '_' | '@' | '!' | WS )+ )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:13:
      // ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '<' | '>' | '=' | '_' | '@' | '!' | WS )+
      {
        // C:\\Dokumente und
        // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:13:
        // ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '<' | '>' | '=' | '_' | '@' | '!' | WS )+
        int cnt1 = 0;
        loop1: do {
          int alt1 = 11;
          alt1 = dfa1.predict(input);
          switch (alt1) {
            case 1:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:14:
            // 'a' .. 'z'
          {
            matchRange('a', 'z');

          }
              break;
            case 2:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:25:
            // 'A' .. 'Z'
          {
            matchRange('A', 'Z');

          }
              break;
            case 3:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:36:
            // '0' .. '9'
          {
            matchRange('0', '9');

          }
              break;
            case 4:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:47:
            // '<'
          {
            match('<');

          }
              break;
            case 5:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:53:
            // '>'
          {
            match('>');

          }
              break;
            case 6:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:59:
            // '='
          {
            match('=');

          }
              break;
            case 7:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:65:
            // '_'
          {
            match('_');

          }
              break;
            case 8:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:71:
            // '@'
          {
            match('@');

          }
              break;
            case 9:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:77:
            // '!'
          {
            match('!');

          }
              break;
            case 10:
            // C:\\Dokumente und
            // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:16:83:
            // WS
          {
            mWS();

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

  // $ANTLR end "PREDICATE"
  // $ANTLR start "T__10"
  public final void mT__10() throws RecognitionException {
    try {
      final int _type = T__10;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:4:7: (
      // ')' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:4:9:
      // ')'
      {
        match(')');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__10"
  // $ANTLR start "T__9"
  public final void mT__9() throws RecognitionException {
    try {
      final int _type = T__9;
      final int _channel = DEFAULT_TOKEN_CHANNEL;
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:3:6: (
      // '(' )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:3:8:
      // '('
      {
        match('(');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }

  // $ANTLR end "T__9"
  @Override
  public void mTokens() throws RecognitionException {
    // C:\\Dokumente und
    // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:8: (
    // T__9 | T__10 | WS | PREDICATE | AND | OR )
    int alt2 = 6;
    switch (input.LA(1)) {
      case '(': {
      alt2 = 1;
    }
        break;
      case ')': {
      alt2 = 2;
    }
        break;
      case '\t':
      case '\n':
      case '\f':
      case '\r':
      case ' ': {
      final int LA2_3 = input.LA(2);

      if (((LA2_3 >= '\t' && LA2_3 <= '\n') || (LA2_3 >= '\f' && LA2_3 <= '\r')
          || (LA2_3 >= ' ' && LA2_3 <= '!') || (LA2_3 >= '0' && LA2_3 <= '9')
          || (LA2_3 >= '<' && LA2_3 <= '>') || (LA2_3 >= '@' && LA2_3 <= 'Z') || LA2_3 == '_'
          || (LA2_3 >= 'a' && LA2_3 <= 'z'))) {
        alt2 = 4;
      } else {
        alt2 = 3;
      }
    }
        break;
      case '!':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case '<':
      case '=':
      case '>':
      case '@':
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      case 'G':
      case 'H':
      case 'I':
      case 'J':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case 'Z':
      case '_':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'g':
      case 'h':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'm':
      case 'n':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 't':
      case 'u':
      case 'v':
      case 'w':
      case 'x':
      case 'y':
      case 'z': {
      alt2 = 4;
    }
        break;
      case '&': {
      alt2 = 5;
    }
        break;
      case '|': {
      alt2 = 6;
    }
        break;
      default:
        final NoViableAltException nvae = new NoViableAltException("", 2, 0, input);

        throw nvae;
    }

    switch (alt2) {
      case 1:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:10:
      // T__9
    {
      mT__9();

    }
        break;
      case 2:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:15:
      // T__10
    {
      mT__10();

    }
        break;
      case 3:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:21:
      // WS
    {
      mWS();

    }
        break;
      case 4:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:24:
      // PREDICATE
    {
      mPREDICATE();

    }
        break;
      case 5:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:34:
      // AND
    {
      mAND();

    }
        break;
      case 6:
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:1:38:
      // OR
    {
      mOR();

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
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:15:5:
      // ( ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' ) )
      // C:\\Dokumente und
      // Einstellungen\\sp\\workspace\\com.google.security.zynamics.binnavi-Trunk\\src\\com.google.security.zynamics.binnavi\\parsers\\filter\\Filter.g:15:8:
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

  class DFA1 extends DFA {

    public DFA1(final BaseRecognizer recognizer) {
      this.recognizer = recognizer;
      this.decisionNumber = 1;
      this.eot = DFA1_eot;
      this.eof = DFA1_eof;
      this.min = DFA1_min;
      this.max = DFA1_max;
      this.accept = DFA1_accept;
      this.special = DFA1_special;
      this.transition = DFA1_transition;
    }

    @Override
    public String getDescription() {
      return "()+ loopback of 16:13: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '<' | '>' | '=' | '_' | '@' | '!' | WS )+";
    }
  }


}
