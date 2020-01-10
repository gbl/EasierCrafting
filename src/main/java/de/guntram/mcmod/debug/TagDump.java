/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.debug;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author gbl
 */
public class TagDump {
    static final Logger LOGGER = LogManager.getLogger(TagDump.class);

    public static void dump(CompoundTag tag, int indent) {
        StringBuilder res;
        if (tag == null) {
            LOGGER.info("dumping null tag");
            return;
        }
        if (tag.getKeys() == null) {
            LOGGER.debug("tag has no keys: "+tag.asString());
            return;
        }
        for (String s: tag.getKeys()) {
            Tag elem = tag.get(s);
            res=new StringBuilder();
            for (int i=0; i<indent; i++)
                res.append("    ");
            if (elem.getType()== 8) {
                res.append(s).append("(string):").append(tag.getString(s));
            } else if (elem.getType() == 2) {
                res.append(s).append("(short):").append(tag.getShort(s));
            } else if (elem.getType() == 3) {
                res.append(s).append("(int):").append(tag.getInt(s));
            } else if (elem.getType() == 10) {
                res.append(s).append("(Compound):");
            } else if (elem.getType() == 9) {
                ListTag list=(ListTag) elem;
                res.append(s).append(": List of type ").append(list.getElementType()).append(" count ").append(list.size());
            } else {
                res.append(s).append("(Type ").append(elem.getType()).append(")");
            }
            LOGGER.info(res);
            if (elem.getType() == 10) {
                dump(tag.getCompound(s), indent+1);
            }
            if (elem.getType() == 9) {
                ListTag list=(ListTag) elem;
                for (int i=0; i<list.size(); i++) {
                    // list.get(i)
                }
            }
        }
    }
    
}
