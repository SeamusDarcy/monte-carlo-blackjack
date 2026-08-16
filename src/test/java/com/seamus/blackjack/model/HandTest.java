package com.seamus.blackjack.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandTest {

    private Hand handOf(Rank... ranks) {
        Hand hand = new Hand();
        for (Rank rank : ranks) {
            hand.addCard(new Card(Suit.HEARTS, rank));
        }
        return hand;
    }

    // total without aces maths logic
    @Test
    void kingSevenIsSeventeen() {
        assertEquals(17, handOf(Rank.KING, Rank.SEVEN).getTotal());
    }

    // an ace stays at 11 when it fits
    @Test
    void aceKingIsTwentyOne() {
        assertEquals(21, handOf(Rank.ACE, Rank.KING).getTotal());
    }

    // two aces cant both be 11 at the same time
    @Test
    void aceAceIsTwelve() {
        assertEquals(12, handOf(Rank.ACE, Rank.ACE).getTotal());
    }

    // ace is turning value to 1 to avoid busting
    @Test
    void aceFiveKingIsSixteen() {
        assertEquals(16, handOf(Rank.ACE, Rank.FIVE, Rank.KING).getTotal());
    }

    // 3 aces = 11 + 1 + 1 with 2 low one high
    @Test
    void aceAceAceIsThirteen() {
        assertEquals(13, handOf(Rank.ACE, Rank.ACE, Rank.ACE).getTotal());
    }

    // a soft ace switches to hard hand when it has to
    @Test
    void softSeventeenBecomesHardSeventeen() {
        assertEquals(17, handOf(Rank.ACE, Rank.SIX, Rank.KING).getTotal());
    }

    // confirms bust when total is over and no aces present
    @Test
    void kingQueenFiveIsBust() {
        assertEquals(25, handOf(Rank.KING, Rank.QUEEN, Rank.FIVE).getTotal());
    }
}
