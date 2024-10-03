package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpec;
import init.sprite.UI.UI;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrPleGooEfficiencies {
    public static final CharSequence TOTAL_WORKFORCE_SPLIT_DESC = "Population";

    public static void POP_SCALING(RDBuilding bu) {
        bu.baseFactors.add(
            new RBooster(new BSourceInfo(TOTAL_WORKFORCE_SPLIT_DESC, UI.icons().s.citizen), 1, 1000000, true) {
                @Override
                public double get(Region t) {
                    int totalPop = RD.RACES().population.get(t);
                    double workforce = RD.RACES().workforce.get(t);

                    double totalWorkforceSplit = 0;

                    for (RDBuilding building : RD.BUILDINGS().all)
                    {
                        if(RD.BUILDINGS().tmp().level(building, t) == 0)
                        {
                            continue;
                        }

                        for(BoostSpec booster : building.baseFactors)
                        {
                            if(booster.booster.info.name == TOTAL_WORKFORCE_SPLIT_DESC)
                            {
                                totalWorkforceSplit += RD.BUILDINGS().tmp().level(building, t);
                                break;
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
