package com.github.yuqingliu.economy.api.managers;

import org.bukkit.event.Listener;

import lombok.NonNull;

public interface EventManager {
    void registerEvent(Listener listener);

    void unregisterEvent(String className);

    @NonNull Listener getEvent(String className);
}
