package ysaak.screenchanger.event;

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
        SHOW_CURRENT_PROFILE,
        REFRESH,
        OPEN_CREATOR
    }
}
