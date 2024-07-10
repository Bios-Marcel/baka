parser grammar FilterParser;

options {
    tokenVocab = FilterLexer;
}

query:                     expression? EOF;
expression:                field operator=(LT | LT_EQ | GT | GT_EQ | EQ | NOT_EQ1) value     #comparatorExpression
                           | expression operator=(AND | OR) expression                       #binaryExpression
                           | OPEN_PAR expression CLOSE_PAR                                   #groupedExpression
                           ;

field:                     IDENTIFIER;
value:                     STRING_LITERAL
                           ;
