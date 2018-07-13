package com.example.joelwasserman.androidbletutorial;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.Date;

//toDo  : See BLE Scanner , BLE Peripheral Simulator , BLE Too from Google Play Store  to simulate Ble communication
public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final static String WIKO_NAME = "View";
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    ScanFilter scanFilter;
    ScanSettings scanSettings;
    BluetoothDevice bluetoothDevice = null;
    BluetoothGatt mBluetoothGatt ;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.i("OnScanResultTag", " onConnectionStateChange "+newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("OnScanResultTag", " onConnectionStateChange ==> STATE_CONNECTED ");

                gatt.discoverServices() ;
                //Discover services when Gatt is connected
//                bleHandler.obtainMessage(MSG_DISCOVER_SERVICES, gatt).sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("OnScanResultTag", " onConnectionStateChange ==> STATE_DISCONNECTED ");

//                bleHandler.obtainMessage(MSG_GATT_DISCONNECTED, gatt).sendToTarget();
            } else {
                Log.e("OnScanResultTag", " onConnectionStateChange ==> ERROR ");

                //If received any error
//                reconnectOnError("onConnectionStateChange", status);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("OnScanResultTag", " onServicesDiscovered ==> GATT_SUCCESS ");

                for (BluetoothGattService bluetoothGattService : gatt.getServices()){
                    Log.i("OnScanResultTag", " bluetoothGattService  =>  "+bluetoothGattService.getUuid());
                }
            } else {
                Log.w("OnScanResultTag", "onServicesDiscovered received: " + status);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i("OnScanResultTag", " onCharacteristicRead ");

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("OnScanResultTag", " onCharacteristicWrite ");

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("OnScanResultTag", " onCharacteristicChanged ");

        }
    } ;
    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e("OnScanResultTag", " callbackType " + callbackType + ", result =  " + result);

            if (result.getDevice() != bluetoothDevice && result.getDevice().getName() != null) {
                bluetoothDevice = result.getDevice() ;
                peripheralTextView.append(new Date() + " :\n Found Device Address: " + bluetoothDevice.getAddress() + " uuid: " + bluetoothDevice.getName() + "\n");

                stopScanning();

                connectToPeripheral(bluetoothDevice);
//                // auto scroll for text view
//                final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
//                // if there is no need to scroll, scrollAmount will be <=0
//                if (scrollAmount > 0)
//                    peripheralTextView.scrollTo(0, scrollAmount);
            }
        }


        @Override
        public void onScanFailed(int errorCode) {

            Log.e("OnScanResultTag", " onScanFailed " + errorCode);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        initBle();
    }

    public void initBle() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        //If name or address of peripheral is known
        scanFilter = new ScanFilter.Builder()
                .setDeviceName(WIKO_NAME)
                .build();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                btScanner.startScan(Collections.singletonList(scanFilter), scanSettings, leScanCallback);
//                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }


    private void connectToPeripheral(BluetoothDevice device) {
        //Always use false on auto-connect parameter
        mBluetoothGatt= device.connectGatt(this, false, mGattCallback);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBluetoothGatt!= null)
            mBluetoothGatt.disconnect();
    }


    /***
     * 07-13 17:09:18.275 22255-22268/com.example.joelwasserman.androidbletutorial I/OnScanResultTag:  bluetoothGattService  =>  00001801-0000-1000-8000-00805f9b34fb
     07-13 17:09:18.275 22255-22268/com.example.joelwasserman.androidbletutorial I/OnScanResultTag:  bluetoothGattService  =>  00001800-0000-1000-8000-00805f9b34fb
     07-13 17:09:18.275 22255-22268/com.example.joelwasserman.androidbletutorial I/OnScanResultTag:  bluetoothGattService  =>  0000fff0-0000-1000-8000-00805f9b34fb
     */

}
