package games.president;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Deck;
import games.president.actions.Pass;
import games.president.actions.PlayCard;
import games.president.actions.PlayCards;
import games.president.cards.PresidentCard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static core.CoreConstants.GameResult.GAME_ONGOING;
import static core.CoreConstants.VisibilityMode.VISIBLE_TO_ALL;
import static core.CoreConstants.VisibilityMode.VISIBLE_TO_OWNER;

public class PresidentForwardModel extends StandardForwardModel {

    @Override
    protected void _setup(AbstractGameState firstState) {
        PresidentGameState gameState = (PresidentGameState) firstState;

        gameState.playerHandCards = new ArrayList<>(firstState.getNPlayers());
        for (int i = 0; i < gameState.getNPlayers(); i++) {
            gameState.playerHandCards.add(new Deck<>("Player " + i + " deck", i, VISIBLE_TO_OWNER));
        }
        gameState.discardPile = new Deck<>("DiscardPile", VISIBLE_TO_ALL);

        createAndDealCards(gameState);
        PresidentParameters params = (PresidentParameters) gameState.getGameParameters();
        gameState.initCardProbabilities(gameState.getNPlayers(), params.maxCardValue);

        gameState.currentCard = new PresidentCard(0);
        gameState.currentRequiredCards = 0;
        gameState.isFirstCard = true;
        gameState.passingPlayers = 0;

        gameState.orderOfPlayerDone = new int[gameState.getNPlayers()];
        for (int playerId = 0; playerId < gameState.getNPlayers(); playerId++) {
            gameState.orderOfPlayerDone[playerId] = -1;
        }

        int nPlayers = gameState.getNPlayers();
        int firstPlayer = gameState.getRnd().nextInt(nPlayers);
        gameState.setFirstPlayer(firstPlayer);
        gameState.setGameStatus(GAME_ONGOING);



    }

    private void createAndDealCards(PresidentGameState game) {
        PresidentParameters gameParam = (PresidentParameters) game.getGameParameters();
        List<PresidentCard> tempPile = new ArrayList<>();

        for (int value = gameParam.minCardValue; value <= gameParam.maxCardValue; value++) {
            for (int copy = 0; copy < gameParam.copiesPerValue; copy++) {
                PresidentCard card = new PresidentCard(value);
                tempPile.add(card);
            }
        }

        Collections.shuffle(tempPile, game.getRnd());

        int currentPlayer = 0;
        for (PresidentCard card : tempPile) {
            game.playerHandCards.get(currentPlayer).add(card);
            currentPlayer = (currentPlayer + 1) % game.getNPlayers();   // round-robin
        }

        if (game.getGameStatus() != CoreConstants.GameResult.GAME_ONGOING) {
            System.out.println("START - All player hand cards: ");
            for (int i = 0; i < game.playerHandCards.size(); i++) {
                Deck<PresidentCard> hand = game.playerHandCards.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append("Player ").append(i).append(" (").append(hand.getSize()).append(" cards): ");
                for (PresidentCard card : hand.getComponents()) {
                    sb.append(card.toString()).append(" ");
                }
                System.out.println(sb);
            }
            System.out.println();
        }
    }

    @Override
    protected void _afterAction(AbstractGameState gameState, AbstractAction action) {

        PresidentGameState game = (PresidentGameState) gameState;
        PresidentParameters params = (PresidentParameters) game.getGameParameters();
        int maxCardValue = params.maxCardValue;
        int currentCardValue = (game.getCurrentCard() != null) ? game.getCurrentCard().value : 0;
        int actingPlayer = game.getCurrentPlayer(); 

        if (action instanceof PlayCard) {
            PlayCard play = (PlayCard) action;
            int val = play.getNumber();                 
            game.updateCardProbabilities(actingPlayer, val, false, currentCardValue, maxCardValue);

        } else if (action instanceof PlayCards) {
            PlayCards playMulti = (PlayCards) action;
            int val = playMulti.getNumber();
            game.updateCardProbabilities(actingPlayer, val, false, currentCardValue, maxCardValue);

        } else if (action instanceof Pass) {
            game.updateCardProbabilities(actingPlayer, null, true, currentCardValue, maxCardValue);
        }

        if (checkGameEnd((PresidentGameState) gameState))
            return;

        if (gameState.getGameStatus() == GAME_ONGOING) {
    
            if (game.isPlayAgain()) {
                game.setPlayAgain(false);

                for (int playerId = 0; playerId < game.getNPlayers(); playerId++) {
                    if (playerId == game.getCurrentPlayer() && game.getPlayerHandCards().get(playerId).getSize() == 0) {
                        endPlayerTurn(game, game.getNextPlayer());
                        break;
                    } else if (playerId == game.getCurrentPlayer() && game.getPlayerHandCards().get(playerId).getSize() != 0) {
                        endPlayerTurn(game, game.getCurrentPlayer());
                        break;
                    }
                }

            } else
                endPlayerTurn(game, game.getNextPlayer());
        }
    }


    private boolean checkGameEnd(PresidentGameState game) {
        int doneCount = 0;
        for (int i : game.orderOfPlayerDone) {
            if (i != -1)
                doneCount++;
        }

        for (int playerId = 0; playerId < game.getNPlayers(); playerId++) {
            if (game.getPlayerHandCards().get(playerId).getSize() == 0
                    && game.getPlayerResults()[playerId] == GAME_ONGOING) {
                doneCount++;
                game.orderOfPlayerDone[playerId] = doneCount;
                game.setPlayerResult(CoreConstants.GameResult.WIN_GAME, playerId); // the player is done
            }
        }

        // THE GAME ENDS WHEN THE SECOND LAST PLAYER IS DONE
        if (doneCount == game.getNPlayers()-1) {
            game.setGameStatus(CoreConstants.GameResult.GAME_END);
            // the player with the smallest value is the winner
            // the player with the value still at "-1" in the looser 

            int winner = -1;
            int looser = -1;
            int bestRank = Integer.MAX_VALUE;
            for (int i = 0; i < game.orderOfPlayerDone.length; i++) {
                int rank = game.orderOfPlayerDone[i];
                if (rank != -1 && rank < bestRank) {
                    bestRank = rank;
                    winner = i;
                }
                if (rank == -1) {
                    looser = i;
                }
            }
            game.setPlayerResult(CoreConstants.GameResult.WIN_GAME, winner);
            game.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, looser);
            // the others remain with "DRAW_GAME"
            printResults(game, winner, looser);
            return true;
        }
        return false;
    }

    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        PresidentGameState game = (PresidentGameState) gameState;
        game.printToConsole();
        ArrayList<AbstractAction> actions = new ArrayList<>();

        int player = game.getCurrentPlayer();
        Deck<PresidentCard> playerHand = game.getPlayerHandCards().get(player);
        int currentRequiredCards = game.getCurrentRequiredCards();
        System.out.println();
        System.out.println("Available actions: ");

        if (currentRequiredCards != 2)      // if 0 or 1 I check actions with one card
            actions.addAll(getActionsWithOneCard(game, playerHand));
        if (currentRequiredCards != 1)      // if 0 or 2 I check actions with two cards
            actions.addAll(getActionsWithTwoCards(game, playerHand));

        if (actions.isEmpty()) {
            actions.add(new Pass());
        }
        System.out.println("----------------------------------------------------");
        return actions;
    }

    private List<AbstractAction> getActionsWithOneCard(PresidentGameState game, Deck<PresidentCard> playerHand) {
        ArrayList<AbstractAction> actionsOneCard = new ArrayList<>();

        for (PresidentCard card : playerHand.getComponents()) {
            int cardIdx = playerHand.getComponents().indexOf(card);
            if (card.isPlayable(game)) {
                PlayCard action = new PlayCard(playerHand.getComponentID(), game.discardPile.getComponentID(), cardIdx, card.value);
                System.out.println("    - "+action.getString(game));
                actionsOneCard.add(action);
            }
        }
        return actionsOneCard;
    }

    private List<AbstractAction> getActionsWithTwoCards(PresidentGameState game, Deck<PresidentCard> playerHand) {
        ArrayList<AbstractAction> actionsTwoCard = new ArrayList<>();
        List<PresidentCard> maybePlayableCards = new ArrayList<>();

        for (PresidentCard card : playerHand.getComponents()) {
            if (card.isPlayable(game)) {
                maybePlayableCards.add(card);       // all cards greater that the current one
            }
        }

        Map<Integer, List<Integer>> grouped = new HashMap<>();      // all cards grouped by value
        for (PresidentCard card : maybePlayableCards) {
            grouped.computeIfAbsent(card.value, ids ->  new ArrayList<>()).add(card.getComponentID());
        }

        for (Map.Entry<Integer, List<Integer>> entry : grouped.entrySet()) {
            int key = entry.getKey();
            List<Integer> ids = entry.getValue();
            if (ids.size() >= 2) {
                ArrayList<Integer> cards = new ArrayList<>();
                cards.add(playerHand.getComponents().indexOf(game.getComponentById(ids.get(0))));
                cards.add(playerHand.getComponents().indexOf(game.getComponentById(ids.get(1))));

                // even if more, I'm interested just in two playable cards, I'll take the first two
                //ArrayList<Integer> cards = new ArrayList<>(List.of(ids.get(0), ids.get(1)));
                PlayCards action = new PlayCards(playerHand.getComponentID(), game.discardPile.getComponentID(), cards, key);
                System.out.println("    - "+action.getString(game));
                actionsTwoCard.add(action);
            }
        }
        return actionsTwoCard;
    }

    public void printResults(PresidentGameState game, int winner, int looser) {
        System.out.println("\n===============================");
        System.out.println("GAME OVER - FINAL RANKINGS");
        System.out.println(
                "Game finished at time: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        System.out.println("===============================");

        List<Integer> finishedPlayers = new ArrayList<>();
        for (int i = 0; i < game.orderOfPlayerDone.length; i++) {
            if (game.orderOfPlayerDone[i] != -1)
                finishedPlayers.add(i);
        }

        finishedPlayers.sort(Comparator.comparingInt(p -> game.orderOfPlayerDone[p]));

        int rank = 1;
        for (int player : finishedPlayers) {
            System.out.printf("%d° Player %d (finished %d°)%n", rank, player, game.orderOfPlayerDone[player]);
            rank++;
        }

        // last player 
        if (looser != -1) {
            System.out.printf("%d° Player %d (last player with cards)%n", rank, looser);
        }

        System.out.println("-------------------------------");
        System.out.println("WINNER: Player " + winner);
        System.out.println("LOSER : Player " + looser);
        System.out.println("===============================\n");
    }
}
