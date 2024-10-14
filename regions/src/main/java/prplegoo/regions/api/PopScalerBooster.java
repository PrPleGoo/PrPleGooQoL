package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;

public abstract class PopScalerBooster extends RBooster {
    public static final CharSequence TOTAL_WORKFORCE_SPLIT_DESC = "Allocated workers:";
    private static final int MAX_ASSIGNABLE_WORKERS = 1000000;

    private final RDBuilding building;

    public PopScalerBooster(RDBuilding building) {
        super(new BSourceInfo(TOTAL_WORKFORCE_SPLIT_DESC, UI.icons().s.citizen), 0, MAX_ASSIGNABLE_WORKERS, true);

        this.building = building;
    }

    public abstract double getLimitScaler(Region t);

    public int getAssignedWorkforceLimit(Region t) {
        return -1;
    }

    @Override
    public double get(Region t) {
        // Sometimes we get a wrong result on the first get (´･ω･`)?
        RD.RACES().workforce.get(t);
        double workforce = RD.RACES().workforce.get(t);

        if (workforce <= 0) {
            return 0;
        }

        int assignedWorkersInRegion = RD.WORKERS().getTotal(t);

        if (assignedWorkersInRegion <= 0) {
            return 0;
        }

        int assignedWorkersInThisBuilding = RD.WORKERS().get(t, building);

        if (assignedWorkersInThisBuilding <= 0) {
            return 0;
        }

        int totalPop = RD.RACES().population.get(t);

        if (totalPop <= 0) {
            return 0;
        }

        double assignableWorkforce = totalPop * workforce / 100;
        double workerSplit = (double) assignedWorkersInThisBuilding / assignedWorkersInRegion;

        double assignedWorkforce = assignableWorkforce * workerSplit;

        if (getAssignedWorkforceLimit(t) != -1) {
            assignedWorkforce = correctWorkerSplit(assignedWorkforce, assignableWorkforce, assignedWorkersInThisBuilding, assignedWorkersInRegion, t);
        }

        return CLAMP.d(assignedWorkforce, 0.0, MAX_ASSIGNABLE_WORKERS);
    }

    private double correctWorkerSplit(double assignedWorkforce, double assignableWorkforce, int assignedWorkersInThisBuilding, int assignedWorkersInRegion, Region region) {
        double maxAssignableWorkers = getAssignedWorkforceLimit(region) * getLimitScaler(region);
        if (maxAssignableWorkers > assignedWorkforce) {
            return assignedWorkforce;
        }

        if(assignedWorkersInRegion == assignedWorkersInThisBuilding){
            return maxAssignableWorkers;
        }

        int x = 1;

        while(assignedWorkforce > maxAssignableWorkers && x < assignedWorkersInThisBuilding){
            assignedWorkforce = assignableWorkforce * (assignedWorkersInThisBuilding - x) / (assignedWorkersInRegion - x);
            x++;
        }

        RD.WORKERS().set(region, building, assignedWorkersInThisBuilding - x + 1);

        return maxAssignableWorkers;
    }
}
