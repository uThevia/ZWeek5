package com.example.zweek5;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//整个活动继承于ListActivity但自定义适配器Adapter
public class MyRateListActivityTwo
        extends ListActivity    //ListActivity类：实现系统自带ListView
        implements Runnable,  //子线程运行
            AdapterView.OnItemClickListener, //ListView单击事件
            AdapterView.OnItemLongClickListener
{
    private final String TAG = "MyRateListActivityTwo";
    private Handler handler = new Handler();
    Message message;
    private ArrayList<HashMap<String, String>> listItemInit, listItemUpdate; //列表
    private SimpleAdapter adapterList;  //系统适配器
    private MyAdapter myAdapter;    //自定义适配器


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListView();

        //使用自定义adapter：MyAdapter
        myAdapter = new MyAdapter(MyRateListActivityTwo.this,
                R.layout.list_item,
                listItemInit);
        this.setListAdapter(myAdapter);
        /*使用系统自定义adapter：
        this.setListAdapter(adapterList);
        */

        //子线程启用
        Thread thread = new Thread(this);
        thread.start();
        handler =new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        listItemUpdate = (ArrayList<HashMap<String, String>>) msg.obj;
                        myAdapter = new MyAdapter(MyRateListActivityTwo.this,
                                R.layout.list_item,
                                listItemUpdate);
                        MyRateListActivityTwo.this.setListAdapter(myAdapter);
                        break;
                }
                super.handleMessage(msg);
            }
        };


        //## 点击列表事件监听
        //方法一：接口
        getListView().setOnItemClickListener(this);
        /*方法二：匿名类
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        */

        //## 长按事件处理
        getListView().setOnItemLongClickListener(this);



    }

    //初始化ListView
    private void initListView(){
        HashMap<String, String> hashMap;
        listItemInit = new ArrayList<HashMap<String, String>>();
        for(int i=1; i <= 10 ;i++){
            hashMap = new HashMap<String, String>();
            hashMap.put("ItemTitle", "ItemTitle"+ i);
            hashMap.put("ItemDetail", ""+i);
            listItemInit.add(hashMap);
        }

        /*
        //使用系统自定义adapter：SimpleAdapter
        adapterList = new SimpleAdapter(MyRateListActivityTwo.this, //上下文
                listItem,   //数据源
                R.layout.list_item, //布局
                new String [] {"ItemTitle", "ItemDetail"},  //键
                new int[] {R.id.itemTitle, R.id.itemDetail} //值所在控件; 与上一一对应
                );
        */
    }

    //获取网络数据:放入List（initRateList）带回主线程
    @Override
    public void run() {
        //局部变量retList为网页获取
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        //获取message对象返回主线程;设置标号what：=0
        message = handler.obtainMessage(0);
        //获取List并存储
        message.obj = getFromUsdcny(list);
        //发送数据
        handler.sendMessage(message);
        Log.i(TAG, "run: rate has been update in run(son thread)");

    }
    //从网页获取List方法：输入初始化的list返回list
    private ArrayList<HashMap<String, String>> getFromUsdcny(ArrayList<HashMap<String, String>> list) {
        int i;
        Document doc = null;
        HashMap<String, String> map;
        try {
            Thread.sleep(1000);
            doc = Jsoup.connect("http://www.usd-cny.com/bankofchina.htm").get();    //同序号1
            Log.i(TAG, "getFromUsdcny:"+doc.title());
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
                Log.i(TAG, "getFromUsdcny: getCountry = " + getTdCountry+ " getRate = " + getRate);

                //把数据加入list
                map = new HashMap<String, String>();    //一定在循环内初始化map，否则map之后更新会导致之前加入list的map也会更新（它们是同一个对象）
                map.put("ItemTitle", getCountry);
                map.put("ItemDetail", getRate);
                list.add(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return list;
    }

    //接口AdapterView.OnItemClickListener : 点击ListView相应方法
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String itemTitle ,itemDetail;

        /*//输出查看参数
        Log.i(TAG, "onItemClick: parent=" + parent);
        Log.i(TAG, "onItemClick: view=" + view);
        Log.i(TAG, "onItemClick: position=" + position);
        Log.i(TAG, "onItemClick: id=" + id);*/

        //根据点击位置（position）获得HashMap对象
            //（获得的是ListView对象所以是ListView对象（由getListView获得）下的getItemAtPosition方法）
        HashMap<String, String> map = (HashMap<String, String>) getListView().getItemAtPosition(position);
        itemTitle = map.get("ItemTitle");
        itemDetail = map.get("ItemDetail");
        Log.i(TAG, "onItemClick: position: itemTitle=" + itemTitle);
        Log.i(TAG, "onItemClick: position: itemDetail=" + itemDetail);
        //根据adapter的view 即布局文件中的控件获得对象
        itemTitle = String.valueOf(((TextView)view.findViewById(R.id.itemTitle)).getText());
        itemDetail = String.valueOf(((TextView)view.findViewById(R.id.itemDetail)).getText());
        Log.i(TAG, "onItemClick: view: itemTitle=" + itemTitle);
        Log.i(TAG, "onItemClick: view: itemDetail=" + itemDetail);

        //跳转到RateCalculate
        Intent intent = new Intent(this, RateCalculate.class);
        intent.putExtra("Country", itemTitle);
        intent.putExtra("Rate", itemDetail);
        startActivity(intent);
        Log.i(TAG, "onItemClick: intent:RateCalculate");
    }

    //接口AdapterView.OnItemLongClickListener : 长按ListView相应方法
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   final int position,  //final确保方法内的匿名类可以访问该变量
                                   long id) {
        Log.i(TAG, "onItemLongClick: position" + position);


        //## 构造对话框:删除提醒
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //标题 参数：整型(用于R.values.strings或字符串
        builder.setTitle("Prompt");
        builder.setMessage("Confirm whether to delete.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "DialogInterface.OnClickListener.onClick:");
                //删除
                listItemUpdate.remove(position);
                listItemUpdate.remove(position);
                //刷新
                myAdapter.notifyDataSetChanged();
                Log.i(TAG, "onItemLongClick: size=" + listItemUpdate.size());
            }
        });
        builder.setNegativeButton("No",null);
        builder.create().show();
        //返回的是：是否屏蔽点击事件即onItemClick方法
        return true;
    }
}
