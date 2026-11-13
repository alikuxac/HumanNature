package com.alikuxac.humannature.api.blood;

import java.util.Random;

public enum BloodType {
    O_NEGATIVE((byte) 0, "O-"),
    O_POSITIVE((byte) 1, "O+"),
    A_NEGATIVE((byte) 2, "A-"),
    A_POSITIVE((byte) 3, "A+"),
    B_NEGATIVE((byte) 4, "B-"),
    B_POSITIVE((byte) 5, "B+"),
    AB_NEGATIVE((byte) 6, "AB-"),
    AB_POSITIVE((byte) 7, "AB+");

    private final byte id;
    private final String displayName;

    BloodType(byte id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public byte getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BloodType fromId(byte id) {
        return switch (id) {
            case 0 -> O_NEGATIVE;
            case 1 -> O_POSITIVE;
            case 2 -> A_NEGATIVE;
            case 3 -> A_POSITIVE;
            case 4 -> B_NEGATIVE;
            case 5 -> B_POSITIVE;
            case 6 -> AB_NEGATIVE;
            case 7 -> AB_POSITIVE;
            default -> O_POSITIVE;
        };
    }

    public static BloodType fromName(String name) {
        if (name == null || name.isEmpty()) return O_POSITIVE;
        for (BloodType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return O_POSITIVE;
    }

    public static BloodType getRandom(Random random) {
        BloodType[] all = values();
        return all[random.nextInt(all.length)];
    }

    public static BloodType getRandom(net.minecraft.util.RandomSource random) {
        BloodType[] all = values();
        return all[random.nextInt(all.length)];
    }

    public boolean canReceiveFrom(BloodType donor) {
        if (donor == null) return false;
        if (this == AB_POSITIVE) return true;
        if (donor == O_NEGATIVE) return true;
        return switch (this) {
            case O_NEGATIVE -> donor == O_NEGATIVE;
            case O_POSITIVE -> donor == O_NEGATIVE || donor == O_POSITIVE;
            case A_NEGATIVE -> donor == A_NEGATIVE || donor == O_NEGATIVE;
            case A_POSITIVE -> donor == A_NEGATIVE || donor == A_POSITIVE || donor == O_NEGATIVE || donor == O_POSITIVE;
            case B_NEGATIVE -> donor == B_NEGATIVE || donor == O_NEGATIVE;
            case B_POSITIVE -> donor == B_NEGATIVE || donor == B_POSITIVE || donor == O_NEGATIVE || donor == O_POSITIVE;
            case AB_NEGATIVE -> donor == AB_NEGATIVE || donor == A_NEGATIVE || donor == B_NEGATIVE || donor == O_NEGATIVE;
            case AB_POSITIVE -> true;
        };
    }

    public boolean canDonateTo(BloodType recipient) {
        return recipient != null && recipient.canReceiveFrom(this);
    }
}

