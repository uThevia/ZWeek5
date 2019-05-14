package com.example.zweek5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Runnable{ //多线程run

    private int num =2 ;
    private final String TAG ="MainActivity";
    EditText input;
    TextView output;
    private double dollarRateDefault=6.7, euroRateDefault=11, wonRateDefault=1.0/500;
    private double dollarRate=dollarRateDefault, euroRate=euroRateDefault, wonRate=wonRateDefault;
    private Handler handler = new Handler();
    private Message message;
    private String updateDateString = "", currentDateString = ""; //更新时间和当前时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置布局
        setContentView(R.layout.activity_main);
        //初始化控件
        input = (EditText) findViewById(R.id.rmb);
        output= (TextView) findViewById(R.id.showOut);

        //初始化参数
        //初始化SharedPreference
        SharedPreferences  sharedPreferences = getSharedPreferences("my_rate", Activity.MODE_PRIVATE);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); //推荐用法只有一个配置文件
        dollarRate = doubleRound(sharedPreferences.getFloat("dollarRate", (float) dollarRateDefault), num);
        euroRate = doubleRound(sharedPreferences.getFloat("euroRate", (float)euroRateDefault), num);
        wonRate = doubleRound(sharedPreferences.getFloat("wonRate", (float)wonRateDefault), num);
        //获取上一次更新时间
        updateDateString = sharedPreferences.getString("updateDateString","");
        //获取当前时间
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd:hh-mm");
        currentDateString = simpleDateFormat.format(currentDate);

        Log.i(TAG, "onCreate: sp dollarRate:"+dollarRate);
        Log.i(TAG, "onCreate: sp euroRate:"+euroRate);
        Log.i(TAG, "onCreate: sp wonRate:"+wonRate);
        Log.i(TAG, "onCreate: sp updateDateSting:"+updateDateString);
        Log.i(TAG, "onCreate: sp currentDateString:"+currentDateString);


        //判断日期并更新(开启子线程)
        if(! currentDateString.equals(updateDateString)){
            Log.i(TAG,"need update date.");
            //开启子线程
            Thread thread = new Thread(this); //目标当前活动对象(以实现接口）
            thread.start();
        }else {
            Log.i(TAG,"not need update date.");
        }
        
        //子线程返回数据
        handler = new Handler(){    //匿名类改写方法
            @Override
            public void handleMessage(Message msg) {
                //判断是哪个线程返回的哪个消息
                switch(message.what){
                    case 0:
                        Bundle bundle = (Bundle) message.obj;
                        dollarRate= bundle.getDouble("dollarRateWeb");
                        euroRate= bundle.getDouble("euroRateWeb");
                        wonRate= bundle.getDouble("wonRateWeb");
                        Log.i(TAG, "handleMessage: dollarRate:"+dollarRate);
                        Log.i(TAG, "handleMessage: euroRate:"+euroRate);
                        Log.i(TAG, "handleMessage: wonRate:"+wonRate);
                        //更新保存日期
                        SharedPreferences  sharedPreferences = getSharedPreferences("my_rate", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("updateDateString", currentDateString);
                        editor.putFloat("dollarRate", (float)dollarRate);
                        editor.putFloat("euroRate", (float)euroRate);
                        editor.putFloat("wonRate", (float)wonRate);
                        editor.apply();//保存数据
                        
                        Toast.makeText(MainActivity.this, "Rates had updated!",Toast.LENGTH_SHORT);
                        break;
                    default:
                }
                super.handleMessage(msg);
            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    /*dollarRate = data.getDoubleExtra("dollarRaten",dollarRate);
                    euroRate = data.getDoubleExtra("euroRaten", euroRate);
                    wonRate = data.getDoubleExtra("wonRaten",wonRate);*/

                    Bundle bundle= data.getExtras();
                    dollarRate = bundle.getDouble("dollarRaten",dollarRate);
                    euroRate = bundle.getDouble("euroRaten",euroRate);
                    wonRate = bundle.getDouble("wonRaten",wonRate);

                    Log.i(TAG, "dollarRate back "+dollarRate);
                    Log.i(TAG, "euroRate back "+euroRate);
                    Log.i(TAG, "wonRate back "+wonRate);

                    //SecondActivity传过来的新设置的汇率写入sp
                    SharedPreferences  sharedPreferences = getSharedPreferences("my_rate", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("dollarRate", (float) dollarRate);
                    editor.putFloat("euroRate", (float) euroRate);
                    editor.putFloat("wonRate", (float) wonRate);
                    editor.commit();
                    Log.i(TAG, "data has been saved");
                }
                break;
            default:

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //按钮响应方法
    public void onClick(View v){
        String str = input.getText().toString();
        double vin=0;
        double val=0;
        if(str.length()>0){
            vin= Double.parseDouble(str);
        }else{
            Toast.makeText(this,"请输入正确形式的金额",Toast.LENGTH_SHORT).show();
            vin=0;
        }
        switch(v.getId()){
            case R.id.btn_dollar:
                val = vin / dollarRate;
                output.setText(String.valueOf(doubleRound(val, num)));
                break;
            case R.id.btn_euro:
                val= vin / euroRate;
                output.setText(String.valueOf(doubleRound(val, num)));
                break;
            case R.id.btn_won:
                val= vin / wonRate;
                output.setText(String.valueOf(doubleRound(val, num)));
                break;
            case R.id.btn_confirm:
                openConfig();
                break;
            default:
        }
    }
    //自定义config按钮/menu_set菜单具体方法
    private void openConfig() {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("dollarRate",dollarRate);
        intent.putExtra("euroRate",euroRate);
        intent.putExtra("wonRate",wonRate);
        Log.i(TAG,"dollarRate to "+dollarRate);
        Log.i(TAG,"euroRate to "+euroRate);
        Log.i(TAG,"wonRate to "+wonRate);
        //startActivity(intent);
        startActivityForResult(intent,1);
    }
    //自定义open_list菜单方法
    private void openList() {
        //跳转：从this到目标activity
            //以下两行为系统自带列表布局，自定义列表布局
        //Intent intent = new Intent(MainActivity.this, RateListActivity.class);
        //Intent intent = new Intent(MainActivity.this, MyRateListActivity.class);
        Intent intent = new Intent(MainActivity.this, MyRateListActivityTwo.class);
        startActivity(intent);
        //startActivityForResult(intent,1);
    }

    //设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rate,menu);
        return true;
    }
    //菜单响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_set:
                openConfig();
                break;
            case R.id.open_list:
                openList();
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    //子线程运行
    @Override
    public void run() {
        int i=0, j=0;
        /*Log.i(TAG, "run:run...");
        for(int i=1;i<6;i++){   //空循环
            Log.i(TAG, "run: i="+i);
            try{
                Thread.sleep(500);  //睡眠500ms
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        //获取Message对象，用于返回到主线程
        message = handler.obtainMessage();
        message.what = 0; //arg1,arg2获取整型，obj获取所有其他， what整型标记当前message标志属性（这里设置为0即为此线程的此消息标志为0）
        //等效于Message message= handler.obtainMessage(int what, int arg1,int arg2, Object obj);
        message.obj = "Test for run of Handler";
        handler.sendMessage(message);*/

        /*//获取网络数据
        URL url = null;
        try {
            url = new URL("http://www.usd-cny.com/bankofchina.htm");                              //新建网址5
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();     //建立链接/连接
            InputStream inputStream = httpURLConnection.getInputStream();                       //获取输入流
            String string = inputStream2String(inputStream);                                    //转化为字符串
            Log.i(TAG, "run: html="+string);
            Document doc = Jsoup.parse(html);   //序号1
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //获取message对象返回主线程
        message = handler.obtainMessage(0);
        //获取bundle
        message.obj = getFromUsdcny();
        //发送数据
        handler.sendMessage(message);
        Log.i(TAG, "rate has been update in run(son thread)");

    }


    /*
    从bandofchina（http://www.boc.cn/sourcedb/whpj/）
    获取汇率数据
    */
    private Bundle getFromBOC() {
        Bundle bundleRates = new Bundle();
        int i;
        Document doc = null;
        try {
            doc = Jsoup.connect("http://http://www.boc.cn/sourcedb/whpj/").get();    //同序号1
            Log.i(TAG, "run:"+doc.title());
            /*Elements newsHeadlines = doc.select("#mp-itn b a");
            for (Element headline : newsHeadlines) {
                Log.i(TAG,"%s\n\t%s"+headline.attr("title")+headline.absUrl("href"));
            }*/

            //从网页转化的文档获取table标签元素部分
            Elements tables = doc.getElementsByTag("table");
            /*int i=1;
            for(Element table: tables){
                Log.i(TAG, "run: table["+i+"]="+table);
                i++;
            }*/
            Element getTable = tables.get(1);
            //Log.i(TAG, "run:getTable"+getTable);
            //从table部分获取td标签元素部分
            Elements tds = getTable.getElementsByTag("td");
            /*i=1;
            for(Element td: tds){
                Log.i(TAG, "run: td["+i+"]="+td);
                Log.i(TAG, "run: td["+i+"].text="+td.text());
                Log.i(TAG, "run: td["+i+"].html="+td.html());
                i++;
            }*/
            //从td部分获取数据并保存在bundleRates中
            Element getTdCountry, getTdRate;
            //j=0;
            String strKeyRate = null;
            for(i=0;i<tds.size();i+=8){
                getTdCountry = tds.get(i);
                getTdRate = tds.get(i+5);
                String getCountry = getTdCountry.text();
                String getRate = getTdRate.text();
                /*Log.i(TAG, "run:Country["+j+"]="+getCountry+": Rate="+getRate);
                j++;*/
                switch(getCountry){
                    case "美元":
                        strKeyRate ="dollarRateWeb";
                        break;
                    case "欧元":
                        strKeyRate ="euroRateWeb";
                        break;
                    case "韩元":
                        strKeyRate ="wonRateWeb";
                        break;
                        default:
                }
                if(strKeyRate != null){
                    bundleRates.putDouble(strKeyRate, doubleRound(getRate, num));
                    strKeyRate=null;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundleRates;
    }

    /*
    从美元人民币汇率网（http://www.usd-cny.com/bankofchina.htm）
    获取汇率数据
    */
    private Bundle getFromUsdcny() {
        Bundle bundleRates = new Bundle();
        int i;
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.usd-cny.com/bankofchina.htm").get();    //同序号1
            Log.i(TAG, "run:"+doc.title());
            /*Elements newsHeadlines = doc.select("#mp-itn b a");
            for (Element headline : newsHeadlines) {
                Log.i(TAG,"%s\n\t%s"+headline.attr("title")+headline.absUrl("href"));
            }*/

            //从网页转化的文档获取table标签元素部分
            Elements tables = doc.getElementsByTag("table");
            /*int i=1;
            for(Element table: tables){
                Log.i(TAG, "run: table["+i+"]="+table);
                i++;
            }*/
            Element getTable = tables.get(0);
            //                Log.i(TAG, "run:getTable"+getTable);
            //从table部分获取td标签元素部分
            Elements tds = getTable.getElementsByTag("td");
            /*i=1;
            for(Element td: tds){
                Log.i(TAG, "run: td["+i+"]="+td);
                Log.i(TAG, "run: td["+i+"].text="+td.text());
                Log.i(TAG, "run: td["+i+"].html="+td.html());
                i++;
            }*/
            //从td部分获取数据并保存在bundleRates中
            Element getTdCountry, getTdRate;
            //j=0;
            String strKeyRate = null;
            for(i=0;i<tds.size();i+=6){
                getTdCountry = tds.get(i);
                getTdRate = tds.get(i+5);
                String getCountry = getTdCountry.text();
                String getRate = getTdRate.text();
                /*Log.i(TAG, "run:Country["+j+"]="+getCountry+": Rate="+getRate);
                j++;*/
                switch(getCountry){
                    case "美元":
                        strKeyRate ="dollarRateWeb";
                        break;
                    case "欧元":
                        strKeyRate ="euroRateWeb";
                        break;
                    case "韩元":
                        strKeyRate ="wonRateWeb";
                        break;
                    default:
                }
                if(strKeyRate != null){
                    bundleRates.putDouble(strKeyRate, (double)Math.round(Double.parseDouble(getRate)/100*1000000)/1000000);
                    strKeyRate=null;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundleRates;
    }

    //## 四舍五入：保留
    //Android 一个活动引用另一个类的方法只能是static方法。因为随时一个活动会销毁以保证运行内存足够，而static方法提前在内存中一直保留
    public static Double doubleRound(double x, int num){
        return (double)Math.round(x*Math.pow(10,num))/Math.pow(10,num);
    }
    public static Double doubleRound(float x, int num){
        return (double)Math.round(x*Math.pow(10,num))/Math.pow(10,num);
    }
    public static Double doubleRound(String str, int num){
        return (double)Math.round(Double.parseDouble(str)*Math.pow(10,num))/Math.pow(10,num);
    }
}



 /*   //网络数据处理inputStream2String
    private String inputStream2String(InputStream inputStream) throws IOException{  //输入流转出为字符串
        final int bufferSize = 1024;
        final char [] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "gb2312"); //编码等于网络源代码meta标签中的charset
        int rsz=0;
        while(true){
            rsz = in.read(buffer, 0, buffer.length);
            if(rsz<0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }
}*/
