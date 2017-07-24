package de.guntram.mcmod.easiercrafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeBook {
    private final GuiContainer container;
    private final int firstCraftSlot;
    private final int gridSize;
    private final int resultSlotNo;
    private final int firstInventorySlotNo;
    TreeMap<String, TreeSet<IRecipe>> craftableCategories;
    private IRecipe underMouse;
    
    private final int itemSize=20;
    private final int xOffset=-itemSize*8-10; // offset of list to standard gui
    private final int itemLift=5;               // how many pixels to display items above where they would be normally
    private int listSize;
    private long recipeUpdateTime;
    
    private GuiTextField pattern;
    private TreeSet<IRecipe> patternMatchingRecipes;
    private int patternListSize;
    
    
    RecipeBook(GuiContainer craftinv, int firstCraftSlot, int gridsize, int resultSlot, int firstInventorySlot) {
        this.container=craftinv;
        this.firstCraftSlot=firstCraftSlot;
        this.gridSize=gridsize;
        this.resultSlotNo=resultSlot;
        // firstInventorySlot is not neccesarily number of slots minus 36 -- 
        // player inventory has 9-44; 45 is offhand
        this.firstInventorySlotNo=firstInventorySlot;
        this.pattern=null;
        underMouse=null;
        updatePatternMatch();
        updateRecipes();
    }
    
    // left is the X position we want to draw at, Y is (normally) 0.
    // However, if our height is larger than the GUI container height,
    // adjust our Y position accordingly.
    void drawRecipeList(FontRenderer fontRenderer, RenderItem itemRenderer,
            int left, int height, int mouseX, int mouseY) {

        boolean underMouseIsCraftable=true;
        // We can't do this in the constructor as we don't yet know various stuff there.
        if (pattern==null) {
            pattern=new GuiTextField(1, fontRenderer, xOffset, 0, 150, 20);
            pattern.setFocused(ConfigurationHandler.getAutoFocusSearch());
        }

        if (recipeUpdateTime!=0 && System.currentTimeMillis() > recipeUpdateTime) {
            updateRecipes();
            recipeUpdateTime=0;
        }

        int xpos, ypos=0;
        int neededHeight=patternListSize+listSize;
        
        if (neededHeight>height)
            ypos-=(neededHeight-height)/2;
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();

        underMouse=null;

        pattern.y=ypos;
        pattern.drawTextBox();
        ypos+=itemSize*3/2;
        ypos=drawRecipeOutputs(patternMatchingRecipes, itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        if (underMouse!=null)
            underMouseIsCraftable=false;
        
        for (String category: craftableCategories.keySet()) {
            fontRenderer.drawString(category, xOffset, ypos, 0xffff00);
            ypos+=itemSize;
            ypos=drawRecipeOutputs(craftableCategories.get(category), itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        }
        if (underMouse!=null) {
            fontRenderer.drawString(underMouse.getRecipeOutput().getDisplayName(), 0, height+3, 0xffff00);
            if (underMouse instanceof ShapedRecipes) {
                fontRenderer.drawString("sr", left-20, height, 0x202020);
                for (int x=0; x<((ShapedRecipes)underMouse).recipeWidth; x++) {
                    for (int y=0; y<((ShapedRecipes)underMouse).recipeHeight; y++) {
                        NonNullList<Ingredient> ingredients = underMouse.getIngredients();
                        renderIngredient(itemRenderer, fontRenderer, ingredients.get(x+y*((ShapedRecipes)underMouse).recipeWidth), itemSize*x, height+itemSize+itemSize*y);                        
                    }
                }
            } else if (underMouse instanceof ShapelessRecipes) {
                fontRenderer.drawString("slr", left-20, height, 0x202020);
                xpos=0;
                for (Ingredient ingredient: ((ShapelessRecipes)underMouse).getIngredients()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
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
            }
        }
     
        RenderHelper.disableStandardItemLighting();
        if (!underMouseIsCraftable)
            underMouse=null;
    }

    private int drawRecipeOutputs(Iterable<IRecipe>recipes, 
            RenderItem itemRenderer, FontRenderer fontRenderer, 
            int xpos, int ypos,
            int mouseX, int mouseY) {

        for (IRecipe recipe: recipes) {
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
        return ypos;
    }
    
    public void renderIngredient(RenderItem itemRenderer, FontRenderer fontRenderer, Ingredient ingredient, int x, int y) {
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
        for (Iterator<IRecipe> i= CraftingManager.REGISTRY.iterator(); i.hasNext();)
            recipes.add(i.next());

        craftableCategories=new TreeMap();
        for (IRecipe recipe:recipes) {
            if (!canCraftRecipe(recipe, inventory, gridSize))
                continue;
            //System.out.println("grid size is "+gridSize+", recipe needs "+recipe.getRecipeSize());
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
                catRecipes=new TreeSet<>(new Comparator<IRecipe>() {
                    @Override
                    public int compare(IRecipe a, IRecipe b) {
                        return a.getRecipeOutput().getDisplayName().compareToIgnoreCase(b.getRecipeOutput().getDisplayName());
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
            listSize+=((tree.size()+7)/8);
        listSize*=itemSize;
    }
    
    public final void updatePatternMatch() {
        
        patternListSize=0;
        patternMatchingRecipes=new TreeSet<>(new Comparator<IRecipe>() {
            @Override
            public int compare(IRecipe a, IRecipe b) {
                return a.getRecipeOutput().getDisplayName().compareToIgnoreCase(b.getRecipeOutput().getDisplayName());
            }
        });

        if (pattern==null)          // constructor run but no gui opened yet
            return;
        String patternText=pattern.getText();
        if (patternText.length()<2)
            return;

        Container inventory=container.inventorySlots;
        List<IRecipe> recipes = new ArrayList<>();
        for (Iterator<IRecipe> i= CraftingManager.REGISTRY.iterator(); i.hasNext();)
            recipes.add(i.next());
        Pattern regex=Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        for (IRecipe recipe:recipes) {
            ItemStack result=recipe.getRecipeOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            if (!regex.matcher(result.getDisplayName()).find()) {
                //System.out.println("not adding "+result.getDisplayName()+" because no match");
                continue;
            }
            //System.out.println("adding "+result.getDisplayName()+" to pattern match "+patternText);
            patternMatchingRecipes.add(recipe);
        }
        patternListSize=((patternMatchingRecipes.size()+7)/8)*itemSize;
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
            //System.out.println(recipe.getRecipeOutput().getDisplayName()+" is a "+recipe.getClass().getCanonicalName());
        }
        return false;
    }
    
    private boolean canCraftShapeless(ShapelessRecipes recipe, Container inventory) {
        NonNullList<Ingredient> neededList = recipe.getIngredients();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShaped(ShapedRecipes recipe, Container inventory, int gridSize) {
        if (recipe.recipeWidth>gridSize || recipe.recipeHeight>gridSize) {
            //System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.recipeWidth+"/"+recipe.recipeHeight);
            return false;
        }
        NonNullList<Ingredient> neededList = recipe.getIngredients();
        return canCraft(recipe, neededList, inventory);
    }

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

    public void mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) {
        if (pattern!=null) {
            //System.out.println("x="+(mouseX-guiLeft)+", y="+(mouseY-guiTop)+"; patternx="+pattern.x+", patterny="+pattern.y);
            pattern.mouseClicked(mouseX-guiLeft, mouseY-guiTop, mouseButton);
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
                String descriptor=stacks[0].getDisplayName()+":"+stacks[0].getItemDamage();
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
            if (underMouse instanceof ShapedRecipes && ((craftslot+1)%((ShapedRecipes)underMouse).recipeWidth)==0) {
                rowadjust+=gridSize-((ShapedRecipes)underMouse).recipeWidth;
            }
        }
        
        if (mouseButton==0) {
            slotClick(resultSlotNo, mouseButton, ClickType.QUICK_MOVE);     // which is really PICKUP ALL
            recipeUpdateTime=System.currentTimeMillis()+ConfigurationHandler.getAutoUpdateRecipeTimer()*1000;
        }
    }
    
    public boolean keyTyped(char c, int i) throws IOException {
        if (c=='\r' || c=='\n') {
            updatePatternMatch();
            pattern.setFocused(false);
            return true;
        } else if (pattern.isFocused()) {
            pattern.textboxKeyTyped(c, i);
            return true;
        } else {
            return false;
        }
    }
    

    private NonNullList<Ingredient> getIngredientsAsList(IRecipe recipe) {
        // Wow. This was so much harder before 1.12.
        return recipe.getIngredients();
    }
    
    private boolean canActAsIngredient(Ingredient recipeComponent, ItemStack inventoryItem) {
        return !inventoryItem.hasTagCompound() && recipeComponent.apply(inventoryItem);
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
        Minecraft mc=Minecraft.getMinecraft();
        // System.out.println("Clicking slot "+slot+" "+(mouseButton==0 ? "left" : "right")+" type:"+clickType.toString());
        mc.playerController.windowClick(mc.player.openContainer.windowId, slot, mouseButton, clickType, mc.player);
    }
}
