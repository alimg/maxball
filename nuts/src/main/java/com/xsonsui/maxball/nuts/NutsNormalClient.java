package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
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
    private SenderThread senderThread;
    private boolean p2pAvailable;
    private int droppedPackets;

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
            senderThread = new SenderThread(socket);
            senderThread.start();
            ReceiverThread receiverThread = new ReceiverThread(socket);
            receiverThread.start();
            int retryCount = 0;
            p2pAvailable = false;
            while (!connected) {
                try {
                    if(retryCount%2==0) {
                        senderThread.send(new NetAddress(
                                InetAddress.getByName(NutsConstants.NUTS_SERVER_IP),
                                NutsConstants.NUTS_SERVER_PORT),
                                new NutsMessage("connect me", serverIp, serverPort));
                    }

                    senderThread.send(new NetAddress(serverIp, serverPort),
                            new NutsMessage("hello", null, 0));

                    NutsMessage response = receiverThread.receive(3);

                    System.out.println("(Client) Response from " +
                            response.srcAddress + ": " + response.srcPort +
                            " <" + response.message + ">");

                    if (response.message.equals("hello")) {
                        senderThread.send(new NetAddress(response.srcAddress, response.srcPort),
                                new NutsMessage("hello", null, 0));

                        response = receiverThread.receive(3);

                        System.out.println("Response from " +
                                response.srcAddress + ": " + response.srcPort +
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
                        System.out.println("Unable punch UDP hole");
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

            int prevSeqNo=0;
            while (connected) {
                try {
                    NutsMessage response = receiverThread.receive(3);
                    if(response.sequenceNo>prevSeqNo || response.sequenceNo<10) {
                        listener.onResponse(response, new NetAddress(response.srcAddress, response.srcPort), p2pAvailable);
                        prevSeqNo = response.sequenceNo;
                    } else {
                        droppedPackets++;
                    }
                } catch (SocketTimeoutException e ){

                }
            }

        } catch (SocketException e) {
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
