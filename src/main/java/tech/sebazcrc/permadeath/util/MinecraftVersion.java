package tech.sebazcrc.permadeath.util;

public enum MinecraftVersion {
    v1_15_R1,
    v1_16_R3,
    v1_20_R1,
    v1_21_R1; 

    private static final String REVISION_PATTERN = "_R\\d";

    public boolean isAboveOrEqual(MinecraftVersion compare) {
        return ordinal() >= compare.ordinal();
    }

    public boolean isSubVersionOf(MinecraftVersion version) {
        String[] currentSplit = name().split("_");
        String[] compareSplit = version.name().split("_");
        if (currentSplit.length > 1 && compareSplit.length > 1) {
            return compareSplit[1].equalsIgnoreCase(currentSplit[1]);
        }
        return false;
    }

    public String getFormattedName() {
        return this.name().replaceAll(REVISION_PATTERN, "").replace("_", ".");
    }
}
