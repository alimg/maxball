package com.xsonsui.maxball;

import com.xsonsui.maxball.model.Lobby;
import com.xsonsui.maxball.model.Player;

import java.util.List;

public class LobbyManager {
    private static final String SERVICE_URL = "54.86.106.48:8081/lobby";
    private static final String STUN_URL = "54.86.106.48:5000";
    public interface Listener<T> {
        public void onSuccess(T result);
        public void onFailed();
    }
    public void listLobbies(Listener<List<Lobby>> listener) {

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
