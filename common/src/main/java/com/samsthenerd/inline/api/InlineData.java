package com.samsthenerd.inline.api;

import com.google.gson.JsonElement;
import com.samsthenerd.inline.impl.InlineStyle;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents some arbitrary data that should be renderered with the
 * specified <code>InlineRenderer</code>.
 * 
 * <p>
 * For the data to be rendered, it must be attached to some Text's Style. 
 * Usually this is done by an {@link InlineMatcher} on the client so that the data
 * can be easily attached anywhere with plain text, however in some cases it may be 
 * more convenient to manually attach the data. 
 * 
 * <p>
 * Unlike most other Inline API classes, InlineData exists on both the client
 * and the server. 
 */
public interface InlineData {

    public Identifier getDataType();

    public Identifier getRendererId();

    public IDSerializer<? extends InlineData> getSerializer();

    public default Style getDataStyle(boolean withAdditional){
        return InlineStyle.fromInlineData(this);
    }

    public default Text getAsText(boolean withAdditional){
        return Text.literal(".").setStyle(getDataStyle(withAdditional));
    }

    public static interface IDSerializer<D extends InlineData> {

        public D deserialize(JsonElement json);

        public JsonElement serializeData(D data);
    }
}
