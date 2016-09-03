package org.sinsing.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends Activity {
    Button b1, b2, b3, b4;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    //ListView lv;
    private Direction direction;
    private byte buffer;
    View imageView;

    private enum Direction {
        LEFT,
        RIGHT;
    }


    private class ManageConnection extends Thread {
        private InputStream in;
        private OutputStream out;

        public ManageConnection(BluetoothSocket socket) {
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                try {
                    byte temp = buffer;
                    // busy wait
                    while (temp == buffer);
                    if(direction == Direction.LEFT)
                        out.write(0);
                    else
                        out.write(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                mmDevice.getName();
                ParcelUuid parcelUuid = mmDevice.getUuids()[0];
                tmp = device.createRfcommSocketToServiceRecord(parcelUuid.getUuid());
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            (new ManageConnection(mmSocket)).start();
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void right(View view) {
        direction = Direction.RIGHT;
        buffer++;
        //float temp = imageView.getRotation() - (float) 1.40625;
        float temp = imageView.getRotation()- (float) 7.5;
        imageView.setRotation(temp);
        Toast.makeText(getApplicationContext(), "rotation is : " + temp, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.button);
        //b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);
        //b4 = (Button) findViewById(R.id.button4);
        direction = Direction.RIGHT;
        buffer = 0;

        imageView = findViewById(R.id.imageView3);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //lv = (ListView) findViewById(R.id.listView);
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void left(View v) {
        direction = Direction.LEFT;
        buffer --;
        //float temp = imageView.getRotation()+ (float) 1.40625;
        float temp = imageView.getRotation()+ (float) 7.5;
        imageView.setRotation(temp);
        Toast.makeText(getApplicationContext(), "rotation is : " + temp, Toast.LENGTH_SHORT).show();
    }

    public void off(View v) {
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }

    public void visible(View v) {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void list(View v) {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());
            if (bt.getName().equals("HC-06")) {
                Toast.makeText(getApplicationContext(), "Connect to : " + bt.getName(), Toast.LENGTH_SHORT).show();
                (new ConnectThread(bt)).start();
            }
        }

        //final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        //lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}

