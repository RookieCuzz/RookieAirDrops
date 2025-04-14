package com.cuzz.rookieairdrops.config;

import org.bukkit.Material;

public class LootItem {
    private final Material material;
    private final int amount;
    private final double chance;

    public LootItem(Material material, int amount, double chance) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }
} 