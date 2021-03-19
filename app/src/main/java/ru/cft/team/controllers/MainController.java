package ru.cft.team.controllers;

public class MainController {

    private static MainController instance;
    private ExchangeRates exchangeRates;

    public MainController(){
        exchangeRates = new ExchangeRates();
    }

    public static MainController getInstance() {
        if (instance == null)
                instance = new MainController();
        return instance;
    }

    public ExchangeRates getExchangeRates() {
        return exchangeRates;
    }

}
