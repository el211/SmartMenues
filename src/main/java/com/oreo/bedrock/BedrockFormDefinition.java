package com.oreo.bedrock;

import com.oreo.action.Action;

import java.util.Collections;
import java.util.List;

public class BedrockFormDefinition {

    public enum FormType {
        SIMPLE_FORM, MODAL_FORM, CUSTOM_FORM, DIALOGUE
    }

    private final FormType type;
    private final String title;
    private final String content;

    private final List<BedrockButton> buttons;

    private final String confirmButton;
    private final String denyButton;
    private final List<Action> confirmActions;
    private final List<Action> denyActions;

    private final List<BedrockFormInput> inputs;
    private final List<Action> submitActions;

    private final String npcName;
    private final String dialogueTag;

    public BedrockFormDefinition(FormType type, String title, String content,
                                 List<BedrockButton> buttons,
                                 String confirmButton, String denyButton,
                                 List<Action> confirmActions, List<Action> denyActions,
                                 List<BedrockFormInput> inputs, List<Action> submitActions,
                                 String npcName, String dialogueTag) {
        this.type = type;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
        this.buttons = buttons == null ? Collections.emptyList() : buttons;
        this.confirmButton = confirmButton == null ? "Confirm" : confirmButton;
        this.denyButton = denyButton == null ? "Cancel" : denyButton;
        this.confirmActions = confirmActions == null ? Collections.emptyList() : confirmActions;
        this.denyActions = denyActions == null ? Collections.emptyList() : denyActions;
        this.inputs = inputs == null ? Collections.emptyList() : inputs;
        this.submitActions = submitActions == null ? Collections.emptyList() : submitActions;
        this.npcName = npcName == null ? "NPC" : npcName;
        this.dialogueTag = dialogueTag == null ? "dialogue" : dialogueTag;
    }

    public FormType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<BedrockButton> getButtons() { return buttons; }
    public String getConfirmButton() { return confirmButton; }
    public String getDenyButton() { return denyButton; }
    public List<Action> getConfirmActions() { return confirmActions; }
    public List<Action> getDenyActions() { return denyActions; }
    public List<BedrockFormInput> getInputs() { return inputs; }
    public List<Action> getSubmitActions() { return submitActions; }
    public String getNpcName() { return npcName; }
    public String getDialogueTag() { return dialogueTag; }
}
