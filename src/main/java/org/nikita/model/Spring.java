package org.nikita.model;

public class Spring {

    private final int id;
    private final int force; // в Ньютонах

    public Spring(int id, int force) {
        this.id = id;
        this.force = force;
    }

    public int getId() {
        return id;
    }

    public int getForce() {
        return force;
    }
}
