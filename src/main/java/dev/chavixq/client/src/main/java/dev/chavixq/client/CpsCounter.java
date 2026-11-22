package dev.chavixq.client;

import java.util.ArrayDeque;
import java.util.Deque;

final class CpsCounter {
    private static final Deque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown;

    static void tick(boolean down) {
        long now = System.currentTimeMillis();
        if (down && !wasDown) clicks.addLast(now);
        wasDown = down;
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) clicks.pollFirst();
    }

    static int cps() {
        return clicks.size();
    }

    private CpsCounter() {}
}
