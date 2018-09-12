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

public final class PublicChat {

    private final int color; // XXX document color
    private final int effects; // XXX document effects
    private final byte[] text;

    public PublicChat(int color, int effects, byte[] text) {
        this.color = color;
        this.effects = effects;
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public int getEffects() {
        return effects;
    }

    public byte[] getText() {
        return text;
    }
}
