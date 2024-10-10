package prplegoo.regions.api;

import snake2d.LOG;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        return key.startsWith("WORLD_RESOURCE_PRODUCTION_");
    }
}
