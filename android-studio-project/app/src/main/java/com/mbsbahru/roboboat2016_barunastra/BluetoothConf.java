package com.mbsbahru.roboboat2016_barunastra;

import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothConf extends Activity {
	private static final int ENABLE_BT_REQ = 10000;

	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter<String> pairedDevicesAdapter;
	private ListView pairedDevices;
	private ProgressBar progress;
	private ListView availableDevices;
	private ArrayAdapter<String> availableDevicesAdapter;
	private List<BluetoothDevice> paired;
	private List<BluetoothDevice> available;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);

		pairedDevices = findViewById(R.id.pairedDevices);
		pairedDevices.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
			final BluetoothDevice device = ((BluetoothDevice) paired.toArray()[arg2]);
			Intent intent = new Intent(BluetoothConf.this, MainActivity.class);
			intent.putExtra("device", device);
			startActivity(intent);
		});
		pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		pairedDevices.setAdapter(pairedDevicesAdapter);

		availableDevices = findViewById(R.id.availableDevices);
		availableDevices.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
			final BluetoothDevice device = ((BluetoothDevice) available.toArray()[arg2]);
			Intent intent = new Intent(BluetoothConf.this, MainActivity.class);
			intent.putExtra("device", device);
			startActivity(intent);

			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
					ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_BT_REQ);
			} else {
				discoverDevices();
			}

		});

		availableDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		availableDevices.setAdapter(availableDevicesAdapter);

		progress = (ProgressBar) findViewById(R.id.progress);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Kok tidak ada Bluetooth sama sekali", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, ENABLE_BT_REQ);
		} else {
			discoverDevices();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ENABLE_BT_REQ) {
			if (resultCode == RESULT_OK) {
				discoverDevices();
			}
		}
	}

	private void discoverDevices() {
		pairedDevicesAdapter.clear();
		availableDevicesAdapter.clear();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
					ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT}, ENABLE_BT_REQ);
			} else {
				bluetoothAdapter.startDiscovery();
			}
		} else {
			// Fallback for older versions
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
					ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_BT_REQ);
			} else {
				bluetoothAdapter.startDiscovery();
			}
		}

		paired = new ArrayList<>(bluetoothAdapter.getBondedDevices());
		for (BluetoothDevice device : paired) {
			pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
		}

		((BaseAdapter) pairedDevices.getAdapter()).notifyDataSetChanged();
		bluetoothAdapter.startDiscovery();
	}

//	private void discoverDevices() {
//	// Register the BroadcastReceiver
//	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//	filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//	filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//	registerReceiver(mReceiver, filter);
//
//	paired = new ArrayList<BluetoothDevice>(bluetoothAdapter.getBondedDevices());
//	for (BluetoothDevice device : paired) {
//	    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
//	}
//
//	((BaseAdapter) pairedDevices.getAdapter()).notifyDataSetChanged();
//
//	bluetoothAdapter.startDiscovery();
//    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem add = menu.add("Pindai");
		add.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Context context = BluetoothConf.this;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
							ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions((Activity) context,
								new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT}, ENABLE_BT_REQ);
					} else {
						discoverDevices();
					}
				} else {
					// Fallback for older versions
					if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
							ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions((Activity) context,
								new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_BT_REQ);
					} else {
						discoverDevices();
					}
				}

				bluetoothAdapter.startDiscovery();
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (available == null) {
					available = new ArrayList<BluetoothDevice>();
				}

				if (!available.contains(device)) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
						if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
								ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions((Activity) context,
									new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT}, ENABLE_BT_REQ);
						} else {
							discoverDevices();
						}
					} else {
						// Fallback for older versions
						if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
								ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions((Activity) context,
									new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_BT_REQ);
						} else {
							discoverDevices();
						}
					}
					availableDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
		    availableDevicesAdapter.notifyDataSetChanged();
		}

		available.add(device);
	    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
		progress.setVisibility(View.VISIBLE);
	    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		progress.setVisibility(View.INVISIBLE);
	    }
	}
    };

}     