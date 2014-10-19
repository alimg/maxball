package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static com.xsonsui.maxball.nuts.NutsConstants.MAX_PACKET_SIZE;
import static com.xsonsui.maxball.nuts.NutsConstants.HEADER_SIZE;

public class SenderThread extends Thread {
    public static final int MAX_SEQUENCE_NO = 1 << 29;
    private final BlockingQueue<MessageEntry> mQueue = new LinkedBlockingQueue<MessageEntry>();
    private final DatagramSocket mSocket;
    private boolean running = true;
    private int seqNo;

    public SenderThread(DatagramSocket socket) {
        mSocket = socket;
    }

    @Override
    public void run() {
        byte[] outBuffer = new byte[MAX_PACKET_SIZE+HEADER_SIZE];
        DatagramPacket packet = new DatagramPacket(outBuffer, outBuffer.length);
        seqNo=0;
        while(running) {
            MessageEntry mp = null;
            try {
                mp = mQueue.take();
                seqNo = (seqNo+1)%MAX_SEQUENCE_NO;

                packet.setAddress(mp.address.srcAddress);
                packet.setPort(mp.address.srcPort);
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(NutsMessage.serialize(mp.message));
                    buffer.position(0);
                    int totalPacketSize = buffer.capacity();
                    int numPackets = totalPacketSize/MAX_PACKET_SIZE + (totalPacketSize%MAX_PACKET_SIZE>0?1:0);

                    for (int i=0; i<numPackets; i++) {
                        int length = Math.min(MAX_PACKET_SIZE, buffer.capacity() - buffer.position());
                        buffer.get(outBuffer, HEADER_SIZE, length);
                        NutsPacket.computePacketHeader(outBuffer, seqNo, length, numPackets, i);
                        packet.setLength(length+HEADER_SIZE);
                        mSocket.send(packet);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void send(NetAddress address, NutsMessage message) {
        try {
            mQueue.put(new MessageEntry(address, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        running = false;
    }

    private class MessageEntry {
        public NetAddress address;
        public NutsMessage message;
        public MessageEntry(NetAddress address, NutsMessage message){
            this.address = address;
            this.message = message;
        }
    }
}