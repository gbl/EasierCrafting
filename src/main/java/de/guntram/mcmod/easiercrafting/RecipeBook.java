package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeBook {
    private GuiContainer container;
    private int firstCraftSlot;
    private int gridSize;
    private int resultSlotNo;
    private int firstInventorySlotNo;
    TreeMap<String, TreeSet<IRecipe>> craftableCategories;
    private IRecipe underMouse;
    
    private final int itemSize=20;
    private final int xOffset=-itemSize*8-10; // offset of list to standard gui
    private final int itemLift=5;               // how many pixels to display items above where they would be normally
    private int listSize;
    private long recipeUpdateTime;
    
    RecipeBook(GuiContainer craftinv, int firstCraftSlot, int gridsize, int resultSlot) {
        this.container=craftinv;
        this.firstCraftSlot=firstCraftSlot;
        this.gridSize=gridsize;
        this.resultSlotNo=resultSlot;
        this.firstInventorySlotNo=craftinv.inventorySlots.inventorySlots.size()-36;
        underMouse=null;

        updateRecipes();
    }
    
    // left is the X position we want to draw at, Y is (normally) 0.
    // However, if our height is larger than the GUI container height,
    // adjust our Y position accordingly.
    void drawRecipeList(FontRenderer fontRenderer, RenderItem itemRenderer,
            int left, int height, int mouseX, int mouseY) {
        
        if (recipeUpdateTime!=0 && System.currentTimeMillis() > recipeUpdateTime) {
            updateRecipes();
            recipeUpdateTime=0;
        }

        int xpos=0, ypos=0;
        if (listSize>height)
            ypos-=(listSize-height)/2;
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        
        underMouse=null;
        for (String category: craftableCategories.keySet()) {
            
            fontRenderer.drawString(category, xOffset, ypos, 0xffff00);
            ypos+=itemSize;
            xpos=0;
            for (IRecipe recipe: craftableCategories.get(category)) {
                ItemStack items=recipe.getRecipeOutput();
                itemRenderer.renderItemAndEffectIntoGUI(items, xOffset+xpos, ypos-itemLift);
                itemRenderer.renderItemOverlays(fontRenderer, items, xOffset+xpos, ypos-itemLift);
                if (mouseX>=xpos+xOffset  && mouseX<=xpos+xOffset+itemSize-1
                &&  mouseY>=ypos-itemLift && mouseY<=ypos-itemLift+itemSize-1) {
                    underMouse=recipe;
                }
                xpos+=itemSize;
                if (xpos>=itemSize*8) {
                    ypos+=itemSize;
                    xpos=0;
                }
            }
            if (xpos!=0)
                ypos+=itemSize;
        }
        if (underMouse!=null) {
            fontRenderer.drawString(underMouse.getRecipeOutput().getDisplayName(), 0, height+3, 0xffff00);
            if (underMouse instanceof ShapedRecipes) {
                fontRenderer.drawString("sr", left-20, height, 0x202020);
                for (int x=0; x<((ShapedRecipes)underMouse).recipeWidth; x++) {
                    for (int y=0; y<((ShapedRecipes)underMouse).recipeHeight; y++) {
                        ItemStack stack = ((ShapedRecipes)underMouse).recipeItems[x+y*((ShapedRecipes)underMouse).recipeWidth];
                        renderIngredient(itemRenderer, fontRenderer, stack, itemSize*x, height+itemSize+itemSize*y);                        
                    }
                }
            } else if (underMouse instanceof ShapelessRecipes) {
                fontRenderer.drawString("slr", left-20, height, 0x202020);
                xpos=0;
                for (ItemStack stack: ((ShapelessRecipes)underMouse).recipeItems) {
                    renderIngredient(itemRenderer, fontRenderer, stack, itemSize*xpos, height+itemSize);
                    xpos++;
                }
            } else if (underMouse instanceof ShapedOreRecipe) {
                fontRenderer.drawString("sor", left-20, height, 0x202020);
                Object[] ingredients = ((ShapedOreRecipe)underMouse).getInput();
                for (int x=0; x<((ShapedOreRecipe)underMouse).getWidth(); x++) {
                    for (int y=0; y<((ShapedOreRecipe)underMouse).getHeight(); y++) {
                        Object ingredient = ingredients[x+y*((ShapedOreRecipe)underMouse).getWidth()];
                        renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*x, height+itemSize+itemSize*y);
                    }
                }
            } else if (underMouse instanceof ShapelessOreRecipe) {
                fontRenderer.drawString("slor", left-20, height, 0x202020);
                xpos=0;
                for (Object ingredient: ((ShapelessOreRecipe)underMouse).getInput()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
    }
    
    public void renderIngredient(RenderItem itemRenderer, FontRenderer fontRenderer, Object ingredient, int x, int y) {
        ItemStack stack;
        if (ingredient instanceof ItemStack)
            stack=(ItemStack) ingredient;
        else if (ingredient instanceof NonNullList)
            stack=(((NonNullList<ItemStack>)ingredient).get(0));
        else
            return;
        if (stack.getItemDamage()==32767) {
            stack=stack.copy();
            NonNullList<ItemStack> subitems=NonNullList.<ItemStack>create();
            stack.setItemDamage(0);
            stack.getItem().getSubItems(stack.getItem(), CreativeTabs.MISC, subitems);
            if (subitems.size()>1)
                stack.setItemDamage((int) ((System.currentTimeMillis()/333)%subitems.size()));
        }
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        itemRenderer.renderItemOverlays(fontRenderer, stack, x, y);
    }
    
    public final void updateRecipes() {
        Container inventory=container.inventorySlots;
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        HashSet<String> known=new HashSet<String>();
        for (IRecipe recipe:recipes) {
            String cls=recipe.getClass().getCanonicalName();
            if (!known.contains(cls)) {
                known.add(cls);
                System.out.println("Found recipe class "+cls);
            }
        }
        craftableCategories=new TreeMap();
        for (IRecipe recipe:recipes) {
            if (!canCraftRecipe(recipe, inventory, gridSize))
                continue;
            System.out.println("grid size is "+gridSize+", recipe needs "+recipe.getRecipeSize());
            ItemStack result=recipe.getRecipeOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            CreativeTabs tab = item.getCreativeTab();
            String category;
            if (tab==null)
                category="(none?)";
            else
                category=I18n.format(tab.getTranslatedTabLabel(), new Object[0]);
            TreeSet<IRecipe> catRecipes=craftableCategories.get(category);
            if (catRecipes==null) {
                catRecipes=new TreeSet<IRecipe>(new Comparator<IRecipe>() {
                    @Override
                    public int compare(IRecipe a, IRecipe b) {
                        return a.getRecipeOutput().getDisplayName().compareToIgnoreCase(b.getRecipeOutput().getDisplayName());
                    }
                }
                );
                craftableCategories.put(category, catRecipes);
            }
            System.out.println("adding "+result.getDisplayName()+" in "+category);
            catRecipes.add(recipe);
        }
        listSize=craftableCategories.size();
        for (TreeSet<IRecipe> tree: craftableCategories.values())
            listSize+=((tree.size()+7)/8);
        listSize*=itemSize;
    }
    
    class Takefrom { 
        Slot invitem; int amount; 
        Takefrom(Slot i, int n) { invitem=i; amount=n; }
    };

    private boolean canCraftRecipe(IRecipe recipe, Container inventory, int gridSize) {
        if (recipe instanceof ShapelessRecipes) {
            return canCraftShapeless((ShapelessRecipes) recipe, inventory);
        } else if (recipe instanceof ShapedRecipes) {
            return canCraftShaped((ShapedRecipes) recipe, inventory, gridSize);
        } else if (recipe instanceof ShapedOreRecipe) {
            return canCraftShapedOre((ShapedOreRecipe) recipe, inventory, gridSize);
        } else if (recipe instanceof ShapelessOreRecipe) {
            return canCraftShapelessOre((ShapelessOreRecipe) recipe, inventory, gridSize);
        } else {
            System.out.println(recipe.getRecipeOutput().getDisplayName()+" is a "+recipe.getClass().getCanonicalName());
        }
        return false;
    }
    
    private boolean canCraftShapeless(ShapelessRecipes recipe, Container inventory) {
        List<ItemStack> neededList = recipe.recipeItems;
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShaped(ShapedRecipes recipe, Container inventory, int gridSize) {
        if (recipe.recipeWidth>gridSize || recipe.recipeHeight>gridSize) {
            System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.recipeWidth+"/"+recipe.recipeHeight);
            return false;
        }
        List<ItemStack> neededList = Arrays.asList(recipe.recipeItems);
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShapedOre(ShapedOreRecipe recipe, Container inventory, int gridSize) {
        if (recipe.getWidth()>gridSize || recipe.getHeight()>gridSize) {
            System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.getWidth()+"/"+recipe.getHeight());
            return false;
        }
        return canCraftOre(recipe, Arrays.asList(recipe.getInput()), inventory);
    }

    private boolean canCraftShapelessOre(ShapelessOreRecipe recipe, Container inventory, int gridSize) {
        return canCraftOre(recipe, recipe.getInput(), inventory);
    }
    
    private boolean canCraftOre(IRecipe recipe, List<Object>input, Container inventory) {
        List<ItemStack>neededList=new ArrayList<ItemStack>();
        for (Object o: input) {
            if (o==null)
                continue;
            if (o instanceof ItemStack)
                neededList.add((ItemStack)o);
            else if (o instanceof NonNullList) {
                neededList.add(((NonNullList<ItemStack>)o).get(0));
            }
            else {
                System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs a "+o+" ("+o.getClass().getCanonicalName()+")");
                return false;
            }
        }
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraft(IRecipe recipe, List<ItemStack> neededList, Container inventory) {
        ArrayList<Takefrom> source=new ArrayList<Takefrom>(neededList.size());
        for (ItemStack neededItem: neededList) {                                // iterate over needed items
            int neededAmount=neededItem.getCount();
            if (neededAmount==0)
                continue;           // can happen with shaped recipes
            System.out.println("need "+neededAmount+" "+neededItem.getDisplayName()+" for "+recipe.getRecipeOutput().getDisplayName());
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
                System.out.println("can't craft "+recipe.getRecipeOutput().getDisplayName()+" because we don't have "+neededItem.getCount()+" "+neededItem.getDisplayName());
                return false;
            }
        }
        System.out.println("enough stuff for "+recipe.getRecipeOutput().getDisplayName());
        return true;
    }
    
    class InputCount {
        int count;
        int items;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // we assume the mouse is clicked where it was when we updated the screen last ...
        if (underMouse==null)
            return;

        // Do nothing if the grid isn't empty.
        for (int craftslot=0; craftslot<gridSize*gridSize; craftslot++) {
            ItemStack stack = container.inventorySlots.getSlot(craftslot+firstCraftSlot).getStack();
            if (stack!=null && !stack.isEmpty())
                return;
        }

        List<ItemStack> recipeInput=getIngredientsAsList(underMouse);
        
        int maxCraftableStacks=64;
        if (isShiftKeyDown()) {
            // Try to find out how much we can craft at once, maximally. This is limited by a) the number of
            // items per stack (for example, dispensers need bows that stack to 1, so we can't craft more than 1
            // dispenser at a time), and b) the number of items we have divided by the number of input slots that
            // need this item (a sea lantern has 5 shard input slots, so if we have 64 shards, we can't craft
            // more than floor(64/5)=12 lanters)

            // this assumes a recipe never needs more than one item in a single input slot (unless more than 1 output item)
            HashMap<String,InputCount> inputCount=new HashMap<String,InputCount>();
            for (ItemStack stack:recipeInput) {
                if (stack==null || stack.isEmpty())
                    continue;
                if (stack.getMaxStackSize()<maxCraftableStacks)                 // limit type a
                    maxCraftableStacks=stack.getMaxStackSize();
                String descriptor=stack.getDisplayName()+":"+stack.getItemDamage();
                if (inputCount.containsKey(descriptor)) {
                    InputCount previous = inputCount.get(descriptor);
                    previous.count++;
                } else {
                    int totalInInv=0;
                    for (int slot=0; slot<36; slot++) {
                        Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
                        ItemStack slotcontent=invitem.getStack();
                        if (canActAsIngredient(stack, slotcontent))
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
                System.out.println(descriptor+": need "+x.count+" times, have "+x.items+" items");
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

        System.out.println("Crafting "+maxCraftableStacks+" items");
        for (int craftslot=0; craftslot<recipeInput.size(); craftslot++) {
            int remaining=maxCraftableStacks;
            ItemStack stack=recipeInput.get(craftslot);
            if (stack==null)
                continue;
            for (int slot=0; remaining>0 && slot<36; slot++) {
                Slot invitem=container.inventorySlots.getSlot(slot+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(stack, slotcontent)) {
                    // TODO: merge more than one inventory slot into one craft slot
                    transfer(slot+firstInventorySlotNo, craftslot+firstCraftSlot, remaining);
                    remaining=maxCraftableStacks-container.inventorySlots.getSlot(craftslot+firstCraftSlot).getStack().getCount();
                }
            }
        }
        
        if (mouseButton==0) {
            slotClick(resultSlotNo, mouseButton, ClickType.QUICK_MOVE);     // which is really PICKUP ALL
            recipeUpdateTime=System.currentTimeMillis()+ConfigurationHandler.getAutoUpdateRecipeTimer()*1000;
        }
    }
    
    /**
     * Gets the ingredient list for a recipe as a List of ItemStacks, no matter
     * what kind of recipe it is. This list may include null entries on shaped
     * recipes to allow easier matching of list indexes to grid positions.
     * @param recipe
     * the recipe we want to get the ingredients for
     * @return
     * a list of ItemStacks
     */
    private List<ItemStack> getIngredientsAsList(IRecipe recipe) {
        // bah, what a mess .. normalize the ingredient datatype to an arraylist
        List<ItemStack> ingredientsList=new ArrayList<ItemStack>();
        if (recipe instanceof ShapedRecipes) {
            for (int y=0; y<gridSize; y++) {
                for (int x=0; x<gridSize; x++) {
                    ItemStack stack;
                    if (x<((ShapedRecipes)recipe).recipeWidth && y<((ShapedRecipes)recipe).recipeHeight)
                        stack = ((ShapedRecipes)recipe).recipeItems[x+y*((ShapedRecipes)recipe).recipeWidth];
                    else
                        stack=null;
                    ingredientsList.add(stack);
                }
            }
        } else if (recipe instanceof ShapelessRecipes) {
            for (ItemStack stack: ((ShapelessRecipes)recipe).recipeItems) {
                ingredientsList.add(stack);
            }
        } else if (recipe instanceof ShapedOreRecipe) {
            Object[] ingredients = ((ShapedOreRecipe)recipe).getInput();
            for (int y=0; y<gridSize; y++) {
                for (int x=0; x<gridSize; x++) {
                    if (x>=((ShapedOreRecipe)recipe).getWidth()
                    ||  y>=((ShapedOreRecipe)recipe).getHeight()) {
                        ingredientsList.add(null);
                        continue;
                    }
                    Object ingredient = ingredients[x+y*((ShapedOreRecipe)recipe).getWidth()];
                    if (ingredient instanceof ItemStack)
                        ingredientsList.add((ItemStack) ingredient);
                    else if (ingredient instanceof NonNullList)
                        ingredientsList.add(((NonNullList<ItemStack>)ingredient).get(0));
                    else
                        ingredientsList.add(null);
                }
            }
        } else if (recipe instanceof ShapelessOreRecipe) {
            for (Object ingredient: ((ShapelessOreRecipe)recipe).getInput()) {
                    if (ingredient instanceof ItemStack)
                        ingredientsList.add((ItemStack) ingredient);
                    else if (ingredient instanceof NonNullList)
                        ingredientsList.add(((NonNullList<ItemStack>)ingredient).get(0));
            }
        }
        return ingredientsList;
    }
    
    private boolean canActAsIngredient(ItemStack recipeComponent, ItemStack inventoryItem) {
        return 
           (recipeComponent.getItem()==inventoryItem.getItem()                 // that can act as source
        &&  !inventoryItem.hasTagCompound()                               // don't use enchanted items as source
        &&  ( recipeComponent.getItemDamage() == 32767
        ||    recipeComponent.getItemDamage() == inventoryItem.getItemDamage()));
        
    }
    
    private void transfer(int from, int to, int amount) {
        Slot fromSlot=container.inventorySlots.getSlot(from);
        ItemStack fromContent=fromSlot.getStack();
        
        System.out.println("Trying to transfer "+amount+" "+fromContent.getDisplayName()+" from slot "+from+" to "+to);
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
            System.out.println("transferred "+transfer+", amount is now "+amount);
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
        Minecraft mc=Minecraft.getMinecraft();
        System.out.println("Clicking slot "+slot+" "+(mouseButton==0 ? "left" : "right"));
        mc.playerController.windowClick(mc.player.openContainer.windowId, slot, mouseButton, clickType, mc.player);
    }
    
}
