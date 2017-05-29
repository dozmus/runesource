package com.rs.entity.player;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A {@link Player}'s in-game settings.
 */
public final class PlayerSettings {

    private MouseButtons mouseButtons = MouseButtons.TWO;
    private Brightness brightness = Brightness.NORMAL;
    private boolean chatEffects = true;
    private boolean splitPrivateChat = false;
    private boolean acceptAid = false;
    private boolean runToggled = false;
    private boolean autoRetaliate = true;
    private int publicChatMode = 0;
    private int privateChatMode = 0;
    private int tradeMode = 0;

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

    public int getPublicChatMode() {
        return publicChatMode;
    }

    public void setPublicChatMode(int publicChatMode) {
        this.publicChatMode = publicChatMode;
    }

    public int getPrivateChatMode() {
        return privateChatMode;
    }

    public void setPrivateChatMode(int privateChatMode) {
        this.privateChatMode = privateChatMode;
    }

    public int getTradeMode() {
        return tradeMode;
    }

    public void setTradeMode(int tradeMode) {
        this.tradeMode = tradeMode;
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
