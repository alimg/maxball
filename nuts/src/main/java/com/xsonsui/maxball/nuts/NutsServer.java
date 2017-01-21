package com.xsonsui.maxball.nuts;

import com.xsonsui.maxball.nuts.model.NetAddress;
import com.xsonsui.maxball.nuts.model.NutsMessage;
import com.xsonsui.maxball.nuts.server.ServerStats;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class NutsServer extends Thread{

    private static final long HOSTCLEANTASK_INTERVAL = 5000;
    private static final long REMOVE_HOST_AFTER = 15000;
    private boolean running = true;
    private ConcurrentHashMap<NetAddress, Long> mHosts = new ConcurrentHashMap<NetAddress, Long>();
    private ServerStats mStats;
    private Timer mTimer = new Timer();

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

            mStats = new ServerStats();

            mTimer.scheduleAtFixedRate(new CleanHostsTask(),
                    HOSTCLEANTASK_INTERVAL, HOSTCLEANTASK_INTERVAL);

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
                            NetAddress clientAddr = new NetAddress(address, port);
                            senderThread.send(clientAddr,
                                    new NutsMessage("pong", null, 0));

                            if (mHosts.containsKey(clientAddr)) {
                                mHosts.put(clientAddr, System.currentTimeMillis());
                            }
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
                            mStats.redirectedPackets++;
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

    private class CleanHostsTask extends TimerTask {
        @Override
        public void run() {
            Iterator<Map.Entry<NetAddress, Long>> it = mHosts.entrySet().iterator();
            long time = System.currentTimeMillis();
            while (it.hasNext()) {
                Map.Entry<NetAddress, Long> entry = it.next();
                if (entry.getValue()-time > REMOVE_HOST_AFTER) {
                    it.remove();
                }
            }
        }
    }
}
