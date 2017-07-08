package com.panku.draglayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static com.panku.draglayout.Cheeses.NAMES;
import static com.panku.draglayout.Cheeses.sCheeseStrings;

public class MainActivity extends AppCompatActivity {

    private DragLayoutView draglayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        draglayout = (DragLayoutView) findViewById(R.id.draglayout);
        ListView lv_letf = (ListView) findViewById(R.id.lv_left);
        ListView lv_main = (ListView) findViewById(R.id.lv_main);
        lv_letf.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sCheeseStrings) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.WHITE);//设置字体颜色
                return tv;
            }
        });
        lv_main.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, NAMES));


        MyLinearLayout mLinearLayout = (MyLinearLayout) findViewById(R.id.fl_main);
        //设置主面板的拦截事件
        mLinearLayout.setDragLayout(draglayout);
    }
}
