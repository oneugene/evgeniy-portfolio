package com.epam.ievgenii_onyshchenko.comonad;

import java.util.function.Function;

public interface Comonad<A> {
    A extract();
    default <B> Comonad<B> map(Function<A, B> f){
        return new MappedComonad<>(this, f);
    }
    default Comonad<Comonad<A>> duplicate(){
        return this.extend(Function.identity());
    }
    default <B> Comonad<B> extend(Function<Comonad<A>, B> f){
        return new ExtendedComonad<>(this, f);
    }
}
