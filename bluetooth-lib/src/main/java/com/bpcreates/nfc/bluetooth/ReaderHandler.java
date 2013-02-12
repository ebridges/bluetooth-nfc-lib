package com.bpcreates.nfc.bluetooth;

import android.os.Handler;
import android.os.Message;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:08 PM
 */
public class ReaderHandler extends Handler {

    private RfidReadListener listener;
    @Override
    public void handleMessage(Message msg) {

        switch (msg.what)
        {
            // Status Changed
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1)
                {
                    case BluetoothService.STATE_CONNECTED:
                        listener.onConnected();
                        break;
                    case BluetoothService.STATE_CONNECTING:
                        break;
                    case BluetoothService.STATE_LISTEN:
                    case BluetoothService.STATE_NONE:
                        listener.onNotConnected();
                        break;
                }
                break;
            // Send
            case BluetoothService.MESSAGE_WRITE:
                break;

            // Receive
            case BluetoothService.MESSAGE_READ:
                if (listener != null)
                {
                    int len = msg.arg1;
                    byte[] tmpBuf = (byte[]) msg.obj;
                    byte[] readBuf= new byte[len];

                    System.arraycopy(tmpBuf,0,readBuf,0,len);
                    String readMessage ="";

                    for (int i = 0; i < len; i++)
                    {
                        readMessage += String.format("%02X", readBuf[i]);
                    }
                    listener.onReadRfid(readMessage,len);
                }
                break;
            case BluetoothService.MESSAGE_DEVICE_NAME:
                String mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                listener.onLinkDevice(mConnectedDeviceName);
                break;
            case BluetoothService.MESSAGE_TOAST:
                break;
            case BluetoothService.MESSAGE_TIMEOUT:
                listener.onTimeout();
                break;
        }
    }

    public void setListener(RfidReadListener listener) {
        this.listener = listener;
    }
}