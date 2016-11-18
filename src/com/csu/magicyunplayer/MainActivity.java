package com.csu.magicyunplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MainActivity extends Activity  {


	private CNHUpdate mCNHUpdate;
	private static MainActivity instance;
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>(); 
	ListView list;
	SimpleAdapter mSchedule;
	//创建进度条
	ProgressDialog proDia;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this; //获取应用程序context
		
		new Thread(checkupdate).start();  //开启检查更新线程
		
		//绑定XML中的ListView，作为Item的容器  
	    list = (ListView) findViewById(R.id.listView1);  
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view,
	    	int position, long id) {
	    		//Toast.makeText(MainActivity.this, mylist.get(position).get("ItemText") , Toast.LENGTH_SHORT).show();
	    		Random ran =new Random(System.currentTimeMillis()); 
	    		Uri uri = Uri.parse(mylist.get(position).get("ItemText")+"?rnd="+ran.nextInt(1000));  
	    		//调用系统自带的播放器 
	    		    Intent intent = new Intent(Intent.ACTION_VIEW); 
	    		    intent.setDataAndType(uri, "video/mp4"); 
	    		    startActivity(intent);
	    	}
	    	});
	    ItemOnLongClick2();
	    RefreshList();
	}
	
	private void ItemOnLongClick2() { 
		list.setOnItemLongClickListener(new OnItemLongClickListener() { 

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, 
                                final int arg2, long arg3) { 
                		Toast.makeText(MainActivity.this, "长按收藏...", Toast.LENGTH_SHORT).show();
                        return true; 
                } 
        }); 

} 
	
	public String getSDPath(){ 
	       File sdDir = null; 
	       boolean sdCardExist = Environment.getExternalStorageState()   
	                           .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在 
	       if   (sdCardExist)   
	       {                               
	         sdDir = Environment.getExternalStorageDirectory();//获取跟目录 
	      }   
	       return sdDir.toString(); 
	       
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {      //接受处理各线程传送的信息
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            if(msg.what==1)  //msg.what=1时为检查更新 参数为UpON
            {
	            String UpON = msg.getData().getString("UpON");
	            if(UpON=="1") // 需要升级
	            {
		            Toast.makeText(MainActivity.this, "该软件需要升级", Toast.LENGTH_SHORT).show();
		            mCNHUpdate = new CNHUpdate(instance);
		    		mCNHUpdate.checkUpdateInfo();
	            }
            }
            if(msg.what==2)  //msg.what=2时为更新列表
            {
            	try
            	{
		            String resstr = msg.getData().getString("resstr");
			        
			        Pattern p = Pattern.compile("<div class=\"tit\">([\\w\\W]*?)</script>");
			        Matcher m = p.matcher(resstr);
			        ArrayList<String> strs = new ArrayList<String>();
			        while (m.find()) {
			            strs.add(m.group(1));            
			        } 
			        
			      //绑定XML中的ListView，作为Item的容器  
	        	    ListView list = (ListView) findViewById(R.id.listView1);  
	            	
	            	//生成动态数组，并且转载数据  
	        	    
	        	    mylist.clear();
	        	    for (String s : strs){
			        	Pattern pattern = Pattern.compile("href=\"[\\w\\W]*?(http://[\\w\\W]+?m3u8[\\w\\W]*?)\"");    
			        	Pattern pattern2 = Pattern.compile("NBA[\\w\\W]+?</span>([\\w\\W]+?)</div>");  
			        	Pattern pattern3 = Pattern.compile("<span class=\"[\\w\\W]+?\">([\\w\\W]+?)</span>");  
			            Matcher matcher = pattern.matcher(s);    
			            Matcher matcher2 = pattern2.matcher(s); 
			            Matcher matcher3 = pattern3.matcher(s); 
			            if(matcher.find())   
			            {
			            	HashMap<String, String> map = new HashMap<String, String>();
			            	if(matcher2.find())
			            	{
			            		if(matcher3.find())
			            			map.put("ItemTitle", matcher2.group(1)+" at "+matcher3.group(1));  
			            		else
			            			map.put("ItemTitle", matcher2.group(1));
			            	}
			            	else
			            		map.put("ItemTitle", "NBA");
		        	        map.put("ItemText", matcher.group(1)); 
		        	        mylist.add(map); 
			            }
	
			        }    
	        	    
	        	    //生成适配器，数组===》ListItem  
	        	    mSchedule = new SimpleAdapter( MainActivity.this, //没什么解释  
	        	                                                mylist,//数据来源   
	        	                                                R.layout.my_listitem,//ListItem的XML实现  
	        	                                                  
	        	                                                //动态数组与ListItem对应的子项          
	        	                                                new String[] {"ItemTitle", "ItemText"},   
	        	                                                  
	        	                                                //ListItem的XML文件里面的两个TextView ID  
	        	                                                new int[] {R.id.ItemTitle,R.id.ItemText});  
	        	    //添加并且显示  
	        	    list.setAdapter(mSchedule); 
	        	    if (mSchedule.isEmpty())
	        	    {
	        	    	new  AlertDialog.Builder(instance)    
	        	    	.setTitle("提示")  
	                    .setMessage("暂无直播源~" )  
	                    .setPositiveButton("确定" ,  null )  		             
	                    .show(); 
	        	    }
	        	    proDia.dismiss();//隐藏对话框
            	}
        	    catch(Exception e){
		            new  AlertDialog.Builder(instance)    
                    .setTitle("提示")  
                    .setMessage("刷新失败！" )  
                    .setPositiveButton("确定" ,  null )  		             
                    .show();  
        	    }
            }
        }
       };
	
	private Runnable checkupdate = new Runnable()    //定义一个新的线程，用来检查更新
	  {  
	    public void run()  
	    {
	      {  
	    	  try
		        {
		          	int localVersion =getVersionName(); 
		            String pathurl = "http://zhuhaidong.vv.si/app/NBAPlayer.php";   //检查更新地址
		            URL url = new URL(pathurl); 
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
		            conn.setReadTimeout(1500); 
		            conn.setRequestMethod("GET"); 
		            InputStream inStream = conn.getInputStream(); 
		            int netVersion=Integer.parseInt(inputStream2String(inStream));
		            Message msg = new Message(); //从此行开始进行线程通讯
		            msg.what=1;
		            Bundle bundle = new Bundle();
		            String UpON;
		            if(netVersion>localVersion)
		            {
		            	UpON = "1";
		            }
		            else
		            {
		            	UpON = "0";
		            }
		            bundle.putString("UpON",UpON);  //往Bundle中存放数据   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
		        }
		        catch (Exception e)

		        {
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };

    private int getVersionName() throws Exception   //获取当前版本号函数
    {  
            PackageManager packageManager = getPackageManager();  
            // getPackageName()是你当前类的包名，0代表是获取版本信息  
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);  
            int version = packInfo.versionCode;  
            return version;  
    } 
    
    public static String inputStream2String(InputStream   is) throws IOException{   //类型转换函数
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        int i=-1; 
        while((i=is.read())!=-1){ 
        baos.write(i); 
        } 
       return baos.toString(); 
    }
    
    public void PostClick(View v)  
    {
    	RefreshList();
    }
	
    public void RefreshList()
    {
    	CreateLoading();
		new Thread(new Runnable() {
			@Override
			public void run() {
				String form="";
				SendPost sendPost= new SendPost(MainActivity.this);
				String resstr = sendPost.sendPostParams("http://360bo.tv/cat/nbacgs/", form);
				Message msg = new Message(); //从此行开始进行线程通讯
	            msg.what=2;
	            Bundle bundle = new Bundle();
	            bundle.putString("resstr",resstr);  //往Bundle中存放数据   
	            msg.setData(bundle);
	            mHandler.sendMessage(msg);		//发送线程之间需要传送的信息
			}
		}).start();
    }
    
    
    public void CreateLoading()
    {
    	//创建我们的进度条
    	proDia=new ProgressDialog(MainActivity.this);
    	proDia.setTitle("Loading");
    	proDia.setMessage("请耐心等待...");
    	proDia.setCanceledOnTouchOutside(false);
    	proDia.onStart();
    	proDia.show();
    }
    
}
