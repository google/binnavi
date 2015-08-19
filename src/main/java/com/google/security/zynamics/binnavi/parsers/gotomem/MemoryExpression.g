grammar MemoryExpression;

options {
	output = AST;
}

tokens { HEXADECIMAL_NUMBER; DECIMAL_NUMBER; ADD_EXPRESSION; MEM_EXPRESSION; SUB_EXPRESSION; }

@header {
package com.google.security.zynamics.BinNavi.parsers.gotomem;
import java.util.HashMap;
}

@rulecatch {
	catch (RecognitionException e)
	{
  		throw e;
	}
}

fragment DIGIT		:	'0' .. '9';

WS 		:  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;};
CHARACTER	: 	('a'..'z' | 'A'..'Z');
NUMBER		:	DIGIT+;
HEX_NUMBER	:	'0x' ('0'..'9'| 'a'..'f' | 'A' .. 'F')+ ;
REGISTER	:	CHARACTER (CHARACTER | DIGIT)+;
OPERAND_PLUS	:	'+';
OPERAND_MINUS	:	'-';
OPERAND_MULT	:	'*';


prog		:	expression EOF;

expression
		:	addExpression;
		
addExpression
		:	multExpression ((OPERAND_PLUS^ | OPERAND_MINUS^) multExpression)*// -> ^(ADD_EXPRESSION multExpression multExpression)
		;

multExpression
		:	primaryExpression (OPERAND_MULT^ primaryExpression)*
		;

primaryExpression
		:	REGISTER
		|	NUMBER
		|	HEX_NUMBER
		|	'[' expression ']' -> ^(MEM_EXPRESSION expression)
		|	'(' expression ')' -> ^(SUB_EXPRESSION expression)
		;
