package ru.cft.team.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.Preference;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.cft.team.MainActivity;
import ru.cft.team.db.ExchangeRatesContract;
import ru.cft.team.db.ExchangeRatesDBHelper;
import ru.cft.team.models.ExchangeRate;

public class ExchangeRates {
    //list использую из-за возможности сортировки, в сортировке используется индекс выбираемости элементов
    private List <ExchangeRate> list;
    //map необходим для получения валют и обновления их в list
    private Map <String, ExchangeRate> map;
    //map для хранения индексов наиболее часто выбираемых валют
    private Map <String, Integer> repeatMap;
    //подключаем базу данных
    private ExchangeRatesDBHelper dbHelper;
    private SQLiteDatabase database;


    public ExchangeRates(ExchangeRatesDBHelper dbHelper) {
        list = new ArrayList<ExchangeRate>();
        map = new HashMap<>();
        repeatMap = new HashMap<>();
        this.dbHelper = dbHelper;
        database = dbHelper.getWritableDatabase();
        //читаем данные из базы данных
        Cursor cursor = database.query(ExchangeRatesContract.ExchangeRatesEntry.TABLE_NAME,
                null, null, null, null, null,
                null);
        while (cursor.moveToNext()){
            ExchangeRate exchangeRate = new ExchangeRate(
                    cursor.getString(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_ID_FROM_SERVICE)),
                    cursor.getString(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_CHAR_CODE)),
                    cursor.getInt(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_NOMINAL)),
                    cursor.getString(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_NAME)),
                    cursor.getDouble(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_VALUE)),
                    cursor.getInt(cursor.getColumnIndex(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_REPEAT_INDEX))
            );
            map.put(exchangeRate.getId(), exchangeRate);
            list.add(exchangeRate);
            repeatMap.put(exchangeRate.getId(), exchangeRate.getRepeatIndex());
        }
        Collections.sort(getList());
        cursor.close();
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
                if (map.get(exchangeRate.getId()) == null) {
                    list.add(exchangeRate);
                    ContentValues contentValues = getContentValues(exchangeRate.getId(), exchangeRate.getCharCode(),
                            exchangeRate.getNominal(), exchangeRate.getName(), exchangeRate.getValue(),
                            1);
                    database.insert(ExchangeRatesContract.ExchangeRatesEntry.TABLE_NAME, null, contentValues);
                }
                else{
                    ContentValues contentValues = getContentValues(exchangeRate.getId(), exchangeRate.getCharCode(),
                            exchangeRate.getNominal(), exchangeRate.getName(), exchangeRate.getValue(),
                            repeatMap.get(exchangeRate.getId()));
                    String where = ExchangeRatesContract.ExchangeRatesEntry.COLUMN_ID_FROM_SERVICE + " = ?";
                    String[] whereArgs = new String[]{exchangeRate.getId()};
                    database.update(ExchangeRatesContract.ExchangeRatesEntry.TABLE_NAME,
                            contentValues, where, whereArgs);
                }
                map.put(exchangeRate.getId(), exchangeRate);

            }
            updateList();
        }
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
        else{
            repeatMap.put(exchangeRate.getId(), repeatMap.get(exchangeRate.getId()) + 1);
            ContentValues contentValues = getContentValues(exchangeRate.getId(), exchangeRate.getCharCode(),
                    exchangeRate.getNominal(), exchangeRate.getName(), exchangeRate.getValue(),
                    repeatMap.get(exchangeRate.getId()));
            String where = ExchangeRatesContract.ExchangeRatesEntry.COLUMN_ID_FROM_SERVICE + " = ?";
            String[] whereArgs = new String[]{exchangeRate.getId()};
            database.update(ExchangeRatesContract.ExchangeRatesEntry.TABLE_NAME,
                    contentValues, where, whereArgs);
        }
    }

    private ContentValues getContentValues (String id, String charCode, int nominal,
                                            String name, double value, int repaetIndex){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_ID_FROM_SERVICE, id);
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_CHAR_CODE, charCode);
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_NOMINAL, nominal);
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_NAME, name);
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_VALUE, value);
        contentValues.put(ExchangeRatesContract.ExchangeRatesEntry.COLUMN_REPEAT_INDEX, repaetIndex);
        return contentValues;
    }
}
