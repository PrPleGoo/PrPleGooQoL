package prplegoo.regions.api;

import game.boosting.*;
import init.resources.Minable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.TERRAINS;
import snake2d.LOG;
import util.dic.Dic;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class PrPleGooEfficiencies {
    public static RBooster POP_SCALING(RDBuilding bu) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return 1.0;
            }
        };
    }

    public static RBooster POP_SCALING_WOOD(RDBuilding bu) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return t.info.terrain(TERRAINS.FOREST());
            }

            @Override
            public int getAssignedWorkforceLimit(Region t) {
                return t.info.area() * 15;
            }
        };
    }

    public static RBooster POP_SCALING_MINABLE(RDBuilding bu, Minable minable) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return t.info.minableD(minable);
            }

            @Override
            public int getAssignedWorkforceLimit(Region t) {
                return 150;
            }
        };
    }

    public static void SLAVERY(RDBuilding bu, double from, double to) {
        for (RDRace rdRace : RD.RACES().all) {
            bu.boosters().push(new RBooster(new BSourceInfo(Dic.¤¤Population, rdRace.race.appearance().icon), from, to, true) {
                @Override
                public double get(Region t) {
                    return rdRace.pop.get(t) / 100.0;
                }

            }, RD.SLAVERY().boostable(rdRace));
        }
    }

    public static void FOOD_CONSUMER(RDBuilding bu) {
        for (RESOURCE food : RESOURCES.EDI().res()) {
            bu.boosters().push(new RBooster(new BSourceInfo(Dic.¤¤Food, food.icon()), 0, 1, false) {
                @Override
                public double get(Region t) {
                    if (!RD.FOOD_CONSUMPTION().has(t, food)) {
                        return 0.0;
                    }

                    int totalFoods = RD.FOOD_CONSUMPTION().getFoodTypeCount(t);
                    int totalPop = RD.RACES().population.get(t);
                    double foodConsumption = RD.FOOD_CONSUMPTION().booster.get(t);

                    LOG.ln("foodConsumption: " + foodConsumption + " totalPop: " + totalPop + " totalFoods: " + totalFoods);
                    return foodConsumption * totalPop / totalFoods;
                }

            }, RD.OUTPUT().get(food).boost);
        }
    }
}
