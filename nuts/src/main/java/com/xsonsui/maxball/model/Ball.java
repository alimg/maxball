package com.xsonsui.maxball.model;

import java.io.Serializable;

/**
 * Created by alim on 9/12/14.
 */
public class Ball implements Serializable{
    public transient float COLLISION_DAMP = 0.011f;
    public transient float WALL_COLLISION_DAMP = 0.003f;
    public float radius;
    public Vector2f position;
    public Vector2f speed;

    public transient Vector2f force = new Vector2f();
    public float mass;
    public float k;

    public Ball(){
        position = new Vector2f();
        speed = new Vector2f();
        radius = 12;
        mass = 0.1f;
        k = 1000f;
    }

    public void addForce(Vector2f pivot, float v) {
        Vector2f f = new Vector2f(position.x - pivot.x, position.y - pivot.y);
        f.normalize();
        f.multiply((radius - v) * k);
        force.add(f);
    }

    public void applyForces(float dt) {
        speed.x += force.x / mass * dt;
        speed.y += force.y / mass * dt;
    }

    public void addForceRelative(Vector2f pivot, float v) {
        //pivot.normalize();
        //pivot.multiply(v * k);
        force.add(pivot, v*k);
    }
}
