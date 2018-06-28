package com.epam.ievgenii_onyshchenko.comonad;

import java.util.function.Function;

public class MappedComonad<A, B> implements Comonad<B> {
    private final Comonad<A> comonad;
    private final Function<A, B> f;

    public MappedComonad(Comonad<A> comonad, Function<A, B> f) {
        this.comonad = comonad;
        this.f = f;
    }

    @Override
    public B extract() {
        return f.apply(comonad.extract());
    }

}
