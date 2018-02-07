package ysaak.wallswitcher.event;

/**
 * Basic action event
 */
public class BasicEvent implements ActionEvent {

    private final Action action;

    public BasicEvent(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        EXIT,
        REFRESH,
        OPEN_CREATOR
    }
}
