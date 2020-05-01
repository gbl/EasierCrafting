package de.guntram.mcmod.easiercrafting;

import java.util.ArrayList;
import java.util.List;

public class LoomRecipe {
    
    private List<LoomStep> steps;
    private String name;

    public LoomRecipe(String name) {
        this.name=name;
        steps = new ArrayList<>();
    }
    
    public void addStep(LoomStep step) {
        steps.add(step);
    }
    
    public String toSaveString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(":\n");
        for (LoomStep step: steps) {
            builder.append(step.pattern).append(":").append(step.colorCode).append("\n");
        }
        return builder.toString();
    }
    
    public static LoomRecipe fromSaveString(String string) throws IllegalArgumentException {
        string=string.replaceAll("\r", "");
        String[] parts = string.split("\n");
        if (!parts[0].endsWith(":")) {
            throw new IllegalArgumentException("First line should be name:");
        }
        LoomRecipe result = new LoomRecipe(parts[0].substring(0, parts[0].length()-1));
        for (int i=1; i<parts.length; i++) {
            String[] patcol = parts[i].split(":");
            if (patcol.length != 2 || patcol[1].length() != 1) {
                throw new IllegalArgumentException("Line "+i+" has bad format "+parts[i]);
            }
            result.addStep(new LoomStep(patcol[0], patcol[1].charAt(0)));
        }
        return result;
    }
}
