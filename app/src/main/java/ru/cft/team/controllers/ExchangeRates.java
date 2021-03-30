package ru.cft.team.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.cft.team.dao.ExchangeRatesDatabase;
import ru.cft.team.models.ExchangeRate;

public class ExchangeRates {
    //list использую из-за возможности сортировки, в сортировке используется индекс выбираемости элементов
    private List <ExchangeRate> list;
    //map необходим для получения валют и обновления их в list
    private final Map <String, ExchangeRate> map;
    //map для хранения индексов наиболее часто выбираемых валют
    private final Map <String, Integer> repeatMap;
    //подключаем базу данных
    private final ExchangeRatesDatabase database;



    public ExchangeRates(ExchangeRatesDatabase database) {
        list = new ArrayList<ExchangeRate>();
        map = new HashMap<>();
        repeatMap = new HashMap<>();
        //читаем данные из базы данных
        this.database = database;
        List<ExchangeRate> exchangeRatesFromDB = database.exchangeRatesDao().getAllExchangeRates();//        Cursor cursor = database.query(ExchangeRatesContract.ExchangeRatesEntry.TABLE_NAME,
        for (ExchangeRate exchangeRate: exchangeRatesFromDB) {
            map.put(exchangeRate.getIdFromService(), exchangeRate);
            repeatMap.put(exchangeRate.getIdFromService(), exchangeRate.getRepeatIndex());
        }
        list = exchangeRatesFromDB;
    }

    public List<ExchangeRate> getList() {
        return list;
    }

    public void setMapFromStr(String str) throws JSONException {
        if (str!=null) {
            JSONObject jsonObject = new JSONObject(str);
            JSONObject jsonValute = jsonObject.getJSONObject("Valute");
            Iterator<String> iter = jsonValute.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                JSONObject iterObject = jsonValute.getJSONObject(key);
                ExchangeRate exchangeRate = new ExchangeRate(
                        iterObject.getString("ID"),
                        iterObject.getString("CharCode"),
                        iterObject.getInt("Nominal"),
                        iterObject.getString("Name"),
                        iterObject.getDouble("Value")
                );
                if (map.get(exchangeRate.getIdFromService()) == null) {
                    list.add(exchangeRate);
                    database.exchangeRatesDao().insertExchangeRate(exchangeRate);
                }
                else{
                    database.exchangeRatesDao().updateExchangeRate(exchangeRate);
                }
                map.put(exchangeRate.getIdFromService(), exchangeRate);

            }
            updateList();
        }
    }

    private void updateList(){
        for (ExchangeRate exchangeRate : getList())
            if (map.get(exchangeRate.getIdFromService())!=null) {
                exchangeRate.setValue(map.get(exchangeRate.getIdFromService()).getValue());
                if(repeatMap.get(exchangeRate.getIdFromService())!=null)
                    exchangeRate.setRepeatIndex(repeatMap.get(exchangeRate.getIdFromService()));
            }
        Collections.sort(getList());
    }

    public void setItemToRepeatMap(ExchangeRate exchangeRate){
        if (repeatMap.get(exchangeRate.getIdFromService())==null) {
            repeatMap.put(exchangeRate.getIdFromService(), 1);
            exchangeRate.setRepeatIndex(1);
        }
        else{
            repeatMap.put(exchangeRate.getIdFromService(), repeatMap.get(exchangeRate.getIdFromService()) + 1);
            exchangeRate.setRepeatIndex(repeatMap.get(exchangeRate.getIdFromService()) + 1);

        }
        database.exchangeRatesDao().updateExchangeRate(exchangeRate);
    }

    public ExchangeRate getExchangeRate(String idFromService) {
        return map.get(idFromService);
    }
}
