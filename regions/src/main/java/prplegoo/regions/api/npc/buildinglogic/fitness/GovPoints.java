package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import world.region.RD;

public class GovPoints extends FitnessRecord {
    public GovPoints(FactionNPC faction, int index) {
        super(faction, index);
    }

    @Override
    public double determineValue(FactionNPC faction) {
        return Math.min(1, RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion()));
    }

    @Override
    public double getFactionDeficitMax(FactionNPC faction) { return -1; }
}

