package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import settlement.room.industry.module.Industry;
import world.map.regions.Region;
import world.region.RBooster;

public class PrPleGooEfficiencies {

    public static RBooster ROOM(Industry.IndustryResource industryResource) {
        return new RBooster(new BSourceInfo("Room type", industryResource.resource.icon()), industryResource.rate, industryResource.rate, true) {

            @Override
            public double get(Region t) {
                return 1.0;
            }
        };
    }
}
