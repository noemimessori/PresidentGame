package games.president.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.actions.DrawComponents;
import core.components.Deck;
import games.president.PresidentGameState;
import games.president.cards.PresidentCard;

import java.util.ArrayList;
import java.util.Objects;

// * the action moves two cards from a deck (player's cards) to another one (discard pile)
// * there's no choice logic here, only the consequences
public class PlayCards extends DrawComponents {

    public final int value; // the value of the card I'm playing

    public PlayCards(int deckFrom, int deckTo, ArrayList ids, int value) {
        super(deckFrom, deckTo, ids);
        this.value = value;
    }

    public int getNumber() {
        return this.value;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        PresidentGameState gameState = (PresidentGameState) gs;
        Deck<PresidentCard> from = (Deck<PresidentCard>) gameState.getComponentById(deckFrom);
        PresidentCard playedCard = from.getComponents().get((Integer) this.idxs.get(0));
        super.execute(gameState);       // card moved - basic move

        // President logic consequences
        gameState.updateCurrentCard(playedCard);
        gameState.clearPassingPlayers();   // reset passing players status

        if(gameState.isFirstCard()) {
            gameState.setCurrentCardUnset(false);
            gameState.updateCurrentRequiredCard(2);
        }

        if (playedCard.value == 10) {    // it closes the discardPile
            gameState.clearDiscardPile();
            gameState.updateCurrentCard(new PresidentCard(0));
            gameState.setCurrentCardUnset(true);
            gameState.updateCurrentRequiredCard(0);
            gameState.setPlayAgain(true);
        }
        System.out.println("Played cards: double "+ value);
        return true;
    }

    @Override
    public AbstractAction copy() {
        return new PlayCards(deckFrom, deckTo, idxs, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayCards)) return false;
        if (!super.equals(obj)) return false;
        PlayCards playCards = (PlayCards) obj;
        return Objects.equals(value, playCards.value);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getString(AbstractGameState gs) {
        return "double " + value;
    }

    @Override
    public void printToConsole(AbstractGameState gameState) {
        super.printToConsole(gameState);
    }
}
