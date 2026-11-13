package com.seamus.blackjack.ui;

import com.seamus.blackjack.model.Hand;
import com.seamus.blackjack.model.Move;

import java.util.Scanner;

public class ConsoleUI {
    private Scanner scanner;

    public ConsoleUI(){
        scanner = new Scanner(System.in);
    }

    public void showPlayerHand(Hand hand){
        System.out.println("Player's Hand : ");
        System.out.println(hand);
        System.out.println("Total : " + hand.getTotal());
        System.out.println();
    }

    public void showDealerHand(Hand hand){
        System.out.println("Dealer's Hand : ");
        System.out.println(hand);
        System.out.println(hand.getTotal());
    }

    public void showDealerCard(Hand hand){
        System.out.println("Dealer's Hand : ");
        System.out.println(hand.getCard(0));
    }

    public Move askMove(boolean canDouble) {
        String prompt = canDouble
                ? "1 to stand, 2 to hit, 3 to double : "
                : "1 to stand, 2 to hit : ";
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": return Move.STAND;
                case "2": return Move.HIT;
                case "3":
                    if (canDouble) return Move.DOUBLE;
                    break;                       // if double is not allowed reprompt
                default:  break;                 //  reprompt
            }
            System.out.println("Not a valid option.");
        }
    }

    public void printBust(){
        System.out.println("Bust!");
    }

    public void printDealerBust(){
        System.out.println("Dealer Bust!");

    }

    public void printBlackjack(){
        System.out.println("Blackjack!");
    }

    public void printDealerBlackjack(){
        System.out.println("Dealer has Blackjack");
    }

    public void printWin(){
        System.out.println("You Win!");
    }

    public void printLoss(){
        System.out.println("You Loose");
    }

    public void printDraw(){
        System.out.println("Draw");
    }

    public void printError(){
        System.out.println("System Error");
    }

}
