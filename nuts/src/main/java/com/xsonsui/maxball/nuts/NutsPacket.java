package com.xsonsui.maxball.nuts;

import static com.xsonsui.maxball.nuts.NutsConstants.HEADER_SIZE;

/**
 * Created by alim on 10/3/14.
 */
public class NutsPacket {
    public int seqNo;
    public byte parts;
    public byte index;
    public short length;
    public short crc;

    public byte[] data;

    private NutsPacket(int seqNo, byte parts, byte index, short length, short crc, byte[] data){
        this.seqNo = seqNo;
        this.parts = parts;
        this.index = index;
        this.length = length;
        this.crc = crc;
        this.data = data;
    }

    public static void computePacketHeader(byte[] outBuffer, int seqNo, int length, int packets, int index) {
        outBuffer[0] = (byte) ((seqNo>>24)&0xff);
        outBuffer[1] = (byte) ((seqNo>>16)&0xff);
        outBuffer[2] = (byte) ((seqNo>>8)&0xff);
        outBuffer[3] = (byte) ((seqNo)&0xff);

        outBuffer[4] = (byte) ((packets)&0xff);
        outBuffer[5] = (byte) ((index)&0xff);

        outBuffer[6] = (byte) ((length>>8)&0xff);
        outBuffer[7] = (byte) ((length)&0xff);

        int checksum = NutsConstants.checksum(outBuffer, HEADER_SIZE, length);
        outBuffer[8] = (byte) ((checksum>>8)&0xff);
        outBuffer[9] = (byte) ((checksum)&0xff);

    }

    public static NutsPacket processHeader(byte[] data) throws PacketIntegrityException {

        int seqNo = ((data[0]&0xff)<<24) | ((data[1]&0xff)<<16) | ((data[2]&0xff)<<8) | (data[3]&0xff);
        byte parts = data[4];
        byte index = data[5];
        short length = (short) (((data[6]&0xff)<<8) | (data[7]&0xff));
        short crc = (short) (((data[8]&0xff)<<8) | (data[9]&0xff));

        short checksum = (short) NutsConstants.checksum(data, HEADER_SIZE, length);

        if (crc != checksum) {
            throw new PacketIntegrityException("otur agla :(");
        }

        return new NutsPacket(seqNo, parts, index, length, crc, data);
    }

    public static class PacketIntegrityException extends Exception {
        public PacketIntegrityException(String s) {
            super(s);
        }
    }
}
