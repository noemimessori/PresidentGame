package games.president.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.actions.DrawCard;
import core.components.Card;
import core.components.Deck;
import games.president.PresidentGameState;
import games.president.cards.PresidentCard;

import java.util.Objects;

// * the action moves a card from a deck (player's cards) to another one (discard pile)
// * there's no choice logic here, only the consequences
public class PlayCard extends DrawCard {

    public final int value; // the value of the card I'm playing

    public PlayCard (int playerHandCards, int discardPile, int cardIndex, int value) {
        this.deckFrom = playerHandCards;
        this.deckTo = discardPile;
        this.fromIndex = cardIndex;
        this.toIndex = 0;       // only the first card on the discard pile matters
        this.value = value;
    }

    public int getNumber() {
        return this.value;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        PresidentGameState gameState = (PresidentGameState) gs;
        super.execute(gameState);       // card moved - basic move

        // President logic consequences
        PresidentCard playedCard = (PresidentCard) gameState.getComponentById(cardId);
        gameState.updateCurrentCard(playedCard);
        gameState.clearPassingPlayers();   // reset passing players status

        if(gameState.isFirstCard()) {
            gameState.setCurrentCardUnset(false);
            gameState.updateCurrentRequiredCard(1);
        }

        if (playedCard.value == 10) {    // it closes the discardPile
            gameState.clearDiscardPile();
            gameState.updateCurrentCard(new PresidentCard(0));
            gameState.setCurrentCardUnset(true);
            gameState.updateCurrentRequiredCard(0);
            gameState.setPlayAgain(true);
        }
        System.out.println("Played card: "+ value);
        return true;
    }

    @Override
    public Card getCard(AbstractGameState gs) {
        if (!executed) {
            Deck<PresidentCard> deck = (Deck<PresidentCard>) gs.getComponentById(deckFrom);
            if (fromIndex == deck.getSize())
                return deck.get(fromIndex - 1);
            return deck.get(fromIndex);
        }
        return (PresidentCard) gs.getComponentById(cardId);
    }

    @Override
    public AbstractAction copy() {
        return new games.president.actions.PlayCard(deckFrom, deckTo, fromIndex,value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayCards)) return false;
        if (!super.equals(obj)) return false;
        PlayCard playCard = (PlayCard) obj;
        return Objects.equals(value, playCard.value);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "one " + value;
    }

    @Override
    public void printToConsole(AbstractGameState gameState) {
        super.printToConsole(gameState);
    }
}
