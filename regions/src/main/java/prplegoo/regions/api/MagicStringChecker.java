package prplegoo.regions.api;

public class MagicStringChecker {
    public static boolean isResourceProductionBooster(String key) {
        return key.startsWith("WORLD_RESOURCE_PRODUCTION_");
    }

    public static boolean isFoodStallBuilding(String key){
        return key.equals("NONADMIN_FOODSTALL");
    }

    public static boolean isSlaverBuilding(String key){
        return key.equals("NONADMIN_SLAVER");
    }

    public static boolean isWorkforceBoostableKey(String key) { return key.equals("WORLD_POINT_WORKFORCE"); }

    public static boolean isPivaRes(String key) { return key.equals("ALCO_BEER");}

    public static boolean isGrowthBuilding(String key) {
        return key.equals("BUILDING_GLOBAL_GROWTH");
    }

    public static boolean isHygieneBuilding(String key) {
        return key.equals("BUILDING_GLOBAL_HYGINE_1");
    }
}
