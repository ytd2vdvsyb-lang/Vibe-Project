package vibe.client.module.movement;

import vibe.client.module.Category;
import vibe.client.module.CheatModule;

/** Без урона от падения. Сама работа в LocalPlayerMixin. */
public final class NoFall extends CheatModule {
    public NoFall() {
        super("no_fall", "NoFall", "Нет урона от падения", Category.MOVEMENT);
    }
}
