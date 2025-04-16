package com.cuzz.rookieairdrops;

import com.cuzz.rookieairdrops.config.AirdropConfig;
import com.cuzz.rookieairdrops.model.ModelEngine;
import com.cuzz.rookieairdrops.model.ModelEngine4;
import org.bukkit.plugin.java.JavaPlugin;

public class RookieAirDrops extends JavaPlugin {
    private static RookieAirDrops instance;
    private ModelEngine modelEngine;
    private AirdropConfig airdropConfig;
    private AirdropCommand airdropCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化配置
        airdropConfig = new AirdropConfig(this);
        
        // 注册命令
        airdropCommand = new AirdropCommand();
        getCommand("airdrop").setExecutor(airdropCommand);
        getCommand("airdrop").setTabCompleter(airdropCommand);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new AirdropListener(airdropConfig), this);
        
        // 初始化模型引擎
        modelEngine = new ModelEngine4();
        
        getLogger().info("RookieAirDrops 已启用!");
    }

    @Override
    public void onDisable() {
        // 关闭调度器
        if (airdropCommand != null) {
            airdropCommand.shutdown();
        }
        
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
    
    public AirdropCommand getAirdropCommand() {
        return airdropCommand;
    }
}
