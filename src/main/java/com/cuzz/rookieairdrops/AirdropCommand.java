package com.cuzz.rookieairdrops;

import com.cuzz.rookieairdrops.config.AirdropType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AirdropCommand implements CommandExecutor, TabCompleter {
    // 基础配置
    private static final double FIXED_Y = 100.0; // 固定的Y轴高度
    private static final double SPEED = 0.1; // 飞行速度
//    private static final double WIND_CHANGE_INTERVAL = 200L; // 风向改变间隔(ticks)

    // 线程池配置
    private static final int THREAD_POOL_SIZE = 3; // 线程池大小
    private static final long UPDATE_INTERVAL = 50L; // 更新间隔(毫秒)

    // 边界配置
    private static final double WORLD_BOUNDARY = 30000000.0; // 世界边界

    // 实例变量
    private Vector windDirection;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private final Map<Bee,AirdropSpawnTask> activeTasks;
    private long lastWindChange;
    private static final long WIND_CHANGE_INTERVAL = 30000; // 30秒改变一次风向
    private static final double WIND_SPEED = 0.1; // 风速
    private static final int MAX_ACTIVE_TASKS = 10; // 最大活动任务数

    public AirdropCommand() {
        this.scheduler = Executors.newScheduledThreadPool(6);
        this.activeTasks = new ConcurrentHashMap<>();
        this.random = new Random();
        this.windDirection = new Vector(1, 0, 0); // 初始风向
        this.lastWindChange = System.currentTimeMillis();
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

        if (args.length < 1) {
            sender.sendMessage("§c用法: /airdrop <spawn [类型]|reload>");
            return true;
        }

        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "spawn":
                if (args.length > 1) {
                    // 指定空投类型
                    spawnAirdrop(player, args[1]);
                } else {
                    // 随机空投
                    spawnAirdrop(player);
                }
                break;
            case "reload":
                reloadConfig(player);
                break;
            default:
                player.sendMessage("§c用法: /airdrop <spawn [类型]|reload>");
                break;
        }
        return true;
    }

    /**
     * 为指定玩家生成随机空投
     * @param player 目标玩家
     */
    public void spawnAirdrop(Player player) {
        spawnAirdrop(player, null);
    }

    /**
     * 为指定玩家生成空投
     * @param player 目标玩家
     * @param typeName 空投类型名称，如果为null则随机选择
     */
    public void spawnAirdrop(Player player, String typeName) {
        // 检查活动任务数量
        if (activeTasks.size() >= MAX_ACTIVE_TASKS) {
            player.sendMessage("§c当前活动空投数量已达上限，请稍后再试！");
            return;
        }

        // 检查线程池状态
        if (scheduler.isShutdown()) {
            player.sendMessage("§c系统正在关闭，无法生成空投！");
            return;
        }

        Location location = player.getLocation();
        Location newLocation = location.clone();

        // 获取空投类型
        AirdropType airdropType;
        if (typeName != null) {
            // 尝试获取指定类型的空投
            airdropType = RookieAirDrops.getInstance().getAirdropConfig().getAirdropTypes().get(typeName);
            if (airdropType == null) {
                player.sendMessage("§c未找到指定的空投类型: " + typeName);
                return;
            }
        } else {
            // 随机选择空投类型
            airdropType = RookieAirDrops.getInstance().getAirdropConfig().getRandomAirdropType();
            if (airdropType == null) {
                player.sendMessage("§c没有可用的空投类型！");
                return;
            }
        }

        // 根据配置的高度范围随机生成高度，并叠加到玩家当前位置的高度上
        int minHeight = airdropType.getHeightMin();
        int maxHeight = airdropType.getHeightMax();
        double randomHeight = minHeight + (random.nextDouble() * (maxHeight - minHeight));
        System.out.println(randomHeight+"@@@@@@@@@@@");
        newLocation.setY(location.getY() + randomHeight);

        // 检查生成位置是否在天空中
        if (newLocation.getBlock().getType() != Material.AIR) {
            player.sendMessage("§c无法在此位置生成空投，该位置不是天空！");
            return;
        }

        try {
            // 生成不可见的蜜蜂实体
            Bee bee = (Bee) newLocation.getWorld().spawn(newLocation, Bee.class);
            bee.setCustomName("§6" + airdropType.getName());
            bee.setCustomNameVisible(true);
            bee.setInvisible(true);
            bee.setAI(false);
            bee.setInvulnerable(true);

            // 应用热气球模型
            RookieAirDrops.getInstance().getModelEngine().spawnModel(bee, airdropType.getModelName());

            // 生成显示实体
            player.getWorld().spawnEntity(newLocation, EntityType.BLOCK_DISPLAY);

            // 创建任务控制热气球移动
            AirdropSpawnTask task = new AirdropSpawnTask(airdropType);
            activeTasks.put(bee, task);

            // 在异步线程中计算移动，但在主线程中应用移动
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (bee.isDead() || task.isCancelled()) {
                        activeTasks.remove(bee);
                        return;
                    }

                    // 更新风向
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastWindChange >= WIND_CHANGE_INTERVAL) {
                        updateWindDirection();
                        lastWindChange = currentTime;
                    }

                    // 计算移动向量
                    Vector moveVector = windDirection.clone().multiply(WIND_SPEED);

                    // 确保热气球不会飞出世界边界
                    Location currentLoc = bee.getLocation();
                    World world = currentLoc.getWorld();
                    if (world != null) {
                        double maxX = world.getWorldBorder().getSize() / 2;
                        double maxZ = world.getWorldBorder().getSize() / 2;

                        if (Math.abs(currentLoc.getX()) > maxX || Math.abs(currentLoc.getZ()) > maxZ) {
                            // 如果即将飞出边界，改变方向
                            windDirection.multiply(-1);
                            moveVector = windDirection.clone().multiply(WIND_SPEED);
                        }
                    }

                    // 计算新位置
                    Location newLoc = currentLoc.clone().add(moveVector);

                    // 在主线程中传送实体
                    final Location finalNewLoc = newLoc;
                    Bukkit.getScheduler().runTask(RookieAirDrops.getInstance(), () -> {
                        if (!bee.isDead() && !task.isCancelled()) {
                            bee.teleport(finalNewLoc);
                        }
                    });

                } catch (Exception e) {
                    RookieAirDrops.getInstance().getLogger().warning("空投移动任务出错: " + e.getMessage());
                    activeTasks.remove(bee);
                }
            }, 0, 10, TimeUnit.MILLISECONDS);

            task.setFuture(future);

            player.sendMessage("§a成功生成" + airdropType.getName() + "空投！");
        } catch (Exception e) {
            player.sendMessage("§c生成空投时出错: " + e.getMessage());
            RookieAirDrops.getInstance().getLogger().warning("生成空投时出错: " + e.getMessage());
        }
    }

    /**
     * 安排下一次空投生成
     * @param airdropType 空投类型
     */
    public void scheduleNextAirdrop(AirdropType airdropType) {
        // 获取配置的时间间隔
        int minTimeGap = airdropType.getMinTimeGap();
        int maxTimeGap = airdropType.getMaxTimeGap();

        // 随机选择时间间隔（分钟）
        int timeGapMinutes = minTimeGap + random.nextInt(maxTimeGap - minTimeGap + 1);

        // 创建定时任务
        AirdropSpawnTask task = new AirdropSpawnTask(airdropType);


        // 安排任务执行
        scheduler.schedule(() -> {
            // 获取在线玩家列表
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

            if (!onlinePlayers.isEmpty()) {
                // 获取配置的玩家数量
                int targetAmount = airdropType.getAmount();

                // 确保不超过在线玩家数量
                int actualAmount = Math.min(targetAmount, onlinePlayers.size());

                // 随机选择指定数量的玩家
                List<Player> selectedPlayers = new ArrayList<>();
                List<Player> availablePlayers = new ArrayList<>(onlinePlayers);

                for (int i = 0; i < actualAmount; i++) {
                    if (availablePlayers.isEmpty()) {
                        break;
                    }

                    int index = random.nextInt(availablePlayers.size());
                    selectedPlayers.add(availablePlayers.get(index));
                    availablePlayers.remove(index);
                }

                // 为每个选中的玩家生成空投
                for (Player targetPlayer : selectedPlayers) {
                    // 在主线程中生成空投
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            spawnAirdrop(targetPlayer);
                        }
                    }.runTask(RookieAirDrops.getInstance());
                }

                // 广播消息
                if (actualAmount > 0) {
                    Bukkit.broadcastMessage("§a已为 " + actualAmount + " 名玩家生成 " + airdropType.getName() + "！");
                }
            }

            // 任务完成后从活动任务列表中移除
            activeTasks.remove(task);
        }, timeGapMinutes, TimeUnit.MINUTES);

        // 广播消息
        Bukkit.broadcastMessage("§a" + timeGapMinutes + " 分钟后将生成 " + airdropType.getAmount() + " 个 " + airdropType.getName() + "！");
    }

    private boolean isLocationValid(Location loc) {
        // 检查位置是否在有效范围内
        return loc.getX() > -WORLD_BOUNDARY && loc.getX() < WORLD_BOUNDARY &&
               loc.getZ() > -WORLD_BOUNDARY && loc.getZ() < WORLD_BOUNDARY;
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        try {
            // 取消所有活动任务
            for (AirdropSpawnTask task : activeTasks.values()) {
                task.cancel();
            }
            activeTasks.clear();

            // 关闭线程池
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 取消指定气球的移动任务
     * @param bee 要取消任务的气球实体
     */
    public void cancelAirdropTask(Bee bee) {
        AirdropSpawnTask airdropSpawnTask = activeTasks.get(bee);
        if (airdropSpawnTask!=null){
            airdropSpawnTask.cancel();
        }
         return;
    }

    /**
     * 重载配置文件
     * @param player 执行命令的玩家
     */
    private void reloadConfig(Player player) {
        player.sendMessage("§a正在重载配置文件...");

        // 在异步线程中执行配置重载
        scheduler.execute(() -> {
            try {
                // 保存当前活动的空投任务
                Map<Bee,AirdropSpawnTask> currentTasks = new ConcurrentHashMap<>();

                // 在主线程中执行配置重载
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 重载配置文件
                        RookieAirDrops.getInstance().getAirdropConfig().loadConfig();

                        // 取消所有当前活动的空投任务
                        for (AirdropSpawnTask task : currentTasks.values()) {
                            // 任务会在执行时自动从 activeTasks 中移除
                            scheduler.schedule(() -> {
                                // 使用新的配置重新安排空投任务
                                scheduleNextAirdrop(task.getAirdropType());
                            }, 0, TimeUnit.MILLISECONDS);
                        }

                        // 发送重载完成消息
                        player.sendMessage("§a配置文件重载完成！");
                    }
                }.runTask(RookieAirDrops.getInstance());
            } catch (Exception e) {
                // 在主线程中发送错误消息
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("§c配置文件重载失败: " + e.getMessage());
                    }
                }.runTask(RookieAirDrops.getInstance());
            }
        });
    }

    /**
     * 空投生成任务类
     */
    private static class AirdropSpawnTask {
        private final AirdropType airdropType;
        private  ScheduledFuture<?> future;
        private boolean cancelled = false;

        public AirdropSpawnTask( AirdropType airdropType) {
            this.airdropType = airdropType;
            this.future = null;
        }


        public AirdropType getAirdropType() {
            return airdropType;
        }

        public void cancel() {
            if (!cancelled) {
                cancelled = true;
                if (future != null) {
                    future.cancel(false);
                }
            }
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数补全
            completions.add("spawn");
            completions.add("reload");

            // 过滤匹配的补全项
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            // 第二个参数补全（空投类型）
            List<String> types = RookieAirDrops.getInstance().getAirdropConfig().getAirdropTypes().keySet().stream()
                .collect(Collectors.toList());

            // 过滤匹配的补全项
            return types.stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return completions;
    }

    /**
     * 更新风向
     */
    private void updateWindDirection() {
        // 生成一个随机的水平方向向量
        double angle = random.nextDouble() * 2 * Math.PI;
        windDirection = new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize();
    }

} 