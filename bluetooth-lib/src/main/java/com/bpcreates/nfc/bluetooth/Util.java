package com.bpcreates.nfc.bluetooth;

import static java.lang.String.format;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/11/13
 * Time: 8:31 PM
 */
public class Util {
    public static boolean isEmpty(String val) {
        return null == val || val.trim().isEmpty();
    }

    public static int str2hex(String str, byte[] desbuf) {
        byte[] tmpbuf = str.getBytes();
        for (int i = 0; i < tmpbuf.length; i++) {
            if (tmpbuf[i] >= '0' && tmpbuf[i] <= '9') {
                tmpbuf[i] = (byte) (tmpbuf[i] - '0');
            } else if (tmpbuf[i] >= 'A' && tmpbuf[i] <= 'F') {
                tmpbuf[i] = (byte) (tmpbuf[i] - 'A' + 0x0A);
            } else {
                return 0;
            }
        }
        for (int i = 0; i < tmpbuf.length - 1; i += 2) {
            desbuf[i / 2] = (byte) (0xf0 & (tmpbuf[i] << 4));
            desbuf[i / 2] |= 0x0f & tmpbuf[i + 1];
        }
        return 1;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static final Integer BLOCK_SIZE = 4;

    public static byte XORByte(byte[] source,long size)
    {
        byte bDest = 0x00;
        for(int i=0; i<size; i++)
        {
            bDest ^= source[i];
        }
        return bDest;
    }

    public static String asString(byte[] data, int len) {
        StringBuilder message = new StringBuilder(len);
        for (int i = 0; i<len; i++) {
            message.append( format("%02X", data[i]) ) ;
        }
        return message.toString();
    }

    public static byte[] toByteArray(int data) {
        return new byte[] {
                (byte)((data >> 24) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 0) & 0xff),
        };
    }


    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        return
            (0xff & data[0]) << 24  |
            (0xff & data[1]) << 16  |
            (0xff & data[2]) << 8   |
            (0xff & data[3]) << 0
        ;
    }
}
