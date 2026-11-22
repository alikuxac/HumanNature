package com.alikuxac.humannature.advancement;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.init.ModEnchantments;
import com.alikuxac.humannature.init.ModItems;
import com.alikuxac.humannature.item.BloodBagItem;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.physiology.NutritionEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import com.alikuxac.humannature.modules.weight.WeightEventHandler;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameplayAdvancementsTest {
    private final Supplier<HumanNatureTrigger> previousTrigger = HumanNatureAdvancements.GAMEPLAY_EVENT;
    private final Supplier<Double> previousCapacity = ServerConfig.WEIGHT_MAX_CAPACITY;
    private final Supplier<Boolean> previousWeight = ServerConfig.ENABLE_WEIGHT;
    private final Supplier<Boolean> previousSplint = ServerConfig.ENABLE_SPLINT;
    private final Supplier<Boolean> previousNutrition = ServerConfig.ENABLE_NUTRIENT_RESERVES;
    private final Supplier<Item> previousBloodBag = ModItems.BLOOD_BAG;
    private HumanNatureTrigger output;
    private ServerPlayer player;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        output = mock(HumanNatureTrigger.class);
        HumanNatureAdvancements.GAMEPLAY_EVENT = () -> output;
        player = serverPlayer();
    }

    @AfterEach
    void restore() {
        HumanNatureAdvancements.GAMEPLAY_EVENT = previousTrigger;
        ServerConfig.WEIGHT_MAX_CAPACITY = previousCapacity;
        ServerConfig.ENABLE_WEIGHT = previousWeight;
        ServerConfig.ENABLE_SPLINT = previousSplint;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = previousNutrition;
        ModItems.BLOOD_BAG = previousBloodBag;
    }

    @Test
    void bothSplintPathsAwardOnlySuccessfulTreatment() throws Exception {
        var splint = mock(SplintItem.class, CALLS_REAL_METHODS);
        var tier = SplintItem.class.getDeclaredField("tier");
        tier.setAccessible(true);
        tier.set(splint, SplintTier.WOODEN);
        var stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(splint);
        assertFalse(InjuryEventHandler.handleQuickTreat(player, LimbType.LEFT_LEG, stack));
        verifyNoInteractions(output);

        InjuryModule.startFractureTimer(player.getUUID(), InjuryTier.TIER_1_SINGLE_LEG);
        assertTrue(InjuryEventHandler.handleQuickTreat(player, LimbType.LEFT_LEG, stack));
        verify(output).trigger(player, "splint_applied");

        InjuryModule.startFractureTimer(player.getUUID(), InjuryTier.TIER_1_SINGLE_LEG);
        splint.finishUsingItem(stack, player.level(), player);
        verify(output, times(2)).trigger(player, "splint_applied");
        verify(output, never()).trigger(player, "fracture_healed");

        ServerConfig.ENABLE_SPLINT = () -> false;
        InjuryModule.startFractureTimer(player.getUUID(), InjuryTier.TIER_1_SINGLE_LEG);
        assertFalse(InjuryEventHandler.handleQuickTreat(player, LimbType.LEFT_LEG, stack));
        splint.finishUsingItem(stack, player.level(), player);
        verify(output, times(2)).trigger(player, "splint_applied");
    }

    @Test
    void naturalRecoveryDispatchesAfterCalciumBuffExpires() {
        InjuryModule.startFractureTimer(player.getUUID(), InjuryTier.TIER_1_SINGLE_LEG);
        var physiology = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        physiology.setCalciumAbsorptionTicks(10);
        InjuryEventHandler.reduceFractureTimer(player, 1, true);
        verifyNoInteractions(output);
        physiology.setCalciumAbsorptionTicks(0);
        InjuryEventHandler.reduceFractureTimer(player, 100_000, true);
        verify(output).trigger(player, "fracture_healed");
        InjuryEventHandler.reduceFractureTimer(player, 100_000, true);
        verify(output, times(1)).trigger(player, "fracture_healed");
    }

    @Test
    void nutritionRequiresAllThreeReservesAndAConsumptionTransition() {
        var data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        data.setCalcium(100);
        data.setCarbohydrates(100);
        data.setVitamins(79);
        var fruit = mock(ItemStack.class);
        when(fruit.is(NutritionEventHandler.FRUIT_FOODS)).thenReturn(true);
        NutritionEventHandler.handleItemConsumption(player, fruit);
        assertEquals(99, data.getVitamins());
        verifyNoInteractions(output);
        NutritionEventHandler.handleItemConsumption(player, fruit);
        assertEquals(100, data.getVitamins());
        verify(output).trigger(player, "max_nutrition");
        NutritionEventHandler.handleItemConsumption(player, fruit);
        verify(output, times(1)).trigger(player, "max_nutrition");

        data.setCalcium(99);
        NutritionEventHandler.handleItemConsumption(player, fruit);
        verify(output, times(1)).trigger(player, "max_nutrition");
        var calciumFood = mock(ItemStack.class);
        when(calciumFood.is(NutritionEventHandler.CALCIUM_RICH_FOODS)).thenReturn(true);
        ServerConfig.ENABLE_NUTRIENT_RESERVES = () -> false;
        NutritionEventHandler.handleItemConsumption(player, calciumFood);
        assertEquals(99, data.getCalcium());
        verify(output, times(1)).trigger(player, "max_nutrition");
    }

    @Test
    void overburdenedDispatchesOnlyOnTierEntry() {
        ServerConfig.WEIGHT_MAX_CAPACITY = () -> 50.0;
        for (int i = 0; i < 18; i++) player.getInventory().items.set(i, new ItemStack(Items.COBBLESTONE, 64));
        WeightEventHandler.recalculateWeight(player);
        WeightEventHandler.recalculateWeight(player);
        verify(output).trigger(player, "overburdened");
        player.getInventory().clearContent();
        WeightEventHandler.recalculateWeight(player);
        for (int i = 0; i < 18; i++) player.getInventory().items.set(i, new ItemStack(Items.COBBLESTONE, 64));
        WeightEventHandler.recalculateWeight(player);
        verify(output, times(2)).trigger(player, "overburdened");
    }

    @Test
    void packMuleUsesTheRegistryMaximumAndOnlyEquippedArmor() {
        var enchantment = mock(Enchantment.class);
        when(enchantment.getMaxLevel()).thenReturn(4);
        var registry = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());
        var holder = Registry.registerForHolder(registry, ModEnchantments.LOAD_BEARING, enchantment);
        registry.freeze();
        var access = mock(RegistryAccess.class);
        when(access.lookupOrThrow(Registries.ENCHANTMENT)).thenReturn(registry.asLookup());
        when(player.registryAccess()).thenReturn(access);
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set(holder, 3);
        armor.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(armor);
        WeightEventHandler.onEquipmentChange(player);
        verifyNoInteractions(output);

        enchantments.set(holder, 4);
        armor.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(ItemStack.EMPTY);
        player.getInventory().items.set(0, armor);
        WeightEventHandler.onEquipmentChange(player);
        verifyNoInteractions(output);
        when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(armor);
        WeightEventHandler.onEquipmentChange(player);
        verify(output).trigger(player, "max_load_bearing");
        WeightEventHandler.onPlayerLoggedIn(player);
        verify(output, times(2)).trigger(player, "max_load_bearing");
        ServerConfig.ENABLE_WEIGHT = () -> false;
        WeightEventHandler.onEquipmentChange(player);
        verify(output, times(2)).trigger(player, "max_load_bearing");
    }

    @Test
    void hypothermiaDispatchesOnEntryRatherThanEveryTick() {
        var data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
        data.setHeatScanCooldown(100);
        when(player.getTicksRequiredToFreeze()).thenReturn(140);
        when(player.getTicksFrozen()).thenReturn(100);
        TemperatureEventHandler.onPlayerTick(player);
        TemperatureEventHandler.onPlayerTick(player);
        assertTrue(data.isHypothermiaDebuffActive());
        verify(output).trigger(player, "hypothermia");
        when(player.getTicksFrozen()).thenReturn(0);
        TemperatureEventHandler.onPlayerTick(player);
        assertFalse(data.isHypothermiaDebuffActive());
        when(player.getTicksFrozen()).thenReturn(100);
        TemperatureEventHandler.onPlayerTick(player);
        verify(output, times(2)).trigger(player, "hypothermia");
        verify(output, never()).trigger(any(), eq("heat_exhaustion"));
    }

    @Test
    void bloodAwardsTheActorOnlyForSelfDrawOrEffectiveCompatibleDonation() {
        var bag = mock(BloodBagItem.class, CALLS_REAL_METHODS);
        ModItems.BLOOD_BAG = () -> Items.PAPER;
        var ownBlood = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        ownBlood.setBloodType(BloodType.O_NEGATIVE);
        ownBlood.setBloodVolume((short) 5000);
        PhysiologyEventHandler.savePlayerBloodNbt(player, ownBlood);
        var stack = new ItemStack(Items.PAPER);
        bag.finishUsingItem(stack, player.level(), player);
        assertEquals(4500, ownBlood.getBloodVolume());
        assertEquals(500, BloodBagItem.getBloodVolume(stack));
        verify(output).trigger(player, "blood_transfused");

        var recipient = serverPlayer();
        var recipientData = PhysiologyModule.getOrCreatePhysiologyData(recipient.getUUID());
        recipientData.setBloodType(BloodType.A_POSITIVE);
        recipientData.setBloodVolume((short) 3000);
        PhysiologyEventHandler.savePlayerBloodNbt(recipient, recipientData);
        bag.interactLivingEntity(stack, player, recipient, InteractionHand.MAIN_HAND);
        assertEquals(3500, recipientData.getBloodVolume());
        verify(output, times(2)).trigger(player, "blood_transfused");
        verify(output, never()).trigger(eq(recipient), anyString());

        recipientData.setBloodVolume((short) 5000);
        PhysiologyEventHandler.savePlayerBloodNbt(recipient, recipientData);
        BloodBagItem.setData(stack, new BloodBagItem.BloodBagData(500, BloodType.O_NEGATIVE));
        bag.interactLivingEntity(stack, player, recipient, InteractionHand.MAIN_HAND);
        verify(output, times(2)).trigger(player, "blood_transfused");

        recipientData.setBloodType(BloodType.O_NEGATIVE);
        recipientData.setBloodVolume((short) 3000);
        PhysiologyEventHandler.savePlayerBloodNbt(recipient, recipientData);
        BloodBagItem.setData(stack, new BloodBagItem.BloodBagData(500, BloodType.A_POSITIVE));
        bag.interactLivingEntity(stack, player, recipient, InteractionHand.MAIN_HAND);
        assertEquals(3000, recipientData.getBloodVolume());
        assertEquals(100.0f, recipientData.getToxinBuildup());
        verify(output, times(2)).trigger(player, "blood_transfused");

        when(player.isShiftKeyDown()).thenReturn(true);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(stack);
        BloodBagItem.setData(stack, new BloodBagItem.BloodBagData(500, BloodType.O_NEGATIVE));
        bag.use(player.level(), player, InteractionHand.MAIN_HAND);
        assertEquals(5000, ownBlood.getBloodVolume());
        stack = new ItemStack(Items.PAPER);
        recipientData.setBloodVolume((short) 5000);
        PhysiologyEventHandler.savePlayerBloodNbt(recipient, recipientData);
        bag.interactLivingEntity(stack, player, recipient, InteractionHand.MAIN_HAND);
        assertEquals(4500, recipientData.getBloodVolume());
        verify(output, times(2)).trigger(player, "blood_transfused");
    }

    @Test
    void realTriggerAwardsOnlyMatchingEventsWhosePlayerPredicatePasses() {
        var advancements = mock(PlayerAdvancements.class);
        when(player.getAdvancements()).thenReturn(advancements);
        when(player.position()).thenReturn(Vec3.ZERO);
        when(player.serverLevel().getRandom()).thenReturn(RandomSource.create(1));
        when(player.serverLevel().getServer()).thenReturn(mock(MinecraftServer.class, RETURNS_DEEP_STUBS));
        var holder = mock(AdvancementHolder.class);
        var trigger = new HumanNatureTrigger();
        var allowed = new HumanNatureTrigger.TriggerInstance(Optional.empty(), "splint_applied");
        var denied = new HumanNatureTrigger.TriggerInstance(Optional.of(ContextAwarePredicate.create(
                LootItemRandomChanceCondition.randomChance(0.0f).build())), "splint_applied");
        trigger.addPlayerListener(advancements, new CriterionTrigger.Listener<>(allowed, holder, "allowed"));
        trigger.addPlayerListener(advancements, new CriterionTrigger.Listener<>(denied, holder, "denied"));
        trigger.trigger(player, "fracture_healed");
        verifyNoInteractions(advancements);
        trigger.trigger(player, "splint_applied");
        verify(advancements).award(holder, "allowed");
        verify(advancements, never()).award(holder, "denied");
    }

    @Test
    void recoveryCannotAwardWhileThePlayerIsDead() {
        InjuryModule.startFractureTimer(player.getUUID(), InjuryTier.TIER_1_SINGLE_LEG);
        var physiology = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        physiology.setCalciumAbsorptionTicks(10);
        InjuryEventHandler.reduceFractureTimer(player, 1, true);
        when(player.isAlive()).thenReturn(false);
        InjuryEventHandler.reduceFractureTimer(player, 100_000, true);
        verifyNoInteractions(output);
    }

    @Test
    void creativeAndSpectatorPlayersCannotDispatchMilestones() {
        when(player.isCreative()).thenReturn(true);
        HumanNatureAdvancements.trigger(player, "splint_applied");
        when(player.isCreative()).thenReturn(false);
        when(player.isSpectator()).thenReturn(true);
        HumanNatureAdvancements.trigger(player, "splint_applied");
        HumanNatureAdvancements.trigger(null, "splint_applied");
        verifyNoInteractions(output);
    }

    private ServerPlayer serverPlayer() {
        var result = mock(ServerPlayer.class);
        var level = mock(ServerLevel.class);
        when(result.getUUID()).thenReturn(UUID.randomUUID());
        when(result.isAlive()).thenReturn(true);
        when(result.level()).thenReturn(level);
        when(result.serverLevel()).thenReturn(level);
        when(result.getPersistentData()).thenReturn(new CompoundTag());
        when(result.getRandom()).thenReturn(RandomSource.create(1));
        when(result.damageSources()).thenReturn(mock(DamageSources.class));
        when(result.getCooldowns()).thenReturn(new ItemCooldowns());
        when(result.getInventory()).thenReturn(new Inventory(result));
        when(result.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
        result.connection = mock(ServerGamePacketListenerImpl.class);
        return result;
    }
}
