package student;

import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CompPlayer implements Player {

    private int Score = 0;

    private Baron baron;

    private ArrayList<Route> list_of_routes;

    private ArrayList<PlayerObserver> observers;

    private ArrayList<Card> cards;

    private int train = 45;

    private Pair last_cards;

    private Boolean hasClaimed;

    private RailroadMap mymap;

    private RailroadBarons mycontrol;

    private java.util.Map<Card,Integer> colorCount;

    private Deck deck;
    /**
     * Creates a new player
     */
    CompPlayer(Baron baron, Deck deck, model.RailroadMap mymap, RailroadBarons control)
    {
        this.baron = baron;
        cards = new ArrayList<>();
        observers = new ArrayList<>();
        list_of_routes = new ArrayList<>();
        last_cards = new Pair((cardDeck) deck);
        this.deck = deck;
        this.hasClaimed = false;
        this.mymap = mymap;
        this.mycontrol = control;
        colorCount = new java.util.Map<Card, Integer>() {
            @Override
            public int size() {
                return colorCount.size();
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public Integer get(Object key) {
                return null;
            }

            @Override
            public Integer put(Card key, Integer value) {
                return null;
            }

            @Override
            public Integer remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends Card, ? extends Integer> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<Card> keySet() {
                return null;
            }

            @Override
            public Collection<Integer> values() {
                return null;
            }

            @Override
            public Set<Entry<Card, Integer>> entrySet() {
                return null;
            }
        };
        int x = 0;
        while (x<4)
        {
            cards.add(deck.drawACard());
            x++;
        }
        for (Card card : cards)
        {
            if (colorCount.containsKey(card)) {
                colorCount.put(card, colorCount.get(card) + 1);
            }
            else {
                colorCount.put(card, 1);
            }
        }
    }
    /**
     * This is called at the start of every game to reset the player to its
     * initial state:
     * <ul>
     *     <li>Number of train pieces reset to the starting number of 45.</li>
     *     <li>All remaining {@link Card cards} cleared from hand.</li>
     *     <li>Score reset to 0.</li>
     *     <li>Claimed {@link model.Route routes} cleared.</li>
     *     <li>Sets the most recently dealt {@link model.Pair} of cards to two
     *     {@link Card#NONE} values.</li>
     * </ul>
     *
     * @param dealt The hand of {@link Card cards} dealt to the player at the
     *              start of the game. By default this will be 4 cards.
     */
    @Override
    public void reset(Card... dealt) {
        Score = 0;
        cards.clear();
        last_cards = null;
        list_of_routes.clear();
        train = 45;
        baron = null;
    }

    /**
     * Adds an {@linkplain PlayerObserver observer} that will be notified when
     * the player changes in some way.
     *
     * @param observer The new {@link PlayerObserver}.
     */
    @Override
    public void addPlayerObserver(PlayerObserver observer) {
        observers.add(observer);

    }


    /**
     * Removes an {@linkplain PlayerObserver observer} so that it is no longer
     * notified when the player changes in some way.
     *
     * @param observer The {@link PlayerObserver} to remove.
     */
    @Override
    public void removePlayerObserver(PlayerObserver observer) {
        observers.remove(observer);

    }

    /**
     * The {@linkplain Baron baron} as which this player is playing the game.
     *
     * @return The {@link Baron} as which this player is playing.
     */
    @Override
    public Baron getBaron() {
        return baron;
    }

    /**
     * Used to start the player's next turn. A {@linkplain model.Pair pair of cards}
     * is dealt to the player, and the player is once again able to claim a
     * {@linkplain model.Route route} on the {@linkplain RailroadMap map}.
     *
     * @param dealt a {@linkplain model.Pair pair of cards} to the player. Note that
     * one or both of these cards may have a value of {@link Card#NONE}.
     */
    @Override
    public void startTurn(model.Pair dealt)
    {
        this.hasClaimed = false;
        this.last_cards = (Pair) dealt;
        cards.add(dealt.getFirstCard());
        cards.add(dealt.getSecondCard());
        for (model.Route route: mymap.getRoutes()){
            if(this.canClaimRoute(route)){
                try{
                this.claimRoute(route);
                break;
            }catch (RailroadBaronsException ex){
                    ex.printStackTrace();
                }
            }
        }
        for(model.PlayerObserver observer: observers){
            observer.playerChanged(this);
        }
        this.mycontrol.endTurn();
    }

    /**
     * Returns the most recently dealt {@linkplain model.Pair pair of cards}. Note
     * that one or both of the {@linkplain Card cards} may have a value of
     * {@link Card#NONE}.
     *
     * @return The most recently dealt {@link model.Pair} of {@link Card Cards}.
     */

    @Override
    public Pair getLastTwoCards() {
        return last_cards;
    }

    /**
     * Returns the number of the specific kind of {@linkplain Card card} that
     * the player currently has in hand. Note that the number may be 0.
     *
     * @param card The {@link Card} of interest.
     * @return The number of the specified type of {@link Card} that the
     * player currently has in hand.
     */
    @Override
    public int countCardsInHand(Card card) {
        int count = 0;
        for (Card card1 : cards)
        {
            if (card1.equals(card))
            {
                count += 1;
            }
        }
        return count ;
    }

    /**
     * Returns the number of game pieces that the player has remaining. Note
     * that the number may be 0.
     *
     * @return The number of game pieces that the player has remaining.
     */
    @Override
    public int getNumberOfPieces() {
        return train;
    }

    /**
     * Returns true iff the following conditions are true:
     *
     * <ul>
     *     <li>The {@linkplain model.Route route} is not already claimed by this or
     *     some other {@linkplain Baron baron}.</li>
     *     <li>The player has not already claimed a route this turn (players
     *     are limited to one claim per turn).</li>
     *     <li>The player has enough {@linkplain Card cards} (including ONE
     *     {@linkplain Card#WILD wild card, if necessary}) to claim the
     *     route.</li>
     *     <li>The player has enough train pieces to claim the route.</li>
     * </ul>
     *
     * @param route The {@link model.Route} being tested to determine whether or not
     *              the player is able to claim it.
     * @return True if the player is able to claim the specified
     * {@link model.Route}, and false otherwise.
     */
    @Override
    public boolean canClaimRoute(model.Route route)
    {
        if (route.getBaron() == Baron.UNCLAIMED) {
            if(!hasClaimed) {
                if (cards.size() >= route.getLength()){
                    if (train >= route.getLength()) {
                        return true;
                    }
                }
            }
            else {
                return false;
            }
        }
        return false;
    }

    /**
     * Claims the given {@linkplain model.Route route} on behalf of this player's
     * {@linkplain Baron Railroad Baron}. It is possible that the player has
     * enough cards in hand to claim the route by using different
     * combinations of {@linkplain Card card}. It is up to the implementor to
     * employ an algorithm that determines which cards to use, but here are
     * some suggestions:
     * <ul>
     *     <li>Use the color with the lowest number of cards necessary to
     *     match the length of the route.</li>
     *     <li>Do not use a wild card unless absolutely necessary (i.e. the
     *     player has length-1 cards of some color in hand and it is the most
     *     numerous card that the player holds).</li>
     * </ul>
     *
     * @param route The {@link model.Route} to claim.
     *
     * @throws RailroadBaronsException If the {@link model.Route} cannot be claimed,
     * i.e. if the {@link #canClaimRoute(model.Route)} method returns false.
     */
    @Override
    public void claimRoute(model.Route route) throws RailroadBaronsException {

        if (this.canClaimRoute(route))
        {
            route.claim(this.baron);
            this.hasClaimed = true;
            mymap.routeClaimed(route);
            Score = Score + route.getPointValue();
            train = train - route.getLength();
            for(model.PlayerObserver observer: observers){
                observer.playerChanged(this);
            }
        }
        else
        {
            throw new RailroadBaronsException("Route has already been claimed");
        }
    }
    /**
     * Returns the {@linkplain Collection collection} of {@linkplain model.Route
     * routes} claimed by this player.
     *
     * @return The {@link Collection} of {@linkplain model.Route Routes} claimed by
     * this player.
     */
    @Override
    public Collection<model.Route> getClaimedRoutes() {
        return new ArrayList<>(list_of_routes);
    }
    /**
     * Returns the players current score based on the
     * {@linkplain model.Route#getPointValue() point value} of each
     * {@linkplain model.Route route} that the player has currently claimed.
     *
     * @return The player's current score.
     */
    @Override
    public int getScore() {
        return Score;
    }

    /**
     * alters the score of the player
     * @param alter the altered score
     */
    public void AlterScore(int alter){
        this.Score = this.Score + alter;
    }
    /**
     * Returns true iff the following conditions are true:
     *
     * <ul>
     *     <li>The player has enough {@linkplain Card cards} (including
     *     {@linkplain Card#WILD wild cards}) to claim a
     *     {@linkplain model.Route route} of the specified length.</li>
     *     <li>The player has enough train pieces to claim a
     *     {@linkplain model.Route route} of the specified length.</li>
     * </ul>
     *
     * @param shortestUnclaimedRoute The length of the shortest unclaimed
     *                               {@link model.Route} in the current game.
     *
     * @return True if the player can claim such a {@link model.Route route}, and
     * false otherwise.
     */
    @Override
    public boolean canContinuePlaying(int shortestUnclaimedRoute) {
        return (shortestUnclaimedRoute >= train || cards.size() >= train);
    }
}
