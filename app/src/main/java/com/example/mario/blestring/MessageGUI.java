package com.example.mario.blestring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

//Main Class
public class MessageGUI extends AppCompatActivity {
    private BluetoothAdapter BA;
    private BroadcastReceiver mReceiver;
    private View v;
    ListView lv;
    private Set<BluetoothDevice> pairedDevices;

    //Main Method (as soon as this screen is reached), Initializes Bluetooth adapter and calls other methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_gui);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializes Bluetooth adapter
        final BluetoothManager BM =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null){
            Toast.makeText(this, "No Bluetooth on this handset", Toast.LENGTH_SHORT).show();
        }
        lv = (ListView)findViewById(R.id.Paired);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Ensures Bluetooth is available on the device and it is enabled. If not,
                // displays a dialog requesting user permission to enable Bluetooth.
                if (!BA.isEnabled()) {
                    Toast.makeText(getApplicationContext(), "Turn On Bluetooth in Settings",Toast.LENGTH_SHORT).show();
                }
                else {
                    //TODO: SEND STRING
                }
            }
        });

        //For Broadcasts when a device is found
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    Toast.makeText(getApplicationContext(), "Discovery Started",Toast.LENGTH_SHORT).show();
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Toast.makeText(getApplicationContext(), "Discovery Finished",Toast.LENGTH_SHORT).show();
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    //Turns On Bluetooth
    public void on(View view) {
        if (BA == null || !BA.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
        }
    }

    //Turns Off Bluetooth
    public void off(View view){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_SHORT).show();
    }

    //Turns on discovery for random range of time, then switches to listen mode.
    public void p2pdiscover(View view){



        int btstate = BA.getState();
        if (btstate == BluetoothAdapter.STATE_DISCONNECTED) {
            if (BA.isDiscovering()) {
                BA.cancelDiscovery();
            }
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //Makes the device visible for others to connect
            int randTime = ThreadLocalRandom.current().nextInt(15, 31);
            getVisible.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, randTime);      //15-30 second visibility (Picked randomly)
            //TODO: Notify "Listening and not listening" states;
            startActivityForResult(getVisible, 0);
            //TODO When not listening, broadcast (END DISCOVERY AFTER SET TIME)
            BA.startDiscovery();

        }

    }

    //List Paired Devices
    public void paired(View view) {
        pairedDevices = BA.getBondedDevices();
        ArrayList devicelist = new ArrayList();
        for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
            devicelist.add(device.getName());
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicelist);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_gui, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ToggleBT) {
            if (!BA.isEnabled()) {
                on(v);
            }
            else {
                off(v);
            }
        }
        if (id == R.id.Connect) {
            p2pdiscover(v);
        }
        if (id == R.id.Paired) {
            Intent activity_pair = new Intent(this, PairedActivity.class);
            startActivity(activity_pair);
            //paired(lv); TODO: Implement This (possibly in the PairedActivity.class)
        }
        if (id == R.id.Connect) {
            p2pdiscover(v);
        }
        if (id == R.id.Exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
