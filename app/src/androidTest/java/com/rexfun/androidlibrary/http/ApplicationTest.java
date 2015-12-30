package com.rexfun.androidlibrary.http;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.rexfun.androidlibraryhttp.HttpActionObj;
import com.rexfun.androidlibraryhttp.HttpResultObj;
import com.rexfun.androidlibraryhttp.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

        new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.i("当前线程", "" + Thread.currentThread().getName());
                Map<String,String> m = new HashMap<String,String>();
                m.put("pid", "2");
                m.put("rownum", "0");
                m.put("pagesize", "5");
                Log.i("请求前", "");
                HttpActionObj actionObj = new HttpActionObj("http://192.168.19.123:8181/paper/client/papermodel/getPageByPid.action", m);
                HttpResultObj<String> r = HttpUtil.submitHttpAction(actionObj, String.class, 5000, 5000, "GET");
                Log.i("请求成功", r.isSuc() + "");
                if(r.isSuc()) {
                    System.out.println("/* success */:"+r.getData());
                } else {
                    System.out.println("/* err */:"+r.getErrMsg());
                }
            }

        }).start();
    }
}