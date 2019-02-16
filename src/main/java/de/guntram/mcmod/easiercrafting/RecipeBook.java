package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class RecipeBook {
    private final GuiContainer container;
    private final int firstCraftSlot;
    private final int gridSize;
    private final int resultSlotNo;
    private final int firstInventorySlotNo;
    TreeMap<String, TreeSet<IRecipe>> craftableCategories;
    private IRecipe underMouse;
    
    private final int itemSize=20;
    private final int itemLift=5;         // how many pixels to display items above where they would be normally

    private int listSize;
    private int itemsPerRow;                // # of items per row. Normally 8.
    private int xOffset;                    // offset of list to standard gui. itemSize*itemsPerRow+10.
    private int mouseScroll;
    private boolean mouseScrollEnabled;
    private int minYtoDraw=0;               // implements clipping top part of the item list
    private int textBoxSize;
    private long recipeUpdateTime;
    
    private GuiTextField pattern;
    private TreeSet<IRecipe> patternMatchingRecipes;
    private int patternListSize;
    
    static ResourceLocation arrows;
    
/**
 * 
 * @param craftinv          The container the recipe book is attached to - this
 *      can be a GuiCrafting or a GuiInventory container
 * @param firstCraftSlot    The slot number of the first slot that is a craft
 *      slot in craftinv
 * @param gridsize          2 (for inventory) or 3 (for workbench)
 * @param resultSlot        the slot number of the craft result slot
 * @param firstInventorySlot
 *                          the slot number of the first inventory slot
 */    
    public RecipeBook(GuiContainer craftinv, int firstCraftSlot, int gridsize, int resultSlot, int firstInventorySlot) {
        this.container=craftinv;
        this.firstCraftSlot=firstCraftSlot;
        this.gridSize=gridsize;
        this.resultSlotNo=resultSlot;
        // firstInventorySlot is not neccesarily number of slots minus 36 -- 
        // player inventory has 9-44; 45 is offhand
        this.firstInventorySlotNo=firstInventorySlot;
        this.pattern=null;
        this.underMouse=null;
        
        if (arrows==null) {
            arrows=new ResourceLocation(EasierCrafting.MODID, "textures/arrows.png");
            SimpleTexture x=new SimpleTexture(arrows);
            boolean flag = Minecraft.getInstance().getTextureManager().loadTexture(arrows, x);
            //System.out.println("loading "+arrows.toString()+": "+flag);
        }
    }
    
    void afterInitGui() {
        int tempItemsPerRow = 8;
        int tempXOffset=-itemSize*tempItemsPerRow -10;
        if (tempXOffset+container.guiLeft < 0) {
            tempItemsPerRow=(container.guiLeft-10)/itemSize;
            tempXOffset=-itemSize*tempItemsPerRow-10;
        }
        textBoxSize=-tempXOffset-15;
        if (ConfigurationHandler.getShowGuiRight())
            tempXOffset=container.xSize+10;
        if (tempItemsPerRow < 2) {
            System.out.println("forcing tempItemsPerRow to 2 when it's "+tempItemsPerRow);
            tempItemsPerRow = 2;
        }
        this.itemsPerRow=tempItemsPerRow;
        this.xOffset=tempXOffset;
        this.mouseScroll=0;
        //System.out.println("left="+container.guiLeft()+", items="+itemsPerRow+", offset="+tempXOffset+", textbox="+textBoxSize);
        updatePatternMatch();
        updateRecipes();
    }
    
    // left is the X position we want to draw at, Y is (normally) 0.
    // However, if our height is larger than the GUI container height,
    // adjust our Y position accordingly.
    void drawRecipeList(FontRenderer fontRenderer, ItemRenderer itemRenderer,
            int left, int height, int mouseX, int mouseY) {
        // We can't do this in the constructor as we don't yet know sizes from initGui.
        // Also, not in afterInitGui() because we don't know fontRender there.
        if (pattern==null) {
            //System.out.println("drawRecipeList creating textbox");
            //Collection<IRecipe> x = Minecraft.getInstance().player.world.getRecipeManager().getRecipes();
            //System.out.println("found "+x.size()+" recipes");
            
            pattern=new GuiTextField(1, fontRenderer, xOffset, 0, textBoxSize, 20);
            pattern.setFocused(ConfigurationHandler.getAutoFocusSearch());
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
            if (ypos < -container.guiTop) {
                ypos = -container.guiTop;
                //System.out.println("mouse wheel text at"+ypos);
                // fontRenderer.drawString(I18n.format("message.usemouse"), xOffset+itemSize, ypos, 0xff0000);
                Minecraft.getInstance().getTextureManager().bindTexture(arrows);
                container.drawTexturedModalRect(xOffset,                ypos,  0, 0, 20, 20);
                container.drawTexturedModalRect(xOffset+textBoxSize-20, ypos, 20, 0, 20, 20);
                mouseScrollEnabled=true;
                ypos+=itemSize;
            } else {
                mouseScrollEnabled=false;
                mouseScroll=0;
            }
        } else {
            mouseScrollEnabled=false;
            mouseScroll=0;
        }
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();

        underMouse=null;

        pattern.y=ypos;
        pattern.drawTextField(0, 0, 0f);    // <-- parameters neccessary but unused 
        ypos+=itemSize*3/2;
        minYtoDraw=ypos;
        ypos-=mouseScroll*itemSize;
        ypos=drawRecipeOutputs(patternMatchingRecipes, itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        if (underMouse!=null)
            underMouseIsCraftable=false;
        
        for (String category: craftableCategories.keySet()) {
            if (ypos>=minYtoDraw)
                fontRenderer.drawString(category, xOffset, ypos, 0xffff00);
            ypos+=itemSize;
            ypos=drawRecipeOutputs(craftableCategories.get(category), itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        }
        if (underMouse!=null) {
            fontRenderer.drawString(underMouse.getRecipeOutput().getDisplayName().getFormattedText(), 0, height+3, 0xffff00);
            if (underMouse instanceof ShapedRecipe) {
                NonNullList<Ingredient> ingredients = underMouse.getIngredients();
                fontRenderer.drawString("sr", left-20, height, 0x202020);
                for (int x=0; x<((ShapedRecipe)underMouse).getWidth(); x++) {
                    for (int y=0; y<((ShapedRecipe)underMouse).getHeight(); y++) {
                        renderIngredient(itemRenderer, fontRenderer, 
                                ingredients.get(x+y*((ShapedRecipe)underMouse).getWidth()), itemSize*x, height+itemSize+itemSize*y);                        
                    }
                }
            } else if (underMouse instanceof ShapelessRecipe) {
                fontRenderer.drawString("slr", left-20, height, 0x202020);
                xpos=0;
                for (Ingredient ingredient: ((ShapelessRecipe)underMouse).getIngredients()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
/* Omit Forge extension that's probably not in forge for 1.13 anyway 
            } else if (underMouse instanceof ShapedOreRecipe) {
                fontRenderer.drawString("sor", left-20, height, 0x202020);
                NonNullList<Ingredient> ingredients = underMouse.getIngredients();
                for (int x=0; x<((ShapedOreRecipe)underMouse).getWidth(); x++) {
                    for (int y=0; y<((ShapedOreRecipe)underMouse).getHeight(); y++) {
                        renderIngredient(itemRenderer, fontRenderer, ingredients.get(x+y*((ShapedOreRecipe)underMouse).getWidth()), itemSize*x, height+itemSize+itemSize*y);
                    }
                }
            } else if (underMouse instanceof ShapelessOreRecipe) {
                fontRenderer.drawString("slor", left-20, height, 0x202020);
                xpos=0;
                for (Ingredient ingredient: ((ShapelessOreRecipe)underMouse).getIngredients()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
*/
            }
        }
     
        RenderHelper.disableStandardItemLighting();
        if (!underMouseIsCraftable)
            underMouse=null;
    }

    private int drawRecipeOutputs(Iterable<IRecipe>recipes, 
            ItemRenderer itemRenderer, FontRenderer fontRenderer, 
            int xpos, int ypos,
            int mouseX, int mouseY) {

        for (IRecipe recipe: recipes) {
            ItemStack items=recipe.getRecipeOutput();
            if (ypos>=minYtoDraw) {
                itemRenderer.renderItemAndEffectIntoGUI(items, xOffset+xpos, ypos-itemLift);
                itemRenderer.renderItemOverlays(fontRenderer, items, xOffset+xpos, ypos-itemLift);
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
    
    public void renderIngredient(ItemRenderer itemRenderer, FontRenderer fontRenderer, Ingredient ingredient, int x, int y) {
        ItemStack[] stacks=ingredient.getMatchingStacks();
        if (stacks.length==0)
            return;
        int toRender=0;
        if (stacks.length>1)
            toRender=(int) ((System.currentTimeMillis()/333)%stacks.length);
        itemRenderer.renderItemAndEffectIntoGUI(stacks[toRender], x, y);
        itemRenderer.renderItemOverlays(fontRenderer, stacks[toRender], x, y);
    }
    
    public final void updateRecipes() {
        Container inventory=container.inventorySlots;
        List<IRecipe> recipes = new ArrayList<>();
//        for (Iterator<IRecipe> i= CraftingManager.REGISTRY.iterator(); i.hasNext();)
//            recipes.add(i.next());
        recipes.addAll(Minecraft.getInstance().player.world.getRecipeManager().getRecipes());
        if (ConfigurationHandler.getAllowGeneratedRecipes()) {
            recipes.addAll(InventoryRecipeScanner.findUnusualRecipes(inventory, firstInventorySlotNo));
        }

        craftableCategories=new TreeMap();
        for (IRecipe recipe:recipes) {
            if (!canCraftRecipe(recipe, inventory, gridSize))
                continue;
            //System.out.println("grid size is "+gridSize+", recipe needs "+recipe.getRecipeSize());
            ItemStack result=recipe.getRecipeOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            ItemGroup tab = item.getGroup();
            String category;
            if (tab==null) {
/*                if (item==Items.FIREWORKS)
                    category = "Fireworks";
else */
                    category="(none?)";
            }
            else
                category=I18n.format(tab.getTabLabel(), new Object[0]);
            TreeSet<IRecipe> catRecipes=craftableCategories.get(category);
            if (catRecipes==null) {
                catRecipes=new TreeSet<>(new Comparator<IRecipe>() {
                    @Override
                    public int compare(IRecipe a, IRecipe b) {
                        return a.getRecipeOutput().getDisplayName().getUnformattedComponentText().
                            compareToIgnoreCase(b.getRecipeOutput().getDisplayName().getUnformattedComponentText());
                    }
                }
                );
                craftableCategories.put(category, catRecipes);
            }
            //System.out.println("adding "+result.getDisplayName()+" in "+category);
            catRecipes.add(recipe);
        }
        listSize=craftableCategories.size();
        for (TreeSet<IRecipe> tree: craftableCategories.values())
            listSize+=((tree.size()+(itemsPerRow-1))/itemsPerRow);
        listSize*=itemSize;
        mouseScroll=0;
    }
    
    public final void updatePatternMatch() {
        
        patternListSize=0;
        patternMatchingRecipes=new TreeSet<>(new Comparator<IRecipe>() {
            @Override
            public int compare(IRecipe a, IRecipe b) {
                return a.getRecipeOutput().getDisplayName().getUnformattedComponentText().
                        compareToIgnoreCase(b.getRecipeOutput().getDisplayName().getUnformattedComponentText());
            }
        });

        if (pattern==null)          // constructor run but no gui opened yet
            return;
        String patternText=pattern.getText();
        if (patternText.length()<2)
            return;

        Container inventory=container.inventorySlots;
        List<IRecipe> recipes = new ArrayList<>();
        recipes.addAll(Minecraft.getInstance().player.world.getRecipeManager().getRecipes());
//        for (Iterator<IRecipe> i= CraftingManager.REGISTRY.iterator(); i.hasNext();)
//            recipes.add(i.next());
        Pattern regex=Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        for (IRecipe recipe:recipes) {
            ItemStack result=recipe.getRecipeOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            if (!regex.matcher(result.getDisplayName().getUnformattedComponentText()).find()) {
                //System.out.println("not adding "+result.getDisplayName()+" because no match");
                continue;
            }
            //System.out.println("adding "+result.getDisplayName()+" to pattern match "+patternText);
            patternMatchingRecipes.add(recipe);
        }
        patternListSize=((patternMatchingRecipes.size()+(itemsPerRow-1))/itemsPerRow)*itemSize;
        mouseScroll=0;
    }

    class Takefrom { 
        Slot invitem; int amount; 
        Takefrom(Slot i, int n) { invitem=i; amount=n; }
    };

    private boolean canCraftRecipe(IRecipe recipe, Container inventory, int gridSize) {
        if (recipe instanceof ShapelessRecipe) {
            return canCraftShapeless((ShapelessRecipe) recipe, inventory);
        } else if (recipe instanceof ShapedRecipe) {
            return canCraftShaped((ShapedRecipe) recipe, inventory, gridSize);
/* forge <= 1.12 only ?
        } else if (recipe instanceof ShapedOreRecipe) {
            return canCraftShapedOre((ShapedOreRecipe) recipe, inventory, gridSize);
        } else if (recipe instanceof ShapelessOreRecipe) {
            return canCraftShapelessOre((ShapelessOreRecipe) recipe, inventory, gridSize);
*/
        } else if (recipe instanceof InventoryGeneratedRecipe || recipe instanceof RepairRecipe) {
            // We just generated the recipe so we should be able to craft it,
            // but make sure the grid is big enough
            return recipe.canFit(gridSize, gridSize);
            // return canCraft(recipe, recipe.getIngredients(), inventory);
        } else {
            //System.out.println(recipe.getRecipeOutput().getDisplayName()+" is a "+recipe.getClass().getCanonicalName());
        }
        return false;
    }
    
    private boolean canCraftShapeless(ShapelessRecipe recipe, Container inventory) {
        NonNullList<Ingredient> neededList = recipe.getIngredients();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShaped(ShapedRecipe recipe, Container inventory, int gridSize) {
        if (recipe.getWidth()>gridSize || recipe.getHeight()>gridSize) {
            //System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.recipeWidth+"/"+recipe.recipeHeight);
            return false;
        }
        NonNullList<Ingredient> neededList = recipe.getIngredients();
        return canCraft(recipe, neededList, inventory);
    }

/* forge <= 1.12    
    private boolean canCraftShapedOre(ShapedOreRecipe recipe, Container inventory, int gridSize) {
        if (recipe.getWidth()>gridSize || recipe.getHeight()>gridSize) {
            //System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.getWidth()+"/"+recipe.getHeight());
            return false;
        }
        return canCraftOre(recipe, recipe.getIngredients(), inventory);
    }

    private boolean canCraftShapelessOre(ShapelessOreRecipe recipe, Container inventory, int gridSize) {
        return canCraftOre(recipe, recipe.getIngredients(), inventory);
    }
*/
    
    private boolean canCraftOre(IRecipe recipe, NonNullList<Ingredient>input, Container inventory) {
        return canCraft(recipe, input, inventory);
    }

    private boolean canCraft(IRecipe recipe, List<Ingredient> neededList, Container inventory) {
        ArrayList<Takefrom> source=new ArrayList<>(neededList.size());
        for (Ingredient neededItem: neededList) {                                // iterate over needed items
            ItemStack[] stacks=neededItem.getMatchingStacks();
            if (stacks.length==0)
                continue;
            int neededAmount=stacks[0].getCount();
            // System.out.println("need "+neededAmount+" "+stacks[0].getDisplayName()+" for "+recipe.getRecipeOutput().getDisplayName());
            for (int i=0; i<36; i++) {
                Slot invitem=inventory.getSlot(i+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(neededItem, slotcontent)) {
                    int providedAmount=slotcontent.getCount();                  // check how many items there are
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
            }
            if (neededAmount>0) {                                               // we don't have enough of this item so we can't craft this
                //System.out.println("can't craft "+recipe.getRecipeOutput().getDisplayName()+" because we don't have "+neededItem.getCount()+" "+neededItem.getDisplayName());
                return false;
            }
        }
        //System.out.println("enough stuff for "+recipe.getRecipeOutput().getDisplayName());
        return true;
    }
    
    class InputCount {
        int count;
        int items;
    }
    
    public void scrollBy(int ticks) {
        int old=mouseScroll;
        if (ticks<=-100 && mouseScroll*itemSize < listSize+patternListSize)      mouseScroll++;
        if (ticks>= 100 && mouseScroll>0)                                        mouseScroll--;
//        if (mouseScroll!=old)
//            System.out.println("mouseScroll is now "+mouseScroll+" vs height "+(listSize+patternListSize));
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) {
        if (pattern!=null) {
            pattern.mouseClicked(mouseX-guiLeft, mouseY-guiTop, mouseButton);
        }

        // mouseXY are screen coords here!
        //System.out.println("mouseY="+mouseY+", guiTop="+guiTop+", mouseX="+mouseX+", xOffset+guiLeft="+(xOffset+container.guiLeft()));
        if (mouseY>0 && mouseY<20 && mouseX>xOffset+container.guiLeft && mouseX<xOffset+container.guiLeft+textBoxSize) {
            if (mouseX<xOffset+container.guiLeft+20)
                scrollBy(-100);
            else if (mouseX>xOffset+container.guiLeft+textBoxSize-20)
                scrollBy(100);
            return;
        }
        
        // we assume the mouse is clicked where it was when we updated the screen last ...
        if (underMouse==null)
            return;

        // Do nothing if the grid isn't empty.
        for (int craftslot=0; craftslot<gridSize*gridSize; craftslot++) {
            ItemStack stack = container.inventorySlots.getSlot(craftslot+firstCraftSlot).getStack();
            if (stack!=null && !stack.isEmpty())
                return;
        }
        
        if (underMouse instanceof RepairRecipe) {
            fillCraftSlotsWithBestRepair((RepairRecipe)underMouse);
        } else {
            fillCraftSlotsWithAnyMaterials(underMouse);
        }

        
        if (mouseButton==0) {
            slotClick(resultSlotNo, mouseButton, ClickType.QUICK_MOVE);     // which is really PICKUP ALL
            recipeUpdateTime=System.currentTimeMillis()+ConfigurationHandler.getAutoUpdateRecipeTimer()*1000;
        }
    }
    
    private void fillCraftSlotsWithAnyMaterials(IRecipe underMouse) {
        NonNullList<Ingredient> recipeInput=getIngredientsAsList(underMouse);
        
        int maxCraftableStacks=64;
        if (isShiftKeyDown()) {
            // Try to find out how much we can craft at once, maximally. This is limited by a) the number of
            // items per stack (for example, dispensers need bows that stack to 1, so we can't craft more than 1
            // dispenser at a time), and b) the number of items we have divided by the number of input slots that
            // need this item (a sea lantern has 5 shard input slots, so if we have 64 shards, we can't craft
            // more than floor(64/5)=12 lanters)

            // this assumes a recipe never needs more than one item in a single input slot (unless more than 1 output item)
            HashMap<String,InputCount> inputCount=new HashMap<>();
            for (Ingredient ingr:recipeInput) {
                ItemStack[] stacks = ingr.getMatchingStacks();
                if (stacks.length==0)
                    continue;
                if (stacks[0].getMaxStackSize()<maxCraftableStacks)                 // limit type a
                    maxCraftableStacks=stacks[0].getMaxStackSize();
                String descriptor=stacks[0].getDisplayName()+":"+stacks[0].getDamage();
                if (inputCount.containsKey(descriptor)) {
                    InputCount previous = inputCount.get(descriptor);
                    previous.count++;
                } else {
                    int totalInInv=0;
                    for (int slot=0; slot<36; slot++) {
                        Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
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
                Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(ingr, slotcontent)) {
                    // System.out.println("craftslot is "+craftslot+", first is "+firstCraftSlot+", rowadjust is "+rowadjust+", transferring "+remaining+" items");
                    // TODO: && (isempty(craftslot) || ismergeable(slot,craftslot))
                    transfer(slot+firstInventorySlotNo, craftslot+firstCraftSlot+rowadjust, remaining);
                    remaining=maxCraftableStacks-container.inventorySlots.getSlot(craftslot+firstCraftSlot+rowadjust).getStack().getCount();
                }
            }
            if (underMouse instanceof ShapedRecipe && ((craftslot+1)%((ShapedRecipe)underMouse).getWidth())==0) {
                rowadjust+=gridSize-((ShapedRecipe)underMouse).getWidth();
            }
        }
    }
    
    private void fillCraftSlotsWithBestRepair(RepairRecipe repairRecipe) {
        // Search for item that has least damage. Put in first slot.
        // Search for second item that
        //   a) repairs first item fully, taking the one that is damaged most
        //   b) does not repair first item fully but adds as much as possible
        int firstSlot=-1, secondSlot=-1;
        for (int slot=0; slot<36; slot++) {
            Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
            ItemStack slotcontent=invitem.getStack();
            if (slotcontent.getItem() == repairRecipe.getItem()
            &&  slotcontent.getDamage()>0
            &&  slotcontent.getEnchantmentTagList().size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()
            ) {
                if (firstSlot==-1)
                    firstSlot=slot;
                else if (container.inventorySlots.getSlot(firstSlot+firstInventorySlotNo).getStack().getDamage() > slotcontent.getDamage())
                    firstSlot=slot;
            }
        }
        if (firstSlot==-1) {
            return;
        }
        int neededRepair=container.inventorySlots.getSlot(firstSlot+firstInventorySlotNo).getStack().getDamage();
        transfer(firstSlot+firstInventorySlotNo, firstCraftSlot, 1);

        for (int slot=0; slot<36; slot++) {
            if (slot==firstSlot)
                continue;
            Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
            ItemStack slotcontent=invitem.getStack();
            if (slotcontent.getItem() == repairRecipe.getItem()
            &&  slotcontent.getDamage()>0
            &&  slotcontent.getEnchantmentTagList().size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()
            ) {
                if (secondSlot==-1)
                    secondSlot=slot;
                else {
                    ItemStack currentRepairStack = container.inventorySlots.getSlot(secondSlot+firstInventorySlotNo).getStack();
                    int currentRepairValue=currentRepairStack.getMaxDamage()-currentRepairStack.getDamage();
                    ItemStack testedRepairStack = container.inventorySlots.getSlot(slot+firstInventorySlotNo).getStack();
                    int testedRepairValue=testedRepairStack.getMaxDamage()-testedRepairStack.getDamage();

                    if (currentRepairValue > neededRepair           // Does the item we remember repair this
                    &&  testedRepairValue > neededRepair            // And does the current item repair as well
                    &&  testedRepairValue < currentRepairValue) {   // but the current item is more damaged so we sacrifice less
                        secondSlot=slot;
                    }
                    else if (testedRepairValue > currentRepairValue) {  // will this item repair more than the remembered one?
                        secondSlot=slot;
                    }
                }
            }
        }
        if (secondSlot==-1) {
            return;
        }
        transfer(secondSlot+firstInventorySlotNo, firstCraftSlot+1, 1);
    }
    
    public boolean keyPressed(int code, int scancode, int modifiers) {
        // System.out.println("key code="+code+", scancode="+scancode+", modifiers="+modifiers);
        if (code==GLFW.GLFW_KEY_ENTER || code==GLFW.GLFW_KEY_KP_ENTER) {
            updatePatternMatch();
            pattern.setFocused(false);
            return true;
        } else if (pattern.isFocused()) {
            // System.out.println("-> sending to pattern");
            return pattern.keyPressed(code, scancode, modifiers);
        } else {
            return false;
        }
    }

    boolean charTyped(char codepoint, int modifiers) {
        // System.out.println("char code="+codepoint+", modifiers="+modifiers);
        if (pattern!=null && pattern.isFocused())
            return pattern.charTyped(codepoint, modifiers);
        return false;
    }
    
    private NonNullList<Ingredient> getIngredientsAsList(IRecipe recipe) {
        // Wow. This was so much harder before 1.12.
        return recipe.getIngredients();
    }
    
    private boolean canActAsIngredient(Ingredient recipeComponent, ItemStack inventoryItem) {
        if (!inventoryItem.hasTag() && recipeComponent.test(inventoryItem))
            return true;
        
        if (inventoryItem.getItem()!=Items.LINGERING_POTION)
            return false;
        
        PotionType neededType = PotionUtils.getPotionFromItem(inventoryItem);
        ItemStack[] possiblePotions = recipeComponent.getMatchingStacks();
        for (ItemStack stack: possiblePotions) {
            if (PotionUtils.getPotionFromItem(stack) == neededType)
                return true;
        }
        return false;
    }
    
    private void transfer(int from, int to, int amount) {
        Slot fromSlot=container.inventorySlots.getSlot(from);
        ItemStack fromContent=fromSlot.getStack();
        
        //System.out.println("Trying to transfer "+amount+" "+fromContent.getDisplayName()+" from slot "+from+" to "+to);
        // want as much as we have, or more? Transfer all there is
        if (amount >= fromSlot.getStack().getCount()) {
            slotClick(from, 0, ClickType.PICKUP);
            slotClick(to, 0, ClickType.PICKUP);
            return;
        }

        // Transfer as many half stacks as possible. If there is an odd number
        // of items in the source slot, right clicking will round the source slot
        // down, and the hand up, so we transfer (n+1)/2 items.
        int transfer;
        while (amount>=(transfer=((fromSlot.getStack().getCount()+1)/2))) {
            slotClick(from, 1, ClickType.PICKUP);       // right click to get half the source
            slotClick(to, 0, ClickType.PICKUP);
            amount-=transfer;
            //System.out.println("transferred "+transfer+", amount is now "+amount);
        }
        
        if (amount>0) {
            int prevCount=fromContent.getCount();
            slotClick(from, 0, ClickType.PICKUP);       // left click source
            for (int i=0; i<amount; i++)
                slotClick(to, 1, ClickType.PICKUP);         // right click target to deposit 1 item
            if (prevCount>amount)
                slotClick(from, 0, ClickType.PICKUP);       // left click source again to put stuff back
        }
    }
    
    private void slotClick(int slot, int mouseButton, ClickType clickType) {
        Minecraft mc=Minecraft.getInstance();
        // System.out.println("Clicking slot "+slot+" "+(mouseButton==0 ? "left" : "right")+" type:"+clickType.toString());
        mc.playerController.windowClick(mc.player.openContainer.windowId, slot, mouseButton, clickType, mc.player);
    }
}
