package com.cuzz.rookieairdrops;

import com.cuzz.rookieairdrops.config.AirdropConfig;
import com.cuzz.rookieairdrops.config.AirdropType;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AirdropListener implements Listener {
    private final CrateList crateList;
    private final AirdropConfig airdropConfig;

    public AirdropListener(AirdropConfig airdropConfig) {
        this.crateList = new CrateList();
        this.airdropConfig = airdropConfig;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (damaged instanceof Bee && damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Entity && 
                ((Entity) projectile.getShooter()).getType() == EntityType.PLAYER) {
                
                // 获取随机空投类型
                AirdropType airdropType = airdropConfig.getRandomAirdropType();
                if (airdropType == null) {
                    return;
                }
                
                // 取消伤害事件
                event.setCancelled(true);

                // 生成粒子效果
                Particle.DustOptions dustOptions = new Particle.DustOptions(airdropType.getDustColor(), 1.0f);
                damaged.getLocation().getWorld().spawnParticle(
                    Particle.DUST,
                    damaged.getLocation(),
                    100,
                    1.5,
                    1.5,
                    1.5,
                    0.3,
                    dustOptions
                );

                // 取消气球的移动任务
                AirdropCommand airdropCommand = RookieAirDrops.getInstance().getAirdropCommand();
                airdropCommand.cancelAirdropTask((Bee) damaged);

                // 移除蜜蜂实体
                damaged.remove();

                // 获取方块名称
                String blockName = airdropType.getBlock();
                if (blockName == null || blockName.isEmpty()) {
                    blockName = "airballoon"; // 默认方块
                }

                // 创建并投放箱子
                Crate crate = new Crate(damaged.getLocation(), damaged.getWorld(), null, blockName);
                crate.dropCrate();
                
                // 安排下一次空投生成
                airdropCommand.scheduleNextAirdrop(airdropType);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        entity.setGlowing(true);

        Crate crate = CrateList.getCrateMap().get(entity);
        if (crate != null) {
            crate.setChestBlock(entity.getLocation().getBlock());
            crate.spawnChest();

            // 获取 Oraxen 方块数据
            BlockData oraxenBlockData = OraxenBlocks.getOraxenBlockData(crate.getBlockName());
            BlockData airBlockData = Bukkit.createBlockData(Material.BARRIER);

            // 从地图中移除箱子
            CrateList.getCrateMap().remove(entity);
        } else {
            event.setCancelled(true);
        }
    }
} 