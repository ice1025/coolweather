package com.coolweather.app.util;

/**
 * Created by libing on 2016/4/8.
 */
public interface HttpCallbackListence {
    void onFinish(String response);
    void onError(Exception e);
}
