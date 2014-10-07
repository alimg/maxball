package com.xsonsui.maxball.game;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                player.team = "b";
                player.position.set(-100, 0);
                gameThread.addPlayer(player);
                playerMap.put(address, player.name);
                if(p2p)
                    playerP2PMap.add(address);
                System.out.println(TAG+ ": Player joined "+address.toString() + " P2P: " + p2p);
            }
        } else if (response.message.equals("input")) {
            Input input = (Input) response.data;
            gameThread.input(playerMap.get(address), input);
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
    }

    public interface GameConnectionListener {
        public void connectToLocalHost(InetAddress publicAddress, int publicPort);
    }
}
