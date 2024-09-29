package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpec;
import init.sprite.UI.UI;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrPleGooEfficiencies {
    public static final CharSequence TOTAL_WORKFORCE_SPLIT = "TOTAL_WORKFORCE_SPLIT";
    public static final CharSequence TOTAL_WORKFORCE_SPLIT_DESC = "Work force split";

    public static void ROOM(INDUSTRY_HASER room, RDBuilding bu) {
        if(room == null)
            return;

        for (Industry industry : room.industries()) {
            for (Industry.IndustryResource industryResource : industry.outs()) {
                bu.baseFactors.add(
                    new RBooster(new BSourceInfo("Room type", industryResource.resource.icon()), industryResource.rate, industryResource.rate, true) {
                        @Override
                        public double get(Region t) {
                            return 1.0;
                        }
                    }.add(bu.efficiency));

                bu.baseFactors.add(
                    new RBooster(new BSourceInfo(TOTAL_WORKFORCE_SPLIT_DESC, UI.icons().s.citizen), 1.0, 1.0, false) {
                        @Override
                        public double get(Region t) {
                            return 1.0;
                        }
                    }.add(bu.efficiency));

                bu.baseFactors.add(
                    new RBooster(new BSourceInfo("Population", industryResource.resource.icon()), 1, 1000000, true) {
                        @Override
                        public double get(Region t) {
                            int totalPop = RD.RACES().population.get(t);
                            double workforce = RD.RACES().workforce.get(t);

                            double totalWorkforceSplit = 0;

                            for (RDBuilding building : RD.BUILDINGS().all)
                            {
                                if(building.level.get(t) == 0
                                    && RD.BUILDINGS().tmp().level(building, t) == 0)
                                {
                                    continue;
                                }

                                for(BoostSpec booster : building.baseFactors)
                                {
                                    if(booster.booster.info.name == TOTAL_WORKFORCE_SPLIT_DESC)
                                    {
                                        totalWorkforceSplit += booster.booster.vGet(t);
                                    }
                                }
                            }

                            totalWorkforceSplit = CLAMP.d(totalWorkforceSplit, 1, 1000000);

                            double workingPopulation = (double) totalPop / totalWorkforceSplit / 100 * workforce;
                            return CLAMP.d(workingPopulation / 1000000, 0.0, 1.0);
                        }
                    }.add(bu.efficiency));
            }
        }
    }
}
