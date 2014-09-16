package com.xsonsui.maxball.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lobby implements Serializable {
    public String name;
    public String ip;
    public int port;
    public List<Player> players = new ArrayList<Player>();
    public String maxPlayers;

    @Override
    public String toString() {
        return String.format("%s @%s:%d (%s/%s)",name, ip, port, players.size(), maxPlayers);
    }
}
