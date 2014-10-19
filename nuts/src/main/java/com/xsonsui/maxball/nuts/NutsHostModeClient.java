package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class NutsHostModeClient extends Thread{
    private final NutsClientListener listener;
    private boolean running = true;
    private long lastPingSent;
    private SenderThread senderThread;
    private InetAddress nutsServerIp;
    private NetAddress nutsServerAddress;
    private ReceiverThread receiverThread;
    private DatagramSocket socket;

    public NutsHostModeClient(NutsClientListener listener) {
        this.listener = listener;
    }
    @Override
    public void run() {
        setName("HostThread");
        try {
            socket = new DatagramSocket(29071);
            socket.setSoTimeout(10000);
            senderThread = new SenderThread(socket);
            senderThread.start();
            receiverThread = new ReceiverThread(socket);
            receiverThread.start();

            nutsServerIp = InetAddress.getByName(NutsConstants.NUTS_SERVER_IP);
            nutsServerAddress = new NetAddress(nutsServerIp, NutsConstants.NUTS_SERVER_PORT);

            senderThread.send(new NetAddress(nutsServerIp, NutsConstants.NUTS_SERVER_PORT),
                    new NutsMessage("register me", null, 0));

            boolean connected = false;
            NutsMessage response = null;
            while (!connected) {
                response = receiverThread.receive(30);
                System.out.println("Response from nuts " +
                        response.srcAddress + ": " + response.srcPort +
                        " <" + response.message + ">");
                if(response.message.equals("you are")) {
                    connected=true;
                }
            }
            listener.onConnected(response.address, response.port);

            while (running) {
                try {
                    response = receiverThread.receive(10);
/*
                    System.out.println("(Server) Response from " +
                            responsePacket.getAddress() + ": " + responsePacket.getPort() +
                            " <" + response.message + ">");*/


                    if (response.message.equals("incoming connection")) {
                        System.out.println("Sending hello " + response.address.toString() + ":" + response.port);
                        senderThread.send(new NetAddress(response.address,
                                response.port), new NutsMessage("hello", null, 0));
                    } else if (response.message.equals("hello")) {
                        senderThread.send(new NetAddress(response.srcAddress,
                                response.srcPort), new NutsMessage("i hear you", null, 0));
                    } else if (response.message.equals("pong")) {
                        System.out.println("Ping to nuts: " + (System.currentTimeMillis() - lastPingSent));
                    } else if (response.srcAddress.equals(nutsServerIp)) {
                        listener.onResponse(response, new NetAddress(response.address, response.port), false);
                    } else {
                        listener.onResponse(response, new NetAddress(response.srcAddress, response.srcPort), true);
                    }
                    if (System.currentTimeMillis()-lastPingSent > 10000) {
                        senderThread.send(new NetAddress(nutsServerIp, NutsConstants.NUTS_SERVER_PORT),
                                new NutsMessage("ping", null, 0));
                        lastPingSent = System.currentTimeMillis();
                    }
                } catch (SocketTimeoutException e) {
                    // send ping
                    senderThread.send(new NetAddress(nutsServerIp, NutsConstants.NUTS_SERVER_PORT),
                            new NutsMessage("ping", null, 0));

                    lastPingSent = System.currentTimeMillis();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopThread() {
        running = false;
        senderThread.stopThread();
        receiverThread.stopThread();
        socket.close();
    }

    public void sendMessage(NetAddress address, NutsMessage message, boolean peer2peer) {
        if (peer2peer)
            senderThread.send(address, message);
        else senderThread.send(nutsServerAddress, new NutsMessage("redirect", address.srcAddress, address.srcPort, message));
    }
}
