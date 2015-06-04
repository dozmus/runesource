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

import java.util.Deque;
import java.util.LinkedList;

/**
 * Handles the movement of a Player.
 * 
 * @author blakeman8192
 */
public class MovementHandler {

	private final Player player;
	private Deque<Point> waypoints = new LinkedList<Point>();
	private boolean runToggled = false;
	private boolean runPath = false;

	/**
	 * Creates a new MovementHandler.
	 * 
	 * @param player
	 *            the Player
	 */
	public MovementHandler(Player player) {
		this.player = player;
	}

	public void process() {
		Point walkPoint = null;
		Point runPoint = null;

		// Handle the movement.
		walkPoint = waypoints.poll();
		if (isRunToggled() || isRunPath()) {
			runPoint = waypoints.poll();
		}
		if (walkPoint != null && walkPoint.getDirection() != -1) {
			player.getPosition().move(Misc.DIRECTION_DELTA_X[walkPoint.getDirection()], Misc.DIRECTION_DELTA_Y[walkPoint.getDirection()]);
			player.setPrimaryDirection(walkPoint.getDirection());
		}
		if (runPoint != null && runPoint.getDirection() != -1) {
			player.getPosition().move(Misc.DIRECTION_DELTA_X[runPoint.getDirection()], Misc.DIRECTION_DELTA_Y[runPoint.getDirection()]);
			player.setSecondaryDirection(runPoint.getDirection());
		}

		// Check for region changes.
		int deltaX = player.getPosition().getX() - player.getCurrentRegion().getRegionX() * 8;
		int deltaY = player.getPosition().getY() - player.getCurrentRegion().getRegionY() * 8;
		if (deltaX < 16 || deltaX >= 88 || deltaY < 16 || deltaY > 88) {
			if (!(player instanceof Npc)) {
				player.sendMapRegion();
			}
		}
	}

	/**
	 * Resets the walking queue.
	 */
	public void reset() {
		setRunPath(false);
		waypoints.clear();

		// Set the base point as this position.
		Position p = player.getPosition();
		waypoints.add(new Point(p.getX(), p.getY(), -1));
	}

	/**
	 * Finishes the current path.
	 */
	public void finish() {
		waypoints.removeFirst();
	}

	/**
	 * Adds a position to the path.
	 * 
	 * @param position
	 *            the position
	 */
	public void addToPath(Position position) {
		if (waypoints.size() == 0) {
			reset();
		}
		Point last = waypoints.peekLast();
		int deltaX = position.getX() - last.getX();
		int deltaY = position.getY() - last.getY();
		int max = Math.max(Math.abs(deltaX), Math.abs(deltaY));
		for (int i = 0; i < max; i++) {
			if (deltaX < 0) {
				deltaX++;
			} else if (deltaX > 0) {
				deltaX--;
			}
			if (deltaY < 0) {
				deltaY++;
			} else if (deltaY > 0) {
				deltaY--;
			}
			addStep(position.getX() - deltaX, position.getY() - deltaY);
		}
	}

	/**
	 * Adds a step.
	 * 
	 * @param x
	 *            the X coordinate
	 * @param y
	 *            the Y coordinate
	 */
	private void addStep(int x, int y) {
		if (waypoints.size() >= 100) {
			return;
		}
		Point last = waypoints.peekLast();
		int deltaX = x - last.getX();
		int deltaY = y - last.getY();
		int direction = Misc.direction(deltaX, deltaY);
		if (direction > -1) {
			waypoints.add(new Point(x, y, direction));
		}
	}

	/**
	 * Toggles the running flag.
	 * 
	 * @param runToggled
	 *            the flag
	 */
	public void setRunToggled(boolean runToggled) {
		this.runToggled = runToggled;
	}

	/**
	 * Gets whether or not run is toggled.
	 * 
	 * @return run toggled
	 */
	public boolean isRunToggled() {
		return runToggled;
	}

	/**
	 * Toggles running for the current path only.
	 * 
	 * @param runPath
	 *            the flag
	 */
	public void setRunPath(boolean runPath) {
		this.runPath = runPath;
	}

	/**
	 * Gets whether or not we're running for the current path.
	 * 
	 * @return running
	 */
	public boolean isRunPath() {
		return runPath;
	}

	/**
	 * An internal Position type class with support for direction.
	 * 
	 * @author blakeman8192
	 */
	private class Point extends Position {

		private int direction;

		/**
		 * Creates a new Point.
		 * 
		 * @param x
		 *            the X coordinate
		 * @param y
		 *            the Y coordinate
		 * @param direction
		 *            the direction to this point
		 */
		public Point(int x, int y, int direction) {
			super(x, y);
			setDirection(direction);
		}

		/**
		 * Sets the direction.
		 * 
		 * @param direction
		 *            the direction
		 */
		public void setDirection(int direction) {
			this.direction = direction;
		}

		/**
		 * Gets the direction.
		 * 
		 * @return the direction
		 */
		public int getDirection() {
			return direction;
		}

	}

}
