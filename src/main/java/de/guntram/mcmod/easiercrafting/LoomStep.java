package de.guntram.mcmod.easiercrafting;

public class LoomStep {
    String pattern;
    char colorCode;
    
    LoomStep(String pattern, char colorCode) {
        this.pattern=pattern;
        this.colorCode=colorCode;
    }

    @Override
    public String toString() {
        return pattern + " in color "+colorCode;
    }
}
