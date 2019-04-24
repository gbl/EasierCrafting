package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Items;
import net.minecraft.container.SlotActionType;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.crafting.CraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.util.DefaultedList;
import org.lwjgl.glfw.GLFW;

public class RecipeBook {
    private final ExtendedGuiCrafting screen;
    private final int firstCraftSlot;
    private final int gridSize;
    private final int resultSlotNo;
    private final int firstInventorySlotNo;
    TreeMap<String, TreeSet<CraftingRecipe>> craftableCategories;
    private CraftingRecipe underMouse;
    
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
    
    private TextFieldWidget pattern;
    private TreeSet<CraftingRecipe> patternMatchingRecipes;
    private int patternListSize;
    
    static Identifier arrows;
    private int containerLeft;
    private int containerTop;
    
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
    public RecipeBook(ExtendedGuiCrafting craftScreen, int firstCraftSlot, int gridsize, int resultSlot, int firstInventorySlot) {
        this.screen=craftScreen;
        this.firstCraftSlot=firstCraftSlot;
        this.gridSize=gridsize;
        this.resultSlotNo=resultSlot;
        // firstInventorySlot is not neccesarily number of slots minus 36 -- 
        // player inventory has 9-44; 45 is offhand
        this.firstInventorySlotNo=firstInventorySlot;
        this.pattern=null;
        this.underMouse=null;
        
        
        if (arrows==null) {
            arrows=new Identifier(EasierCrafting.MODID, "textures/arrows.png");
            /* ??? 
            SimpleTexture x=new SimpleTexture(arrows);
            boolean flag = MinecraftClient.getInstance().getTextureManager().loadTexture(arrows, x);
            //System.out.println("loading "+arrows.toString()+": "+flag);
            */
        }
    }
    
    void afterInitGui() {
        this.containerLeft = (screen.width - 176 /* screen.containerWidth */)/2;
        this.containerTop  = (screen.height - 166 /* screen.containerHeight */) /2;

        int tempItemsPerRow = 8;
        int tempXOffset=-itemSize*tempItemsPerRow -10;
        if (tempXOffset+containerLeft < 0) {
            tempItemsPerRow=(containerLeft-10)/itemSize;
            tempXOffset=-itemSize*tempItemsPerRow-10;
        }
        textBoxSize=-tempXOffset-15;
        if (ConfigurationHandler.getShowGuiRight())
            tempXOffset=176 /* screen.containerWidth */  + 10;
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
    void drawRecipeList(TextRenderer fontRenderer, ItemRenderer itemRenderer,
            int left, int height, int mouseX, int mouseY) {

        // We can't do this in the constructor as we don't yet know sizes from initGui.
        // Also, not in afterInitGui() because we don't know fontRender there.
        if (pattern==null) {
            //System.out.println("drawRecipeList creating textbox");
            //Collection<CraftingRecipe> x = Minecraft.getInstance().player.world.getRecipeManager().getRecipes();
            //System.out.println("found "+x.size()+" recipes");
            
            pattern=new TextFieldWidget(fontRenderer, xOffset, 0, textBoxSize, 20, "");
            pattern.changeFocus(ConfigurationHandler.getAutoFocusSearch());
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
                screen.blit(xOffset,                ypos,  0, 0, 20, 20);
                screen.blit(xOffset+textBoxSize-20, ypos, 20, 0, 20, 20);
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
        GuiLighting.enable();
        GuiLighting.enableForItems();

        underMouse=null;

        pattern.y=ypos;
        pattern.renderButton(0, 0, 0f);    // <-- parameters neccessary but unused 
        ypos+=itemSize*3/2;
        minYtoDraw=ypos;
        ypos-=mouseScroll*itemSize;
        ypos=drawRecipeOutputs(patternMatchingRecipes, itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        if (underMouse!=null)
            underMouseIsCraftable=false;
        
        for (String category: craftableCategories.keySet()) {
//            System.out.println(category+" at "+xOffset+"/"+ypos);
            if (ypos>=minYtoDraw)
                fontRenderer.draw(category, xOffset, ypos, 0xffff00);
            ypos+=itemSize;
            ypos=drawRecipeOutputs(craftableCategories.get(category), itemRenderer, fontRenderer, 0, ypos, mouseX, mouseY);
        }
        if (underMouse!=null) {
            fontRenderer.draw(underMouse.getOutput().getDisplayName().getFormattedText(), 0, height+3, 0xffff00);
            if (underMouse instanceof ShapedRecipe) {
                DefaultedList<Ingredient> ingredients = underMouse.getPreviewInputs();
                fontRenderer.draw("sr", left-20, height, 0x202020);
                for (int x=0; x<((ShapedRecipe)underMouse).getWidth(); x++) {
                    for (int y=0; y<((ShapedRecipe)underMouse).getHeight(); y++) {
                        renderIngredient(itemRenderer, fontRenderer, 
                                ingredients.get(x+y*((ShapedRecipe)underMouse).getWidth()), itemSize*x, height+itemSize+itemSize*y);                        
                    }
                }
            } else if (underMouse instanceof ShapelessRecipe) {
                fontRenderer.draw("slr", left-20, height, 0x202020);
                xpos=0;
                for (Ingredient ingredient: ((ShapelessRecipe)underMouse).getPreviewInputs()) {
                    renderIngredient(itemRenderer, fontRenderer, ingredient, itemSize*xpos, height+itemSize);
                    xpos++;
                }
            }
        }
     
        GuiLighting.disable();
        if (!underMouseIsCraftable)
            underMouse=null;
    }

    private int drawRecipeOutputs(TreeSet<CraftingRecipe>recipes, 
            ItemRenderer itemRenderer, TextRenderer fontRenderer, 
            int xpos, int ypos,
            int mouseX, int mouseY) {

//        System.out.println("drawing recipes at "+xpos+"/"+ypos);
        for (CraftingRecipe recipe: recipes) {
            ItemStack items=recipe.getOutput();
            if (ypos>=minYtoDraw) {
                itemRenderer.renderGuiItemIcon(items, xOffset+xpos, ypos-itemLift);
                itemRenderer.renderGuiItemOverlay(fontRenderer, items, xOffset+xpos, ypos-itemLift);
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
    
    public void renderIngredient(ItemRenderer itemRenderer, TextRenderer fontRenderer, Ingredient ingredient, int x, int y) {
        ItemStack[] stacks=ingredient.getStackArray();
        if (stacks.length==0)
            return;
        int toRender=0;
        if (stacks.length>1)
            toRender=(int) ((System.currentTimeMillis()/333)%stacks.length);
        itemRenderer.renderGuiItem(stacks[toRender], x, y);
        itemRenderer.renderGuiItemOverlay(fontRenderer, stacks[toRender], x, y);
    }
    
    public final void updateRecipes() {
        Container inventory=screen.getContainer();
        List<Recipe> recipes = new ArrayList<>();
        recipes.addAll(MinecraftClient.getInstance().player.world.getRecipeManager().values());
        if (ConfigurationHandler.getAllowGeneratedRecipes()) {
            recipes.addAll(InventoryRecipeScanner.findUnusualRecipes(inventory, firstInventorySlotNo));
        }

        craftableCategories=new TreeMap<>();
        for (Recipe recipe:recipes) {
            if (recipe.getType() != RecipeType.CRAFTING || !(recipe instanceof CraftingRecipe))
                continue;
            if (!canCraftRecipe((CraftingRecipe)recipe, inventory, gridSize))
                continue;
            //System.out.println("grid size is "+gridSize+", recipe needs "+recipe.getRecipeSize());
            ItemStack result=recipe.getOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            ItemGroup tab = item.getItemGroup();
            String category;
            if (tab==null) {
/*                if (item==Items.FIREWORKS)
                    category = "Fireworks";
else */
                    category="(none?)";
            }
            else
                category=I18n.translate(tab.getTranslationKey(), new Object[0]);
            TreeSet<CraftingRecipe> catRecipes=craftableCategories.get(category);
            if (catRecipes==null) {
                catRecipes=new TreeSet<>(new Comparator<CraftingRecipe>() {
                    @Override
                    public int compare(CraftingRecipe a, CraftingRecipe b) {
                        return a.getOutput().getDisplayName().getText().
                            compareToIgnoreCase(b.getOutput().getDisplayName().getText());
                    }
                }
                );
                craftableCategories.put(category, catRecipes);
            }
            //System.out.println("adding "+result.getDisplayName()+" in "+category);
            catRecipes.add((CraftingRecipe)recipe);
        }
        listSize=craftableCategories.size();
        for (TreeSet<CraftingRecipe> tree: craftableCategories.values())
            listSize+=((tree.size()+(itemsPerRow-1))/itemsPerRow);
        listSize*=itemSize;
        mouseScroll=0;
    }
    
    public final void updatePatternMatch() {
        
        patternListSize=0;
        patternMatchingRecipes=new TreeSet<>(new Comparator<CraftingRecipe>() {
            @Override
            public int compare(CraftingRecipe a, CraftingRecipe b) {
                return a.getOutput().getDisplayName().getText().
                        compareToIgnoreCase(b.getOutput().getDisplayName().getText());
            }
        });

        if (pattern==null)          // constructor run but no gui opened yet
            return;
        String patternText=pattern.getText();
        if (patternText.length()<2)
            return;

        Container inventory=screen.getContainer();
        List<Recipe> recipes = new ArrayList<>();
        recipes.addAll(MinecraftClient.getInstance().player.world.getRecipeManager().values());
//        for (Iterator<CraftingRecipe> i= CraftingManager.REGISTRY.iterator(); i.hasNext();)
//            recipes.add(i.next());
        Pattern regex=Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);
        for (Recipe recipe:recipes) {
            if (recipe.getType() != RecipeType.CRAFTING)
                continue;
            ItemStack result=recipe.getOutput();
            Item item = result.getItem();
            if (item==Items.AIR)
                continue;
            if (!regex.matcher(result.getDisplayName().getText()).find()) {
                //System.out.println("not adding "+result.getDisplayName()+" because no match");
                continue;
            }
            //System.out.println("adding "+result.getDisplayName()+" to pattern match "+patternText);
            patternMatchingRecipes.add((CraftingRecipe) recipe);
        }
        patternListSize=((patternMatchingRecipes.size()+(itemsPerRow-1))/itemsPerRow)*itemSize;
        mouseScroll=0;
    }

    class Takefrom { 
        Slot invitem; int amount; 
        Takefrom(Slot i, int n) { invitem=i; amount=n; }
    };

    private boolean canCraftRecipe(CraftingRecipe recipe, Container inventory, int gridSize) {
        if (recipe instanceof ShapelessRecipe) {
            return canCraftShapeless((ShapelessRecipe) recipe, inventory);
        } else if (recipe instanceof ShapedRecipe) {
            return canCraftShaped((ShapedRecipe) recipe, inventory, gridSize);
        } else if (recipe instanceof InventoryGeneratedRecipe || recipe instanceof RepairRecipe) {
            // We just generated the recipe so we should be able to craft it,
            // but make sure the grid is big enough
            return recipe.fits(gridSize, gridSize);
            // return canCraft(recipe, recipe.getIngredients(), inventory);
        } else {
            //System.out.println(recipe.getRecipeOutput().getDisplayName()+" is a "+recipe.getClass().getCanonicalName());
        }
        return false;
    }
    
    private boolean canCraftShapeless(ShapelessRecipe recipe, Container inventory) {
        DefaultedList<Ingredient> neededList = recipe.getPreviewInputs();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftShaped(ShapedRecipe recipe, Container inventory, int gridSize) {
        if (recipe.getWidth()>gridSize || recipe.getHeight()>gridSize) {
            //System.out.println("Can't do "+recipe.getRecipeOutput().getDisplayName()+" as it needs "+recipe.recipeWidth+"/"+recipe.recipeHeight);
            return false;
        }
        DefaultedList<Ingredient> neededList = recipe.getPreviewInputs();
        return canCraft(recipe, neededList, inventory);
    }

    private boolean canCraftOre(CraftingRecipe recipe, DefaultedList<Ingredient>input, Container inventory) {
        return canCraft(recipe, input, inventory);
    }

    private boolean canCraft(CraftingRecipe recipe, List<Ingredient> neededList, Container inventory) {
        ArrayList<Takefrom> source=new ArrayList<>(neededList.size());
        for (Ingredient neededItem: neededList) {                                // iterate over needed items
            ItemStack[] stacks=neededItem.getStackArray();
            if (stacks.length==0)
                continue;
            int neededAmount=stacks[0].getAmount();
            // System.out.println("need "+neededAmount+" "+stacks[0].getDisplayName()+" for "+recipe.getRecipeOutput().getDisplayName());
            for (int i=0; i<36; i++) {
                Slot invitem=inventory.getSlot(i+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(neededItem, slotcontent)) {
                    int providedAmount=slotcontent.getAmount();                 // check how many items there are
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

        // Do nothing if the grid isn't empty.
        for (int craftslot=0; craftslot<gridSize*gridSize; craftslot++) {
            ItemStack stack = screen.getContainer().getSlot(craftslot+firstCraftSlot).getStack();
            if (stack!=null && !stack.isEmpty())
                return;
        }
        
        if (underMouse instanceof RepairRecipe) {
//            fillCraftSlotsWithBestRepair((RepairRecipe)underMouse);       not with 1.14
        } else {
            fillCraftSlotsWithAnyMaterials(underMouse);
        }

        
        if (mouseButton==0) {
            slotClick(resultSlotNo, mouseButton, SlotActionType.QUICK_MOVE);     // which is really PICKUP ALL
            recipeUpdateTime=System.currentTimeMillis()+ConfigurationHandler.getAutoUpdateRecipeTimer()*1000;
        }
    }
    
    private void fillCraftSlotsWithAnyMaterials(CraftingRecipe underMouse) {
        DefaultedList<Ingredient> recipeInput=getIngredientsAsList(underMouse);
        
        int maxCraftableStacks=64;
        if (screen.hasShiftDown()) {
            // Try to find out how much we can craft at once, maximally. This is limited by a) the number of
            // items per stack (for example, dispensers need bows that stack to 1, so we can't craft more than 1
            // dispenser at a time), and b) the number of items we have divided by the number of input slots that
            // need this item (a sea lantern has 5 shard input slots, so if we have 64 shards, we can't craft
            // more than floor(64/5)=12 lanters)

            // this assumes a recipe never needs more than one item in a single input slot (unless more than 1 output item)
            HashMap<String,InputCount> inputCount=new HashMap<>();
            for (Ingredient ingr:recipeInput) {
                ItemStack[] stacks = ingr.getStackArray();
                if (stacks.length==0)
                    continue;
                if (stacks[0].getMaxAmount()<maxCraftableStacks)                 // limit type a
                    maxCraftableStacks=stacks[0].getMaxAmount();
                String descriptor=stacks[0].getDisplayName()+":"+stacks[0].getDamage();
                if (inputCount.containsKey(descriptor)) {
                    InputCount previous = inputCount.get(descriptor);
                    previous.count++;
                } else {
                    int totalInInv=0;
                    for (int slot=0; slot<36; slot++) {
                        Slot invitem=screen.getContainer().getSlot(slot+firstInventorySlotNo);
                        ItemStack slotcontent=invitem.getStack();
                        if (canActAsIngredient(ingr, slotcontent))
                            totalInInv+=slotcontent.getAmount();
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
                Slot invitem=screen.getContainer().getSlot(slot+firstInventorySlotNo);
                ItemStack slotcontent=invitem.getStack();
                if (canActAsIngredient(ingr, slotcontent)) {
                    // System.out.println("craftslot is "+craftslot+", first is "+firstCraftSlot+", rowadjust is "+rowadjust+", transferring "+remaining+" items");
                    // TODO: && (isempty(craftslot) || ismergeable(slot,craftslot))
                    transfer(slot+firstInventorySlotNo, craftslot+firstCraftSlot+rowadjust, remaining);
                    remaining=maxCraftableStacks-screen.getContainer().getSlot(craftslot+firstCraftSlot+rowadjust).getStack().getAmount();
                }
            }
            if (underMouse instanceof ShapedRecipe && ((craftslot+1)%((ShapedRecipe)underMouse).getWidth())==0) {
                rowadjust+=gridSize-((ShapedRecipe)underMouse).getWidth();
            }
        }
    }

/* Not with 1.14 ...
    private void fillCraftSlotsWithBestRepair(RepairRecipe repairRecipe) {
        // Search for item that has least damage. Put in first slot.
        // Search for second item that
        //   a) repairs first item fully, taking the one that is damaged most
        //   b) does not repair first item fully but adds as much as possible
        int firstSlot=-1, secondSlot=-1;
        for (int slot=0; slot<36; slot++) {
            Slot invitem=screen.getContainer().getSlot(slot+firstInventorySlotNo);
            ItemStack slotcontent=invitem.getStack();
            if (slotcontent.getItem() == repairRecipe.getItem()
            &&  slotcontent.getDamage()>0
            &&  slotcontent.getEnchantmentList().size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()
            ) {
                if (firstSlot==-1)
                    firstSlot=slot;
                else if (screen.getContainer().getSlot(firstSlot+firstInventorySlotNo).getStack().getDamage() > slotcontent.getDamage())
                    firstSlot=slot;
            }
        }
        if (firstSlot==-1) {
            return;
        }
        int neededRepair=screen.getContainer().getSlot(firstSlot+firstInventorySlotNo).getStack().getDamage();
        transfer(firstSlot+firstInventorySlotNo, firstCraftSlot, 1);

        for (int slot=0; slot<36; slot++) {
            if (slot==firstSlot)
                continue;
            Slot invitem=screen.getContainer().getSlot(slot+firstInventorySlotNo);
            ItemStack slotcontent=invitem.getStack();
            if (slotcontent.getItem() == repairRecipe.getItem()
            &&  slotcontent.getDamage()>0
            &&  slotcontent.getEnchantmentList().size() <= ConfigurationHandler.getMaxEnchantsAllowedForRepair()
            ) {
                if (secondSlot==-1)
                    secondSlot=slot;
                else {
                    ItemStack currentRepairStack = screen.getContainer().getSlot(secondSlot+firstInventorySlotNo).getStack();
                    int currentRepairValue=currentRepairStack.getDurability()-currentRepairStack.getDamage();
                    ItemStack testedRepairStack = screen.getContainer().getSlot(slot+firstInventorySlotNo).getStack();
                    int testedRepairValue=testedRepairStack.getDurability()-testedRepairStack.getDamage();

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
*/

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
    
    private DefaultedList<Ingredient> getIngredientsAsList(CraftingRecipe recipe) {
        // Wow. This was so much harder before 1.12.
        return recipe.getPreviewInputs();
    }
    
    private boolean canActAsIngredient(Ingredient recipeComponent, ItemStack inventoryItem) {
        if (!inventoryItem.hasTag() && recipeComponent.test(inventoryItem))
            return true;
        
        if (inventoryItem.getItem()!=Items.LINGERING_POTION)
            return false;
        
        Potion neededType = PotionUtil.getPotion(inventoryItem);
        ItemStack[] possiblePotions = recipeComponent.getStackArray();
        for (ItemStack stack: possiblePotions) {
            if (PotionUtil.getPotion(stack) == neededType)
                return true;
        }
        return false;
    }
    
    private void transfer(int from, int to, int amount) {
        Slot fromSlot=screen.getContainer().getSlot(from);
        ItemStack fromContent=fromSlot.getStack();
        
        //System.out.println("Trying to transfer "+amount+" "+fromContent.getDisplayName()+" from slot "+from+" to "+to);
        // want as much as we have, or more? Transfer all there is
        if (amount >= fromSlot.getStack().getAmount()) {
            slotClick(from, 0, SlotActionType.PICKUP);
            slotClick(to, 0, SlotActionType.PICKUP);
            return;
        }

        // Transfer as many half stacks as possible. If there is an odd number
        // of items in the source slot, right clicking will round the source slot
        // down, and the hand up, so we transfer (n+1)/2 items.
        int transfer;
        while (amount>=(transfer=((fromSlot.getStack().getAmount()+1)/2))) {
            slotClick(from, 1, SlotActionType.PICKUP);       // right click to get half the source
            slotClick(to, 0, SlotActionType.PICKUP);
            amount-=transfer;
            //System.out.println("transferred "+transfer+", amount is now "+amount);
        }
        
        if (amount>0) {
            int prevCount=fromContent.getAmount();
            slotClick(from, 0, SlotActionType.PICKUP);       // left click source
            for (int i=0; i<amount; i++)
                slotClick(to, 1, SlotActionType.PICKUP);         // right click target to deposit 1 item
            if (prevCount>amount)
                slotClick(from, 0, SlotActionType.PICKUP);       // left click source again to put stuff back
        }
    }
    
    private void slotClick(int slot, int mouseButton, SlotActionType clickType) {
        screen.slotClick(slot, mouseButton, clickType);
    }
}
