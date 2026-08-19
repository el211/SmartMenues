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

    private final com.oreo.util.CooldownConfig cooldown;

    private GuiItem(
            int slot, String material, String name, List<String> lore, List<String> commands,
            boolean closeOnClick, double price, String requirement, List<Condition> conditions,
            String itemType, String itemId, Integer customModelData,
            Map<String, List<Action>> clickActions,
            List<Condition> viewRequirements, GuiItem elseItem,
            ButtonType buttonType, String chatInputType, String chatPromptMessage, String chatCancelWord,
            boolean glow, List<String> itemFlags, Map<String, Integer> enchantments,
            boolean autoUpdate, int updateInterval, boolean permanent,
            DynamicSource dynamicSource, boolean takeItem, boolean giveItem, int amount,
            List<String> onPlaceCommands, List<Action> onPlaceActions, boolean removeOnPlace,
            com.oreo.util.CooldownConfig cooldown
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
        this.cooldown = cooldown;
    }

    public static final class Builder {
        private int slot = 0;
        private String material = "STONE";
        private String name = "";
        private List<String> lore = Collections.emptyList();
        private List<String> commands = Collections.emptyList();
        private boolean closeOnClick = false;
        private double price = 0;
        private String requirement = "";
        private List<Condition> conditions = Collections.emptyList();
        private String itemType = "vanilla";
        private String itemId = null;
        private Integer customModelData = null;
        private Map<String, List<Action>> clickActions = new HashMap<>();
        private List<Condition> viewRequirements = Collections.emptyList();
        private GuiItem elseItem = null;
        private ButtonType buttonType = ButtonType.NONE;
        private String chatInputType = "TEXT";
        private String chatPromptMessage = "";
        private String chatCancelWord = "cancel";
        private boolean glow = false;
        private List<String> itemFlags = Collections.emptyList();
        private Map<String, Integer> enchantments = Collections.emptyMap();
        private boolean autoUpdate = false;
        private int updateInterval = 20;
        private boolean permanent = false;
        private DynamicSource dynamicSource = DynamicSource.NONE;
        private boolean takeItem = false;
        private boolean giveItem = false;
        private int amount = 1;
        private List<String> onPlaceCommands = Collections.emptyList();
        private List<Action> onPlaceActions = Collections.emptyList();
        private boolean removeOnPlace = true;
        private com.oreo.util.CooldownConfig cooldown = null;

        public Builder slot(int slot) { this.slot = slot; return this; }
        public Builder material(String material) { this.material = material; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder lore(List<String> lore) { this.lore = lore; return this; }
        public Builder commands(List<String> commands) { this.commands = commands; return this; }
        public Builder closeOnClick(boolean v) { this.closeOnClick = v; return this; }
        public Builder price(double price) { this.price = price; return this; }
        public Builder requirement(String req) { this.requirement = req; return this; }
        public Builder conditions(List<Condition> conditions) { this.conditions = conditions; return this; }
        public Builder itemType(String itemType) { this.itemType = itemType; return this; }
        public Builder itemId(String itemId) { this.itemId = itemId; return this; }
        public Builder customModelData(Integer cmd) { this.customModelData = cmd; return this; }
        public Builder clickActions(Map<String, List<Action>> clickActions) { this.clickActions = clickActions; return this; }
        public Builder viewRequirements(List<Condition> viewReqs) { this.viewRequirements = viewReqs; return this; }
        public Builder elseItem(GuiItem elseItem) { this.elseItem = elseItem; return this; }
        public Builder buttonType(ButtonType buttonType) { this.buttonType = buttonType; return this; }
        public Builder chatInputType(String chatInputType) { this.chatInputType = chatInputType; return this; }
        public Builder chatPromptMessage(String chatPromptMessage) { this.chatPromptMessage = chatPromptMessage; return this; }
        public Builder chatCancelWord(String chatCancelWord) { this.chatCancelWord = chatCancelWord; return this; }
        public Builder glow(boolean glow) { this.glow = glow; return this; }
        public Builder itemFlags(List<String> itemFlags) { this.itemFlags = itemFlags; return this; }
        public Builder enchantments(Map<String, Integer> enchantments) { this.enchantments = enchantments; return this; }
        public Builder autoUpdate(boolean autoUpdate) { this.autoUpdate = autoUpdate; return this; }
        public Builder updateInterval(int updateInterval) { this.updateInterval = updateInterval; return this; }
        public Builder permanent(boolean permanent) { this.permanent = permanent; return this; }
        public Builder dynamicSource(DynamicSource dynamicSource) { this.dynamicSource = dynamicSource; return this; }
        public Builder takeItem(boolean takeItem) { this.takeItem = takeItem; return this; }
        public Builder giveItem(boolean giveItem) { this.giveItem = giveItem; return this; }
        public Builder amount(int amount) { this.amount = amount; return this; }
        public Builder onPlaceCommands(List<String> onPlaceCommands) { this.onPlaceCommands = onPlaceCommands; return this; }
        public Builder onPlaceActions(List<Action> onPlaceActions) { this.onPlaceActions = onPlaceActions; return this; }
        public Builder removeOnPlace(boolean removeOnPlace) { this.removeOnPlace = removeOnPlace; return this; }
        public Builder cooldown(com.oreo.util.CooldownConfig cooldown) { this.cooldown = cooldown; return this; }

        public GuiItem build() {
            return new GuiItem(slot, material, name, lore, commands, closeOnClick, price, requirement,
                    conditions, itemType, itemId, customModelData, clickActions, viewRequirements, elseItem,
                    buttonType, chatInputType, chatPromptMessage, chatCancelWord, glow, itemFlags, enchantments,
                    autoUpdate, updateInterval, permanent, dynamicSource, takeItem, giveItem, amount,
                    onPlaceCommands, onPlaceActions, removeOnPlace, cooldown);
        }
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
    public com.oreo.util.CooldownConfig getCooldown() { return cooldown; }

    @Override
    public String toString() {
        return "GuiItem{slot=" + slot + ", material='" + material + "', itemType='" + itemType
                + "', conditions=" + conditions.size() + ", buttonType=" + buttonType + '}';
    }
}
