package com.alikuxac.humannature.item;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.init.ModItems;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodBagItem extends Item {
    public static final String TAG_BLOOD_TYPE = "BloodType";
    public static final String TAG_BLOOD_VOLUME = "BloodVolume";

    public static final int MAX_CAPACITY = 1000;
    public static final int STEP_VOLUME = 500;
    public static final short SAFE_EXTRACTION_THRESHOLD = 3500;
    public static final int CHANNEL_DURATION = 30;
    public static final float NEEDLE_DAMAGE = 1.0F;

    public record BloodBagData(int storedMl, @Nullable BloodType bloodType) {
        public static final BloodBagData EMPTY = new BloodBagData(0, null);

        public boolean isEmpty() {
            return storedMl <= 0 || bloodType == null;
        }

        public boolean isFull() {
            return storedMl >= MAX_CAPACITY;
        }

        public boolean canAccept(BloodType donorType) {
            if (donorType == null) return false;
            if (storedMl >= MAX_CAPACITY) return false;
            return isEmpty() || this.bloodType == donorType;
        }
    }

    public BloodBagItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static BloodBagData getData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return BloodBagData.EMPTY;
        CompoundTag tag = customData.copyTag();
        int vol = tag.contains(TAG_BLOOD_VOLUME) ? tag.getInt(TAG_BLOOD_VOLUME) : 0;
        BloodType type = tag.contains(TAG_BLOOD_TYPE) ? BloodType.fromId(tag.getByte(TAG_BLOOD_TYPE)) : null;
        if (vol <= 0 || type == null) return BloodBagData.EMPTY;
        return new BloodBagData(vol, type);
    }

    public static void setData(ItemStack stack, BloodBagData data) {
        if (data.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
            stack.set(DataComponents.MAX_STACK_SIZE, 16);
        } else {
            stack.set(DataComponents.MAX_STACK_SIZE, 1);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                if (data.bloodType() != null) {
                    tag.putByte(TAG_BLOOD_TYPE, data.bloodType().getId());
                }
                tag.putInt(TAG_BLOOD_VOLUME, data.storedMl());
            });
        }
    }

    public static boolean isEmpty(ItemStack stack) {
        return getData(stack).isEmpty();
    }

    public static BloodType getBloodType(ItemStack stack) {
        return getData(stack).bloodType();
    }

    public static short getBloodVolume(ItemStack stack) {
        return (short) getData(stack).storedMl();
    }

    public static float fillRatioForStored(int storedMl) {
        return (float) Math.max(0, Math.min(MAX_CAPACITY, storedMl)) / MAX_CAPACITY;
    }

    public static int barWidthForStored(int storedMl) {
        return Math.round(fillRatioForStored(storedMl) * 13.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getData(stack).storedMl() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return barWidthForStored(getData(stack).storedMl());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xDC143C;
    }

    public static ItemStack createFilled(BloodType bloodType, short volume) {
        ItemStack stack = new ItemStack(ModItems.BLOOD_BAG.get());
        setData(stack, new BloodBagData(volume, bloodType));
        return stack;
    }

    public static ItemStack createEmpty() {
        return new ItemStack(ModItems.BLOOD_BAG.get());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHANNEL_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (entity instanceof Player player) {
            if (count % 10 == 0) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        SoundSource.PLAYERS,
                        0.4F,
                        1.4F
                );
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ServerConfig.ENABLE_BLOOD_BAG.get()) {
            return InteractionResultHolder.pass(stack);
        }

        BloodBagData data = getData(stack);

        if (player.isShiftKeyDown()) {
            if (data.isEmpty()) {
                return InteractionResultHolder.pass(stack);
            }

            if (!level.isClientSide()) {
                performTransfusion(player, data.bloodType(), (short) data.storedMl());
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (stack.getCount() > 1) {
                    stack.shrink(1);
                    ItemStack emptyBag = createEmpty();
                    if (!player.getInventory().add(emptyBag)) {
                        player.drop(emptyBag, false);
                    }
                } else {
                    setData(stack, BloodBagData.EMPTY);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        } else {
            if (data.isFull()) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.humannature.blood_bag_full").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            PhysiologyEventHandler.initOrLoadPlayerBlood(player, PhysiologyModule.getOrCreatePhysiologyData(player.getUUID()));
            BloodType playerType = PhysiologyModule.getBloodType(player.getUUID());
            if (playerType == null) playerType = BloodType.O_POSITIVE;

            if (!data.canAccept(playerType)) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.humannature.blood_type_mismatch").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            short playerVol = PhysiologyModule.getBloodVolume(player.getUUID());
            if (playerVol <= SAFE_EXTRACTION_THRESHOLD) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.humannature.blood_too_low").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        if (!level.isClientSide()) {
            short playerVol = PhysiologyModule.getBloodVolume(player.getUUID());
            if (playerVol <= SAFE_EXTRACTION_THRESHOLD) {
                player.displayClientMessage(Component.translatable("message.humannature.blood_too_low").withStyle(ChatFormatting.RED), true);
                return stack;
            }

            PhysiologyEventHandler.initOrLoadPlayerBlood(player, PhysiologyModule.getOrCreatePhysiologyData(player.getUUID()));
            BloodType playerType = PhysiologyModule.getBloodType(player.getUUID());
            if (playerType == null) playerType = BloodType.O_POSITIVE;

            BloodBagData data = getData(stack);
            if (!data.canAccept(playerType)) {
                player.displayClientMessage(Component.translatable("message.humannature.blood_type_mismatch").withStyle(ChatFormatting.RED), true);
                return stack;
            }

            int newStored = Math.min(MAX_CAPACITY, data.storedMl() + STEP_VOLUME);

            PhysiologyModule.modifyBloodVolume(player.getUUID(), -STEP_VOLUME);
            player.hurt(player.damageSources().generic(), NEEDLE_DAMAGE);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);

            PhysiologyEventHandler.savePlayerBloodNbt(player, PhysiologyModule.getOrCreatePhysiologyData(player.getUUID()));
            PhysiologyEventHandler.syncBloodToClient(player, PhysiologyModule.getOrCreatePhysiologyData(player.getUUID()));

            if (stack.getCount() > 1) {
                stack.shrink(1);
                ItemStack filledBag = createFilled(playerType, (short) newStored);
                if (!player.getInventory().add(filledBag)) {
                    player.drop(filledBag, false);
                }
            } else {
                setData(stack, new BloodBagData(newStored, playerType));
            }
            if (ServerConfig.ENABLE_BLOOD_BAG.get()) {
                HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.BLOOD_TRANSFUSED);
            }
        }

        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!ServerConfig.ENABLE_BLOOD_BAG.get()) {
            return InteractionResult.PASS;
        }

        if (interactionTarget instanceof Player targetPlayer) {
            Level level = player.level();
            BloodBagData data = getData(stack);

            if (player.isShiftKeyDown()) {
                if (data.isFull()) {
                    if (!level.isClientSide()) {
                        player.displayClientMessage(Component.translatable("message.humannature.blood_bag_full").withStyle(ChatFormatting.RED), true);
                    }
                    return InteractionResult.FAIL;
                }

                PhysiologyEventHandler.initOrLoadPlayerBlood(targetPlayer, PhysiologyModule.getOrCreatePhysiologyData(targetPlayer.getUUID()));
                BloodType targetType = PhysiologyModule.getBloodType(targetPlayer.getUUID());
                if (targetType == null) targetType = BloodType.O_POSITIVE;

                if (!data.canAccept(targetType)) {
                    if (!level.isClientSide()) {
                        player.displayClientMessage(Component.translatable("message.humannature.blood_type_mismatch").withStyle(ChatFormatting.RED), true);
                    }
                    return InteractionResult.FAIL;
                }

                short targetVol = PhysiologyModule.getBloodVolume(targetPlayer.getUUID());
                if (targetVol <= SAFE_EXTRACTION_THRESHOLD) {
                    if (!level.isClientSide()) {
                        player.displayClientMessage(Component.translatable("message.humannature.target_blood_too_low").withStyle(ChatFormatting.RED), true);
                    }
                    return InteractionResult.FAIL;
                }

                if (!level.isClientSide()) {
                    int newStored = Math.min(MAX_CAPACITY, data.storedMl() + STEP_VOLUME);

                    PhysiologyModule.modifyBloodVolume(targetPlayer.getUUID(), -STEP_VOLUME);
                    targetPlayer.hurt(targetPlayer.damageSources().generic(), NEEDLE_DAMAGE);

                    level.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                            SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);

                    PhysiologyEventHandler.savePlayerBloodNbt(targetPlayer, PhysiologyModule.getOrCreatePhysiologyData(targetPlayer.getUUID()));
                    PhysiologyEventHandler.syncBloodToClient(targetPlayer, PhysiologyModule.getOrCreatePhysiologyData(targetPlayer.getUUID()));

                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                        ItemStack filledBag = createFilled(targetType, (short) newStored);
                        if (!player.getInventory().add(filledBag)) {
                            player.drop(filledBag, false);
                        }
                    } else {
                        setData(stack, new BloodBagData(newStored, targetType));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                if (data.isEmpty()) {
                    return InteractionResult.PASS;
                }

                if (!level.isClientSide()) {
                    if (performTransfusionWithResult(targetPlayer, data.bloodType(), (short) data.storedMl())
                            && !player.getUUID().equals(targetPlayer.getUUID())) {
                        HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.BLOOD_TRANSFUSED);
                    }
                    level.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                            SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);

                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                        ItemStack emptyBag = createEmpty();
                        if (!player.getInventory().add(emptyBag)) {
                            player.drop(emptyBag, false);
                        }
                    } else {
                        setData(stack, BloodBagData.EMPTY);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return InteractionResult.PASS;
    }

    public static void performTransfusion(Player recipient, BloodType donorType, short volume) {
        performTransfusionWithResult(recipient, donorType, volume);
    }

    private static boolean performTransfusionWithResult(Player recipient, BloodType donorType, short volume) {
        if (recipient.level().isClientSide() || donorType == null || volume <= 0) return false;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(recipient.getUUID());
        PhysiologyEventHandler.initOrLoadPlayerBlood(recipient, data);
        BloodType recipientType = data.getBloodType();
        if (recipientType == null) recipientType = BloodType.O_POSITIVE;
        short previousVolume = data.getBloodVolume();

        if (recipientType.canReceiveFrom(donorType)) {
            data.modifyBloodVolume(volume);
            recipient.displayClientMessage(Component.translatable("message.humannature.transfusion_success", volume, donorType.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
        } else {
            data.setToxinBuildup(100.0f);
            data.setLastToxinType(ToxinType.POISON);

            recipient.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1, false, false, true));
            recipient.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 2, false, false, true));
            recipient.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, true));
            recipient.hurt(recipient.damageSources().magic(), 6.0F);

            recipient.displayClientMessage(Component.translatable("message.humannature.transfusion_mismatch").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
        }

        PhysiologyEventHandler.savePlayerBloodNbt(recipient, data);
        PhysiologyEventHandler.syncBloodToClient(recipient, data);
        return recipientType.canReceiveFrom(donorType) && data.getBloodVolume() > previousVolume;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        BloodBagData data = getData(stack);
        if (data.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.humannature.blood_bag_empty").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.humannature.blood_bag_extract_hint").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            String typeStr = data.bloodType() != null ? data.bloodType().getDisplayName() : "Unknown";
            tooltipComponents.add(Component.translatable("tooltip.humannature.blood_bag_filled", typeStr, data.storedMl()).withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.translatable("tooltip.humannature.blood_bag_transfuse_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
