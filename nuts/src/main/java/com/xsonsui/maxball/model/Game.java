package com.xsonsui.maxball.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alim on 9/12/14.
 */
public class Game {
    public static final int TEAM_RED = 0;
    public static final int TEAM_BLUE = 1;
    public static final int STATE_PRE_GAME = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_GOAL = 2;
    public static final float ARENA_HEIGHT_2 = 150;
    public static final float ARENA_WIDTH_2 = 300;
    public static final float GOAL_AREA_SIZE = 50;
    public static final float MAX_PLAYER_FORCE = 200;
    public Map<String, Player> players = new HashMap<String, Player>();
    public Map<String, Player> waitingPlayers= new HashMap<String, Player>();

    public Ball ball = new Ball();

    public int gameState;

    public int scoreRed;
    public int scoreBlue;
    private Vector2f vecLeft = new Vector2f(-1, 0);
    private Vector2f vecDown = new Vector2f(0, -1);

    public void step(float dt) {
        for (Player p : players.values()) {
            float l = p.force.length();
            if (l>MAX_PLAYER_FORCE) {
                p.force.normalize();
                p.force.multiply(MAX_PLAYER_FORCE);
            }
            p.force.set(p.input.x, p.input.y);
        }
        ball.force.set(0,0);

        for (Player p1 : players.values()) {
            for (Player p2 : players.values()) {
                if (p1 == p2)
                    break;
                collideBalls(p1, p2);
            }
            collideBalls(p1, ball);
            collideWalls(p1);
        }
        collideWalls(ball);

        Vector2f f = new Vector2f();
        for (Player p : players.values()) {
            f.set(p.speed);
            p.force.add(f,-0.75f);
            f.multiply(f.length()*-0.05f);
            p.force.add(f);
            p.applyForces(dt);
            p.position.add(p.speed, dt);
        }
        ball.applyForces(dt);
        ball.position.add(ball.speed, dt);
        ball.speed.multiply(0.96f);

    }

    private void collideWalls(Ball ball) {
        if (ball.position.x+ball.radius > ARENA_WIDTH_2) {
            ball.addForceRelative(vecLeft, ball.position.x + ball.radius - ARENA_WIDTH_2);
            if (ball==this.ball)
                if (ball.position.y>-GOAL_AREA_SIZE && ball.position.y<GOAL_AREA_SIZE) {
                    goalRight();
                }
        }
        if (ball.position.x-ball.radius < -ARENA_WIDTH_2) {
            ball.addForceRelative(vecLeft, ball.position.x - ball.radius + ARENA_WIDTH_2);
            if (ball==this.ball)
                if (ball.position.y>-GOAL_AREA_SIZE && ball.position.y<GOAL_AREA_SIZE) {
                    goalLeft();
                }
        }

        if (ball.position.y+ball.radius > ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y + ball.radius - ARENA_HEIGHT_2);
        }
        if (ball.position.y-ball.radius < -ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y - ball.radius + ARENA_HEIGHT_2);
        }

    }

    private void goalLeft() {
        scoreBlue++;
        beginNewRound();
    }

    private void goalRight() {
        scoreRed++;
        beginNewRound();
    }
    private void beginNewRound() {
        //gameState = STATE_PRE_GAME;
        placeWaitingPlayers();
        resetPositions();
    }

    private void placeWaitingPlayers() {
        int redCount = 0;
        int blueCount = 0;
        for (Player p: players.values()) {
            if (p.team == TEAM_RED){
                redCount++;
            } else blueCount++;
        }
        for(Player p: waitingPlayers.values()) {
            if (redCount<blueCount) {
                p.team = TEAM_RED;
                players.put(p.name, p);
                redCount++;
            } else {
                p.team = TEAM_BLUE;
                players.put(p.name, p);
                blueCount++;
            }
        }
        waitingPlayers.clear();
    }


    private void resetPositions() {
        int redCount = 0;
        int blueCount = 0;
        for (Player p: players.values()) {
            if (p.team == TEAM_RED){
                redCount++;
            } else blueCount++;
        }

        int r=0, b=0;
        for (Player p: players.values()) {
            if(p.team == TEAM_BLUE) {
                p.position.set( ARENA_WIDTH_2/2f, 1.8f*ARENA_HEIGHT_2/(blueCount+1)*(b-blueCount/2.f+0.5f));
                b++;
            } else {
                p.position.set(-ARENA_WIDTH_2/2f, 1.8f*ARENA_HEIGHT_2/(redCount+1)*(r-redCount/2.f+0.5f));
                r++;
            }
            p.speed.set(0, 0);
            p.force.set(0, 0);
        }
        ball.position.set(0, 0);
        ball.speed.set(0, 0);
        ball.force.set(0, 0);
    }


    private void collideBalls(Ball p1, Ball p2) {
        float d = p1.position.distance(p2.position);
        if (d <= p1.radius+p2.radius) {
            p1.addForce(p2.position, d - p2.radius);
            p2.addForce(p1.position, d - p1.radius);
        }
    }

    public void init() {
        ball = new Ball();
    }

    public void addPlayer(Player player) {
        waitingPlayers.put(player.name, player);
        if(players.size()<2) {
            beginNewRound();
        }
    }

    public void playerDisconnected(String name) {
        waitingPlayers.remove(name);
        players.remove(name);
    }
}
