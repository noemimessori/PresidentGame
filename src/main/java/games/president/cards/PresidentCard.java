package games.president.cards;

import core.components.Card;
import games.president.PresidentGameState;

public class PresidentCard extends Card {

    public final int value; //Card's value

    public PresidentCard(int value) {
        this.value = value;
    }

    public PresidentCard(int value, int id) {
        super(String.valueOf(value), id);
        this.value = value;
    }

    public boolean beats(PresidentCard versusCard) {
        return this.value > versusCard.value;
    }

    public boolean isPlayable(PresidentGameState gameState) {
        PresidentCard currentCard = gameState.getCurrentCard();
        if(gameState.isFirstCard())
            return true;
        return beats(currentCard);
    }

    @Override
    public Card copy() {
        return new PresidentCard(this.value, this.componentID);
    }

    @Override
    public String toString() {
        return "{" + this.value +"}";
    }
}
