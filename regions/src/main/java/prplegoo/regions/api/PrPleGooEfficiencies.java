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

                    return foodConsumption * totalPop / totalFoods;
                }

            }, RD.OUTPUT().get(food).boost);
        }
    }
}
