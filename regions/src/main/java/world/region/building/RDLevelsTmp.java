package world.region.building;

import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuildPoints.RDBuildPoint;

public class RDLevelsTmp {

    Region reg;
    private final int[] levels;
    int active = 0;

    RDLevelsTmp(int am){
        levels = new int[am];
    }

    void init(Region reg) {
        this.reg = reg;
        Arrays.fill(levels, 0);
        for (int i = 0; i < RD.BUILDINGS().all.size(); i++) {
            RDBuilding b = RD.BUILDINGS().all.get(i);
            levels[b.index()] = b.level.get(reg);
        }
    }

    public int level(RDBuilding bu, Region reg) {
        if (active > 0 && reg == this.reg)
            return levels[bu.index()];
        return bu.level.get(reg);
    }

    public void levelSet(RDBuilding bu, int i) {
        levels[bu.index()] = i;
    }

    public boolean hasChange() {
        for (RDBuilding b : RD.BUILDINGS().all) {
            if (levels[b.index()] != b.level.get(reg))
                return true;
        }
        return false;
    }

    public int cost() {
        if (vi == VIEW.RI())
            return cc;
        vi = VIEW.RI();
        int am = 0;
        for (RDBuilding b : RD.BUILDINGS().all) {

            if (levels[b.index()] > b.level.get(reg))
                am += b.levels.get(levels[b.index()]).cost-b.levels.get(b.level.get(reg)).cost;

        }
        cc = am;
        return cc;
    }

    public void accept() {
        for (RDBuilding b : RD.BUILDINGS().all) {
            b.level.set(reg, levels[b.index()]);
        }
    }

    public boolean canAfford() {


        if (cost() > FACTIONS.player().credits().getD())
            return false;
        for (RDBuildPoint b : RD.BUILDINGS().costs.ALL) {
            if (b.bo == BOOSTABLES.CIVICS().GOV) {
                if (b.bo.get(reg.faction()) < 0) {
                    return false;
                }
            }
            else if (b.bo.get(reg) < 0) {
                return false;
            }
        }
        return true;

    }

    private int vi = -1;
    private int cc;

    public CharSequence canAfford(RDBuilding bu, Region reg, int level) {

        int lc = level(bu, reg);
        return bu.canAfford(reg, lc, level);

    }



}
