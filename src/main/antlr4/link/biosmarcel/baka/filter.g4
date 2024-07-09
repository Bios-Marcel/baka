grammar filter;

query:                      expression? EOF;
expression :                field operator=(LT | LT_EQ | GT | GT_EQ | EQ | NOT_EQ1) value     #comparatorExpression
                            | expression operator=(AND | OR) expression                       #binaryExpression
                            | OPEN_PAR expression CLOSE_PAR                                   #groupedExpression
                            ;

field :                     IDENTIFIER;
value:                      STRING_LITERAL
                            ;

STRING_LITERAL:             QUOTE.+?QUOTE;
IDENTIFIER :                [a-z_]+;
OPEN_PAR :                  '(';
CLOSE_PAR :                 ')';
LT :                        '<';
LT_EQ :                     '<=';
GT :                        '>';
GT_EQ :                     '>=';
EQ :                        '=';
NOT_EQ1 :                   '!=';
BETWEEN :                   B E T W E E N;
AND :                       A N D;
OR :                        O R;
NOT :                       N O T;
IN :                        I N;
LIKE :                      L I K E;
SPACES:                     [ \u000B\t\r\n] -> channel(HIDDEN);
UNEXPECTED_CHAR:            . ;

fragment QUOTE :            ["];
fragment DIGIT :            [0-9];
fragment A :                [aA];
fragment B :                [bB];
fragment C :                [cC];
fragment D :                [dD];
fragment E :                [eE];
fragment F :                [fF];
fragment G :                [gG];
fragment H :                [hH];
fragment I :                [iI];
fragment J :                [jJ];
fragment K :                [kK];
fragment L :                [lL];
fragment M :                [mM];
fragment N :                [nN];
fragment O :                [oO];
fragment P :                [pP];
fragment Q :                [qQ];
fragment R :                [rR];
fragment S :                [sS];
fragment T :                [tT];
fragment U :                [uU];
fragment V :                [vV];
fragment W :                [wW];
fragment X :                [xX];
fragment Y :                [yY];
fragment Z :                [zZ];