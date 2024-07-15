package link.biosmarcel.baka.filter;

import link.biosmarcel.baka.FilterLexer;
import link.biosmarcel.baka.FilterParser;
import link.biosmarcel.baka.FilterParserBaseListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Filter<FilterTarget> implements Predicate<FilterTarget> {
    private @Nullable Expression<FilterTarget> query;

    final Map<String, Map<Operator, FieldData<FilterTarget, ?>>> fieldToOperatorToExtractor = new HashMap<>();

    public <ValueType> void register(
            final String field,
            final Operator operator,
            final FilterTargetPredicate<FilterTarget, ValueType> predicate,
            final Function<String, ValueType> convertString) {
        fieldToOperatorToExtractor
                .computeIfAbsent(field, _ -> new HashMap<>(/* FIXME Fixed Operator Size*/))
                .put(operator, new FieldData<>(predicate, convertString));
    }

    public boolean setQuery(final String textQuery) {
        try {
            _setQuery(textQuery);
            return true;
        } catch (final RuntimeException exception) {
            System.out.println("Failed to compile query: " + exception.getMessage());
            return false;
        }
    }

    private void _setQuery(final String textQuery) {
        final var lexer = new FilterLexer(CharStreams.fromString(textQuery));
        final var parser = new FilterParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        final var parsedQuery = parser.query();

        ThreadLocal<Expression<FilterTarget>> root = new ThreadLocal<>();
        final Map<ParseTree, Expression<FilterTarget>> contextToExpression = new HashMap<>();

        ParseTreeWalker.DEFAULT.walk(new FilterParserBaseListener() {
            @Override
            public void enterQuery(final FilterParser.QueryContext ctx) {
                if (ctx.getRuleContext().getChildCount() == 2) {
                    final Expression<FilterTarget> expression = new Expression<>();
                    root.set(expression);
                    contextToExpression.put(ctx, expression);
                }
            }

            private void insertPredicate(final Expression<FilterTarget> expression, final Predicate<FilterTarget> predicate) {
                if (expression.predicate == null) {
                    expression.predicate = predicate;
                } else if (expression.next == null) {
                    expression.next = predicate;
                } else {
                    throw new IllegalStateException("No space left");
                }
            }

            @Override
            public void enterGroupedExpression(final FilterParser.GroupedExpressionContext ctx) {
                for (final var child : ctx.children) {
                    if (child instanceof ErrorNodeImpl) {
                        throw new IllegalStateException("Query incomplete");
                    }
                }

                var targetContext = contextToExpression.get(ctx.parent);
                final var expression = new Expression<FilterTarget>();
                contextToExpression.put(ctx, expression);
                insertPredicate(targetContext, expression);
            }

            @Override
            public void enterBinaryExpression(final FilterParser.BinaryExpressionContext ctx) {
                for (final var child : ctx.children) {
                    if (child instanceof ErrorNodeImpl) {
                        throw new IllegalStateException("Query incomplete");
                    }
                }

                final var expression = new Expression<FilterTarget>();
                if (ctx.operator.getText().equalsIgnoreCase("AND")) {
                    expression.binaryExpressionType = BinaryExpressionType.AND;
                } else if (ctx.operator.getText().equalsIgnoreCase("OR")) {
                    expression.binaryExpressionType = BinaryExpressionType.OR;
                } else {
                    throw new RuntimeException("Unknown Binary Operator");
                }

                var targetContext = contextToExpression.get(ctx.parent);

                contextToExpression.put(ctx, expression);
                insertPredicate(targetContext, expression);
            }

            @Override
            public void enterComparatorExpression(final FilterParser.ComparatorExpressionContext ctx) {
                for (final var child : ctx.children) {
                    if (child instanceof ErrorNodeImpl) {
                        throw new IllegalStateException("Query incomplete");
                    }
                }

                final var targetContext = contextToExpression.get(ctx.parent);
                final var operatorToExtractor = fieldToOperatorToExtractor.get(ctx.field().getText());
                if (operatorToExtractor == null) {
                    throw new IllegalStateException("Field not registered: " + ctx.field().getText());
                }

                if (ctx.children.size() <= 1) {
                    throw new IllegalStateException("missing operator");
                }

                final Operator operator = switch (ctx.operator.getText()) {
                    case "=" -> Operator.EQ;
                    case "!=" -> Operator.NOT_EQ;
                    case ">" -> Operator.GT;
                    case "<" -> Operator.LT;
                    case ">=" -> Operator.GT_EQ;
                    case "<=" -> Operator.LT_EQ;
                    case "contains", "has" -> Operator.HAS;
                    default -> throw new UnsupportedOperationException("Unknown operator: " + ctx.operator.getText());
                };
                final FieldData<FilterTarget, Object> extractor = (FieldData<FilterTarget, Object>) operatorToExtractor.get(operator);
                if (extractor == null) {
                    throw new IllegalStateException("Extractor not registered: " + ctx.field().getText() + "." + ctx.operator.getText());
                }

                final var value = ctx.value();
                String unquoted = value.getText();
                if (unquoted.isBlank()) {
                    throw new IllegalStateException("missing value");
                }

                final var stringToken = value.STRING();
                if (stringToken != null) {
                    unquoted = stringToken.getText().substring(1, stringToken.getText().length() - 1);
                }

                final var converted = extractor.convertString.apply(unquoted);
                final Predicate<FilterTarget> predicate = x -> extractor.operate.test(x, converted);
                insertPredicate(targetContext, predicate);
            }
        }, parsedQuery);

        query = root.get();
    }

    public boolean test(final FilterTarget target) {
        if (query == null) {
            return true;
        }

        return query.test(target);
    }
}
