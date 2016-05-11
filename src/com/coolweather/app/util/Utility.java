package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by libing on 2016/4/8.
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if (!TextUtils.isEmpty(response)){
            String [] allProvinces = response.split(",");
            if(allProvinces!=null&&allProvinces.length>0){
                for (String province : allProvinces) {
                    String [] array = province.split("\\|");
                    Province province1 = new Province();
                    province1.setProvinceCode(array[0]);
                    province1.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province1);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String [] allCities = response.split(",");
            if(allCities!=null&&allCities.length>0){
                for (String c : allCities) {
                    String [] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if(allCounties!=null && allCounties.length>0){
                for (String allCounty : allCounties) {
                    String [] array = allCounty.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    public static void handleWeatherResponse(Context context,String response){
        System.out.println(response+"========服务器返回的数据=============");
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("retData");
            System.out.println(weatherInfo+"=========解析出的天气信息==============");
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

    /**
     * 将服务器返回的数据存储到SharedPreferences中
     * @param context
     * @param cityName
     * @param cityCode
     * @param weatherDesp
     * @param temp1
     * @param temp2
     * @param publishTime
     */
    public static void saveWeatherInfo(Context context, String cityName,String cityCode, String weatherDesp, String temp1, String temp2, String publishTime) {
        System.out.println("+========准备把天气信息保存到数据库中======");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("city_code", cityCode);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("temp1", temp1+"℃");
        editor.putString("temp2", temp2+"℃");
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
        System.out.println("保存完成");
    }
    public static void saveWeatherCode(Context context,String weatherCode){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("weather_code",weatherCode);
        editor.commit();
    }
}
