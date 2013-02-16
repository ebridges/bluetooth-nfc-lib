package com.bpcreates.nfc.bluetooth;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:29 PM
 */
@SuppressWarnings("UnusedDeclaration")
public class RfidReader extends BluetoothService {
    private static final String TAG = "N330B.RfidReader";

    private Integer timeout;

    public Timer timer =null;
    private class MyTimeTask extends TimerTask {
        @Override
        public void run() {
            sendMessage(MESSAGE_TIMEOUT, -1, -1, null);
        }
    }

    public RfidReader(RfidReadListener listener) {
        super(new ReaderHandler(listener));
        this.timeout = Constants.DEFAULT_TIMEOUT_MS;
    }

    public void scanRfid() {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "scanRfid() called.");
        }

        if (getState() != STATE_CONNECTED) {
            return;
        }

        byte[] bCommand= new byte[16];
        int  iCommandLen;
        iCommandLen = 3+3;
        bCommand[0]=0x00;
        bCommand[1]=0x00;
        bCommand[2]=0x03;
        bCommand[3]=0x20;
        bCommand[4]=0x00;
        bCommand[5]= Util.XORByte(bCommand, iCommandLen - 1);

        Log.d(TAG, "SCAN: command array len: " + bCommand.length);
        Log.d(TAG, "SCAN: command array: "+Util.bytesToHex(bCommand));

        write(bCommand);

        waitOnDevice(this.timeout);
    }

    public void setWaitMode() {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "setWaitMode() called.");
        }

        if (getState() != STATE_CONNECTED) {
            return;
        }

        byte[] command=new byte[6];
        Integer commandLen = 4;
        command[0]=0x00;
        command[1]=0x00;
        command[2]=commandLen.byteValue();
        command[3]=0x06; // working mode system command
        command[4]=0x02; // set 'wait' working mode
        command[5]=0x06; // check code

        Log.d(TAG, "WAITMODE: command array len: " + command.length);
        Log.d(TAG, "WAITMODE: command array: "+Util.bytesToHex(command));

        write(command);

        waitOnDevice(this.timeout);
    }

    public void readBlocks(byte[] password) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "readBlocks() called.");
        }

        if (getState() != STATE_CONNECTED) {
            return;
        }

        byte[] send = new byte[7+6];
        send[0] = (byte) 0x00;
        send[1] = (byte) 0x00;
        send[2] = (byte) 0x0A;
        send[3] = (byte) 0x41;
        send[4] = (byte) 0x00;
        send[5] = (byte) 0x01;
        System.arraycopy(password, 0, send,6, 6);
        send[12]= Util.XORByte(send, 12);

        Log.d(TAG, "READ: command array len: " + send.length);
        Log.d(TAG, "READ: command array: "+ Util.bytesToHex(send));

        write(send);

        waitOnDevice(this.timeout);
    }

    public void writeBlocks(byte useKeyA, byte[] password,byte[] data) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "readBlocks() called.");
        }

        if (getState() != STATE_CONNECTED) {
            return;
        }

        byte[] send = new byte[ 6+Util.BLOCK_SIZE ];

        send[0] = (byte) 0x00;  // prefix
        send[1] = (byte) 0x00;  // prefix
        send[2] = (byte) 0x07;  // length
        send[3] = (byte) 0x42;  // command
        send[4] = (byte) 0x08;  // block num
        System.arraycopy(data, 0, send, 5, Util.BLOCK_SIZE);  // data
        send[send.length-1] = Util.XORByte(send, send.length - 1); // checksum

        Log.d(TAG, "WRITE: command array len: " + send.length);
        Log.d(TAG, "WRITE: command array: "+ Util.bytesToHex(send));

        write(send);

        waitOnDevice(this.timeout);
    }

    private void waitOnDevice(long waitLen) {
        if(timer!=null) {
            timer.cancel();
            timer=null;
        }
        timer = new Timer();
        timer.schedule(new MyTimeTask(), waitLen);
    }

}
