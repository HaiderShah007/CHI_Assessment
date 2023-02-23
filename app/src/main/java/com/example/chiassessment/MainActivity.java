package com.example.chiassessment;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chiassessment.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    boolean mScanning;
    private ActivityMainBinding binding;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000; // Scan for 10 seconds
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    private Set<String> mDeviceAddresses;
    private android.bluetooth.BluetoothAdapter mBluetoothAdapter;
    ArrayList<String> rssiList = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 101;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_Scan = 111;
    // UUIDs for the service and characteristics
    private static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    CustomBluetoothAdapter adapter = null;
    private List<ScanFilter> filters = new ArrayList<>();
    private BluetoothLeScanner bluetoothLeScanner;
    ScanSettings scanSettings;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (checkIsDeviceSupported()) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            //Devices shown at my side when i pass null
            //filters.add(new ScanFilter.Builder().setDeviceName(null).build());
            filters.add(new ScanFilter.Builder().setDeviceName("SWAN").build());
            filters.add(new ScanFilter.Builder().setDeviceAddress("F8:0B:AC:1B:A0:C1").build());


            scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setReportDelay(1l)
                    .build();
            mHandler = new Handler();
            mDeviceAddresses = new HashSet<>();
            requestLocationPermission();
            requestBluetoothPermission();

        } else {
            Toast.makeText(this, "Your device not supported BLE", Toast.LENGTH_SHORT).show();
        }

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ENABLE_BT);
        }
    }

    private void requestBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_Scan);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_Scan);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onResume() {
        super.onResume();

        // Request Bluetooth to be enabled if not enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
            } else {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        } else {
            scanLeDevice(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled, start scanning for devices
                scanLeDevice(true);
            } else {
                // The user did not enable Bluetooth, close the app
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
    }


    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice().getName() != null) {
                Log.e("Result", result.getDevice().getName());
                Toast.makeText(MainActivity.this, result.getDevice().getName(), Toast.LENGTH_SHORT).show();
            }


        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            if (results != null) {
                binding.progress.setVisibility(View.GONE);
                setUpRecyclerView(results);
            } else {
                Toast.makeText(MainActivity.this, "No Device Found", Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this, "Error" + errorCode, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT) {
            // If the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this,SplashScreen.class));
            } else {
                // Permission denied, display a message and close the app
                Toast.makeText(this, "Bluetooth permission required for scanning", Toast.LENGTH_SHORT).show();

            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_Scan) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanLeDevice(true);

            } else {
                Toast.makeText(this, "Bluetooth permission required for scanning", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(() -> {
                mScanning = false;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner.startScan(filters, scanSettings, mLeScanCallback);
                }

            }, SCAN_PERIOD);

            mScanning = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }

        } else {
            mScanning = false;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner.startScan(filters, scanSettings, mLeScanCallback);
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void setUpRecyclerView(List<ScanResult> list) {

        adapter = new CustomBluetoothAdapter(list, device -> {
            bluetoothLeScanner.stopScan(mLeScanCallback);
            Toast.makeText(this, device.getAddress(), Toast.LENGTH_SHORT).show();
            // BluetoothDevice selectedDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
            //selectedDevice.setPairingConfirmation(true);
            BluetoothGatt bluetoothGatt = device.connectGatt(this, true, gattCallback);
            bluetoothGatt.disconnect();
            bluetoothGatt.connect();

        });
        LinearLayoutManager lm = new LinearLayoutManager(this);
        binding.rvBluetoothDevices.setLayoutManager(lm);
        binding.rvBluetoothDevices.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.discoverServices();
                    Toast.makeText(MainActivity.this, "Connection Successful", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(MainActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            //   BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);
            if (service != null) {
                // Iterate over the characteristics of the service
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    int properties = characteristic.getProperties();

                    // Determine the properties of the characteristic
                    boolean hasReadProperty = (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
                    boolean hasWriteProperty = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                    boolean hasNotifyProperty = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;

                    // Obtain the UUID and value of the characteristic
                    UUID uuid = characteristic.getUuid();
                    byte[] value = characteristic.getValue();

                    // Print the properties and values of the characteristic
                    Log.d(TAG, "Characteristic UUID: " + uuid);
                    Log.d(TAG, "Read property: " + hasReadProperty);
                    Log.d(TAG, "Write property: " + hasWriteProperty);
                    Log.d(TAG, "Notify property: " + hasNotifyProperty);
                    Log.d(TAG, "Value: " + Arrays.toString(value));
                }
            }

        }
    };

    private Boolean checkIsDeviceSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

}