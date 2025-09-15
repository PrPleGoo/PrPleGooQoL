package prplegoo.regions.api.npc;

import init.resources.*;
import init.type.BUILDING_PREF;
import init.type.BUILDING_PREFS;
import init.type.HCLASS;
import init.type.HCLASSES;
import lombok.Getter;
import settlement.main.SETT;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import world.region.RD;
import world.region.pop.RDRace;

import static settlement.main.SETT.TERRAIN;

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
    private final int maxRegions;
    @Getter
    private final double[] consumption;
    @Getter
    private final double[] consumptionCapitalPop;
    @Getter
    private final double[][] consumptionPreferredCapitalPop;

    @Getter
    private final int index;

    public KingLevel(Json kingLevelJson, int index) {
        govPoints = kingLevelJson.d("GOV_POINTS");
        govPointsPerRegion = kingLevelJson.d("GOV_POINTS_PER_REGION");
        capitalPopulationCapacityMul = kingLevelJson.d("CAPITAL_POPULATION_CAPACITY_MUL");
        income = kingLevelJson.d("INCOME");
        maxRegions = kingLevelJson.i("MAX_REGIONS");

        consumption = MapConsumption(kingLevelJson.json("CONSUMPTION"));
        consumptionCapitalPop = MapConsumption(kingLevelJson.json("CONSUMPTION_CAPITAL_POP"));
        consumptionPreferredCapitalPop = MapPreferredConsumption(kingLevelJson.json("CONSUMPTION_PREFERRED_CAPITAL_POP"));
        this.index = index;
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
                if (key.equals("DRINK")) {
                    for (ResGDrink drink : race.race.pref().drink) {
                        result[race.index()][drink.resource.index()] = consumptionValue;
                    }
                    continue;
                }
                if (key.equals("FURNITURE")) {
                    HCLASS clas = HCLASSES.CITIZEN();
                    LIST<RES_AMOUNT> rr = race.race.home().clas(clas).resources();
                    for (int ri = 0; ri < rr.size(); ri++) {
                        result[race.index()][rr.get(ri).resource().index()] = rr.get(ri).amount() * consumptionValue;
                    }
                    continue;
                }
                if (key.equals("STRUCTURE")) {
                    for (TBuilding building : TERRAIN().BUILDINGS.all()) {
                        if (building.resource == null || building.resAmount == 0) {
                            continue;
                        }

                        result[race.index()][building.resource.index()] = race.race.pref().structure(BUILDING_PREFS.get(building)) * consumptionValue;
                    }
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
