package com.cuzz.rookieairdrops.config;

import org.bukkit.Color;

import java.util.List;

public class AirdropType {
    private String name;
    private String modelName;
    private Color dustColor;
    private List<LootItem> lootItems;
    private double spawnRate;
    private int amount;
    private int minTimeGap;
    private int maxTimeGap;
    private int lifeTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Color getDustColor() {
        return dustColor;
    }

    public void setDustColor(Color dustColor) {
        this.dustColor = dustColor;
    }

    public List<LootItem> getLootItems() {
        return lootItems;
    }

    public void setLootItems(List<LootItem> lootItems) {
        this.lootItems = lootItems;
    }

    public double getSpawnRate() {
        return spawnRate;
    }

    public void setSpawnRate(double spawnRate) {
        this.spawnRate = spawnRate;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMinTimeGap() {
        return minTimeGap;
    }

    public void setMinTimeGap(int minTimeGap) {
        this.minTimeGap = minTimeGap;
    }

    public int getMaxTimeGap() {
        return maxTimeGap;
    }

    public void setMaxTimeGap(int maxTimeGap) {
        this.maxTimeGap = maxTimeGap;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }
} 