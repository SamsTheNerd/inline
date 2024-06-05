package com.samsthenerd.inline.api.data;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.EntityTTProvider;
import com.samsthenerd.inline.utils.EntityCradle;
import com.samsthenerd.inline.utils.cradles.EntTypeCradle;
import com.samsthenerd.inline.utils.cradles.NbtCradle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EntityInlineData implements InlineData{

    protected static final Random random = Random.create();
    protected float uniqueOffset = 0;
    private final EntityCradle cradle;

    public EntityInlineData(EntityCradle cradle){
        this.cradle = cradle;
    }

    public static EntityInlineData fromEntity(Entity entity){
        return new EntityInlineData(new NbtCradle(entity));
    }

    public static EntityInlineData fromNbt(NbtCompound tag){
        return new EntityInlineData(new NbtCradle(tag));
    }

    public static EntityInlineData fromType(EntityType type){
        return new EntityInlineData(new EntTypeCradle(type));
    }

    public IDSerializer<EntityInlineData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    @Nullable
    public Entity getEntity(World world){
        return cradle.getEntity(world);
    }

    public float getUniqueOffset(){
        return uniqueOffset;
    }

    public HoverEvent getEntityDisplayHoverEvent(){
        return new HoverEvent(
            HoverEvent.Action.SHOW_ITEM, 
            new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(EntityTTProvider.INSTANCE, cradle))
        ); 
    }

    public Style getDataStyle(boolean withAdditional){
        Style superStyle = InlineData.super.getDataStyle(withAdditional);
        if(!withAdditional) return superStyle;
        return superStyle.withParent(Style.EMPTY.withHoverEvent(getEntityDisplayHoverEvent()));
    }

    public static class Serializer implements InlineData.IDSerializer<EntityInlineData> {
        public static Serializer INSTANCE = new Serializer();

        public EntityInlineData deserialize(JsonObject json){
            return new EntityInlineData(
                EntityCradle.CRADLE_CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(Inline.LOGGER::error).get()
            );
        }

        public JsonElement serializeData(EntityInlineData data){
            return EntityCradle.CRADLE_CODEC.encodeStart(JsonOps.INSTANCE, data.cradle).resultOrPartial(Inline.LOGGER::error).orElseThrow();
        }
    }
}

