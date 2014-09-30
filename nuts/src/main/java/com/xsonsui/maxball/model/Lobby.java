package com.xsonsui.maxball.model;


import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Lobby implements Serializable {
    public String name;
    public InetAddress ip;
    public int port;
    public List<Player> players = new ArrayList<Player>();
    public String maxPlayers;

    public Lobby(InetAddress srcAddress, int srcPort) {
        ip = srcAddress;
        this.port = srcPort;
        this.name = "unknown";
    }

    @Override
    public String toString() {
        return String.format("%s @%s:%d (%s/%s)",name, ip, port, players.size(), maxPlayers);
    }
}
