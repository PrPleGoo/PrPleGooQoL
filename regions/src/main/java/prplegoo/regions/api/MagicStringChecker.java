package prplegoo.regions.api;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        return key.startsWith("WORLD_RESOURCE_PRODUCTION_");
    }

    public static boolean isLawBuilding(String key){
        return key.equals("INFRA_GALLOWS");
    }

    public static boolean isWorkforceBoostableKey(String key) { return key.equals("WORLD_POINT_WORKFORCE"); }

    public static boolean isGrowthBuilding(String key) {
        return key.equals("CIVIC_GROWTH");
    }
    public static boolean isQuarantineBuilding(String key) {
        return key.equals("INFRA_HYGINE");
    }
}
