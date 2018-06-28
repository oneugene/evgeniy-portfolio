package com.epam.ievgenii_onyshchenko.comonad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComonadUtil {

    public static <A> Optional<List<A>> grouping(int splitSize, Comonad<Optional<A>> comonad) {
        List<A> result = new ArrayList<>(splitSize);
        for (int counter = 0; counter < splitSize; ++counter) {
            comonad.extract().ifPresent(result::add);
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
}
