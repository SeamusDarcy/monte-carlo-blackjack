package com.seamus.blackjack.ai;

import com.seamus.blackjack.model.Card;
import com.seamus.blackjack.model.Hand;
import com.seamus.blackjack.model.Rank;
import com.seamus.blackjack.model.Suit;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonteCarloAdvisorTest {

    private Hand handOf(Rank... ranks) {
        Hand hand = new Hand();
        for (Rank rank : ranks) {
            hand.addCard(new Card(Suit.HEARTS, rank));
        }
        return hand;
    }

    // Full deck minus players 2 cards and dealer one face up == 49
    @Test
    void buildPoolLeavesFortyNineCards() {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        advisor.buildPool(handOf(Rank.KING, Rank.SEVEN), new Card(Suit.SPADES, Rank.FOUR));
        assertEquals(49, advisor.getCardCount());
    }

    // checks the right card is decremented tens 16 -> 15, seven + four -> 4, 3
    @Test
    void buildPoolRemovesTheVisibleCards() {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        advisor.buildPool(handOf(Rank.KING, Rank.SEVEN), new Card(Suit.SPADES, Rank.FOUR));
        assertEquals(15, advisor.getDeckCount(10));
        assertEquals(3, advisor.getDeckCount(7));
        assertEquals(3, advisor.getDeckCount(4));
    }

    // checks hand.getTotal is the same as for handstate
    @Test
    void handStateTotalMatchesHandTotal() {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        Hand hand = handOf(Rank.ACE, Rank.SIX, Rank.KING);
        assertEquals(hand.getTotal(), advisor.handState(hand)[0]);
    }

    // every draw is between 2 and 11 over 5_000 draws
    @Test
    void drawCardAlwaysInRange() {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        advisor.buildPool(handOf(Rank.KING, Rank.SEVEN), new Card(Suit.SPADES, Rank.FOUR));
        for (int i = 0; i < 5000; i++) {
            int value = advisor.drawCard();
            assertTrue(value >= 2 && value <= 11);
        }
    }

    // rigs the draw pool to have only tens and then checks every draw returns a 10
    @Test
    void tensOnlyPoolAlwaysDrawsTen() throws Exception {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        Field deckCountField = MonteCarloAdvisor.class.getDeclaredField("deckCount");
        Field cardCountField = MonteCarloAdvisor.class.getDeclaredField("cardCount");
        deckCountField.setAccessible(true);
        cardCountField.setAccessible(true);
        int[] tensOnly = new int[12];
        tensOnly[10] = 16;
        deckCountField.set(advisor, tensOnly);
        cardCountField.set(advisor, 16);
        for (int i = 0; i < 1000; i++) {
            assertEquals(10, advisor.drawCard());
        }
    }
}
