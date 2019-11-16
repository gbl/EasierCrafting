/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.mixins;

import de.guntram.mcmod.easiercrafting.PropertyDelegateProvider;
import net.minecraft.container.AbstractFurnaceContainer;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.PropertyDelegate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 *
 * @author gbl
 */
@Mixin(AbstractFurnaceContainer.class)
public abstract class AbstractFurnaceContainerMixin extends CraftingContainer implements PropertyDelegateProvider {
    
    @Shadow @Final private PropertyDelegate propertyDelegate;
    
    public AbstractFurnaceContainerMixin(ContainerType containerType_1, int int_1) {
        super(containerType_1, int_1);
    }
    
    @Override
    public int getPropertyDelegate(int index) {
        return this.propertyDelegate.get(index);
    }
}
