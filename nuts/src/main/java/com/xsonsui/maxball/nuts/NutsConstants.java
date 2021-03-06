package com.xsonsui.maxball.nuts;

/**
 * Created by alim on 9/14/14.
 */
public class NutsConstants {
    public static final int MAX_PACKET_SIZE = 470;
    public static final int HEADER_SIZE = 10;

    public static final String NUTS_SERVER_ADDRESS = "alimgokkaya.com";

    public static final int NUTS_SERVER_PORT = 5077;

    public static int checksum(byte[] outBuffer, int offset, int length) {
        int crc = outBuffer[length-1];
        for (int i=offset+1;i<length;i+=2) {
            crc= crc | outBuffer[i] | (outBuffer[i-1]<<8);
        }
        return crc;
    }
}
