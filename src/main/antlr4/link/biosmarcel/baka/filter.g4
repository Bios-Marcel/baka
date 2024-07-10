grammar filter;

query:                     expression? EOF;
expression:                field operator=(LT | LT_EQ | GT | GT_EQ | EQ | NOT_EQ1) value     #comparatorExpression
                           | expression operator=(AND | OR) expression                       #binaryExpression
                           | OPEN_PAR expression CLOSE_PAR                                   #groupedExpression
                           ;

field:                     IDENTIFIER;
value:                     STRING_LITERAL
                           ;

AND:                       A N D;
OR:                        O R;
OPEN_PAR:                  '(';
CLOSE_PAR:                 ')';
LT:                        '<';
LT_EQ:                     '<=';
GT:                        '>';
GT_EQ:                     '>=';
EQ:                        '=';
NOT_EQ1:                   '!=';
IDENTIFIER:                [a-z_]+;
STRING_LITERAL:            QUOTE.+?QUOTE;
SPACES:                    [ \u000B\t\r\n] -> channel(HIDDEN);

fragment QUOTE:            ["];
fragment DIGIT:            [0-9];
fragment A:                [aA];
fragment B:                [bB];
fragment C:                [cC];
fragment D:                [dD];
fragment E:                [eE];
fragment F:                [fF];
fragment G:                [gG];
fragment H:                [hH];
fragment I:                [iI];
fragment J:                [jJ];
fragment K:                [kK];
fragment L:                [lL];
fragment M:                [mM];
fragment N:                [nN];
fragment O:                [oO];
fragment P:                [pP];
fragment Q:                [qQ];
fragment R:                [rR];
fragment S:                [sS];
fragment T:                [tT];
fragment U:                [uU];
fragment V:                [vV];
fragment W:                [wW];
fragment X:                [xX];
fragment Y:                [yY];
fragment Z:                [zZ];