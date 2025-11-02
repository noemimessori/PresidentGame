package players.president;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.president.PresidentGameState;
import games.president.PresidentParameters;
import games.president.actions.PlayCard;
import games.president.actions.PlayCards;
import players.PlayerParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AggressivePlayer extends AbstractPlayer {

    private final IStateHeuristic heuristic;

    public AggressivePlayer(PlayerParameters params, IStateHeuristic heuristic, String name, Random random) {
        super(params, name);
        this.heuristic = heuristic;
        this.rnd = random != null ? random : new Random(System.currentTimeMillis());
    }

    public AggressivePlayer(IStateHeuristic heuristic) {
        this(null, heuristic, "AggressivePlayer", new Random());
    }

    public AggressivePlayer(IStateHeuristic heuristic, Random random) {
        this(null, heuristic, "AggressivePlayer", random);
    }

    public AggressivePlayer(Random random) {
        this(null, null, "AggressivePlayer", random);
    }

    public AggressivePlayer() {
        this(null, null, "AggressivePlayer", new Random());
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gs, List<AbstractAction> actions) {
        PresidentGameState game = (PresidentGameState) gs;
        int playerId = gs.getCurrentPlayer();
        int nPlayers = game.getNPlayers();
        int maxValue = ((PresidentParameters) game.getGameParameters()).maxCardValue;

        double bestScore = Double.NEGATIVE_INFINITY;
        AbstractAction bestAction = null;

        // consider both single and double-card actions (PlayCard/PlayCards)
        for (AbstractAction a : actions) {
            int value;
            if (a instanceof PlayCard) {
                value = ((PlayCard) a).getNumber();
            } else if (a instanceof PlayCards) {
                value = ((PlayCards) a).getNumber();
            } else {
                continue;
            }

            // compute pWin: product over opponents
            double pWin = 1.0;
            for (int opp = 0; opp < nPlayers; opp++) {
                if (opp == playerId) continue;
                Map<Integer, Double> oppMap = game.getCardProbabilities().get(opp);
                double sumHigher = 0.0;
                if (oppMap != null) {
                    for (int higher = value + 1; higher <= maxValue; higher++) {
                        sumHigher += oppMap.getOrDefault(higher, 0.0);
                    }
                }
                pWin *= (1.0 - sumHigher);
            }

            if (pWin > bestScore) {
                bestScore = pWin;
                bestAction = a;
            }
        }

        return bestAction != null ? bestAction : actions.get(0);
    }

    @Override
    public players.president.AggressivePlayer copy() {
        return this;
    }

}
