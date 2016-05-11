package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.CoolWeatherDB;
import com.coolweather.app.util.HttpCallbackListence;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libing on 2016/4/8.
 */
public class MainActivity extends Activity{
    private EditText editText;
    private Button button;
    private ImageView imageView;
    private ImageLoader mimageLoader=new ImageLoader();


    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTRY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    /**省列表*/
    private List<Province> provinceList;
    /**市列表*/
    private List<City> cityList;
    /**县级列表*/
    private List<County> countyList;
    /**选中的省份*/
    private Province selectedProvince;
    /**选中的城市*/
    private City selectedCity;
    /**当前选中的级别*/
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*************/
      /*  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }*/
        /***************/


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        /**
         * 城市列表选择
         */
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTRY){
                    String countyCode = countyList.get(position).getCountyCode();
//                    String countyName = countyList.get(position).getCountyName();
                    Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
//                    intent.putExtra("county_name",countyName);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //默认加载省级数据
        queryProvinces();


        editText = (EditText) findViewById(R.id.edit);
        button= (Button) findViewById(R.id.button);
//        imageView= (ImageView) findViewById(R.id.imageTest);
//         new ImageLoader().showImageByThread(imageView,"http://img3.imgtn.bdimg.com/it/u=1631136700,1620277457&fm=11&gp=0.jpg");
//        mimageLoader.showImageByAsyncTask(imageView, "http://img3.imgtn.bdimg.com/it/u=1631136700,1620277457&fm=11&gp=0.jpg");
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
    /**
     * 查询全国的省，优先从数据库查询，如果数据库没有，再去服务器上查询
     */
    private void queryProvinces(){
        provinceList= coolWeatherDB.loadProvince();
        if(provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询选中省内的所有市，优先从数据库查询，如果没有再到服务器去查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    /**
     * 查询选中市所有的县，优先从数据库查询，如果没有再到服务器去查询
     */
    private void queryCounties(){
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTRY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    /**
     * 根据传入的代号和类型，从服务器查询省市县数据
     */
    private void queryFromServer(String code, final String type) {
        String address;
        if(!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else {
            address="http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListence() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result= Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    /**
                     * 通过runOnUiThread（）方法回到主线程处理逻辑
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭进度对话框
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });





















    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    /**
     * 捕获back按键 ，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出
     */
    @Override
    public void onBackPressed() {
        if(currentLevel==LEVEL_COUNTRY){
            queryCities();
        }else if (currentLevel==LEVEL_CITY){
            queryProvinces();
        }else {
            finish();
        }
    }


}
