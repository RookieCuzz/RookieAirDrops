package com.cuzz.rookieairdrops;

import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Crate {
    private final Location location;
    private final World world;
    private final List<ItemStack> items;
    private final String blockName;
    private Block chestBlock;
    private Entity displayEntity;
    private FallingBlock fallingCrate;

    public Crate(Location location, World world, List<ItemStack> items, String blockName) {
        this.location = location;
        this.world = world;
        this.items = items;
        this.blockName = blockName;
    }

    public void dropCrate() {
        // 获取 Oraxen 方块数据
        BlockData oraxenBlockData = OraxenBlocks.getOraxenBlockData(blockName);
        if (oraxenBlockData != null) {
            // 生成下落的方块
            fallingCrate = world.spawnFallingBlock(location, oraxenBlockData);
            fallingCrate.setGlowing(true);
            fallingCrate.setCustomName("§7坠落中.....");
            fallingCrate.setCustomNameVisible(true);
            
            // 禁用重力
            fallingCrate.setGravity(false);
            
            // 创建定时任务控制下落速度
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fallingCrate.isDead()) {
                        this.cancel();
                        return;
                    }
                    // 设置缓慢下落的速度
                    fallingCrate.setVelocity(new Vector(0.0, -0.3, 0.0));
                }
            }.runTaskTimer(RookieAirDrops.getInstance(), 0L, 2L);
            
            // 添加到箱子列表
            CrateList.getCrateMap().put(fallingCrate, this);
        } else {
            Bukkit.getLogger().warning("方块获取失败: " + blockName);
        }
    }

    public void setChestBlock(Block block) {
        this.chestBlock = block;
    }

    public void spawnChest() {
        if (chestBlock != null) {
            // 设置方块为箱子
            chestBlock.setType(org.bukkit.Material.CHEST);

            // 如果是箱子，填充物品
            if (chestBlock.getState() instanceof org.bukkit.block.Chest) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestBlock.getState();
                for (ItemStack item : items) {
                    chest.getInventory().addItem(item);
                }
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    public World getWorld() {
        return world;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Block getChestBlock() {
        return chestBlock;
    }

    public Entity getDisplayEntity() {
        return displayEntity;
    }

    public FallingBlock getFallingCrate() {
        return fallingCrate;
    }
    
    public String getBlockName() {
        return blockName;
    }
} 