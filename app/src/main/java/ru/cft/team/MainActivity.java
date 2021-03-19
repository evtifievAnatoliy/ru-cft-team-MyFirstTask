package ru.cft.team;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import ru.cft.team.models.ExchangeRate;

public class MainActivity extends AppCompatActivity {

    private ListView listViewExchangeRates;
    private Button buttonUpdate;
    private ArrayAdapter<ExchangeRate> arrayAdapter;
    private String updateTimeStr;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRunning = true;
        setContentView(R.layout.activity_main);
        listViewExchangeRates = (ListView) findViewById(R.id.listViewExchangeRates);
        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        arrayAdapter = new ArrayAdapter<ExchangeRate> (this, android.R.layout.simple_list_item_1,
                MainController.getInstance().getExchangeRates().getList());
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
        runUpdatingExchangeRates();
    }

    public void onClickButtonUpdate(View view) {
        startDownloadJSONTask();
    }

    public void onClickItemListViewExchangeRates(int selectedItem) {
        Intent intent = new Intent(this, ConvertActivity.class);
        intent.putExtra("selectedItem", selectedItem);
        MainController.getInstance().getExchangeRates().setItemToRepeatMap(
                (ExchangeRate) listViewExchangeRates.getItemAtPosition(selectedItem)
        );
        startActivity(intent);
    }

    //метод автоматического обновления
    private void runUpdatingExchangeRates(){
        final Handler handler = new Handler();
        final long updateTime = 60000*360;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(isRunning) {
                    startDownloadJSONTask();
                }
                handler.postDelayed(this, updateTime);
            }
        });

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
        DownloadJSONTask task = new DownloadJSONTask(arrayAdapter, buttonUpdate, updateTimeStr, getResources().getString(R.string.buttonUpdate), this);
        task.execute("https://www.cbr-xml-daily.ru/daily_json.js");
    }

    //класс для асинхронного получения данных с сервера валют.
    private static class DownloadJSONTask extends AsyncTask<String, Void, String>{

        private ArrayAdapter<ExchangeRate> arrayAdapter;
        private Button buttonUpdate;
        private String updateTimeStr;
        private String buttonUpdateName;
        private MainActivity mainActivity;

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
                Toast.makeText(mainActivity, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mainActivity, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(mainActivity, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                MainController.getInstance().getExchangeRates().setMapFromStr(s);
                //добавляем информацию об времени обновления на кнопку Update
                Time time = new Time();
                time.setToNow();
                updateTimeStr = String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d:%02d",
                        time.year, time.month, time.monthDay,
                        time.hour, time.minute, time.second);
                buttonUpdate.setText(buttonUpdateName + "(" + updateTimeStr + ")");
                arrayAdapter.notifyDataSetChanged();
                //выводим сообщение об удачном обновлении пользователю
                Toast.makeText(mainActivity, "Update Completed", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(mainActivity, "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}