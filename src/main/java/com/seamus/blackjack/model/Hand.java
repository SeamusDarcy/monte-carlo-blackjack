package com.seamus.blackjack.model;

import java.util.ArrayList;

public class Hand {
    private ArrayList<Card> cards = new ArrayList<>();

    public void addCard(Card card){
        cards.add(card);

    }
    public int  getTotal(){
        int totalValue = 0;
        int aceCount = 0;
        for(Card card : cards){
            if(card.getRank() == Rank.ACE){
                aceCount++;
            }
            totalValue = totalValue + card.getRank().getValue();
        }
        // ace splitting logic
        while (totalValue > 21 && aceCount != 0){
            totalValue = totalValue - 10;
            aceCount = aceCount - 1;
        }
        return totalValue;
    }

    // a natural: 21 with the first two cards
    public boolean isBlackjack(){
        return cards.size() == 2 && getTotal() == 21;
    }

    public Card getCard(int i){
        return cards.get(i);
    }

    public int size() {
        return cards.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Card card : cards) {
            stringBuilder.append(card).append("\n");
        }
       return stringBuilder.toString();
    }
}
