package games.president;

import core.AbstractGameState;
import core.CoreConstants;
import core.interfaces.IStateHeuristic;
import evaluation.optimisation.TunableParameters;

// the method *evaluateState* is called when I have to choose an action
// for each available action I simulate the next state with *evaluateState* and I get a value of goodness
// then I choose the action that cause the highest value of goodness

public class PresidentHeuristic extends TunableParameters implements IStateHeuristic {

    // principal factors
    double FACTOR_FEW_CARDS = 1.0;                     // player remain with fewer cards = good
    double FACTOR_HIGH_CARDS = 0.5;                    // player remain with higher cards
    double FACTOR_COMBO = 0.7;                         // player plays more cards AND complicates the turn of the next players

    // advanced factors
    double FACTOR_RELATIVE_ADVANTAGE = 0.3;            // player is in advantage comparing to the others
    double FACTOR_FLEXIBILITY = 0.3;                   // player avoid to block himself
    double FACTOR_NEXT_PLAYER = 0.2;                   // player complicates the turn of the next player

    public PresidentHeuristic() {
        addTunableParameter("FACTOR_FEW_CARDS", FACTOR_FEW_CARDS);
        addTunableParameter("FACTOR_HIGH_CARDS", FACTOR_HIGH_CARDS);
        addTunableParameter("FACTOR_NEXT_PLAYER", FACTOR_NEXT_PLAYER);
        addTunableParameter("FACTOR_RELATIVE_ADVANTAGE", FACTOR_RELATIVE_ADVANTAGE);
        addTunableParameter("FACTOR_FLEXIBILITY", FACTOR_FLEXIBILITY);
        addTunableParameter("FACTOR_COMBOS", FACTOR_COMBO);
    }

    @Override
    protected PresidentHeuristic _copy() {
        PresidentHeuristic ret = new PresidentHeuristic();
        ret.FACTOR_FEW_CARDS = FACTOR_FEW_CARDS;
        ret.FACTOR_HIGH_CARDS = FACTOR_HIGH_CARDS;
        ret.FACTOR_NEXT_PLAYER = FACTOR_NEXT_PLAYER;
        ret.FACTOR_RELATIVE_ADVANTAGE = FACTOR_RELATIVE_ADVANTAGE;
        ret.FACTOR_FLEXIBILITY = FACTOR_FLEXIBILITY;
        ret.FACTOR_COMBO = FACTOR_COMBO;
        return ret;
    }

    @Override
    protected boolean _equals(Object o) {
        if (!(o instanceof PresidentHeuristic)) return false;
        PresidentHeuristic other = (PresidentHeuristic) o;
        return other.FACTOR_FEW_CARDS == FACTOR_FEW_CARDS &&
                other.FACTOR_HIGH_CARDS == FACTOR_HIGH_CARDS &&
                other.FACTOR_NEXT_PLAYER == FACTOR_NEXT_PLAYER &&
                other.FACTOR_RELATIVE_ADVANTAGE == FACTOR_RELATIVE_ADVANTAGE &&
                other.FACTOR_FLEXIBILITY == FACTOR_FLEXIBILITY &&
                other.FACTOR_COMBO == FACTOR_COMBO;
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        PresidentGameState gameState = (PresidentGameState) gs;
        PresidentParameters params = (PresidentParameters) gameState.getGameParameters();
        CoreConstants.GameResult playerResult = gs.getPlayerResults()[playerId];

        if (playerResult == CoreConstants.GameResult.LOSE_GAME)
            return -1;
        if (playerResult == CoreConstants.GameResult.WIN_GAME)
            return 1;

        int maxCards = gameState.getMaxCardsPerPlayer();
        double score = 0.0;

        // 1. Few cards left
        score += FACTOR_FEW_CARDS * (maxCards - gameState.getPlayerHandCards().get(playerId).getSize()) / (double) maxCards;

        // 2. High cards left
        score += FACTOR_HIGH_CARDS * gameState.getAverageCardValue(playerId) / params.maxCardValue;

        // 3. Avoid next player from winning
        int nextPlayer = gameState.getNextPlayer();
        score += FACTOR_NEXT_PLAYER * (maxCards - gameState.getPlayerHandCards().get(nextPlayer).getSize()) / (double) maxCards;

        // 4. Relative advantage
        double avgOpponents = gameState.getAverageOpponentsHandSize(playerId);
        score += FACTOR_RELATIVE_ADVANTAGE * (avgOpponents - gameState.getPlayerHandCards().get(nextPlayer).getSize()) / (double) maxCards;

        // 5. Flexibility
        score += FACTOR_FLEXIBILITY * gameState.getPlayableCardsCount(playerId) / (double) gameState.getPlayerHandCards().get(nextPlayer).getSize();

        // 6. Combo possibility
        score += FACTOR_COMBO * gameState.getComboCount(playerId) / (double) gameState.getPlayerHandCards().get(nextPlayer).getSize();

        return score;
    }

    @Override
    public PresidentHeuristic instantiate() {
        return _copy();
    }

    @Override
    public void _reset() {
        FACTOR_FEW_CARDS = (double) getParameterValue("FACTOR_FEW_CARDS");
        FACTOR_HIGH_CARDS = (double) getParameterValue("FACTOR_HIGH_CARDS");
        FACTOR_NEXT_PLAYER = (double) getParameterValue("FACTOR_NEXT_PLAYER");
        FACTOR_RELATIVE_ADVANTAGE = (double) getParameterValue("FACTOR_RELATIVE_ADVANTAGE");
        FACTOR_FLEXIBILITY = (double) getParameterValue("FACTOR_FLEXIBILITY");
        FACTOR_COMBO = (double) getParameterValue("FACTOR_COMBOS");
    }
}
