package com.example.zweek5;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SecondActivity extends AppCompatActivity {
    
    EditText EditText1, EditText2, EditText3;
    double dollarRaten = 1, euroRaten = 1, wonRaten = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        
        EditText1= (EditText) findViewById(R.id.EditText1);
        EditText2= (EditText) findViewById(R.id.EditText2);
        EditText3= (EditText) findViewById(R.id.EditText3);

        Intent intent =getIntent();
        dollarRaten = intent.getDoubleExtra("dollarRate",dollarRaten);
        euroRaten = intent.getDoubleExtra("euroRate",euroRaten);
        wonRaten = intent.getDoubleExtra("wonRate",wonRaten);
        EditText1.setText(String.valueOf(dollarRaten));
        EditText2.setText(String.valueOf(euroRaten));
        EditText3.setText(String.valueOf(wonRaten));

    }


    public  void onClick(View v){
        switch (v.getId()){
            case R.id.btn_save:

                    if(EditText1.getText().toString().length()>0 ){
                        dollarRaten = doubleRound(Double.parseDouble(EditText1.getText().toString()));
                    }
                    if(EditText1.getText().toString().length()>0){
                        euroRaten = doubleRound(Double.parseDouble(EditText2.getText().toString()));
                    }
                    if(EditText1.getText().toString().length()>0){
                        wonRaten = doubleRound(Double.parseDouble(EditText3.getText().toString()));
                    }
                    Intent intent=new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("dollarRaten",dollarRaten);
                    bundle.putDouble("euroRaten",euroRaten);
                    bundle.putDouble("wonRaten",wonRaten);
                    intent.putExtras(bundle);
                    /*intent.putExtra("dollarRaten",dollarRaten);
                    intent.putExtra("euroRaten", euroRaten);
                    intent.putExtra("wonRaten", wonRaten);*/
                    setResult(RESULT_OK,intent);
                    finish();
                break;
                default:
        }

    }


    //四舍五入
    public Double doubleRound(Double d){
        return (double)Math.round(d*10000)/10000;
    }
    public Double doubleRound(String str){
        return (double)Math.round(Double.parseDouble(str)*10000)/10000;
    }
    
}
