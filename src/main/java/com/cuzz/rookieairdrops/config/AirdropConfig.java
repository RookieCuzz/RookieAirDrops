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
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        ConfigurationSection airdropsSection = config.getConfigurationSection("AirDrops");
        if (airdropsSection == null) {
            plugin.getLogger().warning("配置文件中没有找到 AirDrops 部分，将使用默认配置");
            return;
        }

        for (String key : airdropsSection.getKeys(false)) {
            ConfigurationSection typeSection = airdropsSection.getConfigurationSection(key);
            if (typeSection == null) {
                continue;
            }

            AirdropType type = new AirdropType();
            type.setName(key);
            type.setModelName(typeSection.getString("ModelName", "hot_air_balloon"));
            
            // 解析颜色
            String colorStr = typeSection.getString("DustColor", "9400D3");
            type.setDustColor(parseColor(colorStr));
            
            // 解析掉落物
            List<LootItem> lootItems = new ArrayList<>();
            ConfigurationSection lootSection = typeSection.getConfigurationSection("Loot");
            if (lootSection != null) {
                for (String lootKey : lootSection.getKeys(false)) {
                    String lootStr = lootSection.getString(lootKey);
                    if (lootStr == null) {
                        continue;
                    }
                    
                    String[] parts = lootStr.split("@");
                    if (parts.length >= 3) {
                        String itemStr = parts[0];
                        int amount = Integer.parseInt(parts[1]);
                        double chance = Double.parseDouble(parts[2]);
                        
                        Material material = Material.valueOf(itemStr.split(":")[1].toUpperCase());
                        lootItems.add(new LootItem(material, amount, chance));
                    }
                }
            }
            type.setLootItems(lootItems);
            
            // 解析生成设置
            type.setSpawnRate(typeSection.getDouble("SpawnRate", 0.9));
            type.setAmount(typeSection.getInt("Amount", 1));
            
            ConfigurationSection timeGapSection = typeSection.getConfigurationSection("TimeGap");
            if (timeGapSection != null) {
                type.setMinTimeGap(timeGapSection.getInt("Min", 10));
                type.setMaxTimeGap(timeGapSection.getInt("Max", 60));
            }
            
            type.setLifeTime(typeSection.getInt("LifeTime", 30));
            
            airdropTypes.put(key, type);
        }
        
        plugin.getLogger().info("已加载 " + airdropTypes.size() + " 种空投类型");
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