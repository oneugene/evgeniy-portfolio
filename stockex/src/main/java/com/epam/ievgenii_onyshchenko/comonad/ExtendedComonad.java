package com.epam.ievgenii_onyshchenko.comonad;


import java.util.function.Function;

public class ExtendedComonad<A, B> implements Comonad<B> {
    private final Comonad<A> comonad;
    private final Function<Comonad<A>, B> f;

    public ExtendedComonad(Comonad<A> comonad, Function<Comonad<A>, B> f) {
        this.comonad = comonad;
        this.f = f;
    }

    @Override
    public B extract() {
        return f.apply(comonad);
    }

}
