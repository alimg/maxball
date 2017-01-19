package com.xsonsui.maxball.nuts;

import com.xsonsui.maxball.nuts.model.NetAddress;
import com.xsonsui.maxball.nuts.model.NutsMessage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class NutsServer extends Thread{

    private boolean running = true;
    private HashMap<NetAddress, Long> mHosts = new HashMap<NetAddress, Long>();

    public NutsServer(){
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(NutsConstants.NUTS_SERVER_PORT);
            //socket.bind(new InetSocketAddress(NutsConstants.NUTS_SERVER_PORT));
            SenderThread senderThread = new SenderThread(socket);
            senderThread.start();
            ReceiverThread receiverThread = new ReceiverThread(socket);
            receiverThread.start();

            while(running) {
                try {
                    NutsMessage request = receiverThread.receive(3);

                    InetAddress address = request.srcAddress;
                    int port = request.srcPort;

                    try {
                        /*
                        System.out.println("Message from " +
                                address.toString() + ": " + port +
                                " <" + request.message + ">");*/

                        if (request.message.equals("register me")) {
                            System.out.println("register: " + address + ":" + port);
                            senderThread.send(new NetAddress(address, port),
                                    new NutsMessage("you are", address, port));
                            mHosts.put(new NetAddress(address, port), System.currentTimeMillis());
                        } else if (request.message.equals("connect me")) {
                            System.out.println("connect me: " + request.srcAddress.toString() +
                                    ":" + request.srcPort + " to " + request.address.toString()+":"+request.port);
                            senderThread.send(new NetAddress(request.address, request.port),
                                    new NutsMessage("incoming connection", address, port));
                        } else if (request.message.equals("ping")) {
                            senderThread.send(new NetAddress(address, port),
                                    new NutsMessage("pong", address, port));
                        } else if (request.message.equals("list servers")) {
                            senderThread.send(new NetAddress(address, port),
                                    new NutsMessage("server list", address, port,
                                            new ArrayList<NetAddress>(mHosts.keySet())));
                        } else if (request.message.equals("redirect")) {
                            NutsMessage message = ((NutsMessage) request.data);
                            message.address = address;
                            message.port = port;
                            senderThread.send(new NetAddress(request.address, request.port),
                                    message);
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                } catch (SocketTimeoutException e) {

                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stopServer(){
        running = false;
    }
}
