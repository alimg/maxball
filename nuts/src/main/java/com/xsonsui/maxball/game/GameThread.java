package com.xsonsui.maxball.game;

import com.xsonsui.maxball.model.Game;
import com.xsonsui.maxball.model.GameUpdate;
import com.xsonsui.maxball.model.Input;
import com.xsonsui.maxball.model.Player;

public class GameThread extends Thread{

    private static final String TAG = "GameThread";
    private final Game mGame;
    private final Game dState[];
    private final Game zState;
    private final GameViewInterface view;
    private boolean running = true;
    private GameUpdateListener gameUpdateListener;
    private boolean paused = false;
    private Object pauseObj = new Object();

    public GameThread(Game game, GameViewInterface view) {
        mGame = game;
        dState = new Game[4];
        dState[0] = new Game();
        dState[1] = new Game();
        dState[2] = new Game();
        dState[3] = new Game();
        zState = new Game();
        this.view = view;
    }

    @Override
    public void run() {
        double t = 0.0;
        float dt = 0.02f;

        long currentTime = System.currentTimeMillis();
        double accumulator = 0.0;

        int steps;
        int frameCount=0;

        while (running)
        {
            long newTime = System.currentTimeMillis();
            double frameTime = (newTime - currentTime)/1000.0;
            if(currentTime/1000!=newTime/1000) {
                System.out.println("GameThread: fps: " + frameCount + " acc: " + accumulator);
                frameCount=0;
            }
            currentTime = newTime;
            while(paused) {
                synchronized (pauseObj){
                    try {
                        pauseObj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentTime = System.currentTimeMillis();
            }
            frameCount++;

            accumulator += frameTime;

            steps=0;
            while ( accumulator >= dt )
            {
                synchronized (mGame) {
                    mGame.writeP(dState[0]);
                    mGame.writeP(dState[1]);
                    mGame.writeP(dState[2]);
                    mGame.writeP(dState[3]);
                    mGame.writeP(zState);
                    mGame.writeD(zState);
                    dState[0].evaluate(0, zState);
                    //dState[0].writeD(dState[1]);
                    dState[1].evaluate(dt * 0.5f, dState[0]);
                   // dState[1].writeD(dState[2]);
                    dState[2].evaluate(dt * 0.5f, dState[1]);
                  //  dState[2].writeD(dState[3]);
                    dState[3].evaluate(dt, dState[2]);
                    mGame.checkCollisions();
                    mGame.integrate(dt, dState);
                }
                accumulator -= dt;
                t += dt;
                steps++;
            }
            if (steps>0 && gameUpdateListener != null) {
                gameUpdateListener.updated(mGame);
            }

            view.draw(mGame);
        }

    }

    public void stopThread() {
        running = false;
    }

    public void setGameUpdateListener(GameUpdateListener listener) {
        gameUpdateListener = listener;
    }

    public void initGame() {
        mGame.init();
    }

    public void addPlayer(Player player) {
        synchronized (mGame) {
            mGame.addPlayer(player);
        }
    }

    public void input(String playerName, Input input) {
        synchronized (mGame) {
            if(mGame.players.containsKey(playerName))
                mGame.players.get(playerName).input = input;
            else {
                System.out.println(TAG+": player "+playerName+ " not found");
            }
        }
    }

    public void updateGame(GameUpdate update) {
        synchronized (mGame) {
            update.updateGame(mGame);
        }
    }

    public void pauseGame() {
        paused = true;
    }

    public void resumeGame() {
        paused = false;
        synchronized (pauseObj) {
            pauseObj.notifyAll();
        }
    }

    public void playerDisconnected(String name) {
        synchronized (mGame) {
            mGame.playerDisconnected(name);
        }
    }

    public interface GameUpdateListener{
        public void updated(Game game);
    }
}
