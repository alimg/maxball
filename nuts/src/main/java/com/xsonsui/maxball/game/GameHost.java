package com.xsonsui.maxball.game;

import com.xsonsui.maxball.model.Game;
import com.xsonsui.maxball.model.GameUpdate;
import com.xsonsui.maxball.model.Input;
import com.xsonsui.maxball.model.JoinRequest;
import com.xsonsui.maxball.model.Player;
import com.xsonsui.maxball.nuts.model.NetAddress;
import com.xsonsui.maxball.nuts.NutsClientListener;
import com.xsonsui.maxball.nuts.NutsHostModeClient;
import com.xsonsui.maxball.nuts.model.NutsMessage;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alim on 9/14/14.
 */
public class GameHost implements NutsClientListener, GameThread.GameUpdateListener {
    private static final String TAG = "GameHost";
    private final GameConnectionListener gameActivity;
    private final GameThread gameThread;
    private NutsHostModeClient client;
    private Map<NetAddress, String> playerMap = new HashMap<NetAddress, String>();
    private Set<NetAddress> playerP2PMap = new HashSet<NetAddress>();
    private Map<NetAddress, Long> lastHeard = new ConcurrentHashMap<NetAddress, Long>();
    private long lastCheck;

    public GameHost(GameConnectionListener gameActivity, GameThread gameThread) {
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
        System.out.println(TAG + ": Connected to nuts: " + publicAddress.toString() + ":" + publicPort);
        gameActivity.connectToLocalHost(publicAddress, publicPort);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onResponse(final NutsMessage response, final NetAddress address, final boolean p2p) {
        if (response.message.equals("join")) {
            JoinRequest joinRequest = (JoinRequest) response.data;
            if(!playerMap.containsKey(address)) {
                Player player = new Player(joinRequest.name);
                player.avatar = joinRequest.avatar;
                player.position.set(-100, 0);
                gameThread.addPlayer(player);
                playerMap.put(address, player.name);
                if(p2p)
                    playerP2PMap.add(address);
                lastHeard.put(address, System.currentTimeMillis());
                System.out.println(TAG + ": Player joined "+address.toString() + " P2P: " + p2p);
            }
        } else if (response.message.equals("input")) {
            Input input = (Input) response.data;
            gameThread.input(playerMap.get(address), input);
            lastHeard.put(address, System.currentTimeMillis());
        } else if (response.message.equals("hearth beat")) {
            lastHeard.put(address, System.currentTimeMillis());
        }
    }

    @Override
    public void updated(Game game) {
        synchronized (game) {
            GameUpdate update = new GameUpdate(game);
            NutsMessage message = new NutsMessage("update", null, 0);
            message.data = update;

            for (NetAddress addr : playerMap.keySet()) {
                client.sendMessage(addr, message, playerP2PMap.contains(addr));
            }
        }
        long time = System.currentTimeMillis();
        if (lastCheck+3000 < time) {
            for (Map.Entry<NetAddress, Long> e :lastHeard.entrySet()) {
                if (e.getValue() + 5000 < time ) {
                    playerDisconnected(e.getKey());
                }
            }
        }
    }

    private void playerDisconnected(NetAddress addr) {
        String name = playerMap.get(addr);
        if (name == null)
           return;
        gameThread.playerDisconnected(name);
        playerMap.remove(addr);
        playerP2PMap.remove(addr);
        lastHeard.remove(addr);
    }

    public void closeConnection() {
        client.stopThread();
    }

    public interface GameConnectionListener {
        public void connectToLocalHost(InetAddress publicAddress, int publicPort);
    }
}
