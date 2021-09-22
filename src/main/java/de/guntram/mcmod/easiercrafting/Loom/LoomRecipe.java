package de.guntram.mcmod.easiercrafting.Loom;

import de.guntram.mcmod.easiercrafting.EasierCrafting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class LoomRecipe implements Recipe {
    
    private List<LoomStep> steps;
    String name;
    public final static RecipeType recipeType = RecipeType.register("easiercrafting:loom_recipe");

    public LoomRecipe(String name) {
        this.name=name;
        steps = new ArrayList<>();
    }
    
    public void addStep(LoomStep step) {
        steps.add(step);
    }
    
    public String toSaveString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(":\n");
        for (LoomStep step: steps) {
            builder.append(step.pattern).append(":").append(step.colorCode).append("\n");
        }
        return builder.toString();
    }
    
    public static LoomRecipe fromSaveString(String string) throws IllegalArgumentException {
        string=string.replaceAll("\r", "");
        String[] parts = string.split("\n");
        if (!parts[0].endsWith(":")) {
            throw new IllegalArgumentException("First line should be name:");
        }
        LoomRecipe result = new LoomRecipe(parts[0].substring(0, parts[0].length()-1));
        for (int i=1; i<parts.length; i++) {
            String[] patcol = parts[i].split(":");
            if (patcol.length != 2 || patcol[1].length() != 1) {
                throw new IllegalArgumentException("Line "+i+" has bad format "+parts[i]);
            }
            result.addStep(new LoomStep(patcol[0], patcol[1].charAt(0)));
        }
        return result;
    }
    
    public int getStepCount() {
        return steps.size();
    }
    
    public LoomStep getStep(int index) {
        return steps.get(index);
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        return true;
    }

    @Override
    public ItemStack craft(Inventory inv) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        if (MinecraftClient.getInstance().currentScreen instanceof ExtendedGuiLoom) {
            ExtendedGuiLoom screen = (ExtendedGuiLoom) MinecraftClient.getInstance().currentScreen;
            ItemStack bannerStack = screen.getBannerItemStack();
            // ItemStack bannerStack = new ItemStack(new WallBannerBlock(DyeColor.byId(screen.getColor(0)),
            //         Block.Settings.of(Material.WOOD)).asItem());
            NbtCompound compoundTag = bannerStack.getOrCreateSubNbt("BlockEntityTag");
            NbtList patterns;
            if (compoundTag.contains("Patterns", 9)) {
               patterns = compoundTag.getList("Patterns", 10);
            } else {
               patterns = new NbtList();
               compoundTag.put("Patterns", patterns);
            }            
            for (int i=1; i<steps.size(); i++) {            // start at 1 as [0] is the banner itself
                NbtCompound patternElement = new NbtCompound();
                patternElement.putString("Pattern", steps.get(i).pattern);
                patternElement.putInt("Color", screen.getColor(steps.get(i).colorCode-'A'));
                patterns.add(patternElement);
            }
            return bannerStack;
        } else {
            return new ItemStack(Items.AIR);
        }
    }
    
    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        if (MinecraftClient.getInstance().currentScreen instanceof ExtendedGuiLoom) {
            ExtendedGuiLoom screen = (ExtendedGuiLoom) MinecraftClient.getInstance().currentScreen;
            ingredients.add(Ingredient.ofStacks(screen.getBannerItemStack()));
            for (LoomStep step: steps) {
                ingredients.add(
                    Ingredient.ofStacks(new ItemStack(DyeItem.byColor(DyeColor.byId(screen.getColor(step.colorCode-'A')))))
                );
            }
        }        
        return ingredients;
    }

    @Override
    public Identifier getId() {
        return new Identifier(EasierCrafting.MODID, this.name);
    }

    @Override
    public RecipeSerializer getSerializer() {
        return null;
    }

    @Override
    public RecipeType getType() {
        return recipeType;
    }

    @Override
    public String getGroup() {
        return EasierCrafting.MODID+":"+name;
    }
    
}
