package com.xsonsui.maxball.model;

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
    public static final float MAX_PLAYER_FORCE = 300;
    private static final float KICK_FORCE = 2f;
    public Map<String, Player> players = new HashMap<String, Player>();
    public Map<String, Player> waitingPlayers= new HashMap<String, Player>();

    public Ball ball = new Ball();

    public int gameState;

    public int scoreRed;
    public int scoreBlue;
    private Vector2f vecLeft = new Vector2f(-1, 0);
    private Vector2f vecDown = new Vector2f(0, -1);

    public void evaluate(float dt, Game delta) {
        ball.position.add(delta.ball.speed, dt);
        ball.speed.add(delta.ball.force, dt/ball.mass);
        for (Player p : players.values()) {
            Player p2 = delta.players.get(p.name);
            p.position.add(p2.speed, dt);
            p.speed.add(p2.force, dt/p.mass);
        }


        for (Player p1 : players.values()) {
            for (Player p2 : players.values()) {
                if (p1 == p2)
                    break;
                collideBalls(p1, p2, 0.00f);
            }
            collideBalls(p1, ball, p1.input.kick);
            collideWalls(p1);
        }
        collideWalls(ball);
        final Vector2f f = new Vector2f();
        for (Player p : players.values()) {
            f.set(p.input.x, p.input.y);
            float l = f.length();
            if (l>MAX_PLAYER_FORCE) {
                f.normalize();
                f.multiply(MAX_PLAYER_FORCE);
            }
            p.force.add(f);
            f.set(p.speed);
            p.force.add(f, -0.75f);
            f.multiply(f.length() * -0.0625f);
            p.force.add(f);
        }
        f.set(ball.speed);
        f.multiply(f.length() * -0.00001f);
        ball.force.add(f);
        ball.force.add(ball.speed, -0.1f);
    }

    public void checkCollisions() {
        collideWalls(ball);
    }

    private void collideWalls(Ball ball) {
        if (ball.position.x+ball.radius > ARENA_WIDTH_2) {
            ball.addForceRelative(vecLeft, ball.position.x + ball.radius - ARENA_WIDTH_2);
            ball.addForceRelative(vecLeft, -ball.WALL_COLLISION_DAMP * vecLeft.dot(ball.speed));
            if (ball==this.ball)
                if (ball.position.y>-GOAL_AREA_SIZE && ball.position.y<GOAL_AREA_SIZE) {
                    goalRight();
                }
        }
        if (ball.position.x-ball.radius < -ARENA_WIDTH_2) {
            ball.addForceRelative(vecLeft, ball.position.x - ball.radius + ARENA_WIDTH_2);
            ball.addForceRelative(vecLeft, -ball.WALL_COLLISION_DAMP * vecLeft.dot(ball.speed));
            if (ball==this.ball)
                if (ball.position.y>-GOAL_AREA_SIZE && ball.position.y<GOAL_AREA_SIZE) {
                    goalLeft();
                }
        }

        if (ball.position.y+ball.radius > ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y + ball.radius - ARENA_HEIGHT_2);
            ball.addForceRelative(vecDown, -ball.WALL_COLLISION_DAMP * vecDown.dot(ball.speed));
        }
        if (ball.position.y-ball.radius < -ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y - ball.radius + ARENA_HEIGHT_2);
            ball.addForceRelative(vecDown, -ball.WALL_COLLISION_DAMP * vecDown.dot(ball.speed));
        }

    }

    private void collideBalls(Ball p1, Ball p2, float kick) {
        final Vector2f f = new Vector2f();
        final Vector2f n = new Vector2f();
        float d = p1.position.distance(p2.position);
        if (d <= p1.radius+p2.radius) {
            p1.addForce(p2.position, d - p2.radius);
            p2.addForce(p1.position, d - p1.radius);
            n.set(p2.position.x-p1.position.x,p2.position.y-p1.position.y);
            n.normalize();
            f.set(p2.speed.x-p1.speed.x,p2.speed.y-p1.speed.y);
            if(kick>0.1f) {
                p2.addForceRelative(n, KICK_FORCE);
            }
            else {
                float cdf = n.dot(f);
                p2.addForceRelative(n, -cdf * p2.COLLISION_DAMP);
                //if (cdf > 0)
                    //p2.addForceRelative(n, -cdf * p2.COLLISION_DAMP);
                //else p1.addForceRelative(n, -cdf * p1.COLLISION_DAMP);
            }
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

    public void writeP(Game game) {
        game.ball.position.set(ball.position);
        game.ball.speed.set(ball.speed);
        game.ball.force.set(0, 0);
        for(Map.Entry<String, Player> p: players.entrySet()) {
            if (!game.players.containsKey(p.getKey())) {
                game.players.put(p.getKey(),new Player(p.getValue()));
            }
            Player p1 = p.getValue();
            Player p2 = game.players.get(p.getKey());
            p2.position.set(p1.position);
            p2.input.set(p1.input);
            p2.speed.set(p1.speed);
            p2.force.set(0, 0);
        }
    }

    public void writeD(Game game) {
        game.ball.speed.set(0, 0);
        game.ball.force.set(0, 0);
        for(Map.Entry<String, Player> p: players.entrySet()) {
            Player p1 = p.getValue();
            Player p2 = game.players.get(p.getKey());
            p2.speed.set(0, 0);
            p2.force.set(0, 0);
        }
    }

    public void integrate(float dt, Game[] states) {
        integrate(dt, ball, states[0].ball, states[1].ball, states[2].ball, states[3].ball);
        for(Map.Entry<String, Player> p: players.entrySet()) {
            Ball p1 = p.getValue();
            Ball p2 = states[0].players.get(p.getKey());
            Ball p3 = states[1].players.get(p.getKey());
            Ball p4 = states[2].players.get(p.getKey());
            Ball p5 = states[3].players.get(p.getKey());
            integrate(dt, p1, p2, p3, p4, p5);
        }
    }

    private void integrate(float dt, Ball p1, Ball p2, Ball p3, Ball p4, Ball p5) {
        p1.speed.set(p3.speed);
        p1.speed.add(p4.speed);
        p1.speed.multiply(2);
        p1.speed.add(p2.speed);
        p1.speed.add(p5.speed);
        p1.speed.multiply(1f/6f);

        p1.force.set(p3.force);
        p1.force.add(p4.force);
        p1.force.multiply(2);
        p1.force.add(p2.force);
        p1.force.add(p5.force);
        p1.force.multiply(1f/6f);

        p1.position.add(p1.speed, dt);
        p1.speed.add(p1.force, dt/p1.mass);
    }
}
