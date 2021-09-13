# EasierCrafting

This makes crafting easier by displaying a list of items you can craft using 
your current inventory. Clicking one of these items will craft this item 
once, while Shift-Clicking an item will fill the crafting grid once and 
shift-click the output field to craft as many items as possible.

Also, if you're not sure about a specific recipe, you can enter the (partial) 
name of the output item in a text box above the recipe list; getting a list of 
all craftable items that match that name, no matter if you have the ingredients. 
Moving your mouse to one of these items will show you the recipe.

This mod will not fill the crafting grid more than once per click, so you can't
craft your whole inventory at once. This is for the benefit of servers that 
allow mods, but don't want mods to be able to do "more" than vanilla 
minecraft - if you want to convert your whole inventory of sugar cane to 
paper, you'll have to keep shift-clicking the paper icon, converting one 
crafting grid (3 stacks) at a time.

(Also, I wasn't able to get this to work reliably, and gave up on it after
spending a lot of time trying!)

Version 1.6.7 has a new feature that requests an inventory resync after each
craft. It seems like this is a problem when you're playing on servers that
are not Spigot/Paper servers. There's a new config option that you can turn
on. It's not turned on by default as what it's doing is a bit hacky, and I
don't know if there are server plugins (like NoCheatPlus) that don't like it
and kick you, or get you banned. So please only turn it on if you have problems.
This is not needed any more in MC 1.17, because the protocol between server
and clients has changed.

To make sure the mod doesn't slow down your minecraft, it has been optimized 
using [![JProfiler Logo](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png "Logo")](https://www.ej-technologies.com/products/jprofiler/overview.html).
