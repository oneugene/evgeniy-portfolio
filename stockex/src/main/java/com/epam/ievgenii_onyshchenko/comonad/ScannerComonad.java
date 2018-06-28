package com.epam.ievgenii_onyshchenko.comonad;

import java.util.Optional;
import java.util.Scanner;

public class ScannerComonad implements Comonad<Optional<String>> {
    private final Scanner it;

    public ScannerComonad(Scanner it) {
        this.it = it;
    }

    public Optional<String> extract() {
        if (it.hasNextLine()) {
            return Optional.of(it.nextLine());
        }
        return Optional.empty();
    }
}
