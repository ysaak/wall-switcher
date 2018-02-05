package ysaak.screenchanger;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import ysaak.screenchanger.event.BasicEvent;
import ysaak.screenchanger.event.SetWallpaperEvent;
import ysaak.screenchanger.event.ShowWallpaperEvent;
import ysaak.screenchanger.utils.ProfileUtils;
import ysaak.screenchanger.utils.eventbus.EventBus;
import ysaak.screenchanger.utils.i18n.I18n;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class AppMenu {

    private BooleanProperty defaultProfileAvailable;
    private final ObservableList<String> availableProfiles;

    private final PopupMenu popup;
    private final Menu menu;

    AppMenu() {
        defaultProfileAvailable = new SimpleBooleanProperty(false);
        defaultProfileAvailable.addListener((observable, oldValue, newValue) -> rebuildShowMenu());
        availableProfiles = FXCollections.observableArrayList();
        availableProfiles.addListener((ListChangeListener<String>) c -> rebuildShowMenu());

        popup = new PopupMenu();

        popup.add(new BasicEventMenuItem("tray.showProfileId", BasicEvent.Action.SHOW_CURRENT_PROFILE));
        popup.add(new BasicEventMenuItem("tray.refresh", BasicEvent.Action.REFRESH));

        popup.addSeparator();

        menu = new Menu(I18n.get("tray.showWallpaper"));
        rebuildShowMenu();
        popup.add(menu);

        popup.add(new BasicEventMenuItem("tray.openCreator", BasicEvent.Action.OPEN_CREATOR));

        popup.add(new SetWallpaperMenuItem("tray.changeWallpaper", ProfileUtils.CURRENT_PROFILE));
        popup.add(new SetWallpaperMenuItem("tray.changeDefaultWallpaper", ProfileUtils.DEFAULT_PROFILE));

        popup.addSeparator();

        popup.add(new BasicEventMenuItem("tray.exit", BasicEvent.Action.EXIT));
    }

    BooleanProperty defaultProfileAvailableProperty() {
        return defaultProfileAvailable;
    }

    ObservableList<String> getAvailableProfiles() {
        return availableProfiles;
    }

    PopupMenu getPopup() {
        return popup;
    }

    private void rebuildShowMenu() {
        menu.removeAll();

        if (!defaultProfileAvailable.get() && availableProfiles.isEmpty()) {
            menu.add("No profile defined");
        } else {
            if (defaultProfileAvailable.get()) {
                menu.add(new ShowWallpaperMenuItem(I18n.get("common.default"), ProfileUtils.DEFAULT_PROFILE));

                if (!availableProfiles.isEmpty()) {
                    menu.addSeparator();
                }
            }

            final List<MenuItem> items = new ArrayList<>();
            for (final String profile : availableProfiles) {
                items.add(new ShowWallpaperMenuItem(profile));
            }

			items.sort(Comparator.comparing(MenuItem::getLabel));

            for (final MenuItem item : items) {
                menu.add(item);
            }
        }
    }

    private class BasicEventMenuItem extends MenuItem implements ActionListener {

        private final BasicEvent.Action action;

        BasicEventMenuItem(String label, BasicEvent.Action action) {
            super(I18n.get(label));
            this.action = action;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            EventBus.post(new BasicEvent(action));
        }
    }

    private class SetWallpaperMenuItem extends MenuItem implements ActionListener {
        private final String profileId;

        SetWallpaperMenuItem(String label, String profileId) {
            super(I18n.get(label));
            this.profileId = profileId;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Platform.runLater(() -> {
                Optional<File> file = Dialogs.openFileChooser();
                file.ifPresent(f -> EventBus.post(new SetWallpaperEvent(profileId, f)));
            });
        }
    }

    private class ShowWallpaperMenuItem extends MenuItem implements ActionListener {
        private final String profileId;

        ShowWallpaperMenuItem(String profileId) {
            this(ProfileUtils.buildHumanReadableProfileName(profileId), profileId);

        }

        ShowWallpaperMenuItem(String label, String profileId) {
            super(label);
            this.profileId = profileId;
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            EventBus.post(new ShowWallpaperEvent(profileId));
        }
    }
}
