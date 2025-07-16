grammar Tuga;

@header {
    package Tuga;
}

program     : (var_declaration)* (function_declaration)+ EOF ;

var_declaration : ID (',' ID)* ':' tipo SEMICOLON ;

function_declaration : 'funcao' ID '(' (param_list)? ')' (':' tipo)? block              #FunctionDeclaration;

param_list  : param (',' param)* ;
param       : ID ':' tipo ;

tipo        : 'inteiro' | 'real' | 'booleano' | 'string' ;

statement   : print
            | assignment
            | while
            | if
            | return
            | function_call
            | empty
            | block
            ;

print       : PRINT expr SEMICOLON                                                      # PrintStatement ;
assignment  : ID '<-' expr SEMICOLON                                                    # AssignmentStatement ;
while       : WHILE LPAREN expr RPAREN (statement | block)                              # WhileStatement ;
if          : IF LPAREN expr RPAREN thenBody=body (ELSE elseBody=body)?                 # IfStatement ;
return      : 'retorna' expr? SEMICOLON                                                 # ReturnStatement ;
function_call : ID '(' (expr (',' expr)*)? ')' SEMICOLON                                # FunctionCall ;
empty       : SEMICOLON                                                                 # EmptyStatement ;
block       : INICIO (var_declaration)* (statement)* FIM                                # BlockStatement ;

body        : statement | block ;

expr        : LPAREN expr RPAREN                                        # ParenExpr
            | (MINUS | NAO) expr                                        # UnaryExpr
            | expr op=(MUL | DIV | MOD) expr                            # MulDivModExpr
            | expr op=(PLUS | MINUS) expr                               # AddSubExpr
            | expr op=(MENOR | MAIOR | MENORIGUAL | MAIORIGUAL) expr    # RelationalExpr
            | expr op=(IGUAL | DIFERENTE) expr                          # EqualityExpr
            | expr E expr                                               # AndExpr
            | expr OU expr                                              # OrExpr
            | INT                                                       # IntLiteral
            | DOUBLE                                                    # DoubleLiteral
            | BOOLEAN                                                   # BoolLiteral
            | STRING                                                    # StringLiteral
            | ID                                                        # VarExpr
            | function_call                                             # FunctionCallExpr
            ;

// Key Words
BOOLEAN     : 'verdadeiro' | 'falso';
PRINT       : 'escreve';
INICIO      : 'inicio';
FIM         : 'fim';
WHILE       : 'enquanto';
IF          : 'se';
ELSE        : 'senao';

// Operators
PLUS        : '+' ;
MINUS       : '-' ;
MUL         : '*' ;
DIV         : '/' ;
MOD         : '%' ;
E           : 'e' ;
OU          : 'ou' ;
NAO         : 'nao' ;
MENOR       : '<' ;
MAIOR       : '>' ;
MENORIGUAL  : '<=' ;
MAIORIGUAL  : '>=' ;
IGUAL       : 'igual' ;
DIFERENTE   : 'diferente' ;

// Extra
LPAREN  : '(' ;
RPAREN  : ')' ;
SEMICOLON : ';' ;

// Literals and ID
INT         : [0-9]+ ;
DOUBLE      : [0-9]+ '.' [0-9]+ ;
STRING      : '"' .*? '"' ;
ID          : [a-zA-Z_][a-zA-Z0-9_]* ;

// WhiteSpaces and Comments
WS          : [ \t\r\n]+ -> skip;
SL_COMMENT  : '//' .*? (EOF|'\n') -> skip;
ML_COMMENT  : '/*' .*? '*/' -> skip;