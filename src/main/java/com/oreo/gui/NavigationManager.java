package com.oreo.gui;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NavigationManager {

    private static final Map<UUID, Deque<String>> history = new ConcurrentHashMap<>();

    private NavigationManager() {}

    public static void push(UUID playerId, String guiId) {
        history.computeIfAbsent(playerId, k -> new ArrayDeque<>()).push(guiId);
    }

    public static String pop(UUID playerId) {
        Deque<String> stack = history.get(playerId);
        if (stack == null || stack.isEmpty()) return null;
        return stack.pop();
    }

    public static String peekFirst(UUID playerId) {
        Deque<String> stack = history.get(playerId);
        if (stack == null || stack.isEmpty()) return null;

        return ((ArrayDeque<String>) stack).peekLast();
    }

    public static void clearAll(UUID playerId) {
        history.remove(playerId);
    }

    public static boolean hasHistory(UUID playerId) {
        Deque<String> stack = history.get(playerId);
        return stack != null && !stack.isEmpty();
    }

    public static int size(UUID playerId) {
        Deque<String> stack = history.get(playerId);
        return stack == null ? 0 : stack.size();
    }
}
