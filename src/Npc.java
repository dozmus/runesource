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
 * A non-player-character. Extends Player so that we can share the many
 * attributes.
 * 
 * @author blakeman8192
 */
public class Npc extends Player {

	/** The NPC ID. */
	private int npcId;

	/** Whether or not the NPC is visible. */
	private boolean isVisible = true;

	/**
	 * Creates a new Npc.
	 * 
	 * @param npcId
	 *            the NPC ID
	 */
	public Npc(int npcId) {
		super(null); // No selection key.
		this.setNpcId(npcId);
	}

	@Override
	public void process() {
		// NPC-specific processing.
		getMovementHandler().process();
	}

	@Override
	public void reset() {
		super.reset();
		// TODO: Any other NPC resetting that isn't in Player.
	}

	/**
	 * Sets the NPC ID.
	 * 
	 * @param npcId
	 *            the npcId
	 */
	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	/**
	 * Gets the NPC ID.
	 * 
	 * @return the npcId
	 */
	public int getNpcId() {
		return npcId;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isVisible() {
		return isVisible;
	}

}
