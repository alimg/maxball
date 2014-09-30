package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class NutsNormalClient extends Thread{

    private final InetAddress peerIp;
    private final int peerPort;
    private InetAddress serverIp;
    private int serverPort;
    private final NutsClientListener listener;
    private boolean connected;
    private DatagramSocket mSocket;
    private SenderThread senderThread;
    private boolean p2pAvailable;

    public NutsNormalClient(InetAddress serverIp, int serverPort, NutsClientListener listener) {
        try {
            this.serverIp = InetAddress.getByName(serverIp.toString().split("/")[1]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.serverPort = serverPort;
        this.listener = listener;

        this.peerIp = serverIp;
        this.peerPort = serverPort;
        System.out.println("client created "+serverIp.toString()+":"+serverPort);
    }

    @Override
    public void run() {
        setName("NormalClientThread");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(null);
            socket.setSoTimeout(3000);
            mSocket = socket;
            senderThread = new SenderThread(socket);
            senderThread.start();

            DatagramPacket requestPacket = new DatagramPacket(new byte[1024], 1024);
            DatagramPacket responsePacket;
            ReceiverThread receiverThread = new ReceiverThread(mSocket);
            receiverThread.start();
            int retryCount = 0;
            p2pAvailable = false;
            while (!connected) {
                try {
                    if(retryCount%2==0) {
                        requestPacket.setAddress(InetAddress.getByName(NutsConstants.NUTS_SERVER_IP));
                        requestPacket.setPort(NutsConstants.NUTS_SERVER_PORT);
                        requestPacket.setData(NutsMessage.serialize(new NutsMessage("connect me", serverIp, serverPort)));
                        System.out.println("len 1 "+requestPacket.getLength());
                        socket.send(requestPacket);
                    }

                    requestPacket.setAddress(serverIp);
                    requestPacket.setPort(serverPort);
                    requestPacket.setData(NutsMessage.serialize(new NutsMessage("hello", null, 0)));
                    System.out.println("len 2 "+requestPacket.getLength());
                    socket.send(requestPacket);


                    responsePacket = receiverThread.receive();
                    NutsMessage response = NutsMessage.deserialize(responsePacket.getData());

                    System.out.println("(Client) Response from " +
                            responsePacket.getAddress() + ": " + responsePacket.getPort() +
                            " <" + response.message + ">");

                    if (response.message.equals("hello")) {

                        requestPacket.setAddress(responsePacket.getAddress());
                        requestPacket.setPort(responsePacket.getPort());
                        requestPacket.setData(NutsMessage.serialize(new NutsMessage("hello", null, 0)));
                        socket.send(requestPacket);

                        responsePacket = receiverThread.receive();
                        response = NutsMessage.deserialize(responsePacket.getData());

                        System.out.println("Response from " +
                                responsePacket.getAddress() + ": " + responsePacket.getPort() +
                                " <" + response.message + ">");
                        if (response.message.equals("i hear you")) {
                            connected = true;
                            p2pAvailable = true;
                            System.out.println("Connection established");
                        } else {
                            retryCount ++;
                        }
                    } else if (response.message.equals("i hear you")) {
                        connected = true;
                        p2pAvailable = true;
                        System.out.println("Connection established");
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Unable to reach server retrying");
                    retryCount ++;
                    if (retryCount >= 3) {
                        p2pAvailable = false;
                        connected = true;
                        System.out.print("Unable punch a UDP hole");
                    }
                }
            }
            if (!p2pAvailable){
                //TODO get a dedicated server for packet redirection from somewhere.
                // We just use the main server for now.
                serverIp = InetAddress.getByName(NutsConstants.NUTS_SERVER_IP);
                serverPort = NutsConstants.NUTS_SERVER_PORT;
            }
            listener.onConnected(null, 0);

            while (connected) {
                try {
                    responsePacket = receiverThread.receive();
                    NutsMessage response = NutsMessage.deserialize(responsePacket.getData());

                    listener.onResponse(response, new NetAddress(responsePacket.getAddress(), responsePacket.getPort()), p2pAvailable);
                } catch (Exception e) {
                    System.err.println("Corrupted packet received");
                    //e.printStackTrace();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.onDisconnected();
            if (socket!=null) {
                socket.close();
            }
        }
    }

    public void sendMessage(NutsMessage message) {
        if(p2pAvailable) {
            senderThread.send(new NetAddress(serverIp, serverPort), message);
        } else {
            senderThread.send(new NetAddress(serverIp, serverPort), new NutsMessage("redirect", peerIp, peerPort, message));
        }
    }
}
