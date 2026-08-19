package com.oreo.gui;

import com.oreo.SmartMenus;
import com.oreo.bedrock.BedrockManager;
import com.oreo.condition.Condition;
import org.bukkit.entity.Player;

public final class GuiOpener {

    private GuiOpener() {
    }

    public static boolean open(SmartMenus plugin, Player player, GuiDefinition def,
                               boolean checkRequirements, boolean checkCooldown, boolean allowAutoConvert) {
        if (def == null) return false;

        if (checkRequirements) {
            for (Condition req : def.getOpenRequirements()) {
                if (!req.check(player)) {
                    player.sendMessage(req.getErrorMessage(player));
                    return false;
                }
            }
        }

        if (checkCooldown && plugin.getCooldownManager() != null
                && !plugin.getCooldownManager().tryUse(player, def.getOpenCooldown())) {
            return false;
        }

        BedrockManager bm = plugin.getBedrockManager();
        if (bm != null) {
            if (bm.openForBedrock(player, def)) return true;
            if (allowAutoConvert && bm.autoConvertForBedrock(player, def)) return true;
        }

        def.createInventory(plugin.getInventoryManager(), plugin).open(player);
        return true;
    }
}
