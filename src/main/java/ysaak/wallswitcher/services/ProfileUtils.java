package ysaak.wallswitcher.services;

/**
 * Profiles utilities
 */
public final class ProfileUtils {
    /**
     * Default profile ID
     */
    public static final String DEFAULT_PROFILE = "default";

    /**
     * Current profile ID
     */
    public static final String CURRENT_PROFILE = "current";

    public static String buildHumanReadableProfileName(final String profileId) {
        final String elements[] = profileId.split("-");

        final StringBuilder name = new StringBuilder("[");
        name.append(elements[0]).append("] ");

        for (int i = 2; i < elements.length; i += 2) {
            if (i != 2) {
                name.append(" / ");
            }

            name.append(elements[i].replace(".0", ""));
        }

        return name.toString();
    }
}
