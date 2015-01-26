grammar Plic;

root
	: prog NEWLINE;

prog
	: 'do' declaration* instruction+ 'end' ;

declaration
	: dec_var
	| dec_func
	| dec_proc ;

dec_var
	: type IDF (',' IDF)*;

type
	: 'integer'
	| 'boolean'
	| array;

array
	: 'array' '\[' bounds '\]';

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
	: affectation
	| bloc
	| iteration
	| condition
	| return
	| read
	| write;

bloc
	: 'begin' declaration* instruction+ 'end';

affectation
	: IDF '=' exp;

iteration
	: 'for' IDF 'in' exp '..' exp 'do' instruction+ 'end';

condition
	: 'if' exp 'then' instruction+ ('else' instruction+)? 'fi';

return
	: 'return' '(' exp ')';

read
	: 'read' IDF;

write
	: 'write' write_arg;

write_arg
	: exp
	| CSTE_CHAINE;

exp
	: IDF idf_arg exp_arg
	| CSTE_ENT exp_arg
	| 'true' exp_arg
	| 'false' exp_arg
	| '(' exp ')' exp_arg
	| '-' exp exp_arg ;
	
exp_arg
	: oper exp exp_arg
	| ;
	
idf_arg
	: '(' ( exp ( ',' exp)* )? ')'
	| ;

oper
	: '+' 
	| '-' 
	| '*' 
	| '<' 
	| '<=' 
	| '>' 
	| '>=' 
	| '==' 
	| '!=' ;




NEWLINE:'\r'? '\n' ;
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;
CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\'' .* '\'' ;
IDF : ('a'..'z')('a'..'z'|'A'..'Z'|'0'..'9')* ;