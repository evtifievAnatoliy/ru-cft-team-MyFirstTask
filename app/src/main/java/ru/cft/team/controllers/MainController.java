package ru.cft.team.controllers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import ru.cft.team.dao.ExchangeRatesDatabase;

public class MainController extends AndroidViewModel {

    private static MainController instance;
    private final ExchangeRates exchangeRates;
    private static  ExchangeRatesDatabase database;
    private static final Object LOCK = new Object();

    public MainController(@NonNull Application application){
        super(application);
        database = ExchangeRatesDatabase.getInstance(getApplication());
        exchangeRates = new ExchangeRates(database);
    }

    public static MainController getInstance(Application application) {
        synchronized (LOCK) {
            if (instance == null)
                instance = new MainController(application);
        }
        return instance;
    }

    public ExchangeRates getExchangeRates() {
        return exchangeRates;
    }

}
