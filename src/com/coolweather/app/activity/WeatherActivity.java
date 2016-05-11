package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListence;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLOutput;

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
        if(!TextUtils.isEmpty(res)){
            /**
             * 判断方法未完善,目前默认用中文
             */
            System.out.println(res+"输入的内容======");
            if(true){
                System.out.println("暂默认执行这里");
                queryWeatherBycityNameZHONGWEN(res);
            }else{//拼音
                queryWeatherByCityName(res);
            }

        }

        /***通过点击城市列表过来的***/
        String countyCode = getIntent().getExtras().getString("county_code");

        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中。。。");
           // weatherInfoLayout.setVisibility(View.INVISIBLE);
            //cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);//根据县级代号查询天气
        }else{
            //没有县级代号时，直接显示本地天气
            showWeather();
        }

    }
    /**
     * 根据汉子来得到天气数据
     */
    private void queryWeatherBycityNameZHONGWEN(String hanzi){
        try {
            String re= URLEncoder.encode(hanzi,"utf-8");
            String address = "http://apistore.baidu.com/microservice/weather?cityname="+re;
            queryFromServer(address,"weatherCode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据拼音来得到天气数据
     */
    private void queryWeatherByCityName(String pinyin){
     //通过res来得到json数据
        String address = "http://apistore.baidu.com/microservice/weather?citypinyin="+pinyin;
        queryFromServer(address,"weatherCode");
    }


    /**根据县级代号查询天气代号**/
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        System.out.println("======queryWeatherCode()=================");
        queryFromServer(address,"countyCode");
    }

    /**
     * 查询天气代号对应的天气
     */
    private void queryWeatherInfo(String code){
        System.out.println(code+"=========天气代码=========");
        String address = "http://apistore.baidu.com/microservice/weather?cityid="+code;
        //http://apistore.baidu.com/microservice/weather?cityid=101010100
        System.out.println("======queryWeatherInfo()====准备去 queryFromServer(address,weatherCode);=============");
        queryFromServer(address,"weatherCode");
    }

    private void queryFromServer(final String address,final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListence() {
            @Override
            public void onFinish(final String response) {
                /****判断***/
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        System.out.println(type+"===========???countyCode===");
                        System.out.println(address);
                        System.out.println(response+"=========回调++服务器返回的数据===============");
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if(array!=null&&array.length==2){
                            String weatherCode = array[1];
                            System.out.println(weatherCode+"=====天气代号weatherCode========");
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)) {
                    System.out.println("我从queryWeatherInfo（address，”weatherCode“）带着地址 和天气代码来了");
                    System.out.println(address);
                    System.out.println(type+"===========???weatherCode===");
                   // 处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    System.out.println("我要开始显示天气了");
                    //显示本地天气
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
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
        System.out.println("我是传说中的showWeather()=========我要从数据库中取天气信息了===========");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
        currentDataText.setText(prefs.getString("current_date",""));
        System.out.println(cityNameText.getText());
        System.out.println(temp1Text.getText());
        System.out.println(temp2Text.getText());
        System.out.println(weatherDespText.getText());
        System.out.println(publishText.getText());
        System.out.println(currentDataText.getText());
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
