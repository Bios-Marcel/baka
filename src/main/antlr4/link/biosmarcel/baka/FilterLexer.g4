lexer grammar FilterLexer;

options {
    caseInsensitive = true;
}

HAS:                       'has';
AND:                       'and';
OR:                        'or';
OPEN_PAR:                  '(';
CLOSE_PAR:                 ')';
LT:                        '<';
LT_EQ:                     '<=';
GT:                        '>';
GT_EQ:                     '>=';
EQ:                        '=';
NOT_EQ:                    '!=';
STRING:                    ["].*?["];
BOOLEAN:                   ('true'|'false');
NUMBER:                    [,.0-9]+;
WORD:                      ~[() \u000B\t\r\n"]+;
SPACES:                    [ \u000B\t\r\n] -> channel(HIDDEN);