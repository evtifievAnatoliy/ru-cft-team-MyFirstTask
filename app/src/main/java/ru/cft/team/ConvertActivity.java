package ru.cft.team;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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

    //подключаем MainController
    private MainController mainController;

    private TextView exchangeRateTextViewFromActivityConvert;
    private EditText editTextNumberFromActivityConvert;
    private TextView resultTextViewFromActivityConver;


    //валюта
    private ExchangeRate exchangeRate;
    //текст который выводится после конвертации валюты
    private String resultStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);
        Intent intent = getIntent();
        exchangeRateTextViewFromActivityConvert = findViewById(R.id.exchangeRateTextViewFromActivityConvert);
        editTextNumberFromActivityConvert = findViewById(R.id.editTextNumberFromActivityConvert);
        resultTextViewFromActivityConver = findViewById(R.id.resultTextViewFromActivityConvert);
        mainController = ViewModelProviders.of(this).get(MainController.class);

        Object o = intent.getStringExtra("selectedItem");
        exchangeRate = mainController.getExchangeRates().getExchangeRate(o.toString());
        exchangeRateTextViewFromActivityConvert.setText(exchangeRate.toString());

        //если активность запускается не первый раз
        if(savedInstanceState!=null) {
            resultStr = savedInstanceState.getString("resultStr");
            resultTextViewFromActivityConver.setText(resultStr);

        }
    }

    public void onClickbuttonBackFromActivityConvert(View view) {
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickButtonConvertFromRUInActivityConvert(View view) {
        if(editTextNumberFromActivityConvert.getText().toString().isEmpty())
            Toast.makeText(this, getResources().getString(R.string.labelSumTextViewFromActivityConvert), Toast.LENGTH_SHORT).show();
        else {
            try {
                int amount = Integer.parseInt(editTextNumberFromActivityConvert.getText().toString());
                double d = exchangeRate.getNumbersForAmountFromRU(amount);
                resultStr = String.format(Locale.getDefault(), getResources().getString(R.string.result_conver_string_from_RU),
                        amount, exchangeRate.getName(), exchangeRate.getNumbersForAmountFromRU(amount), exchangeRate.getCharCode());
                resultTextViewFromActivityConver.setText(resultStr);
                hideKeyboard(view);
            } catch (NullPointerException e) {
                Toast.makeText(this, "NullPointerException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "NumberFormatException: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickButtonConvertInRUInActivityConvert(View view) {
        if (editTextNumberFromActivityConvert.getText().toString().isEmpty())
            Toast.makeText(this, getResources().getString(R.string.labelSumTextViewFromActivityConvert), Toast.LENGTH_SHORT).show();
        else {
            try {
                int amount = Integer.parseInt(editTextNumberFromActivityConvert.getText().toString());
                double d = exchangeRate.getNumbersForAmountInRU(amount);
                resultStr = String.format(Locale.getDefault(), getResources().getString(R.string.result_conver_string_in_RU),
                        amount, exchangeRate.getName(), exchangeRate.getNumbersForAmountInRU(amount));
                resultTextViewFromActivityConver.setText(resultStr);
                hideKeyboard(view);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("resultStr", resultStr);
    }

    //метод для того, чтобы скрыть клавиатуру
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}