package it.polimi.se2019.server.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlightlyDamaged extends PlayerDamage {
    private boolean added = false;

    @Override
    public List<ArrayList<PartialCombo>> getMoves() {
        if (!added)
            addMoves();
        return moves;
    }

    @Override
    public Integer getMaximumHits() { return 6; }

    @Override
    public PlayerDamage findNextHealthState() {
        return new VeryDamaged();
    }

    @Override
    public boolean isFinalFrenzy() {
        return false;
    }

    @Override
    protected void addMoves() {
        moves.addAll(findPreviousHealthState().getMoves());
        ArrayList<PartialCombo> combos = new ArrayList<>(Arrays.asList(PartialCombo.MOVE, PartialCombo.MOVE, PartialCombo.GRAB));
        moves.add(combos);
        added = true;
    }

    @Override
    public PlayerDamage findPreviousHealthState() {
        return new Healthy();
    }
}
