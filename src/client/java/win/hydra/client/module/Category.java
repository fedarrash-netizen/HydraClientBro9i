package win.hydra.client.module;

/**
 * Module categories used in ClickGui.
 */
public enum Category {
    COMBAT,
    MOVEMENT,
    RENDER,
    PLAYER,
    HUD,
    MISC;

    public String getDisplayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}


