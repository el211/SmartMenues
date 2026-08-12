package com.oreo.gui;

import com.oreo.SmartMenus;
import com.oreo.action.Action;
import com.oreo.bedrock.BedrockFormDefinition;
import com.oreo.condition.Condition;
import com.oreo.util.ColorUtil;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.event.inventory.InventoryType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiDefinition {

    public static class ArgDefinition {
        public final String name;
        public final String type;
        public final boolean required;
        public final String defaultValue;

        public ArgDefinition(String name, String type, boolean required, String defaultValue) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }

    private final String id;
    private final String title;
    private final int rows;
    private final Map<Integer, GuiItem> items;
    private final List<String> commands;
    private final Integer npcId;
    private final GuiItem fillItem;
    private final InventoryType inventoryType;
    private final List<ArgDefinition> commandArgs;
    private final boolean useBottomInventory;

    private final Map<Integer, GuiItem> bottomItems;

    private final BedrockFormDefinition bedrockDefinition;

    private final boolean bedrockAutoConvert;

    private final List<Condition> openRequirements;
    private final List<Action> openActions;
    private final List<Action> closeActions;

    private final String openSound;
    private final float openSoundVolume;
    private final float openSoundPitch;

    private final BottomInventoryMode bottomInventoryMode;

    @Deprecated
    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items, List<String> commands) {
        this(id, title, rows, items, commands, null);
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items, List<String> commands, Integer npcId) {
        this(id, title, rows, items, commands, npcId, null, null, Collections.emptyList());
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                false, Collections.emptyMap());
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                useBottomInventory, bottomItems, null, false);
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems,
                         BedrockFormDefinition bedrockDefinition) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                useBottomInventory, bottomItems, bedrockDefinition, false);
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems,
                         BedrockFormDefinition bedrockDefinition, boolean bedrockAutoConvert) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                useBottomInventory, bottomItems, bedrockDefinition, bedrockAutoConvert,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems,
                         BedrockFormDefinition bedrockDefinition, boolean bedrockAutoConvert,
                         List<Condition> openRequirements, List<Action> openActions, List<Action> closeActions) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                useBottomInventory, bottomItems, bedrockDefinition, bedrockAutoConvert,
                openRequirements, openActions, closeActions, null, 1.0f, 1.0f);
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems,
                         BedrockFormDefinition bedrockDefinition, boolean bedrockAutoConvert,
                         List<Condition> openRequirements, List<Action> openActions, List<Action> closeActions,
                         String openSound, float openSoundVolume, float openSoundPitch) {
        this(id, title, rows, items, commands, npcId, fillItem, inventoryType, commandArgs,
                useBottomInventory, bottomItems, bedrockDefinition, bedrockAutoConvert,
                openRequirements, openActions, closeActions, openSound, openSoundVolume, openSoundPitch,
                BottomInventoryMode.DEFAULT);
    }

    public GuiDefinition(String id, String title, int rows, Map<Integer, GuiItem> items,
                         List<String> commands, Integer npcId,
                         GuiItem fillItem, InventoryType inventoryType,
                         List<ArgDefinition> commandArgs,
                         boolean useBottomInventory, Map<Integer, GuiItem> bottomItems,
                         BedrockFormDefinition bedrockDefinition, boolean bedrockAutoConvert,
                         List<Condition> openRequirements, List<Action> openActions, List<Action> closeActions,
                         String openSound, float openSoundVolume, float openSoundPitch,
                         BottomInventoryMode bottomInventoryMode) {
        this.id = id;
        this.title = title;
        this.rows = rows;
        this.items = items;
        this.commands = commands != null ? commands : Collections.emptyList();
        this.npcId = npcId;
        this.fillItem = fillItem;
        this.inventoryType = inventoryType != null ? inventoryType : InventoryType.CHEST;
        this.commandArgs = commandArgs != null ? commandArgs : Collections.emptyList();
        this.useBottomInventory = useBottomInventory;
        this.bottomItems = bottomItems != null ? bottomItems : Collections.emptyMap();
        this.bedrockDefinition = bedrockDefinition;
        this.bedrockAutoConvert = bedrockAutoConvert;
        this.openRequirements = openRequirements != null ? openRequirements : Collections.emptyList();
        this.openActions = openActions != null ? openActions : Collections.emptyList();
        this.closeActions = closeActions != null ? closeActions : Collections.emptyList();
        this.openSound = openSound;
        this.openSoundVolume = openSoundVolume;
        this.openSoundPitch = openSoundPitch;
        this.bottomInventoryMode = bottomInventoryMode != null ? bottomInventoryMode : BottomInventoryMode.DEFAULT;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getRows() { return rows; }
    public Map<Integer, GuiItem> getItems() { return Collections.unmodifiableMap(items); }
    public List<String> getCommands() { return Collections.unmodifiableList(commands); }
    public Integer getNpcId() { return npcId; }
    public GuiItem getFillItem() { return fillItem; }
    public InventoryType getInventoryType() { return inventoryType; }
    public List<ArgDefinition> getCommandArgs() { return Collections.unmodifiableList(commandArgs); }
    public boolean isUseBottomInventory() { return useBottomInventory; }
    public Map<Integer, GuiItem> getBottomItems() { return Collections.unmodifiableMap(bottomItems); }

    public BedrockFormDefinition getBedrockDefinition() { return bedrockDefinition; }
    public boolean hasBedrockForm() { return bedrockDefinition != null; }
    public boolean isBedrockAutoConvert() { return bedrockAutoConvert; }

    public List<Condition> getOpenRequirements() { return Collections.unmodifiableList(openRequirements); }
    public List<Action> getOpenActions() { return Collections.unmodifiableList(openActions); }
    public List<Action> getCloseActions() { return Collections.unmodifiableList(closeActions); }
    public String getOpenSound() { return openSound; }
    public float getOpenSoundVolume() { return openSoundVolume; }
    public float getOpenSoundPitch() { return openSoundPitch; }
    public BottomInventoryMode getBottomInventoryMode() { return bottomInventoryMode; }

    public boolean hasNpcBinding() { return npcId != null; }
    public boolean isNpcBound(int checkNpcId) { return npcId != null && npcId == checkNpcId; }

    public SmartInventory createInventory(InventoryManager manager, SmartMenus plugin) {
        SmartInventory.Builder builder = SmartInventory.builder()
                .id("smartmenus:" + id)
                .provider(new GuiInventoryProvider(this, plugin, plugin.getItemProvider()))
                .title(ColorUtil.color(title))
                .manager(manager);

        if (inventoryType == InventoryType.CHEST) {
            builder.size(rows, 9);
        } else {

            builder.type(inventoryType);
        }

        return builder.build();
    }
}
