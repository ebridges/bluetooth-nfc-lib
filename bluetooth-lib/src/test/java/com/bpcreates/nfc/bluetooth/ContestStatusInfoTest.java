package com.bpcreates.nfc.bluetooth;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/16/13
 * Time: 6:30 PM
 */
public class ContestStatusInfoTest {
    private static final String TEST_PHONE_NUM = "2125551212";
    private static final Integer TEST_STATUS = 5;
    private static final byte[] TEST_FIRST_BLOCK = Util.toByteArray( Integer.parseInt( TEST_STATUS+TEST_PHONE_NUM.substring(0,3)) );
    private static final byte[] TEST_SECOND_BLOCK =  Util.toByteArray( Integer.parseInt( TEST_PHONE_NUM.substring(3) ) );

    @Test
    public void testParsePhoneNum(){
        String areaCode = TEST_PHONE_NUM.substring(0,3);
        System.out.println("areaCode: "+ areaCode);
        assertEquals(3, areaCode.length());
        String phone = TEST_PHONE_NUM.substring(3);
        System.out.println("phoneNum: "+phone);
        assertEquals(7, phone.length());
    }

    @Test
    public void testGetContestStatus() throws Exception {
        ContestStatusInfo info = new ContestStatusInfo(TEST_FIRST_BLOCK, TEST_SECOND_BLOCK);
        assertEquals(TEST_STATUS, info.getContestStatus());
    }

    @Test
    public void testGetPhoneNum() throws Exception {
        ContestStatusInfo info = new ContestStatusInfo(TEST_FIRST_BLOCK, TEST_SECOND_BLOCK);
        assertEquals(TEST_PHONE_NUM, info.getPhoneNum());
    }

    @Test
    public void testGetFirstBlock() throws Exception {
        ContestStatusInfo info = new ContestStatusInfo(TEST_STATUS, TEST_PHONE_NUM);
        assertEquals(TEST_FIRST_BLOCK.length, info.getFirstBlock().length);
        for(int i=0; i<TEST_FIRST_BLOCK.length; i++) {
            System.out.println("block idx: "+i);
            assertEquals(TEST_FIRST_BLOCK[i], info.getFirstBlock()[i]);
        }
    }

    @Test
    public void testGetSecondBlock() throws Exception {
        ContestStatusInfo info = new ContestStatusInfo(TEST_STATUS, TEST_PHONE_NUM);
        assertEquals(TEST_SECOND_BLOCK.length, info.getSecondBlock().length);
        for(int i=0; i<TEST_SECOND_BLOCK.length; i++) {
            System.out.println("block idx: "+i);
            assertEquals(TEST_SECOND_BLOCK[i], info.getSecondBlock()[i]);
        }
    }
}
