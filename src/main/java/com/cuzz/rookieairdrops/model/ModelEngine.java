package com.cuzz.rookieairdrops.model;

import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.*;

public abstract class ModelEngine {
    public abstract boolean existModel(final String modelId);
    
    public abstract boolean existModelAnimation(final String modelId, final String animationId);
    
    public String getModelsFormat() {
        final Set<String> models = this.getModels();
        return String.join(", ", models);
    }
    
    public abstract Set<String> getModels();
    
    public abstract UUID spawnModel(final LivingEntity entity, final String modelId);
    
    public abstract UUID spawnModel(final Location location, final String modelId, final Player player);
    
    public abstract void removePlayerModel(final UUID uniqueId, final String modelId, final Player player);
    
    public abstract void addPlayerModel(final UUID uniqueId, final Player player);
    
    public abstract void addAnimation(final UUID uniqueId, final String modelId, final String animationId, 
                                    final double in, final double out, final double speed, final boolean loop);
    
    public abstract void stopAnimations(final UUID uniqueId, final String modelId);
    
    public abstract boolean isNew();
} 