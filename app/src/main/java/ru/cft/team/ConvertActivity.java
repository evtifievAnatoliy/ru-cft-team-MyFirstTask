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
import ru.cft.team.models.ExchangeRate;

public class ConvertActivity extends AppCompatActivity {

    private Spinner spinnerExchangeRates;
    private EditText editTextNumberFromActivityConvert;
    private TextView resultTextViewFromActivityConver;

    private ArrayAdapter<ExchangeRate> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);
        Intent intent = getIntent();
        spinnerExchangeRates = (Spinner) findViewById(R.id.spinnerConvertInActivityConvert);
        editTextNumberFromActivityConvert = (EditText) findViewById(R.id.editTextNumberFromActivityConvert);
        resultTextViewFromActivityConver = (TextView) findViewById(R.id.resultTextViewFromActivityConvert);

        arrayAdapter = new ArrayAdapter<ExchangeRate> (this, android.R.layout.simple_list_item_1,
                MainController.getInstance().getExchangeRates().getList());
        spinnerExchangeRates.setAdapter(arrayAdapter);
        spinnerExchangeRates.setSelection(intent.getIntExtra("selectedItem", 0));
    }

    public void onClickbuttonBackFromActivityConvert(View view) {
        finish();
    }

    public void onClickButtonConvertInActivityConvert(View view) {
        if(editTextNumberFromActivityConvert.getText().toString().isEmpty())
            Toast.makeText(this, getResources().getString(R.string.labelSumTextViewFromActivityConvert), Toast.LENGTH_SHORT).show();
        else {
            try {
                int amount = Integer.parseInt(editTextNumberFromActivityConvert.getText().toString());
                ExchangeRate exchangeRate = (ExchangeRate) spinnerExchangeRates.getSelectedItem();
                double d = exchangeRate.getNumbersForAmount(amount);
                String resultStr = String.format(Locale.getDefault(), getResources().getString(R.string.result_conver_string),
                        amount, exchangeRate.getName(), exchangeRate.getNumbersForAmount(amount), exchangeRate.getCharCode());
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