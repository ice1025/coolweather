package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListence;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

/**
 * Created by libing on 2016/4/8.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    //城市名称
    private TextView cityNameText;
    //发布时间
    private TextView publishText;
    //天气信息
    private TextView weatherDespText;
    //最低气温
    private TextView temp1Text;
    //最高气温
    private TextView temp2Text;
    //当前日期
    private TextView currentDataText;

    private Button changeCity;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化控件
        weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText= (TextView) findViewById(R.id.city_name);
        publishText= (TextView) findViewById(R.id.publish_text);
        weatherDespText= (TextView) findViewById(R.id.weather_desp);
        temp1Text= (TextView) findViewById(R.id.temp1);
        temp2Text= (TextView) findViewById(R.id.temp2);
        currentDataText= (TextView) findViewById(R.id.current_data);
        changeCity= (Button) findViewById(R.id.changeCity);
        changeCity.setOnClickListener(this);
        //得到所传的值
        String res = getIntent().getExtras().getString("key");
        //判断res是拼音还是汉字

        //通过res来得到json数据
        String address = "http://apistore.baidu.com/microservice/weather?citypinyin="+res;
        queryFromServer(address);
    }

    private void queryFromServer(final String address) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListence() {
            @Override
            public void onFinish(final String response) {
                Utility.handleWeatherResponse(getApplication(),response);
                //显示本地天气
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
        currentDataText.setText(prefs.getString("current_date",""));
    }

    @Override
    public void onClick(View v) {
        if(R.id.changeCity==v.getId()){
            //切换城市即跳转到主activity
            Intent intent = new Intent(getApplication(),MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        //监听back键跳转到主activity
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
