/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.delayedslotclicks;

import de.guntram.mcmod.easiercrafting.SlotClickAccepter;
import java.util.LinkedList;
import net.minecraft.container.SlotActionType;

/**
 *
 * @author gbl
 */
public class DelayedSlotClickQueue {
    
    static class ClickInfo {
        SlotClickAccepter screen;
        int slot;
        int button;
        SlotActionType clickType;
        
        ClickInfo(SlotClickAccepter screen, int slot, int button, SlotActionType clickType) {
            this.screen=screen;
            this.slot=slot;
            this.button=button;
            this.clickType=clickType;
        }
    }
    
    public static abstract class ClickGenerator<T extends Object> implements Runnable {
        T addInfo;
        public ClickGenerator(T info) {
            addInfo = info;
        }
        public abstract void runWithInfo(T info);
        @Override
        public final void run() {
            runWithInfo(addInfo);
        }
    }
    
    private LinkedList<ClickInfo> pendingClicks;
    private LinkedList<Runnable> pendingGenerators;
    private static DelayedSlotClickQueue instance;
    
    private DelayedSlotClickQueue() {
        pendingClicks=new LinkedList<>();
        pendingGenerators = new LinkedList<>();
    }
    
    public static DelayedSlotClickQueue getInstance() {
        if (instance == null) {
            instance=new DelayedSlotClickQueue();
        }
        return instance;
    }
    
    public static void addClick(SlotClickAccepter screen, int slot, int button, SlotActionType clickType) {
        getInstance().pendingClicks.offerLast(new ClickInfo(screen, slot, button, clickType));
    }
    
    public static void addGenerator(Runnable generator) {
        getInstance().pendingGenerators.offerLast(generator);
    }
    
    public static void execute() {
        getInstance().internalExecute(true);
    }
    
    public static void executeClicksOnly() {
        getInstance().internalExecute(false);
    }
    
    public static void clearGenerators() {
        getInstance().pendingGenerators.clear();
    }
    
    public static void clear() {
        getInstance().pendingGenerators.clear();
        getInstance().pendingClicks.clear();
    }
    
    private void internalExecute(boolean runGenerators) {
        while (runGenerators && pendingClicks.isEmpty() && !pendingGenerators.isEmpty()) {
            Runnable generator = pendingGenerators.pollFirst();
            generator.run();
        }
        if (!pendingClicks.isEmpty()) {
            ClickInfo info = pendingClicks.pollFirst();
            info.screen.slotClick(info.slot, info.button, info.clickType);
        }
    }
}
