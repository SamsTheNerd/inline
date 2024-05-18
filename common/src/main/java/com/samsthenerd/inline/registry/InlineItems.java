package com.samsthenerd.inline.registry;

import com.samsthenerd.inline.Inline;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKeys;

public class InlineItems {
    public static DeferredRegister<Item> items = DeferredRegister.create(Inline.MOD_ID, RegistryKeys.ITEM);
    public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Inline.MOD_ID, RegistryKeys.ITEM_GROUP);

    // public static final RegistrySupplier<ItemPokemonCard> POKEMON_CARD_ITEM = item("pokemon_card", () -> new ItemPokemonCard(defItemSettings()));

    // public static final RegistrySupplier<ItemCardPack> CARD_PACK_ITEM = item("card_pack", () -> new ItemCardPack(defItemSettings()));

    // public static final RegistrySupplier<ItemGroup> COBBLE_CARDS_GROUP = TABS.register("cobblecards_tab", () ->
    //         CreativeTabRegistry.create(Text.translatable("itemgroup.cobblecards.general"),
    //                 () -> new ItemStack(POKEMON_CARD_ITEM.get())));

    // public static <T extends Item> RegistrySupplier<T> item(String name, Supplier<T> item) {
	// 	return items.register(new Identifier(CobbleCards.MOD_ID, name), item);
	// }

    public static Item.Settings defItemSettings(){
		return new Item.Settings();
	}

    public static void register(){
        items.register();
        TABS.register();
    }
}
