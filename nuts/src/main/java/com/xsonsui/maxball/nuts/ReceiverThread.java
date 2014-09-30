package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by alim on 9/19/14.
 */
public class ReceiverThread extends Thread {

    private final DatagramSocket mSocket;
    private boolean running = true;
    private byte[] buffer = new byte[4096];
    private LinkedBlockingQueue<DatagramPacket> mQueue = new LinkedBlockingQueue<DatagramPacket>();

    public ReceiverThread(DatagramSocket socket) {
        mSocket = socket;
    }
    @Override
    public void run() {
        setName("ReceiverThread");
        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                mSocket.receive(packet);
                mQueue.offer(packet);
            } catch (SocketTimeoutException e) {
                interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DatagramPacket receive() throws SocketTimeoutException {
        try {
            DatagramPacket packet = mQueue.poll(3, TimeUnit.SECONDS);
            if (packet == null) {
                throw new SocketTimeoutException("");
            }
            return packet;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new SocketTimeoutException("");
    }

    public void stopThread() {
        running = false;
    }

}
