package link.biosmarcel.baka.filter;

import link.biosmarcel.baka.FilterLexer;
import link.biosmarcel.baka.FilterParser;
import link.biosmarcel.baka.FilterParserBaseListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class Filter<FilterTarget> implements Predicate<FilterTarget> {
    private @Nullable Expression<FilterTarget> query;

    final Map<String, Map<String /* FIXME Custom Compare Operator Type*/, Function<FilterTarget, Comparable<?>>>> fieldToOperatorToExtractor = new HashMap<>();

    public void register(
            final String field,
            final String operator,
            final Function<FilterTarget, Comparable<?>> extractor) {
        fieldToOperatorToExtractor
                .computeIfAbsent(field, _ -> new HashMap<>(/* FIXME Fixed Operator Size*/))
                .put(operator, extractor);
    }

    public void setQuery(final String textQuery) {
        final var lexer = new FilterLexer(CharStreams.fromString(textQuery));
        final var parser = new FilterParser(new CommonTokenStream(lexer));
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
                var targetContext = contextToExpression.get(ctx.parent);
                final var expression = new Expression<FilterTarget>();
                contextToExpression.put(ctx, expression);
                insertPredicate(targetContext, expression);
            }

            @Override
            public void enterBinaryExpression(final FilterParser.BinaryExpressionContext ctx) {
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
                final var targetContext = contextToExpression.get(ctx.parent);

                final var operatorToExtractor = fieldToOperatorToExtractor.get(ctx.field().getText());
                if (operatorToExtractor == null) {
                    throw new IllegalStateException("Field not registered: " + ctx.field().getText());
                }
                final var extractor = operatorToExtractor.get(ctx.operator.getText());
                if (extractor == null) {
                    throw new IllegalStateException("Extractor not registered: " + ctx.field().getText() + "." + ctx.operator.getText());
                }
                final Predicate<FilterTarget> predicate = switch (ctx.operator.getText()) {
                    case "=" -> x -> {
                        Comparable<String> apply = (Comparable<String>) extractor.apply(x);
                        return apply.compareTo(ctx.value().getText().substring(1, ctx.value().getText().length() - 1)) == 0;
                    };
                    case "!=" -> x -> {
                        Comparable<String> apply = (Comparable<String>) extractor.apply(x);
                        return apply.compareTo(ctx.value().getText().substring(1, ctx.value().getText().length() - 1)) != 0;
                    };
                    default -> throw new UnsupportedOperationException("operator not yet support");
                };

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
