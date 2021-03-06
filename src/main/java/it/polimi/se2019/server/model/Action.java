package it.polimi.se2019.server.model;

import java.io.Serializable;

/**
 * This class describes the single actions that can be done when using a weapon.
 */

public class Action implements Serializable {
    /**
     * Describes the action.
     */
    private ActionType actionType;
    /**
     * Defines whether the action needs to be done more than once.
     */
    private int value;
    private Direction direction;
    private boolean area;

    public ActionType getActionType() { return actionType; }

    public Direction getDirection() { return direction; }

    public Integer getValue() { return value; }

    public boolean isArea() {
        return area;
    }

    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public void setDirection(Direction direction) { this.direction = direction; }

    public void setValue(Integer value) { this.value = value; }

    @Override
    public String toString() {
        return "Action{" +
                "actionType=" + actionType +
                '}';
    }
}
