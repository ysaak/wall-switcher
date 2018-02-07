package ysaak.wallswitcher.services.profile;

import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import ysaak.wallswitcher.data.Profile;
import ysaak.wallswitcher.data.ScreenDto;
import ysaak.wallswitcher.event.NewProfileDetectedEvent;
import ysaak.wallswitcher.services.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ProfileService {
    private static final int REFRESH_DELAY = 2000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final transient Object lock = new Object();
    private ScheduledFuture<?> future = null;

    private String lastProfileId = Profile.DEFAULT_PROFILE_ID;

    public void init() {
        updateTaskStart();

        javafx.stage.Screen.getScreens().addListener((final ListChangeListener.Change<? extends javafx.stage.Screen> c) -> updateTaskStart());
    }

    /**
     * Starts the timer of the wallpaper update.
     *
     * The update is delayed since the list is updated multiple times
     */
    private void updateTaskStart() {
        synchronized (lock) {
            if (future == null || future.isDone()) {
                future = scheduler.schedule(this::updateDisplay, REFRESH_DELAY, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Determine current profile et fire event if profile is different from the last one
     */
    private void updateDisplay() {
        final Profile profile = getCurrentProfile();

        if (!profile.getId().equals(lastProfileId)) {
            lastProfileId = profile.getId();
            EventBus.post(new NewProfileDetectedEvent(profile));
        }
    }

    public String getCurrentProfileId() {
        return lastProfileId;
    }

    public Profile getCurrentProfile() {
        final List<ScreenDto> screenList = getScreenList();
        final String profileId = calculateProfileId(screenList);

        return new Profile(profileId, screenList);
    }

    private List<ScreenDto> getScreenList() {
        List<ScreenDto> screens = new ArrayList<>();

        int id = 1;

        for (javafx.stage.Screen s : javafx.stage.Screen.getScreens()) {
            final Rectangle2D bounds = s.getBounds();
            screens.add(new ScreenDto(id++, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
        }

        return screens;
    }

    private static String calculateProfileId(List<ScreenDto> screens) {
        StringBuilder builder = new StringBuilder();

        builder.append(screens.size());

        for (ScreenDto screen : screens) {
            builder.append('-')
                    .append(screen.getX()).append('x')
                    .append(screen.getY()).append('-')
                    .append(screen.getWidth()).append('x')
                    .append(screen.getHeight());
        }

        return builder.toString();
    }
}
