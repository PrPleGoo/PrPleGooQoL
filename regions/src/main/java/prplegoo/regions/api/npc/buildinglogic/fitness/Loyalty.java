package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import snake2d.LOG;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public class Loyalty extends FitnessRecord {
    public Loyalty(FactionGenetic faction, int index) {
        super(faction, index);
    }
    @Override
    public double determineValue(FactionNPC faction, Region region) {
        double totalLoyalty = 0;
        double popCount = 0;

        for (int i = 0; i < RD.RACES().all.size(); i++) {
            RDRace race = RD.RACES().all.get(i);
            double popTarget = race.pop.target(region);
            totalLoyalty += popTarget * race.loyalty.target.get(region);
            popCount += popTarget;
        }

        if (popCount > 0) {
            totalLoyalty /= popCount;
        }

        return Math.min(totalLoyalty, 1.0);
    }

    @Override
    public double getRegionDeficitMax(FactionNPC faction) { return -0.1; }

    @Override
    public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
        return true;
    }
}
