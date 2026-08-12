package com.oreo.items;

import com.oreo.SmartMenus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class DefaultItemProvider implements ItemProvider {

    private final SmartMenus plugin;

    private Object currencyService;
    private Object warpService;
    private Object economyService;

    public DefaultItemProvider(SmartMenus plugin) {
        this.plugin = plugin;
        reloadHooks();
    }

    public void reloadHooks() {
        Plugin oreo = Bukkit.getPluginManager().getPlugin("OreoEssentials");
        if (oreo == null || !oreo.isEnabled()) {
            currencyService = null;
            warpService = null;
            economyService = null;
            plugin.getLogger().info("[SmartMenus] OreoEssentials not found (or not enabled). Hooks cleared.");
            return;
        }

        Object cs = null;
        Object ws = null;
        Object es = null;

        try {
            @SuppressWarnings("unchecked")
            Class<Object> csClass = (Class<Object>) Class.forName("fr.elias.oreoEssentials.modules.currency.CurrencyService");
            cs = Bukkit.getServicesManager().load(csClass);
        } catch (Throwable ignored) {}
        try {
            @SuppressWarnings("unchecked")
            Class<Object> wsClass = (Class<Object>) Class.forName("fr.elias.oreoEssentials.modules.warps.WarpService");
            ws = Bukkit.getServicesManager().load(wsClass);
        } catch (Throwable ignored) {}
        try {
            @SuppressWarnings("unchecked")
            Class<Object> esClass = (Class<Object>) Class.forName("fr.elias.oreoEssentials.modules.economy.EconomyService");
            es = Bukkit.getServicesManager().load(esClass);
        } catch (Throwable ignored) {}

        if (cs == null) cs = tryGetter(oreo, "getCurrencyService");
        if (ws == null) ws = tryGetter(oreo, "getWarpService");
        if (es == null) es = tryGetter(oreo, "getEconomyService");

        currencyService = cs;
        warpService = ws;
        economyService = es;

        plugin.getLogger().info("[SmartMenus] OreoEssentials detected. Hooks: "
                + "Currency=" + (currencyService != null)
                + ", Warps=" + (warpService != null)
                + ", Economy=" + (economyService != null));
    }

    private Object tryGetter(Plugin plugin, String methodName) {
        try {
            Method m = plugin.getClass().getMethod(methodName);
            m.setAccessible(true);
            return m.invoke(plugin);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Throwable t) {
            this.plugin.getLogger().log(Level.WARNING,
                    "[SmartMenus] Failed calling " + plugin.getName() + "." + methodName + "()", t);
            return null;
        }
    }

    public boolean hasOreoCurrency() { return currencyService != null; }
    public boolean hasOreoWarps() { return warpService != null; }
    public boolean hasOreoEconomy() { return economyService != null; }

    public Object getCurrencyService() { return currencyService; }
    public Object getWarpService() { return warpService; }
    public Object getEconomyService() { return economyService; }

    @Override
    public ItemStack getItem(String material, String itemType, Integer customModelData) {
        if (itemType == null || itemType.equalsIgnoreCase("vanilla")) {
            return getVanillaItem(material, customModelData);
        } else if (itemType.equalsIgnoreCase("itemsadder")) {
            return getItemsAdderItem(material);
        } else if (itemType.equalsIgnoreCase("nexo")) {
            return getNexoItem(material);
        }

        plugin.getLogger().warning("Unknown item type: " + itemType + ", falling back to vanilla");
        return getVanillaItem(material, customModelData);
    }

    @Override
    public boolean isAvailable(String itemType) {
        if (itemType == null || itemType.equalsIgnoreCase("vanilla")) {
            return true;
        } else if (itemType.equalsIgnoreCase("itemsadder")) {
            return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        } else if (itemType.equalsIgnoreCase("nexo")) {
            return Bukkit.getPluginManager().getPlugin("Nexo") != null;
        }
        return false;
    }

    private ItemStack getVanillaItem(String materialName, Integer customModelData) {
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) {
            plugin.getLogger().warning("Invalid material: " + materialName);
            return new ItemStack(Material.STONE);
        }

        ItemStack item = new ItemStack(mat);

        if (customModelData != null && customModelData > 0) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    private ItemStack getItemsAdderItem(String itemId) {
        if (!isAvailable("itemsadder")) {
            plugin.getLogger().warning("ItemsAdder is not available!");
            return new ItemStack(Material.STONE);
        }

        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, itemId);

            if (customStack == null) {
                plugin.getLogger().warning("ItemsAdder item not found: " + itemId);
                return new ItemStack(Material.STONE);
            }

            return (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get ItemsAdder item: " + itemId + " - " + e.getMessage());
            return new ItemStack(Material.STONE);
        }
    }

    private ItemStack getNexoItem(String itemId) {
        if (!isAvailable("nexo")) {
            plugin.getLogger().warning("Nexo is not available!");
            return new ItemStack(Material.STONE);
        }

        try {
            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object itemBuilder = nexoItemsClass.getMethod("itemFromId", String.class).invoke(null, itemId);

            if (itemBuilder == null) {
                plugin.getLogger().warning("Nexo item not found: " + itemId);
                return new ItemStack(Material.STONE);
            }

            return (ItemStack) itemBuilder.getClass().getMethod("build").invoke(itemBuilder);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get Nexo item: " + itemId + " - " + e.getMessage());
            return new ItemStack(Material.STONE);
        }
    }
}
