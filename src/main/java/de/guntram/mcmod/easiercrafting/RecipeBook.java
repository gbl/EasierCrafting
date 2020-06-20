package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import me.shedaniel.rei.api.ConfigObject;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class RecipeBook {
    
    public static final Logger LOGGER = LogManager.getLogger(RecipeBook.class);

    public final HandledScreen screen;
    private final int firstCraftSlot;
    private final int gridSize;
    private final int resultSlotNo;
    public final int firstInventorySlotNo;
    public TreeMap<String, RecipeTreeSet> craftableCategories;
    public Recipe underMouse;
    
    private final int itemSize=20;
    private final int itemLift=5;         // how many pixels to display items above where they would be normally

    private int listSize;
    private int itemsPerRow;                // # of items per row. Normally 8.
    private int xOffset;                    // offset of list to standard gui. itemSize*itemsPerRow+10.
    private int mouseScroll;
    private int minYtoDraw=0;               // implements clipping top part of the item list
    private int textBoxSize;
    private long recipeUpdateTime;
    private RecipeType wantedRecipeType;

    public TextFieldWidget pattern;
    public RecipeTreeSet patternMatchingRecipes;
    public int patternListSize;
    
    static Identifier arrows;
    private int containerLeft;
    private int containerTop;
    
    ClientPlayerEntity player;
    
/**
 * 
 * @param craftScreen        The container the recipe book is attached to - this
 *      can be a GuiCrafting or a GuiInventory container
 * @param firstCraftSlot    The slot number of the first slot that is a craft
 *      slot in craftinv
 * @param gridsize          2 (for inventory) or 3 (for workbench)
 * @param resultSlot        the slot number of the craft result slot
 * @param firstInventorySlot
 *                          the slot number of the first inventory slot
 */    
    public RecipeBook(HandledScreen craftScreen, int firstCraftSlot, int gridsize, int resultSlot, int firstInventorySlot) {
        this.screen=craftScreen;
        this.firstCraftSlot=firstCraftSlot;
        this.gridSize=gridsize;
        this.resultSlotNo=resultSlot;
        this.firstInventorySlotNo=firstInventorySlot;
        this.pattern=null;
        this.underMouse=null;
        player = MinecraftClient.getInstance().player;
        
        if (screen instanceof ExtendedGuiStonecutter) {
            wantedRecipeType = RecipeType.STONECUTTING;
        } else if (screen instanceof ExtendedGuiCrafting || screen instanceof ExtendedGuiInventory) {
            wantedRecipeType = RecipeType.CRAFTING;
        } else if (screen instanceof ExtendedGuiBrewingStand) {
            wantedRecipeType = BrewingRecipe.recipeType;
        } else {
            wantedRecipeType = null;        // for example with brewing stand
        }

        if (arrows==null) {
            arrows=new Identifier(EasierCrafting.MODID, "textures/arrows.png");
        }
    }
    
    public void afterInitGui() {
        final int distanceFromGui = 25;
        
        this.containerLeft = (screen.width - 176 /*screen.containerWidth */)/2;
        this.containerTop  = (screen.height - 166 /* screen.containerHeight */) /2;

        int tempItemsPerRow = 8;
        int tempXOffset=-itemSize*tempItemsPerRow - distanceFromGui;
        if (tempXOffset+containerLeft < 0) {
            tempItemsPerRow=(containerLeft-distanceFromGui)/itemSize;
            tempXOffset=-itemSize*tempItemsPerRow-distanceFromGui;
        }
        textBoxSize=-tempXOffset-15;
        if (ConfigurationHandler.getShowGuiRight())
            tempXOffset=176 /* screen.containerWidth */  + distanceFromGui;
        if (tempItemsPerRow < 2) {
            System.out.println("forcing tempItemsPerRow to 2 when it's "+tempItemsPerRow);
            tempItemsPerRow = 2;
        }
        this.itemsPerRow=tempItemsPerRow;
        this.xOffset=tempXOffset;
        this.mouseScroll=0;
//        System.out.println("left="+containerLeft+", items="+itemsPerRow+", offset="+tempXOffset+", textbox="+textBoxSize);
        updatePatternMatch();
        updateRecipes();
    }
    
    // left is the X position we want to draw at, Y is (normally) 0.
    // However, if our height is larger than the GUI container height,
    // adjust our Y position accordingly.
    public void drawRecipeList(MatrixStack stack, TextRenderer fontRenderer, ItemRenderer itemRenderer,
            int left, int height, int mouseX, int mouseY) {

        // We can't do this in the constructor as we don't yet know sizes from initGui.
        // Also, not in afterInitGui() because we don't know fontRender there.
        if (pattern==null) {
            pattern=new TextFieldWidget(fontRenderer, xOffset, 0, textBoxSize, 20, new LiteralText(""));
            if (ConfigurationHandler.getAutoFocusSearch()) {
                // doh - in 1.15, changeFocus toggles the focus and ignores the parameter
                pattern.changeFocus(true);
            }
        }
        
        try {
            if (ConfigObject.getInstance().isOverlayVisible()) {
                return;
            }
        } catch (NoClassDefFoundError ex) {
            ;
        }

        boolean underMouseIsCraftable=true;
        if (recipeUpdateTime!=0 && System.currentTimeMillis() > recipeUpdateTime) {
            updateRecipes();
            recipeUpdateTime=0;
        }

        int xpos, ypos=0;
        int neededHeight=patternListSize+listSize;
        
        if (neededHeight>height) {
            ypos-=(neededHeight-height)/2;
            //System.out.println("ypos is now "+ypos);
            if (ypos < -containerTop) {
                ypos = -containerTop;
                //System.out.println("mouse wheel text at"+ypos);
                // fontRenderer.drawString(I18n.format("message.usemouse"), xOffset+itemSize, ypos, 0xff0000);
                MinecraftClient.getInstance().getTextureManager().bindTexture(arrows);
                screen.drawTexture(stack, xOffset,                ypos,  0, 0, 20, 20);
                screen.drawTexture(stack, xOffset+textBoxSize-20, ypos, 20, 0, 20, 20);
                ypos+=itemSize;
            } else {
                mouseScroll=0;
            }
        } else {
            mouseScroll=0;
        }
//        GuiLighting.enable();
//        GuiLighting.enableForItems();

        underMouse=null;

        pattern.y=ypos;
        pattern.renderButton(stack, 0, 0, 0f);    // <-- parameters neccessary but unused 
        ypos+=itemSize*3/2;
        minYtoDraw=ypos;
        ypos-=mouseScroll*itemSize;
        ypos=drawRecipeOutputs(patternMatchingRecipes, itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        if (underMouse!=null)
            underMouseIsCraftable=false;
        
        for (String category: craftableCategories.keySet()) {
//            System.out.println(category+" at "+xOffset+"/"+ypos);
            if (ypos>=minYtoDraw)
                fontRenderer.draw(stack, category, xOffset, ypos, 0xffff00);
            ypos+=itemSize;
            ypos=drawRecipeOutputs(craftableCategories.get(category), itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        }
        if (underMouse!=null) {
            String displayName = EasierCrafting.recipeDisplayName(underMouse);
            fontRenderer.draw(stack, displayName, 0, height+3, 0xffff00);
            if (underMouse instanceof ShapedRecipe) {
                DefaultedList<Ingredient> ingredients = underMouse.getPreviewInputs();
                fontRenderer.draw(stack, "sr", left-20, height, 0x202020);
                for (int x=0; x<((ShapedRecipe)underMouse).getWidth(); x++) {
                    for (int y=0; y<((ShapedRecipe)underMouse).getHeight(); y++) {
                        renderIngredient(itemRenderer, fontRenderer, 
                                ingredients.get(x+y*((ShapedRecipe)underMouse).getWidth()), itemSize*x, height+itemSize+itemSize*y);                        
                    }
                }
            } else if (underMouse instanceof ShapelessRecipe) {
                fontRenderer.draw(stack, "slr", left-20, height, 0x202020);
                xpos=0;
                for (Ingredient ingredient: ((ShapelessRecipe)underMouse).getPreviewInputs()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
            } else if (underMouse instanceof CuttingRecipe) {
                fontRenderer.draw(stack, "from "+((Ingredient)(underMouse.getPreviewInputs().get(0))).getMatchingStacksClient()[0].getName().asString(),
                        0, height+itemSize, 0xffff00);
                xpos=0;
                for (Ingredient ingredient: ((CuttingRecipe)underMouse).getPreviewInputs()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+2*itemSize);
                    xpos++;
                }
            } else if (underMouse instanceof BrewingRecipe) {
                ypos=1;
                for (Object i: ((BrewingRecipe)underMouse).getPreviewInputs()) {
                    Ingredient ingredient = (Ingredient) i;
                    renderIngredient(itemRenderer, fontRenderer, ingredient, 0, height+ypos*itemSize);
                    fontRenderer.draw(stack, ingredient.getMatchingStacksClient()[0].getName(), itemSize, height+5+ypos*itemSize, 0xffff00);
                    ypos++;
                }
            }
        }
     
//        GuiLighting.disable();
        if (!underMouseIsCraftable) {
            // prevent action when clicking the output icon
            underMouse=null;
        }
    }

    public int drawRecipeOutputs(RecipeTreeSet recipes, 
            ItemRenderer itemRenderer, TextRenderer fontRenderer, 
            int xpos, int ypos,
            int mouseX, int mouseY) {

//        System.out.println("drawing recipes at "+xpos+"/"+ypos);
        for (Recipe recipe: recipes) {
            ItemStack items=recipe.getOutput();
            if (ypos>=minYtoDraw) {
                renderSingleRecipeOutput(itemRenderer, fontRenderer, items, xOffset+xpos, ypos-itemLift);
                if (mouseX>=xpos+xOffset  && mouseX<=xpos+xOffset+itemSize-1
                &&  mouseY>=ypos-itemLift && mouseY<=ypos-itemLift+itemSize-1) {
                    underMouse=recipe;
                }
            }
            xpos+=itemSize;
            if (xpos>=itemSize*itemsPerRow) {
                ypos+=itemSize;
                xpos=0;
            }
        }
        if (xpos!=0)
            ypos+=itemSize;
        return ypos;
    }
    
    public void renderSingleRecipeOutput(ItemRenderer itemRenderer, TextRenderer fontRenderer,
            ItemStack items, int x, int y) {
        itemRenderer.renderGuiItemIcon(items, x, y);
        itemRenderer.renderGuiItemOverlay(fontRenderer, items, x, y);
    }
    
    public void renderIngredient(ItemRenderer itemRenderer, TextRenderer fontRenderer, Ingredient ingredient, int x, int y) {
        ItemStack[] stacks=ingredient.getMatchingStacksClient();
        if (stacks.length==0)
            return;
        int toRender=0;
        if (stacks.length>1)
            toRender=(int) ((System.currentTimeMillis()/333)%stacks.length);
        itemRenderer.renderInGuiWithOverrides(stacks[toRender], x, y);
        itemRenderer.renderGuiItemOverlay(fontRenderer, stacks[toRender], x, y);
    }
    
    public void updateRecipes() {
        ScreenHandler inventory = screen.getScreenHandler();
        List<Recipe> recipes = new ArrayList<>();
        if (wantedRecipeType == BrewingRecipe.recipeType) {
            Level level = Level.DEBUG;
            LOGGER.log(level, "recipebook: size= "+inventory.slots.size());

            List<BrewingRecipe> potionRecipes = BrewingRecipeRegistryCache.registeredPotionRecipes();
            Set<BrewingRecipe> possiblePotionRecipes = new HashSet<>();
            List<BrewingRecipe> itemRecipes = BrewingRecipeRegistryCache.registeredItemRecipes();
            Set<BrewingRecipe> possibleItemRecipes = new HashSet<>();
            for (int i=0; i<inventory.slots.size(); i++) {
                // This loop also looks at the items in the brewing stand, which is fine!
                ItemStack stack=inventory.getSlot(i).getStack();
                Potion potionType = PotionUtil.getPotion(stack);
                if (!stack.isEmpty() && potionType != Potions.EMPTY) {
                    BrewingRecipe newRecipe;
                    LOGGER.log(level, "slot "+i+" has "+stack.getCount()+" of "+stack.getItem().getName().asString() + " potion type "+potionType.finishTranslationKey(""));
                    for (BrewingRecipe br: itemRecipes) {
                        if (br.getInputPotion().getItem() == stack.getItem()) {
                            // This potion item can be converted to a different item.
                            // Ignore whether or not we have the ingredient, 
                            // this will be taken care of in the same way as other recipes

                            ItemStack input  = new ItemStack(br.getInputPotion().getItem()); PotionUtil.setPotion(input, potionType);
                            ItemStack output = new ItemStack(br.getOutput().getItem()); PotionUtil.setPotion(output, potionType);
                            possibleItemRecipes.add(newRecipe = new BrewingRecipe(false, input, br.getIngredient(), output));
                            LOGGER.log(level, "adding recipe "+newRecipe.toString());
                        }
                    }
                    for (BrewingRecipe br: potionRecipes) {
                        if (PotionUtil.getPotion(br.getInputPotion()) == potionType) {
                            ItemStack input = new ItemStack(stack.getItem()); PotionUtil.setPotion(input, potionType);
                            ItemStack output = new ItemStack(stack.getItem()); PotionUtil.setPotion(output, PotionUtil.getPotion(br.getOutput()));
                            possiblePotionRecipes.add(newRecipe = new BrewingRecipe(true, input, br.getIngredient(), output));
                            LOGGER.log(level, "adding recipe "+newRecipe.toString());
                        }
                    }
                }
            }
            recipes.addAll(possibleItemRecipes);
            recipes.addAll(possiblePotionRecipes);
        } else {
            recipes.addAll(player.world.getRecipeManager().values());
            recipes.addAll(LocalRecipeManager.getInstance().values());
            if (wantedRecipeType == RecipeType.CRAFTING && ConfigurationHandler.getAllowGeneratedRecipes()) {
                recipes.addAll(InventoryRecipeScanner.findUnusualRecipes(inventory, firstInventorySlotNo));
            }
        }

        craftableCategories=new TreeMap<>();
        for (Recipe recipe:recipes) {
            if (!recipeTypeMatchesWorkstation(recipe))
                continue;
            if (!canCraftRecipe((Recipe)recipe, inventory, gridSize))
                continue;
            //System.out.println("grid size is "+gridSize+", recipe needs "+recipe.getRecipeSize());
            ItemStack result=recipe.getOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            ItemGroup tab = item.getGroup();
            String category;
            if (!ConfigurationHandler.getCategorizeRecipes()) {
                category="Possible Recipes";
            } else if (recipe.getGroup().startsWith(EasierCrafting.MODID+":")) {
                category = "Special Recipes";
            } else if (tab==null) {
                    category=recipe.getGroup();
            } else {
                if (wantedRecipeType == RecipeType.STONECUTTING) {
                    Block block = Block.getBlockFromItem(item);
                    if (block instanceof StairsBlock) {
                        category = "Stairs";
                    } else if (block instanceof SlabBlock) {
                        category = "Slabs";
                    } else if (block instanceof WallBlock) {
                        category = "Walls";
                    } else {
                        category = "Blocks";
                    }
                } else if (wantedRecipeType == BrewingRecipe.recipeType) {
                    category=((BrewingRecipe)recipe).getCategory();
                } else {
                    category=I18n.translate(tab.getTranslationKey(), new Object[0]);
                }
            }
            RecipeTreeSet catRecipes=craftableCategories.get(category);
            if (catRecipes==null) {
                catRecipes=new RecipeTreeSet();
                craftableCategories.put(category, catRecipes);
            }
            //System.out.println("adding "+result.getDisplayName()+" in "+category);
            catRecipes.add((Recipe)recipe);
        }
        recalcListSize();
    }
    
    public void recalcListSize() {
        listSize=craftableCategories.size();
        for (RecipeTreeSet tree: craftableCategories.values())
            listSize+=((tree.size()+(itemsPerRow-1))/itemsPerRow);
        listSize*=itemSize;
        mouseScroll=0;
    }
    
    public String getPatternText() {
        if (pattern == null)
            return "";
        return pattern.getText();
    }
    
    public void updatePatternMatch() {
        patternListSize=0;
        patternMatchingRecipes=new RecipeTreeSet();

        String patternText=getPatternText();
        if (patternText.length()<2)
            return;

        List<Recipe> recipes = new ArrayList<>();
        if (wantedRecipeType == BrewingRecipe.recipeType) {
            recipes.addAll(BrewingRecipeRegistryCache.registeredBrewingRecipes());
        } else {
            recipes.addAll(player.world.getRecipeManager().values());
            recipes.addAll(LocalRecipeManager.getInstance().values());
        }
        Pattern regex=Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        for (Recipe recipe:recipes) {
            if (!recipeTypeMatchesWorkstation(recipe))
                continue;
            ItemStack result=recipe.getOutput();
            if (result.getItem() == Items.AIR) {
                continue;
            }
            if (!regex.matcher(EasierCrafting.recipeDisplayName(recipe)).find()) {
                //System.out.println("not adding "+result.getDisplayName()+" because no match");
                continue;
            }
            //System.out.println("adding "+result.getDisplayName()+" to pattern match "+patternText);
            patternMatchingRecipes.add(recipe);
        }
        recalcPatternMatchSize();
    }
    
    public void recalcPatternMatchSize() {
        patternListSize=((patternMatchingRecipes.size()+(itemsPerRow-1))/itemsPerRow)*itemSize;
        mouseScroll=0;
    }
    
    private boolean recipeTypeMatchesWorkstation(Recipe recipe) {
        return wantedRecipeType == recipe.getType();
    }

    class Takefrom { 
        Slot invitem; int amount; 
        Takefrom(Slot i, int n) { invitem=i; amount=n; }
    };

    private boolean canCraftRecipe(Recipe recipe, ScreenHandler inventory, int gridSize) {
        if (recipe instanceof ShapelessRecipe) {
            return canCraftShapeless((ShapelessRecipe) recipe, inventory);
        } else if (recipe instanceof ShapedRecipe) {
            return canCraftShaped((ShapedRecipe) recipe, inventory, gridSize);
        } else if (recipe instanceof InventoryGeneratedRecipe || recipe instanceof RepairRecipe) {
            return recipe.fits(gridSize, gridSize);
        } else if (recipe instanceof CuttingRecipe) {
            ItemStack stack = recipe.getOutput();
            LOGGER.debug("output: " + stack.getItem().getName().asString());
            for (Ingredient ing : (List<Ingredient>) recipe.getPreviewInputs()) {
                ItemStack[] stacks = ing.getMatchingStacksClient();
                if (stacks.length > 1) {
                    LOGGER.debug(stacks.length + " possible inputs for " + stack.getItem().getName().asString());
                    for (ItemStack stack2 : stacks) {
                        LOGGER.debug("    " + stack2.getItem().getName().asString());
                    }
                }
            }
            return canCraftCutting((CuttingRecipe) recipe, inventory);
        } else if (recipe instanceof BrewingRecipe) {
            return canBrew((BrewingRecipe)recipe, inventory);
        } else {
            //System.out.println(recipe.getRecipeOutput().getDisplayName()+" is a "+recipe.getClass().getCanonicalName());
        }
        return false;
    }
    
    private boolean canCraftShapeless(ShapelessRecipe recipe, ScreenHandler inventory) {
        DefaultedList<Ingredient> neededList = recipe.getPreviewInputs();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShaped(ShapedRecipe recipe, ScreenHandler inventory, int gridSize) {
        if (!recipe.fits(gridSize, gridSize)) {
            return false;
        }
        DefaultedList<Ingredient> neededList = recipe.getPreviewInputs();
        return canCraft(recipe, neededList, inventory);
    }
    
    private boolean canCraftCutting(CuttingRecipe recipe, ScreenHandler inventory) {
        DefaultedList<Ingredient> neededList = recipe.getPreviewInputs();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftOre(Recipe recipe, DefaultedList<Ingredient>input, ScreenHandler inventory) {
        return canCraft(recipe, input, inventory);
    }

    private boolean canCraft(Recipe recipe, List<Ingredient> neededList, ScreenHandler inventory) {
        ArrayList<Takefrom> source=new ArrayList<>(neededList.size());
        for (Ingredient neededItem: neededList) {                                // iterate over needed items
            ItemStack[] stacks=neededItem.getMatchingStacksClient();
            if (stacks.length==0)
                continue;
            int neededAmount=stacks[0].getCount();
            // System.out.println("need "+neededAmount+" "+stacks[0].getDisplayName()+" for "+recipe.getRecipeOutput().getDisplayName());
            if (recipe.getOutput().getItem() == Items.DISPENSER) {
                LOGGER.debug("look for dispenser item "+I18n.translate(stacks[0].getItem().getTranslationKey()));
            }
            for (int i=0; i<36; i++) {
                Slot invitem=inventory.getSlot(i+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(neededItem, slotcontent)) {
                    if (recipe.getOutput().getItem() == Items.DISPENSER) {
                        LOGGER.debug("Item in inv slot " +i + ":" +I18n.translate(slotcontent.getItem().getTranslationKey()) + " works");
                    }
                    int providedAmount=slotcontent.getCount();                 // check how many items there are
                    for (int j=0; j<source.size(); j++)                         // subtract how many have been used on other slots
                        if (source.get(j).invitem==invitem)
                            providedAmount-=source.get(j).amount;
                    if (providedAmount>neededAmount)                            // don't provide more than needed
                        providedAmount=neededAmount;
                    if (providedAmount>0) {
                        source.add(new Takefrom(invitem, providedAmount));      // and remember how much we can take from here
                        neededAmount-=providedAmount;
                    }
                }
                else {
                    if (recipe.getOutput().getItem() == Items.DISPENSER) {
                        LOGGER.debug("Item in inv slot " +i + ":" +I18n.translate(stacks[0].getItem().getTranslationKey()) + "doesn't work");
                    }
                }
            }
            if (neededAmount>0) {                                               // we don't have enough of this item so we can't craft this
                //System.out.println("can't craft "+recipe.getRecipeOutput().getDisplayName()+" because we don't have "+neededItem.getCount()+" "+neededItem.getDisplayName());
                return false;
            }
        }
        //System.out.println("enough stuff for "+recipe.getRecipeOutput().getDisplayName());
        return true;
    }

    // This is a bit more complicated, because we have to handle item recipes (potion -> splash -> lingering)
    // as well as potion recipes (Item stays the same, but the Potion NBT tag changes).
    // In both cases, we need the ingredient, which can be in the player inventory or the brewing stand.
    // In case of Potion recipes, we need the input potion type in either inventory or brewing stand. Brewing
    // stand is fine, we know exactly what's going to be crafted in this case. But if all inputs are in the
    // player inventory, and there's more than one usable item (for example, Potion water -> weakness, and the player
    // has water and splash water in his inventory; or potion -> splash, and the player has weakness and night vision
    // in his inventory), we might want to act somehow to prevent crafting the wrong input ...

    private boolean canBrew(BrewingRecipe recipe, ScreenHandler inventory) {
        List<Ingredient> inputs=recipe.getPreviewInputs();
        Item ingredient = inputs.get(1).getMatchingStacksClient()[0].getItem();
        ItemStack inputPotionStack = inputs.get(0).getMatchingStacksClient()[0];
        Potion inputPotion = PotionUtil.getPotion(inputs.get(0).getMatchingStacksClient()[0]);
        boolean haveIngredient = false;
        boolean haveInputPotion = false;

        Level level=Level.DEBUG;
        if (ingredient == Items.GUNPOWDER || ingredient == Items.NETHER_WART) {
            level = Level.INFO;
        }

/*        LOGGER.log(level, "Check for "+(recipe.isItemRecipe() ? "Item recipe " : "Potion recipe ")+
                PotionUtil.getPotion(recipe.getOutput()).getName(recipe.getOutput().getItem().getName().asString()+" ")+
                " from "+
                PotionUtil.getPotion(inputPotionStack).getName(inputPotionStack.getItem().getName().asString()+" ")+
                " and "+
                ingredient.getName().asString()); */
        // check if the brewing stand has a usable input potion
        for (int i=0; i<3; i++) {
            ItemStack inventoryItemStack = inventory.getSlot(i+firstCraftSlot).getStack();
            if (recipe.isItemRecipe()) {
                haveInputPotion |= (inventoryItemStack.getItem() == inputPotionStack.getItem());
            } else {
                haveInputPotion |= PotionUtil.getPotion(inventoryItemStack) == PotionUtil.getPotion(inputPotionStack);
            }
            if (haveInputPotion) {
                break;
            }
        }
//        LOGGER.log(level, "  haveInputPotion in Brewing Stand is "+haveInputPotion);

        // check if the brewing stand already has the ingredient
//        LOGGER.log(level, "  ing slot item is "+inventory.getSlot(3+firstCraftSlot).getStack().getItem());
//        LOGGER.log(level, "  ingredient is "+ingredient);
        if (inventory.getSlot(3+firstCraftSlot).getStack().getItem() == ingredient) {
            haveIngredient = true;
        }
//        LOGGER.log(level, "  haveIngredient in Brewing Stand is "+haveIngredient);

        // check the player inventory
        for (int i=0; i<36; i++) {
            if (haveInputPotion && haveIngredient) {
                break;
            }
            ItemStack inventoryItemStack = inventory.getSlot(i+firstInventorySlotNo).getStack();
            if (inventoryItemStack.isEmpty())
                continue;

            if (recipe.isItemRecipe()) {
//                LOGGER.log(Level.TRACE, "item recipe compare "+inventoryItemStack.getItem()+" to "+inputPotionStack.getItem());
                haveInputPotion |= (inventoryItemStack.getItem() == inputPotionStack.getItem());
            } else {
//                LOGGER.log(Level.TRACE, "potion recipe compare "+PotionUtil.getPotion(inventoryItemStack).getName("")+" to "+PotionUtil.getPotion(inputPotionStack).getName(""));
                haveInputPotion |= (PotionUtil.getPotion(inventoryItemStack) == PotionUtil.getPotion(inputPotionStack));
            }
            if (inventoryItemStack.getItem() == ingredient) {
                haveIngredient = true;
            }
        }
//         LOGGER.log(level, MessageFormat.format("  after player inv; haveInputPotion = {0}, haveIngredient ={1}", haveInputPotion, haveIngredient));
        return haveInputPotion && haveIngredient;
    }
    
    class InputCount {
        int count;
        int items;
    }
    
    public void scrollBy(int ticks) {
        int old=mouseScroll;
        if (ticks<=-100 && mouseScroll*itemSize < listSize+patternListSize)      mouseScroll++;
        if (ticks>= 100 && mouseScroll>0)                                        mouseScroll--;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) {
        if (pattern!=null) {
            pattern.mouseClicked(mouseX-guiLeft, mouseY-guiTop, mouseButton);
        }

        // mouseXY are screen coords here!
        //System.out.println("mouseY="+mouseY+", guiTop="+guiTop+", mouseX="+mouseX+", xOffset+guiLeft="+(xOffset+container.guiLeft()));
        if (mouseY>0 && mouseY<20 && mouseX>xOffset+containerLeft && mouseX<xOffset+containerLeft+textBoxSize) {
            if (mouseX<xOffset+containerLeft+20)
                scrollBy(-100);
            else if (mouseX>xOffset+containerLeft+textBoxSize-20)
                scrollBy(100);
            return;
        }
        
        // we assume the mouse is clicked where it was when we updated the screen last ...
        if (underMouse==null)
            return;

        if (underMouse.getType() == BrewingRecipe.recipeType) {
            // this is so different from other containers, we handle it now and return
            fillBrewingStandSlots((BrewingRecipe)underMouse);
            return;
        }

//        do {
            // Do nothing if the grid isn't empty.
//            boolean empty = true;
            for (int craftslot=0; craftslot<gridSize*gridSize; craftslot++) {
                ItemStack stack = screen.getScreenHandler().getSlot(craftslot+firstCraftSlot).getStack();
                if (stack!=null && !stack.isEmpty()) {
                    return;
//                    LOGGER.info("returning as slot "+craftslot+" has "+stack.getCount()+" of "+stack.getTranslationKey());
//                    empty = false;
                }
            }
//            if (!empty) return;

            if (underMouse instanceof RepairRecipe) {
                fillCraftSlotsWithBestRepair((RepairRecipe) underMouse);
            } else {
                fillCraftSlotsWithAnyMaterials(underMouse);
            }
            if (underMouse.getType() == RecipeType.STONECUTTING) {
                StonecutterScreenHandler container = (StonecutterScreenHandler) screen.getScreenHandler();
                List<StonecuttingRecipe> recipes = container.getAvailableRecipes();
                int index = recipes.indexOf(underMouse);
                if (index >= 0) {
                    container.onButtonClick(null, index);
                    MinecraftClient.getInstance().interactionManager.clickButton(container.syncId, index);
                }
            }

//            LOGGER.info("Item in result slot is "+screen.getContainer().getSlot(resultSlotNo).getStack().getItem().getName().asString());


            if (mouseButton==0) {
                slotClick(resultSlotNo, mouseButton, SlotActionType.QUICK_MOVE);     // which is really PICKUP ALL
                recipeUpdateTime=System.currentTimeMillis()+ConfigurationHandler.getAutoUpdateRecipeTimer()*1000;
            }
//            LOGGER.info("mousebutton = "+mouseButton);
//            LOGGER.info("hasControl = "+Screen.hasControlDown());
//            LOGGER.info("hasShift = "+Screen.hasShiftDown());
//            LOGGER.info("canCraft = "+canCraftRecipe(underMouse, screen.getContainer(), gridSize));
//        } while (mouseButton==0 && Screen.hasControlDown() && Screen.hasShiftDown() && canCraftRecipe(underMouse, screen.getContainer(), gridSize));
    }
    
    private void fillCraftSlotsWithAnyMaterials(Recipe underMouse) {
        DefaultedList<Ingredient> recipeInput=getIngredientsAsList(underMouse);
        
        int maxCraftableStacks=64;
        if (Screen.hasShiftDown()) {
            // Try to find out how much we can craft at once, maximally. This is limited by a) the number of
            // items per stack (for example, dispensers need bows that stack to 1, so we can't craft more than 1
            // dispenser at a time), and b) the number of items we have divided by the number of input slots that
            // need this item (a sea lantern has 5 shard input slots, so if we have 64 shards, we can't craft
            // more than floor(64/5)=12 lanters)

            // this assumes a recipe never needs more than one item in a single input slot (unless more than 1 output item)
            HashMap<String,InputCount> inputCount=new HashMap<>();
            for (Ingredient ingr:recipeInput) {
                ItemStack[] stacks = ingr.getMatchingStacksClient();
                if (stacks.length==0)
                    continue;
                if (stacks[0].getMaxCount()<maxCraftableStacks)                 // limit type a
                    maxCraftableStacks=stacks[0].getMaxCount();
                String descriptor=stacks[0].getName()+":"+stacks[0].getDamage();
                if (inputCount.containsKey(descriptor)) {
                    InputCount previous = inputCount.get(descriptor);
                    previous.count++;
                } else {
                    int totalInInv=0;
                    for (int slot=0; slot<36; slot++) {
                        Slot invitem=screen.getScreenHandler().getSlot(slot+firstInventorySlotNo);
                        ItemStack slotcontent=invitem.getStack();
                        if (canActAsIngredient(ingr, slotcontent))
                            totalInInv+=slotcontent.getCount();
                    }
                    InputCount current=new InputCount();
                    current.count=1;
                    current.items=totalInInv;
                    inputCount.put(descriptor, current);
                }
            }
            for (String descriptor:inputCount.keySet()) {
                InputCount x=inputCount.get(descriptor);
                //System.out.println(descriptor+": need "+x.count+" times, have "+x.items+" items");
                if ((x.items/x.count)<maxCraftableStacks)
                    maxCraftableStacks=x.items/x.count;                         // limit type b
            }
            
            // There is still a problem, ignored right now. We might have alternatives for 
            // one slot, and several matching items, but can't cram all of them into the slot.
            // For example, when doing a tripwire hook while we have 20 iron, 30 sticks,
            // 10 oak planks, and 5 birch planks, maxCraftableStacks will be 15 here,
            // but should be 10, AND we should remember to use oak, not birch.
        } else {
            maxCraftableStacks=1;
        }

        int rowadjust=0;
        for (int craftslot=0; craftslot<recipeInput.size(); craftslot++) {
            int remaining=maxCraftableStacks;
            Ingredient ingr=recipeInput.get(craftslot);
            for (int slot=0; remaining>0 && slot<36; slot++) {
                Slot invitem=screen.getScreenHandler().getSlot(slot+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(ingr, slotcontent)) {
                    // System.out.println("craftslot is "+craftslot+", first is "+firstCraftSlot+", rowadjust is "+rowadjust+", transferring "+remaining+" items");
                    // TODO: && (isempty(craftslot) || ismergeable(slot,craftslot))
                    transfer(slot+firstInventorySlotNo, craftslot+firstCraftSlot+rowadjust, remaining);
                    remaining=maxCraftableStacks-screen.getScreenHandler().getSlot(craftslot+firstCraftSlot+rowadjust).getStack().getCount();
                }
            }
            if (underMouse instanceof ShapedRecipe && ((craftslot+1)%((ShapedRecipe)underMouse).getWidth())==0) {
                rowadjust+=gridSize-((ShapedRecipe)underMouse).getWidth();
            }
        }
    }

    private void fillCraftSlotsWithBestRepair(RepairRecipe repairRecipe) {
        
        // New algorithm: acutally, combining the best item with the worst item
        // is almost always right as it maximizes the 10% bonus from good items

        int bestItemSlot=-1, worstItemSlot=-1;
        for (int slot=0; slot<36; slot++) {
            Slot invitem=screen.getScreenHandler().getSlot(slot+firstInventorySlotNo);
            ItemStack slotcontent=invitem.getStack();
            if (slotcontent.getItem() == repairRecipe.getItem()
            &&  slotcontent.getDamage()>0
            &&  slotcontent.getEnchantments().size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()
            ) {
                if (bestItemSlot==-1)
                    bestItemSlot=worstItemSlot=slot;
                else if (getDamage(bestItemSlot) > slotcontent.getDamage())
                    bestItemSlot=slot;
                else if (getDamage(worstItemSlot) < slotcontent.getDamage())
                    worstItemSlot=slot;
            }
        }
        if (bestItemSlot==-1 || worstItemSlot == -1 || worstItemSlot == bestItemSlot) {
            return;
        }

        transfer(bestItemSlot+firstInventorySlotNo, firstCraftSlot, 1);
        transfer(worstItemSlot+firstInventorySlotNo, firstCraftSlot+1, 1);
    }

    private void fillBrewingStandSlots(BrewingRecipe recipe) {
        ScreenHandler container = screen.getScreenHandler();
        ItemStack ingredientStack = container.getSlot(3+firstCraftSlot).getStack();
        List<Ingredient> inputs=recipe.getPreviewInputs();
        ItemStack inputPotionStack = inputs.get(0).getMatchingStacksClient()[0];
        if (ingredientStack.isEmpty()) {
            Item neededItem = ((Ingredient)(recipe.getPreviewInputs().get(1))).getMatchingStacksClient()[0].getItem();
            for (int slot=0; slot<36; slot++) {
                if (container.getSlot(slot+firstInventorySlotNo).getStack().getItem() == neededItem) {
                    LOGGER.debug("transfer from inv slot "+slot+" to ingred. slot "+3);
                    transfer(slot+firstInventorySlotNo, 3+firstCraftSlot, 1);
                    break;
                }
            }
        } else if (ingredientStack.getItem() != recipe.getIngredient().getItem()) {
            MinecraftClient.getInstance().inGameHud.setOverlayMessage(new LiteralText("Remove the wrong ingredient from the brewing stand first"), true);
            return;
        }
        for (int potionSlot=0; potionSlot<3; potionSlot++) {
            if (!container.getSlot(potionSlot+firstCraftSlot).getStack().isEmpty()) {
                continue;
            }
            for (int slot=0; slot<36; slot++) {
                ItemStack inventoryItemStack = container.getSlot(slot+firstInventorySlotNo).getStack();
                if (inventoryItemStack.isEmpty())
                    continue;
                boolean matches = (PotionUtil.getPotion(inventoryItemStack) == PotionUtil.getPotion(inputPotionStack));
//                if (recipe.isItemRecipe()) {
                    matches &= (inventoryItemStack.getItem() == inputPotionStack.getItem());
//                }
                if (matches) {
                    LOGGER.debug("transfer from inv slot "+slot+" to potion slot "+potionSlot);
                    transfer(slot+firstInventorySlotNo, potionSlot+firstCraftSlot, 1);
                    break;
                }
            }
        }
    }
    
    private int getDamage(int slot) {
        ItemStack stack = screen.getScreenHandler().getSlot(slot+firstInventorySlotNo).getStack();
        return stack.getDamage();
    }

    private int getRemainingDurability(int slot) {
        ItemStack stack = screen.getScreenHandler().getSlot(slot+firstInventorySlotNo).getStack();
        int remainingDurability = stack.getMaxDamage() - stack.getDamage();
        return remainingDurability;
    }

    public boolean keyPressed(int code, int scancode, int modifiers) {
        if (pattern==null)
            return false;
        // System.out.println("key code="+code+", scancode="+scancode+", modifiers="+modifiers);
        if (code==GLFW.GLFW_KEY_ENTER || code==GLFW.GLFW_KEY_KP_ENTER) {
            updatePatternMatch();
            pattern.changeFocus(false);
            return true;
        } else if (pattern.isFocused()) {
            // System.out.println("-> sending to pattern");
            pattern.keyPressed(code, scancode, modifiers);
            return true;            // prevent 'e' from closing screen
        } else {
            return false;
        }
    }

    public boolean charTyped(char codepoint, int modifiers) {
        // System.out.println("char code="+codepoint+", modifiers="+modifiers);
        if (pattern!=null && pattern.isFocused())
            return pattern.charTyped(codepoint, modifiers);
        return false;
    }
    
    private DefaultedList<Ingredient> getIngredientsAsList(Recipe recipe) {
        return recipe.getPreviewInputs();
    }
    
    private boolean canActAsIngredient(Ingredient recipeComponent, ItemStack inventoryItem) {

        boolean tagForbidsItem = false;

        if (inventoryItem.hasTag()) {
            CompoundTag tag = inventoryItem.getTag();
            for (String tagName: tag.getKeys()) {
                if (!(tagName.equals("Damage")) || tag.getInt(tagName) != 0) {
                    tagForbidsItem = true;
                }
            }
        }

        if (!tagForbidsItem && recipeComponent.test(inventoryItem))
            return true;

        // TagDump.dump(inventoryItem.getTag(), 0);

        if (inventoryItem.getItem()!=Items.LINGERING_POTION)
            return false;

        Potion neededType = PotionUtil.getPotion(inventoryItem);
        ItemStack[] possiblePotions = recipeComponent.getMatchingStacksClient();
        for (ItemStack stack: possiblePotions) {
            // LOGGER.info("in lingering potion check, component = "+recipeComponent.getMatchingStacksClient()[0].getTranslationKey()+", invItem = "+inventoryItem.getTranslationKey());
            if (PotionUtil.getPotion(stack) == neededType && recipeComponent.test(inventoryItem)) {
                return true;
            }
        }
        return false;
    }
    
    public void transfer(int from, int to, int amount) {
        Slot fromSlot=screen.getScreenHandler().getSlot(from);
        ItemStack fromContent=fromSlot.getStack();
        
        //System.out.println("Trying to transfer "+amount+" "+fromContent.getDisplayName()+" from slot "+from+" to "+to);
        // want as much as we have, or more? Transfer all there is
        if (amount >= fromSlot.getStack().getCount()) {
            slotClick(from, 0, SlotActionType.PICKUP);
            slotClick(to, 0, SlotActionType.PICKUP);
            return;
        }

        // Transfer as many half stacks as possible. If there is an odd number
        // of items in the source slot, right clicking will round the source slot
        // down, and the hand up, so we transfer (n+1)/2 items.
        int transfer;
        while (amount>=(transfer=((fromSlot.getStack().getCount()+1)/2))) {
            slotClick(from, 1, SlotActionType.PICKUP);       // right click to get half the source
            slotClick(to, 0, SlotActionType.PICKUP);
            amount-=transfer;
            //System.out.println("transferred "+transfer+", amount is now "+amount);
        }
        
        if (amount>0) {
            int prevCount=fromContent.getCount();
            slotClick(from, 0, SlotActionType.PICKUP);       // left click source
            for (int i=0; i<amount; i++)
                slotClick(to, 1, SlotActionType.PICKUP);         // right click target to deposit 1 item
            if (prevCount>amount)
                slotClick(from, 0, SlotActionType.PICKUP);       // left click source again to put stuff back
        }
    }
    
    public void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        ((SlotClickAccepter)screen).slotClick(slot, mouseButton, clickType);
    }
}
