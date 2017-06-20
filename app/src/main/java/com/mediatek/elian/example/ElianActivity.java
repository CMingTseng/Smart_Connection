package com.mediatek.elian.example;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.elian.ElianNative;


public class ElianActivity extends Activity {
    private Button mButtonStartV1;
    private Button mButtonStartV4;
    private Button mButtonStartV1V4;
    private Button mButtonStop;
    private ElianNative elian;

	private byte AuthModeOpen = 0x00;
	private byte AuthModeShared = 0x01;
	private byte AuthModeAutoSwitch = 0x02;
	private byte AuthModeWPA = 0x03;
	private byte AuthModeWPAPSK = 0x04;
	private byte AuthModeWPANone = 0x05;
	private byte AuthModeWPA2 = 0x06;
	private byte AuthModeWPA2PSK = 0x07;   
	private byte AuthModeWPA1WPA2 = 0x08;
	private byte AuthModeWPA1PSKWPA2PSK = 0x09;
    
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }    
	private void showDialog(Context context, String title, String msg) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("exit",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						finish();
					}
				});

		builder.show();
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
	    WifiManager mWifiManager;
	    String mConnectedSsid;
	    byte mAuthMode = 0;
	    
		super.onCreate(savedInstanceState);		
        setContentView(R.layout.activity_elian);

		boolean result = ElianNative.loadLibrary();
		if (!result)
		{
			showDialog(this, "Error", "can't load elianjni lib");
		}
        
        elian = new ElianNative();
        
        TextView mSSID = (TextView)findViewById(R.id.SSID);

		mWifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE); 
		if(mWifiManager.isWifiEnabled())
		{
        	WifiInfo WifiInfo = mWifiManager.getConnectionInfo();
        	mConnectedSsid = WifiInfo.getSSID();
			int iLen = mConnectedSsid.length();

			if (iLen == 0)
			{
				return;
			}
			
			if (mConnectedSsid.startsWith("\"") && mConnectedSsid.endsWith("\""))
			{
				mConnectedSsid = mConnectedSsid.substring(1, iLen - 1);
			}
	//		mConnectedSsid = mConnectedSsid.replace('\"', ' ');
	//		mConnectedSsid = mConnectedSsid.trim();
			mSSID.setText(mConnectedSsid);			
		}        

        mButtonStartV1 = (Button)findViewById(R.id.startV1);
        mButtonStartV4 = (Button)findViewById(R.id.startV4);
        mButtonStartV1V4 = (Button)findViewById(R.id.startV1V4);
        mButtonStop = (Button)findViewById(R.id.stop);
		mButtonStop.setEnabled(false);

        mButtonStartV1.setOnClickListener(startListener);
        mButtonStartV4.setOnClickListener(startListener);
        mButtonStartV1V4.setOnClickListener(startListener);
        mButtonStop.setOnClickListener(stopListener);
        
        int libVersion = elian.GetLibVersion();
        int protoVersion = elian.GetProtoVersion();

        ((TextView)findViewById(R.id.Notice)).setText("Version:" + getVersion() + 
        		", libVersion:" + libVersion + ", protocolVersion:" + protoVersion);
	}

	Button.OnClickListener startListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
			String ssid = ((TextView) findViewById(R.id.SSID)).getText()
					.toString();
			String password = ((TextView) findViewById(R.id.Password))
					.getText().toString();			
			String custom = ((TextView) findViewById(R.id.Custom))
					.getText().toString();
			byte authmode = 0;
			
			if (arg0.getId() == R.id.startV1)
			{
				elian.InitSmartConnection(null, 1, 0);
				elian.StartSmartConnection(ssid, password, custom);				
			}
			else if (arg0.getId() == R.id.startV4)
			{
				elian.InitSmartConnection(null, 0, 1);
				elian.StartSmartConnection(ssid, password, custom);
			}
			else if (arg0.getId() == R.id.startV1V4)
			{
				elian.InitSmartConnection(null, 1, 1);
				elian.StartSmartConnection(ssid, password, custom);				
			}
			mButtonStartV1.setEnabled(false);
			mButtonStartV4.setEnabled(false);
			mButtonStartV1V4.setEnabled(false);
			mButtonStop.setEnabled(true);
		}
	};

	Button.OnClickListener stopListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
			elian.StopSmartConnection();
			mButtonStartV1.setEnabled(true);
			mButtonStartV4.setEnabled(true);
			mButtonStartV1V4.setEnabled(true);
			mButtonStop.setEnabled(false);
		}
	};

	@Override
	protected void onPause() {
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		elian.StopSmartConnection();
		mButtonStartV1.setEnabled(true);
		mButtonStartV4.setEnabled(true);
		mButtonStartV1V4.setEnabled(true);
		mButtonStop.setEnabled(false);
		
		super.onDestroy();
	}
}
