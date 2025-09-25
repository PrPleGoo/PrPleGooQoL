package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import world.map.regions.Region;
import world.region.RD;

public class Workforce extends FitnessRecord {
    public Workforce(FactionGenetic faction, int index) {
        super(faction, index);
    }

    @Override
    public double determineValue(FactionNPC faction, Region region) {
        return Math.min(10, RD.SLAVERY().getWorkforce().bo.get(region));
    }

    @Override
    public double getRegionDeficitMax(FactionNPC faction) { return 0; }
}
