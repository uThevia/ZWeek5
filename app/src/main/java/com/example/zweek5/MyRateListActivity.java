package com.example.zweek5;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.zweek5.R.layout.activity_my_rate_list;

//自定义布局中ListView为自带
public class MyRateListActivity extends AppCompatActivity implements Runnable, AdapterView.OnItemClickListener {
    private ListView myListView;
    private List<String> initRateList, updateRateList;
    private Handler handler;
    private Message message;
    private final String TAG = "MyRateListActivity";
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_my_rate_list);

        myListView = (ListView) findViewById(R.id.my_rate_list);
        //初始化list数据
        initRateList = new ArrayList<String>();
        for(int i=1; i<100; i++){
            initRateList.add("rate"+i);
        }

        //adapter:返回View绑定数据和控件
            //simple_list_item_1控制多行单列列表
        adapter = new ArrayAdapter<String>(MyRateListActivity.this,
                android.R.layout.simple_list_item_1,
                initRateList);
            //由于自定义包含列表的布局未继承父类ListActivity就无法继承setListAdapter方法，只能用ListView.setAdapter方法设置adapter
        myListView.setAdapter(adapter);
        //当列表为空时显示nodata控件
        myListView.setEmptyView(findViewById(R.id.nodata));
        myListView.setOnItemClickListener(MyRateListActivity.this);

        /*//子线程启用
        Thread thread = new Thread(this);
        thread.start();
        handler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        updateRateList = (List<String>) msg.obj;
                        adapter = new ArrayAdapter<String>(MyRateListActivity.this, //若为this则为handler
                                android.R.layout.simple_expandable_list_item_1,
                                updateRateList);
                        myListView.setAdapter(adapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };*/


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
    private List<String> getFromUsdcny(List<String> setList) {
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
                //加入list
                setList.add(getCountry+":"+getRate);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return setList;
    }


    //删除数据
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Log.i(TAG, "onItemClick: parent" + parent); //parent 为ListView对象
        Log.i(TAG, "onItemClick: position" + position);
        //点击移除数据:直接法 只适用于ArrayAdapter的Adapter
        adapter.remove(parent.getItemAtPosition(position));
        //告诉ListView已经更新并刷新
        adapter.notifyDataSetChanged();


    }
}
