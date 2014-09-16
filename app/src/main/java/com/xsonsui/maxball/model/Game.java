package com.xsonsui.maxball.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alim on 9/12/14.
 */
public class Game implements Serializable {
    public static final float ARENA_HEIGHT_2 = 150;
    public static final float ARENA_WIDTH_2 = 300;
    public Map<String, Player> players = new HashMap<String, Player>();
    public Ball ball;

    public int gameState;

    public int scoreRed;
    public int scoreBlue;
    private Vector2f vecLeft = new Vector2f(-1, 0);
    private Vector2f vecDown = new Vector2f(0, -1);

    public void step(float dt) {
        for (Player p : players.values()) {
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

        for (Player p : players.values()) {
            p.applyForces(dt);
            p.position.add(p.speed, dt);
            p.speed.multiply(0.99f);
        }
        ball.applyForces(dt);
        ball.position.add(ball.speed, dt);
        ball.speed.multiply(0.99f);

    }

    private void collideWalls(Ball ball) {
        if (ball.position.x+ball.radius > ARENA_WIDTH_2) {
           ball.addForceRelative(vecLeft, ball.position.x + ball.radius - ARENA_WIDTH_2);
        }
        if (ball.position.x-ball.radius < -ARENA_WIDTH_2) {
            ball.addForceRelative(vecLeft, ball.position.x - ball.radius + ARENA_WIDTH_2);
        }

        if (ball.position.y+ball.radius > ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y + ball.radius - ARENA_HEIGHT_2);
        }
        if (ball.position.y-ball.radius < -ARENA_HEIGHT_2) {
            ball.addForceRelative(vecDown, ball.position.y - ball.radius + ARENA_HEIGHT_2);
        }

    }

    private void collideBalls(Ball p1, Ball p2) {
        float d = p1.position.distance(p2.position);
        if (d <= p1.radius+p2.radius) {
            p1.addForce(p2.position, d - p2.radius);
            p2.addForce(p1.position, d - p1.radius);
        }
    }

    public void synchronize(Game update) {

    }

    public void init() {
        ball = new Ball();
    }
}
