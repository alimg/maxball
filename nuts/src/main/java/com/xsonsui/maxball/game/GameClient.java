package com.xsonsui.maxball.game;

import com.xsonsui.maxball.model.GameUpdate;
import com.xsonsui.maxball.model.Input;
import com.xsonsui.maxball.model.JoinRequest;
import com.xsonsui.maxball.nuts.NetAddress;
import com.xsonsui.maxball.nuts.NutsClientListener;
import com.xsonsui.maxball.nuts.NutsMessage;
import com.xsonsui.maxball.nuts.NutsNormalClient;

import java.net.InetAddress;

/**
 * Created by alim on 9/14/14.
 */
public class GameClient implements NutsClientListener, GameInputListener {
    private final GameThread gameThread;
    private final String playerName;
    private final String playerAvatar;
    private final boolean isLocalGame;
    private NutsNormalClient client;
    private Input mInput = new Input();

    public GameClient(GameThread gameThread, String playerName, String playerAvatar, boolean isLocalGame) {
        this.gameThread = gameThread;
        this.playerName = playerName;
        this.playerAvatar = playerAvatar;
        this.isLocalGame = isLocalGame;
    }

    @Override
    public void onConnected(InetAddress publicAddress, int publicPort) {
        NutsMessage message = new NutsMessage("join", null, 0);
        JoinRequest req = new JoinRequest();
        req.name = playerName;
        req.avatar = playerAvatar;
        message.data = req;

        client.sendMessage(message);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onResponse(NutsMessage response, NetAddress address, boolean p2p) {
        if(response.message.equals("update") && !isLocalGame){
            GameUpdate update = (GameUpdate) response.data;
            gameThread.updateGame(update);
        }

    }

    @Override
    public void inputMove(float x, float y) {
        NutsMessage message = new NutsMessage("input", null, 0);
        mInput.x=x;
        mInput.y=y;
        message.data = mInput;
        client.sendMessage(message);
    }

    @Override
    public void inputKick(float p) {
        NutsMessage message = new NutsMessage("input", null, 0);
        mInput.kick=p;
        message.data = mInput;
        client.sendMessage(message);
    }

    public void setClient(NutsNormalClient client) {
        this.client = client;
    }
}
