package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.coolweather.app.R;

/**
 * Created by libing on 2016/4/8.
 */
public class MainActivity extends Activity{
    private EditText editText;
    private Button button;
    private ImageView imageView;
    private ImageLoader mimageLoader=new ImageLoader();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editText = (EditText) findViewById(R.id.edit);
        button= (Button) findViewById(R.id.button);
        imageView= (ImageView) findViewById(R.id.imageTest);
        // new ImageLoader().showImageByThread(imageView,"http://img3.imgtn.bdimg.com/it/u=1631136700,1620277457&fm=11&gp=0.jpg");
        //mimageLoader.showImageByAsyncTask(imageView, "http://img3.imgtn.bdimg.com/it/u=1631136700,1620277457&fm=11&gp=0.jpg");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //搜索操作
                //跳转到第二个activity并且把text值传过去
                Intent intent = new Intent();
                intent.setClassName(getApplicationContext(),"com.coolweather.app.activity.WeatherActivity");
                Bundle b = new Bundle();
                b.putString("key",editText.getText().toString());
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }
    //得到城市信息列表

}
