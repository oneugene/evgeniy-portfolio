package com.epam.ievgenii_onyshchenko.comonad;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class ComonadTest {
    private static int SPLIT_SIZE = 2;
    private static int TOTAL_SIZE = 3;
    private static int VALUES_TO_CHECK = 1000;
    private long seed ;

    @Before
    public void setUp(){
        seed = System.nanoTime();
    }

    @Test
    public void testExtendWithRemainder(){
        Iterator<Integer> iterator = IntStream.range(0, TOTAL_SIZE).iterator();

        Comonad<Optional<Integer>> comonad = new IteratorComonad<>(iterator);

        Comonad<Optional<List<Integer>>> listComonad = comonad.extend(this::grouping);

        Assert.assertEquals(Arrays.asList(0, 1), listComonad.extract().get());
        Assert.assertEquals(Collections.singletonList(2), listComonad.extract().get());
        Assert.assertFalse(listComonad.extract().isPresent());
    }

    @Test
    public void testExtendWithoutRemainder(){
        Iterator<Integer> iterator = IntStream.range(0, SPLIT_SIZE).iterator();

        Comonad<Optional<Integer>> comonad = new IteratorComonad<>(iterator);

        Comonad<Optional<List<Integer>>> listComonad = comonad.extend(this::grouping);

        Assert.assertEquals(Arrays.asList(0, 1), listComonad.extract().get());
        Assert.assertFalse(listComonad.extract().isPresent());
    }

    @Test
    public void testMap(){
        Random values = new Random(seed);
        Comonad<Integer> comonad = createEndless(seed);
        Comonad<String> stringComonad = comonad.map(this::format);

        for(int i=1; i<VALUES_TO_CHECK; ++i) {
            String result = stringComonad.extract();
            Assert.assertEquals(format(values.nextInt()), result);
        }
    }


    /**
     * Left identity: wa.duplicate.extract == wa
     */
    @Test
    public void testLeftIdentity(){
        Comonad<Integer> comonad1 = createEndless(seed).duplicate().extract();
        Comonad<Integer> comonad2 = createEndless(seed);


        for(int i=0; i<VALUES_TO_CHECK; ++i) {
            Assert.assertEquals(comonad1.extract(), comonad2.extract());
        }
    }

    /**
     * Right identity: wa.extend(extract) == wa
     */
    @Test
    public void testRightIdentity(){
        Comonad<Integer> comonad1 = createEndless(seed).extend(Comonad::extract);
        Comonad<Integer> comonad2 = createEndless(seed);

        for(int i=0; i<VALUES_TO_CHECK; ++i) {
            Assert.assertEquals(comonad1.extract(), comonad2.extract());
        }
    }

    /**
     * Associativity: wa.duplicate.duplicate == wa.extend(duplicate)
     */
    @Test
    public void testAssociativity(){
        Comonad<Integer> val1 = createEndless(seed).duplicate().duplicate().extract().extract();
        Comonad<Integer> val3 =  createEndless(seed).extend(Comonad::duplicate).extract().extract();

        for(int i=0; i<VALUES_TO_CHECK; ++i) {
            Assert.assertEquals(val1.extract(), val3.extract());
        }
    }

    private String format(Integer v) {
        return String.format("%x", v);
    }

    private Comonad<Integer> createEndless(long seed) {
        return new Comonad<Integer>() {
            private Random value = new Random(seed);
            @Override
            public Integer extract() {
                return value.nextInt();
            }
        };
    }

    private Optional<List<Integer>> grouping(Comonad<Optional<Integer>> comonad){
        return ComonadUtil.grouping(SPLIT_SIZE, comonad);
    }
}
