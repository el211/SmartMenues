package com.oreo.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class SmartScheduler {

    private static final boolean FOLIA;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
        FOLIA = folia;
    }

    private SmartScheduler() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runTask(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            runGlobalTask(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runTaskForPlayer(JavaPlugin plugin, Player player, Runnable task) {
        if (FOLIA) {
            runEntityTask(plugin, player, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            runGlobalTaskLater(plugin, task, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runTaskLaterForPlayer(JavaPlugin plugin, Player player, Runnable task, long delayTicks) {
        if (FOLIA) {
            runEntityTaskLater(plugin, player, task, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static Object runTaskTimer(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (FOLIA) {
            return runGlobalTaskTimer(plugin, task, delayTicks, periodTicks);
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    public static void cancelTask(Object handle) {
        if (handle == null) return;
        try {

            if (handle instanceof org.bukkit.scheduler.BukkitTask) {
                ((org.bukkit.scheduler.BukkitTask) handle).cancel();
                return;
            }

            Method cancel = handle.getClass().getMethod("cancel");
            cancel.invoke(handle);
        } catch (Exception ignored) {
        }
    }

    private static void runGlobalTask(JavaPlugin plugin, Runnable task) {
        try {
            Object scheduler = Bukkit.getServer().getClass()
                    .getMethod("getGlobalRegionScheduler")
                    .invoke(Bukkit.getServer());
            scheduler.getClass()
                    .getMethod("run", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run());
        } catch (Exception e) {

            task.run();
        }
    }

    private static void runGlobalTaskLater(JavaPlugin plugin, Runnable task, long delay) {
        try {
            Object scheduler = Bukkit.getServer().getClass()
                    .getMethod("getGlobalRegionScheduler")
                    .invoke(Bukkit.getServer());
            scheduler.getClass()
                    .getMethod("runDelayed", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, long.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run(), delay);
        } catch (Exception e) {
            runGlobalTask(plugin, task);
        }
    }

    private static Object runGlobalTaskTimer(JavaPlugin plugin, Runnable task, long delay, long period) {
        try {
            Object scheduler = Bukkit.getServer().getClass()
                    .getMethod("getGlobalRegionScheduler")
                    .invoke(Bukkit.getServer());
            return scheduler.getClass()
                    .getMethod("runAtFixedRate",
                            org.bukkit.plugin.Plugin.class,
                            java.util.function.Consumer.class,
                            long.class, long.class)
                    .invoke(scheduler, plugin,
                            (java.util.function.Consumer<Object>) ignored -> task.run(),
                            Math.max(1, delay), Math.max(1, period));
        } catch (Exception e) {
            return null;
        }
    }

    private static void runEntityTask(JavaPlugin plugin, Player player, Runnable task) {
        try {
            Object scheduler = player.getClass()
                    .getMethod("getScheduler")
                    .invoke(player);
            scheduler.getClass()
                    .getMethod("run", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, Runnable.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run(), (Runnable) null);
        } catch (Exception e) {
            runGlobalTask(plugin, task);
        }
    }

    private static void runEntityTaskLater(JavaPlugin plugin, Player player, Runnable task, long delay) {
        try {
            Object scheduler = player.getClass()
                    .getMethod("getScheduler")
                    .invoke(player);
            scheduler.getClass()
                    .getMethod("runDelayed", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run(), (Runnable) null, delay);
        } catch (Exception e) {
            runGlobalTaskLater(plugin, task, delay);
        }
    }
}
