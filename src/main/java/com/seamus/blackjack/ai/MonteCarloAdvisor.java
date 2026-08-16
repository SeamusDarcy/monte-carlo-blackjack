package com.seamus.blackjack.ai;

import com.seamus.blackjack.model.Card;
import com.seamus.blackjack.model.Hand;
import com.seamus.blackjack.model.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class MonteCarloAdvisor {
    private int[] deckCount;
    private int cardCount;

    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService pool = Executors.newFixedThreadPool(THREADS, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    public MonteCarloAdvisor(){
        deckCount = new int[12];
    }

    public void buildPool(Hand hand, Card card){
        for (int i = 2; i < 12; i++){
            if (i == 10){
                deckCount[i] = 16;
            } else{
                deckCount[i] = 4;
            }
        }
        for (int i = 0; i < hand.size(); i++){
            int value = hand.getCard(i).getRank().getValue();
            deckCount[value]--;
        }
        deckCount[card.getRank().getValue()]--;
        cardCount = 0;
        for (int i = 2; i < 12; i++){
            cardCount += deckCount[i];
        }
    }

    public int drawCard(){
        int roll = ThreadLocalRandom.current().nextInt(cardCount);   // each value has a block, 4 for most value 10 is 16 due to face cards
        int value = 2;
        while (roll >= deckCount[value]){   // if the roll is more than the block length it goes thorough block then remove block length from roll value
            roll -= deckCount[value];
            value++;
        }
        return value;
    }

    public int simulateDealerOnce(int upValue){
        int total = upValue;
        int aceCount = 0;
        if (upValue == 11) {
            aceCount++;
        }
        while (total < 17){
            int card = drawCard();
            if (card == 11){
                aceCount++;
            }
            total += card;
            while (total > 21 && aceCount > 0){
                total -= 10;
                aceCount--;
            }
        }
        return total;
    }

     public double standEV(int playerTotal, int upValue){
        int trials = 200_000;
        int chunk = trials / THREADS;
        List<Future<Long>> futures = new ArrayList<>();
        for (int t = 0; t < THREADS; t++){
            final int count = (t == THREADS - 1) ? (trials - chunk * (THREADS - 1)) : chunk;
            futures.add(pool.submit(() -> {
                long partial = 0;
                for (int i = 0; i < count; i++){
                    int dealer = simulateDealerOnce(upValue);
                    if (dealer > 21){
                        partial += 1;
                    } else if (dealer < playerTotal){
                        partial += 1;
                    } else if (dealer > playerTotal){
                        partial -= 1;
                    }
                }
                return partial;
            }));
        }
        long score = 0;
        try {
            for (Future<Long> future : futures){
                score += future.get();
            }
        } catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
        return (double) score / trials;
     }

    public double doubleEV(int playerTotal, int playerAces, int upValue){
        double score = 0;

        for (int v = 2; v <= 11; v++){
            double p = (double) deckCount[v] / cardCount;   // chance of this card

            int total = playerTotal;
            int aces  = playerAces;
            if (v == 11) aces++;
            total += v;
            while (total > 21 && aces > 0){
                total -= 10;
                aces--;
            }

            if (total > 21){
                score += p * (-1);                      // busted -> lose
            } else {
                score += p * standEV(total, upValue);   // survived -> value of standing here
            }
        }

        return 2 * score;   // doubled stake
    }

    public double hitEV(int playerTotal, int playerAces, int upValue){
        double score = 0;

        for (int v = 2; v <= 11; v++){
            double p = (double) deckCount[v] / cardCount;

            int total = playerTotal;
            int aces  = playerAces;
            if (v == 11) aces++;
            total += v;
            while (total > 21 && aces > 0){
                total -= 10;
                aces--;
            }

            if (total > 21){
                score += p * (-1);
            } else if (total == 21){
                score += p * standEV(total, upValue);
            } else {
                double standHere = standEV(total, upValue);
                double hitAgain  = hitEV(total, aces, upValue);
                score += p * Math.max(standHere, hitAgain); // factors the abilty to hit again and selects if its better EV
            }
        }

        return score;
    }

    public Move advise(int playerTotal, int playerAces, int upValue, boolean canDouble){
        double stand = standEV(playerTotal, upValue);
        double hit   = hitEV(playerTotal, playerAces, upValue);

        System.out.println("stand EV:  " + stand);
        System.out.println("hit EV:    " + hit);

        if (canDouble){
            double dbl = doubleEV(playerTotal, playerAces, upValue);
            System.out.println("double EV: " + dbl);

            if (dbl >= stand && dbl >= hit){
                return Move.DOUBLE;
            } else if (hit >= stand){
                return Move.HIT;
            } else {
                return Move.STAND;
            }
        } else {
            if (hit >= stand){
                return Move.HIT;
            } else {
                return Move.STAND;
            }
        }
    }

    public int[] handState(Hand hand){
        int total = 0;
        int aces = 0;
        for (int i = 0; i < hand.size(); i++){
            int v = hand.getCard(i).getRank().getValue();
            total += v;
            if (v == 11) aces++;
            while (total > 21 && aces > 0){
                total -= 10;
                aces--;
            }
        }
        return new int[]{total, aces};
    }




    public int getDeckCount(int value){
        return deckCount[value];
    }

    public int getCardCount(){
        return cardCount;
    }
}
