package ru.cft.team;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import ru.cft.team.adapters.ExchangeRatesAdapter;
import ru.cft.team.controllers.MainController;
import ru.cft.team.dao.ExchangeRatesDatabase;
import ru.cft.team.models.ExchangeRate;

public class MainActivity extends AppCompatActivity {


    //подключаем базу данных
    private ExchangeRatesDatabase database;

    //подключаем MainController
    private MainController mainController;

    private RecyclerView recyclerViewExchangeRates;
    private Button buttonUpdate;
    private ExchangeRatesAdapter exchangeRatesAdapter;
    private String updateTimeStr;
    private boolean isRunning = false;

    public ExchangeRatesDatabase getDatabase() {
        return database;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainController = ViewModelProviders.of(this).get(MainController.class);
        isRunning = true;
        setContentView(R.layout.activity_main);
        recyclerViewExchangeRates = findViewById(R.id.recyclerViewExchangeRates);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        exchangeRatesAdapter = new ExchangeRatesAdapter(mainController.getExchangeRates().getList());
        recyclerViewExchangeRates.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExchangeRates.setAdapter(exchangeRatesAdapter);
        //подключаем слушатель для RecyclerView
        exchangeRatesAdapter.setOnExchangeRateClickListener(new ExchangeRatesAdapter.OnExchangeRateClickListener() {
            @Override
            public void onExchangeRateClick(int position) {
                onClickItemRecyclerViewExchangeRates(position);
            }
        });
        //добавляем смахивание (Swiped) для RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                onClickItemRecyclerViewExchangeRates(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerViewExchangeRates);

        //если активность запускается не первый раз
        if(savedInstanceState!=null) {
            updateTimeStr = savedInstanceState.getString("updateTimeStr");
            buttonUpdate.setText(getResources().getString(R.string.buttonUpdate) + "(" + updateTimeStr + ")");
        }
        else{
            //Если запускаем первый раз, то читаем дату последнего обновления из  SharedPreferences (постоянное хранение данных)
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            updateTimeStr = preferences.getString("updateTimeStr", "first");
            buttonUpdate.setText(getResources().getString(R.string.buttonUpdate) + "(" +
                    preferences.getString("updateTimeStr", "first") + ")");
        }
        runUpdatingExchangeRates();
    }

    public MainController getMainController() {
        return mainController;
    }

    public void onClickButtonUpdate(View view) {
        startDownloadJSONTask();
    }

    public void onClickItemRecyclerViewExchangeRates(int selectedItem) {
        Intent intent = new Intent(this, ConvertActivity.class);
        ExchangeRate exchangeRate = mainController.getExchangeRates().getExchangeRate(selectedItem);
        intent.putExtra("selectedItem", exchangeRate.getIdFromService());
        mainController.getExchangeRates().setItemToRepeatMap(
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
        //обновим данные в RecyclerView
        if (exchangeRatesAdapter!=null)
            exchangeRatesAdapter.notifyDataSetChanged();
    }

    private void startDownloadJSONTask(){
        try {
            DownloadJSONTask task = new DownloadJSONTask(exchangeRatesAdapter, buttonUpdate, updateTimeStr, getResources().getString(R.string.buttonUpdate), this);
            task.execute(getResources().getString(R.string.url));

        }
        catch (Exception e){
            Toast.makeText(this, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //класс для асинхронного получения данных с сервера валют.
    private static class DownloadJSONTask extends AsyncTask<String, Void, String>{

        private final ExchangeRatesAdapter exchangeRatesAdapter;
        private final Button buttonUpdate;
        private String updateTimeStr;
        private final String buttonUpdateName;
        private final MainActivity mainActivity;

        public DownloadJSONTask(ExchangeRatesAdapter exchangeRatesAdapter, Button buttonUpdate, String updateTimeStr, String buttonUpdateName, MainActivity mainActivity) {
            this.exchangeRatesAdapter = exchangeRatesAdapter;
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
                    updateTimeStr = mainActivity.getMainController().getExchangeRates().setMapFromStr(s);
                    //добавляем информацию об времени обновления на кнопку Update
                    if (updateTimeStr != null) {
                        String[] updateTimeStrs = updateTimeStr.split("[T+]");
                        if (updateTimeStrs.length > 2)
                            updateTimeStr = updateTimeStrs[0] + " " + updateTimeStrs[1];
                    }
                    else
                        updateTimeStr = "ParseAtrIsNull";
                    buttonUpdate.setText(buttonUpdateName + "(" + updateTimeStr + ")");
                    exchangeRatesAdapter.notifyDataSetChanged();
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