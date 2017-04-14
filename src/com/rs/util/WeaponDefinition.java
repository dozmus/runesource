package com.rs.util;

/**
 * A weapon's definition.
 */
public final class WeaponDefinition implements Comparable<WeaponDefinition> {

    private final int id;
    private final String name;
    private final Type type;

    public WeaponDefinition(int id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(WeaponDefinition o) {
        return id - o.id;
    }

    public enum Type {
        UNARMED(5855, 5857, -1),
        STAFF(328, 331, 329),
        WARHAMMER(425, 428, 426),
        SYTHE(776, 779, 777),
        BATTLE_AXE(1698, 1701, 1699),
        BOW(1764, 1767, 1765),
        CROSSBOW(1749, 1752, 1750),
        SWORD(2423, 2426, 2424),
        FLOWERS(2423, 2426, 2424),
        DAGGER(2276, 2279, 2277),
        MACE(3796, 3799, 3797),
        KNIFE(4446, 4449, 4447),
        DART(4446, 4449, 4447),
        JAVELIN(4446, 4449, 4447),
        THROWNAXE(4446, 4449, 4447),
        SPEAR(4679, 4682, 4680),
        SWORD_2H(4705, 4708, 4706),
        LONGSWORD(4705, 4708, 4706),
        PICKAXE(5570, 5573, 5571),
        CLAW(7762, 7765, 7763),
        HALBERD(8460, 8463, 8461),
        WHIP(12290, 12293, 12291),
        UNKNOWN(-1, -1, -1);

        private final int interfaceId;
        private final int weaponNameId;
        private final int weaponImageId;

        Type(int interfaceId, int weaponNameId, int weaponImageId) {
            this.interfaceId = interfaceId;
            this.weaponNameId = weaponNameId;
            this.weaponImageId = weaponImageId;
        }

        public int getInterfaceId() {
            return interfaceId;
        }

        public int getWeaponNameId() {
            return weaponNameId;
        }

        public int getWeaponImageId() {
            return weaponImageId;
        }
    }
}
