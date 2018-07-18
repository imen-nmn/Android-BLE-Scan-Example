package com.example.joelwasserman.androidbletutorial;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static com.example.joelwasserman.androidbletutorial.StringHelpers.hexStringToString;

//toDo  : In this sample I used BLE Tool App to simulate Ble sensor (Gatt server)
//you can see also BLE Scanner , BLE Peripheral Simulator app in the store

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final static String WIKO_NAME = "View";
    private final static String SERVICE_1 = "00001801-0000-1000-8000-00805f9b34fb";
    private final static String SERVICE_2 = "00001800-0000-1000-8000-00805f9b34fb";
    private final static String SERVICE_3 = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final static String CHARATERISTIC_4 = "0000fff4-0000-1000-8000-00805f9b34fb";
    private final static String DESCRIPTOR_NOTIFY = "00002901-0000-1000-8000-00805f9b34fb";

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;
    private Button startScanningButton;
    private Button stopScanningButton;
    private TextView peripheralTextView;
    private ScanFilter scanFilter;
    private ScanSettings scanSettings;

    private EditText editText;

    private BluetoothDevice bluetoothDevice = null;
    private BluetoothGatt mBluetoothGatt = null;

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e("OnScanResultTag", " callbackType " + callbackType + ", result =  " + result);

            if (result.getDevice() != bluetoothDevice && result.getDevice().getName() != null) {
                bluetoothDevice = result.getDevice();
                stopScanning();

                String deviceLog = new Date() + " :\n Found Device Address: " + bluetoothDevice.getAddress()
                        + " uuid: " + bluetoothDevice.getName() + "\n";

                appendTv(deviceLog);


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


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.i("OnScanResultTag", " onConnectionStateChange " + newState);


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("OnScanResultTag", " onConnectionStateChange ==> STATE_CONNECTED ");
                enableEdit(true);

                appendTv("STATE_CONNECTED");

                gatt.discoverServices();
                //Discover services when Gatt is connected
//                bleHandler.obtainMessage(MSG_DISCOVER_SERVICES, gatt).sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                enableEdit(false);

                Log.i("OnScanResultTag", " onConnectionStateChange ==> STATE_DISCONNECTED ");
                appendTv("STATE_DISCONNECTED");
                stopScanning();
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
                appendTv("GATT_SUCCESS");
                enableEdit(true);

                Log.e("OnScanResultTag", " onServicesDiscovered ==> GATT_SUCCESS ");
                displayServicesUuid(gatt, SERVICE_3);
            } else {
                Log.w("OnScanResultTag", "onServicesDiscovered received: " + status);
            }

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            String log = "\n____________________ onCharacteristicRead __________________";
            appendTv("onCharacteristicRead ");
            Log.i("OnScanResultTag", log);
            displayCharacteristic(characteristic);

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            String log = "\"/____________________ onDescriptorRead __________________/\"";
            appendTv("onDescriptorRead");
            Log.i("OnScanResultTag", log);
            displayDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("OnScanResultTag", " onCharacteristicWrite ");
            appendTv("onCharacteristicWrite");
            displayCharacteristic(characteristic);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            appendTv(" onCharacteristicChanged " + StringHelpers.byteToString(characteristic.getValue()));
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFY));
            displayDescriptor(descriptor);
            Log.i("OnScanResultTag", " onCharacteristicChanged ");

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

        editText = (EditText) findViewById(R.id.editText);
        enableEdit(false);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    writeCharacteristic(v.getText().toString());
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect peripherals.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
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
        peripheralTextView.setText("Start scanning...\n");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                btScanner.startScan(Collections.singletonList(scanFilter),
                        scanSettings,
                        leScanCallback);
//                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        appendTv("Stopped Scanning...");
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
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null)
            mBluetoothGatt.disconnect();
        stopScanning();
    }


    private void activateNotifCharac(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        mBluetoothGatt.readCharacteristic(characteristic);

    }

    private void activateNotifWrite(BluetoothGattCharacteristic characteristic, UUID uuid) {
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(uuid);
        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        //clientConfig.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        mBluetoothGatt.writeDescriptor(clientConfig);
    }

    private void displayALLServices(BluetoothGatt gatt) {
        Log.i("OnScanResultTag", "\n **** Gatt  of " + gatt.getDevice().getAddress() + " *****");
        for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
            displayCharacteristicUuid(bluetoothGattService);
            Log.i("OnScanResultTag", " bluetoothGattService  =>  " + bluetoothGattService.getUuid());
        }
    }

    private void displayCharacteristicUuid(BluetoothGattService bluetoothGattService) {
        Log.i("OnScanResultTag", "\n **** Characteristics of  service  " + bluetoothGattService.getUuid() + " *****");

        for (BluetoothGattCharacteristic Characteris : bluetoothGattService.getCharacteristics()) {


            String charcLog = " **** Characteris  =>  property = " + StringHelpers.fromDecimalToBinary(Characteris.getProperties())
                    + " getPermissions = " + Characteris.getPermissions()
                    + " getWriteType = " + Characteris.getWriteType()
                    + " getValue = " + Characteris.getValue();
            Log.e("OnScanResultTag", charcLog);
            appendTv(charcLog);
            displayDescriptors(Characteris);
            break;
        }
    }

    private void displayDescriptors(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        Log.i("OnScanResultTag", "\n **** descriptors of  BluetoothGattCharacteristic " + bluetoothGattCharacteristic.getUuid() + " *****");

        for (BluetoothGattDescriptor descriptor : bluetoothGattCharacteristic.getDescriptors()) {
            Log.i("OnScanResultTag", " **** descriptor  ***** =>  uuid  = " + descriptor.getUuid()
                    + ",  value = " + descriptor.getValue());
//                activateNotifWrite(bluetoothGattCharacteristic, descriptor.getUuid()) ;

//            appendTv(" **** descriptor  ***** =>  uuid  = " + descriptor.getUuid()
//                    + ",  value = " + descriptor.getValue());
            if (descriptor.getUuid().equals(UUID.fromString(DESCRIPTOR_NOTIFY)))
//            mBluetoothGatt.readDescriptor(descriptor);
                enableCharacteristicNotification(mBluetoothGatt,
                        bluetoothGattCharacteristic,
                        true);

        }
    }

    private void displayServicesUuid(BluetoothGatt gatt, String uuidStr) {
        Log.i("OnScanResultTag", "\n **** Gatt  of " + gatt.getDevice().getAddress() + " *****");
        BluetoothGattService service = gatt.getService(UUID.fromString(uuidStr));
        BluetoothGattCharacteristic characteris = service.getCharacteristic(UUID.fromString(CHARATERISTIC_4));
        gatt.readCharacteristic(characteris);

        enableCharacteristicNotification(gatt,
                characteris,
                true);
//        displayDescriptors(Characteris);
    }

    private void displayCharacteristic(BluetoothGattCharacteristic Characteris) {
        Log.i("OnScanResultTag", "\n **** Display  Characteristics  " + Characteris.getUuid() + " *****");
        Log.e("OnScanResultTag", "**** property = " + Characteris.getProperties()
                + "**** value = " + Arrays.toString(Characteris.getValue())
                + "**** getPermissions = " + Characteris.getPermissions());

        String log = "\n**** Display  Characteristics: uuid = " + Characteris.getUuid() + " *****"
                + " \n**** property = " + StringHelpers.fromDecimalToBinary(Characteris.getProperties())
                + "\n**** value = " + Arrays.toString(Characteris.getValue())
                + "\n**** StringValue = " + StringHelpers.byteToString(Characteris.getValue())
                + "\n**** getPermissions = " + Characteris.getPermissions();
        appendTv(log);
        displayDescriptors(Characteris);
    }

    private void displayDescriptor(BluetoothGattDescriptor descriptor) {
        Log.i("OnScanResultTag", "\n**** DisplayDescriptor: uuid = " + descriptor.getUuid() + " *****");
        Log.i("OnScanResultTag", " **** descriptor  ***** =>  uuid  = " + descriptor.getUuid()
                + ",  value = " + Arrays.toString(descriptor.getValue()));
        String hexStringValue = StringHelpers.bytesToHex(descriptor.getValue());


        Log.e("OnScanResultTag", " **** descriptor  ***** =>  uuid  = " + descriptor.getUuid()
                + ",  value = " + hexStringValue);

        String log = "\n____________________________\n"
                + "\n**** Display DESCRIPTOR : uuid = " + descriptor.getUuid() + " *****"
                + "\n**** value bytes = " + Arrays.toString(descriptor.getValue())
                + "\n**** hexStringValue " + hexStringValue
                + "\n**** StringValue " + hexStringToString(hexStringValue)
                + "\n____________________________\n";
        appendTv(log);

    }

    private void appendTv(final String text) {
        peripheralTextView.post(new Runnable() {
            @Override
            public void run() {
                peripheralTextView.append(text + "\n");
                //                // auto scroll for text view
                final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                // if there is no need to scroll, scrollAmount will be <=0
                if (scrollAmount > 0)
                    peripheralTextView.scrollTo(0, scrollAmount);
            }
        });
    }

    private void enableEdit(final boolean enable) {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.setEnabled(enable);
            }
        });
    }


    public boolean enableCharacteristicNotification(BluetoothGatt bluetoothGatt,
                                                    BluetoothGattCharacteristic characteristic,
                                                    boolean enable) {
        appendTv("\n**** enableCharacteristicNotification  ****  ==> " + enable);
        bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFY));
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
        return bluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?

    }

    public void writeCharacteristic(String value) {
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(SERVICE_3));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARATERISTIC_4));
        characteristic.setValue(value);
//        characteristic.setValue(2, BluetoothGattCharacteristic.FORMAT_UINT8, 0) ; // bytes
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     *
     * I/OnScanResultTag:  **** descriptors of  BluetoothGattCharacteristic 0000fff4-0000-1000-8000-00805f9b34fb *****
     I/OnScanResultTag:  **** descriptor  ***** =>  uuid  = 00002904-0000-1000-8000-00805f9b34fb,  value = null
     I/OnScanResultTag:  **** descriptor  ***** =>  uuid  = 00002902-0000-1000-8000-00805f9b34fb,  value = null
     I/OnScanResultTag:  **** descriptor  ***** =>  uuid  = 00002901-0000-1000-8000-00805f9b34fb,  value = null
     **/
}
