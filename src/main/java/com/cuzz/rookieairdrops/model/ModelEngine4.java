package com.cuzz.rookieairdrops.model;

import com.ticxo.modelengine.api.*;
import com.ticxo.modelengine.api.generator.blueprint.*;
import com.ticxo.modelengine.api.model.*;
import com.ticxo.modelengine.api.entity.*;
import com.ticxo.modelengine.api.animation.*;
import com.ticxo.modelengine.api.animation.property.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

public class ModelEngine4 extends ModelEngine {
    @Override
    public boolean existModel(final String modelId) {
        final ModelBlueprint modelBlueprint = ModelEngineAPI.getBlueprint(modelId);
        return modelBlueprint != null;
    }
    
    @Override
    public boolean existModelAnimation(final String modelId, final String animationId) {
        final ModelBlueprint modelBlueprint = ModelEngineAPI.getBlueprint(modelId);
        return modelBlueprint != null && modelBlueprint.getAnimations().containsKey(animationId);
    }
    
    @Override
    public Set<String> getModels() {
        return ModelEngineAPI.getAPI().getModelRegistry().getKeys();
    }
    
    @Override
    public UUID spawnModel(final LivingEntity entity, final String modelId) {
        final ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
        final ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        activeModel.setCanHurt(false);
        modeledEntity.addModel(activeModel, true);
        return modeledEntity.getBase().getUUID();
    }
    
    @Override
    public UUID spawnModel(final Location location, final String modelId, final Player player) {
        final Dummy<?> dummy = new Dummy<>();
        dummy.setDetectingPlayers(false);
        dummy.syncLocation(location);
        final ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        final ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        activeModel.setCanHurt(false);
        modeledEntity.addModel(activeModel, true);
        addPlayerModel(dummy.getUUID(), player);
        return dummy.getUUID();
    }
    
    @Override
    public void stopAnimations(final UUID uniqueId, final String modelId) {
        final ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uniqueId);
        if (modeledEntity == null) {
            return;
        }
        modeledEntity.getModel(modelId).ifPresent(activeModel -> 
            activeModel.getAnimationHandler().forceStopAllAnimations());
    }
    
    @Override
    public void removePlayerModel(final UUID uniqueId, final String modelId, final Player player) {
        final ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uniqueId);
        if (modeledEntity == null) {
            return;
        }
        modeledEntity.removeModel(modelId).ifPresent(ActiveModel::destroy);
        modeledEntity.markRemoved();
        ModelEngineAPI.removeModeledEntity(uniqueId);
    }
    
    @Override
    public void addPlayerModel(final UUID uniqueId, final Player player) {
        final ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uniqueId);
        if (modeledEntity == null) {
            return;
        }
        final Dummy<?> dummy = (Dummy<?>)modeledEntity.getBase();
        dummy.setForceViewing(player, true);
    }
    
    @Override
    public void addAnimation(final UUID uniqueId, final String modelId, final String animationId, 
                           final double in, final double out, final double speed, final boolean loop) {
        final ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uniqueId);
        if (modeledEntity == null) {
            return;
        }
        modeledEntity.getModel(modelId).ifPresent(activeModel -> {
            final SimpleProperty simpleProperty = new SimpleProperty(activeModel, 
                activeModel.getBlueprint().getAnimations().get(animationId), in, out, speed);
            if (!activeModel.getAnimationHandler().playAnimation(simpleProperty, true)) {
                Bukkit.getLogger().warning("The animation id(" + animationId + ") does not exist, please make sure to add the corresponding animations to the model.");
            }
            simpleProperty.setForceLoopMode(loop ? BlueprintAnimation.LoopMode.LOOP : BlueprintAnimation.LoopMode.ONCE);
        });
    }
    
    @Override
    public boolean isNew() {
        return true;
    }
} 