package ysaak.wallswitcher.event;

import ysaak.wallswitcher.data.Profile;

public class NewProfileDetectedEvent {
    private final Profile profile;

    public NewProfileDetectedEvent(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}
