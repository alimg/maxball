package com.xsonsui.maxball.game;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xsonsui.maxball.GameActivity;
import com.xsonsui.maxball.GameView;
import com.xsonsui.maxball.model.Game;
import com.xsonsui.maxball.model.GameUpdate;
import com.xsonsui.maxball.model.Input;
import com.xsonsui.maxball.model.JoinRequest;
import com.xsonsui.maxball.model.Player;
import com.xsonsui.maxball.nuts.NetAddress;
import com.xsonsui.maxball.nuts.NutsClientListener;
import com.xsonsui.maxball.nuts.NutsHostModeClient;
import com.xsonsui.maxball.nuts.NutsMessage;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alim on 9/14/14.
 */
public class GameHost implements NutsClientListener, GameThread.GameUpdateListener {
    private static final String TAG = "GameHost";
    private final GameActivity gameActivity;
    private final GameThread gameThread;
    private NutsHostModeClient client;
    private Map<NetAddress, String> playerMap = new HashMap<NetAddress, String>();

    public GameHost(GameActivity gameActivity, GameThread gameThread) {
        this.gameActivity = gameActivity;
        this.gameThread = gameThread;
        gameThread.setGameUpdateListener(this);
        gameThread.initGame();
    }

    public void setClient(NutsHostModeClient client) {
        this.client = client;
    }

    @Override
    public void onConnected(InetAddress publicAddress, int publicPort) {
        gameActivity.connectToLocalHost(publicAddress, publicPort);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onResponse(final NutsMessage response, final NetAddress address) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (response.message.equals("join")) {
                    JoinRequest joinRequest = (JoinRequest) response.data;
                    if(!playerMap.containsKey(address)) {
                        Player player = new Player(joinRequest.name);
                        player.avatar = joinRequest.avatar;
                        player.team = "b";
                        player.position.set(-100,0);
                        gameThread.addPlayer(player);
                        playerMap.put(address, player.name);
                        Log.d(TAG, "Player joined "+address.toString());
                    }
                } else if (response.message.equals("input")) {
                    Input input = (Input) response.data;
                    gameThread.input(playerMap.get(address), input);
                }
            }
        });
    }

    @Override
    public void updated(Game game) {
        synchronized (game) {
            GameUpdate update = new GameUpdate(game);
            NutsMessage message = new NutsMessage("update", null, 0);
            message.data = update;

            for (NetAddress addr : playerMap.keySet()) {
                client.sendMessage(addr, message);
            }
        }
    }
}
