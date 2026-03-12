package org.example.ostriv.ostriv.mobs;

public class Unit {
    private String name;
    private int healthpoints;
    private int lvl;
    private int damage;
    private int x;
    private int y;

    public Unit(int damage, int exp, int healthpoints, int inventory, int lvl, int lvl2, String name, int x, int y) {
        this.damage = damage;
        this.healthpoints = healthpoints;
        this.lvl = lvl;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealthpoints() {
        return healthpoints;
    }

    public void setHealthpoints(int healthpoints) {
        this.healthpoints = healthpoints;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
