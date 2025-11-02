package games.president;

import core.AbstractParameters;
import core.Game;
import evaluation.optimisation.TunableParameters;
import games.GameType;

import java.util.Arrays;
import java.util.Objects;

public class PresidentParameters extends TunableParameters {

    public String dataPath = "data/president/";
    public int nNumberCards = 20; //10*2

    //min 1 -> max 10
    public int minCardValue = 1;
    public int maxCardValue = 10;
    public int copiesPerValue = 2; //fixed

    public PresidentParameters() {
        addTunableParameter("minCardValue", 2, Arrays.asList(2, 3, 4));
        addTunableParameter("maxCardValue", 10, Arrays.asList(10, 11, 12));
        _reset();
    }

    @Override
    public void _reset() {
        minCardValue = (int) getParameterValue("minCardValue");
        maxCardValue = (int) getParameterValue("maxCardValue");
    }

    @Override
    protected AbstractParameters _copy() {
        PresidentParameters copy = new PresidentParameters();
        copy.minCardValue = this.minCardValue;
        copy.maxCardValue = this.maxCardValue;
        copy.copiesPerValue = this.copiesPerValue;
        return copy;
    }

    public String getDataPath() {
        return dataPath;
    }

    @Override
    protected boolean _equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PresidentParameters that = (PresidentParameters) o;
        return nNumberCards == that.nNumberCards
                && minCardValue == that.minCardValue
                && maxCardValue == that.maxCardValue
                && copiesPerValue == that.copiesPerValue
                && Objects.equals(dataPath, that.dataPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataPath, nNumberCards, minCardValue, maxCardValue, copiesPerValue);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.President,new PresidentForwardModel(),new PresidentGameState(this, GameType.President.getMinPlayers())
        );
    }

}
