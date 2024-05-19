package com.samsthenerd.inline.api.data;

import javax.annotation.Nullable;

import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.impl.data.EntityTypeInlineData;
import com.samsthenerd.inline.impl.data.NbtEntityInlineData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class EntityInlineData implements InlineData{

    protected static final Random random = Random.create();
    protected float uniqueOffset = 0;

    public static EntityInlineData fromEntity(Entity entity){
        return new NbtEntityInlineData(entity);
    }

    public static EntityInlineData fromNbt(NbtCompound nbt){
        return new NbtEntityInlineData(nbt);
    }

    public static EntityInlineData fromType(EntityType type){
        return new EntityTypeInlineData(type);
    }

    public abstract Identifier getDataType();

    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "entity");
    }

    public abstract IDSerializer<? extends EntityInlineData> getSerializer();

    @Nullable
    public abstract Entity getEntity(World world);

    public float getUniqueOffset(){
        return uniqueOffset;
    }
}

