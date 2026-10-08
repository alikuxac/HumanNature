package com.alikuxac.humannature.modules.injury;

public enum LimbType {
    HEAD(0, "Head"),
    TORSO(1, "Torso"),
    MAIN_ARM(2, "Main Arm"),
    OFF_ARM(3, "Off Arm"),
    LEFT_LEG(4, "Left Leg"),
    RIGHT_LEG(5, "Right Leg");

    private final int index;
    private final String displayName;

    LimbType(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LimbType fromIndex(int index) {
        for (LimbType type : values()) {
            if (type.index == index) return type;
        }
        return HEAD;
    }
}
