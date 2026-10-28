package vibe.client.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    HUD("HUD");

    private final String title;

    Category(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
