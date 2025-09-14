package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import world.map.regions.Region;
import world.region.RD;

public class Loyalty extends FitnessRecord {
    public Loyalty(FactionNPC faction, int index) {
        super(faction, index);
    }
    @Override
    public double determineValue(FactionNPC faction, Region region) {
        double amount = 0;

        for (int i = 0; i < RD.RACES().all.size(); i++) {
            if (RD.RACES().edicts.massacre.toggled(RD.RACES().all.get(i)).get(region) == 1) {
                continue;
            }

            amount += Math.min(RD.RACES().all.get(i).loyalty.target.get(region), 0);
        }

        return amount;
    }
}
