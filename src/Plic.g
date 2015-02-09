grammar Plic;

root
	: prog NEWLINE*;

prog
	: DO NEWLINE* declaration* instruction+ 'end' ;

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
	| return_func NEWLINE*
	| proc_call
	| read NEWLINE*
	| write NEWLINE*;

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

return_func
	: 'return' '(' exp ')';

proc_call
	: IDF '(' ( exp ( ',' exp)* )? ')';


read
	: 'read' IDF;

write
	: 'write' write_arg;

write_arg
	: exp
	| CSTE_CHAINE;

exp
	: plusmoins;

plusmoins
	: fois plusmoins2;

plusmoins2
	: '+' plusmoins
	| '-' plusmoins
	| ;

fois
	: unaire fois2;

fois2
	: '*' fois
	| ;

unaire
	: '-'? comp;

comp
	: parenthesis comp2;

comp2
	: '==' comp
	| '!=' comp
	| '<' comp
	| '<=' comp
	| '>' comp
	| '>=' comp
	| ;

parenthesis
	: '(' exp ')' | atom ;

atom
	: 'true'
	| 'false'
	| CSTE_ENT
	| IDF atom_rec;

atom_rec
	: idf_arg
	| '[' exp (',' exp)* ']' ;

idf_arg
	: '(' ( exp ( ',' exp)* )? ')'
	| ;



DO : 'do';
CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\"' .* '\"' ;
IDF : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9')* ;
NEWLINE:'\r'? '\n' {$channel=HIDDEN;};
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;