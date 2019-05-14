package com.example.zweek5;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//整个布局为自带
public class RateListActivity extends ListActivity implements Runnable{
    //列表Data
    List<String> initRateList, updateRateList;
    Handler handler;
    Message message;
    private final String TAG = "RateListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //由于父类ListActivity已经有布局所以不需要布局填充
        //setContentView(R.layout.activity_rate_list);

        initRateList = new ArrayList<String>();
        for(int i=1; i<100; i++){
            initRateList.add("rate"+i);
        }

        //adapter
        ListAdapter listAdapter = new ArrayAdapter<String>(RateListActivity.this, android.R.layout.simple_expandable_list_item_1, initRateList);   //simple_expandable_list_item_1为安卓自带
        setListAdapter(listAdapter);
        //子线程启用
        Thread thread = new Thread(this);
        thread.start();
        handler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        updateRateList = (List<String>) msg.obj;
                        ListAdapter listAdapter = new ArrayAdapter<String>(RateListActivity.this, android.R.layout.simple_expandable_list_item_1, updateRateList); //第一个参数若为this则为handler
                        setListAdapter(listAdapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };

    }

    //获取网络数据:放入List（initRateList）带回主线程
    @Override
    public void run() {
        //局部变量retList为网页获取
        List<String> retList = new ArrayList<String>();
        //获取message对象返回主线程;设置标号what：=0
        message = handler.obtainMessage(0);
        //获取List并存储
        message.obj = getFromUsdcny(retList);
        //发送数据
        handler.sendMessage(message);
        Log.i(TAG, "rate has been update in run(son thread)");

    }
    //从网页获取List方法：输入初始化的list返回list
    private List<String> getFromUsdcny(List<String> retList) {
        int i;
        Document doc = null;
        try {
            Thread.sleep(1000);
            doc = Jsoup.connect("http://www.usd-cny.com/bankofchina.htm").get();    //同序号1
            Log.i(TAG, "run:"+doc.title());
            //从网页转化的文档获取table标签元素部分
            Elements tables = doc.getElementsByTag("table");
            Element getTable = tables.get(0);
            //从table部分获取td标签元素部分
            Elements tds = getTable.getElementsByTag("td");
            Element getTdCountry, getTdRate;
            for(i=0;i<tds.size();i+=6){
                getTdCountry = tds.get(i);
                getTdRate = tds.get(i+5);
                String getCountry = getTdCountry.text();
                String getRate = getTdRate.text();
                retList.add(getCountry+":"+getRate);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return retList;
    }

}


/*
* ListActivity为屏幕全列表
* adapter适配器
* ListView 整个屏幕显示的若干个TextView
*
*/