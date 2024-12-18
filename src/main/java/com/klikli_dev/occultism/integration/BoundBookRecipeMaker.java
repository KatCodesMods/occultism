package com.klikli_dev.occultism.integration;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.crafting.recipe.BoundBookOfBindingRecipe;
import com.klikli_dev.occultism.registry.OccultismItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class BoundBookRecipeMaker {

    public static List<CraftingRecipe> createRecipes() {
        return List.of(
                makeRecipe(new ItemStack(OccultismItems.BOOK_OF_BINDING_FOLIOT.get())),
                makeRecipe(new ItemStack(OccultismItems.BOOK_OF_BINDING_DJINNI.get())),
                makeRecipe(new ItemStack(OccultismItems.BOOK_OF_BINDING_AFRIT.get())),
                makeRecipe(new ItemStack(OccultismItems.BOOK_OF_BINDING_MARID.get()))
        );
    }

    private static CraftingRecipe makeRecipe(ItemStack bookOfBinding) {
        String group = "occultism.bound_book_of_binding";
        var id = new ResourceLocation(Occultism.MODID, group + bookOfBinding.getDescriptionId());
        return new ShapelessRecipe(id, group, CraftingBookCategory.MISC,
                BoundBookOfBindingRecipe.getBoundBookFromBook(bookOfBinding),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(OccultismItems.DICTIONARY_OF_SPIRITS.get()), Ingredient.of(bookOfBinding)));
    }

}
