package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by libing on 2016/4/8.
 */
public class Utility {

    public static void handleWeatherResponse(Context context,String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("retData");

            String cityName = weatherInfo.getString("city");
            URLEncoder.encode(cityName,"utf-8");
            String cityCode = weatherInfo.getString("citycode");
            URLEncoder.encode(cityCode,"utf-8");
            String weatherDesp = weatherInfo.getString("weather");
            URLEncoder.encode(weatherDesp,"utf-8");
            String temp1 = weatherInfo.getString("l_tmp");
            URLEncoder.encode(temp1,"utf-8");
            String temp2 = weatherInfo.getString("h_tmp");
            URLEncoder.encode(temp2,"utf-8");
            String publishTime = weatherInfo.getString("time");
            URLEncoder.encode(publishTime,"utf-8");
            saveWeatherInfo(context,cityName,cityCode,weatherDesp,temp1,temp2,publishTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveWeatherInfo(Context context, String cityName,String cityCode, String weatherDesp, String temp1, String temp2, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("city_code", cityCode);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
    }
}
