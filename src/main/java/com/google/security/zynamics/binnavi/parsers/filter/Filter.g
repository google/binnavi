grammar Filter;

options {
	output = AST;
}

tokens { SUB_EXPRESSION; }

@header {
package com.google.security.zynamics.BinNavi.parsers.filter;
import java.util.HashMap;
}


WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;};
PREDICATE	:	('a'..'z' | 'A'..'Z' | '0'..'9' | '<' | '>' | '=' | '_' | '@' | '!' | WS)+ ;
AND		:	'&&';
OR		:	'||';

prog		:	expression;

expression
		:	andExpression (OR^ andExpression)*
		;

andExpression
		:	primaryExpression (AND^ primaryExpression)*
		;
	
primaryExpression
		:	PREDICATE
		|	'(' expression ')' -> ^(SUB_EXPRESSION expression)
		;
