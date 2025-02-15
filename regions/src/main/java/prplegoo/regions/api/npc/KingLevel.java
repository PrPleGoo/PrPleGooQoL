package prplegoo.regions.api.npc;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGEat;
import lombok.Getter;
import snake2d.util.file.Json;

public class KingLevel {
    @Getter
    private final double govPoints;
    @Getter
    private final double govPointsPerRegion;
    @Getter
    private final double capitalPopulationCapacityMul;
    @Getter
    private final double income;
    private final double[] consumption;
    private final double[] consumptionCapitalPop;
    // TODO: ConsumptionPreferredFood
    // TODO: ConsumptionFurniture
    // TODO: ConsumptionPreferredDrink

    public KingLevel(Json kingLevelJson) {
        govPoints = kingLevelJson.d("GOV_POINTS");
        govPointsPerRegion = kingLevelJson.d("GOV_POINTS_PER_REGION");
        capitalPopulationCapacityMul = kingLevelJson.d("CAPITAL_POPULATION_CAPACITY_MUL");
        income = kingLevelJson.d("INCOME");

        consumption = MapConsumption(kingLevelJson.json("CONSUMPTION"));
        consumptionCapitalPop = MapConsumption(kingLevelJson.json("CONSUMPTION_CAPITAL_POP"));
    }

    private double[] MapConsumption(Json consumptionJson) {
        double[] result = new double[RESOURCES.ALL().size()];

        for(String key : consumptionJson.keys()) {
            double consumptionValue = consumptionJson.d(key);
            if (key.equals("EDIBLE")) {
                HandleEdible(consumptionValue, result);
                continue;
            }

            for (RESOURCE resource : RESOURCES.ALL()) {
                if (resource.key.equals(key)) {
                    result[resource.index()] = consumptionValue;
                }
            }
        }

        return result;
    }

    private void HandleEdible(double edibleValue, double[] result) {
        for(ResGEat edible : RESOURCES.EDI().all()) {
            if (edible.serve) {
                result[edible.resource.index()] = edibleValue;
            }
        }
    }
}
