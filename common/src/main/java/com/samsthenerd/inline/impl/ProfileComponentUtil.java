package com.samsthenerd.inline.impl;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.type.ProfileComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface ProfileComponentUtil {
    static ProfileComponent from(@Nullable UUID id, @Nullable String name) {
        return new ProfileComponent(Optional.ofNullable(name), Optional.ofNullable(id), new PropertyMap());
    }
}
