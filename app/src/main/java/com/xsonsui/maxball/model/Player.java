package com.xsonsui.maxball.model;

public class Player extends Ball{
    public String name;
    public String avatar;
    public String team;

    public Input input;

    public Player(String name) {
        super();
        this.mass=1;
        this.name = name;
        this.radius = 20;
        this.input = new Input();
    }
}
