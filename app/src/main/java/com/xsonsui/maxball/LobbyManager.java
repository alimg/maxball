package com.xsonsui.maxball;

import android.os.Handler;
import android.os.Looper;

import com.xsonsui.maxball.model.Lobby;
import com.xsonsui.maxball.nuts.model.NetAddress;
import com.xsonsui.maxball.nuts.NutsConstants;
import com.xsonsui.maxball.nuts.model.NutsMessage;
import com.xsonsui.maxball.nuts.model.NutsPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LobbyManager {
    public interface Listener<T> {
        public void onSuccess(T result);
        public void onFailed();
    }
    public void listLobbies(final Listener<List<NetAddress>> listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(null);
                    DatagramPacket packet = new DatagramPacket(new byte[600],600);
                    DatagramPacket outPacket = new DatagramPacket(new byte[1024],1024);
                    outPacket.setAddress(InetAddress.getByName(NutsConstants.NUTS_SERVER_ADDRESS));
                    outPacket.setPort(NutsConstants.NUTS_SERVER_PORT);
                    byte msgBytes[] = NutsMessage.serialize(new NutsMessage("list servers", null, 0));
                    ByteBuffer b = ByteBuffer.wrap(outPacket.getData());
                    b.position(NutsConstants.HEADER_SIZE);
                    b.put(msgBytes, 0, msgBytes.length);
                    NutsPacket.computePacketHeader(outPacket.getData(), 0, msgBytes.length, 1, 0);
                    outPacket.setData(outPacket.getData());
                    socket.send(outPacket);

                    socket.receive(packet);
                    try {
                        NutsPacket part = NutsPacket.processHeader(packet.getData());
                        ArrayList<NutsPacket> cache = new ArrayList<NutsPacket>(part.parts);
                        cache.add(part);
                        int totalLength = part.length;
                        for (int i=1; i<part.parts; i++) {
                            packet = new DatagramPacket(new byte[600],600);
                            socket.receive(packet);
                            part = NutsPacket.processHeader(packet.getData());
                            cache.add(part);
                            totalLength += part.length;
                        }
                        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
                        for (NutsPacket p: cache) {
                            buffer.position(p.index*NutsConstants.MAX_PACKET_SIZE);
                            buffer.put(p.data, NutsConstants.HEADER_SIZE, p.length);
                        }

                        final NutsMessage response = NutsMessage.deserialize(buffer.array());
                        if(response.message.equals("server list")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onSuccess((List<NetAddress>) response.data);
                                }
                            });
                        }
                    } catch (NutsPacket.PacketIntegrityException e) {
                        e.printStackTrace();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public void joinLobby(Lobby lobby, Listener<String> listener) {

    }

    public void createLobby(String name, String sessionId, Listener<Lobby> listener) {

    }

    public void leaveLobby(Lobby lobby, String sessionId, Listener<String> listener) {

    }

    public void registerPlayer(String player, Listener<String> listener) {

    }

}
