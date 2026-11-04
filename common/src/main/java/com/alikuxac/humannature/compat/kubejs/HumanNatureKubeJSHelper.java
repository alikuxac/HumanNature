package com.alikuxac.humannature.compat.kubejs;

import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import net.minecraft.world.entity.player.Player;

public final class HumanNatureKubeJSHelper {
    private HumanNatureKubeJSHelper() {}

    public static boolean isKubeJSLoaded() {
        try {
            Class.forName("dev.latvian.mods.kubejs.KubeJS");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static byte getLimbSeverity(Player player, String limbName) {
        return getLimbSeverity(player, parseLimb(limbName));
    }

    public static byte getLimbSeverity(Player player, LimbType limb) {
        return HumanNatureAPI.getInstance().getInjury().getLimbSeverity(player, limb != null ? limb : LimbType.TORSO);
    }

    public static void setLimbSeverity(Player player, String limbName, int severity) {
        setLimbSeverity(player, parseLimb(limbName), severity);
    }

    public static void setLimbSeverity(Player player, LimbType limb, int severity) {
        HumanNatureAPI.getInstance().getInjury().setLimbSeverity(player, limb != null ? limb : LimbType.TORSO, severity);
    }

    public static boolean hasFracture(Player player) {
        return HumanNatureAPI.getInstance().getInjury().hasFracture(player);
    }

    public static String getFractureTier(Player player) {
        return HumanNatureAPI.getInstance().getInjury().getFractureTier(player).name();
    }

    public static void treatLimb(Player player, String limbName) {
        treatLimb(player, parseLimb(limbName));
    }

    public static void treatLimb(Player player, LimbType limb) {
        HumanNatureAPI.getInstance().getInjury().treatLimb(player, limb != null ? limb : LimbType.TORSO);
    }

    public static void clearInjuries(Player player) {
        HumanNatureAPI.getInstance().getInjury().clearInjuries(player);
    }

    public static byte getCalcium(Player player) {
        return HumanNatureAPI.getInstance().getNutrition().getCalcium(player);
    }

    public static void modifyCalcium(Player player, int delta) {
        HumanNatureAPI.getInstance().getNutrition().modifyCalcium(player, delta);
    }

    public static byte getCarbohydrates(Player player) {
        return HumanNatureAPI.getInstance().getNutrition().getCarbohydrates(player);
    }

    public static void modifyCarbohydrates(Player player, int delta) {
        HumanNatureAPI.getInstance().getNutrition().modifyCarbohydrates(player, delta);
    }

    public static byte getVitamins(Player player) {
        return HumanNatureAPI.getInstance().getNutrition().getVitamins(player);
    }

    public static void modifyVitamins(Player player, int delta) {
        HumanNatureAPI.getInstance().getNutrition().modifyVitamins(player, delta);
    }

    public static float getToxinBuildup(Player player) {
        return HumanNatureAPI.getInstance().getNutrition().getToxinBuildup(player);
    }

    public static void setToxinBuildup(Player player, float amount) {
        HumanNatureAPI.getInstance().getNutrition().setToxinBuildup(player, amount);
    }

    public static float getExertionScore(Player player) {
        return HumanNatureAPI.getInstance().getNutrition().getExertionScore(player);
    }

    public static float getCoreTemperature(Player player) {
        return HumanNatureAPI.getInstance().getTemperature().getCoreTemperature(player);
    }

    public static void modifyCoreTemperature(Player player, float delta) {
        HumanNatureAPI.getInstance().getTemperature().modifyCoreTemperature(player, delta);
    }

    public static float getWetnessBuffer(Player player) {
        return HumanNatureAPI.getInstance().getTemperature().getWetnessBuffer(player);
    }

    public static void setWetnessBuffer(Player player, float amount) {
        HumanNatureAPI.getInstance().getTemperature().setWetnessBuffer(player, amount);
    }

    public static boolean isFreezing(Player player) {
        return HumanNatureAPI.getInstance().getTemperature().isFreezing(player);
    }

    public static float getCarriedWeight(Player player) {
        return HumanNatureAPI.getInstance().getWeight().getCarriedWeight(player);
    }

    public static float getMaxCapacity(Player player) {
        return HumanNatureAPI.getInstance().getWeight().getMaxCapacity(player);
    }

    public static String getEncumbranceTier(Player player) {
        return HumanNatureAPI.getInstance().getWeight().getEncumbranceTier(player).getDisplayName();
    }

    public static boolean isEncumbered(Player player) {
        return HumanNatureAPI.getInstance().getWeight().isEncumbered(player);
    }

    private static LimbType parseLimb(String name) {
        if (name == null) return LimbType.TORSO;
        for (LimbType limb : LimbType.values()) {
            if (limb.name().equalsIgnoreCase(name) || limb.getDisplayName().equalsIgnoreCase(name)) {
                return limb;
            }
        }
        return LimbType.TORSO;
    }
}
