package com.bpcreates.nfc.bluetooth;

import android.os.Handler;
import android.os.Message;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:08 PM
 */
class ReaderHandler extends Handler {
    private final RfidReadListener listener;

    public ReaderHandler(RfidReadListener listener) {
        if(null == listener) {
            throw new IllegalArgumentException("null listener");
        }
        this.listener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
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
            case BluetoothService.MESSAGE_WRITE:
                listener.onMessageWrite(msg);
                break;
            case BluetoothService.MESSAGE_READ:
                int len = msg.arg1;
                byte[] data = (byte[]) msg.obj;
                String message = Util.asString(data, len);
                listener.onReadRfid(message,len);
                break;
            case BluetoothService.MESSAGE_DEVICE_NAME:
                String mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                listener.onLinkDevice(mConnectedDeviceName);
                break;
            case BluetoothService.MESSAGE_TOAST:
                listener.onMessageToast();
                break;
            case BluetoothService.MESSAGE_TIMEOUT:
                listener.onTimeout();
                break;
        }
    }
}