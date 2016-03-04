package com.gaoxy.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClient {

	public static String post(String url,Map<String,String> param,int timeOut){
		RequestConfig config = RequestConfig.custom().setSocketTimeout(timeOut).build();
		CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();
		HttpPost httpPost = null;
		CloseableHttpResponse response = null;
		String result = null;
		try{
			httpPost = new HttpPost(url);
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			for(String key:param.keySet()){
				params.add(new BasicNameValuePair(key, param.get(key)));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			response = client.execute(httpPost);
			response.getStatusLine().getStatusCode();
			result = EntityUtils.toString(response.getEntity(),"utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				response.close();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static String get(String url,int timeOut){
		RequestConfig config = RequestConfig.custom().setSocketTimeout(timeOut).build();
		CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();
		HttpGet httpGet = null;
		CloseableHttpResponse response = null;
		String result = null;
		try{
			httpGet = new HttpGet(url);
			response = client.execute(httpGet);
//			System.out.println(response.getStatusLine().getStatusCode());
			result = EntityUtils.toString(response.getEntity(),"utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				response.close();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
