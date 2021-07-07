/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easiercrafting.delayedslotclicks;

import de.guntram.mcmod.easiercrafting.ConfigurationHandler;
import de.guntram.mcmod.easiercrafting.SlotClickAccepter;
import java.util.LinkedList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.screen.slot.SlotActionType;

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
    
    private static DelayedSlotClickQueue instance;
    private final LinkedList<ClickInfo> pendingClicks;
    private final LinkedList<Runnable> pendingGenerators;
    private int tickCount;
    
    private DelayedSlotClickQueue() {
        pendingClicks=new LinkedList<>();
        pendingGenerators = new LinkedList<>();
        ClientTickEvents.END_CLIENT_TICK.register(e->executeEveryNTicks());
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
    
    private void executeEveryNTicks() {
        if (--tickCount <= 0) {
            tickCount = ConfigurationHandler.getLoomClickSpeed();
            internalExecute(true);
        }
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
