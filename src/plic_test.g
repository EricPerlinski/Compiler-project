grammar plic_test;

root
	: prog;

prog
	: 'do' declaration* instruction+ 'end' ;

declaration
	: dec_var
	| dec_func
	| dec_proc;

dec_var
	: type ID (',' ID)*;

type
	: 'integer'
	| 'boolean'
	| array;

array
	: 'array' '[' bounds ']';

bounds
	: INT '..' INT (',' INT '..' INT)*;

dec_func
	: ent_func declaration* instruction+ 'end';

dec_proc
	: ent_proc declaration* instruction+ 'end';

ent_func
	: 'function' type ID param;

ent_proc
	: 'procedure' ID param;

param
	: '(' (formal (',' formal)*)? ')';

formal
	: ('adr')? ID ':' type;

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
	: ID affectation_rec;

affectation_rec
	: '=' exp
	| '[' exp (',' exp)? ']' '=' exp ;

iteration
	: 'for' ID 'in' exp '..' exp 'do' instruction+ 'end';

condition
	: 'if' exp 'then' instruction+ ('else' instruction+)? 'fi';

return_func
	: 'return' '(' exp ')';

proc_call
	: ID '(' ( exp ( ',' exp)* )? ')';


read
	: 'read' ID;

write
	: 'write' write_arg;

write_arg
	: exp
	| STRING;

exp
	: mult (add_oper mult)*;
	
mult
	: comp (mult_oper comp)*;
	
comp
	: unaire (comp_oper unaire)*;
	
unaire
	: unaire_oper* parent;

parent
	: atom
	| '(' exp ')';

add_oper
	: '+' 
	| '-' ;
	
mult_oper
	: '*';
	
comp_oper
	: '<' 
	| '<=' 
	| '>' 
	| '>=' 
	| '==' 
	| '!=' ;
	
unaire_oper
	: '-';
	
atom
	: INT;


ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :	'0'..'9'+
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
