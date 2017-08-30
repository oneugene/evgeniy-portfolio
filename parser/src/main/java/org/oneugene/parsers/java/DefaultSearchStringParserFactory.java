package org.oneugene.parsers.java;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.functors.Tuple3;
import org.codehaus.jparsec.pattern.Patterns;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultSearchStringParserFactory implements SearchStringParserFactory {

    private <T> Parser<T> whitespaces(Parser<T> p) {
        return p.between(Scanners.WHITESPACES.skipMany(), Scanners.WHITESPACES.skipMany());
    }
    private Parser<Void> parenthesisOpen(){
        return whitespaces(Scanners.isChar('('));
    }
    private Parser<Void> parenthesisClose(){
        return whitespaces(Scanners.isChar(')' ));
    }

    public Parser<String> attribute() {
        return whitespaces(Patterns
                .regex("[A-Za-z]\\w*")
                .toScanner("attribute").source());
    }

    public Parser<String> valueString() {
        return whitespaces(Scanners.DOUBLE_QUOTE_STRING.map(new Map<String, String>() {
            public String map(String s) {
                String unescaped = StringEscapeUtils.unescapeJava(s);
                return unescaped.substring(1, unescaped.length() - 1);
            }
        }));
    }

    public Parser<List<String>> valueSingleton() {
        return valueString().map(new Map<String, List<String>>() {
            public List<String> map(String s) {
                return Collections.singletonList(s);
            }
        });
    }

    public Parser<List<String>> valueCouple() {
        return Parsers.pair(parenthesisOpen().next(valueString()), valueString().between(Scanners.isChar(','), parenthesisClose()))
                .map(new Map<Pair<String, String>, List<String>>() {
                    public List<String> map(Pair<String, String> pair) {
                        return Arrays.asList(pair.a, pair.b);
                    }
                });
    }

    public Parser<List<String>> valueList() {
        return valueString().sepBy(Scanners.isChar(',')).between(parenthesisOpen(), parenthesisClose());
    }

    public Parser<ValueOpPredicate> likeParser() {
        return Parsers.tuple(attribute(), Scanners.string("LIKE").source(), valueSingleton())
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> leParser() {
        return Parsers.tuple(attribute(), Scanners.string("<=").source(), valueSingleton())
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> geParser() {
        return Parsers.tuple(attribute(), Scanners.string(">=").source(), valueSingleton())
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> eqParser() {
        return Parsers.tuple(attribute(), Scanners.string("=").source(), Parsers.or(valueSingleton(), valueList()))
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> neParser() {
        return Parsers.tuple(attribute(), Scanners.string("<>").source(), Parsers.or(valueSingleton(), valueList()))
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> betweenParser() {
        return Parsers.tuple(attribute(), Scanners.string("BETWEEN").source(), valueCouple())
                .map(new ValueMap());
    }

    public Parser<ValueOpPredicate> valueOpParsers() {
        return Parsers.or(eqParser(), likeParser(), leParser(), geParser(), neParser(), betweenParser());
    }

    private Parser.Reference<CompositePredicate> notParserRef() {
        Parser.Reference<CompositePredicate> ref = Parser.newReference();
        Parser<CompositePredicate> result = Parsers.tuple(
                whitespaces(Scanners.string("NOT").source()),
                allPredicateParsers2(ref.lazy())).map(new Map<Pair<String, Predicate>, CompositePredicate>() {
            public CompositePredicate map(Pair<String, Predicate> pair) {
                return new CompositePredicate.Builder()
                        .setOperation(pair.a)
                        .setChildren(Collections.singletonList(pair.b))
                        .build();
            }
        });
        ref.set(result);
        return ref;
    }

    public Parser<CompositePredicate> notParser() {
        return notParserRef().lazy();
    }

    private Parser<Predicate> allPredicateParsers(Parser<CompositePredicate> lazyNotParser) {
        return Parsers.or(valueOpParsers(), lazyNotParser);
    }

    private Parser<Predicate> allPredicateParsers2(Parser<CompositePredicate> lazyNotParser) {
        return Parsers.or(allPredicateParsers(lazyNotParser), Parsers.between(parenthesisOpen(), allPredicateParsers(lazyNotParser), parenthesisClose()));
    }

    private Parser.Reference<Predicate> factor(Parser<Predicate> lazyExpr) {
        Parser.Reference<Predicate> ref = Parser.newReference();
        Parser.Reference<CompositePredicate> notParserRef = notParserRef();
        Parser<Predicate> result = Parsers.or(allPredicateParsers2(notParserRef.lazy()), lazyExpr
                .between(parenthesisOpen(), parenthesisClose()));
        ref.set(result);
        return ref;
    }

    private Parser.Reference<Pair<String, Predicate>> subAnd(Parser<Predicate> lazyExpr) {
        Parser.Reference<Pair<String, Predicate>> ref = Parser.newReference();
        Parser.Reference<Predicate> factorRef = factor(lazyExpr);
        Parser<Pair<String, Predicate>> subAnd = Parsers.tuple(
                Scanners.string("AND").source(),
                factorRef.lazy()
        );
        ref.set(subAnd);
        return ref;
    }

    private Parser.Reference<Predicate> and(Parser<Predicate> lazyExpr) {
        Parser.Reference<Predicate> ref = Parser.newReference();
        Parser.Reference<Pair<String, Predicate>> subAnd = subAnd(lazyExpr);
        Parser.Reference<Predicate> factorRef = factor(lazyExpr);
        Parser<Pair<Predicate, List<Pair<String, Predicate>>>> tuple = Parsers.tuple(factorRef.lazy(), subAnd.lazy().many());
        Parser<Predicate> result = tuple.map(new AndOrMap());
        ref.set(result);
        return ref;
    }

    private Parser.Reference<Pair<String, Predicate>> subOr(Parser<Predicate> lazyExpr) {
        Parser.Reference<Pair<String, Predicate>> ref = Parser.newReference();
        Parser.Reference<Predicate> andRef = and(lazyExpr);
        Parser<Pair<String, Predicate>> subOr = Parsers.tuple(
                Scanners.string("OR").source(),
                andRef.lazy()
        );
        ref.set(subOr);
        return ref;
    }

    public Parser<Predicate> expression() {
        Parser.Reference<Predicate> lazyExpr = Parser.newReference();
        Parser.Reference<Predicate> and = and(lazyExpr.lazy());
        Parser.Reference<Pair<String, Predicate>> subOr = subOr(lazyExpr.lazy());
        Parser<Predicate> result = Parsers.tuple(and.lazy(), subOr.lazy().many()).map(new AndOrMap());
        lazyExpr.set(result);
        return lazyExpr.get();
    }

    private static class ValueMap implements Map<Tuple3<String, String, List<String>>, ValueOpPredicate> {
        public ValueOpPredicate map(Tuple3<String, String, List<String>> tuple) {
            return new ValueOpPredicate.Builder().setAttribute(tuple.a).setOperation(tuple.b).setValues(tuple.c).build();
        }
    }

    private static class AndOrMap implements Map<Pair<Predicate, List<Pair<String, Predicate>>>, Predicate> {
        public Predicate map(Pair<Predicate, List<Pair<String, Predicate>>> predicateListPair) {
            Predicate acc = predicateListPair.a;
            for (Pair<String, Predicate> pair : predicateListPair.b) {
                acc = new CompositePredicate.Builder().setOperation(pair.a).setChildren(Arrays.asList(acc, pair.b)).build();
            }
            return acc;
        }
    }
}
