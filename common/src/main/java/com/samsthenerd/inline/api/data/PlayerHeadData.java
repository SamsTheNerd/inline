package com.samsthenerd.inline.api.data;


import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.EntityTTProvider;
import com.samsthenerd.inline.utils.cradles.PlayerCradle;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PlayerHeadData implements InlineData<PlayerHeadData> {
    private ProfileComponent profile;

    public PlayerHeadData(ProfileComponent profile) {
        this.profile = profile;
    }

    @Override
    public PlayerHeadDataType getType() {
        return PlayerHeadDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId() {
        return Inline.id("playerhead");
    }

    public HoverEvent getEntityDisplayHoverEvent() {
        return new HoverEvent(
          HoverEvent.Action.SHOW_ITEM,
          new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(EntityTTProvider.INSTANCE, new PlayerCradle(profile())))
        );
    }

    public Style getDataStyle(boolean withAdditional) {
        Style superStyle = InlineData.super.asStyle(withAdditional);
        if (!withAdditional) return superStyle;
        return superStyle.withParent(Style.EMPTY.withHoverEvent(getEntityDisplayHoverEvent()));
    }

    public ProfileComponent profile() {
        if (!profile.isCompleted()) {
            profile.getFuture().thenAcceptAsync(profileComponent -> {
                profile = profileComponent;
            });
        }
        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PlayerHeadData) obj;
        return Objects.equals(this.profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile);
    }

    @Override
    public String toString() {
        return "PlayerHeadData[" +
               "profile=" + profile + ']';
    }


    public static class PlayerHeadDataType implements InlineDataType<PlayerHeadData> {
        public static PlayerHeadDataType INSTANCE = new PlayerHeadDataType();

        @Override
        public Identifier getId() {
            return Inline.id("playerhead");
        }

        @Override
        public Codec<PlayerHeadData> getCodec() {
            return ProfileComponent.CODEC.xmap(PlayerHeadData::new, data -> data.profile);
        }
    }
}
