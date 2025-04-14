package com.cuzz.rookieairdrops.model;

import com.cuzz.rookieairdrops.RookieAirDrops;
import com.ticxo.modelengine.api.*;
import com.ticxo.modelengine.api.entity.*;
import com.ticxo.modelengine.api.model.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.UUID;

public class MEG {
    private UUID megUniqueId;
    private String modelName;
    private final RookieAirDrops plugin;

    public MEG(RookieAirDrops plugin) {
        this.plugin = plugin;
    }
    
    public void playAnimation(final Player player, final String animationId) {
        if (player == null || this.megUniqueId == null) {
            return;
        }
        plugin.getModelEngine().addAnimation(this.megUniqueId, this.modelName, animationId, 0.5, 1.0, 1.0, false);
    }
    
    public void spawnModel(final Location location, final String modelId, final Player player) {
        final Dummy<?> dummy = new Dummy<>();
        dummy.setDetectingPlayers(false);
        dummy.syncLocation(location);
        final ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        final ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        activeModel.setCanHurt(false);
        modeledEntity.addModel(activeModel, true);
        plugin.getModelEngine().addPlayerModel(dummy.getUUID(), player);
        this.megUniqueId = dummy.getUUID();
        this.modelName = modelId;
    }
    
    public void removeModel(final Player player) {
        if (this.megUniqueId == null) {
            plugin.getLogger().info("ModelEntity not exist");
            return;
        }
        plugin.getModelEngine().removePlayerModel(this.megUniqueId, this.modelName, player);
    }
} 