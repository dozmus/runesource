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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SkillsTest {

    private Skills skills;

    @BeforeEach
    void setUp() {
        skills = new Skills();
    }

    @Test
    void testLevels_NewAccount() {
        assertEquals(3, skills.getCombatLevel());
        assertEquals(30, skills.getTotalLevel());
    }

    @Test
    void testLevels_MaxedAccount() {
        List<Skill> skillList = new ArrayList<>();

        for (SkillType type : SkillType.values()) {
            skillList.add(new Skill(type, 99, 99, 13034431));
        }

        Skills skills = new Skills(skillList);
        assertEquals(126, skills.getCombatLevel());
        assertEquals(2079, skills.getTotalLevel());
    }

    @Test
    void testAddExperience() {
        skills.addExperience(SkillType.ATTACK, 50);
        assertEquals(1, skills.level(SkillType.ATTACK));
        assertEquals(1, skills.maxLevel(SkillType.ATTACK));
        assertEquals(30, skills.getTotalLevel());
    }

    @Test
    void testAddExperience_LevelUp_OneLevel() {
        skills.addExperience(SkillType.ATTACK, 83);
        assertEquals(2, skills.level(SkillType.ATTACK));
        assertEquals(2, skills.maxLevel(SkillType.ATTACK));
        assertEquals(31, skills.getTotalLevel());
    }

    @Test
    void testAddExperience_LevelUp_TwoLevels() {
        skills.addExperience(SkillType.ATTACK, 174);
        assertEquals(3, skills.level(SkillType.ATTACK));
        assertEquals(3, skills.maxLevel(SkillType.ATTACK));
        assertEquals(32, skills.getTotalLevel());
    }

    @Test
    void testCombatLevel() {
        assertEquals(3, Skills.combatLevel(1, 1, 1, 1, 1, 10, 1));
        assertEquals(126, Skills.combatLevel(99, 99, 99, 99, 99, 99, 99));
    }

    @Test
    void testLevelForExp() {
        assertEquals(1, Skills.levelForExp(0));
        assertEquals(2, Skills.levelForExp(83));
        assertEquals(99, Skills.levelForExp(13034431));
        assertEquals(99, Skills.levelForExp(20000000));
    }

    @Test
    void testAddExperience_RemoveExperience() {
        assertEquals(3, skills.getCombatLevel());
        assertEquals(30, skills.getTotalLevel());

        for (SkillType type : SkillType.values()) {
            skills.addExperience(type, 13034431);
        }
        assertEquals(126, skills.getCombatLevel());
        assertEquals(2079, skills.getTotalLevel());

        for (SkillType type : SkillType.values()) {
            skills.addExperience(type, -13034431);
        }
        assertEquals(3, skills.getCombatLevel());
        assertEquals(30, skills.getTotalLevel());
    }

    @Test
    void testNormalizeExp() {
        assertEquals(0, Skills.normalizeExp(-1));
        assertEquals(0, Skills.normalizeExp(0));
        assertEquals(100_000, Skills.normalizeExp(100_000));
        assertEquals(200_000_000, Skills.normalizeExp(200_000_000));
        assertEquals(200_000_000, Skills.normalizeExp(200_000_001));
    }
}
