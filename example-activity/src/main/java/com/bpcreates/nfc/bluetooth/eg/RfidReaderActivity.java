package com.bpcreates.nfc.bluetooth.eg;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bpcreates.nfc.R;
import com.bpcreates.nfc.bluetooth.RfidReadListener;
import com.bpcreates.nfc.bluetooth.RfidReader;
import com.bpcreates.nfc.bluetooth.Util;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:59 PM
 */
public class RfidReaderActivity extends Activity implements RfidReadListener {
    private static final String TAG = "N330B.RfidReader";

    private static final String READERNAME1 = "rs9a-nxp-reader";
    private static final String READERNAME2 = "RS-9BTRFIDReader";

    private BluetoothAdapter bluetoothAdapter;
    private static int findDeviceNum = 0;
    private ArrayAdapter<String> newDevicesArrayAdapter;
    private Spinner devicesList;
    private RfidReader rfidReader = new RfidReader(this);
    private EditText mEditUID, mEditBlocks, mEditWriteBlocks, mEditKey;
    private CheckBox keyCheckBox;
    private static boolean isConnected = false;
    private String readMessage = "";
    public boolean bHaveCmdProcess = false;
    private byte[] writeBuffer = new byte[Util.BLOCK_SIZE];
    public byte[] password = new byte[6];    //Key value

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }

        Button scanButton = (Button) findViewById(R.id.bt_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        isConnected = false;
        mEditUID = (EditText) findViewById(R.id.TxtUID);
        mEditBlocks = (EditText) findViewById(R.id.TxtBlocks);
        mEditWriteBlocks = (EditText) findViewById(R.id.ETxtWriteBlocks);
        keyCheckBox = (CheckBox) findViewById(R.id.id_checkKey);
        mEditKey = (EditText) findViewById(R.id.eTxtIdKey);
        devicesList = (Spinner) findViewById(R.id.listDevices);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);//, strDevice);
        newDevicesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesList.setAdapter(newDevicesArrayAdapter);

        Button openButton = (Button) findViewById(R.id.bt_opendev);
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isConnected) {
                    showMsg("Device is ON already");
                    return;
                }

                bluetoothAdapter.cancelDiscovery();
                if (-1 == devicesList.getSelectedItemPosition()) {
                    showMsg("Select the device");
                    return;
                }

                // Get the device MAC address, which is the last 17 chars
                String info = devicesList.getSelectedItem().toString();
                String address = info.substring(info.length() - 17);
                Log.d(TAG, "device addr:" + address);

                Log.d(TAG, "connecting device...");
                rfidReader.connect(address);
            }
        });

        Button wirteBlocksBtn = (Button) findViewById(R.id.btn_writeblocks);
        wirteBlocksBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isConnected) {
                    showMsg("Open the device first");
                    return;
                }

                if (checkKey() != 1) {
                    return;
                }

                byte[] tmpbuf = mEditWriteBlocks.getText().toString().getBytes();
                if (tmpbuf.length != Util.BLOCK_SIZE * 2) {
                    showMsg("Input " + Util.BLOCK_SIZE + " bytes of HEX data");
                    return;
                }

                for (int i = 0; i < tmpbuf.length; i++) {
                    if (tmpbuf[i] >= '0' && tmpbuf[i] <= '9') {
                        tmpbuf[i] = (byte) (tmpbuf[i] - '0');
                    } else if (tmpbuf[i] >= 'A' && tmpbuf[i] <= 'F') {
                        tmpbuf[i] = (byte) (tmpbuf[i] - 'A' + 0x0A);
                    } else {
                        showMsg("HEX only! (0~9 or A~F)");
                        return;
                    }
                }

                for (int i = 0; i < tmpbuf.length - 1; i += 2) {
                    writeBuffer[i / 2] = (byte) (0xf0 & (tmpbuf[i] << 4));
                    writeBuffer[i / 2] |= 0x0f & tmpbuf[i + 1];
                }

                if (bHaveCmdProcess) {
                    return;
                }

                bHaveCmdProcess = true;
                readMessage = "";
                lastAA = false;

                if (keyCheckBox.isChecked()) {
                    rfidReader.writeBlocks((byte) 0, password, writeBuffer);
                } else {
                    rfidReader.writeBlocks((byte) 1, password, writeBuffer);
                }
            }
        });

        Button readUidBtn = (Button) findViewById(R.id.bt_readuid);
        readUidBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isConnected) {
                    showMsg("Open the device first");
                    return;
                }

                if (bHaveCmdProcess) {
                    return;
                }

                bHaveCmdProcess = true;
                readMessage = "";
                lastAA = false;

                rfidReader.scanRfid();
            }
        });

        Button readBlocksBtn = (Button) findViewById(R.id.btn_readBlocks);
        readBlocksBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isConnected) {
                    showMsg("Open the device first");
                    return;
                }

                if (checkKey() != 1) {
                    return;
                }

                if (bHaveCmdProcess) {
                    return;
                }

                bHaveCmdProcess = true;
                lastAA = false;
                readMessage = "";

                rfidReader.readBlocks(password);
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    private static String lastString = "";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                {
                    String strtmp;
                    strtmp = device.getName() + device.getAddress();

                    if (READERNAME1.equals(device.getName()) || READERNAME2.equals(device.getName())) {
                        if (lastString.endsWith(strtmp)){
                            return;
                        }

                        lastString = strtmp;

                        findDeviceNum++;
                        newDevicesArrayAdapter.add(strtmp);
                    }
                    Log.d(TAG, "find device:" + device.getName()
                            + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (findDeviceNum == 0) {
                    showMsg("No Found Device!");
                }
            }
        }
    };

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "doDiscovery()");
        }

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        findDeviceNum = 0;
        newDevicesArrayAdapter.clear();

        // Request discovery from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDestroy()");
        }
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        rfidReader.stop();
        if (rfidReader.timer != null) {
            rfidReader.timer.cancel();
        }
        rfidReader.timer = null;

        this.unregisterReceiver(mReceiver);
        finish();

        Runtime.getRuntime().exit(0);
    }

    public int checkKey() {
        if (mEditKey.length() != 12) {
            showMsg("Input 6 bytes of Key");
            return 0;
        }

        if (Util.str2hex(mEditKey.getText().toString(), password) != 1) {
            showMsg("Invalid data! make sure it is HEX only");
            return 0;
        }

        return 1;
    }

    public void resetCmd() {
        readMessage = "";
        lastAA = false;
        if (null != rfidReader.timer) {
            rfidReader.timer.cancel();
        }
        bHaveCmdProcess = false;
    }

    private boolean lastAA = false;

    public void onReadRfid(String rfid, int len) {
        String tmpString = "";
        tmpString += rfid;

        Log.d(TAG, "Value read from RFID: " + rfid);

        if (tmpString.startsWith("00") && lastAA) {
            tmpString = tmpString.substring(2);
        }

        //noinspection RedundantIfStatement
        if (tmpString.endsWith("AA")) {
            lastAA = true;
        } else {
            lastAA = false;
        }

        if (tmpString.contains("AA00")) {
            tmpString = tmpString.replaceAll("AA00", "AA");
        }

        readMessage += tmpString;

        if (readMessage.startsWith("AA55")) {
            if (readMessage.length() >= 6) {
                byte[] tmpbuf = readMessage.getBytes();
                for (int i = 0; i < tmpbuf.length; i++) {
                    if (tmpbuf[i] >= '0' && tmpbuf[i] <= '9') {
                        tmpbuf[i] = (byte) (tmpbuf[i] - '0');
                    } else if (tmpbuf[i] >= 'A' && tmpbuf[i] <= 'F') {
                        tmpbuf[i] = (byte) (tmpbuf[i] - 'A' + 0x0A);
                    }
                }
                if (tmpbuf.length / 2 < 2)
                    return;

                byte[] hexbuf = new byte[tmpbuf.length / 2 - 2];
                for (int i = 4; i < tmpbuf.length - 1; i += 2) {
                    hexbuf[(i - 4) / 2] = (byte) (0xf0 & (tmpbuf[i] << 4));
                    hexbuf[(i - 4) / 2] |= 0x0f & tmpbuf[i + 1];
                }
                if (hexbuf.length >= (hexbuf[0] + 1)) {
                    byte len_in_cmd = hexbuf[0];
                    if (hexbuf[len_in_cmd] != Util.XORByte(hexbuf, hexbuf[0])) {
                        showMsg("Verification Failed");
                        resetCmd();
                        return;
                    }
                    byte cmd = hexbuf[1];

                    if (cmd == 0x20) {
                        String string = "";
                        for (int i = 0; i < hexbuf[0] - 2; i++) {
                            string += String.format("%02X", hexbuf[i + 2]);
                        }
                        mEditUID.setText(string);
                    } else if (cmd == 0x41) {
                        String string = "";
                        for (int i = 0; i < hexbuf[0] - 2; i++) {
                            string += String.format("%02X", hexbuf[i + 2]);
                        }
                        mEditBlocks.setText(string);
                    } else if (cmd == 0x42) {
                        showMsg("Block Writing Succeed");
                    } else {
                        showMsg("Error");
                    }
                    resetCmd();
                }
            }
        }
    }

    public void onConnected() {
        isConnected = true;
        Button scanButton = (Button) findViewById(R.id.bt_scan);
        scanButton.setEnabled(false);
        Button openButton = (Button) findViewById(R.id.bt_opendev);
        openButton.setEnabled(false);
        showMsg("Connected!");
    }

    public void onNotConnected() {
        isConnected = false;
        Button scanButton = (Button) findViewById(R.id.bt_scan);
        scanButton.setEnabled(true);
        Button openButton = (Button) findViewById(R.id.bt_opendev);
        openButton.setEnabled(true);
    }

    public void onLinkDevice(String mConnectedDeviceName) {
        Log.d(TAG, "onLinkDevice() called: " + mConnectedDeviceName);
        Log.d(TAG, "setting wait mode...");
        this.rfidReader.setWaitMode();
    }

    public void onTimeout() {
        synchronized (this) {
            resetCmd();
            showMsg("Connection Timed out!");
        }
    }

    public void showMsg(CharSequence text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}