package com.xsonsui.maxball.nuts;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class NutsServer extends Thread{

    private boolean running = true;

    public NutsServer(){
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(null);
            socket.bind(new InetSocketAddress(NutsConstants.NUTS_SERVER_PORT));

            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            DatagramPacket outPacket = new DatagramPacket(new byte[1024], 1024);
            while(running) {
                try {
                    socket.receive(packet);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    byte[] data = packet.getData();

                    try {
                        NutsMessage reqData = NutsMessage.deserialize(data);

                        System.out.println("Message from " +
                                packet.getAddress().toString() + ": " + packet.getPort() +
                                " <" + reqData.message + ">");

                        if (reqData.message.equals("register me")) {
                            outPacket.setAddress(address);
                            outPacket.setPort(port);
                            outPacket.setData(NutsMessage.serialize(new NutsMessage("you are", address, port)));
                            socket.send(outPacket);
                        } else if (reqData.message.equals("connect me")){
                            System.out.println("connect me: "+reqData.address.toString()+":"+reqData.port);
                            outPacket.setAddress(InetAddress.getByAddress(reqData.address.getAddress()));
                            outPacket.setPort(reqData.port);
                            outPacket.setData(NutsMessage.serialize(new NutsMessage("incoming connection", address, port)));
                            socket.send(outPacket);
                        } else if (reqData.message.equals("ping")) {
                            outPacket.setAddress(address);
                            outPacket.setPort(port);
                            outPacket.setData(NutsMessage.serialize(new NutsMessage("pong", address, port)));
                            socket.send(outPacket);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
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
