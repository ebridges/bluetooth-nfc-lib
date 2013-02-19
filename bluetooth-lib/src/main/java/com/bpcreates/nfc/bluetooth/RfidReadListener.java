package com.bpcreates.nfc.bluetooth;

import android.os.Message;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:31 PM
 */
public interface RfidReadListener {
    public void onReadRfid(String rfid,int len);
    public void onConnected();
    public void onNotConnected();
    public void onLinkDevice(String mConnectedDeviceName);
    public void onTimeout();
    public void onConnectionLost();
}
