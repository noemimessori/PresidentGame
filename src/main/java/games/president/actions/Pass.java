package games.president.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import games.president.PresidentGameState;
import games.president.cards.PresidentCard;

// * the action does nothing, unless everybody is passing
// * there's no choice logic here, only the consequences
public class Pass extends AbstractAction {

    @Override
    public boolean execute(AbstractGameState gs) {
        PresidentGameState gameState = (PresidentGameState) gs;
        gameState.addPassingPlayer();

        if (gameState.getPassingPlayers() == gameState.getNPlayers()) {        // everybody passed
            // I'm the last one, so I'm clearing the turn
            gameState.clearPassingPlayers();
            gameState.clearDiscardPile();
            gameState.updateCurrentCard(new PresidentCard(0));
            gameState.updateCurrentRequiredCard(0);
            gameState.setCurrentCardUnset(true);
            // the next player will find everything clear to do play the first card
        }
        System.out.println("Player is passing");
        return true;
    }

    @Override
    public AbstractAction copy() {
        return new Pass();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pass;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return "Player passing.";
    }

    @Override
    public void printToConsole(AbstractGameState gameState) {
        System.out.println("Player "+ gameState.getCurrentPlayer() +" is passing.");
    }
}
