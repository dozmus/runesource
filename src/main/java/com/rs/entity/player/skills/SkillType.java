package com.rs.entity.player.skills;
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

public enum SkillType {

    ATTACK(true),
    DEFENCE(true),
    STRENGTH(true),
    HITPOINTS(true),
    RANGED(true),
    PRAYER(true),
    MAGIC(true),
    COOKING,
    WOODCUTTING,
    FLETCHING,
    FISHING,
    FIREMAKING,
    CRAFTING,
    SMITHING,
    MINING,
    HERBLORE,
    AGILITY,
    THIEVING,
    SLAYER,
    FARMING,
    RUNECRAFTING;

    private final boolean combatSkill;

    SkillType() {
        this(false);
    }

    SkillType(boolean combatSkill) {
        this.combatSkill = combatSkill;
    }

    public boolean isCombatSkill() {
        return combatSkill;
    }
}
