package com.seamus.blackjack;


import com.seamus.blackjack.ai.MonteCarloAdvisor;
import com.seamus.blackjack.game.Game;
import com.seamus.blackjack.model.*;

public class Main {
    static void main() {
        Game game = new Game();
        game.play();
    }
}
