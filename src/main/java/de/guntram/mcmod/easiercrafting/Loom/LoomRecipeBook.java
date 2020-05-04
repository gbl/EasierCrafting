/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.Loom;

import com.mojang.blaze3d.systems.RenderSystem;
import de.guntram.mcmod.easiercrafting.EasierCrafting;
import de.guntram.mcmod.easiercrafting.RecipeBook;
import static de.guntram.mcmod.easiercrafting.RecipeBook.LOGGER;
import de.guntram.mcmod.easiercrafting.RecipeTreeSet;
import de.guntram.mcmod.easiercrafting.delayedslotclicks.DelayedSlotClickQueue;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.container.LoomContainer;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;

/**
 *
 * @author gbl
 */
public class LoomRecipeBook extends RecipeBook {
    
    public LoomRecipeBook(AbstractContainerScreen<LoomContainer> screen) {
        super(screen, 0, 0, 3, 4);
    }
    
    @Override
    public void updateRecipes() {
        
        RecipeTreeSet recipes = new RecipeTreeSet();
        recipes.addAll(LoomRecipeRegistry.getAllRecipes());
        craftableCategories = new TreeMap<>();
        craftableCategories.put("Banners", recipes);
        recalcListSize();
    }
    
    @Override
    public void updatePatternMatch() {
        patternMatchingRecipes = new RecipeTreeSet();
        patternListSize = 0;
        String patternText=getPatternText();
        if (patternText.isEmpty()) {
            return;
        }
        Pattern regex = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        for (LoomRecipe recipe: LoomRecipeRegistry.getAllRecipes()) {
            if (regex.matcher(EasierCrafting.recipeDisplayName(recipe)).find()) {
                patternMatchingRecipes.add(recipe);
            }
        }
        recalcPatternMatchSize();
    }

    @Override
    public int drawRecipeOutputs(RecipeTreeSet recipes, ItemRenderer itemRenderer, TextRenderer fontRenderer, int xpos, int ypos, int mouseX, int mouseY) {
        return super.drawRecipeOutputs(recipes, itemRenderer, fontRenderer, xpos, ypos, mouseX, mouseY);
    }
    
    @Override
    public void renderSingleRecipeOutput(ItemRenderer itemRenderer, TextRenderer fontRenderer,
            ItemStack items, int x, int y) {

        RenderSystem.pushMatrix();
        RenderSystem.scaled(1.5, 1.5, 1.5);
        itemRenderer.renderGuiItemIcon(items, x*2/3, y*2/3);
        RenderSystem.popMatrix();
        itemRenderer.renderGuiItemOverlay(fontRenderer, items, x, y);

/*
        BannerBlockEntity bbe =  new BannerBlockEntity();
        bbe.readFrom(items, ((BannerItem)items.getItem()).getColor());
        bbe.setPreview(true);
        MatrixStack stack = new MatrixStack();
        stack.translate(x, y, 0);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        ModelPart modelPart = BannerBlockEntityRenderer.createField();
        BannerBlockEntityRenderer.method_23802(bbe, stack, immediate, 
                15728880, OverlayTexture.DEFAULT_UV, modelPart, ModelLoader.BANNER_BASE, true);
*/
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) {
        if (underMouse != null) {
            System.out.println("try to craft "+underMouse.getGroup());
            craftRecipe((LoomRecipe) underMouse);
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton, guiLeft, guiTop);
        }
    }
    
    @Override
    public boolean charTyped(char chr, int keyCode) {
        System.out.println("char '"+chr+"'");
        if (chr == ' ') {
            DelayedSlotClickQueue.execute();
            return true;
        }
        return super.charTyped(chr, keyCode);
    }
    
    public void craftRecipe(final LoomRecipe recipe) {
        int originalBannerSlotIndex = findBlankBannerSlot();
        if (originalBannerSlotIndex == -1) {
            LOGGER.warn("found no inventory slot for empty banner");
            return;
        }
        DelayedSlotClickQueue.addGenerator(() -> {
            LOGGER.info("transfer banner from "+originalBannerSlotIndex+" to 0");
            transfer(originalBannerSlotIndex, 0, 1);
        });
        
        for (int i=1; i<recipe.getStepCount(); i++) {
            DelayedSlotClickQueue.addGenerator(new DelayedSlotClickQueue.ClickGenerator<Integer>(i) {
                @Override
                public void runWithInfo(Integer i) {
                    LoomStep step = recipe.getStep(i);
                    BannerPattern pattern = BannerPattern.byId(step.pattern);
                    if (pattern == null) {
                        LOGGER.warn("no BannerPattern found for "+step.pattern);
                        return;
                    }
                    DyeColor dye = DyeColor.byId(((ExtendedGuiLoom)screen).getColor(step.colorCode-'A'));
                    int dyeSlotIndex = findItemSlot(DyeItem.byColor(dye));
                    if (dyeSlotIndex == -1) {
                        LOGGER.warn("no dye found");
                        return;
                    }
                    LOGGER.info("transfer dye from "+dyeSlotIndex+" to 1");
                    transfer(dyeSlotIndex, 1, 1);
                    if (pattern.ordinal() <= BannerPattern.LOOM_APPLICABLE_COUNT) {
                        LOGGER.info("click container button "+pattern.ordinal());
                        MinecraftClient.getInstance().interactionManager
                                .clickButton((screen.getContainer()).syncId, pattern.ordinal());// click loom button
                    } else {
                        LOGGER.warn("item patterns not implemented yet");
                        return;
                        // ItemStack stack = pattern.baseStack;
                        // find item stack in inventory
                        // transfer to slot 2

                    }

                    // LOGGER.info("returning before first step");
                    // if (i>0) return;

                    if (i == recipe.getStepCount() - 1) {
                        LOGGER.info("transfer banner back to inventory");
                        slotClick(3, 0, SlotActionType.QUICK_MOVE);
                    } else {
                        LOGGER.info("transfer banner to input");
                        transfer(3, 0, 1);
                    }
                }
            });
        }
        // DelayedSlotClickQueue.execute(); Or don't as we expect to get a confirm 
        // for the THROW action that's generated for clicking outside the
        // GUI
    }
    
    @Override
    public void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        DelayedSlotClickQueue.addClick((ExtendedGuiLoom)screen, slot, mouseButton, clickType);
    }
    
    private int findBlankBannerSlot() {
        ItemStack blankBanner = ((ExtendedGuiLoom)screen).getBannerItemStack();
        for (int i=0; i<36; i++) {
            Slot slot=screen.getContainer().getSlot(i+firstInventorySlotNo);
            if (slot.getStack().getItem() != blankBanner.getItem()) {
                continue;
            }
            CompoundTag cTag = slot.getStack().getOrCreateSubTag("BlockEntityTag");
            if (!cTag.contains("Patterns", 9)
            ||  cTag.getList("Patterns", 10).isEmpty()) {
                return i+firstInventorySlotNo;
            }
        }
        return -1;
    }
    
    private int findItemSlot(Item item) {
        for (int i=0; i<36; i++) {
            Slot slot=screen.getContainer().getSlot(i+firstInventorySlotNo);
            if (slot.getStack().getItem() == item) {
                return i+firstInventorySlotNo;
            }
        }
        return -1;
    }
}
