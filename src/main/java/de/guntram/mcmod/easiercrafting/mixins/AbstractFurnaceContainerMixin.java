/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.accessorInterfaces.PropertyDelegateProvider;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 *
 * @author gbl
 */
@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class AbstractFurnaceContainerMixin extends AbstractRecipeScreenHandler implements PropertyDelegateProvider {
    
    @Shadow @Final private PropertyDelegate propertyDelegate;
    
    public AbstractFurnaceContainerMixin(ScreenHandlerType containerType_1, int int_1) {
        super(containerType_1, int_1);
    }
    
    @Override
    public int getPropertyDelegate(int index) {
        return this.propertyDelegate.get(index);
    }
}
