package ru.cft.team;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Locale;


import javax.net.ssl.HttpsURLConnection;

import ru.cft.team.controllers.MainController;
import ru.cft.team.dao.ExchangeRatesDatabase;
import ru.cft.team.models.ExchangeRate;

public class MainActivity extends AppCompatActivity {


    //подключаем базу данных
    private ExchangeRatesDatabase database;

    private ListView listViewExchangeRates;
    private Button buttonUpdate;
    private ArrayAdapter<ExchangeRate> arrayAdapter;
    private String updateTimeStr;
    private boolean isRunning = false;

    public ExchangeRatesDatabase getDatabase() {
        return database;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = ExchangeRatesDatabase.getInstance(this);
        isRunning = true;
        setContentView(R.layout.activity_main);
        listViewExchangeRates = findViewById(R.id.listViewExchangeRates);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        arrayAdapter = new ArrayAdapter<ExchangeRate> (this, android.R.layout.simple_list_item_1,
                MainController.getInstance(database).getExchangeRates().getList());
        listViewExchangeRates.setAdapter(arrayAdapter);
        listViewExchangeRates.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewExchangeRates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickItemListViewExchangeRates(position);
            }
        });
        //если активность запускается не первый раз
        if(savedInstanceState!=null) {
            updateTimeStr = savedInstanceState.getString("updateTimeStr");
            buttonUpdate.setText(getResources().getString(R.string.buttonUpdate) + "(" + updateTimeStr + ")");
        }
        else{
            //Если запускаем первый раз, то читаем дату последнего обновления из  SharedPreferences (постоянное хранение данных)
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String str = preferences.getString("updateTimeStr", "first");
            buttonUpdate.setText(getResources().getString(R.string.buttonUpdate) + "(" +
                    preferences.getString("updateTimeStr", "first") + ")");
        }
        runUpdatingExchangeRates();
    }

    public void onClickButtonUpdate(View view) {
        startDownloadJSONTask();
    }

    public void onClickItemListViewExchangeRates(int selectedItem) {
        Intent intent = new Intent(this, ConvertActivity.class);
        ExchangeRate exchangeRate = (ExchangeRate) listViewExchangeRates.getItemAtPosition(selectedItem);
        intent.putExtra("selectedItem", exchangeRate.getIdFromService());
        MainController.getInstance(database).getExchangeRates().setItemToRepeatMap(
                exchangeRate
        );
        startActivity(intent);
    }

    //метод автоматического обновления
    private void runUpdatingExchangeRates(){
        final Handler handler = new Handler();
        final long updateTime = 60000*360;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isRunning) {
                    startDownloadJSONTask();
                }
                handler.postDelayed(this, updateTime);
            }
        }, updateTime);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("updateTimeStr", updateTimeStr);
    }


    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    private void startDownloadJSONTask(){
        try {
            DownloadJSONTask task = new DownloadJSONTask(arrayAdapter, buttonUpdate, updateTimeStr, getResources().getString(R.string.buttonUpdate), this);
            task.execute(getResources().getString(R.string.url));

        }
        catch (Exception e){
            Toast.makeText(this, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //класс для асинхронного получения данных с сервера валют.
    private static class DownloadJSONTask extends AsyncTask<String, Void, String>{

        private final ArrayAdapter<ExchangeRate> arrayAdapter;
        private final Button buttonUpdate;
        private String updateTimeStr;
        private final String buttonUpdateName;
        private final MainActivity mainActivity;

        public DownloadJSONTask(ArrayAdapter<ExchangeRate> arrayAdapter, Button buttonUpdate, String updateTimeStr, String buttonUpdateName, MainActivity mainActivity) {
            this.arrayAdapter = arrayAdapter;
            this.buttonUpdate = buttonUpdate;
            this.updateTimeStr = updateTimeStr;
            this.buttonUpdateName = buttonUpdateName;
            this.mainActivity = mainActivity;
        }



        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpsURLConnection connection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                //запись в лог
            } catch (IOException e) {
                //запись в лог
            }
            catch (Exception e) {
                //запись в лог
            }finally
            {
                if (connection != null) {
                        connection.disconnect();
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                //обновляем данные в контроллере
                if(s!=null) {
                    MainController.getInstance(mainActivity.getDatabase()).getExchangeRates().setMapFromStr(s);
                    //добавляем информацию об времени обновления на кнопку Update
                    Time time = new Time(Time.getCurrentTimezone());
                    time.setToNow();
                    updateTimeStr = String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d:%02d",
                            time.year, time.month + 1, time.monthDay,
                            time.hour, time.minute, time.second);
                    buttonUpdate.setText(buttonUpdateName + "(" + updateTimeStr + ")");
                    arrayAdapter.notifyDataSetChanged();
                    //Добавление последней даты обновления в  SharedPreferences (постоянное хранение данных)
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                    preferences.edit().putString("updateTimeStr", updateTimeStr).apply();
                    //выводим сообщение об удачном обновлении пользователю
                    Toast.makeText(mainActivity, "Update Completed", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mainActivity, "Update error", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(mainActivity, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}