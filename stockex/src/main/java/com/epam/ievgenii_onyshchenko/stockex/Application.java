package com.epam.ievgenii_onyshchenko.stockex;

import com.epam.ievgenii_onyshchenko.comonad.Comonad;
import com.epam.ievgenii_onyshchenko.comonad.ScannerComonad;
import com.epam.ievgenii_onyshchenko.stockex.model.Input;

import java.util.Optional;
import java.util.Scanner;

public class Application {

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        Comonad<Optional<Input>> program = new ScannerComonad(scanner).map(val -> val.map(InputConverter::convert));

        Optional<Input> currentState ;
        while((currentState = program.extract()).isPresent()){
            currentState.ifPresent(val -> System.out.println(val.toString()));
        }
    }
}
