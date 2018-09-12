package com.rs.entity.action;
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

import com.rs.entity.Position;

public final class AsyncMovement {

    private final Position startPosition;
    private final Position endPosition;
    private final int startToEndSpeed;
    private final int endToStartSpeed;
    private final int direction;

    public AsyncMovement(Position startPosition, Position endPosition, int startToEndSpeed, int endToStartSpeed,
                         int direction) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.startToEndSpeed = startToEndSpeed;
        this.endToStartSpeed = endToStartSpeed;
        this.direction = direction;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public int getStartToEndSpeed() {
        return startToEndSpeed;
    }

    public int getEndToStartSpeed() {
        return endToStartSpeed;
    }

    public int getDirection() {
        return direction;
    }
}
