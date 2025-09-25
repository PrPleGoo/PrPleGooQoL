package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import snake2d.LOG;
import world.map.regions.Region;
import world.region.RD;

public class Health extends FitnessRecord {
    public Health(FactionGenetic faction, int index) {
        super(faction, index);
    }

    @Override
    public double determineValue(FactionNPC faction, Region region) {
        return RD.HEALTH().boostablee.get(region);
    }

    @Override
    public double getRegionDeficitMax(FactionNPC faction) { return 0.5; }

    @Override
    public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
        return true;
    }
}

