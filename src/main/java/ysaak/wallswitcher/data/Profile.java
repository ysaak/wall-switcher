package ysaak.wallswitcher.data;

import java.util.List;

public class Profile {
    public static final String DEFAULT_PROFILE_ID = "default";

    private final String id;

    private final List<ScreenDto> screenList;

    public Profile(String id, List<ScreenDto> screenList) {
        this.id = id;
        this.screenList = screenList;
    }

    public String getId() {
        return id;
    }

    public List<ScreenDto> getScreenList() {
        return screenList;
    }
}
