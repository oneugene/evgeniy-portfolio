package com.epam.ievgenii_onyshchenko.comonad;

import java.util.Iterator;
import java.util.Optional;

public class IteratorComonad<A> implements Comonad<Optional<A>> {
    private final Iterator<A> it;

    public IteratorComonad(Iterator<A> it) {
        this.it = it;
    }

    public Optional<A> extract() {
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }
}
