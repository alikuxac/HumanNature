package com.alikuxac.humannature.command;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class HumanNatureCommands {
    private static final String[] TYPE_NAMES = {
            "O_NEG", "O_POS", "A_NEG", "A_POS", "B_NEG", "B_POS", "AB_NEG", "AB_POS"
    };
    private static final BloodType[] TYPES = {
            BloodType.O_NEGATIVE, BloodType.O_POSITIVE, BloodType.A_NEGATIVE, BloodType.A_POSITIVE,
            BloodType.B_NEGATIVE, BloodType.B_POSITIVE, BloodType.AB_NEGATIVE, BloodType.AB_POSITIVE
    };

    private HumanNatureCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("humannature")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("blood")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(HumanNatureCommands::getBlood)))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("blood_type", StringArgumentType.word())
                                                .suggests(HumanNatureCommands::suggestTypes)
                                                .executes(HumanNatureCommands::setBlood))))
                        .then(Commands.literal("volume")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 5000))
                                                .executes(HumanNatureCommands::setVolume))))));
    }

    static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestTypes(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (BloodType type : TYPES) {
            builder.suggest(type.getDisplayName());
        }
        return builder.buildFuture();
    }

    static BloodType parseType(String input) {
        if (input == null) return null;
        String value = input.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            if (TYPE_NAMES[i].equals(value)) return TYPES[i];
        }
        for (BloodType type : BloodType.values()) {
            if (type.name().equals(value) || type.getDisplayName().equalsIgnoreCase(input.trim())) {
                return type;
            }
        }
        return null;
    }

    private static int getBlood(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(target.getUUID());
        BloodType type = data.getBloodType();
        String typeName = type != null ? type.getDisplayName() : "Unknown";
        String percent = String.format(Locale.ROOT, "%.0f%%", data.getBloodVolume() / 50.0);
        context.getSource().sendSuccess(() -> Component.literal(
                "§c[HumanNature] " + target.getName().getString() + " — Blood Type: " + typeName
                        + ", Volume: " + data.getBloodVolume() + " / 5000 ml (" + percent + ")"), false);
        return 1;
    }

    private static int setBlood(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        BloodType type = parseType(StringArgumentType.getString(context, "blood_type"));
        if (type == null) {
            context.getSource().sendFailure(Component.literal("§c[HumanNature] Unknown blood type: "
                    + StringArgumentType.getString(context, "blood_type")));
            return 0;
        }
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(target.getUUID());
        data.setBloodType(type);
        PhysiologyEventHandler.savePlayerBloodNbt(target, data);
        PhysiologyEventHandler.syncBloodToClient(target, data);
        context.getSource().sendSuccess(() -> Component.literal("§a[HumanNature] Set "
                + target.getName().getString() + " blood type to " + type.getDisplayName()), true);
        return 1;
    }

    private static int setVolume(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        int amount = IntegerArgumentType.getInteger(context, "amount");
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(target.getUUID());
        data.setBloodVolume((short) amount);
        PhysiologyEventHandler.savePlayerBloodNbt(target, data);
        PhysiologyEventHandler.syncBloodToClient(target, data);
        context.getSource().sendSuccess(() -> Component.literal("§a[HumanNature] Set "
                + target.getName().getString() + " blood volume to " + data.getBloodVolume() + " ml"), true);
        return 1;
    }
}
