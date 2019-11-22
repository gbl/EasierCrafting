/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting;

import net.minecraft.container.SlotActionType;

/**
 *
 * @author gbl
 */
public interface SlotClickAccepter {
    public void slotClick(int slot, int mouseButton, SlotActionType clickType);
}
