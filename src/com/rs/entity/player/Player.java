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

import com.rs.Server;
import com.rs.WorldHandler;
import com.rs.entity.MovementHandler;
import com.rs.entity.Position;
import com.rs.entity.npc.Npc;
import com.rs.entity.player.obj.Animation;
import com.rs.entity.player.obj.Graphic;
import com.rs.io.PlayerFileHandler;
import com.rs.net.StreamBuffer;
import com.rs.util.Misc;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a logged-in player.
 *
 * @author blakeman8192
 * @author Pure_
 */
public class Player extends Client {

    private final List<Player> players = new LinkedList<Player>();
    private final List<Npc> npcs = new LinkedList<Npc>();
    PlayerAttributes attributes = new PlayerAttributes();
    private MovementHandler movementHandler = new MovementHandler(this);
    private Position currentRegion = new Position(0, 0, 0);
    private int primaryDirection = -1;
    private int secondaryDirection = -1;
    private int slot = -1;
    private int chatColor;
    private int chatEffects;
    private byte[] chatText;
    // Various player update flags.
    private boolean updateRequired = false;
    private boolean appearanceUpdateRequired = false;
    private boolean chatUpdateRequired = false;
    private boolean animationUpdateRequired = false;
    private boolean graphicUpdateRequired = false;
    private boolean forceChatUpdateRequired = false;
    private String forceChatText;
    private boolean needsPlacement = false;
    private boolean resetMovementQueue = false;
    private Animation animation = new Animation();
    private Graphic graphic = new Graphic();

    /**
     * Creates a new Player.
     *
     * @param key the SelectionKey
     */
    public Player(SelectionKey key) {
        super(key);

        // Resetting attributes
        attributes.reset();
    }

    /**
     * Performs processing for this player.
     *
     * @throws Exception
     */
    public void process() throws Exception {
        // If no is received packet for more than 5 seconds, disconnect.
        if (getTimeoutStopwatch().elapsed() > 5000) {
            System.out.println(this + " timed out.");
            disconnect();
            return;
        }
        movementHandler.process();
    }

    /**
     * Teleports the player to the desired position.
     *
     * @param position the position
     */
    public void teleport(Position position) {
        movementHandler.reset();
        getPosition().setAs(position);
        setResetMovementQueue(true);
        setNeedsPlacement(true);
        sendMapRegion();
    }

    /**
     * Resets the player after updating.
     */
    public void reset() {
        setPrimaryDirection(-1);
        setSecondaryDirection(-1);
        setUpdateRequired(false);
        setAppearanceUpdateRequired(false);
        setAnimationUpdateRequired(false);
        setGraphicUpdateRequired(false);
        setForceChatUpdateRequired(false);
        setChatUpdateRequired(false);
        setResetMovementQueue(false);
        setNeedsPlacement(false);
    }

    @Override
    public void login(String username, String password) throws Exception {
        int response = Misc.LOGIN_RESPONSE_OK;

        // Updating credentials
        String rawPassword = password;
        getAttributes().setUsername(username);
        getAttributes().setPassword(Server.getInstance().getSettings().isHashingPasswords() ? Misc.hashSha256(password) : password);

        // Check if the player is already logged in.
        if (WorldHandler.isPlayerOnline(username)) {
            response = Misc.LOGIN_RESPONSE_ACCOUNT_ONLINE;
        }

        // Load the player and send the login response.
        PlayerFileHandler.LoadResponse status = Server.getInstance().getPlayerFileHandler().load(this);
        boolean validCredentials = Misc.validatePassword(rawPassword) && Misc.validateUsername(getAttributes().getUsername());

        // Invalid username/password - we skip the check if the account is found because the validation may have changed since
        if ((status != PlayerFileHandler.LoadResponse.SUCCESS && !validCredentials)
                || status == PlayerFileHandler.LoadResponse.INVALID_CREDENTIALS) {
            response = Misc.LOGIN_RESPONSE_INVALID_CREDENTIALS;
        }

        // Sending response
        StreamBuffer.OutBuffer resp = StreamBuffer.newOutBuffer(3);
        resp.writeByte(response);
        resp.writeByte(getAttributes().getStaffRights());
        resp.writeByte(0);
        send(resp.getBuffer());

        if (response != Misc.LOGIN_RESPONSE_OK) {
            disconnect();
            return;
        }

        // Initialising player session
        WorldHandler.register(this);
        sendMapRegion();
        sendInventory();
        sendSkills();
        sendEquipment();
        setUpdateRequired(true);
        setAppearanceUpdateRequired(true);
        sendSidebarInterface(1, 3917);
        sendSidebarInterface(2, 638);
        sendSidebarInterface(3, 3213);
        sendSidebarInterface(4, 1644);
        sendSidebarInterface(5, 5608);
        sendSidebarInterface(6, 1151);
        sendSidebarInterface(8, 5065);
        sendSidebarInterface(9, 5715);
        sendSidebarInterface(10, 2449);
        sendSidebarInterface(11, 4445);
        sendSidebarInterface(12, 147);
        sendSidebarInterface(13, 6299);
        sendSidebarInterface(0, 2423);
        sendRunEnergy();
        sendResetAllButtonStates();
        sendMessage("Welcome to " + Server.getInstance().getSettings().getServerName() + "!");
        System.out.println(this + " has logged in.");
    }

    @Override
    public void logout() throws Exception {
        WorldHandler.unregister(this);
        setStage(Client.Stage.LOGGED_OUT);
        System.out.println(this + " has logged out.");

        if (getSlot() != -1) {
            Server.getInstance().getPlayerFileHandler().save(this);
        }
    }

    @Override
    public String toString() {
        return getAttributes().getUsername() == null ? "Client(" + getHost() + ")" : "Player(" + getAttributes().getUsername() + "@" + getHost() + ")";
    }

    /**
     * Gets the player's Position.
     *
     * @return the position
     */
    public Position getPosition() {
        return getAttributes().getPosition();
    }

    /**
     * Sets the player's Position. <b>Please use this method with caution</b>,
     * as reference conflicts may lead this player to move when they shouldn't.
     * Consider using position.setAs(other) instead of this method if you wish
     * to set the current players <b>coordinates</b> (not actual position
     * reference) to that of another position.
     *
     * @param position the new Position
     */
    public void setPosition(Position position) {
        getAttributes().setPosition(position);
    }

    /**
     * Gets the MovementHandler.
     *
     * @return the movement handler
     */
    public MovementHandler getMovementHandler() {
        return movementHandler;
    }

    /**
     * Sets the MovementHandler.
     *
     * @param movementHandler the movement handler
     */
    public void setMovementHandler(MovementHandler movementHandler) {
        this.movementHandler = movementHandler;
    }

    /**
     * Gets the player's primary movement direction.
     *
     * @return the direction
     */
    public int getPrimaryDirection() {
        return primaryDirection;
    }

    /**
     * Sets the player's primary movement direction.
     *
     * @param primaryDirection the direction
     */
    public void setPrimaryDirection(int primaryDirection) {
        this.primaryDirection = primaryDirection;
    }

    /**
     * Gets the player's secondary movement direction.
     *
     * @return the direction
     */
    public int getSecondaryDirection() {
        return secondaryDirection;
    }

    /**
     * Sets the player's secondary movement direction.
     *
     * @param secondaryDirection the direction
     */
    public void setSecondaryDirection(int secondaryDirection) {
        this.secondaryDirection = secondaryDirection;
    }

    /**
     * Gets the current region.
     *
     * @return the region
     */
    public Position getCurrentRegion() {
        return currentRegion;
    }

    /**
     * Sets the current region.
     *
     * @param currentRegion the region
     */
    public void setCurrentRegion(Position currentRegion) {
        this.currentRegion = currentRegion;
    }

    /**
     * Sets the needsPlacement boolean.
     *
     * @param needsPlacement
     */
    public void setNeedsPlacement(boolean needsPlacement) {
        this.needsPlacement = needsPlacement;
    }

    /**
     * Gets whether or not the player needs to be placed.
     *
     * @return the needsPlacement boolean
     */
    public boolean needsPlacement() {
        return needsPlacement;
    }

    /**
     * Gets the player slot.
     *
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Sets the player slot.
     *
     * @param slot the slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public void setUpdateRequired(boolean updateRequired) {
        this.updateRequired = updateRequired;
    }

    public boolean isAppearanceUpdateRequired() {
        return appearanceUpdateRequired;
    }

    public void setAppearanceUpdateRequired(boolean appearanceUpdateRequired) {
        if (appearanceUpdateRequired) {
            setUpdateRequired(true);
        }
        this.appearanceUpdateRequired = appearanceUpdateRequired;
    }

    public boolean isResetMovementQueue() {
        return resetMovementQueue;
    }

    public void setResetMovementQueue(boolean resetMovementQueue) {
        this.resetMovementQueue = resetMovementQueue;
    }

    public int getChatColor() {
        return chatColor;
    }

    public void setChatColor(int chatColor) {
        this.chatColor = chatColor;
    }

    public int getChatEffects() {
        return chatEffects;
    }

    public void setChatEffects(int chatEffects) {
        this.chatEffects = chatEffects;
    }

    public byte[] getChatText() {
        return chatText;
    }

    public void setChatText(byte[] chatText) {
        this.chatText = chatText;
    }

    public boolean isChatUpdateRequired() {
        return chatUpdateRequired;
    }

    public void setChatUpdateRequired(boolean chatUpdateRequired) {
        if (chatUpdateRequired) {
            setUpdateRequired(true);
        }
        this.chatUpdateRequired = chatUpdateRequired;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Npc> getNpcs() {
        return npcs;
    }

    public PlayerAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PlayerAttributes attributes) {
        this.attributes = attributes;
    }

    public float getRunEnergyIncrement() {
        return 0.6f;
    }

    public float getRunEnergyDecrement() {
        return 0.6f;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
    }

    public boolean isAnimationUpdateRequired() {
        return animationUpdateRequired;
    }

    public void setAnimationUpdateRequired(boolean animationUpdateRequired) {
        this.animationUpdateRequired = animationUpdateRequired;
    }

    public boolean isGraphicUpdateRequired() {
        return graphicUpdateRequired;
    }

    public void setGraphicUpdateRequired(boolean graphicUpdateRequired) {
        this.graphicUpdateRequired = graphicUpdateRequired;
    }

    public boolean isForceChatUpdateRequired() {
        return forceChatUpdateRequired;
    }

    public void setForceChatUpdateRequired(boolean forceChatUpdateRequired) {
        this.forceChatUpdateRequired = forceChatUpdateRequired;
    }

    public String getForceChatText() {
        return forceChatText;
    }

    public void setForceChatText(String forceChatText) {
        this.forceChatText = forceChatText;
    }

    public void startAnimation(Animation animation) {
        setAnimation(animation);
        setAnimationUpdateRequired(true);
        setUpdateRequired(true);
    }

    public void startAnimation(int animationId, int delay) {
        startAnimation(new Animation(animationId, delay));
    }

    public void startAnimation(int animationId) {
        startAnimation(animationId, 0);
    }

    public void startGraphic(Graphic graphic) {
        setGraphic(graphic);
        setGraphicUpdateRequired(true);
        setUpdateRequired(true);
    }

    public void startGraphic(int graphicId, int delay) {
        startGraphic(new Graphic(graphicId, delay));
    }

    public void startGraphic(int graphicId) {
        startGraphic(graphicId, 0);
    }
}
