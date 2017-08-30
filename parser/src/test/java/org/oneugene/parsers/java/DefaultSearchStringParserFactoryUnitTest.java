package org.oneugene.parsers.java;

import org.codehaus.jparsec.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DefaultSearchStringParserFactoryUnitTest {
    private DefaultSearchStringParserFactory valueToTest;

    @Before
    public void setUp() {
        valueToTest = new DefaultSearchStringParserFactory();
    }

    @Test
    public void testAttribute_when_correctAttributeName_than_parseWithoutErrors() {
        Parser<String> attributeParser = valueToTest.attribute();


        String parsed = attributeParser.parse(" \t\r\nQwerty12 ");

        Assert.assertEquals("Qwerty12", parsed);
    }

    @Test
    public void testValueString_when_correctString_than_parseWithoutErrors() {
        Parser<String> valueStringParser = valueToTest.valueString();


        String parsed = valueStringParser.parse("\"qwe\\r\\n\\\"rty\"");
        System.out.println(parsed);

        Assert.assertEquals("qwe\r\n\"rty", parsed);
    }

    @Test
    public void testValueCouple_when_correctString_than_parseWithoutErrors() {
        Parser<List<String>> valueCoupleParser = valueToTest.valueCouple();

        List<String> parsed = valueCoupleParser.parse("( \"one\" , \"two\" )");
        System.out.println(parsed);

        Assert.assertEquals(2, parsed.size());
        Assert.assertEquals("one", parsed.get(0));
        Assert.assertEquals("two", parsed.get(1));
    }

    @Test
    public void testValueList_when_correctString_than_parseWithoutErrors() {
        Parser<List<String>> valueListParser = valueToTest.valueList();

        List<String> parsed = valueListParser.parse("( \"one\", \"two\" , \"tree\" )");
        System.out.println(parsed);

        Assert.assertEquals(3, parsed.size());
        Assert.assertEquals("one", parsed.get(0));
        Assert.assertEquals("two", parsed.get(1));
        Assert.assertEquals("tree", parsed.get(2));

        parsed = valueListParser.parse("()");
        System.out.println(parsed);

        Assert.assertEquals(0, parsed.size());
    }

    @Test
    public void testLikeParser_when_correctString_than_parseWithoutErrors() {
        Parser<ValueOpPredicate> likeParser = valueToTest.likeParser();

        ValueOpPredicate parsed = likeParser.parse("\tA1 LIKE \"one\" ");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals("LIKE", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one"), parsed.getValues());
    }

    @Test
    public void testEqParser_when_correctString_than_parseWithoutErrors() {
        Parser<ValueOpPredicate> eqParser = valueToTest.eqParser();

        ValueOpPredicate parsed = eqParser.parse("\tA1 = \"one\" ");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals("=", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one"), parsed.getValues());

        parsed = eqParser.parse("\tA1 = (\"one\",\"two\",\"three\")");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals("=", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one", "two", "three"), parsed.getValues());
    }

    @Test
    public void testGeParser_when_correctString_than_parseWithoutErrors() {
        Parser<ValueOpPredicate> geParser= valueToTest.geParser();

        ValueOpPredicate parsed = geParser.parse("\tA1 >= \"one\" ");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals(">=", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one"), parsed.getValues());
    }

    @Test
    public void testLeParser_when_correctString_than_parseWithoutErrors() {
        Parser<ValueOpPredicate> leParser = valueToTest.leParser();

        ValueOpPredicate parsed = leParser.parse("\tA1 <= \"one\" ");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals("<=", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one"), parsed.getValues());
    }

    @Test
    public void testBetweenParser_when_correctString_than_parseWithoutErrors() {
        Parser<ValueOpPredicate> betweenParser = valueToTest.betweenParser();

        ValueOpPredicate parsed = betweenParser.parse("\tA1 BETWEEN (\"one\", \"two\") ");
        System.out.println(parsed);

        Assert.assertEquals("A1", parsed.getAttribute());
        Assert.assertEquals("BETWEEN", parsed.getOperation());
        Assert.assertEquals(Arrays.asList("one", "two"), parsed.getValues());
    }

    @Test
    public void testNotParser_when_correctString_than_parseWithoutErrors() {
        Parser<CompositePredicate> notParser = valueToTest.notParser();

        CompositePredicate parsed = notParser.parse("\t NOT ( A1 BETWEEN (\"one\", \"two\") )");
        System.out.println(parsed);

        Assert.assertEquals("NOT", parsed.getOperation());
        Assert.assertEquals(1, parsed.getChildren().size());

        ValueOpPredicate between = (ValueOpPredicate) parsed.getChildren().iterator().next();
        Assert.assertEquals("A1", between.getAttribute());
        Assert.assertEquals("BETWEEN", between.getOperation());
        Assert.assertEquals(Arrays.asList("one", "two"), between.getValues());
    }

    @Test
    public void testExpressionParser_check_andIsMoreImportantThanOr() {
        Parser<Predicate> expressionParser = valueToTest.expression();

        CompositePredicate parsed = (CompositePredicate) expressionParser.parse(" q = \"1\" AND c=\"2\" OR r=\"3\" AND u=\"4\" ");

        Assert.assertEquals("OR", parsed.getOperation());
        Assert.assertEquals(2, parsed.getChildren().size());
        for (Predicate p : parsed.getChildren()) {
            CompositePredicate sub = (CompositePredicate) p;
            Assert.assertEquals("AND", sub.getOperation());
        }
    }

    @Test
    public void testExpressionParser_check_ParenthesisShouldSetEvaluationOrder() {
        Parser<Predicate> expressionParser = valueToTest.expression();

        {
            CompositePredicate parsed = (CompositePredicate) expressionParser.parse(" q = \"1\" OR c=\"2\" AND r=\"3\" ");
            System.out.println(parsed);

            Assert.assertEquals("OR", parsed.getOperation());
            Assert.assertEquals(2, parsed.getChildren().size());
            CompositePredicate second = (CompositePredicate) parsed.getChildren().get(1);
            Assert.assertEquals("AND", second.getOperation());
        }
        {
            CompositePredicate parsed = (CompositePredicate) expressionParser.parse(" ( q = \"1\" OR c = \"2\" ) AND r = \"3\" ");
            System.out.println(parsed);

            Assert.assertEquals("AND", parsed.getOperation());
            Assert.assertEquals(2, parsed.getChildren().size());
            CompositePredicate second = (CompositePredicate) parsed.getChildren().get(0);
            Assert.assertEquals("OR", second.getOperation());
        }
    }
}
