package com.oreo.util;

import com.oreo.SmartMenus;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final SmartMenus plugin;
    private final Map<String, Map<UUID, Long>> store = new ConcurrentHashMap<>();
    private final File storageFile;

    public CooldownManager(SmartMenus plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        load();
    }

    public long remainingMillis(String id, UUID uuid) {
        Map<UUID, Long> bucket = store.get(id);
        if (bucket == null) return 0L;
        Long expiry = bucket.get(uuid);
        if (expiry == null) return 0L;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0L) {
            bucket.remove(uuid);
            return 0L;
        }
        return remaining;
    }

    public void apply(String id, UUID uuid, long durationMillis) {
        store.computeIfAbsent(id, k -> new ConcurrentHashMap<>())
                .put(uuid, System.currentTimeMillis() + durationMillis);
    }

    public void clear(String id, UUID uuid) {
        Map<UUID, Long> bucket = store.get(id);
        if (bucket != null) bucket.remove(uuid);
    }

    public void load() {
        store.clear();
        if (!storageFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(storageFile);
        long now = System.currentTimeMillis();
        int loaded = 0;

        for (String id : config.getKeys(false)) {
            ConfigurationSection bucketSection = config.getConfigurationSection(id);
            if (bucketSection == null) continue;
            for (String uuidKey : bucketSection.getKeys(false)) {
                long expiry = bucketSection.getLong(uuidKey, 0L);
                if (expiry <= now) continue;
                try {
                    UUID uuid = UUID.fromString(uuidKey);
                    store.computeIfAbsent(id, k -> new ConcurrentHashMap<>()).put(uuid, expiry);
                    loaded++;
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        if (loaded > 0) {
            plugin.getLogger().info("[SmartMenus] Loaded " + loaded + " active cooldown(s).");
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        long now = System.currentTimeMillis();
        int saved = 0;

        for (Map.Entry<String, Map<UUID, Long>> bucket : store.entrySet()) {
            for (Map.Entry<UUID, Long> entry : bucket.getValue().entrySet()) {
                if (entry.getValue() <= now) continue;
                config.set(bucket.getKey() + "." + entry.getKey(), entry.getValue());
                saved++;
            }
        }

        try {
            if (saved == 0 && storageFile.exists()) {
                storageFile.delete();
                return;
            }
            if (saved == 0) return;
            File parent = storageFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[SmartMenus] Failed to save cooldowns.yml: " + e.getMessage());
        }
    }

    public boolean tryUse(Player player, CooldownConfig config) {
        if (config == null || !config.isEnabled()) return true;

        String bypass = config.getBypassPermission();
        if (bypass != null && !bypass.isEmpty() && player.hasPermission(bypass)) return true;

        long remaining = remainingMillis(config.getId(), player.getUniqueId());
        if (remaining > 0L) {
            notifyBlocked(player, config, remaining);
            return false;
        }

        apply(config.getId(), player.getUniqueId(), config.getDurationMillis());
        return true;
    }

    private void notifyBlocked(Player player, CooldownConfig config, long remainingMillis) {
        long seconds = (remainingMillis + 999L) / 1000L;
        long minutes = (seconds + 59L) / 60L;
        String formatted = formatTime(seconds);

        String message = applyPlaceholders(config.getMessage(), formatted, seconds, minutes);
        if (message != null && !message.isEmpty()) {
            if (config.isActionBar()) {
                ((Audience) player).sendActionBar(ColorUtil.colorComponent(message));
            } else {
                player.sendMessage(ColorUtil.color(message));
            }
        }

        String title = applyPlaceholders(config.getTitle(), formatted, seconds, minutes);
        String subtitle = applyPlaceholders(config.getSubtitle(), formatted, seconds, minutes);
        if ((title != null && !title.isEmpty()) || (subtitle != null && !subtitle.isEmpty())) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Title adventureTitle = Title.title(
                    legacy.deserialize(ColorUtil.color(title == null ? "" : title)),
                    legacy.deserialize(ColorUtil.color(subtitle == null ? "" : subtitle)),
                    Title.Times.times(
                            Duration.ofMillis(config.getFadeIn() * 50L),
                            Duration.ofMillis(config.getStay() * 50L),
                            Duration.ofMillis(config.getFadeOut() * 50L)
                    )
            );
            ((Audience) player).showTitle(adventureTitle);
        }

        String soundName = config.getSound();
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase(java.util.Locale.ENGLISH));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[SmartMenus] Unknown cooldown sound: " + soundName);
            }
        }
    }

    private String applyPlaceholders(String text, String formatted, long seconds, long minutes) {
        if (text == null || text.isEmpty()) return text;
        return text
                .replace("%time_left%", formatted)
                .replace("%time%", formatted)
                .replace("%seconds%", String.valueOf(seconds))
                .replace("%minutes%", String.valueOf(minutes));
    }

    public static String formatTime(long totalSeconds) {
        if (totalSeconds <= 0L) return "0s";
        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        if (seconds > 0) builder.append(seconds).append("s");
        return builder.toString().trim();
    }
}
