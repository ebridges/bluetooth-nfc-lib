package com.bpcreates.nfc.bluetooth;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 2/16/13
 * Time: 5:57 PM
 */
public class ContestStatusService {
    private RfidReader reader;

    public ContestStatusService(RfidReader reader) {
        this.reader = reader;
    }

    public ContestStatusService(RfidReadListener listener) {
        this.reader = new RfidReader(listener);
    }

    public void readContestStatus() {
        this.reader.readBlocks(ContestStatusInfo.FIRST_BLOCK_POSITION);
        this.reader.readBlocks(ContestStatusInfo.SECOND_BLOCK_POSITION);
    }

    public void writeContestStatus(ContestStatusInfo info) {
        this.reader.writeBlocks(ContestStatusInfo.FIRST_BLOCK_POSITION, info.getFirstBlock());
        this.reader.writeBlocks(ContestStatusInfo.SECOND_BLOCK_POSITION, info.getSecondBlock());
    }
}
