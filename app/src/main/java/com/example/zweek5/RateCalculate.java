package com.example.zweek5;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class RateCalculate extends AppCompatActivity {
    private static  final String TAG = "MyRateListActivityTwo";
    TextView textViewCountry, textViewRate;
    String country="";
    Double rate = 1.0;
    EditText editTextRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_calculate);

        textViewCountry = findViewById(R.id.textViewCountry_RateCalculate);
        textViewRate = findViewById(R.id.textViewRate_RateCalculate);
        editTextRate = findViewById(R.id.editView_RateCalculate);

        country = getIntent().getStringExtra("Country");
        rate = Double.parseDouble(getIntent().getStringExtra("Rate"));
        Log.i(TAG, "onCreate: country =" + country);
        Log.i(TAG, "onCreate: rate =" + rate);
        textViewCountry.setText(country);

        //EditText文本输入变化事件监听器：实现实时计算
        editTextRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textViewRate.setText("Please input...");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewRate.setText("Waiting for input...");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    Double val = Double.parseDouble(s.toString());
                    textViewRate.setText( val+ " RMB = " + MainActivity.doubleRound(val, 4) + " " +country);
                }else {
                    textViewRate.setText("Input none! Please input RMB.");
                }
            }
        });
    }


}
