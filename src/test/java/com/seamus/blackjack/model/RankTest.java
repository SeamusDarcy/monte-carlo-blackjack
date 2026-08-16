package com.seamus.blackjack.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankTest {

    // check all face cards return value 10
    @Test
    void faceCardsAreWorthTen() {
        assertEquals(10, Rank.JACK.getValue());
        assertEquals(10, Rank.QUEEN.getValue());
        assertEquals(10, Rank.KING.getValue());
    }

    // ace based logic before soft is 11
    @Test
    void aceIsWorthEleven() {
        assertEquals(11, Rank.ACE.getValue());
    }
}
