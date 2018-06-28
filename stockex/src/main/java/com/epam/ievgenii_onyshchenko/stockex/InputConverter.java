package com.epam.ievgenii_onyshchenko.stockex;

import com.epam.ievgenii_onyshchenko.stockex.model.BuyInput;
import com.epam.ievgenii_onyshchenko.stockex.model.Input;
import com.epam.ievgenii_onyshchenko.stockex.model.InvalidInput;
import com.epam.ievgenii_onyshchenko.stockex.model.ListInput;
import com.epam.ievgenii_onyshchenko.stockex.model.SellInput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputConverter {
    private static Pattern BUY = Pattern.compile("^BUY\\s+(\\d+)\\s+(\\d+)$");
    private static Pattern SELL = Pattern.compile("^SELL\\s+(\\d+)\\s+(\\d+)$");
    private static Pattern LIST = Pattern.compile("^LIST$");

    public static Input convert(String line) {
        Matcher matcher = BUY.matcher(line);
        if (matcher.find()) {
            return new BuyInput(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }

        matcher = SELL.matcher(line);
        if (matcher.find()) {
            return new SellInput(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }

        matcher = LIST.matcher(line);
        if (matcher.find()) {
            return new ListInput();
        }

        return new InvalidInput();
    }
}
