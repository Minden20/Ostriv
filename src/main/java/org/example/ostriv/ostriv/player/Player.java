package org.example.ostriv.ostriv.player;

public class Player {
    private String name;
    private int health;
    private int inventory;
    private int lvl;
    private int exp;
    private int damage;
    private int x;
    private int y;

    public Player(int damage, int exp, int health, int inventory, int lvl, String name, int x, int y) {
        this.damage = damage;
        this.exp = exp;
        this.health = health;
        this.inventory = inventory;
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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
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
