grammar Condition;

options {
	output = AST;
}

tokens { SUB_EXPRESSION; MEMORY_EXPRESSION; }

@header {
package com.google.security.zynamics.BinNavi.parsers.BreakpointCondition;
import java.util.HashMap;
}

@members {
  @Override
  public void reportError(RecognitionException e) {
    throw new IllegalArgumentException(e);
  }
}

@lexer::members {
  @Override
  public void reportError(RecognitionException e) {
    throw new IllegalArgumentException(e);
  }
}

fragment DIGIT		:	'0' .. '9';

WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;};
CHARACTER	: 	('a'..'z' | 'A'..'Z');
NUMBER		:	DIGIT+;
HEX_NUMBER	:	'0x' ('0'..'9'| 'a'..'f' | 'A' .. 'F')+ ;
IDENTIFIER	:	CHARACTER (CHARACTER | DIGIT)+;
EQ_SIGN 	: 	'==';
GEQ_SIGN 	: 	'>=';
LEQ_SIGN 	: 	'<=';
LT_SIGN 	: 	'<';
GT_SIGN 	: 	'>';
NEQ_SIGN 	: 	'!=';
NEQ_SIGN_2 	: 	'<>';
OR		:	'||';
AND		:	'&&';
B_OR		:	'|';
B_AND		:	'&';
B_XOR		:	'^';
LSH		:	'<<';
RSH		:	'>>';
MULT		:	'*';
DIV		:	'/';
MOD		:	'%';
ADD		:	'+';
SUB		:	'-';

prog		:	conditionChain EOF;

conditionChain
		:	andExpression (OR^ andExpression)*
		;

andExpression
		:	condition (AND^ condition)*
		;

condition	: 	formula (EQ_SIGN|GEQ_SIGN|LEQ_SIGN|LT_SIGN|GT_SIGN|NEQ_SIGN|NEQ_SIGN_2)^ formula
		;

formula
		:	bxorExpression (B_OR^ bxorExpression)*
		;
		
bxorExpression
		:	bandExpression (B_XOR^ bandExpression)*
		;
		
bandExpression
		:	shiftExpression (B_AND^ shiftExpression)*
		;
		
shiftExpression
		:	addExpression ((LSH|RSH)^ addExpression)*
		;
		
addExpression
		:	multExpression ((ADD|SUB)^ multExpression)*
		;
		
multExpression
		:	primaryExpression ((MULT|DIV|MOD)^ primaryExpression)*
		;

primaryExpression
		:	IDENTIFIER
		|	NUMBER
		|	HEX_NUMBER
		|	'[' formula ']' -> ^(MEMORY_EXPRESSION formula)
		|	'(' formula ')' -> ^(SUB_EXPRESSION formula)
		;
