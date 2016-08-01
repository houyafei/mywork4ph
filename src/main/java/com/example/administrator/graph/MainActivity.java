package com.example.administrator.graph;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.graph.views.GraphLines;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final int VALUE =0x999 ;

    private static ArrayList<ArrayList<Integer>> mValuesList4Save = new ArrayList<>();

    private GraphLines mLines ;

    private ArrayList<Integer> mValues1 = new ArrayList<>();
    private ArrayList<Integer> mValues2 = new ArrayList<>();

    private ArrayList<ArrayList<Integer>> mValuesList = new ArrayList<>();
   // private SensorOpen seneor ;//= new SensorOpen(MainActivity.this);

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mValues1.size()>40){
                //mValues.clear();
                mValues1.remove(0);
                mValues2.remove(0);
                System.out.println();
                handler.sendEmptyMessageDelayed(VALUE, 100);
            }else{

                int random1 = (int)(Math.random()*100) ;
                mValues1.add(random1);
                int random2 = (int)(Math.random()*100) ;
                mValues2.add(random2);
                mValuesList.add(mValues1);
                mValuesList.add(mValues2) ;
                mLines.setmVluesArray(mValuesList);

//                System.out.print(random+"--");

                handler.sendEmptyMessageDelayed(VALUE,200);

            }
            //System.out.println(mValues);
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLines = (GraphLines) findViewById(R.id.id_lines);



        handler.sendEmptyMessageDelayed(VALUE,200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mValuesList4Save!=null){
            mValuesList = mValuesList4Save;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mValuesList = mValuesList4Save;
    }
}
