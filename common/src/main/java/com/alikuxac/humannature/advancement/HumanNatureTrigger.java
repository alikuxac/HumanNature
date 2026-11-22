package com.alikuxac.humannature.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class HumanNatureTrigger extends SimpleCriterionTrigger<HumanNatureTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String eventId) {
        trigger(player, instance -> instance.matches(eventId));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, String eventId) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.fieldOf("event_id").forGetter(TriggerInstance::eventId)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(String eventId) {
            return this.eventId.equals(eventId);
        }
    }
}
