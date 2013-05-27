/**
 *  @version 1.0 (20.05.2013)
 *  
 *  @author Robotonics
 * 
 */

package com.robotonics.btjuke;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
 
import com.robotonics.btjuke.R;
import android.app.AlertDialog;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.*;
import android.app.*;
 
public class MainActivity extends Activity {
   
  Button Send;
  TextView txtArduino;
  Handler h;
  EditText trackNumber;
  String value;
  public static int trackno;
  final Context context = this; 
  private Button button; 
   
  final int RECIEVE_MESSAGE = 1;// Status  for Handler
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private StringBuilder sb = new StringBuilder();
  
  private ConnectedThread mConnectedThread;
  
  
  // SPP UUID service
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
  // MAC-address of Bluetooth module (you must edit this line)
  private static String address = "00:12:11:28:07:57";
   
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
 
    setContentView(R.layout.activity_main);
    trackNumber= (EditText) findViewById(R.id.userInput);
    button= (Button) findViewById(R.id.Play);				
   			
    txtArduino = (TextView) findViewById(R.id.txtArduino);
	// for display the received data from the Arduino


	
    
    h = new Handler() {
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
            case RECIEVE_MESSAGE:													// if receive massage
            	byte[] readBuf = (byte[]) msg.obj;
            	String strIncom = new String(readBuf, 0, msg.arg1);					// create string from bytes array
            	sb.append(strIncom);												// append string
            	int endOfLineIndex = sb.indexOf("\r\n");							// determine the end-of-line
            	if (endOfLineIndex > 0) { 											// if end-of-line,
            		String sbprint = sb.substring(0, endOfLineIndex);				// extract string
                    sb.delete(0, sb.length());										// and clear
                	txtArduino.setText("Data from Arduino: " + sbprint); 	        // update TextView
                
                	Send.setEnabled(true); 
                }
            
            	break;
    		}
        };
	};
     
    btAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
    checkBTState();
	
    
   
	  button.setOnClickListener(new OnClickListener() {
		  
	  @Override public void onClick(View arg0) {   // get prompts.xml view
	  
	  LayoutInflater li = LayoutInflater.from(context); 
	  
	  View promptsView = li.inflate(R.layout.prompts, null);
	  
	  AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( context);   // set prompts.xml to alertdialog builder 
	  
	  alertDialogBuilder.setView(promptsView); 
	  
	  trackNumber = (EditText) promptsView .findViewById(R.id.userInput);   // set dialog message
	  
	  alertDialogBuilder .setCancelable(false) .setPositiveButton("Play", new DialogInterface.OnClickListener() { 
	  
	  public void onClick(DialogInterface dialog,int id) { // get user input and write to attached thread 
	  
	  value=trackNumber.getText().toString();
	  
	  mConnectedThread.write(value);
	
	  } }) 
	  
	  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
	  
	  public void onClick(DialogInterface dialog,int id) { 
	  
	  dialog.cancel(); 
	  
	  } });   
	  
	  // create alert dialog 
	  
	  AlertDialog alertDialog = alertDialogBuilder.create();   
	  
	  alertDialog.show(); // show dialog
	  
	  alertDialog.getWindow().setLayout(350,250); // change width and height of dialog
	  
	  } }); 
	  
	  } 
 
  
  
  private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
      if(Build.VERSION.SDK_INT >= 10){
          try {
              final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
              return (BluetoothSocket) m.invoke(device, MY_UUID);
          } catch (Exception e) {
              // do something here?
          }
      }
      return  device.createRfcommSocketToServiceRecord(MY_UUID);
  }
  
  
   
  @Override
  public void onResume() {
    super.onResume();
   
    // Set up a pointer to the remote node using it's address.
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
   
    // Two things are needed to make a connection:
    //   A MAC address, which we got above.
    //   A Service ID or UUID.  In this case we are using the
    //     UUID for SPP.
    
	try {
		btSocket = createBluetoothSocket(device);
	} catch (IOException e) {
		errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
	}
    
    /*try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }*/
   
    // Discovery is resource intensive.  Make sure it isn't going on
    // when you attempt to connect and pass your message.
    btAdapter.cancelDiscovery();
   
    // Establish the connection.  This will block until it connects.
   
    try {
      btSocket.connect();
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
     
    // Create a data stream so we can talk to server
   
    mConnectedThread = new ConnectedThread(btSocket);
    mConnectedThread.start();
  }
 	public void sendtrack(){


	}
  @Override
  public void onPause() {
    super.onPause();
 
  
    try     {
      btSocket.close();
    } catch (IOException e2) {
      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
    }
  }
   
  private void checkBTState() {
    // Check for Bluetooth support and then check to make sure it is turned on
    // Emulator doesn't support Bluetooth and will return null
    if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth not support");
    } else {
      if (btAdapter.isEnabled()) {
    
      } else {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
 
  private void errorExit(String title, String message){
    Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    finish();
  }
 
  private class ConnectedThread extends Thread {
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[256];  // buffer store for the stream
	        int bytes; // bytes returned from read()

	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	        	try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);		// Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(String message) {
	   
	    	byte[] msgBuffer = message.getBytes();
	    	try {
	            mmOutStream.write(msgBuffer);
	        } catch (IOException e) {
	          // do something here?   
	          }
	    }
	}
	
	// add new subroutines here if needed

}
	
