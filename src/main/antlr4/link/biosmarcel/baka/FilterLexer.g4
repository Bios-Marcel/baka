lexer grammar FilterLexer;

options {
    caseInsensitive = true;
}

AND:                       'and';
OR:                        'or';
OPEN_PAR:                  '(';
CLOSE_PAR:                 ')';
LT:                        '<';
LT_EQ:                     '<=';
GT:                        '>';
GT_EQ:                     '>=';
EQ:                        '=';
NOT_EQ1:                   '!=';
IDENTIFIER:                [a-z_]+;
STRING_LITERAL:            ["].+?["];
SPACES:                    [ \u000B\t\r\n] -> channel(HIDDEN);
