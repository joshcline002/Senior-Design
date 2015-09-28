package edu.iastate.ece.sd.httpdec1504.kepros;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import org.w3c.dom.Text;

/**
 * Created by wipark on 8/25/15.
 */
public class viewData_Fragment extends android.support.v4.app.Fragment {

    View rootview;

    Button btDataOn, btDataOff;
    TextView txtArduino, txtString, txtStringLength, bluetoothDataRec;
    Handler bluetoothIn;
    ArrayList<String> mylist = new ArrayList<String>();




    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private BluetoothDevice myBTDevice;
    private UUID MY_UUID;
    private TextView text;
    private ArrayAdapter<String> CheckAdapter;
    ListView list;

    private InputStream mmInStream;
    private OutputStream mmOutStream;

    // Will Attempt

    Handler bluetoothHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
                recDataString.append(msg.obj.toString());
                 mylist.add(String.valueOf(recDataString));
                 recDataString.delete(0, recDataString.length());                    //clear all string data
                }
        };




    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           mylist.add((String)msg.obj);
        }
    };

    private StringBuilder recDataString = new StringBuilder();
    //private ConnectedThread mConnectedThread;

    // String for MAC address
    private static String address;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.viewdata_layout, container, false);
        CheckAdapter = new ArrayAdapter<String>(rootview.getContext(), android.R.layout.simple_list_item_1);
        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        list = (ListView) rootview.findViewById(R.id.datalist);
        list.setAdapter(CheckAdapter);
        init.start();
        bluetoothHandler.postDelayed(stringToList, 10);
    }

    private Thread init = new Thread(){
        @Override
        public void start() {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    if(device.getName().compareTo("Posture") == 0) {
                        myBTDevice=device;
                    }
                }
            }
            connectThread.start();
        }
    };

    private Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                while (true) {
                    sleep(10);
                    manageConnectedSocket();;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Thread connectThread = new Thread() {
        @Override
        public void start() {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = myBTDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            btSocket = tmp;
            run.start();
        }
    };

    private Thread run = new Thread(){
    @Override
    public void start() {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            btSocket.connect();
            Log.d("BLUETOOTHSOCKET", "Bluetooth Socket Connected!");
            thread.start();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                btSocket.close();
                Log.d("BLUETOOTHSOCKET", "Bluetooth Socket Closed!");
            } catch (IOException closeException) {
            }
            return;
        }
    }
    };

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            btSocket.close();
        } catch (IOException e) { }
    }

    public void manageConnectedSocket(){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

       // byte[] buffer = new byte[1024];  // buffer store for the stream
       // int bytes; // bytes returned from read()
        while (true) {
            try {
                byte[] buffer = new byte[1024];  // buffer store for the stream

                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
                mmInStream = tmpIn;
                mmOutStream = tmpOut;

                int bytes = mmInStream.read(buffer);
                //Log.i("NumOfBytes", "read nbytes: " + bytes);

                String readMessage = new String(buffer, 0, bytes);
                //Log.d("readmessage stuff", readMessage);
                bluetoothHandler.obtainMessage(1, bytes, -1, readMessage).sendToTarget();
               //  mylist.add("Woooooop"); // If there is no myList add calll... then it doesnt show data. which makes no sense
                // i lied

                //   bytes = btSocket.getInputStream().read(buffer);
                //Log.d("ReadingBytes", "What should be inputed here im not sure");
               // mHandler.obtainMessage(1, bytes, -1, buffer)
                 //       .sendToTarget();
                //bluetoothHandler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    private Runnable stringToList = new Runnable() {
        public void run() {
            for (int i = 0; i<mylist.size(); i++) {
                CheckAdapter.add(mylist.get(i));
            }
            mylist.clear();
           // mHandler.postDelayed(stringToList, 10);
            bluetoothHandler.postDelayed(stringToList, 10);
        }
    };
}
