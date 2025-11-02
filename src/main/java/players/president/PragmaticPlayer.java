package players.president;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.president.PresidentGameState;
import games.president.actions.PlayCard;
import games.president.actions.PlayCards;
import players.PlayerParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PragmaticPlayer extends AbstractPlayer {

    private final IStateHeuristic heuristic;
    private final int rolloutDepth = 3;      // future moves to simulate
    private final int rolloutCount = 5;      // simulations per action

    public PragmaticPlayer(PlayerParameters params, IStateHeuristic heuristic, String name, Random random) {
        super(params, name);
        this.heuristic = heuristic;
        this.rnd = random != null ? random : new Random(System.currentTimeMillis());
    }

    public PragmaticPlayer(IStateHeuristic heuristic) {
        this(null, heuristic, "PragmaticPlayer", new Random());
    }

    public PragmaticPlayer(IStateHeuristic heuristic, Random random) {
        this(null, heuristic, "PragmaticPlayer", random);
    }

    public PragmaticPlayer() {
        this(null, null, "PragmaticPlayer", new Random());
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gs, List<AbstractAction> actions) {
        double maxValue = Double.NEGATIVE_INFINITY;
        AbstractAction bestAction = null;

        int playerID = gs.getCurrentPlayer();
        PresidentGameState game = (PresidentGameState) gs;
        int currentRequiredCards = game.getCurrentRequiredCards();

        List<PlayCard> singleCardActions = new ArrayList<>();
        List<PlayCards> multiCardActions = new ArrayList<>();
        for (AbstractAction action : actions) {
            if (action instanceof PlayCard)
                singleCardActions.add((PlayCard) action);
            else
                multiCardActions.add((PlayCards) action);
        }

        if (singleCardActions.isEmpty() && multiCardActions.isEmpty())
            return actions.get(0);   // Pass

        // Candidate actions (respecting currentRequiredCards)
        List<AbstractAction> candidateActions = new ArrayList<>();
        if (currentRequiredCards != 2) candidateActions.addAll(singleCardActions);
        if (currentRequiredCards != 1) candidateActions.addAll(multiCardActions);

        
        for (AbstractAction action : candidateActions) {
            double totalValue = 0.0; 
            //  rolloutCount rollouts per each action, then take the average
            for (int i = 0; i < rolloutCount; i++) {
                // PresidentGameState._copy can perform redeterminisation
                AbstractGameState simulated = gs.copy(playerID);
                getForwardModel().next(simulated, action);

                // simulate rollout from the new state
                double rolloutScore = simulateRollout(simulated, playerID, rolloutDepth);
                totalValue += rolloutScore;
            }

            double avgValue = totalValue / rolloutCount;
            if (avgValue > maxValue) {
                maxValue = avgValue;
                bestAction = action;
            }
        }

        return bestAction;
    }

    private double simulateRollout(AbstractGameState gs, int playerID, int depth) {
        // terminal check
        if (depth == 0 || !gs.isNotTerminal()) {
            return (heuristic != null) ? heuristic.evaluateState(gs, playerID) : gs.getHeuristicScore(playerID);
        }

        List<AbstractAction> possibleActions = getForwardModel().computeAvailableActions(gs);

        if (possibleActions == null || possibleActions.isEmpty()) {
            return (heuristic != null) ? heuristic.evaluateState(gs, playerID) : gs.getHeuristicScore(playerID);
        }
        
        AbstractAction randomAction = possibleActions.get(gs.getRnd().nextInt(possibleActions.size()));
        getForwardModel().next(gs, randomAction);

        return simulateRollout(gs, playerID, depth - 1);
    }

    @Override
    public PragmaticPlayer copy() {
        return this;
    }
}
