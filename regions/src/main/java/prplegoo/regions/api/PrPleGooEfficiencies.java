package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import settlement.room.industry.module.Industry;
import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.pop.RDRaces;

public class PrPleGooEfficiencies {

    public static BoostSpec[] ROOM(Industry.IndustryResource industryResource, Boostable efficiency) {
        return new BoostSpec[] {
            new RBooster(new BSourceInfo("Room type", industryResource.resource.icon()), industryResource.rate, industryResource.rate, true) {

                @Override
                public double get(Region t) {
                    return 1.0;
                }
            }.add(efficiency),
            new RBooster(new BSourceInfo("Population", industryResource.resource.icon()), 1, 1000000, true) {
                @Override
                public double get(Region t) {
                    int totalPop = RD.RACES().population.get(t);
                    double workforce = RD.RACES().workforce.get(t);

                    double workingPopulation = (double) totalPop / 100 * workforce;

                    return CLAMP.d(workingPopulation / 1000000, 0.0, 1.0);
                }
            }.add(efficiency),
        };
    }
}
