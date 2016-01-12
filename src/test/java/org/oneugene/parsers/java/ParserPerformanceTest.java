package org.oneugene.parsers.java;

import org.codehaus.jparsec.Parser;
import org.junit.Before;
import org.junit.Test;

public class ParserPerformanceTest {
    private DefaultSearchStringParserFactory valueToTest;

    @Before
    public void setUp() {
        valueToTest = new DefaultSearchStringParserFactory();
    }

    @Test
    public void testPerformance() {
        Parser<Predicate> expressionParser = valueToTest.expression();
        String toParse = "q = \"1\" AND c<=\"2\" OR r>=\"3\" AND u BETWEEN (\"4\", \"5\") AND (qwe <> \"tryrty\" OR l45 <= \"asd\")";
        int iters = 1000000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iters; i += 1) {
            Predicate res = expressionParser.parse(toParse);
//            System.out.println(res);

        }
        long end = System.currentTimeMillis();
        System.out.println(iters + " took " + (end - start) + " ms, " + (1.0 * iters / (end - start)) + " per ms");
    }
}
