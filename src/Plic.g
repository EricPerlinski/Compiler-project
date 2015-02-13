grammar Plic;

options{
	output=AST;
}

tokens {
	PROG;
	DECLARATION;
	INSTRUCTION;
	FUNCTION;
	PROCEDURE;
	BLOC;
	AFFECTATION;
	IF;
	FOR;
	RETURN;
	READ;
	WRITE;
	PROC_CALL;
	FUNC_CALL;
	AFF_LEFT;
	AFF_RIGHT;
	ARRAY;
	CONDITION;
	IF_BLOC;
	ELSE_BLOC;
}



root
	: prog 
	;

prog
	: 'do' declaration* instruction+ 'end'
		->  ^(PROG declaration* instruction+)
	;

declaration
	: dec_var
	| dec_func
	| dec_proc
	;

dec_var
	: type IDF (',' IDF)*
		-> ^(DECLARATION type IDF+)
	;

type
	: 'integer'
	| 'boolean'
	| array
	;

array
	: 'array' '[' bounds ']'
		-> 'array' bounds
	;

bounds
	: CSTE_ENT '..' CSTE_ENT (',' CSTE_ENT '..' CSTE_ENT)*
		-> (CSTE_ENT CSTE_ENT)+
	;

dec_func
	: ent_func declaration* instruction+ 'end'
		-> ^(FUNCTION ent_func declaration* instruction+)
	;

dec_proc
	: ent_proc declaration* instruction+ 'end'
		-> ^(PROCEDURE ent_proc declaration* instruction+) 
	;

ent_func
	: 'function'! type IDF param
	;

ent_proc
	: 'procedure'! IDF param
	;

param
	: '(' (formal (',' formal)*)? ')'
		-> formal*
	;

formal
	: ('adr')? IDF ':' type
		-> ('adr')? IDF type
	;

instruction
	: affectation
	| bloc
	| iteration
	| condition
	| return_func
	| proc_call
	| read
	| write;

bloc
	: 'begin' declaration* instruction+ 'end'
		-> ^(BLOC declaration* instruction+) 
	;

affectation
	: IDF
	(
		('=' exp 
			-> ^(AFFECTATION ^(AFF_LEFT IDF) ^(AFF_RIGHT exp)) )|
		('[' exp (',' exp)? ']' '=' exp  
			-> ^(AFFECTATION ^(AFF_LEFT IDF exp+) ^(AFF_RIGHT exp)))
	)
		
		
	;

affectation_rec
	: '=' exp
		-> exp
	| '[' exp (',' exp)? ']' '=' exp 
		-> exp+ exp
	;

iteration
	: 'for' IDF 'in' exp '..' exp 'do'  instruction+ 'end'
		-> ^(FOR IDF exp exp instruction+)
	;

condition
	: 'if' exp 'then' condition_if  ('else' condition_else )? 'fi'
		-> ^(IF ^(CONDITION exp) ^(IF_BLOC condition_if) ^(ELSE_BLOC condition_else)?)
	;

condition_if
	: instruction+
	;

condition_else
	: instruction+
	;

return_func
	: 'return' '(' exp ')'
		-> ^(RETURN exp)
	;

proc_call	
	: IDF '(' ( exp ( ',' exp)* )? ')'
		-> ^(PROC_CALL IDF exp*)
	;


read
	: 'read' IDF
		-> ^(READ IDF)
	;

write
	: 'write' write_arg
		-> ^(WRITE write_arg)
	;

write_arg
	: exp
	| CSTE_CHAINE;

exp
	: plusmoins;

plusmoins
	: fois (('+'|'-') fois)*;

fois
	: unaire ('*' unaire)*;


unaire
	: '-'? comp;

comp
	: parenthesis ( comp_oper parenthesis)*;

comp_oper
	: '<' 
	| '<=' 
	| '>' 
	| '>=' 
	| '==' 
	| '!=' ;

parenthesis
	: '(' exp ')' 
	| atom ;

atom
	: 'true'
	| 'false'
	| CSTE_ENT
	| IDF 
		(
		idf_arg 
			-> ^(FUNC_CALL IDF idf_arg)
		|
		'[' exp (',' exp)* ']'
			-> ^(ARRAY IDF exp*)
		|
			-> IDF
		)
	;


idf_arg
	: '(' ( exp ( ',' exp)* )? ')'
		-> exp*
	;

CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\"' .* '\"' ;
IDF : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NEWLINE:'\r'? '\n' {$channel=HIDDEN;};
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;