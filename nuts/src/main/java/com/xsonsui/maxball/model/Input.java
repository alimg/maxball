package com.xsonsui.maxball.model;

import java.io.Serializable;

/**
 * Created by alim on 9/13/14.
 */
public class Input implements Serializable {
    public float x;
    public float y;
    public float kick;

    public void set(Input input) {
        x = input.x;
        y = input.y;
        kick = input.kick;
    }
}
