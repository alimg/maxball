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
    private DatagramSocket mSocket;
    private SenderThread senderThread;

    public NutsHostModeClient(NutsClientListener listener) {
        this.listener = listener;
    }
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(29071);
            socket.setSoTimeout(10000);
            mSocket = socket;
            senderThread = new SenderThread(socket);
            senderThread.start();

            DatagramPacket requestPacket = new DatagramPacket(new byte[1024], 1024);
            DatagramPacket responsePacket = new DatagramPacket(new byte[1024], 1024);
            DatagramPacket pingPacket = new DatagramPacket(new byte[1024], 1024);

            pingPacket.setAddress(InetAddress.getByName(NutsConstants.NUTS_SERVER_IP));
            pingPacket.setPort(NutsConstants.NUTS_SERVER_PORT);
            pingPacket.setData(NutsMessage.serialize(new NutsMessage("ping", null, 0)));

            requestPacket.setAddress(InetAddress.getByName(NutsConstants.NUTS_SERVER_IP));
            requestPacket.setPort(NutsConstants.NUTS_SERVER_PORT);
            requestPacket.setData(NutsMessage.serialize(new NutsMessage("register me", null, 0)));
            socket.send(requestPacket);

            boolean connected = false;
            NutsMessage response = null;
            while (!connected) {
                socket.receive(responsePacket);
                response = NutsMessage.deserialize(responsePacket.getData());
                System.out.println("Response form nuts " +
                        responsePacket.getAddress() + ": " + responsePacket.getPort() +
                        " <" + response.message + ">");
                if(response.message.equals("you are")) {
                    connected=true;
                }
            }
            listener.onConnected(response.address, response.port);

            while (running) {
                try {
                    socket.receive(responsePacket);
                    response = NutsMessage.deserialize(responsePacket.getData());

                    System.out.println("(Server) Response from " +
                            responsePacket.getAddress() + ": " + responsePacket.getPort() +
                            " <" + response.message + ">");

                    if (response.message.equals("incoming connection")) {
                        System.out.println("Sending hello "+response.address.toString()+":"+response.port);
                        requestPacket.setAddress(InetAddress.getByAddress(response.address.getAddress()));
                        requestPacket.setPort(response.port);
                        requestPacket.setData(NutsMessage.serialize(new NutsMessage("hello", null, 0)));
                        socket.send(requestPacket);
                        socket.send(requestPacket);
                    } else if (response.message.equals("hello")) {
                        requestPacket.setAddress(responsePacket.getAddress());
                        requestPacket.setPort(responsePacket.getPort());
                        requestPacket.setData(NutsMessage.serialize(new NutsMessage("i hear you", null, 0)));
                        socket.send(requestPacket);
                    } else if (response.message.equals("pong")){
                        System.out.println("Ping to nuts: "+(System.currentTimeMillis()-lastPingSent));
                    } else {
                        listener.onResponse(response, new NetAddress(responsePacket.getAddress(), responsePacket.getPort()));
                    }
                    if (System.currentTimeMillis()-lastPingSent > 10000) {
                        socket.send(pingPacket);
                        lastPingSent = System.currentTimeMillis();
                    }
                } catch (SocketTimeoutException e) {
                    //listener.onDisconnected();
                    // send ping
                    socket.send(pingPacket);
                    lastPingSent = System.currentTimeMillis();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void stopThread() {
        running = false;
    }

    public void sendMessage(NetAddress address, NutsMessage message) {
        senderThread.send(address, message);
    }
}
