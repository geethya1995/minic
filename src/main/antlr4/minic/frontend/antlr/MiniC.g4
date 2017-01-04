grammar MiniC;

program
    : statement* EOF
    ;

statement
    : block
    | SEMI
    | assignment
    | declaration
    | 'if' parExpression statement ('else' statement)?
    | 'while' parExpression statement
    | 'break' SEMI
    | 'exit' SEMI
    | 'print' parExpression SEMI
    | 'println' parExpression SEMI
    ;

block
    : '{' statement* '}'
    ;

expression
    : literal
    | Identifier
    | ('!' | '-') expression
    | expression ('*' | '/' | '%') expression
    | expression ('+' | '-') expression
    | expression ('==' | '!=') expression
    | expression ('<' | '>' | '<=' || '>=') expression
    | expression ('&&') expression
    | expression ('||') expression
    | parExpression
    | 'readInt()'
    | 'readDouble()'
    | 'readLine()'
    | 'toString' parExpression
    ;

parExpression : '(' expression ')';

assignment : Identifier assignmentOp expression SEMI;

declaration : type Identifier (assignmentOp expression)? SEMI;

assignmentOp : '=';

type : INT_TYPE
     | DOUBLE_TYPE
     | BOOL_TYPE
     | STRING_TYPE
     ;

literal : IntegerLiteral
        | FloatingPointLiteral
        | StringLiteral
        | BooleanLiteral
        ;

// lexer rules (starting with uppercase)

IntegerLiteral : DIGIT+;
FloatingPointLiteral : DIGIT+ '.' DIGIT+;
StringLiteral : '"' (ESC | ~["\\])* '"' ;
BooleanLiteral : 'true' | 'false';

SEMI : ';';

fragment
DIGIT : '0'..'9';

fragment
LETTER : ('a'..'z' | 'A'..'Z');

fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;


WS  :  [ \t\r\n\u000C]+ -> skip
    ;

// tokens, needed to be able to be able to reference them via constants

IF_KEYWORD: 'if';
ELSE_KEYWORD: 'else';
WHILE_KEYWORD: 'while';
BREAK_KEYWORD: 'break';
CONTINUE_KEYWORD: 'continue';
EXIT_KEYWORD: 'exit';

INT_TYPE: 'int';
DOUBLE_TYPE: 'double';
STRING_TYPE: 'string';
BOOL_TYPE: 'bool';

MUL : '*';
DIV : '/';
PLUS : '+';
MINUS : '-';
MOD : '%';
LT : '<';
GT : '>';
LTEQ : '<=';
GTEQ : '>=';
ASSIGN : '=';
EQ : '==';
NOTEQ : '!=';
NOT : '!';
AND : '&&';
OR : '||';
LPAR: '(';
RPAR: ')';
LBRACE: '{';
RBRACE: '}';

// must be last, otherwise some tokens like types, keywords may be incorrectly recognized as identifiers
Identifier : (LETTER | '_') (LETTER | DIGIT | '_')* ;
