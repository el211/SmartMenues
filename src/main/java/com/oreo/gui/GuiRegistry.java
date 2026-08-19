package com.oreo.gui;

import com.oreo.SmartMenus;
import com.oreo.action.Action;
import com.oreo.action.ActionFactory;
import com.oreo.util.Ids;
import com.oreo.bedrock.BedrockButton;
import com.oreo.bedrock.BedrockFormDefinition;
import com.oreo.bedrock.BedrockFormInput;
import com.oreo.condition.Condition;
import com.oreo.condition.ConditionFactory;
import com.oreo.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class GuiRegistry {

    private final SmartMenus plugin;
    private final Map<String, GuiDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, File> guiFiles = new HashMap<>();
    private final ConditionFactory conditionFactory;
    private final Map<Integer, String> npcToGuiMap = new HashMap<>();

    public void cacheNpcBinding(int npcId, String guiId) { npcToGuiMap.put(npcId, guiId); }
    public String getGuiByNpc(int npcId) { return npcToGuiMap.get(npcId); }
    public void clearNpcBindings() { npcToGuiMap.clear(); }

    public GuiRegistry(SmartMenus plugin) {
        this.plugin = plugin;
        this.conditionFactory = new ConditionFactory(plugin);
    }

    public void reload() {
        definitions.clear();
        guiFiles.clear();
        clearNpcBindings();

        File guiFolder = new File(plugin.getDataFolder(), "guis");
        if (!guiFolder.exists() && !guiFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create guis folder: " + guiFolder.getPath());
            return;
        }

        if (plugin.getPatternRegistry() != null) {
            plugin.getPatternRegistry().reload();
        }

        List<File> files = new ArrayList<>();
        collectYamlFiles(guiFolder, files);
        if (files.isEmpty()) {
            plugin.getLogger().warning("No GUI files found in guis/ folder.");
            return;
        }

        for (File file : files) {
            loadGuiFile(file);
        }

        plugin.getLogger().info("Loaded " + definitions.size() + " GUI definition(s).");
    }

    private void loadGuiFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection guisSection = config.getConfigurationSection("guis");
        if (guisSection == null) {
            guisSection = config;
        }

        Set<String> guiIds = new LinkedHashSet<>();
        for (String id : guisSection.getKeys(false)) {
            ConfigurationSection section = guisSection.getConfigurationSection(id);
            if (isGuiSection(section)) {
                guiIds.add(id);
            }
        }

        if (guiIds.isEmpty()) {
            plugin.getLogger().warning("No GUI definitions found in " + file.getName() + ".");
            return;
        }

        for (String id : guiIds) {
            try {
                GuiDefinition definition = loadGuiDefinition(id, guisSection.getConfigurationSection(id));
                if (definition != null) {
                    definitions.put(id, definition);
                    guiFiles.put(id, file);
                    if (definition.hasNpcBinding()) {
                        cacheNpcBinding(definition.getNpcId(), id);
                        plugin.getLogger().info("GUI '" + id + "' bound to NPC ID: " + definition.getNpcId());
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load GUI '" + id + "': " + e.getMessage(), e);
            }
        }
    }

    private void collectYamlFiles(File folder, List<File> files) {
        File[] children = folder.listFiles();
        if (children == null) return;

        Arrays.sort(children, Comparator.comparing(File::getName));
        for (File child : children) {
            if (child.isDirectory()) {
                collectYamlFiles(child, files);
            } else if (child.getName().endsWith(".yml") || child.getName().endsWith(".yaml")) {
                files.add(child);
            }
        }
    }

    private boolean isGuiSection(ConfigurationSection section) {
        return section != null
                && (section.isSet("title")
                || section.isSet("rows")
                || section.isSet("items")
                || section.isSet("commands")
                || section.isSet("inventory-type")
                || section.isSet("inventory_type"));
    }

    private GuiDefinition loadGuiDefinition(String id, ConfigurationSection guiSection) {
        if (guiSection == null) return null;

        String title = guiSection.getString("title", id);
        int rows = Math.min(6, Math.max(1, guiSection.getInt("rows", 1)));
        List<String> guiCommands = guiSection.getStringList("commands");

        Integer npcId = guiSection.isSet("npc_id") ? guiSection.getInt("npc_id") : null;

        InventoryType inventoryType = null;
        if (guiSection.isSet("inventory-type") || guiSection.isSet("inventory_type")) {
            try {
                String typeName = guiSection.getString("inventory-type", guiSection.getString("inventory_type", "CHEST"));
                inventoryType = InventoryType.valueOf(typeName.toUpperCase(Locale.ENGLISH).replace('-', '_'));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown inventory-type for GUI " + id + ": " + guiSection.getString("inventory-type", guiSection.getString("inventory_type")));
            }
        }

        GuiItem fillItem = null;
        if (guiSection.isSet("fill")) {
            ConfigurationSection fillSection = guiSection.getConfigurationSection("fill");
            if (fillSection != null) {
                fillItem = loadElseItem(id, fillSection);
            }
        }

        List<GuiDefinition.ArgDefinition> commandArgs = new ArrayList<>();
        if (guiSection.isList("args")) {
            for (Object argObj : guiSection.getList("args")) {
                if (!(argObj instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> argMap = (Map<String, Object>) argObj;
                String argName = argMap.containsKey("name") ? argMap.get("name").toString() : null;
                if (argName == null) continue;
                String argType = argMap.containsKey("type") ? argMap.get("type").toString() : "text";
                boolean required = argMap.containsKey("required") && Boolean.parseBoolean(argMap.get("required").toString());
                String defaultValue = argMap.containsKey("default") ? argMap.get("default").toString() : null;
                commandArgs.add(new GuiDefinition.ArgDefinition(argName, argType, required, defaultValue));
            }
        }

        boolean useBottomInventory = guiSection.getBoolean("use_bottom_inventory",
                guiSection.getBoolean("use-bottom-inventory", false));

        BottomInventoryMode bottomInventoryMode = BottomInventoryMode.DEFAULT;
        if (useBottomInventory && guiSection.isSet("bottom_inventory_mode")) {
            String modeStr = guiSection.getString("bottom_inventory_mode", "DEFAULT");
            bottomInventoryMode = BottomInventoryMode.parse(modeStr, id, plugin.getLogger());
        }

        Map<Integer, GuiItem> items = new LinkedHashMap<>();
        ConfigurationSection itemsSection = guiSection.getConfigurationSection("items");

        if (guiSection.isList("patterns") && itemsSection != null && plugin.getPatternRegistry() != null) {
            for (Object patternObj : guiSection.getList("patterns")) {
                if (patternObj instanceof String) {
                    plugin.getPatternRegistry().applyPattern((String) patternObj, itemsSection, Collections.emptyMap());
                } else if (patternObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> patternMap = (Map<String, Object>) patternObj;
                    String patId = patternMap.containsKey("id") ? patternMap.get("id").toString() : null;
                    if (patId == null) continue;
                    Map<String, String> vars = new HashMap<>();
                    Object varsObj = patternMap.get("vars");
                    if (varsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> varsMap = (Map<String, Object>) varsObj;
                        for (Map.Entry<String, Object> e : varsMap.entrySet()) {
                            vars.put(e.getKey(), e.getValue() != null ? e.getValue().toString() : "");
                        }
                    }
                    plugin.getPatternRegistry().applyPattern(patId, itemsSection, vars);
                }
            }
        }

        if (itemsSection != null) {
            for (String slotKey : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(slotKey);
                if (itemSection == null) continue;

                List<Integer> slots = parseSlots(slotKey, itemSection);
                if (slots.isEmpty()) {
                    plugin.getLogger().warning("Invalid slot '" + slotKey + "' in GUI " + id);
                    continue;
                }

                for (int slot : slots) {
                    GuiItem guiItem = loadGuiItem(id, slot, itemSection);
                    if (guiItem != null) {
                        items.put(slot, guiItem);
                    }
                }
            }
        }

        Map<Integer, GuiItem> bottomItems = new LinkedHashMap<>();
        ConfigurationSection bottomSection = guiSection.getConfigurationSection("bottom_items");
        if (bottomSection == null) bottomSection = guiSection.getConfigurationSection("bottom-items");
        if (bottomSection != null) {
            for (String slotKey : bottomSection.getKeys(false)) {
                ConfigurationSection itemSection = bottomSection.getConfigurationSection(slotKey);
                if (itemSection == null) continue;

                List<Integer> slots = parseSlots(slotKey, itemSection);
                for (int slot : slots) {
                    if (slot < 0 || slot > 35) {
                        plugin.getLogger().warning("bottom_items slot " + slot + " out of range (0-35) in GUI " + id);
                        continue;
                    }
                    GuiItem guiItem = loadGuiItem(id, slot, itemSection);
                    if (guiItem != null) bottomItems.put(slot, guiItem);
                }
            }
        }

        BedrockFormDefinition bedrockDefinition = null;
        boolean bedrockAutoConvert = guiSection.getBoolean("auto-convert",
                guiSection.getBoolean("auto_convert", false));

        ConfigurationSection bedrockSection = guiSection.getConfigurationSection("bedrock");
        if (bedrockSection != null) {
            bedrockDefinition = loadBedrockDefinition(id, bedrockSection);
        }

        if (bedrockDefinition != null && bedrockAutoConvert) {
            plugin.getLogger().info("  GUI '" + id + "': explicit bedrock: block takes priority over auto-convert.");
            bedrockAutoConvert = false;
        }

        List<Condition> openRequirements = parseConditionsFromKey(guiSection, "open-requirements");
        List<Action> openActions = com.oreo.action.ActionFactory.parseActions(guiSection.getList("open-actions"));
        List<Action> closeActions = com.oreo.action.ActionFactory.parseActions(guiSection.getList("close-actions"));

        String openSound = null;
        float openSoundVolume = 1.0f;
        float openSoundPitch = 1.0f;
        if (guiSection.isSet("open_sound") || guiSection.isSet("open-sound")) {
            String soundKey = guiSection.isSet("open_sound") ? "open_sound" : "open-sound";
            if (guiSection.isConfigurationSection(soundKey)) {
                ConfigurationSection soundSection = guiSection.getConfigurationSection(soundKey);
                openSound = soundSection.getString("sound");
                openSoundVolume = (float) soundSection.getDouble("volume", 1.0);
                openSoundPitch = (float) soundSection.getDouble("pitch", 1.0);
            } else {
                openSound = guiSection.getString(soundKey);
            }
        }

        com.oreo.util.CooldownConfig openCooldown = null;
        ConfigurationSection cooldownSection = guiSection.getConfigurationSection("cooldown");
        if (cooldownSection == null) cooldownSection = guiSection.getConfigurationSection("open-cooldown");
        if (cooldownSection != null) {
            openCooldown = com.oreo.util.CooldownConfig.parse(cooldownSection, "gui:" + id);
        }

        return new GuiDefinition.Builder(id, title)
                .rows(rows)
                .items(items)
                .commands(guiCommands)
                .npcId(npcId)
                .fillItem(fillItem)
                .inventoryType(inventoryType)
                .commandArgs(commandArgs)
                .useBottomInventory(useBottomInventory)
                .bottomItems(bottomItems)
                .bedrockDefinition(bedrockDefinition)
                .bedrockAutoConvert(bedrockAutoConvert)
                .openRequirements(openRequirements)
                .openActions(openActions)
                .closeActions(closeActions)
                .openSound(openSound)
                .openSoundVolume(openSoundVolume)
                .openSoundPitch(openSoundPitch)
                .bottomInventoryMode(bottomInventoryMode)
                .openCooldown(openCooldown)
                .build();
    }

    private BedrockFormDefinition loadBedrockDefinition(String guiId, ConfigurationSection section) {
        try {
            String typeStr = section.getString("type", "SIMPLE_FORM").toUpperCase(Locale.ENGLISH).replace('-', '_');
            BedrockFormDefinition.FormType type;
            try {
                type = BedrockFormDefinition.FormType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown bedrock form type '" + typeStr + "' in GUI " + guiId + ", defaulting to SIMPLE_FORM.");
                type = BedrockFormDefinition.FormType.SIMPLE_FORM;
            }

            String title   = section.getString("title", guiId);
            String content = section.getString("content", "");

            List<BedrockButton> buttons = new ArrayList<>();
            List<?> buttonList = section.getList("buttons");
            if (buttonList != null) {
                for (Object obj : buttonList) {
                    if (!(obj instanceof Map)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> btnMap = (Map<String, Object>) obj;
                    BedrockButton btn = loadBedrockButton(guiId, btnMap);
                    if (btn != null) buttons.add(btn);
                }
            }

            String confirmButton  = section.getString("confirm-button", "Confirm");
            String denyButton     = section.getString("deny-button", "Cancel");
            List<Action> confirmActions = ActionFactory.parseActions(section.getList("confirm-actions"));
            List<Action> denyActions    = ActionFactory.parseActions(section.getList("deny-actions"));

            List<BedrockFormInput> inputs = new ArrayList<>();
            List<?> inputList = section.getList("inputs");
            if (inputList != null) {
                for (Object obj : inputList) {
                    if (!(obj instanceof Map)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> inputMap = (Map<String, Object>) obj;
                    BedrockFormInput inp = loadBedrockInput(guiId, inputMap);
                    if (inp != null) inputs.add(inp);
                }
            }
            List<Action> submitActions    = ActionFactory.parseActions(section.getList("submit-actions"));
            String submitButtonText       = section.getString("submit-button", "Submit");
            List<Action> closeActions     = ActionFactory.parseActions(section.getList("close-actions"));

            String npcName     = section.getString("npc-name", section.getString("npc_name", "NPC"));
            String dialogueTag = section.getString("dialogue-tag", section.getString("dialogue_tag", guiId + ".dialogue"));

            plugin.getLogger().info("  Loaded bedrock form for GUI '" + guiId + "': type=" + type
                    + " buttons=" + buttons.size());

            return new BedrockFormDefinition.Builder(type)
                    .title(title)
                    .content(content)
                    .buttons(buttons)
                    .confirmButton(confirmButton)
                    .denyButton(denyButton)
                    .confirmActions(confirmActions)
                    .denyActions(denyActions)
                    .inputs(inputs)
                    .submitActions(submitActions)
                    .submitButtonText(submitButtonText)
                    .closeActions(closeActions)
                    .npcName(npcName)
                    .dialogueTag(dialogueTag)
                    .build();

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load bedrock: block for GUI " + guiId + ": " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private BedrockButton loadBedrockButton(String guiId, Map<String, Object> map) {
        try {
            String text      = map.containsKey("text")       ? map.get("text").toString()       : "Button";
            String imageType = map.containsKey("image-type") ? map.get("image-type").toString() : null;
            String imageData = map.containsKey("image-data") ? map.get("image-data").toString() : null;

            List<Condition> conditions = Collections.emptyList();
            if (map.containsKey("conditions") && map.get("conditions") instanceof List) {
                YamlConfiguration tmp = new YamlConfiguration();
                tmp.set("conditions", map.get("conditions"));
                ConfigurationSection tmpSection = tmp.createSection("__btn");
                tmpSection.set("conditions", map.get("conditions"));
                conditions = parseConditionsFromKey(tmpSection, "conditions");
            }

            List<Action> actions = Collections.emptyList();
            if (map.containsKey("actions") && map.get("actions") instanceof List) {
                actions = ActionFactory.parseActions((List<?>) map.get("actions"));
            }

            return new BedrockButton(text, imageType, imageData, conditions, actions);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse bedrock button in GUI " + guiId + ": " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private BedrockFormInput loadBedrockInput(String guiId, Map<String, Object> map) {
        try {
            String typeStr = map.containsKey("type") ? map.get("type").toString().toUpperCase() : "INPUT";
            String label   = map.containsKey("label") ? map.get("label").toString() : "";

            String key     = map.containsKey("key")   ? map.get("key").toString()   : Ids.slugify(label);

            switch (typeStr) {
                case "INPUT":
                    return BedrockFormInput.input(
                            key, label,
                            map.containsKey("placeholder") ? map.get("placeholder").toString() : "",
                            map.containsKey("default")     ? map.get("default").toString()     : "",
                            map.containsKey("max-length")  ? Integer.parseInt(map.get("max-length").toString()) : 256);
                case "DROPDOWN": {
                    List<String> opts = map.containsKey("options") && map.get("options") instanceof List
                            ? toStringList((List<?>) map.get("options"))
                            : new ArrayList<>();
                    int defIdx = map.containsKey("default-index") ? Integer.parseInt(map.get("default-index").toString()) : 0;
                    return BedrockFormInput.dropdown(key, label, opts, defIdx);
                }
                case "SLIDER": {
                    float min  = map.containsKey("min")   ? Float.parseFloat(map.get("min").toString())   : 0f;
                    float max  = map.containsKey("max")   ? Float.parseFloat(map.get("max").toString())   : 100f;
                    float step = map.containsKey("step")  ? Float.parseFloat(map.get("step").toString())  : 1f;
                    float def  = map.containsKey("default") ? Float.parseFloat(map.get("default").toString()) : min;
                    return BedrockFormInput.slider(key, label, min, max, step, def);
                }
                case "STEP_SLIDER": {
                    List<String> steps = map.containsKey("steps") && map.get("steps") instanceof List
                            ? toStringList((List<?>) map.get("steps"))
                            : new ArrayList<>();
                    int defIdx = map.containsKey("default-index") ? Integer.parseInt(map.get("default-index").toString()) : 0;
                    return BedrockFormInput.stepSlider(key, label, steps, defIdx);
                }
                case "TOGGLE": {
                    boolean def = map.containsKey("default") && Boolean.parseBoolean(map.get("default").toString());
                    return BedrockFormInput.toggle(key, label, def);
                }
                case "LABEL":
                    return BedrockFormInput.label(label);
                default:
                    plugin.getLogger().warning("Unknown bedrock input type '" + typeStr + "' in GUI " + guiId);
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse bedrock input in GUI " + guiId + ": " + e.getMessage());
            return null;
        }
    }

    private List<String> toStringList(List<?> raw) {
        List<String> result = new ArrayList<>();
        for (Object o : raw) {
            if (o != null) result.add(o.toString());
        }
        return result;
    }

    private GuiItem loadGuiItem(String guiId, int slot, ConfigurationSection itemSection) {
        try {
            String material = itemSection.getString("material", "STONE");
            String name = itemSection.getString("name", "");
            List<String> lore = ColorUtil.colorList(itemSection.getStringList("lore"));
            List<String> commands = itemSection.getStringList("commands");
            boolean closeOnClick = itemSection.getBoolean("close", false);

            double price = itemSection.getDouble("price", 0.0);
            String requirement = itemSection.getString("requirement", "");

            String itemType = itemSection.getString("item_type", "vanilla").toLowerCase();
            String itemId = itemSection.getString("item_id");
            Integer customModelData = itemSection.isSet("custom_model_data") ?
                    itemSection.getInt("custom_model_data") : null;

            if (material != null && material.toLowerCase().startsWith("hdb:")) {
                itemId = material.substring(4);
                itemType = "headdatabase";
                material = "PLAYER_HEAD";
            }
            if ("headdatabase".equalsIgnoreCase(itemType) && itemId == null) {
                itemId = itemSection.getString("item_id");
            }

            List<Condition> conditions = parseConditions(itemSection);
            if (!conditions.isEmpty()) {
                plugin.getLogger().info("  Slot " + slot + ": Loaded " + conditions.size() + " condition(s)");
            }
            if (!"vanilla".equals(itemType)) {
                plugin.getLogger().info("  Slot " + slot + ": Using " + itemType + " item: " + itemId);
            }

            Map<String, List<Action>> clickActions = new HashMap<>();

            if (itemSection.isList("actions")) {
                List<Action> anyActions = ActionFactory.parseActions(itemSection.getList("actions"));
                if (!anyActions.isEmpty()) clickActions.put("ANY", anyActions);
            }

            ConfigurationSection clickActionsSection = itemSection.getConfigurationSection("click_actions");
            if (clickActionsSection != null) {
                for (String clickType : clickActionsSection.getKeys(false)) {
                    if (clickActionsSection.isList(clickType)) {
                        List<Action> typeActions = ActionFactory.parseActions(clickActionsSection.getList(clickType));
                        if (!typeActions.isEmpty()) {
                            clickActions.put(clickType.toUpperCase(), typeActions);
                        }
                    }
                }
            }

            if (!commands.isEmpty()) {
                List<Action> legacyActions = new ArrayList<>();
                for (String cmd : commands) {
                    legacyActions.add(new ActionFactory.ConsoleCommandAction(cmd));
                }
                clickActions.put("LEGACY", legacyActions);
            }

            List<Condition> viewRequirements = parseConditionsFromKey(itemSection, "view-requirements");
            GuiItem elseItem = null;
            if (itemSection.isSet("else-item")) {
                ConfigurationSection elseSection = itemSection.getConfigurationSection("else-item");
                if (elseSection != null) {
                    elseItem = loadElseItem(guiId, elseSection);
                }
            }

            ButtonType buttonType = ButtonType.NONE;
            if (itemSection.isSet("type")) {
                buttonType = ButtonType.fromString(itemSection.getString("type", ""));
            }
            String chatInputType = itemSection.getString("chat-input-type", "TEXT");
            String chatPromptMessage = itemSection.getString("chat-prompt", "&7Please type in chat.");
            String chatCancelWord = itemSection.getString("chat-cancel-word", "cancel");

            boolean glow = itemSection.getBoolean("glow", false);
            List<String> itemFlags = itemSection.getStringList("item_flags");
            Map<String, Integer> enchantments = new HashMap<>();
            if (itemSection.isSet("enchantments")) {
                ConfigurationSection enchSection = itemSection.getConfigurationSection("enchantments");
                if (enchSection != null) {
                    for (String enchKey : enchSection.getKeys(false)) {
                        enchantments.put(enchKey, enchSection.getInt(enchKey, 1));
                    }
                }
            }

            boolean autoUpdate = itemSection.getBoolean("update", false);
            int updateInterval = itemSection.getInt("update-interval", 20);

            boolean permanent = itemSection.getBoolean("is-permanent", itemSection.getBoolean("permanent", false));
            DynamicSource dynamicSource = DynamicSource.fromString(itemSection.getString("source", ""));
            boolean takeItem = itemSection.getBoolean("take-item", itemSection.getBoolean("take_item", false));
            boolean giveItem = itemSection.getBoolean("give-item", itemSection.getBoolean("give_item", false));
            int amount = Math.max(1, itemSection.getInt("amount", 1));

            List<String> onPlaceCommands = new ArrayList<>();
            List<Action> onPlaceActions = new ArrayList<>();
            List<?> rawOnPlace = itemSection.getList("on-place", Collections.emptyList());
            for (Object obj : rawOnPlace) {
                if (obj instanceof String s) {
                    onPlaceCommands.add(s);
                    onPlaceActions.add(new ActionFactory.ConsoleCommandAction(s));
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Action a = ActionFactory.parseAction((Map<String, Object>) obj);
                    if (a != null) onPlaceActions.add(a);
                }
            }
            boolean removeOnPlace = itemSection.getBoolean("remove-on-place", true);

            com.oreo.util.CooldownConfig itemCooldown = null;
            ConfigurationSection itemCooldownSection = itemSection.getConfigurationSection("cooldown");
            if (itemCooldownSection != null) {
                itemCooldown = com.oreo.util.CooldownConfig.parse(itemCooldownSection, guiId + ":slot:" + slot);
            }

            return new GuiItem.Builder()
                    .slot(slot)
                    .material(material)
                    .name(name)
                    .lore(lore)
                    .commands(commands)
                    .closeOnClick(closeOnClick)
                    .price(price)
                    .requirement(requirement)
                    .conditions(conditions)
                    .itemType(itemType)
                    .itemId(itemId)
                    .customModelData(customModelData)
                    .clickActions(clickActions)
                    .viewRequirements(viewRequirements)
                    .elseItem(elseItem)
                    .buttonType(buttonType)
                    .chatInputType(chatInputType)
                    .chatPromptMessage(chatPromptMessage)
                    .chatCancelWord(chatCancelWord)
                    .glow(glow)
                    .itemFlags(itemFlags)
                    .enchantments(enchantments)
                    .autoUpdate(autoUpdate)
                    .updateInterval(updateInterval)
                    .permanent(permanent)
                    .dynamicSource(dynamicSource)
                    .takeItem(takeItem)
                    .giveItem(giveItem)
                    .amount(amount)
                    .onPlaceCommands(onPlaceCommands)
                    .onPlaceActions(onPlaceActions)
                    .removeOnPlace(removeOnPlace)
                    .cooldown(itemCooldown)
                    .build();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load item at slot " + slot + " in GUI " + guiId + ": " + e.getMessage(), e);
            return null;
        }
    }

    private GuiItem loadElseItem(String guiId, ConfigurationSection section) {
        try {
            String material = section.getString("material", "STONE");
            String name = section.getString("name", "");
            List<String> lore = ColorUtil.colorList(section.getStringList("lore"));
            boolean closeOnClick = section.getBoolean("close", false);
            boolean glow = section.getBoolean("glow", false);
            List<String> itemFlags = section.getStringList("item_flags");

            Map<String, List<Action>> clickActions = new HashMap<>();
            if (section.isList("actions")) {
                List<Action> anyActions = ActionFactory.parseActions(section.getList("actions"));
                if (!anyActions.isEmpty()) clickActions.put("ANY", anyActions);
            }

            ConfigurationSection clickActionsSection = section.getConfigurationSection("click_actions");
            if (clickActionsSection != null) {
                for (String clickType : clickActionsSection.getKeys(false)) {
                    if (clickActionsSection.isList(clickType)) {
                        List<Action> typeActions = ActionFactory.parseActions(clickActionsSection.getList(clickType));
                        if (!typeActions.isEmpty()) clickActions.put(clickType.toUpperCase(), typeActions);
                    }
                }
            }

            String itemType = section.getString("item_type", "vanilla").toLowerCase();
            String itemId = section.getString("item_id");
            Integer customModelData = section.isSet("custom_model_data") ? section.getInt("custom_model_data") : null;
            boolean autoUpdate = section.getBoolean("update", false);
            int amount = Math.max(1, section.getInt("amount", 1));

            if (material != null && material.toLowerCase().startsWith("hdb:")) {
                itemId = material.substring(4);
                itemType = "headdatabase";
                material = "PLAYER_HEAD";
            }

            List<Condition> viewRequirements = parseConditionsFromKey(section, "view-requirements");
            GuiItem nestedElseItem = null;
            if (section.isSet("else-item")) {
                ConfigurationSection elseSection = section.getConfigurationSection("else-item");
                if (elseSection != null) {
                    nestedElseItem = loadElseItem(guiId, elseSection);
                }
            }

            return new GuiItem.Builder()
                    .material(material)
                    .name(name)
                    .lore(lore)
                    .closeOnClick(closeOnClick)
                    .itemType(itemType)
                    .itemId(itemId)
                    .customModelData(customModelData)
                    .clickActions(clickActions)
                    .viewRequirements(viewRequirements)
                    .elseItem(nestedElseItem)
                    .glow(glow)
                    .itemFlags(itemFlags)
                    .autoUpdate(autoUpdate)
                    .amount(amount)
                    .build();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load else-item/fill in GUI " + guiId + ": " + e.getMessage());
            return null;
        }
    }

    private List<Condition> parseConditions(ConfigurationSection itemSection) {
        return parseConditionsFromKey(itemSection, "conditions");
    }

    private List<Condition> parseConditionsFromKey(ConfigurationSection itemSection, String key) {
        if (!itemSection.isList(key)) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        List<?> conditionsList = itemSection.getList(key);

        if (conditionsList == null) return Collections.emptyList();

        for (Object condObj : conditionsList) {
            if (!(condObj instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> condMap = (Map<String, Object>) condObj;

            YamlConfiguration tempConfig = new YamlConfiguration();
            for (Map.Entry<String, Object> entry : condMap.entrySet()) {
                tempConfig.set(entry.getKey(), entry.getValue());
            }

            try {
                Condition condition = conditionFactory.parseCondition(tempConfig);
                if (condition != null) {
                    conditions.add(condition);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse condition: " + e.getMessage(), e);
            }
        }

        return conditions;
    }

    public GuiDefinition getGui(String id) { return definitions.get(id); }

    public Set<String> getGuiIds() { return Collections.unmodifiableSet(definitions.keySet()); }

    public File getGuiFile(String id) { return guiFiles.get(id); }

    private List<Integer> parseSlots(String slotKey, ConfigurationSection itemSection) {
        List<Integer> slots = new ArrayList<>();

        if (itemSection.isSet("slot")) {
            addSlotValue(slots, itemSection.get("slot"));
        } else if (itemSection.isSet("slots")) {
            Object rawSlots = itemSection.get("slots");
            if (rawSlots instanceof List<?>) {
                for (Object value : (List<?>) rawSlots) {
                    addSlotValue(slots, value);
                }
            } else {
                addSlotValue(slots, rawSlots);
            }
        } else {
            addSlotValue(slots, slotKey);
        }

        LinkedHashSet<Integer> unique = new LinkedHashSet<>();
        for (int slot : slots) {
            if (slot >= 0) {
                unique.add(slot);
            }
        }
        return new ArrayList<>(unique);
    }

    private void addSlotValue(List<Integer> slots, Object value) {
        slots.addAll(SlotParser.parse(value));
    }
}
