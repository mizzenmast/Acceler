package com.test.acceler;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.SharedPreferences;
import android.content.Context;
import java.net.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View.*;
import java.io.*;
import  java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;

	private Sensor mAccelerometer;
	OutputStreamWriter out;
	static final double STRAF_LEAN_SENSITIVITY = 6.0;
	static final double FORWARD_BACK_LEAN_SENSITIVITY = 3.0;
	static final double BACK_LEAN_SENSITIVITY = 6.0;
	static final double JOG_SENSITIVITY = 10;
	static final double JUMP_SENSITIVITY = 30.0;
	static final double BASE_JUMP_SENSITIVITY = 20.0;
	static long[] sendTimes = new long[256];
	static long[] sendCounts = new long[256];
	static long[] requiredSendCounts = new long[256];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		 TabHost tabs = (TabHost)findViewById(R.id.tabhost);

	        tabs.setup();

	        TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");

	        spec1.setContent(R.id.tab1);
	        spec1.setIndicator("Main");

	        tabs.addTab(spec1);

	        TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
	        spec2.setContent(R.id.tab2);
	        spec2.setIndicator("More");

	        tabs.addTab(spec2);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		Log.d("OnCreate", "Started up");
		EditText editText1 = (EditText)findViewById(R.id.editText1);  // tv is id in XML file for TextView
		editText1.setText(getString("ipAddress", "192.168.1.114"));
		File file = new File(this.getExternalFilesDir(null), "logs.txt");
		requiredSendCounts['w']=5;
		try {
			file.delete();
			file.createNewFile();

			FileOutputStream os = new FileOutputStream(file, true); 
			out = new OutputStreamWriter(os);
		} catch(Exception E) {
			E.printStackTrace();
		}

		SeekBar walkCountBar = (SeekBar)findViewById(R.id.walkCountBar);
		walkCountBar.setProgress(getInt("walkCountBar", 5));
		walkCountBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateSeekBar("Walk Count: ", "", "walkCountBar", R.id.walkCountBar, R.id.walkCountLabel);
				requiredSendCounts['w'] = progress;
			}
		});

		
		SeekBar jogBar = (SeekBar)findViewById(R.id.jogBar);
		jogBar.setProgress(getInt("jogBar", 50));
		jogBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateSeekBar("Jog Sensitivity: ", "%", "jogBar", R.id.jogBar, R.id.jogLabel);
			}
		});
		SeekBar jumpSensitivityBar = (SeekBar)findViewById(R.id.jumpSensitivityBar);
		jumpSensitivityBar.setProgress(getInt("jumpSensitivityBar", 50));
		jumpSensitivityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateSeekBar("Jump Sensitivity: ", "%", "jumpSensitivityBar", R.id.jumpSensitivityBar, R.id.jumpSensitivityLabel);
			}
		});

		
		SeekBar leanBar = (SeekBar)findViewById(R.id.leanBar);
		leanBar.setProgress(getInt("leanBar", 50));

		leanBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updateSeekBar("Lean Sensitivity: ", "%", "leanBar", R.id.leanBar, R.id.leanLabel);
			}
		});

		updateSeekBar("Lean Sensitivity: ", "%", "leanBar", R.id.leanBar, R.id.leanLabel);
		updateSeekBar("Jog Sensitivity: ", "%", "jogBar", R.id.jogBar, R.id.jogLabel);
		updateSeekBar("Jump Sensitivity: ", "%", "jumpSensitivityBar", R.id.jumpSensitivityBar, R.id.jumpSensitivityLabel);
		updateSeekBar("Walk Count: ", "", "walkCountBar", R.id.walkCountBar, R.id.walkCountLabel);
		
		Button btn1 = (Button) findViewById(R.id.button1);
		btn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(200);
							baselining = true;
							meanCount=0;
						
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						// Vibrate for 500 milliseconds
						vib.vibrate(500);
						baselining = false;
					}
				}).start();
			}
		});

		configureCheckBox((CheckBox)findViewById(R.id.backwardBox), "backwardBox");
		configureCheckBox((CheckBox)findViewById(R.id.forwardBox), "forwardBox");
		configureCheckBox((CheckBox)findViewById(R.id.leftRightBox), "leftRightBox");
		
	}
	
	private void configureCheckBox(CheckBox cb, String label) {
		final String labela = label;
		int v = getInt(label, 1);
		if (v == 1) {
			cb.setChecked(true);
		} else {
			cb.setChecked(false);
		}
		cb.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox)v;
				putInt(labela, cb.isChecked() ? 1 : 0);
			}
		});
	}
	
	private void updateSeekBar(String label, String postfix, String key, int sbID, int tvID) {
		TextView tv = (TextView)findViewById(tvID);
		SeekBar sb = (SeekBar)findViewById(sbID);
		tv.setText(label+sb.getProgress()+postfix);
		putInt(key, sb.getProgress());
	}

	private float getLeanSensitivity() {
		SeekBar leanBar = (SeekBar)findViewById(R.id.leanBar);
		float retVal = (float)leanBar.getProgress();
		retVal = retVal / 100;
		return retVal;
	}


	private float getJumpSensitivity() {
		SeekBar jumpSensitivityBar = (SeekBar)findViewById(R.id.jumpSensitivityBar);
		float retVal = (float)jumpSensitivityBar.getProgress();
		retVal = retVal / 100;
		return retVal;
	}
	
	private float getJogSensitivity() {
		SeekBar jumpSensitivityBar = (SeekBar)findViewById(R.id.jogBar);
		float retVal = (float)jumpSensitivityBar.getProgress();
		retVal = retVal / 100;
		return retVal;
	}

	private boolean getJogForward() {
		CheckBox forwardBox = (CheckBox)findViewById(R.id.forwardBox);
		return forwardBox.isChecked();
	}
	private boolean getJogBackward() {
		CheckBox backwardBox = (CheckBox)findViewById(R.id.backwardBox);
		return backwardBox.isChecked();
	}
	private boolean getJogLeftRight() {
		CheckBox leftRightBox = (CheckBox)findViewById(R.id.leftRightBox);
		return leftRightBox.isChecked();
	}

	private void putString(String key, String value) {
		SharedPreferences preferences = this.getSharedPreferences("MyPreferences", MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void putInt(String key, int value) {
		SharedPreferences preferences = this.getSharedPreferences("MyPreferences", MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	private String getString(String key, String defValue) {
		SharedPreferences preferences = this.getSharedPreferences("MyPreferences", MODE_PRIVATE);  
		return preferences.getString(key, defValue);
	}

	private int getInt(String key, int defValue) {
		SharedPreferences preferences = this.getSharedPreferences("MyPreferences", MODE_PRIVATE);  
		return preferences.getInt(key, defValue);
	}
	
	private void log(String log) {
		try {
		sendDataString(System.currentTimeMillis()+" "+log+"\n");
		}catch (Exception E) {
			E.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this
	}

	public float getMinMaxDiff(float value, List<Float> list, int maxQueueSize) {
		if (list == null) {
			return 0;
		}
		Float f = Float.valueOf(value);
		list.add(f);
		if (list.size() > maxQueueSize) {
			list.remove(0);
		} else {
			return 0;
		}
		float initial = list.get(0);
		float end = list.get(list.size()-1);
		float middle = list.get(list.size()/2);
		float maxDif = (middle - end) + (middle - initial);
		float minDif = (end - middle) + (end - initial);

		if (maxDif > minDif) {
			return maxDif;
		} else {
			return minDif;
		}
	}

	public void sendDataString(String chp) {
		final String str = chp;
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {

					try {
						EditText editText1 = (EditText)findViewById(R.id.editText1);  // tv is id in XML file for TextView
						putString("ipAddress", editText1.getText().toString());   
						InetAddress serverAddr = InetAddress.getByName(editText1.getText().toString());

						DatagramSocket socket = new DatagramSocket();
						byte[] buf = str.getBytes("UTF-8");

						DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, 9875);
						socket.send(packet);    	} 
					catch(Exception E) {
						System.out.println(E);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start(); 
	}


	public void sendData(char chp, int delayp) {
		if (sendCounts[chp]++ < requiredSendCounts[chp]) {
			return;
		}
		sendCounts[chp] = 0;
		
		long curTime = System.currentTimeMillis();
		if (curTime - sendTimes[chp] < 50) {
			return;
		}
		sendTimes[chp] = curTime;
		final char ch = chp;
		final int delay = delayp;
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					if (delay > 0) {
						Thread.sleep(delay);
					}
					try {
						EditText editText1 = (EditText)findViewById(R.id.editText1);  // tv is id in XML file for TextView
						putString("ipAddress", editText1.getText().toString());   
						InetAddress serverAddr = InetAddress.getByName(editText1.getText().toString());

						DatagramSocket socket = new DatagramSocket();
						byte[] buf = ("1").getBytes();
						buf[0]=(byte)ch;

						DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, 9876);
						socket.send(packet);    	} 
					catch(Exception E) {
						System.out.println(E);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start(); 
	}
	
	public void sendData(char chp) {
		sendData(chp, 0);
	}
	private float meanZ = 0;
	private float meanX  = 0;
	private float meanY  = 0;
	private int meanCount = 0;


	private float tempMeanZ = 0;
	private float tempMeanX  = 0;
	private float tempMeanY  = 0;
	private int tempMeanCount = 0;
	
	boolean baselining = false;
	List<Float> yQueue = new LinkedList<Float>();
	@Override

	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		handleSensorValues(x, y, z);
	}
	
	
	/** cross platform **/
	int leaningForwardBack(float z, float zmean, boolean flip) {
		log("lfb z "+z+" zmean "+zmean+" flip "+flip+" leanSensitivity "+FORWARD_BACK_LEAN_SENSITIVITY*getLeanSensitivity());
		if ((zmean - z) > FORWARD_BACK_LEAN_SENSITIVITY*getLeanSensitivity()) {
			return 1;
		} else if ((zmean - z) > FORWARD_BACK_LEAN_SENSITIVITY*getLeanSensitivity()*1.5) {
			return 2;
		}else if ((zmean - z) < -1*BACK_LEAN_SENSITIVITY*getLeanSensitivity()) {
			return -1;
		} 
		return 0;
	}
	
	int leaningLeftRight(float x, float xmean, boolean flip) {
		log("lfr x "+x+" xmean "+xmean+" flip "+flip+" leanSensitivity "+STRAF_LEAN_SENSITIVITY*getLeanSensitivity());
		if ((xmean - x) > STRAF_LEAN_SENSITIVITY*getLeanSensitivity()) {
			return flip ? 1 : -1;
		} else if ((xmean - x) < -1*STRAF_LEAN_SENSITIVITY*getLeanSensitivity()) {
			return flip ? -1 : 1;
		} 
		return 0;
	}
	
   void handleSensorValues(float x, float y, float z) {
		if (baselining) {
			float total = meanZ * meanCount;
			total = total + z;
			meanZ = total / (meanCount + 1);
			total = meanX * meanCount;
			total = total + x;
			meanX = total / (meanCount + 1);
			total = meanY * meanCount;
			total = total + y;
			meanY = total / (meanCount + 1);
			meanCount++;
			return;
		}
		if (meanCount == 0) {
			return;
		}

		boolean stepped = false;
		boolean jogForward = getJogForward();
		boolean jogBackward = getJogBackward();
		boolean jogLeftRight = getJogLeftRight();

		float minMaxDif = getMinMaxDiff(y, yQueue, 20);
		
		stepped =  Math.abs(y) > 2*JOG_SENSITIVITY*getJogSensitivity();
		if (stepped) {
			sendData('w');
		}
		
		stepped =  Math.abs(y) > 3*JOG_SENSITIVITY*getJogSensitivity();
		if (stepped) {
			sendData('w', 50);
			sendData('?', 50);
		}
		stepped =  Math.abs(y) > 4*JOG_SENSITIVITY*getJogSensitivity();
		if (stepped) {
			sendData('w', 100);
			sendData('!', 100);
		}
		
		if (true) {
			return;
		}
		//Don't do anything else if we're jumping
		//if (System.currentTimeMillis() - sendTimes['j'] < 100) {
		//	return;
		//}
		
		if (Math.abs(minMaxDif) > BASE_JUMP_SENSITIVITY+JUMP_SENSITIVITY*getJumpSensitivity()) {
			sendData('j');
			sendData('w');
		}
		if (jogForward || jogBackward || jogLeftRight) {
			//only check if one of these values is set
			stepped =  Math.abs(y) > JOG_SENSITIVITY*getJogSensitivity();
			//minMaxDif 
			log("step "+stepped +" y "+y+" mMD "+minMaxDif +" jS "+6*getJogSensitivity());
		} 

		if (tempMeanCount < 8) {
			float total = tempMeanZ * tempMeanCount;
			total = total + z;
			tempMeanZ = total / (tempMeanCount + 1);
			total = tempMeanX * tempMeanCount;
			total = total + x;
			tempMeanX = total / (tempMeanCount + 1);
			total = tempMeanY * tempMeanCount;
			total = total + y;
			tempMeanY = total / (tempMeanCount + 1);
			
			
			/** Keeping this code here, trying to reduce noise in X/Z if Y is spiking
			float ySpread = Math.abs(tempMeanY - meanY);
			ySpread = (Math.abs(meanY) + (ySpread/4)) / meanY;
			ySpread = Math.abs(ySpread);
			tempMeanZ = tempMeanZ / ySpread;
			log("ZTEMP "+z+" ySpread "+ySpread+" tempMeanZ "+tempMeanZ);
			log("XTEMP "+x+" ySpread "+ySpread+" tempMeanX "+tempMeanZ);
			tempMeanX = tempMeanX / ySpread;
			**/
			tempMeanCount++;
			if (!stepped) 
				return;
		}
		tempMeanCount = 0;
		int lfb = leaningForwardBack(tempMeanZ, meanZ, meanY < 0);
		int llr = leaningLeftRight(tempMeanX, meanX, meanY < 0);

		if (lfb > 1) {
			sendData('c');
		} 
		if (lfb > 0  && llr == 0) {
			if ((jogForward && stepped) || (!jogForward)) { 
				if (minMaxDif > JOG_SENSITIVITY*getJogSensitivity()*1.5) {
					sendTimes['w']=0;
					sendData('w');
					sendData('!');
					sendTimes['w']=0;
					if (minMaxDif > JOG_SENSITIVITY*getJogSensitivity()*2) {
						sendData('w');
						sendData('+');
						sendTimes['w']=0;
					}
				}
				sendData('w');
			}
		} else if (lfb < 0 && llr == 0) {
			if ((jogBackward && stepped) || (!jogBackward)) {
				// Don't go back if we just walked
				if (System.currentTimeMillis() - sendTimes['w'] < 300) {
					return;
				}
				//sendData('s');
			}
		}

		if (!stepped) {	
			if (llr < 0 ) {
				if ((jogLeftRight && stepped) || (!jogLeftRight)) { 
					sendData('a');
				}
			} else if (llr > 0) {
				if ((jogLeftRight && stepped) || (!jogLeftRight)) { 
					sendData('d');
				}
			}
		}

		if (stepped && lfb == 0 && llr == 0) {
			sendData('w');
		}
	}

}
