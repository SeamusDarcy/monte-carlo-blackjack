package com.seamus.blackjack.benchmark;

import com.seamus.blackjack.ai.MonteCarloAdvisor;
import com.seamus.blackjack.model.Card;
import com.seamus.blackjack.model.Hand;
import com.seamus.blackjack.model.Move;
import com.seamus.blackjack.model.Rank;
import com.seamus.blackjack.model.Suit;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvBenchmark {

    private static final int PLAYER_TOTAL = 12;
    private static final int PLAYER_ACES = 0;
    private static final int UP_VALUE = 10;
    private static final boolean CAN_DOUBLE = true;

    private static final int WARMUP = 5;
    private static final int MEASURE = 20;
    private static final double EPSILON = 0.02;

    private static long blackhole = 0;

    private static Hand playerHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.SEVEN));
        hand.addCard(new Card(Suit.SPADES, Rank.FIVE));
        return hand;
    }

    private static Card dealerUpCard() {
        return new Card(Suit.CLUBS, Rank.KING);
    }

    private static MonteCarloAdvisor serialAdvisor() throws Exception {
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();
        Field poolField = MonteCarloAdvisor.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ((ExecutorService) poolField.get(advisor)).shutdownNow();
        poolField.set(advisor, Executors.newFixedThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }));
        return advisor;
    }

    private static PrintStream mute() {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
    }

    private static long medianMs(MonteCarloAdvisor advisor) {
        PrintStream original = System.out;
        long[] samples = new long[MEASURE];
        try {
            System.setOut(mute());
            for (int i = 0; i < WARMUP; i++) {
                advisor.buildPool(playerHand(), dealerUpCard());
                blackhole += advisor.advise(PLAYER_TOTAL, PLAYER_ACES, UP_VALUE, CAN_DOUBLE).ordinal();
            }
            for (int i = 0; i < MEASURE; i++) {
                advisor.buildPool(playerHand(), dealerUpCard());
                long start = System.nanoTime();
                Move move = advisor.advise(PLAYER_TOTAL, PLAYER_ACES, UP_VALUE, CAN_DOUBLE);
                long end = System.nanoTime();
                blackhole += move.ordinal();
                samples[i] = (end - start) / 1_000_000;
            }
        } finally {
            System.setOut(original);
        }
        Arrays.sort(samples);
        return samples[samples.length / 2];
    }

    private static Move quietAdvise(MonteCarloAdvisor advisor) {
        PrintStream original = System.out;
        try {
            System.setOut(mute());
            advisor.buildPool(playerHand(), dealerUpCard());
            return advisor.advise(PLAYER_TOTAL, PLAYER_ACES, UP_VALUE, CAN_DOUBLE);
        } finally {
            System.setOut(original);
        }
    }

    private static double standEv(MonteCarloAdvisor advisor) {
        advisor.buildPool(playerHand(), dealerUpCard());
        return advisor.standEV(PLAYER_TOTAL, UP_VALUE);
    }

    public static void main(String[] args) throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();

        MonteCarloAdvisor serial = serialAdvisor();
        MonteCarloAdvisor parallel = new MonteCarloAdvisor();

        long serialMs = medianMs(serial);
        long parallelMs = medianMs(parallel);

        double serialEv = standEv(serial);
        double parallelEv = standEv(parallel);
        double evDiff = Math.abs(serialEv - parallelEv);
        Move serialMove = quietAdvise(serial);
        Move parallelMove = quietAdvise(parallel);

        System.out.println("=== EV Benchmark: advise(12, 0, 10, true) ===");
        System.out.println("cores                = " + cores);
        System.out.println("serial median ms     = " + serialMs);
        System.out.println("parallel median ms   = " + parallelMs);
        if (parallelMs > 0) {
            System.out.printf("speedup              = %.2fx%n", (double) serialMs / parallelMs);
        } else {
            System.out.println("speedup              = n/a (parallel too fast to measure in ms)");
        }
        System.out.println();
        System.out.println("--- equivalence check (epsilon " + EPSILON + ") ---");
        System.out.printf("serial   standEV     = %.6f  advise=%s%n", serialEv, serialMove);
        System.out.printf("parallel standEV     = %.6f  advise=%s%n", parallelEv, parallelMove);
        System.out.printf("|EV diff|            = %.6f%n", evDiff);
        if (evDiff > EPSILON) {
            System.out.println("*** WARNING: EV mismatch beyond tolerance - one path may be buggy, speedup is meaningless ***");
        } else if (serialMove != parallelMove) {
            System.out.println("*** WARNING: paths disagree on the chosen move ***");
        } else {
            System.out.println("OK: paths agree within tolerance");
        }

        System.out.println("(blackhole=" + blackhole + ")");
    }
}
