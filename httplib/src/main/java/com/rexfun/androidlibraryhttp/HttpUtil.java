package com.rexfun.androidlibraryhttp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil
{
	private static RequestQueue mQueue;
	
	public static <T> HttpResultObj<T> submitHttpAction(HttpActionObj actionObj, Class<T> clazz, String method)
	{
		return submitHttpAction(actionObj, clazz, 30000, 30000, method);
	}
	@SuppressWarnings("unchecked")
	public static <T> HttpResultObj<T> submitHttpAction(HttpActionObj actionObj, Class<T> clazz, int connectionTimeout, int soTimeout, String method)
	{
		Log.i("<* http url *>", actionObj.getUrl());
		HttpResultObj<T> o = new HttpResultObj<T>();
		try
		{
			//设置网络超时
			HttpClient client =  new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), connectionTimeout);//连接建立的超时时间
			HttpConnectionParams.setSoTimeout(client.getParams(), soTimeout);//连接建立后，没有收到response的超时时间
			//发送请求
			HttpResponse resp = null;
			if(method.equals("POST"))
			{
				//初始化HttpPost
				HttpPost req = new HttpPost(actionObj.getUrl());
				req.setEntity(new UrlEncodedFormEntity(actionObj.getParams(), HTTP.UTF_8));
				//执行请求
				resp = client.execute(req);
			}
			else if(method.equals("GET"))
			{
				String url = actionObj.getUrl();
				//初始化HttpGet
				for (int i=0; i<actionObj.getParams().size(); i++)
				{
					if (i==0)
						url += "?" + actionObj.getParams().get(i).getName() + "=" + actionObj.getParams().get(i).getValue();
					else
						url += "&" + actionObj.getParams().get(i).getName() + "=" + actionObj.getParams().get(i).getValue();
				}
				HttpGet req = new HttpGet(url);
				//执行请求
				resp = client.execute(req);
			}
			else
			{
				o.setSuc(false);
				o.setErrMsg("请求失败：请正确填写 GET / POST");
				Log.i("<* 请求失败：*>", "请正确填写 GET / POST");
			}
			//请求响应
			if(resp.getStatusLine().getStatusCode() == 200)
			{
				if(clazz.isAssignableFrom(String.class))
				{
					o.setData((T) EntityUtils.toString(resp.getEntity(),"UTF-8"));
				}
				else if(clazz.isAssignableFrom(Map.class))
				{
					String rs = EntityUtils.toString(resp.getEntity(),"UTF-8");
					String[] rsArr = rs.split("&");
					Map<String,String> m = new HashMap<String,String>();
					for(String r : rsArr)
					{
						String[] mapArr = r.split("=");
						m.put(mapArr[0], mapArr[1]);
					}
					o.setData((T) m);
				}
				else if(clazz.isAssignableFrom(Bitmap.class))
				{
					InputStream is = resp.getEntity().getContent();
					o.setData((T) BitmapFactory.decodeStream(is));
				}
				else if(clazz.isAssignableFrom(InputStream.class))
				{
					o.setData((T) resp.getEntity().getContent());
				}
				o.setContentLength(resp.getEntity().getContentLength());
				o.setSuc(true);
				o.setSucMsg("请求成功："+resp.getStatusLine().getStatusCode());
			}
			else 
			{
				o.setSuc(false);
				o.setErrMsg("请求失败："+resp.getStatusLine().getStatusCode());
			}
		}
		catch(Exception e)
		{
			o.setSuc(false);
			o.setErrMsg("请求异常："+e.getMessage());
			e.printStackTrace();
		}
		return o;
	}

	public static void submitVollyHttpActionBitmap(Context ctx, final HttpActionObj actionObj, final ImageView v)
	{
		mQueue = Volley.newRequestQueue(ctx);
		Response.Listener<Bitmap> sucListener = new Response.Listener<Bitmap>()
		{  
            @Override  
            public void onResponse(Bitmap response)
            {
            	v.setImageBitmap(response);
            }  
        };
        Response.ErrorListener errListener = new Response.ErrorListener()
        {  
            @Override  
            public void onErrorResponse(VolleyError error)
            {  
                Log.e("TAG", error.getMessage(), error);
//                v.setImageResource(R.drawable.默认图片);
            }  
        };
        ImageRequest imageRequest = new ImageRequest(actionObj.getUrl(), sucListener, 0, 0, Config.RGB_565, errListener)
        {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError
			{
				return actionObj.getParamsMap();
			}
		};
		mQueue.add(imageRequest);
	}
	
	public static String submitVollyHttpAction(Context ctx, final HttpActionObj actionObj)
	{
		final String[] result = null;
		mQueue = Volley.newRequestQueue(ctx);
		Response.Listener<String> sucListener = new Response.Listener<String>() {  
			@Override  
			public void onResponse(String response) {  
				Log.d("TAG", response);
				result[0] = response;
			}  
		};
		Response.ErrorListener errListener = new Response.ErrorListener() {  
			@Override  
			public void onErrorResponse(VolleyError error) {  
				Log.e("TAG", error.getMessage(), error);  
			}  
		};
		StringRequest stringRequest = new StringRequest(actionObj.getUrl(), sucListener, errListener){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError
			{
				return actionObj.getParamsMap();
			}
		}; 
		mQueue.add(stringRequest);
		return result[0];
	}
	
	/**
	 * Long类型id数组转成String类型（如new Long[]{1，2，3，4}转"1,2,3,4"）
	 * @param ids Long[]
	 * @return String
	 */
	public static String idsConvertToStr(Long[] ids)
	{
		String _ids = "";
		for(int i=0;i<ids.length;i++)
		{
			_ids += ids[i];
			if(i+1<ids.length) _ids += ",";
		}
		Log.i("列名串",_ids);
		return _ids;
	}
}
