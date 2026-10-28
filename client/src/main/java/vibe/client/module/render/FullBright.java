package vibe.client.module.render;

import vibe.client.module.Category;
import vibe.client.module.Module;

/** Максимальная яркость: всё видно в пещерах и ночью. Работает через OptionInstanceMixin. */
public final class FullBright extends Module {
    public FullBright() {
        super("full_bright", "FullBright", "Всё светло без факелов", false);
    }

    @Override
    public Category category() {
        return Category.RENDER;
    }
}
