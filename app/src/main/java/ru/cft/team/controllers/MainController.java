package ru.cft.team.controllers;

import ru.cft.team.dao.ExchangeRatesDatabase;

public class MainController {

    private static MainController instance;
    private final ExchangeRates exchangeRates;
    private static final Object LOCK = new Object();

    public MainController(ExchangeRatesDatabase database){
        exchangeRates = new ExchangeRates(database);
    }

    public static MainController getInstance(ExchangeRatesDatabase database) {
        synchronized (LOCK) {
            if (instance == null)
                instance = new MainController(database);
        }
        return instance;
    }

    public ExchangeRates getExchangeRates() {
        return exchangeRates;
    }

}
