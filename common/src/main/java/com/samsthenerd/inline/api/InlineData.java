package com.samsthenerd.inline.api;

import com.mojang.serialization.Codec;
import com.samsthenerd.inline.api.matching.InlineMatcher;
import com.samsthenerd.inline.api.client.InlineRenderer;
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
public interface InlineData<Self extends InlineData<Self>> {

    /**
     * Gets the InlineDataType of this data.
     * Used mostly for serialization.
     * @return type of this data.
     */
    InlineDataType<Self> getType();

    /**
     * Gets which {@link InlineRenderer} should be used for rendering this data.
     * @return id of renderer
     */
    Identifier getRendererId();

    /**
     * Gets a suitable Style for this data, without the data itself.
     * <p>
     * This is primarily used for adding hover or click events.
     * @return Style with no attached data
     */
    default Style getExtraStyle(){
        return Style.EMPTY;
    }

    /**
     * Gets a Style with this data attached.
     * @param withExtra whether or not extra styling, such as hover or click events, should be added.
     * @return Style with attached data and extra styling if withExtra is true
     */
    default Style asStyle(boolean withExtra){
        Style dataStyle = InlineStyle.fromInlineData(this);
        if(withExtra){
            dataStyle = dataStyle.withParent(getExtraStyle());
        }
        return dataStyle;
    }

    /**
     * Gets Text with this data attached.
     * <p>
     * The actual Text string is left up to the implementation.
     * @param withExtra whether or not extra styling, such as hover or click events, should be added.
     * @return Text with attached data and extra styling if withExtra is true
     */
    default Text asText(boolean withExtra){
        return Text.literal(".").setStyle(asStyle(withExtra));
    }

    /**
     * The type of some InlineData.
     * Used mostly for deserialization. 
     * <p>
     * Make sure to register each type with {@link InlineAPI#addDataType(InlineDataType)}
     */
    interface InlineDataType<D extends InlineData<D>> {

        /**
         * Gets a unique identifier for this data type. 
         * Used for deserialization
         * @return id
         */
        public Identifier getId();

        /**
         * Gets a codec for serializing data of type D.
         * @return codec
         */
        public Codec<D> getCodec();
    }
}
