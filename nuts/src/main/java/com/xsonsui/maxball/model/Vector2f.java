package com.xsonsui.maxball.model;

import java.io.Serializable;

/**
 * Created by alim on 9/12/14.
 */
public class Vector2f implements Serializable{
    public float x;
    public float y;

    public Vector2f(){
        x=0;
        y=0;
    }

    public Vector2f(float x, float y) {
        this.x=x;
        this.y=y;
    }

    public float distance(Vector2f position) {
        return (float) Math.sqrt((x - position.x) * (x - position.x) +
                (y - position.y) * (y - position.y));
    }

    public void normalize() {
        float d = length();
        if(d==0) {
            x=0;
            y=0;
            return;
        }
        x = x / d;
        y = y / d;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public void multiply(float k) {
        x = x * k;
        y = y * k;
    }

    public void add(Vector2f f) {
        x += f.x;
        y += f.y;
    }

    public void add(Vector2f v, float dt) {
        x += v.x * dt;
        y += v.y * dt;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2f v) {
        x = v.x;
        y = v.y;
    }

    public float dot(Vector2f v) {
        return v.x*x + v.y*y;
    }
}
