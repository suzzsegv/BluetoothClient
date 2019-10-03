package com.example.bluetoothclient;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_DISCOVERABLE_BT = 2;
    final int REQUEST_PERMISSION = 1000;
    ListView listView;
    ArrayAdapter<String> mArrayAdapter;
    private ScanCallback mScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        BluetoothManager bluetoothManager =
                (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(MainActivity.class.getName(), "Device does not support Bluetooth");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // mBluetoothLeScannerの初期化
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Register for broadcasts when a device is discovered
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        this.registerReceiver(mReceiver, filter);

//        mBluetoothAdapter.startDiscovery();
        // Register for broadcasts when discovery has finished
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        this.registerReceiver(mReceiver, filter);

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(MainActivity.class.getName(), "callbackType = " + callbackType);
                BluetoothDevice bluetoothDevice = result.getDevice();
                Log.d(MainActivity.class.getName(), "address:" + bluetoothDevice.getAddress());
                Log.d(MainActivity.class.getName(), "name:" + bluetoothDevice.getName());
                mArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(MainActivity.class.getName(), "onBatchScanResults()");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(MainActivity.class.getName(), "onScanFailed()");
            }
        };

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            private Handler handler;
            private final int SCAN_PERIOD = 30000;
            @Override
            public void onClick(View view) {
                //スキャニングを30秒後に停止
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothLeScanner.stopScan(mScanCallback);
                    }
                }, SCAN_PERIOD);
                //スキャンの開始
                mBluetoothLeScanner.startScan(mScanCallback);
            }
        });

        listView = (ListView)findViewById(R.id.ListView1);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mArrayAdapter);
    }

    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "ACCESS_FINE_LOCATION は許可されています。", Toast.LENGTH_LONG).show();
        }
        // 許可されていない場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        // 許可が必要な説明を表示するかどうか判定
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "ACCESS_FINE_LOCATION: 追加説明", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "ACCESS_FINE_LOCATION: 今後は確認しないが選ばれている。", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION);
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ACCESS_FINE_LOCATION が許可されました。", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION は拒否されました。", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth を使用できません。", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 90);
                    startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
                }
                break;
            case REQUEST_DISCOVERABLE_BT:
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth 機器を検出できません。", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Toast.makeText(this, "Bluetooth 機器の検出を開始します。", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Bluetooth 機器が見つかりませんでした。", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
}
