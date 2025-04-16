package com.cuzz.rookieairdrops.config;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AirdropConfig {
    private final Plugin plugin;
    private final Map<String, AirdropType> airdropTypes;
    private final Random random;

    public AirdropConfig(Plugin plugin) {
        this.plugin = plugin;
        this.airdropTypes = new HashMap<>();
        this.random = new Random();
        loadConfig();
    }

    public void loadConfig() {
        // 清空现有配置
        airdropTypes.clear();
        
        // 获取配置文件中的空投配置
        ConfigurationSection airDropsSection = plugin.getConfig().getConfigurationSection("AirDrops");
        if (airDropsSection == null) {
            plugin.getLogger().warning("配置文件中没有找到 AirDrops 节点！");
            return;
        }
        
        // 遍历所有空投类型
        for (String typeName : airDropsSection.getKeys(false)) {
            ConfigurationSection typeSection = airDropsSection.getConfigurationSection(typeName);
            if (typeSection == null) continue;
            
            // 创建空投类型对象
            AirdropType airdropType = new AirdropType();
            airdropType.setName(typeName);
            airdropType.setModelName(typeSection.getString("ModelName", "default_model"));
            airdropType.setDustColor(parseColor(typeSection.getString("DustColor", "FFFFFF")));
            airdropType.setSpawnRate(typeSection.getDouble("SpawnRate", 1.0));
            airdropType.setBlock(typeSection.getString("Block", "airballoon"));
            airdropType.setLifeTime(typeSection.getInt("LifeTime", 30));
            
            // 加载高度配置
            ConfigurationSection heightSection = typeSection.getConfigurationSection("Height");
            if (heightSection != null) {
                airdropType.setHeightMin(heightSection.getInt("Min", 20));
                airdropType.setHeightMax(heightSection.getInt("Max", 30));
            } else {
                // 设置默认高度范围
                airdropType.setHeightMin(20);
                airdropType.setHeightMax(30);
            }
            
            // 加载时间间隔配置
            ConfigurationSection timeGapSection = typeSection.getConfigurationSection("TimeGap");
            if (timeGapSection != null) {
                airdropType.setMinTimeGap(timeGapSection.getInt("Min", 10));
                airdropType.setMaxTimeGap(timeGapSection.getInt("Max", 60));
            } else {
                // 设置默认时间间隔
                airdropType.setMinTimeGap(10);
                airdropType.setMaxTimeGap(60);
            }
            
            // 加载生成数量
            airdropType.setAmount(typeSection.getInt("Amount", 1));
            
            // 加载掉落物配置
            List<String> lootList = typeSection.getStringList("Loot");
            for (String lootEntry : lootList) {
                String[] parts = lootEntry.split("@");
                if (parts.length >= 3) {
                    String itemId = parts[0];
                    int amount = Integer.parseInt(parts[1]);
                    double chance = Double.parseDouble(parts[2]);
//                    airdropType.addLoot(itemId, amount, chance);
                }
            }
            
            // 将空投类型添加到配置中
            airdropTypes.put(typeName, airdropType);
        }
        
        plugin.getLogger().info("已加载 " + airdropTypes.size() + " 个空投类型配置");
    }

    private Color parseColor(String colorStr) {
        try {
            // 移除可能的 # 前缀
            if (colorStr.startsWith("#")) {
                colorStr = colorStr.substring(1);
            }
            
            // 解析 RGB 值
            int r = Integer.parseInt(colorStr.substring(0, 2), 16);
            int g = Integer.parseInt(colorStr.substring(2, 4), 16);
            int b = Integer.parseInt(colorStr.substring(4, 6), 16);
            
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            plugin.getLogger().warning("无法解析颜色: " + colorStr + "，使用默认紫罗兰色");
            return Color.fromRGB(148, 0, 211); // 默认紫罗兰色
        }
    }

    public AirdropType getRandomAirdropType() {
        List<AirdropType> availableTypes = new ArrayList<>();
        double totalChance = 0;
        
        for (AirdropType type : airdropTypes.values()) {
            if (random.nextDouble() < type.getSpawnRate()) {
                availableTypes.add(type);
                totalChance += type.getSpawnRate();
            }
        }
        
        if (availableTypes.isEmpty()) {
            return null;
        }
        
        double randomValue = random.nextDouble() * totalChance;
        double currentSum = 0;
        
        for (AirdropType type : availableTypes) {
            currentSum += type.getSpawnRate();
            if (randomValue <= currentSum) {
                return type;
            }
        }
        
        return availableTypes.get(0);
    }

    public List<ItemStack> generateLoot(AirdropType type) {
        List<ItemStack> items = new ArrayList<>();
        
        for (LootItem lootItem : type.getLootItems()) {
            if (random.nextDouble() < lootItem.getChance()) {
                items.add(new ItemStack(lootItem.getMaterial(), lootItem.getAmount()));
            }
        }
        
        return items;
    }

    public Map<String, AirdropType> getAirdropTypes() {
        return airdropTypes;
    }
} 