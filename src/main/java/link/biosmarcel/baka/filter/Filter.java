package link.biosmarcel.baka.filter;

import link.biosmarcel.baka.FilterLexer;
import link.biosmarcel.baka.FilterParser;
import link.biosmarcel.baka.FilterParserBaseListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@NullMarked
public class Filter<FilterTarget> implements Predicate<FilterTarget> {
    private @Nullable Expression<FilterTarget> query;

    final Map<String, Map<Operator, FieldData<FilterTarget, ?>>> fieldToOperatorToExtractor = new HashMap<>();

    public <ValueType> void register(
            final String field,
            final Operator operator,
            final FilterTargetPredicate<FilterTarget, ValueType> predicate,
            final Function<String, ValueType> convertString) {
        fieldToOperatorToExtractor
                .computeIfAbsent(field, _ -> new HashMap<>())
                .put(operator, new FieldData<>(predicate, convertString));
    }

    public void setQuery(final String textQuery) {
        final var lexer = new FilterLexer(CharStreams.fromString(textQuery));
        final var parser = new FilterParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        final var parsedQuery = parser.query();

        ThreadLocal<Expression<FilterTarget>> root = new ThreadLocal<>();
        final Map<ParseTree, Expression<FilterTarget>> contextToExpression = new HashMap<>();

        ParseTreeWalker.DEFAULT.walk(new FilterParserBaseListener() {
            @Override
            public void enterQuery(final FilterParser.QueryContext ctx) {
                if (textQuery.isBlank()) {
                    throw new IncompleteQueryException(true, "", fieldToOperatorToExtractor.keySet().stream().toList());
                }

                final Expression<FilterTarget> expression = new Expression<>();
                root.set(expression);
                contextToExpression.put(ctx, expression);
            }

            @Override
            public void visitErrorNode(final ErrorNode node) {
                throw new IllegalStateException("Unknown token '%s'".formatted(node.getText()));
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
                if (ctx.getChild(1).getText().isBlank()) {
                    throw new IncompleteQueryException(false, "", fieldToOperatorToExtractor.keySet().stream().toList());
                }

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
                    throw new IllegalStateException("Unsupported binary operator '%s'".formatted(ctx.operator.getText()));
                }

                if (ctx.getChildCount() == 3 && ctx.getChild(2).getText().isBlank()) {
                    throw new IncompleteQueryException(false, "", fieldToOperatorToExtractor.keySet().stream().toList());
                }

                var targetContext = contextToExpression.get(ctx.parent);

                contextToExpression.put(ctx, expression);
                insertPredicate(targetContext, expression);
            }

            private List<String> autocompleteOperators(final Collection<Operator> operators, final String text) {
                final String textLowered = text.toLowerCase();
                return operators
                        .stream()
                        .filter(operator -> operator.text.toLowerCase().startsWith(textLowered))
                        .map(op -> op.text)
                        .sorted()
                        .toList();
            }

            @Override
            public void enterComparatorExpression(final FilterParser.ComparatorExpressionContext ctx) {
                final var targetContext = contextToExpression.get(ctx.parent);
                final var field = ctx.field().getText();
                final var lowercaseField = field.toLowerCase();
                final var operatorToExtractor = fieldToOperatorToExtractor.get(lowercaseField);
                if (operatorToExtractor == null) {
                    final List<String> options = fieldToOperatorToExtractor.keySet()
                            .stream()
                            .filter(key -> key.startsWith(lowercaseField))
                            .toList();
                    if (options.isEmpty()) {
                        throw new IllegalStateException("Unsupported field '%s'".formatted(field));
                    }

                    throw new IncompleteQueryException(false,
                            ctx.field().getText(),
                            options);
                }

                if (ctx.children.size() <= 1) {
                    throw new IncompleteQueryException(false, "", autocompleteOperators(operatorToExtractor.keySet(), ""));
                }

                final Operator operator = Operator.match(ctx.operator.getText());
                if (operator == null) {
                    String operatorText = ctx.getChild(1).getText();
                    final List<String> options = autocompleteOperators(operatorToExtractor.keySet(), operatorText);
                    if (options.isEmpty()) {
                        throw new IllegalStateException("Unsupported operator '%s' for field %s".formatted(operatorText, field));
                    }

                    throw new IncompleteQueryException(
                            false,
                            operatorText,
                            options);
                }

                final FieldData<FilterTarget, Object> extractor = (FieldData<FilterTarget, Object>) operatorToExtractor.get(operator);
                if (extractor == null) {
                    throw new IllegalStateException("Extractor not registered: " + ctx.field().getText() + "." + ctx.operator.getText());
                }

                final var value = ctx.value();
                String unquoted = value.getText();
                if (unquoted.isBlank()) {
                    throw new IncompleteQueryException(false, unquoted, Collections.EMPTY_LIST);
                }

                final var stringToken = value.STRING();
                //noinspection ConstantValue
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
