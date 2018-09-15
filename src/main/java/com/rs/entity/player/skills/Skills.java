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

import java.util.ArrayList;
import java.util.List;

import static com.rs.entity.player.skills.SkillType.*;

public class Skills {

    public static final int MAX_EXPERIENCE = 200_000_000;
    public static final int MIN_EXPERIENCE = 0;
    /**
     * A mapping of level i to the experience required to achieve it.
     */
    public static final int[] EXPERIENCE_TABLE = {
            0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973,
            4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406,
            24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333,
            111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599,
            407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443,
            1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776,
            4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431
    };
    private final List<Skill> skills;
    private transient int combatLevel;
    private transient int totalLevel;

    public Skills(List<Skill> skills) {
        this.skills = skills;
        updateTotalLevel();
        updateCombatLevel();
    }

    public Skills() {
        skills = new ArrayList<>();
        reset();
    }

    public void reset() {
        skills.clear();

        for (SkillType type : SkillType.values()) {
            Skill skill = new Skill(type);
            skills.add(skill);
        }
        addExperience(HITPOINTS, 1154);
        updateTotalLevel();
        updateCombatLevel();
    }

    public int experience(SkillType type) {
        return skill(type).getExperience();
    }

    public int level(SkillType type) {
        return skill(type).getLevel();
    }

    public int maxLevel(SkillType type) {
        return skill(type).getMaxLevel();
    }

    public void addExperience(SkillType type, int experience) {
        Skill skill = skill(type);
        setExperience(type, normalizeExp(skill.getExperience() + experience));
    }

    public void setExperience(SkillType type, int newExperience) {
        newExperience = normalizeExp(newExperience);

        Skill skill = skill(type);
        int newLevel = levelForExp(newExperience);
        int levelChange = newLevel - skill.getMaxLevel();
        skill.setExperience(newExperience);

        if (levelChange != 0) {
            updateTotalLevel(levelChange);
            skill.setMaxLevel(skill.getMaxLevel() + levelChange);
            skill.setLevel(skill.getLevel() + levelChange);

            if (type.isCombatSkill()) {
                updateCombatLevel();
            }
        }
    }

    private void updateTotalLevel(int change) {
        totalLevel += change;
    }

    public void updateTotalLevel() {
        totalLevel = skills.stream().mapToInt(Skill::getMaxLevel).sum();
    }

    public void updateCombatLevel() {
        combatLevel = combatLevel(maxLevel(ATTACK), maxLevel(STRENGTH), maxLevel(MAGIC), maxLevel(RANGED),
                maxLevel(DEFENCE), maxLevel(HITPOINTS), maxLevel(PRAYER));
    }

    private Skill skill(SkillType type) {
        return skills.get(type.ordinal());
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public static int combatLevel(double atk, double str, double mag, double rng, double def, double hp, double pry) {
        // Source: http://runescape.wikia.com/wiki/Combat_level#History
        double k = 1.3d * Math.max(atk + str, Math.max(2 * mag, 2 * rng));
        double total = k + def + hp + Math.floor(0.5d * pry);
        return (int) Math.floor(total / 4);
    }

    public static int levelForExp(int exp) {
        // Source: https://www.reddit.com/r/2007scape/comments/3idn2b/exp_to_lvl_formula/cufhr5q
        for (int level = 0; level < EXPERIENCE_TABLE.length - 1; level++) {
            if (EXPERIENCE_TABLE[level + 1] > exp)
                return level;
        }
        return 99;
    }

    public static int normalizeExp(int exp) {
        return Math.min(MAX_EXPERIENCE, Math.max(exp, MIN_EXPERIENCE));
    }
}
