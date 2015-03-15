grammar Plic_test;

root
	: prog NEWLINE*;

prog
	: 'do' NEWLINE* declaration* instruction+ 'end' ;

declaration
	: dec_var NEWLINE*
	| dec_func NEWLINE*
	| dec_proc NEWLINE*;

dec_var
	: type IDF (',' IDF)*;

type
	: 'integer'
	| 'boolean'
	| array;

array
	: 'array' '[' bounds ']';

bounds
	: CSTE_ENT '..' CSTE_ENT (',' CSTE_ENT '..' CSTE_ENT)*;

dec_func
	: ent_func declaration* instruction+ 'end';

dec_proc
	: ent_proc declaration* instruction+ 'end';

ent_func
	: 'function' type IDF param;

ent_proc
	: 'procedure' IDF param;

param
	: '(' (formal (',' formal)*)? ')';

formal
	: ('adr')? IDF ':' type;

instruction
	: affectation NEWLINE*
	| bloc NEWLINE*
	| iteration NEWLINE*
	| condition NEWLINE*
	;

bloc
	: 'begin' declaration* instruction+ 'end';

affectation
	: IDF affectation_rec;

affectation_rec
	: '=' exp
	| '[' exp (',' exp)? ']' '=' exp ;

iteration
	: 'for' IDF 'in' exp '..' exp 'do' NEWLINE* instruction+ 'end';

condition
	: 'if' exp NEWLINE* 'then' NEWLINE* instruction+ ('else' NEWLINE* instruction+)? 'fi';


exp
	: mult (exp2)*
	;
	
 exp2 	: '+' mult
 	| '-' mult
	;

 mult	
 	:  term ('*' term)*
 	;
 
 term 	
 	:unaire (term2)*
 	; 
 	
 term2 
 	:comp_oper unaire
 	;
 		
 unaire 	
 	: CSTE_ENT
	| IDF
	|'-'exp
	;

comp_oper
	: '=='
	| '!='
	| '<'  
	| '>' 
	| '<='
	| '>=' 
	;

CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\"' .* '\"' ;
IDF : ('a'..'z')('a'..'z'|'A'..'Z'|'0'..'9')* ;
NEWLINE:'\r'? '\n' {$channel=HIDDEN;};
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;