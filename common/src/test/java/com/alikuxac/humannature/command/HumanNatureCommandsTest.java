package com.alikuxac.humannature.command;

import com.alikuxac.humannature.api.blood.BloodType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HumanNatureCommandsTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void parsesShortAndFullBloodTypeNames() {
        assertEquals(BloodType.A_POSITIVE, HumanNatureCommands.parseType("A_POS"));
        assertEquals(BloodType.A_POSITIVE, HumanNatureCommands.parseType("a_pos"));
        assertEquals(BloodType.O_NEGATIVE, HumanNatureCommands.parseType("O_NEG"));
        assertEquals(BloodType.AB_POSITIVE, HumanNatureCommands.parseType("AB_POSITIVE"));
        assertEquals(BloodType.B_POSITIVE, HumanNatureCommands.parseType("B+"));
        assertNull(HumanNatureCommands.parseType("NOPE"));
    }

    @Test
    void feedbackIsUnavailableWithoutAdminPermission() {
        CommandSourceStack source = mock(CommandSourceStack.class);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        HumanNatureCommands.register(dispatcher);

        assertThrows(CommandSyntaxException.class,
                () -> dispatcher.execute("humannature feedback", source));
        verify(source, never()).sendSuccess(any(), anyBoolean());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void adminFeedbackPrivatelyShowsClickableGithubLink() throws CommandSyntaxException {
        CommandSourceStack source = mock(CommandSourceStack.class);
        when(source.hasPermission(2)).thenReturn(true);
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        HumanNatureCommands.register(dispatcher);

        assertEquals(1, dispatcher.execute("humannature feedback", source));

        ArgumentCaptor<Supplier<Component>> output = ArgumentCaptor.forClass((Class) Supplier.class);
        verify(source).sendSuccess(output.capture(), eq(false));
        Component message = output.getValue().get();
        assertTrue(message.getString().contains("GitHub: Click here to report an issue"));
        assertFalse(message.getString().contains("https://"));
        assertTrue(message.getSiblings().stream()
                .map(component -> component.getStyle().getClickEvent())
                .filter(java.util.Objects::nonNull)
                .anyMatch(click -> click.getAction() == ClickEvent.Action.OPEN_URL
                        && click.getValue().equals("https://github.com/alikuxac/HumanNature/issues")));
    }

    @Test
    void placeholderContactsAreVisibleButNotClickable() {
        Component message = HumanNatureCommands.feedbackMessage("YOUR_DISCORD_INVITE_URL", "YOUR_FEEDBACK_EMAIL");

        assertTrue(message.getString().contains("Discord: Not configured yet"));
        assertTrue(message.getString().contains("Email: Not configured yet"));
        assertEquals(1, message.getSiblings().stream()
                .filter(component -> component.getStyle().getClickEvent() != null).count());
    }

    @Test
    void configuredDiscordOpensUrlAndEmailCopiesAddress() {
        Component message = HumanNatureCommands.feedbackMessage(
                "https://discord.gg/example", "feedback@example.com");
        assertTrue(message.getString().contains("Discord: Click here to join Discord"));
        assertTrue(message.getString().contains("Email: Click here to copy email"));
        assertFalse(message.getString().contains("https://"));
        assertFalse(message.getString().contains("feedback@example.com"));
        var actions = message.getSiblings().stream()
                .map(component -> component.getStyle().getClickEvent())
                .filter(java.util.Objects::nonNull)
                .toList();

        assertEquals(3, actions.size());
        assertTrue(actions.stream().anyMatch(click -> click.getAction() == ClickEvent.Action.OPEN_URL
                && click.getValue().equals("https://discord.gg/example")));
        assertTrue(actions.stream().anyMatch(click -> click.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD
                && click.getValue().equals("feedback@example.com")));
    }
}
