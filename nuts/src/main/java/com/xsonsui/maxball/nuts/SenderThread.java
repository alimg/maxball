package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SenderThread extends Thread {
    private final BlockingQueue<MessagePair> mQueue = new LinkedBlockingQueue<MessagePair>();
    private final DatagramSocket mSocket;
    private boolean running = true;

    public SenderThread(DatagramSocket socket) {
        mSocket = socket;
    }

    @Override
    public void run() {
        while(running) {
            MessagePair mp = null;
            try {
                mp = mQueue.take();

                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                packet.setAddress(mp.address.srcAddress);
                packet.setPort(mp.address.srcPort);
                try {
                    packet.setData(NutsMessage.serialize(mp.message));
                    if(packet.getData().length>1400){
                        System.err.println("Packet size is too high: " + packet.getData().length);
                    }
                    mSocket.send(packet);
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
            mQueue.put(new MessagePair(address, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {

    }

    private class MessagePair{
        public NetAddress address;
        public NutsMessage message;
        public MessagePair(NetAddress address, NutsMessage message){
            this.address = address;
            this.message = message;
        }
    }
}