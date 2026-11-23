package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.item.SplintTier;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.network.SyncInjuryPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SplintRetentionAndOsteogenesisTest {

    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        InjuryModule.clearFractureTimer(playerId);
        PhysiologyModule.getOrCreatePhysiologyData(playerId).resetCalcium();
    }

    @Test
    void testSplintAttachmentAndRemoval() {
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(playerId);
        assertFalse(data.hasAnySplint());
        assertFalse(data.isSplinted(LimbType.LEFT_LEG));
        assertNull(data.getSplint(LimbType.LEFT_LEG));

        // Set limb fracture and attach wooden splint
        data.setSeverity(LimbType.LEFT_LEG, 2);
        data.setSplint(LimbType.LEFT_LEG, SplintTier.WOODEN, SplintTier.WOODEN.getMaxDurability());
        InjuryModule.setInjuryData(playerId, data);

        assertTrue(data.isSplinted(LimbType.LEFT_LEG));
        assertTrue(data.hasAnySplint());
        assertEquals(SplintTier.WOODEN, data.getSplint(LimbType.LEFT_LEG));
        assertEquals(1, data.getSplintDurability(LimbType.LEFT_LEG));
        assertEquals(1, data.getRawSplints()[LimbType.LEFT_LEG.getIndex()]);

        // Removing splint
        data.removeSplint(LimbType.LEFT_LEG);
        assertFalse(data.isSplinted(LimbType.LEFT_LEG));
        assertFalse(data.hasAnySplint());
        assertNull(data.getSplint(LimbType.LEFT_LEG));
        assertEquals(0, data.getSplintDurability(LimbType.LEFT_LEG));
    }

    @Test
    void testSplintDurabilityAndDislodge() {
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(playerId);
        data.setSeverity(LimbType.RIGHT_LEG, 2);
        data.setSplint(LimbType.RIGHT_LEG, SplintTier.REINFORCED, SplintTier.REINFORCED.getMaxDurability());

        assertEquals(4, data.getSplintDurability(LimbType.RIGHT_LEG));

        // Damage by 2 points - should still hold
        boolean broken = data.damageSplint(LimbType.RIGHT_LEG, 2);
        assertFalse(broken);
        assertTrue(data.isSplinted(LimbType.RIGHT_LEG));
        assertEquals(2, data.getSplintDurability(LimbType.RIGHT_LEG));

        // Damage by remaining 2 points - should break/dislodge
        broken = data.damageSplint(LimbType.RIGHT_LEG, 2);
        assertTrue(broken);
        assertFalse(data.isSplinted(LimbType.RIGHT_LEG));
        assertEquals(0, data.getSplintDurability(LimbType.RIGHT_LEG));
    }

    @Test
    void testSplintTierProperties() {
        // Wooden
        assertEquals(1, SplintTier.WOODEN.getMaxDurability());
        assertEquals(0.15F, SplintTier.WOODEN.getSprintDislodgeChance(), 0.001F);
        assertEquals(0.40F, SplintTier.WOODEN.getLightFallDislodgeChance(), 0.001F);
        assertEquals(1.0F, SplintTier.WOODEN.getNaturalHealingMultiplier(), 0.001F);
        assertEquals(20, SplintTier.WOODEN.getSprintDurabilityDrainInterval());

        // Reinforced
        assertEquals(4, SplintTier.REINFORCED.getMaxDurability());
        assertEquals(0.02F, SplintTier.REINFORCED.getSprintDislodgeChance(), 0.001F);
        assertEquals(0.05F, SplintTier.REINFORCED.getLightFallDislodgeChance(), 0.001F);
        assertEquals(1.25F, SplintTier.REINFORCED.getNaturalHealingMultiplier(), 0.001F);
        assertEquals(100, SplintTier.REINFORCED.getSprintDurabilityDrainInterval());

        // Golden
        assertEquals(8, SplintTier.GOLDEN.getMaxDurability());
        assertEquals(0.01F, SplintTier.GOLDEN.getSprintDislodgeChance(), 0.001F);
        assertEquals(0.02F, SplintTier.GOLDEN.getLightFallDislodgeChance(), 0.001F);
        assertEquals(1.50F, SplintTier.GOLDEN.getNaturalHealingMultiplier(), 0.001F);
        assertEquals(150, SplintTier.GOLDEN.getSprintDurabilityDrainInterval());

        // Diamond
        assertEquals(16, SplintTier.DIAMOND.getMaxDurability());
        assertEquals(0.0F, SplintTier.DIAMOND.getSprintDislodgeChance(), 0.001F);
        assertEquals(0.0F, SplintTier.DIAMOND.getLightFallDislodgeChance(), 0.001F);
        assertEquals(1.75F, SplintTier.DIAMOND.getNaturalHealingMultiplier(), 0.001F);
        assertEquals(400, SplintTier.DIAMOND.getSprintDurabilityDrainInterval());

        // Netherite
        assertEquals(24, SplintTier.NETHERITE.getMaxDurability());
        assertEquals(0.0F, SplintTier.NETHERITE.getSprintDislodgeChance(), 0.001F);
        assertEquals(0.0F, SplintTier.NETHERITE.getLightFallDislodgeChance(), 0.001F);
        assertEquals(2.0F, SplintTier.NETHERITE.getNaturalHealingMultiplier(), 0.001F);
        assertEquals(0, SplintTier.NETHERITE.getSprintDurabilityDrainInterval());
    }

    @Test
    void testSplintTierIndexConversion() {
        for (SplintTier tier : SplintTier.values()) {
            int idx = tier.getIndex();
            assertEquals(tier, SplintTier.fromIndex(idx));
        }
        assertNull(SplintTier.fromIndex(0));
        assertNull(SplintTier.fromIndex(99));
    }

    @Test
    void testDowngradeWorstInjuryClearsSplintWhenHealed() {
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(playerId);
        data.setSeverity(LimbType.MAIN_ARM, 1);
        data.setSplint(LimbType.MAIN_ARM, SplintTier.WOODEN, 1);

        assertTrue(data.isSplinted(LimbType.MAIN_ARM));

        // Downgrading worst injury reduces severity to 0, which must remove splint
        boolean downgraded = data.downgradeWorstInjury();
        assertTrue(downgraded);
        assertEquals(0, data.getSeverity(LimbType.MAIN_ARM));
        assertFalse(data.isSplinted(LimbType.MAIN_ARM));
    }

    @Test
    void testSyncInjuryPayloadCodec() {
        byte[] limbs = new byte[]{1, 2, 0, 0, 1, 0};
        byte[] splints = new byte[]{0, 2, 0, 0, 1, 0};

        SyncInjuryPayload original = new SyncInjuryPayload(limbs, splints);
        ByteBuf buf = Unpooled.buffer();

        SyncInjuryPayload.STREAM_CODEC.encode(buf, original);
        SyncInjuryPayload decoded = SyncInjuryPayload.STREAM_CODEC.decode(buf);

        assertArrayEquals(limbs, decoded.limbs());
        assertArrayEquals(splints, decoded.splints());
    }
}
