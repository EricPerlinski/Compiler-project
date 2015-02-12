grammar Plic;

root
	: prog;

prog
	: 'do' declaration* instruction+ 'end' ;

declaration
	: dec_var
	| dec_func
	| dec_proc;

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
	: affectation
	| bloc
	| iteration
	| condition
	| return_func
	| proc_call
	| read
	| write;

bloc
	: 'begin' declaration* instruction+ 'end';

affectation
	: IDF affectation_rec;

affectation_rec
	: '=' exp
	| '[' exp (',' exp)? ']' '=' exp ;

iteration
	: 'for' IDF 'in' exp '..' exp 'do'  instruction+ 'end';

condition
	: 'if' exp 'then'  instruction+ ('else'  instruction+)? 'fi';

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
	: fois plusmoins2*;

plusmoins2
	: '+' fois
	| '-' fois;

fois
	: unaire fois2*;

fois2
	: '*' unaire;

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

CSTE_ENT : '0'..'9'+ ;
CSTE_CHAINE : '\"' .* '\"' ;
IDF : ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NEWLINE:'\r'? '\n' {$channel=HIDDEN;};
WS  :   (' '|'\t'|('/*' .* '*/'))+ {$channel=HIDDEN;} ;