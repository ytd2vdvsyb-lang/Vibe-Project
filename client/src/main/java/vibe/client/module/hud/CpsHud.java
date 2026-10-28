package vibe.client.module.hud;

import net.minecraft.client.Minecraft;
import vibe.client.module.TextHudModule;

import java.util.ArrayDeque;
import java.util.Deque;

/** Клики в секунду для левой и правой кнопки. */
public final class CpsHud extends TextHudModule {
    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private boolean leftWasDown;
    private boolean rightWasDown;

    public CpsHud() {
        super("cps", "CPS", "Клики в секунду", false, 4, 58);
    }

    /** Опрашивает кнопки каждый кадр, чтобы ловить быстрые клики. */
    public void sample(Minecraft mc) {
        long now = System.currentTimeMillis();
        boolean leftDown = mc.options.keyAttack.isDown();
        boolean rightDown = mc.options.keyUse.isDown();
        if (leftDown && !leftWasDown) left.addLast(now);
        if (rightDown && !rightWasDown) right.addLast(now);
        leftWasDown = leftDown;
        rightWasDown = rightDown;
        while (!left.isEmpty() && now - left.peekFirst() > 1000) left.removeFirst();
        while (!right.isEmpty() && now - right.peekFirst() > 1000) right.removeFirst();
    }

    @Override
    protected String text(Minecraft mc) {
        return left.size() + " | " + right.size() + " CPS";
    }
}
