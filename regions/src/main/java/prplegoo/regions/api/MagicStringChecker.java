package prplegoo.regions.api;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        return key.startsWith("WORLD_RESOURCE_PRODUCTION_");
    }

    public static boolean isResourceConsumptionBooster(String key) {
        return key.startsWith("WORLD_RESOURCE_CONSUMPTION_");
    }

    public static boolean isTech(String key) {
        return key.equals("CIVIC_INNOVATION")
                || key.equals("CIVIC_KNOWLEDGE");
    }

    public static boolean isLawBuilding(String key){
        return key.equals("INFRA_GALLOWS");
    }

    public static boolean isWorkforceBoostableKey(String key) { return key.equals("WORLD_POINT_WORKFORCE"); }

    public static boolean isEstateBuilding(String key) {
        return key.equals("CIVIC_CENTRE");
    }
}
