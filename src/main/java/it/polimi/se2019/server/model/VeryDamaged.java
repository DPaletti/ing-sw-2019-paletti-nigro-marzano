package it.polimi.se2019.server.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines a player that was hit 6 times or over. This player can use the standard moves plus an extra move.
 * See {@link it.polimi.se2019.server.model.PlayerDamage}.
 */

public class VeryDamaged extends PlayerDamage {
    private boolean added = false;

    @Override
    public List<ArrayList<PartialCombo>> getMoves() {
        if (!added)
            addMoves();
        return moves;
    }

    @Override
    public Integer getMaximumHits() { return 10; }

    @Override
    public PlayerDamage findNextHealthState() {
        return new Healthy();
    }

    @Override
    public boolean isFinalFrenzy() {
        return false;
    }

    @Override
    protected void addMoves() {
        moves.addAll(findPreviousHealthState().getMoves());
        ArrayList<PartialCombo> combos = new ArrayList<>(Arrays.asList(PartialCombo.MOVE, PartialCombo.SHOOT));
        moves.add(combos);
        added = true;
    }

    @Override
    public PlayerDamage findPreviousHealthState() {
        return new SlightlyDamaged();
    }
}
