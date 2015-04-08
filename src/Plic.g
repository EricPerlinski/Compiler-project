grammar Plic;

options{
	output=AST;
}

tokens {
	PROG;
	DECLARATION;
	DECLARATIONS;
	INSTRUCTION;
	INSTRUCTIONS;
	VARIABLE;
	FUNCTION;
	PROCEDURE;
	PROTOTYPE;
	BLOC;
	PARAMS;
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
	UNAIRE;
	ARRAY;
	BOUNDS;
	BOUND;
}



root
	: prog 
	;

prog
	: 'do' declaration* instruction+ 'end'
		->  ^(PROG ^(DECLARATIONS declaration*) ^(INSTRUCTIONS instruction+))
	;

declaration
	: dec_var
		->^(VARIABLE dec_var)
	| dec_func
		-> dec_func
	| dec_proc
		->dec_proc
	;

dec_var
	: type IDF (',' IDF)*
		-> type IDF+
	;

type
	: 'integer'

	| 'boolean'
		
	| 'array' '[' bounds ']'
		-> ^(ARRAY ^(BOUNDS bounds))
	;

bounds
	: cste_array '..' cste_array (',' cste_array '..' cste_array)*
		-> ^(BOUND cste_array cste_array)+
	;

dec_func
	: ent_func declaration* instruction+ 'end'
		-> ^(FUNCTION ^(PROTOTYPE ent_func) ^(DECLARATIONS declaration*) ^(INSTRUCTIONS instruction+))
	;

dec_proc
	: ent_proc declaration* instruction+ 'end'
		-> ^(PROCEDURE ^(PROTOTYPE ent_proc) ^(DECLARATIONS declaration*) ^(INSTRUCTIONS instruction+)) 
	;

ent_func
	: 'function'! type IDF param
	;

ent_proc
	: 'procedure'! IDF param
	;

param
	: '(' (formal (',' formal)*)? ')'
		-> ^(PARAMS formal*)
	;

formal
	: 'adr' IDF ':' type
		-> ^(VARIABLE 'adr' type IDF)
	| IDF ':' type
		->^(VARIABLE type IDF)
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
	: atom_aff '=' exp 
			-> ^(AFFECTATION ^(AFF_LEFT atom_aff) ^(AFF_RIGHT exp))
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
	: fois (('+'|'-')^ fois)*
		;

fois
	: unaire ('*'^ unaire)*;


unaire
	: '-' comp
		-> ^(UNAIRE comp)
	| comp
	;

comp
	: parenthesis ( comp_oper^ parenthesis)*;

comp_oper
	: '<' 
	| '<=' 
	| '>' 
	| '>=' 
	| '==' 
	| '!=' ;

parenthesis
	: '(' exp ')' 
		-> exp
	| atom ;

atom
	: 'true'
	| 'false'
	| CSTE_ENT
	| IDF idf_arg 
		-> ^(FUNC_CALL IDF idf_arg?)
	| atom_aff
	;

atom_aff
	: IDF 
		(
		'[' exp (',' exp)* ']'
			-> ^(ARRAY IDF exp*)
		|
			-> IDF
		);


idf_arg
	: '(' ( exp ( ',' exp)* )? ')'
		-> (exp*)?
	;

cste_array
	:
	CSTE_ENT 
	| '-'CSTE_ENT -> ^(UNAIRE CSTE_ENT)
	;

CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\"' .* '\"' ;
IDF : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NEWLINE:'\r'? '\n' {$channel=HIDDEN;};
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;