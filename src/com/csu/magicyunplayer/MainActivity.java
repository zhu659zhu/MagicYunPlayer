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
	//����������
	ProgressDialog proDia;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this; //��ȡӦ�ó���context
		
		new Thread(checkupdate).start();  //�����������߳�
		
		//��XML�е�ListView����ΪItem������  
	    list = (ListView) findViewById(R.id.listView1);  
	    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view,
	    	int position, long id) {
	    		//Toast.makeText(MainActivity.this, mylist.get(position).get("ItemText") , Toast.LENGTH_SHORT).show();
	    		Random ran =new Random(System.currentTimeMillis()); 
	    		Uri uri = Uri.parse(mylist.get(position).get("ItemText")+"?rnd="+ran.nextInt(1000));  
	    		//����ϵͳ�Դ��Ĳ����� 
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
                		Toast.makeText(MainActivity.this, "�����ղ�...", Toast.LENGTH_SHORT).show();
                        return true; 
                } 
        }); 

} 
	
	public String getSDPath(){ 
	       File sdDir = null; 
	       boolean sdCardExist = Environment.getExternalStorageState()   
	                           .equals(Environment.MEDIA_MOUNTED);   //�ж�sd���Ƿ���� 
	       if   (sdCardExist)   
	       {                               
	         sdDir = Environment.getExternalStorageDirectory();//��ȡ��Ŀ¼ 
	      }   
	       return sdDir.toString(); 
	       
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {      //���ܴ�����̴߳��͵���Ϣ
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            if(msg.what==1)  //msg.what=1ʱΪ������ ����ΪUpON
            {
	            String UpON = msg.getData().getString("UpON");
	            if(UpON=="1") // ��Ҫ����
	            {
		            Toast.makeText(MainActivity.this, "�������Ҫ����", Toast.LENGTH_SHORT).show();
		            mCNHUpdate = new CNHUpdate(instance);
		    		mCNHUpdate.checkUpdateInfo();
	            }
            }
            if(msg.what==2)  //msg.what=2ʱΪ�����б�
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
			        
			      //��XML�е�ListView����ΪItem������  
	        	    ListView list = (ListView) findViewById(R.id.listView1);  
	            	
	            	//���ɶ�̬���飬����ת������  
	        	    
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
	        	    
	        	    //����������������===��ListItem  
	        	    mSchedule = new SimpleAdapter( MainActivity.this, //ûʲô����  
	        	                                                mylist,//������Դ   
	        	                                                R.layout.my_listitem,//ListItem��XMLʵ��  
	        	                                                  
	        	                                                //��̬������ListItem��Ӧ������          
	        	                                                new String[] {"ItemTitle", "ItemText"},   
	        	                                                  
	        	                                                //ListItem��XML�ļ����������TextView ID  
	        	                                                new int[] {R.id.ItemTitle,R.id.ItemText});  
	        	    //��Ӳ�����ʾ  
	        	    list.setAdapter(mSchedule); 
	        	    if (mSchedule.isEmpty())
	        	    {
	        	    	new  AlertDialog.Builder(instance)    
	        	    	.setTitle("��ʾ")  
	                    .setMessage("����ֱ��Դ~" )  
	                    .setPositiveButton("ȷ��" ,  null )  		             
	                    .show(); 
	        	    }
	        	    proDia.dismiss();//���ضԻ���
            	}
        	    catch(Exception e){
		            new  AlertDialog.Builder(instance)    
                    .setTitle("��ʾ")  
                    .setMessage("ˢ��ʧ�ܣ�" )  
                    .setPositiveButton("ȷ��" ,  null )  		             
                    .show();  
        	    }
            }
        }
       };
	
	private Runnable checkupdate = new Runnable()    //����һ���µ��̣߳�����������
	  {  
	    public void run()  
	    {
	      {  
	    	  try
		        {
		          	int localVersion =getVersionName(); 
		            String pathurl = "http://zhuhaidong.vv.si/app/NBAPlayer.php";   //�����µ�ַ
		            URL url = new URL(pathurl); 
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); 
		            conn.setReadTimeout(1500); 
		            conn.setRequestMethod("GET"); 
		            InputStream inStream = conn.getInputStream(); 
		            int netVersion=Integer.parseInt(inputStream2String(inStream));
		            Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
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
		            bundle.putString("UpON",UpON);  //��Bundle�д������   
		            msg.setData(bundle);
		            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
		        }
		        catch (Exception e)

		        {
		        	e.printStackTrace();
		        }
	      }  
	    }  
	  };

    private int getVersionName() throws Exception   //��ȡ��ǰ�汾�ź���
    {  
            PackageManager packageManager = getPackageManager();  
            // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ  
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);  
            int version = packInfo.versionCode;  
            return version;  
    } 
    
    public static String inputStream2String(InputStream   is) throws IOException{   //����ת������
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
				Message msg = new Message(); //�Ӵ��п�ʼ�����߳�ͨѶ
	            msg.what=2;
	            Bundle bundle = new Bundle();
	            bundle.putString("resstr",resstr);  //��Bundle�д������   
	            msg.setData(bundle);
	            mHandler.sendMessage(msg);		//�����߳�֮����Ҫ���͵���Ϣ
			}
		}).start();
    }
    
    
    public void CreateLoading()
    {
    	//�������ǵĽ�����
    	proDia=new ProgressDialog(MainActivity.this);
    	proDia.setTitle("Loading");
    	proDia.setMessage("�����ĵȴ�...");
    	proDia.setCanceledOnTouchOutside(false);
    	proDia.onStart();
    	proDia.show();
    }
    
}
