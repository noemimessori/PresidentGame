package games.president;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Deck;
import core.interfaces.IPrintable;
import games.GameType;
import games.president.cards.PresidentCard;
import java.util.*;

import static core.CoreConstants.VisibilityMode.HIDDEN_TO_ALL;


public class PresidentGameState extends AbstractGameState implements IPrintable {

    // playerID, cardValue (probability that playerID has cardValue)
    private Map<Integer, Map<Integer, Double>> cardProbabilities = new HashMap<>();

    public Map<Integer, Map<Integer, Double>> getCardProbabilities() {
        return cardProbabilities;
    }

    // global remaining copies per card value (value -> remaining copies)
    private Map<Integer, Integer> remainingCounts = new HashMap<>();

    public Map<Integer, Integer> getRemainingCounts() {
        return remainingCounts;
    }

    List<Deck<PresidentCard>> playerHandCards;
    Deck<PresidentCard> discardPile;
    PresidentCard currentCard;
    int currentRequiredCards; //number of cards to play

    boolean isFirstCard;   // current card unset
    boolean playAgain;
    int passingPlayers;
    int[] orderOfPlayerDone;

    public PresidentGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
    }

    @Override
    protected GameType _getGameType() {
        return GameType.President;
    }

    @Override
    protected List<Component> _getAllComponents() {
        return new ArrayList<Component>() {{
            addAll(playerHandCards);
            add(discardPile);
            add(currentCard);
        }};
    }

    @Override
    protected AbstractGameState _copy(int playerId) {
        PresidentGameState copy = new PresidentGameState(gameParameters.copy(), getNPlayers());
        copy.playerHandCards = new ArrayList<>();
        copy.isFirstCard = isFirstCard;
        copy.passingPlayers = passingPlayers;
        copy.orderOfPlayerDone = orderOfPlayerDone;
        copy.discardPile = discardPile.copy();
        copy.currentRequiredCards = currentRequiredCards;
        copy.currentCard = (PresidentCard) currentCard.copy();
        Deck<PresidentCard> hiddenPile = new Deck<>("Starting_deck", HIDDEN_TO_ALL);

        for (Deck<PresidentCard> deck : playerHandCards) {
            copy.playerHandCards.add(deck.copy());
        }

        // copy card probabilities (deep copy) to avoid NPEs when agents read them
        if (this.cardProbabilities != null) {
            copy.cardProbabilities = new HashMap<>();
            for (Map.Entry<Integer, Map<Integer, Double>> e : this.cardProbabilities.entrySet()) {
                copy.cardProbabilities.put(e.getKey(), new HashMap<>(e.getValue()));
            }
        } else {
            copy.cardProbabilities = new HashMap<>();
        }

        if (getCoreGameParameters().partialObservable && playerId != -1) { // to hide cards of other players
            // collect hidden cards
            List<PresidentCard> pool = new ArrayList<>();
            for (int i = 0; i < getNPlayers(); i++) {
                if (i != playerId) {
                    pool.addAll(copy.playerHandCards.get(i).getComponents());
                }
            }

            // clear other players' hands
            for (int i = 0; i < getNPlayers(); i++) {
                if (i == playerId) continue;
                Deck<PresidentCard> deck = copy.playerHandCards.get(i);
                int nCards = deck.getSize();
                deck.clear();

                Map<Integer, Double> oppMap = copy.cardProbabilities.get(i); // probabilities for opponent 

                for (int c = 0; c < nCards; c++) {
                    if (pool.isEmpty()) break;

                    Map<Integer, Integer> valueCounts = new HashMap<>();
                    for (PresidentCard pc : pool) valueCounts.merge(pc.value, 1, Integer::sum);

                    List<Integer> values = new ArrayList<>(valueCounts.keySet());
                    double[] weights = new double[values.size()];
                    double totalW = 0.0;
                    for (int vi = 0; vi < values.size(); vi++) {
                        int v = values.get(vi);
                        double marginal = 1.0;
                        if (oppMap != null) marginal = Math.max(oppMap.getOrDefault(v, 0.0), 1e-6);
                        // more weight to more probable cards
                        double w = marginal * valueCounts.get(v);
                        weights[vi] = w;
                        totalW += w;
                    }

                    // fallback
                    if (totalW <= 0.0) {
                        for (int vi = 0; vi < weights.length; vi++) {
                            weights[vi] = 1.0;
                        }
                        totalW = weights.length;
                    }

                    double r = redeterminisationRnd.nextDouble() * totalW;
                    double acc = 0.0;
                    int chosenValueIdx = 0;
                    for (int vi = 0; vi < weights.length; vi++) {
                        acc += weights[vi];
                        if (r <= acc) { chosenValueIdx = vi; break; }
                    }
                    int chosenValue = values.get(chosenValueIdx);

                    PresidentCard chosenCard = null;
                    for (int k = 0; k < pool.size(); k++) {
                        if (pool.get(k).value == chosenValue) {
                            chosenCard = pool.remove(k); // remove a card with chosenValue from pool
                            break;
                        }
                    }
                    if (chosenCard == null) { // fallback: remove first
                        chosenCard = pool.remove(0);
                    }
                    deck.add(chosenCard);
                }
            }

            // redistribute remaining cards (if any) to other players
            int idx = 0;
            while (!pool.isEmpty()) {
                int target = idx % getNPlayers();
                if (target == playerId) { idx++; continue; }
                copy.playerHandCards.get(target).add(pool.remove(0));
                idx++;
            }
        }
        return copy;
    }



    @Override
    protected double _getHeuristicScore(int playerId) {
        return new PresidentHeuristic().evaluateState(this, playerId);
    }

    @Override
    public double getGameScore(int playerId) {
        int doneOrder = orderOfPlayerDone[playerId];
        if (doneOrder == -1)    // not done yet   
            return 0;
        return getNPlayers() - doneOrder;
    }

    public int getNextPlayer() {
        int nextOwner = (turnOwner + 1) % nPlayers; // if last come back to first
        while (!isNotTerminalForPlayer(nextOwner)) {
            nextOwner = (nextOwner + 1) % nPlayers;
        }
        return nextOwner;
    }

    public void updateCurrentCard(PresidentCard card) {
        currentCard = card;
    }

    public void updateCurrentRequiredCard(int numberCards) {
        currentRequiredCards = numberCards;
    }

    @Override
    protected boolean _equals(Object o) {
        if(o instanceof PresidentGameState other) {
            return discardPile.equals(other.discardPile) &&
                    Arrays.equals(orderOfPlayerDone, other.orderOfPlayerDone) &&
                    playerHandCards.equals(other.playerHandCards) &&
                    isFirstCard == other.isFirstCard &&
                    passingPlayers == other.passingPlayers &&
                    currentCard == other.currentCard &&
                    currentRequiredCards == other.currentRequiredCards;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerHandCards, discardPile, currentCard, Arrays.hashCode(orderOfPlayerDone), isFirstCard, passingPlayers);
    }

    @Override
    public void printToConsole() {

        System.out.println("====================================================");
        System.out.println("                    GAME STATUS");
        System.out.println("Players Status: " + Arrays.toString(orderOfPlayerDone));
        System.out.println("Turn of player: " + getCurrentPlayer());
        Deck<PresidentCard> hand = playerHandCards.get(getCurrentPlayer());
        StringBuilder sb = new StringBuilder();
        sb.append("Cards of current player ");
        for (PresidentCard card : hand.getComponents()) {
            sb.append(card.toString()).append(" ");
        }
        System.out.println(sb);

        if (currentCard.value != 0) {
            System.out.println("Card on discard pile: " +currentCard.value);
            System.out.println("Number of required cards: "+ currentRequiredCards);
        } else  {
            System.out.println("Play what you want!");
        }
    }

    public List<Deck<PresidentCard>> getPlayerHandCards() {
        return playerHandCards;
    }
    public Deck<PresidentCard> getDiscardPile() {
        return discardPile;
    }
    public void clearDiscardPile() {
        this.discardPile.clear();
    }
    public int getCurrentRequiredCards() {
        return currentRequiredCards;
    }
    public int getPassingPlayers() {
        return passingPlayers;
    }
    public void clearPassingPlayers() {
        this.passingPlayers = 0;
    }
    public void addPassingPlayer() {
        this.passingPlayers++;
    }
    public boolean isFirstCard() {
        return isFirstCard;
    }
    public void setCurrentCardUnset(boolean isFirstCard) {
        this.isFirstCard = isFirstCard;
    }
    public PresidentCard getCurrentCard() {
        return currentCard;
    }
    public boolean isPlayAgain() {
        return playAgain;
    }
    public void setPlayAgain(boolean playAgain) {
        this.playAgain = playAgain;
    }

    public int getMaxCardsPerPlayer() {
        PresidentParameters gameParam = (PresidentParameters) getGameParameters();
        int players = getNPlayers();
        int totCards = gameParam.nNumberCards;

        return switch (players) {
            case 2 -> totCards / 2;
            case 3 -> Math.round((float) totCards / 3);
            case 4 -> totCards / 4;
            default -> 0;
        };
    }

    public double getAverageCardValue(int playerId) {
        List<PresidentCard> hand = playerHandCards.get(playerId).getComponents();
        if (hand.isEmpty()) return 0.0;

        double sum = 0.0;
        for (PresidentCard c : hand) {
            sum += c.value;
        }
        return sum / hand.size();
    }

    public double getAverageOpponentsHandSize(int playerId) {
        int totalCards = 0;
        int nOpponents = 0;

        for (int i = 0; i < playerHandCards.size(); i++) {
            if (i != playerId) {
                totalCards += playerHandCards.get(i).getSize();
                nOpponents++;
            }
        }
        return (double) totalCards / nOpponents;
    }

    public double getPlayableCardsCount(int playerId) {
        List<PresidentCard> hand = playerHandCards.get(playerId).getComponents();
        int playableCards = 0;
        for (PresidentCard card : hand) {
            if (card.isPlayable(this))
                playableCards++;
        }
        return playableCards;
    }

    public double getComboCount(int playerId) {
        int comboCount = 0;
        List<PresidentCard> maybePlayableCards = new ArrayList<>();

        for (PresidentCard card : playerHandCards.get(playerId).getComponents()) {
            if (card.isPlayable(this)) {
                maybePlayableCards.add(card);       // all cards greater that the current one
            }
        }

        Map<Integer, List<Integer>> grouped = new HashMap<>();      // all cards grouped by value
        for (PresidentCard card : maybePlayableCards) {
            grouped.computeIfAbsent(card.value, ids ->  new ArrayList<>()).add(card.getComponentID());
        }

        for (List<Integer> ids : grouped.values()) {
            if (ids.size() >= 2) {
                comboCount++;
            }
        }
        return comboCount;
    }


    public void initCardProbabilities(int nPlayers, int maxCardValue) {
        cardProbabilities.clear();
        remainingCounts.clear();

        // copiesPerValue
        int copies = 2;
        if (getGameParameters() instanceof PresidentParameters) {
            copies = ((PresidentParameters) getGameParameters()).copiesPerValue;
        }

        // at first copiesPerValue = remainingCounts
        for (int v = 1; v <= maxCardValue; v++) {
            remainingCounts.put(v, copies);
        }

        if (playerHandCards != null) {
            for (Deck<PresidentCard> d : playerHandCards) {
                if (d == null) continue;
                for (PresidentCard c : d.getComponents()) {
                    remainingCounts.merge(c.value, -1, Integer::sum);
                }
            }
        }
        if (discardPile != null) {
            for (PresidentCard c : discardPile.getComponents()) {
                remainingCounts.merge(c.value, -1, Integer::sum);
            }
        }

        for (int v = 1; v <= maxCardValue; v++) {
            remainingCounts.put(v, Math.max(0, remainingCounts.getOrDefault(v, 0)));
        }

        for (int p = 0; p < nPlayers; p++) {
            Map<Integer, Double> probs = new HashMap<>();
            double sum = 0.0;
            for (int v = 1; v <= maxCardValue; v++) {
                double w = Math.max(1e-6, remainingCounts.getOrDefault(v, 0));
                probs.put(v, w);
                sum += w;
            }
            for (int v = 1; v <= maxCardValue; v++) probs.put(v, probs.get(v) / sum);
            cardProbabilities.put(p, probs);
        }
    }

    public void updateCardProbabilities(int playerId, Integer playedValue, boolean passed, int currentValue, int maxCardValue) {
        if (cardProbabilities == null) return;

        int beforeCount = -1;
        if (!passed && playedValue != null) {
            beforeCount = remainingCounts.getOrDefault(playedValue, 0);
            remainingCounts.put(playedValue, Math.max(0, beforeCount - 1));
        }

        Map<Integer, Double> probsActing = cardProbabilities.get(playerId);
        if (probsActing == null) return;

        if (!passed && playedValue != null) {
            int afterCount = remainingCounts.getOrDefault(playedValue, 0);
            double factor = beforeCount > 0 ? ((double) afterCount / (double) beforeCount) : 0.0;
            double old = probsActing.getOrDefault(playedValue, 0.0);
            probsActing.put(playedValue, old * factor);
        } else if (passed) {
            // player passed: no cards > currentValue
            for (int v = currentValue + 1; v <= maxCardValue; v++) {
                probsActing.put(v, 0.0);
            }
            normalize(probsActing);
        }

        for (int p = 0; p < getNPlayers(); p++) {
            Map<Integer, Double> probs = cardProbabilities.get(p);
            if (probs == null) continue;
            double sum = 0.0;
            for (int v = 1; v <= maxCardValue; v++) {
                double rem = Math.max(1e-6, remainingCounts.getOrDefault(v, 0));
                double prior = probs.getOrDefault(v, 0.0);
                double newVal = prior * rem;
                probs.put(v, newVal);
                sum += newVal;
            }
            
            if (sum <= 0.0) {
                double s2 = 0.0;
                for (int v = 1; v <= maxCardValue; v++) {
                    double w = Math.max(1e-6, remainingCounts.getOrDefault(v, 0));
                    probs.put(v, w);
                    s2 += w;
                }
                for (int v = 1; v <= maxCardValue; v++) probs.put(v, probs.get(v) / s2);
            } else {
                for (int v = 1; v <= maxCardValue; v++) probs.put(v, probs.get(v) / sum);
            }
        }
    }

    private void normalize(Map<Integer, Double> map) {
        double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum == 0) return;
        for (Map.Entry<Integer, Double> e : map.entrySet()) {
            e.setValue(e.getValue() / sum);
        }
    }

}
