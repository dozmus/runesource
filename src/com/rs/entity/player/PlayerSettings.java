package com.rs.entity.player;

/**
 * Created by pure on 21/04/2017.
 */
public class PlayerSettings {

    private MouseButtons mouseButtons = MouseButtons.TWO;
    private Brightness brightness = Brightness.NORMAL;
    private boolean chatEffects = true;
    private boolean splitPrivateChat = false;
    private boolean acceptAid = false;
    private boolean runToggled = false;
    private boolean autoRetaliate = true;

    public MouseButtons getMouseButtons() {
        return mouseButtons;
    }

    public void setMouseButtons(MouseButtons mouseButtons) {
        this.mouseButtons = mouseButtons;
    }

    public Brightness getBrightness() {
        return brightness;
    }

    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public boolean isChatEffects() {
        return chatEffects;
    }

    public void setChatEffects(boolean chatEffects) {
        this.chatEffects = chatEffects;
    }

    public boolean isSplitPrivateChat() {
        return splitPrivateChat;
    }

    public void setSplitPrivateChat(boolean splitPrivateChat) {
        this.splitPrivateChat = splitPrivateChat;
    }

    public boolean isAcceptAid() {
        return acceptAid;
    }

    public void setAcceptAid(boolean acceptAid) {
        this.acceptAid = acceptAid;
    }

    public boolean isRunToggled() {
        return runToggled;
    }

    public void setRunToggled(boolean runToggled) {
        this.runToggled = runToggled;
    }

    public boolean isAutoRetaliate() {
        return autoRetaliate;
    }

    public void setAutoRetaliate(boolean autoRetaliate) {
        this.autoRetaliate = autoRetaliate;
    }

    public enum MouseButtons {
        ONE, TWO
    }

    public enum Brightness {
        DARK, NORMAL, BRIGHT, VERY_BRIGHT;

        public int settingValue() {
            return ordinal() + 1;
        }
    }
}
