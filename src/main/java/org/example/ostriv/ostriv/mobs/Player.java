package org.example.ostriv.ostriv.mobs;

public class Player extends Unit {
    private int inventory;
    private int energy;
    private int exp;

    public Player(int damage, int exp, int healthpoints, int inventory, int energy, int lvl, String name, int x, int y) {
        super(damage, exp, healthpoints, inventory, energy, lvl, name, x, y);
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }
    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
    
    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
    
}
