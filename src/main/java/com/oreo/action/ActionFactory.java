package com.oreo.action;

import com.oreo.SmartMenus;
import com.oreo.gui.GuiDefinition;
import com.oreo.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ActionFactory {

    private static final Random RANDOM = new Random();

    private static Map<String, java.util.function.Function<Map<String, Object>, Action>> customRegistry = null;

    public static void setCustomRegistry(Map<String, java.util.function.Function<Map<String, Object>, Action>> registry) {
        customRegistry = registry;
    }

    @SuppressWarnings("unchecked")
    public static List<Action> parseActions(List<?> raw) {
        List<Action> actions = new ArrayList<>();
        if (raw == null) return actions;
        for (Object obj : raw) {
            if (obj instanceof Map) {
                try {
                    Action a = parseAction((Map<String, Object>) obj);
                    if (a != null) actions.add(a);
                } catch (Exception ignored) {
                }
            }
        }
        return actions;
    }

    public static Action parseAction(Map<String, Object> map) {
        if (map == null) return null;
        Object typeObj = map.get("type");
        if (typeObj == null) return null;
        String type = typeObj.toString().toUpperCase();

        switch (type) {
            case "PLAYER_MESSAGE":
                return new PlayerMessageAction(str(map, "message", ""));
            case "BROADCAST":
                return new BroadcastAction(str(map, "message", ""));
            case "ACTION_BAR":
                return new ActionBarAction(str(map, "message", ""));
            case "TITLE":
                return new TitleAction(
                        str(map, "title", ""),
                        str(map, "subtitle", ""),
                        intVal(map, "fade-in", 10),
                        intVal(map, "stay", 40),
                        intVal(map, "fade-out", 10)
                );
            case "SOUND":
                return new SoundAction(
                        str(map, "sound", "UI_BUTTON_CLICK"),
                        floatVal(map, "volume", 1.0f),
                        floatVal(map, "pitch", 1.0f),
                        false
                );
            case "BROADCAST_SOUND":
                return new SoundAction(
                        str(map, "sound", "UI_BUTTON_CLICK"),
                        floatVal(map, "volume", 1.0f),
                        floatVal(map, "pitch", 1.0f),
                        true
                );
            case "TELEPORT":
                return new TeleportAction(
                        str(map, "world", "world"),
                        doubleVal(map, "x", 0.0),
                        doubleVal(map, "y", 64.0),
                        doubleVal(map, "z", 0.0),
                        floatVal(map, "yaw", 0.0f),
                        floatVal(map, "pitch", 0.0f)
                );
            case "CONSOLE_COMMAND":
                return new ConsoleCommandAction(str(map, "command", ""));
            case "PLAYER_COMMAND":
                return new PlayerCommandAction(str(map, "command", ""));
            case "RANDOM_CONSOLE_COMMAND":
                return new RandomConsoleCommandAction(strList(map, "commands"));
            case "RANDOM_PLAYER_COMMAND":
                return new RandomPlayerCommandAction(strList(map, "commands"));
            case "CHANCE_REWARD":
                return new ChanceRewardAction(rewardList(map, "rewards"));
            case "OPEN_INVENTORY":
                return new OpenInventoryAction(str(map, "inventory", ""));
            case "OPEN_GUI":
                return new OpenInventoryAction(str(map, "gui", str(map, "inventory", "")));
            case "CLOSE_INVENTORY":
                return new CloseInventoryAction();
            case "OPEN_BEDROCK_FORM":
                return new OpenBedrockFormAction(str(map, "inventory", str(map, "gui", "")));
            case "OPEN_BEDROCK_DIALOGUE":
                return new OpenBedrockDialogueAction(str(map, "inventory", str(map, "gui", "")));
            case "BACK":
                return new BackAction();
            case "SERVER_CONNECT":
                return new ServerConnectAction(str(map, "server", ""));
            case "SCRIPT":
                return new ScriptAction(str(map, "script", ""));
            default:

                if (customRegistry != null) {
                    java.util.function.Function<Map<String, Object>, Action> factory = customRegistry.get(type);
                    if (factory != null) {
                        try {
                            return factory.apply(map);
                        } catch (Exception e) {

                        }
                    }
                }
                return null;
        }
    }

    private static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    private static int intVal(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v != null) {
            try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static float floatVal(Map<String, Object> m, String key, float def) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).floatValue();
        if (v != null) {
            try { return Float.parseFloat(v.toString()); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private static double doubleVal(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v != null) {
            try { return Double.parseDouble(v.toString()); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Map<String, Object> m, String key) {
        Object v = m.get(key);
        List<String> result = new ArrayList<>();
        if (v instanceof List) {
            for (Object o : (List<?>) v) {
                if (o != null) result.add(o.toString());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rewardList(Map<String, Object> m, String key) {
        Object v = m.get(key);
        List<Map<String, Object>> result = new ArrayList<>();
        if (v instanceof List) {
            for (Object o : (List<?>) v) {
                if (o instanceof Map) result.add((Map<String, Object>) o);
            }
        }
        return result;
    }

    public static String replaceVars(String text, Player player, Map<String, String> vars) {
        if (text == null) return "";
        if (player != null) text = text.replace("{player}", player.getName());
        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                text = text.replace("%" + e.getKey() + "%", e.getValue() != null ? e.getValue() : "");
            }
        }
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (NoClassDefFoundError | RuntimeException ignored) {

            }
        }
        return text;
    }

    public static final class ConsoleCommandAction implements Action {
        private final String command;
        public ConsoleCommandAction(String command) { this.command = command; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String cmd = replaceVars(command, player, vars);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    private static final class PlayerMessageAction implements Action {
        private final String message;
        PlayerMessageAction(String message) { this.message = message; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            player.sendMessage(ColorUtil.color(replaceVars(message, player, vars)));
        }
    }

    private static final class BroadcastAction implements Action {
        private final String message;
        BroadcastAction(String message) { this.message = message; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            Bukkit.broadcastMessage(ColorUtil.color(replaceVars(message, player, vars)));
        }
    }

    private static final class ActionBarAction implements Action {
        private final String message;
        ActionBarAction(String message) { this.message = message; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String msg = ColorUtil.color(replaceVars(message, player, vars));
            net.kyori.adventure.text.Component component =
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                            .legacySection().deserialize(msg);
            ((net.kyori.adventure.audience.Audience) player).sendActionBar(component);
        }
    }

    private static final class TitleAction implements Action {
        private final String title, subtitle;
        private final int fadeIn, stay, fadeOut;
        TitleAction(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            this.title = title; this.subtitle = subtitle;
            this.fadeIn = fadeIn; this.stay = stay; this.fadeOut = fadeOut;
        }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacy =
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();
            net.kyori.adventure.title.Title adventureTitle = net.kyori.adventure.title.Title.title(
                    legacy.deserialize(ColorUtil.color(replaceVars(title, player, vars))),
                    legacy.deserialize(ColorUtil.color(replaceVars(subtitle, player, vars))),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(fadeIn * 50L),
                            java.time.Duration.ofMillis(stay * 50L),
                            java.time.Duration.ofMillis(fadeOut * 50L)
                    )
            );
            ((net.kyori.adventure.audience.Audience) player).showTitle(adventureTitle);
        }
    }

    private static final class SoundAction implements Action {
        private final String soundName;
        private final float volume, pitch;
        private final boolean broadcast;
        SoundAction(String soundName, float volume, float pitch, boolean broadcast) {
            this.soundName = soundName; this.volume = volume; this.pitch = pitch;
            this.broadcast = broadcast;
        }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            Sound sound;
            try {
                sound = Sound.valueOf(replaceVars(soundName, player, vars).toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[SmartMenus] Unknown sound: " + soundName);
                return;
            }
            if (broadcast) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), sound, volume, pitch);
                }
            } else {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    private static final class TeleportAction implements Action {
        private final String world;
        private final double x, y, z;
        private final float yaw, pitch;
        TeleportAction(String world, double x, double y, double z, float yaw, float pitch) {
            this.world = world; this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            World w = Bukkit.getWorld(replaceVars(world, player, vars));
            if (w == null) {
                plugin.getLogger().warning("[SmartMenus] Teleport: world not found: " + world);
                return;
            }
            player.teleport(new Location(w, x, y, z, yaw, pitch));
        }
    }

    private static final class PlayerCommandAction implements Action {
        private final String command;
        PlayerCommandAction(String command) { this.command = command; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String cmd = replaceVars(command, player, vars);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            player.performCommand(cmd);
        }
    }

    private static final class RandomConsoleCommandAction implements Action {
        private final List<String> commands;
        RandomConsoleCommandAction(List<String> commands) { this.commands = commands; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            if (commands.isEmpty()) return;
            String cmd = replaceVars(commands.get(RANDOM.nextInt(commands.size())), player, vars);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    private static final class RandomPlayerCommandAction implements Action {
        private final List<String> commands;
        RandomPlayerCommandAction(List<String> commands) { this.commands = commands; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            if (commands.isEmpty()) return;
            String cmd = replaceVars(commands.get(RANDOM.nextInt(commands.size())), player, vars);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            player.performCommand(cmd);
        }
    }

    private static final class OpenInventoryAction implements Action {
        private final String guiId;
        OpenInventoryAction(String guiId) { this.guiId = guiId; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String id = replaceVars(guiId, player, vars);
            GuiDefinition def = plugin.getGuiRegistry().getGui(id);
            if (def == null) {

                String normalizedId = id.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
                if (!normalizedId.equals(id)) {
                    def = plugin.getGuiRegistry().getGui(normalizedId);
                }
            }
            if (def == null) {
                plugin.getLogger().warning("[SmartMenus] OPEN_GUI: GUI not found: '" + id
                        + "'. Known GUIs: " + plugin.getGuiRegistry().getGuiIds());
                return;
            }
            for (com.oreo.condition.Condition req : def.getOpenRequirements()) {
                if (!req.check(player)) {
                    player.sendMessage(req.getErrorMessage(player));
                    return;
                }
            }
            com.oreo.bedrock.BedrockManager bm = plugin.getBedrockManager();
            if (bm != null && (bm.openForBedrock(player, def) || bm.autoConvertForBedrock(player, def))) return;
            def.createInventory(plugin.getInventoryManager(), plugin).open(player);
        }
    }

    private static final class CloseInventoryAction implements Action {
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            player.closeInventory();
        }
    }

    public static final class OpenBedrockFormAction implements Action {
        private final String guiId;
        public OpenBedrockFormAction(String guiId) { this.guiId = guiId; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String id = replaceVars(guiId, player, vars);
            GuiDefinition def = plugin.getGuiRegistry().getGui(id);
            if (def == null) {
                plugin.getLogger().warning("[SmartMenus] OPEN_BEDROCK_FORM: GUI not found: " + id);
                return;
            }
            com.oreo.bedrock.BedrockManager bm = plugin.getBedrockManager();
            if (bm != null && (bm.openForBedrock(player, def) || bm.autoConvertForBedrock(player, def))) return;
            def.createInventory(plugin.getInventoryManager(), plugin).open(player);
        }
    }

    public static final class OpenBedrockDialogueAction implements Action {
        private final String guiId;
        public OpenBedrockDialogueAction(String guiId) { this.guiId = guiId; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String id = replaceVars(guiId, player, vars);
            GuiDefinition def = plugin.getGuiRegistry().getGui(id);
            if (def == null) {
                plugin.getLogger().warning("[SmartMenus] OPEN_BEDROCK_DIALOGUE: GUI not found: " + id);
                return;
            }
            com.oreo.bedrock.BedrockManager bm = plugin.getBedrockManager();
            if (bm != null && bm.openForBedrock(player, def)) return;
            def.createInventory(plugin.getInventoryManager(), plugin).open(player);
        }
    }

    private static final class BackAction implements Action {
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {

            player.closeInventory();
        }
    }

    private static final class ServerConnectAction implements Action {
        private final String server;
        ServerConnectAction(String server) { this.server = server; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            String srv = replaceVars(server, player, vars);
            try {
                java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
                java.io.DataOutputStream out = new java.io.DataOutputStream(bout);
                out.writeUTF("Connect");
                out.writeUTF(srv);
                player.sendPluginMessage(plugin, "BungeeCord", bout.toByteArray());
            } catch (Exception e) {
                plugin.getLogger().warning("[SmartMenus] SERVER_CONNECT failed: " + e.getMessage());
            }
        }
    }

    private static final class ScriptAction implements Action {
        private final String script;
        ScriptAction(String script) { this.script = script; }
        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            plugin.getScriptEngine().evalAction(script, player);
        }
    }

    public static final class ChanceRewardAction implements Action {
        private final List<Map<String, Object>> rewards;

        public ChanceRewardAction(List<Map<String, Object>> rewards) {
            this.rewards = rewards;
        }

        @Override
        public void execute(SmartMenus plugin, Player player, Map<String, String> vars) {
            if (rewards.isEmpty()) return;
            double roll = RANDOM.nextDouble() * 100.0;
            double cumulative = 0.0;
            for (Map<String, Object> reward : rewards) {
                cumulative += doubleVal(reward, "chance", 0.0);
                if (roll < cumulative) {
                    for (String cmd : strList(reward, "commands")) {
                        String resolved = replaceVars(cmd, player, vars);
                        if (resolved.startsWith("/")) resolved = resolved.substring(1);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
                    }
                    for (String msg : strList(reward, "messages")) {
                        net.kyori.adventure.text.Component component =
                                ColorUtil.colorComponent(replaceVars(msg, player, vars));
                        ((net.kyori.adventure.audience.Audience) player).sendMessage(component);
                    }
                    return;
                }
            }

        }
    }
}
