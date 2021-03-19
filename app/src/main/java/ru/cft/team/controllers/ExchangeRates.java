package ru.cft.team.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.cft.team.models.ExchangeRate;

public class ExchangeRates {
    //list использую из-за возможности сортировки, в сортировке используется индекс выбираемости элементов
    List <ExchangeRate> list;
    //map необходим для получения валют и обновления их в list
    Map <String, ExchangeRate> map;
    //map для хранения индексов наиболее часто выбираемых валют
    Map <String, Integer> repeatMap;

    public ExchangeRates() {
        list = new ArrayList<ExchangeRate>();
        map = new HashMap<>();
        repeatMap = new HashMap<>();
    }

    public List<ExchangeRate> getList() {
        return list;
    }

    public void setMapFromStr(String str) throws JSONException {
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
            if (map.get(exchangeRate.getId())==null)
                list.add(exchangeRate);
            map.put(exchangeRate.getId(), exchangeRate);
        }
        updateList();
    }

    private void updateList(){
        for (ExchangeRate exchangeRate : getList())
            if (map.get(exchangeRate.getId())!=null) {
                exchangeRate.setValue(map.get(exchangeRate.getId()).getValue());
                if(repeatMap.get(exchangeRate.getId())!=null)
                    exchangeRate.setRepeatIndex(repeatMap.get(exchangeRate.getId()));
            }
        Collections.sort(getList());
    }

    public void setItemToRepeatMap(ExchangeRate exchangeRate){
        if (repeatMap.get(exchangeRate.getId())==null)
            repeatMap.put(exchangeRate.getId(), 1);
        else
            repeatMap.put(exchangeRate.getId(), repeatMap.get(exchangeRate.getId())+1);
    }
}
