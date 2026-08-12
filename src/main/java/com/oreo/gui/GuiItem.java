package com.oreo.gui;

import com.oreo.action.Action;
import com.oreo.condition.Condition;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiItem {

    private final int slot;
    private final String material;
    private final String name;
    private final List<String> lore;
    private final List<String> commands;
    private final boolean closeOnClick;

    @Deprecated private final double price;
    @Deprecated private final String requirement;

    private final List<Condition> conditions;
    private final String itemType;
    private final String itemId;
    private final Integer customModelData;

    private final Map<String, List<Action>> clickActions;

    private final List<Condition> viewRequirements;
    private final GuiItem elseItem;

    private final ButtonType buttonType;
    private final String chatInputType;
    private final String chatPromptMessage;
    private final String chatCancelWord;

    private final boolean glow;
    private final List<String> itemFlags;
    private final Map<String, Integer> enchantments;

    private final boolean autoUpdate;
    private final int updateInterval;

    private final boolean permanent;
    private final DynamicSource dynamicSource;
    private final boolean takeItem;
    private final boolean giveItem;

    private final int amount;

    private final List<String> onPlaceCommands;
    private final List<Action> onPlaceActions;
    private final boolean removeOnPlace;

    @Deprecated
    public GuiItem(int slot, String material, String name, List<String> lore, List<String> commands,
                   boolean closeOnClick, double price, String requirement) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement,
                Collections.emptyList(), "vanilla", null, null);
    }

    public GuiItem(int slot, String material, String name, List<String> lore, List<String> commands,
                   boolean closeOnClick, double price, String requirement, List<Condition> conditions,
                   String itemType, String itemId, Integer customModelData) {
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.commands = commands == null ? Collections.emptyList() : commands;
        this.closeOnClick = closeOnClick;
        this.price = price;
        this.requirement = requirement;
        this.conditions = conditions == null ? Collections.emptyList() : conditions;
        this.itemType = itemType == null ? "vanilla" : itemType;
        this.itemId = itemId;
        this.customModelData = customModelData;
        this.clickActions = new HashMap<>();
        this.viewRequirements = Collections.emptyList();
        this.elseItem = null;
        this.buttonType = ButtonType.NONE;
        this.chatInputType = "TEXT";
        this.chatPromptMessage = "";
        this.chatCancelWord = "cancel";
        this.glow = false;
        this.itemFlags = Collections.emptyList();
        this.enchantments = Collections.emptyMap();
        this.autoUpdate = false;
        this.updateInterval = 20;
        this.permanent = false;
        this.dynamicSource = DynamicSource.NONE;
        this.takeItem = false;
        this.giveItem = false;
        this.amount = 1;
        this.onPlaceCommands = Collections.emptyList();
        this.onPlaceActions = Collections.emptyList();
        this.removeOnPlace = true;
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent
    ) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement, conditions, itemType,
                itemId, customModelData, clickActions, viewRequirements, elseItem, buttonType, chatInputType,
                chatPromptMessage, chatCancelWord, glow, itemFlags, enchantments, autoUpdate, updateInterval,
                permanent, DynamicSource.NONE, false, false);
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem
    ) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement, conditions, itemType,
                itemId, customModelData, clickActions, viewRequirements, elseItem, buttonType, chatInputType,
                chatPromptMessage, chatCancelWord, glow, itemFlags, enchantments, autoUpdate, updateInterval,
                permanent, dynamicSource, takeItem, false);
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem, boolean giveItem
    ) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement, conditions,
                itemType, itemId, customModelData, clickActions, viewRequirements, elseItem,
                buttonType, chatInputType, chatPromptMessage, chatCancelWord,
                glow, itemFlags, enchantments, autoUpdate, updateInterval, permanent,
                dynamicSource, takeItem, giveItem, 1, Collections.emptyList(), true);
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem, boolean giveItem, int amount
    ) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement, conditions,
                itemType, itemId, customModelData, clickActions, viewRequirements, elseItem,
                buttonType, chatInputType, chatPromptMessage, chatCancelWord,
                glow, itemFlags, enchantments, autoUpdate, updateInterval, permanent,
                dynamicSource, takeItem, giveItem, amount, Collections.emptyList(), true);
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem, boolean giveItem, int amount,
            List<String> onPlaceCommands, boolean removeOnPlace
    ) {
        this(slot, material, name, lore, commands, closeOnClick, price, requirement, conditions,
                itemType, itemId, customModelData, clickActions, viewRequirements, elseItem,
                buttonType, chatInputType, chatPromptMessage, chatCancelWord,
                glow, itemFlags, enchantments, autoUpdate, updateInterval, permanent,
                dynamicSource, takeItem, giveItem, amount,
                onPlaceCommands, Collections.emptyList(), removeOnPlace);
    }

    public GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem, boolean giveItem, int amount,
            List<String> onPlaceCommands, List<Action> onPlaceActions, boolean removeOnPlace
    ) {
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.commands = commands == null ? Collections.emptyList() : commands;
        this.closeOnClick = closeOnClick;
        this.price = price;
        this.requirement = requirement;
        this.conditions = conditions == null ? Collections.emptyList() : conditions;
        this.itemType = itemType == null ? "vanilla" : itemType;
        this.itemId = itemId;
        this.customModelData = customModelData;
        this.clickActions = clickActions == null ? new HashMap<>() : clickActions;
        this.viewRequirements = viewRequirements == null ? Collections.emptyList() : viewRequirements;
        this.elseItem = elseItem;
        this.buttonType = buttonType == null ? ButtonType.NONE : buttonType;
        this.chatInputType = chatInputType == null ? "TEXT" : chatInputType;
        this.chatPromptMessage = chatPromptMessage == null ? "" : chatPromptMessage;
        this.chatCancelWord = chatCancelWord == null ? "cancel" : chatCancelWord;
        this.glow = glow;
        this.itemFlags = itemFlags == null ? Collections.emptyList() : itemFlags;
        this.enchantments = enchantments == null ? Collections.emptyMap() : enchantments;
        this.autoUpdate = autoUpdate;
        this.updateInterval = updateInterval;
        this.permanent = permanent;
        this.dynamicSource = dynamicSource == null ? DynamicSource.NONE : dynamicSource;
        this.takeItem = takeItem;
        this.giveItem = giveItem;
        this.amount = Math.max(1, amount);
        this.onPlaceCommands = onPlaceCommands == null ? Collections.emptyList() : onPlaceCommands;
        this.onPlaceActions = onPlaceActions == null ? Collections.emptyList() : onPlaceActions;
        this.removeOnPlace = removeOnPlace;
    }

    public int getSlot() { return slot; }
    public String getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return Collections.unmodifiableList(lore); }
    public List<String> getCommands() { return Collections.unmodifiableList(commands); }
    public boolean isCloseOnClick() { return closeOnClick; }

    @Deprecated public double getPrice() { return price; }
    @Deprecated public String getRequirement() { return requirement; }
    @Deprecated public boolean hasPrice() { return price > 0; }
    @Deprecated public boolean hasRequirement() { return requirement != null && !requirement.isEmpty(); }

    public List<Condition> getConditions() { return Collections.unmodifiableList(conditions); }
    public boolean hasConditions() { return !conditions.isEmpty(); }
    public String getItemType() { return itemType; }
    public String getItemId() { return itemId; }
    public Integer getCustomModelData() { return customModelData; }

    public boolean isCustomItem() {
        return "itemsadder".equalsIgnoreCase(itemType) || "nexo".equalsIgnoreCase(itemType);
    }

    public boolean isVanillaItem() { return "vanilla".equalsIgnoreCase(itemType); }

    public Map<String, List<Action>> getClickActions() { return Collections.unmodifiableMap(clickActions); }

    public List<Action> getActionsForClick(String clickKey) {

        if (clickActions.containsKey(clickKey)) return clickActions.get(clickKey);
        if (clickActions.containsKey("ANY")) return clickActions.get("ANY");
        if (clickActions.containsKey("LEGACY")) return clickActions.get("LEGACY");
        return Collections.emptyList();
    }

    public List<Condition> getViewRequirements() { return Collections.unmodifiableList(viewRequirements); }
    public GuiItem getElseItem() { return elseItem; }

    public ButtonType getButtonType() { return buttonType; }
    public String getChatInputType() { return chatInputType; }
    public String getChatPromptMessage() { return chatPromptMessage; }
    public String getChatCancelWord() { return chatCancelWord; }

    public boolean isGlow() { return glow; }
    public List<String> getItemFlags() { return Collections.unmodifiableList(itemFlags); }
    public Map<String, Integer> getEnchantments() { return Collections.unmodifiableMap(enchantments); }

    public boolean isAutoUpdate() { return autoUpdate; }
    public int getUpdateInterval() { return updateInterval; }

    public boolean isPermanent() { return permanent; }
    public DynamicSource getDynamicSource() { return dynamicSource; }
    public boolean isTakeItem() { return takeItem; }
    public boolean isGiveItem() { return giveItem; }
    public int getAmount() { return amount; }

    public List<String> getOnPlaceCommands() { return Collections.unmodifiableList(onPlaceCommands); }
    public List<Action> getOnPlaceActions() { return Collections.unmodifiableList(onPlaceActions); }
    public boolean isRemoveOnPlace() { return removeOnPlace; }

    @Override
    public String toString() {
        return "GuiItem{" +
                "slot=" + slot +
                ", material='" + material + '\'' +
                ", itemType='" + itemType + '\'' +
                ", conditions=" + conditions.size() +
                ", buttonType=" + buttonType +
                '}';
    }
}
