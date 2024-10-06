package prplegoo.regions.api;

import snake2d.LOG;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        boolean result = key.startsWith("WORLD_RESOURCE_PRODUCTION_");
        if(result) {
            LOG.ln("isResourceProductionBooster, " + key);
        }

        return result;
    }
}
