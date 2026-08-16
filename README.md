# Monte Carlo Blackjack

A terminal blackjack game written in Java where the code decides what move to make, not a person. Instead of asking the player to hit or stand, it runs a Monte Carlo simulation for every possible move, works out the expected value of each one, and plays whichever has the highest return.

## How it works

At each decision the advisor looks at the player's hand and the dealer's face up card, then works out the expected value (EV) of every move that is allowed - stand, hit, and double - and plays whichever comes back highest.

Each move's EV is worked out slightly differently:

- **Stand** - the dealer's hidden card and any cards they draw next are unknown, so the dealer's hand is simulated thousands of times to see how often they land on each total or bust. The player's fixed total is then scored against that distribution.
- **Hit and double** - there are only ten possible cards that can come out (2 to 11), and the chance of each one is known from the cards still left in the deck, so instead of random simulation every card is checked once and weighted by how likely it is. Double draws one card and then has to stop. Hit can keep going, so it takes the better of standing or hitting again from the new total (this part calls itself).

Cards that are already visible - the player's two cards and the dealer's up card - are removed from the deck before any of this, so the probabilities reflect what is actually left rather than a full deck.

Payouts are +1 for a win, -1 for a loss, 0 for a push, and doubled for a double.

## Running it

Requires Java 21 and Maven.

Compile it with:

```
mvn compile
```

then run it with:

```
java -cp target/classes com.seamus.blackjack.Main
```
## Example

```
Player's Hand :
QUEEN of DIAMONDS
SEVEN of HEARTS
Total : 17
Dealer's Hand :
FOUR of HEARTS
stand EV:  -0.061985
hit EV:    -0.533768
double EV: -1.067154
Advisor chose: STAND

Dealer's Hand :
FOUR of HEARTS
EIGHT of DIAMONDS
FOUR of SPADES
TEN of CLUBS
26
Dealer Bust!
You Win!
You win 1
```

Here the player has 17 against a dealer 4. Standing is close to break even, hitting almost always busts a 17, and doubling is worse again, so the advisor stands. The dealer then draws out to 26 and busts.

## Tests

There is a JUnit 5 test suite covering the parts of the game where the maths has to be right. Run it with:

```
mvn test
```

The tests are:

- **RankTest** - checks the card values: the face cards (jack, queen, king) are all worth ten, and an ace is worth eleven before any soft/hard adjustment.
- **HandTest** - checks the hand totalling, including the ace logic: a plain total with no aces, an ace staying at eleven when it fits, two aces coming to twelve, and aces dropping to one to avoid a bust.
- **MonteCarloAdvisorTest** - checks the advisor's deck bookkeeping: building the card pool leaves forty-nine cards and removes the two player cards and the dealer's up card from the counts.

## Project structure

- **model** - the cards, deck, hand, ranks and suits
- **game** - the game loop, dealing, the player and dealer turns, and settling the result
- **ai** - the Monte Carlo advisor that works out the EVs and picks the move
- **ui** - all the console output

## Not included

Splitting is not implemented yet. The advisor plays every other part of the game on its own and picks the mathematically best move, but a pair can't be split at the moment that is the main thing left to add.
