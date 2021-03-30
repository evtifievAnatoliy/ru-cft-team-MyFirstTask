package ru.cft.team;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ru.cft.team.controllers.MainController;
import ru.cft.team.dao.ExchangeRatesDatabase;
import ru.cft.team.models.ExchangeRate;

public class ConvertActivity extends AppCompatActivity {

    //подключаем базу данных
    private ExchangeRatesDatabase database;

    //Spiner изначально устанавливается на выбранном элементе из главной антивности
    //Использую именно Spiner, т.к. хочу дать возможность пользователю выбрать так же другую валюту для конвертации
    private TextView exchangeRateTextViewFromActivityConvert;
    private EditText editTextNumberFromActivityConvert;
    private TextView resultTextViewFromActivityConver;

   private ExchangeRate exchangeRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);
        Intent intent = getIntent();
        exchangeRateTextViewFromActivityConvert = findViewById(R.id.exchangeRateTextViewFromActivityConvert);
        editTextNumberFromActivityConvert = findViewById(R.id.editTextNumberFromActivityConvert);
        resultTextViewFromActivityConver = findViewById(R.id.resultTextViewFromActivityConvert);
        database = ExchangeRatesDatabase.getInstance(this);

        Object o = intent.getStringExtra("selectedItem");
        exchangeRate = MainController.getInstance(database).getExchangeRates().getExchangeRate(o.toString());
        exchangeRateTextViewFromActivityConvert.setText(exchangeRate.toString());
    }

    public void onClickbuttonBackFromActivityConvert(View view) {
        finish();
    }

    public void onClickButtonConvertFromRUInActivityConvert(View view) {
        if(editTextNumberFromActivityConvert.getText().toString().isEmpty())
            Toast.makeText(this, getResources().getString(R.string.labelSumTextViewFromActivityConvert), Toast.LENGTH_SHORT).show();
        else {
            try {
                int amount = Integer.parseInt(editTextNumberFromActivityConvert.getText().toString());
                double d = exchangeRate.getNumbersForAmountFromRU(amount);
                String resultStr = String.format(Locale.getDefault(), getResources().getString(R.string.result_conver_string_from_RU),
                        amount, exchangeRate.getName(), exchangeRate.getNumbersForAmountFromRU(amount), exchangeRate.getCharCode());
                resultTextViewFromActivityConver.setText(resultStr);
            } catch (NullPointerException e) {
                Toast.makeText(this, "NullPointerException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "NumberFormatException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onClickButtonConvertInRUInActivityConvert(View view) {
        if (editTextNumberFromActivityConvert.getText().toString().isEmpty())
            Toast.makeText(this, getResources().getString(R.string.labelSumTextViewFromActivityConvert), Toast.LENGTH_SHORT).show();
        else {
            try {
                int amount = Integer.parseInt(editTextNumberFromActivityConvert.getText().toString());
                double d = exchangeRate.getNumbersForAmountInRU(amount);
                String resultStr = String.format(Locale.getDefault(), getResources().getString(R.string.result_conver_string_in_RU),
                        amount, exchangeRate.getName(), exchangeRate.getNumbersForAmountInRU(amount));
                resultTextViewFromActivityConver.setText(resultStr);
            } catch (NullPointerException e) {
                Toast.makeText(this, "NullPointerException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "NumberFormatException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void onClickButtonSendMsg(View view) {
        String msg = resultTextViewFromActivityConver.getText().toString();
        Intent sendMsgIntent = new Intent(Intent.ACTION_SEND);
        sendMsgIntent.setType("text/plain");
        sendMsgIntent.putExtra(Intent.EXTRA_TEXT, msg);
        Intent chosenIntent = Intent.createChooser(sendMsgIntent, getString(R.string.chooser_title));
        startActivity(chosenIntent);
    }




}