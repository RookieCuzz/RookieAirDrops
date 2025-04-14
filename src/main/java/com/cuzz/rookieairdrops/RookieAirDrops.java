package com.cuzz.rookieairdrops;

import com.cuzz.rookieairdrops.config.AirdropConfig;
import com.cuzz.rookieairdrops.model.ModelEngine;
import com.cuzz.rookieairdrops.model.ModelEngine4;
import org.bukkit.plugin.java.JavaPlugin;

public class RookieAirDrops extends JavaPlugin {
    private static RookieAirDrops instance;
    private ModelEngine modelEngine;
    private AirdropConfig airdropConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化配置
        airdropConfig = new AirdropConfig(this);
        
        // 初始化 ModelEngine
        modelEngine = new ModelEngine4();
        
        // 注册命令
        getCommand("airdrop").setExecutor(new AirdropCommand());
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new AirdropListener(airdropConfig), this);
        
        getLogger().info("RookieAirDrops 已启用!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RookieAirDrops 已禁用!");
    }

    public static RookieAirDrops getInstance() {
        return instance;
    }

    public ModelEngine getModelEngine() {
        return modelEngine;
    }

    public AirdropConfig getAirdropConfig() {
        return airdropConfig;
    }
}
