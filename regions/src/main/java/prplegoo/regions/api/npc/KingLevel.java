package prplegoo.regions.api.npc;

import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGEat;
import lombok.Getter;
import snake2d.util.file.Json;
import world.region.RD;
import world.region.pop.RDRace;

public class KingLevel {
    @Getter
    private final double govPoints;
    @Getter
    private final double govPointsPerRegion;
    @Getter
    private final double capitalPopulationCapacityMul;
    @Getter
    private final double income;
    @Getter
    private final double[] consumption;
    @Getter
    private final double[] consumptionCapitalPop;
    @Getter
    private final double[][] consumptionPreferredCapitalPop;
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
        consumptionPreferredCapitalPop = MapPreferredConsumption(kingLevelJson.json("CONSUMPTION_PREFERRED_CAPITAL_POP"));
    }

    private double[] MapConsumption(Json consumptionJson) {
        double[] result = new double[RESOURCES.ALL().size()];

        for (String key : consumptionJson.keys()) {
            double consumptionValue = consumptionJson.d(key);
            if (key.equals("EDIBLE")) {
                HandleEdible(consumptionValue, result);
                continue;
            }

            for (int i = 0; i < RESOURCES.ALL().size(); i++) {
                if (RESOURCES.ALL().get(i).key.equals(key)) {
                    result[RESOURCES.ALL().get(i).index()] = consumptionValue;
                }
            }
        }

        return result;
    }

    private double[][] MapPreferredConsumption(Json consumptionJson) {
        double[][] result = new double[RD.RACES().all.size()][RESOURCES.ALL().size()];

        for (int i = 0; i < RD.RACES().all.size(); i++) {
            RDRace race = RD.RACES().all.get(i);
            for (String key : consumptionJson.keys()) {
                double consumptionValue = consumptionJson.d(key);
                if (key.equals("EDIBLE")) {
                    for (ResG edible : race.race.pref().food) {
                        result[race.index()][edible.resource.index()] = consumptionValue;
                    }
                    continue;
                }
                if (key.equals("FURNITURE")) {
//                    for(ResG edible : race.race) {
//                        result[race.index()][edible.resource.index()] = consumptionValue;
//                    }
                    continue;
                }
            }
        }

        return result;
    }

    private void HandleEdible(double edibleValue, double[] result) {
        for (ResGEat edible : RESOURCES.EDI().all()) {
            if (edible.serve) {
                result[edible.resource.index()] = edibleValue;
            }
        }
    }
}
