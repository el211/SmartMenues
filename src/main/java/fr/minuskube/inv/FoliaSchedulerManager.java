package fr.minuskube.inv;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FoliaSchedulerManager implements SchedulerManager {

    private final JavaPlugin plugin;

    private final Map<Player, Object> tasks = new HashMap<>();

    public FoliaSchedulerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTask(BukkitRunnable runnable, Player player,
                        long delay, long period, SchedulerManager.SchedulerType type) {

        Runnable task = runnable::run;

        Object handle;
        if (delay == 0 && period == 0) {

            handle = runEntityTask(player, task, null);
        } else if (period == 0) {

            handle = runEntityTaskLater(player, task, null, Math.max(1, delay));
        } else {

            handle = runEntityTaskTimer(player, task, null, Math.max(1, delay), Math.max(1, period));
        }

        if (handle != null) {
            tasks.put(player, handle);
        }
    }

    @Override
    public void cancelTaskByPlayer(Player player) {
        Object handle = tasks.remove(player);
        if (handle == null) return;
        try {
            Method cancel = handle.getClass().getMethod("cancel");
            cancel.invoke(handle);
        } catch (Exception ignored) {
        }
    }

    private Object runEntityTask(Player player, Runnable task, Runnable retired) {
        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            Consumer<Object> consumer = ignored -> task.run();
            return scheduler.getClass()
                    .getMethod("run", org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class)
                    .invoke(scheduler, plugin, consumer, retired);
        } catch (Exception e) {
            plugin.getLogger().warning("[SmartMenus] FoliaSchedulerManager: run() failed: " + e.getMessage());
            return null;
        }
    }

    private Object runEntityTaskLater(Player player, Runnable task, Runnable retired, long delay) {
        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            Consumer<Object> consumer = ignored -> task.run();
            return scheduler.getClass()
                    .getMethod("runDelayed",
                            org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class, long.class)
                    .invoke(scheduler, plugin, consumer, retired, delay);
        } catch (Exception e) {
            plugin.getLogger().warning("[SmartMenus] FoliaSchedulerManager: runDelayed() failed: " + e.getMessage());
            return null;
        }
    }

    private Object runEntityTaskTimer(Player player, Runnable task, Runnable retired,
                                       long initialDelay, long period) {
        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            Consumer<Object> consumer = ignored -> task.run();
            return scheduler.getClass()
                    .getMethod("runAtFixedRate",
                            org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class,
                            long.class, long.class)
                    .invoke(scheduler, plugin, consumer, retired, initialDelay, period);
        } catch (Exception e) {
            plugin.getLogger().warning("[SmartMenus] FoliaSchedulerManager: runAtFixedRate() failed: " + e.getMessage());
            return null;
        }
    }
}
