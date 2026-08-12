package com.oreo.command;

import com.oreo.SmartMenus;
import com.oreo.gui.GuiDefinition;
import com.oreo.util.ColorUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class OGUICommand implements CommandExecutor, TabCompleter {

    private final SmartMenus plugin;

    public OGUICommand(SmartMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.color("&e&lSmart Menus"));
            sender.sendMessage(ColorUtil.color("&7Usage: &f/smartmenus <open|reload|captureitem|editor|itemlevel> [args]"));
            sender.sendMessage(ColorUtil.color("&7Examples:"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus open shop"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus open shop PlayerName"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus reload"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus captureitem shop 13 &7— capture held item into slot 13 of 'shop'"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus itemlevel give <player> <item_id> <level>"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus itemlevel set  <player> <item_id> <level>"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus itemlevel check [player]"));
            sender.sendMessage(ColorUtil.color("  &f/smartmenus itemlevel list"));
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ENGLISH);

        if (subCommand.equals("reload")) {
            if (!hasAnyPermission(sender, "smartmenus.reload", "ogui.reload")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission to reload GUIs."));
                return true;
            }

            plugin.reloadGuis();
            sender.sendMessage(ColorUtil.color("&a✔ Smart Menus reloaded successfully!"));
            sender.sendMessage(ColorUtil.color("&7Loaded &f" + plugin.getGuiRegistry().getGuiIds().size() + " &7GUI(s)"));
            return true;
        }

        if (subCommand.equals("open")) {
            if (!hasAnyPermission(sender, "smartmenus.open", "ogui.open")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission to open GUIs."));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ColorUtil.color("&cUsage: /smartmenus open <id> [player]"));
                sender.sendMessage(ColorUtil.color("&7Available GUIs: &f" + String.join(", ", plugin.getGuiRegistry().getGuiIds())));
                return true;
            }

            String id = args[1];
            GuiDefinition definition = plugin.getGuiRegistry().getGui(id);

            if (definition == null) {
                sender.sendMessage(ColorUtil.color("&cUnknown GUI id: &f" + id));
                sender.sendMessage(ColorUtil.color("&7Available GUIs: &f" + String.join(", ", plugin.getGuiRegistry().getGuiIds())));
                return true;
            }

            Player target;
            if (args.length >= 3) {
                target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cPlayer not found: &f" + args[2]));
                    return true;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(ColorUtil.color("&cYou must specify a player from console."));
                return true;
            }

            com.oreo.bedrock.BedrockManager bm = plugin.getBedrockManager();
            if (bm == null
                    || (!bm.openForBedrock(target, definition)
                        && !bm.autoConvertForBedrock(target, definition))) {
                definition.createInventory(plugin.getInventoryManager(), plugin).open(target);
            }

            if (!target.equals(sender)) {
                sender.sendMessage(ColorUtil.color("&aOpened GUI &f" + id + " &afor &f" + target.getName()));
            }

            return true;
        }

        if (subCommand.equals("captureitem")) {
            if (!hasAnyPermission(sender, "smartmenus.captureitem", "ogui.captureitem")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.color("&cThis command can only be used by a player."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.color("&cUsage: /smartmenus captureitem <gui_id> <slot> [item_key]"));
                return true;
            }

            Player player = (Player) sender;
            String guiId = args[1];
            int slot;
            try {
                slot = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.color("&cSlot must be a number."));
                return true;
            }

            GuiDefinition definition = plugin.getGuiRegistry().getGui(guiId);
            if (definition == null) {
                sender.sendMessage(ColorUtil.color("&cUnknown GUI: &f" + guiId));
                return true;
            }

            File guiFile = plugin.getGuiRegistry().getGuiFile(guiId);
            if (guiFile == null) {
                sender.sendMessage(ColorUtil.color("&cCannot find the file for GUI: &f" + guiId));
                return true;
            }

            ItemStack held = player.getInventory().getItemInMainHand();
            if (held == null || held.getType().isAir()) {
                sender.sendMessage(ColorUtil.color("&cHold the item you want to capture in your main hand."));
                return true;
            }

            String itemKey = args.length >= 4 ? args[3] : "captured_slot_" + slot;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);

            String basePath = config.isConfigurationSection("guis")
                    ? "guis." + guiId + ".items." + itemKey
                    : guiId + ".items." + itemKey;

            config.set(basePath + ".slot", slot);
            config.set(basePath + ".material", held.getType().name());

            ItemMeta meta = held.getItemMeta();
            if (meta != null) {

                if (meta.hasDisplayName()) {
                    config.set(basePath + ".name", meta.getDisplayName().replace('§', '&'));
                }

                if (meta.hasLore() && meta.getLore() != null) {
                    List<String> lore = new ArrayList<>();
                    for (String line : meta.getLore()) {
                        lore.add(line.replace('§', '&'));
                    }
                    config.set(basePath + ".lore", lore);
                }

                if (meta.hasCustomModelData()) {
                    config.set(basePath + ".custom_model_data", meta.getCustomModelData());
                }

                if (!meta.getItemFlags().isEmpty()) {
                    List<String> flags = new ArrayList<>();
                    for (ItemFlag flag : meta.getItemFlags()) {
                        flags.add(flag.name());
                    }
                    config.set(basePath + ".item_flags", flags);
                }

                Map<Enchantment, Integer> enchants = meta.getEnchants();
                if (!enchants.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        config.set(basePath + ".enchantments." + entry.getKey().getKey().getKey(), entry.getValue());
                    }
                    config.set(basePath + ".glow", false);
                } else {

                    config.set(basePath + ".glow", false);
                }

                if (meta instanceof EnchantmentStorageMeta) {
                    EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
                    for (Map.Entry<Enchantment, Integer> entry : esm.getStoredEnchants().entrySet()) {
                        config.set(basePath + ".enchantments." + entry.getKey().getKey().getKey(), entry.getValue());
                    }
                }
            }

            try {
                config.save(guiFile);
                plugin.reloadGuis();
                sender.sendMessage(ColorUtil.color("&a✔ Item captured as &f'" + itemKey + "'&a in GUI &f'" + guiId + "'&a at slot &f" + slot + "&a."));
                sender.sendMessage(ColorUtil.color("&7File: &f" + guiFile.getName()));
            } catch (IOException e) {
                sender.sendMessage(ColorUtil.color("&cFailed to save the GUI file: " + e.getMessage()));
                plugin.getLogger().severe("captureitem: failed to save " + guiFile.getName() + ": " + e.getMessage());
            }

            return true;
        }

        if (subCommand.equals("editor")) {
            if (!hasAnyPermission(sender, "smartmenus.editor", "ogui.editor")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission to use the GUI editor."));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.color("&cThe editor can only be used by players."));
                return true;
            }
            plugin.getEditorManager().openEditor((Player) sender);
            return true;
        }

        if (subCommand.equals("convert")) {
            if (!hasAnyPermission(sender, "smartmenus.convert", "ogui.convert")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission to convert GUIs."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.color("&e&lSmartMenus Converter"));
                sender.sendMessage(ColorUtil.color("&7Usage: &f/smartmenus convert <type> <file>"));
                sender.sendMessage(ColorUtil.color("&7Types: &fcp &8(CommandPanel) &7| &fdm &8(DeluxeMenus) &7| &fzm &8(ZMenus)"));
                sender.sendMessage(ColorUtil.color("&7Example: &f/smartmenus convert dm guide"));
                sender.sendMessage(ColorUtil.color("&7Inputs:  &fplugins/OGUI/converter/commandpanel/"));
                sender.sendMessage(ColorUtil.color("&7         &fplugins/OGUI/converter/deluxemenus/"));
                sender.sendMessage(ColorUtil.color("&7         &fplugins/OGUI/converter/zmenus/"));
                sender.sendMessage(ColorUtil.color("&7Outputs: &fplugins/OGUI/guis/converted/"));
                return true;
            }
            String convertType = args[1].toLowerCase(Locale.ENGLISH);
            String inputSubDir = switch (convertType) {
                case "cp", "commandpanel"  -> "converter/commandpanel";
                case "dm", "deluxemenus"   -> "converter/deluxemenus";
                case "zm", "zmenus"        -> "converter/zmenus";
                default -> null;
            };
            if (inputSubDir == null) {
                sender.sendMessage(ColorUtil.color("&cUnknown type: &f" + convertType
                        + " &c— use &fcp&c, &fdm&c, or &fzm"));
                return true;
            }

            String fileName = args[2];
            if (!fileName.endsWith(".yml")) fileName += ".yml";

            File inputDir  = new File(plugin.getDataFolder(), inputSubDir);
            File inputFile = new File(inputDir, fileName);
            if (!inputFile.exists()) {
                sender.sendMessage(ColorUtil.color("&c\u2717 File not found: &f" + inputFile.getAbsolutePath()));
                sender.sendMessage(ColorUtil.color("&7Place your YAML files in: &f" + inputDir.getPath()));
                return true;
            }

            File outputDir = new File(plugin.getDataFolder(), "guis/converted");
            try {
                File outputFile = switch (convertType) {
                    case "cp", "commandpanel" ->
                            com.oreo.converter.CommandPanelConverter.convert(inputFile, outputDir, null);
                    case "dm", "deluxemenus"  ->
                            com.oreo.converter.DeluxeMenusConverter.convert(inputFile, outputDir, null);
                    case "zm", "zmenus"       ->
                            com.oreo.converter.ZMenusConverter.convert(inputFile, outputDir, null);
                    default -> throw new IOException("Unknown type");
                };
                plugin.reloadGuis();
                sender.sendMessage(ColorUtil.color("&a\u2714 Converted successfully!"));
                sender.sendMessage(ColorUtil.color("&7Output: &fplugins/OGUI/guis/converted/" + outputFile.getName()));
                sender.sendMessage(ColorUtil.color("&7Use &f/smartmenus open "
                        + outputFile.getName().replace(".yml", "") + " &7to test."));
            } catch (IOException e) {
                sender.sendMessage(ColorUtil.color("&c\u2717 Conversion failed: " + e.getMessage()));
                plugin.getLogger().severe("[Converter] Failed to convert " + fileName + ": " + e.getMessage());
            }
            return true;
        }

        if (subCommand.equals("itemlevel")) {
            if (!hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) {
                sender.sendMessage(ColorUtil.color("&cYou do not have permission to manage item levels."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.color("&e&lItem Level Commands"));
                sender.sendMessage(ColorUtil.color("&7/smartmenus itemlevel give <player> <item_id> <level>"));
                sender.sendMessage(ColorUtil.color("&7/smartmenus itemlevel set  <player> <item_id> <level>"));
                sender.sendMessage(ColorUtil.color("&7/smartmenus itemlevel check [player]"));
                sender.sendMessage(ColorUtil.color("&7/smartmenus itemlevel list"));
                return true;
            }

            String subop = args[1].toLowerCase(Locale.ENGLISH);

            if (subop.equals("list")) {
                sender.sendMessage(ColorUtil.color("&e&lRegistered Item Upgrades:"));
                for (String id : plugin.getItemLevelManager().getItemIds()) {
                    int min = plugin.getItemLevelManager().getMinLevel(id);
                    int max = plugin.getItemLevelManager().getMaxLevel(id);
                    sender.sendMessage(ColorUtil.color("  &f" + id + " &8— levels &f" + min + "&8-&f" + max));
                }
                return true;
            }

            if (subop.equals("check")) {
                Player target;
                if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        sender.sendMessage(ColorUtil.color("&cPlayer not found: &f" + args[2]));
                        return true;
                    }
                } else if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage(ColorUtil.color("&cYou must specify a player from console."));
                    return true;
                }
                ItemStack held = target.getInventory().getItemInMainHand();
                String heldId = plugin.getItemLevelManager().getItemId(held);
                if (heldId == null) {
                    sender.sendMessage(ColorUtil.color("&cThis item is not a SmartMenus leveled item."));
                    return true;
                }
                int heldLevel = plugin.getItemLevelManager().getItemLevel(held);
                int heldMax = plugin.getItemLevelManager().getMaxLevel(heldId);
                sender.sendMessage(ColorUtil.color("&e&lItem Level Info &8[" + target.getName() + "]"));
                sender.sendMessage(ColorUtil.color("  &7Item ID: &f" + heldId));
                sender.sendMessage(ColorUtil.color("  &7Level:   &f" + heldLevel + " &8/ &f" + heldMax));
                return true;
            }

            if (subop.equals("give") || subop.equals("set")) {
                if (args.length < 5) {
                    sender.sendMessage(ColorUtil.color("&cUsage: /smartmenus itemlevel " + subop + " <player> <item_id> <level>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cPlayer not found: &f" + args[2]));
                    return true;
                }
                String itemId = args[3];
                int level;
                try {
                    level = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.color("&cLevel must be a number."));
                    return true;
                }
                int max = plugin.getItemLevelManager().getMaxLevel(itemId);

                if (subop.equals("give")) {
                    boolean ok = plugin.getItemLevelManager().give(target, itemId, level);
                    if (!ok) {
                        sender.sendMessage(ColorUtil.color("&cUnknown item id '" + itemId + "' or level " + level + "."));
                        return true;
                    }
                    sender.sendMessage(ColorUtil.color("&a\u2714 Gave &f" + target.getName() + " &a\u2192 &f" + itemId + " &8[Level " + level + "/" + max + "]"));
                } else {
                    if (plugin.getItemLevelManager().getItem(itemId, level) == null) {
                        sender.sendMessage(ColorUtil.color("&cUnknown item id '" + itemId + "' or level " + level + "."));
                        return true;
                    }
                    boolean replaced = plugin.getItemLevelManager().set(target, itemId, level);
                    String action = replaced ? "Upgraded" : "Gave (not found in inventory)";
                    sender.sendMessage(ColorUtil.color("&a\u2714 " + action + ": &f" + target.getName() + " &a\u2192 &f" + itemId + " &8[Level " + level + "/" + max + "]"));
                }
                return true;
            }

            sender.sendMessage(ColorUtil.color("&cUnknown itemlevel subop: &f" + subop));
            sender.sendMessage(ColorUtil.color("&7Use: give, set, check, list"));
            return true;
        }

        sender.sendMessage(ColorUtil.color("&cUnknown subcommand: &f" + subCommand));
        sender.sendMessage(ColorUtil.color("&7Use &f/smartmenus &7for help"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (hasAnyPermission(sender, "smartmenus.open", "ogui.open")) {
                options.add("open");
            }
            if (hasAnyPermission(sender, "smartmenus.reload", "ogui.reload")) {
                options.add("reload");
            }
            if (hasAnyPermission(sender, "smartmenus.captureitem", "ogui.captureitem")) {
                options.add("captureitem");
            }
            if (hasAnyPermission(sender, "smartmenus.editor", "ogui.editor")) {
                options.add("editor");
            }
            if (hasAnyPermission(sender, "smartmenus.convert", "ogui.convert")) {
                options.add("convert");
            }
            if (hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) {
                options.add("itemlevel");
            }
            return filterOptions(options, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            if (!hasAnyPermission(sender, "smartmenus.open", "ogui.open")) {
                return Collections.emptyList();
            }
            return filterOptions(new ArrayList<>(plugin.getGuiRegistry().getGuiIds()), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            if (!hasAnyPermission(sender, "smartmenus.open", "ogui.open")) {
                return Collections.emptyList();
            }
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return filterOptions(players, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("captureitem")) {
            if (!hasAnyPermission(sender, "smartmenus.captureitem", "ogui.captureitem")) {
                return Collections.emptyList();
            }
            return filterOptions(new ArrayList<>(plugin.getGuiRegistry().getGuiIds()), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("captureitem")) {
            List<String> slots = new ArrayList<>();
            for (int i = 0; i <= 53; i++) slots.add(String.valueOf(i));
            return filterOptions(slots, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("convert")) {
            return filterOptions(List.of("cp", "dm", "zm"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("convert")) {
            if (!hasAnyPermission(sender, "smartmenus.convert", "ogui.convert")) return Collections.emptyList();
            String subDir = switch (args[1].toLowerCase(Locale.ENGLISH)) {
                case "dm", "deluxemenus" -> "converter/deluxemenus";
                case "zm", "zmenus"      -> "converter/zmenus";
                default                  -> "converter/commandpanel";
            };
            File inputDir = new File(plugin.getDataFolder(), subDir);
            List<String> files = new ArrayList<>();
            if (inputDir.exists() && inputDir.isDirectory()) {
                File[] ymlFiles = inputDir.listFiles((d, n) -> n.endsWith(".yml"));
                if (ymlFiles != null) {
                    for (File f : ymlFiles) files.add(f.getName().replace(".yml", ""));
                }
            }
            return filterOptions(files, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("itemlevel")) {
            if (!hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) return Collections.emptyList();
            return filterOptions(List.of("give", "set", "check", "list"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("itemlevel")) {
            if (!hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) return Collections.emptyList();
            String subop = args[1].toLowerCase(Locale.ENGLISH);
            if (subop.equals("give") || subop.equals("set") || subop.equals("check")) {
                List<String> players = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }
                return filterOptions(players, args[2]);
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("itemlevel")) {
            if (!hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) return Collections.emptyList();
            String subop = args[1].toLowerCase(Locale.ENGLISH);
            if (subop.equals("give") || subop.equals("set")) {
                return filterOptions(new ArrayList<>(plugin.getItemLevelManager().getItemIds()), args[3]);
            }
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("itemlevel")) {
            if (!hasAnyPermission(sender, "smartmenus.itemlevel", "ogui.itemlevel")) return Collections.emptyList();
            String subop = args[1].toLowerCase(Locale.ENGLISH);
            if (subop.equals("give") || subop.equals("set")) {
                String itemId = args[3];
                List<String> levels = new ArrayList<>();
                for (int lvl : plugin.getItemLevelManager().getLevels(itemId)) {
                    levels.add(String.valueOf(lvl));
                }
                return filterOptions(levels, args[4]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterOptions(List<String> options, String input) {
        if (input.isEmpty()) {
            return options;
        }

        List<String> filtered = new ArrayList<>();
        String lowerInput = input.toLowerCase(Locale.ENGLISH);

        for (String option : options) {
            if (option.toLowerCase(Locale.ENGLISH).startsWith(lowerInput)) {
                filtered.add(option);
            }
        }

        return filtered;
    }

    private boolean hasAnyPermission(CommandSender sender, String primary, String legacy) {
        return sender.hasPermission(primary) || sender.hasPermission(legacy);
    }
}
