package com.xsonsui.maxball.model;

public class Player extends Ball{
    public String name;
    public String avatar;
    public int team;

    public Input input;

    public Player(String name) {
        super();
        init();
        this.mass=1;
        this.name = name;
        this.radius = 15;
    }

    private void init() {
        WALL_COLLISION_DAMP = 0.01f;
        this.input = new Input();
    }

    public Player(Player p) {
        super();
        init();
        position.set(p.position);
        speed.set(p.speed);
        force.set(p.force);
        name = p.name;
        mass = p.mass;
        radius = p.radius;
    }
}
