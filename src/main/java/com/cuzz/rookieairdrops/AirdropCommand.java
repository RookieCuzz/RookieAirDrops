package com.cuzz.rookieairdrops;

import com.cuzz.rookieairdrops.config.AirdropType;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AirdropCommand implements CommandExecutor {
    private static final double FIXED_Y = 100.0; // 固定的Y轴高度
    private static final double SPEED = 0.1; // 飞行速度
    private static final double WIND_CHANGE_INTERVAL = 200L; // 风向改变间隔(ticks)
    private Vector windDirection;
    private final Random random;
    private final ScheduledExecutorService scheduler;

    public AirdropCommand() {
        // 初始化风向为随机方向
        this.windDirection = getRandomWindDirection();
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(3);
    }

    private Vector getRandomWindDirection() {
        // 生成一个随机的水平方向向量
        double angle = Math.random() * 2 * Math.PI;
        return new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c这个命令只能由玩家执行！");
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("spawn")) {
            sender.sendMessage("§c用法: /airdrop spawn");
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();
        Location newLocation = location.clone();
        newLocation.setY(FIXED_Y);

        // 获取随机空投类型
        AirdropType airdropType = RookieAirDrops.getInstance().getAirdropConfig().getRandomAirdropType();
        if (airdropType == null) {
            player.sendMessage("§c没有可用的空投类型！");
            return true;
        }

        // 生成不可见的蜜蜂实体
        Bee bee = (Bee) location.getWorld().spawn(newLocation, Bee.class);
        bee.setCustomName("§6" + airdropType.getName());
        bee.setCustomNameVisible(true);
        bee.setInvisible(true);
        bee.setAI(false);
        bee.setInvulnerable(true);

        // 应用热气球模型
        RookieAirDrops.getInstance().getModelEngine().spawnModel(bee, airdropType.getModelName());

        // 生成显示实体
        player.getWorld().spawnEntity(player.getLocation(), EntityType.BLOCK_DISPLAY);

        // 创建异步任务控制热气球移动
        AtomicLong lastWindChange = new AtomicLong(System.currentTimeMillis());
        
        // 使用异步调度器计算新位置
        scheduler.scheduleAtFixedRate(() -> {
            // 检查实体是否有效
            if (!bee.isValid()) {
                scheduler.shutdown();
                return;
            }
            
            // 定期改变风向
            if (System.currentTimeMillis() - lastWindChange.get() > WIND_CHANGE_INTERVAL * 50) {
                windDirection = getRandomWindDirection();
                lastWindChange.set(System.currentTimeMillis());
            }
            
            // 计算新位置
            Location currentLoc = bee.getLocation();
            Location newLoc = currentLoc.clone();
            
            // 保持Y轴高度不变
            newLoc.setY(FIXED_Y);
            
            // 根据风向移动
            newLoc.add(windDirection.clone().multiply(SPEED));
            
            // 检查是否超出世界边界
            if (isLocationValid(newLoc)) {
                // 将实体传送操作放在主线程中执行
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (bee.isValid()) {
                            bee.teleport(newLoc);
                        }
                    }
                }.runTask(RookieAirDrops.getInstance());
            } else {
                // 如果超出边界，改变方向
                windDirection = getRandomWindDirection();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        player.sendMessage("§a" + airdropType.getName() + " 已生成！");
        return true;
    }

    private boolean isLocationValid(Location loc) {
        // 检查位置是否在有效范围内
        return loc.getX() > -30000000 && loc.getX() < 30000000 &&
               loc.getZ() > -30000000 && loc.getZ() < 30000000;
    }
} 