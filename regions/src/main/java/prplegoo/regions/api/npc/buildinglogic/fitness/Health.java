package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import world.map.regions.Region;
import world.region.RD;

public class Health extends FitnessRecord {
    public Health(FactionNPC faction, int index) {
        super(faction, index);
    }

    @Override
    public double determineValue(FactionNPC faction, Region region) {
        return Math.min(RD.HEALTH().boostablee.get(region) - 0.5, 0.5);
    }

    @Override
    public double getRegionDeficitMax(FactionNPC faction) { return 0; }
}
