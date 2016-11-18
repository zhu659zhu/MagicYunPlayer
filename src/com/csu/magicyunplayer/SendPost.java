package com.csu.magicyunplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
 /*
 * 用于向指定网址POST发送数据
 */
public class SendPost {
	Context context;
	public SendPost(Context contexts){
		this.context=contexts;
	}
	public String sendPostParams(String url,String params){
		PrintWriter out=null;
		BufferedReader bufferedReader=null;
		String result="";
		try {
			URL realurl=new URL(url);
			URLConnection conn=realurl.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			//conn.setRequestProperty("Cache-Control", "no-cache");
			//conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(15* 1000);
			conn.setDoInput(true);
			out=new PrintWriter(conn.getOutputStream());
			out.print(params);
			out.flush();
			
			bufferedReader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"GBK"));
			String line;
			while ((line=bufferedReader.readLine())!=null) {
				result+="\n"+line;
			}
			//获取cookie
			Map<String,List<String>> map=conn.getHeaderFields();
			String head=map.toString();Log.v("", head);
			Pattern pattern=Pattern.compile("(Set-Cookie=.{1})(.+/)(],)");
			Matcher matcher=pattern.matcher(head);
			String cookies=null;
			if(matcher.find()){cookies=matcher.group(2);}Log.v("", "cook"+cookies);
			SharedPreferences sharedPreference = context.getSharedPreferences("user-info", Context.MODE_PRIVATE);
			Editor editor=sharedPreference.edit();
			editor.putString("cookie", cookies);
			editor.commit();
		}catch (SocketTimeoutException e) {
			e.printStackTrace();
			// TODO: handle exception
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				if(out!=null){out.close();}
				if(bufferedReader!=null){bufferedReader.close();}
			} catch (IOException e) {
				
			}
		}
		return result;
	}
}
