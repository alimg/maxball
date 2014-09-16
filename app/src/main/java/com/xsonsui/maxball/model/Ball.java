package com.xsonsui.maxball.model;

import java.io.Serializable;

/**
 * Created by alim on 9/12/14.
 */
public class Ball implements Serializable{
    public Vector2f position;
    public float radius;
    public Vector2f speed;

    public Vector2f force;
    public float mass;
    public float k;

    public Ball(){
        position = new Vector2f();
        speed = new Vector2f();
        force = new Vector2f();
        radius= 16;
        mass = 0.5f;
        k = 2000f;
    }

    Vector2f f = new Vector2f();
    public void addForce(Vector2f pivot, float v) {
        f.set(position.x - pivot.x, position.y - pivot.y);
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
