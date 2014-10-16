package com.xsonsui.maxball.nuts;

import com.xsonsui.maxball.game.GameHost;
import com.xsonsui.maxball.game.GameThread;
import com.xsonsui.maxball.game.GameViewInterface;
import com.xsonsui.maxball.model.Game;

import java.net.InetAddress;

/**
 * Created by alim on 9/11/14.
 */
public class HostModeClientMain {

    private static Game game;
    private static GameThread gameThread;
    private static GameHost host;

    public static void main(String args[]) {
        game = new Game();
        gameThread = new GameThread(game, new GameViewInterface() {
            @Override
            public void draw(Game mGame) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
        host = new GameHost(new GameHost.GameConnectionListener() {
            @Override
            public void connectToLocalHost(InetAddress publicAddress, int publicPort) {

            }
        }, gameThread);
        NutsHostModeClient client = new NutsHostModeClient(host);
        host.setClient(client);
        client.start();
    }
}
