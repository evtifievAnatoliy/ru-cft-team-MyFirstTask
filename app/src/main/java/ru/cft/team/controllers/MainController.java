package ru.cft.team.controllers;

import ru.cft.team.MainActivity;
import ru.cft.team.db.ExchangeRatesDBHelper;

public class MainController {

    private static MainController instance;
    private ExchangeRates exchangeRates;

    public MainController(ExchangeRatesDBHelper dbHelper){
        exchangeRates = new ExchangeRates(dbHelper);
    }

    public static MainController getInstance(ExchangeRatesDBHelper dbHelper) {
        if (instance == null)
                instance = new MainController(dbHelper);
        return instance;
    }

    public ExchangeRates getExchangeRates() {
        return exchangeRates;
    }

}
