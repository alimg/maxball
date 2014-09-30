package com.xsonsui.maxball;

import android.os.Handler;
import android.os.Looper;

import com.xsonsui.maxball.model.Lobby;
import com.xsonsui.maxball.nuts.NetAddress;
import com.xsonsui.maxball.nuts.NutsConstants;
import com.xsonsui.maxball.nuts.NutsMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class LobbyManager {
    private static final String SERVICE_URL = "54.86.106.48:8081/lobby";
    private static final String STUN_URL = "54.86.106.48:5000";
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
                    DatagramPacket packet = new DatagramPacket(new byte[10240],10240);
                    DatagramPacket outPacket = new DatagramPacket(new byte[1024],1024);
                    outPacket.setAddress(InetAddress.getByName(NutsConstants.NUTS_SERVER_IP));
                    outPacket.setPort(NutsConstants.NUTS_SERVER_PORT);
                    outPacket.setData(NutsMessage.serialize(new NutsMessage("list servers", null, 0)));
                    socket.send(outPacket);

                    socket.receive(packet);
                    final NutsMessage response = NutsMessage.deserialize(packet.getData());
                    if(response.message.equals("server list")) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess((List<NetAddress>) response.data);
                            }
                        });
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
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
