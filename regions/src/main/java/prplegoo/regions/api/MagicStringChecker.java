package prplegoo.regions.api;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        return key.contains("RESOURCE_");
    }
}
