package com.cuzz.rookieairdrops;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class CrateList {
    private static final Map<Entity,Crate> crateMap = new HashMap<>();

    public static Map<Entity,Crate> getCrateMap() {
        return crateMap;
    }

    public static void addCrate(Entity entity,Crate crate) {
        crateMap.put(entity, crate);
    }

    public static void removeCrate(Entity entity) {
        crateMap.remove(entity);
    }

    public static Crate getCrate(Entity entity) {
        return crateMap.get(entity);
    }

    public static boolean hasCrate(Entity entity) {
        return crateMap.containsKey(entity);
    }
} 