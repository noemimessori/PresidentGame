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

public class ProtectivePlayer extends AbstractPlayer {
    private final IStateHeuristic heuristic;

    public ProtectivePlayer(PlayerParameters params, IStateHeuristic heuristic, String name, Random random) {
        super(params, name);
        this.heuristic = heuristic;
        this.rnd = random != null ? random : new Random(System.currentTimeMillis());
    }

    public ProtectivePlayer() {
        this(null, null, "ProtectivePlayer", new Random());
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gs, List<AbstractAction> actions) {
        PresidentGameState game = (PresidentGameState) gs;
        int playerId = gs.getCurrentPlayer();
        int maxValue = ((PresidentParameters) game.getGameParameters()).maxCardValue;

        // soglia di "sicurezza" (da tarare)
        final double safeThreshold = 0.20;

        List<PlayCard> playCard = new ArrayList<>();
        List<PlayCards> playCards = new ArrayList<>();
        for (AbstractAction action : actions) {
            if (action instanceof PlayCard)
                playCard.add((PlayCard) action);
            else if (action instanceof PlayCards)
                playCards.add((PlayCards) action);
        }

        if (playCard.isEmpty() && playCards.isEmpty())
            return actions.get(0);   // pass

        AbstractAction bestSafeAction = null;
        int bestSafeValue = Integer.MAX_VALUE;

        AbstractAction bestFallbackByPwin = null;
        double bestPwin = Double.NEGATIVE_INFINITY;

        // helper: evaluate a value using cardProbabilities
        for (PlayCard action : playCard) {
            int value = action.getNumber();
            double pWin = 1.0;
            for (int opp = 0; opp < game.getNPlayers(); opp++) {
                if (opp == playerId) continue;
                Map<Integer, Double> oppMap = game.getCardProbabilities().get(opp);
                double sumHigher = 0.0;
                if (oppMap != null) {
                    for (int h = value + 1; h <= maxValue; h++) {
                        sumHigher += oppMap.getOrDefault(h, 0.0);
                    }
                }
                pWin *= (1.0 - sumHigher);
            }

            if (pWin >= safeThreshold) {
                if (value < bestSafeValue) {
                    bestSafeValue = value;
                    bestSafeAction = action;
                }
            }
            if (pWin > bestPwin) {
                bestPwin = pWin;
                bestFallbackByPwin = action;
            }
        }

        for (PlayCards action : playCards) {
            int value = action.getNumber();
            double pWin = 1.0;
            for (int opp = 0; opp < game.getNPlayers(); opp++) {
                if (opp == playerId) continue;
                Map<Integer, Double> oppMap = game.getCardProbabilities().get(opp);
                double sumHigher = 0.0;
                if (oppMap != null) {
                    for (int h = value + 1; h <= maxValue; h++) {
                        sumHigher += oppMap.getOrDefault(h, 0.0);
                    }
                }
                pWin *= (1.0 - sumHigher);
            }

            if (pWin >= safeThreshold) {
                if (value < bestSafeValue) {
                    bestSafeValue = value;
                    bestSafeAction = action;
                }
            }
            if (pWin > bestPwin) {
                bestPwin = pWin;
                bestFallbackByPwin = action;
            }
        }

        if (bestSafeAction != null) return bestSafeAction;
        if (bestFallbackByPwin != null) return bestFallbackByPwin;

        // fallback: original behaviour (lowest playable)
        double minValue = Double.POSITIVE_INFINITY;
        AbstractAction bestAction = null;
        for (PlayCard action : playCard) {
            int number = action.getNumber();
            if (number < minValue) {
                minValue = number;
                bestAction = action;
            }
        }
        for (PlayCards action : playCards) {
            int number = action.getNumber();
            if (number < minValue) {
                minValue = number;
                bestAction = action;
            }
        }
        return bestAction != null ? bestAction : actions.get(0);
    }

    @Override
    public players.president.ProtectivePlayer copy() {
        return this;
    }
}
