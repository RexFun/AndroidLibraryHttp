package com.rexfun.androidlibrary.http;

import com.rexfun.androidlibraryhttp.HttpActionObj;
import com.rexfun.androidlibraryhttp.HttpResultObj;
import com.rexfun.androidlibraryhttp.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac373 on 15/12/29.
 */
public class Test {
    public static void main(String args[]) throws Exception {
        new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //Log.e("当前线程：", "" + Thread.currentThread().getName());
                Map<String,String> m = new HashMap<String,String>();
                m.put("pid", "2");
                m.put("rownum", "0");
                m.put("pagesize", "5");
                HttpActionObj actionObj = new HttpActionObj("http://192.168.19.123:8181/paper/client/papermodel/getByPid.action", m);
                HttpResultObj<String> r = HttpUtil.submitHttpAction(actionObj, String.class, 5000, 5000, "GET");

                if(r.isSuc()) {
                    System.out.println("/* success */:"+r.getData());
                } else {
                    System.out.println("/* err */:"+r.getErrMsg());
                }
            }
        }).start();
    }
}
