package com.seamus.blackjack.game;

import com.seamus.blackjack.model.Card;
import com.seamus.blackjack.model.Deck;
import com.seamus.blackjack.model.Hand;
import com.seamus.blackjack.model.Move;
import com.seamus.blackjack.ui.ConsoleUI;
import com.seamus.blackjack.ai.MonteCarloAdvisor;
import com.seamus.blackjack.model.Move;

import java.util.Scanner;

public class Game {
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    private ConsoleUI ui;
    private int stake = 1;

    public Game(){
        deck = new Deck();
        deck.shuffle();
        playerHand = new Hand();
        dealerHand = new Hand();
        ui = new ConsoleUI();

    }

    public void dealOpeningHands(){
        for (int i = 0 ; i <2 ; i++){
            playerHand.addCard(deck.deal());
            dealerHand.addCard(deck.deal());
        }
    }

    public void playerTurn(){
        MonteCarloAdvisor advisor = new MonteCarloAdvisor();

        ui.showPlayerHand(playerHand);
        ui.showDealerCard(dealerHand);

        while (true){
            advisor.buildPool(playerHand, dealerHand.getCard(0));

            int[] state = advisor.handState(playerHand);
            int total = state[0];
            int aces  = state[1];
            int up    = dealerHand.getCard(0).getRank().getValue();
            boolean canDouble = playerHand.size() == 2;

            Move move = advisor.advise(total, aces, up, canDouble);
            System.out.println("Advisor chose: " + move);

            if (move == Move.STAND){
                break;
            }

            if (move == Move.DOUBLE){
                stake = 2;
            }

            playerHand.addCard(deck.deal());
            ui.showPlayerHand(playerHand);

            if (playerHand.getTotal() > 21){
                ui.printBust();
                break;
            }

            if (move == Move.DOUBLE){
                break;
            }
        }
    }

    public void dealerTurn(){
        ui.showDealerHand(dealerHand);
        while (dealerHand.getTotal() < 17){
            dealerHand.addCard(deck.deal());
            ui.showDealerHand(dealerHand);
            if (dealerHand.getTotal() > 21){
                ui.printDealerBust();
            }
        }
    }

    public int playerWin(){
        // 1 : win, 2 : loss, 3 : draw
        if (playerHand.getTotal()>21){
            return 2;
        }
        if (dealerHand.getTotal()>21){
            return 1;
        }
        if (playerHand.getTotal() == dealerHand.getTotal()){
            return 3;
        } else if (playerHand.getTotal() > dealerHand.getTotal()){
            return 1;
        } else return 2;

    }
    public void play() {
        dealOpeningHands();
        playerTurn();
        if (playerHand.getTotal() <= 21) {
            dealerTurn();
        }
        int result = playerWin();
        switch (result){
            case 1:
                ui.printWin();
                System.out.println("You win " + stake);
                break;
            case 2:
                ui.printLoss();
                System.out.println("You lose " + stake);
                break;
            case 3:
                ui.printDraw();
                break;
            default:
                ui.printError();
                break;

        }
    }
}


