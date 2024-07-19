parser grammar FilterParser;

options {
    tokenVocab = FilterLexer;
}

query:                     expression? EOF;
expression:                field operator=(LT | LT_EQ | GT | GT_EQ | EQ | NOT_EQ | HAS) value     #comparatorExpression
                           | OPEN_PAR expression CLOSE_PAR                                        #groupedExpression
                           | expression operator=(AND | OR) expression                            #binaryExpression
                           ;

field:                     WORD;
value:                     STRING
                           | BOOLEAN
                           | NUMBER
                           | WORD
                           ;
