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
    private static final int MAX_ASSIGNABLE_WORKERS = 1000000;

    public static void POP_SCALING(RDBuilding bu) {
        bu.baseFactors.add(
            new RBooster(new BSourceInfo(TOTAL_WORKFORCE_SPLIT_DESC, UI.icons().s.citizen), 0, MAX_ASSIGNABLE_WORKERS, true) {
                private final RDBuilding building = bu;

                @Override
                public double get(Region t) {
                    double workforce = RD.RACES().workforce.get(t);

                    if(workforce <= 0){
                        return 0;
                    }

                    int assignedWorkersInRegion = RD.WORKERS().getTotal(t);

                    if(assignedWorkersInRegion <= 0){
                        return 0;
                    }

                    int assignedWorkersInThisBuilding = RD.WORKERS().get(t, building);

                    if(assignedWorkersInThisBuilding <= 0){
                        return 0;
                    }

                    int totalPop = RD.RACES().population.get(t);

                    if(totalPop <= 0){
                        return 0;
                    }

                    double assignableWorkforce = totalPop * workforce / 100;
                    double workerSplit = (double) assignedWorkersInThisBuilding / assignedWorkersInRegion;

                    double assignedWorkforce = assignableWorkforce * workerSplit;

                    return CLAMP.d(assignedWorkforce / MAX_ASSIGNABLE_WORKERS, 0.0, 1.0);
                }
            }.add(bu.efficiency));
    }
}
