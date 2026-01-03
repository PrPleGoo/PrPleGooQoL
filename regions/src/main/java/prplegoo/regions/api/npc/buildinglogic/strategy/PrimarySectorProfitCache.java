package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.sets.LIST;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrimarySectorProfitCache {
    private final double[][] PrimarySectorProfitCache;
    private final double[] PrimarySectorMaxProfitCache;
    private final double[] PrimarySectorAverageProfitCache;
    private static PrimarySectorProfitCache _instance;

    public static PrimarySectorProfitCache getInstance() {
        if (_instance == null) {
            _instance = new PrimarySectorProfitCache();
        }

        return _instance;
    }

    public static void Reset() {
        _instance = null;
    }

    public PrimarySectorProfitCache(){
        PrimarySectorProfitCache = new double[FACTIONS.NPC_MAX()][RD.BUILDINGS().all.size()];
        PrimarySectorMaxProfitCache = new double[FACTIONS.NPC_MAX()];
        PrimarySectorAverageProfitCache = new double[FACTIONS.NPC_MAX()];
    }

    public double get(FactionNPC faction, int buildingIndex) {
        if (getMax(faction) == 0) {
            setCache(faction);
        }

        return PrimarySectorProfitCache[faction.index()][buildingIndex];
    }

    public double getMax(FactionNPC faction) {
        if (PrimarySectorMaxProfitCache[faction.index()] == 0) {
            setCache(faction);
        }

        return PrimarySectorMaxProfitCache[faction.index()];
    }

    public double getAverage(FactionNPC faction) {
        if (getMax(faction) == 0) {
            setCache(faction);
        }

        return PrimarySectorAverageProfitCache[faction.index()];
    }

    public void setCache(FactionNPC faction) {
        PrimarySectorMaxProfitCache[faction.index()] = -1;

        double totalProfit = 0;
        int totalBuildings = 0;
        for(int i = 0; i < RD.BUILDINGS().all.size(); i++) {
            RDBuilding building = RD.BUILDINGS().all.get(i);
            if (!isPrimarySector(building)) {
                continue;
            }

            totalBuildings++;

            PrimarySectorProfitCache[faction.index()][i] = calculateProfit(building, faction);
            totalProfit += PrimarySectorProfitCache[faction.index()][i];

            if (PrimarySectorProfitCache[faction.index()][i] > PrimarySectorMaxProfitCache[faction.index()]) {
                PrimarySectorMaxProfitCache[faction.index()] = PrimarySectorProfitCache[faction.index()][i];
            }
        }

        PrimarySectorAverageProfitCache[faction.index()] = totalProfit / totalBuildings;
    }

    private static double calculateProfit(RDBuilding building, FactionNPC faction) {
        RoomBlueprintImp blue = building.getBlue();
        LIST<Industry> industries = ((INDUSTRY_HASER) blue).industries();
        LIST<IndustryResource> outputs = industries.get(0).outs();

        double multiplier = KingLevels.getInstance().getModifiedTechMul(building, faction);

        double priceSum = 0.0;
        for (int j = 0; j < outputs.size(); j++) {
            RESOURCE resource = outputs.get(j).resource;

            double price = faction.stockpile.price.get(resource);

            priceSum += outputs.get(j).rate * price * multiplier;
        }

        return priceSum;
    }

    public static boolean isPrimarySector(RDBuilding building) {
        RoomBlueprintImp blue = building.getBlue();
        if (blue == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) blue).industries();

        return industries.size() == 1 && industries.get(0).ins().isEmpty();
    }
}
