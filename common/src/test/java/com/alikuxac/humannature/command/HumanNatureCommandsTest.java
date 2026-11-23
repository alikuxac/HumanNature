package com.alikuxac.humannature.command;

import com.alikuxac.humannature.api.blood.BloodType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HumanNatureCommandsTest {

    @Test
    void parsesShortAndFullBloodTypeNames() {
        assertEquals(BloodType.A_POSITIVE, HumanNatureCommands.parseType("A_POS"));
        assertEquals(BloodType.A_POSITIVE, HumanNatureCommands.parseType("a_pos"));
        assertEquals(BloodType.O_NEGATIVE, HumanNatureCommands.parseType("O_NEG"));
        assertEquals(BloodType.AB_POSITIVE, HumanNatureCommands.parseType("AB_POSITIVE"));
        assertEquals(BloodType.B_POSITIVE, HumanNatureCommands.parseType("B+"));
        assertNull(HumanNatureCommands.parseType("NOPE"));
    }
}
