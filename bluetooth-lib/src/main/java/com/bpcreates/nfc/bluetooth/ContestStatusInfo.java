package com.bpcreates.nfc.bluetooth;

import static com.bpcreates.nfc.bluetooth.Util.isEmpty;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/16/13
 * Time: 5:58 PM
 */

public class ContestStatusInfo {
    public static final byte FIRST_BLOCK_POSITION = 4;
    public static final byte SECOND_BLOCK_POSITION = 5;

    private String phoneNum;
    private Integer contestStatus;
    private byte[] firstBlock;
    private byte[] secondBlock;

    public ContestStatusInfo(Integer contestStatus, String phoneNum) {
        validatePhoneNum(phoneNum);
        this.phoneNum = phoneNum;
        this.contestStatus = contestStatus;
        this.firstBlock = initFirstBlock();
        this.secondBlock = initSecondBlock();
    }

    public ContestStatusInfo(byte[] firstBlock, byte[] secondBlock) {
        this.firstBlock = firstBlock;
        this.secondBlock = secondBlock;
        initFields();
    }

    private void initFields() {
        StringBuilder sb = new StringBuilder(32);

        int first = Util.toInt(this.firstBlock);
        int second = Util.toInt(this.secondBlock);

        sb.append(first);
        sb.append(second);

        String val = sb.toString();

        this.contestStatus = Integer.parseInt(val.substring(0, 1));
        this.phoneNum = sb.substring(1);
    }

    public Integer getContestStatus() {
        return contestStatus;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    byte[] getFirstBlock() {
        return firstBlock;
    }

    byte[] getSecondBlock() {
        return secondBlock;
    }

    private byte[] initFirstBlock() {
        StringBuilder sb = new StringBuilder(24);
        sb.append(contestStatus);
        sb.append(phoneNum.substring(0, 3));
        Integer val = Integer.parseInt(sb.toString());
        return Util.toByteArray(val);
    }

    private byte[] initSecondBlock() {
        Integer val = Integer.parseInt(phoneNum.substring(3));
        return Util.toByteArray(val);
    }

    private static void validatePhoneNum(String phoneNum) {
        if(isEmpty(phoneNum) || phoneNum.length() != 10) {
            throw new IllegalArgumentException("invalid phonenum");
        }
        char[] chars = phoneNum.toCharArray();
        for(int i=0; i<chars.length; i++) {
            if(!Character.isDigit(chars[i])) {
                throw new IllegalArgumentException("non-numeric phonenum");
            }
        }
    }
}
