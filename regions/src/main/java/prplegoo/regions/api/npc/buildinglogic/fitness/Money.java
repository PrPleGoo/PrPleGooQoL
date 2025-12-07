package prplegoo.regions.api.npc.buildinglogic.fitness;

import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import world.region.RD;

public class Money extends FitnessRecord {
    public Money(FactionGenetic faction, int index) {
        super(faction, index);
    }

    @Override
    public double determineValue(FactionNPC faction) {
        double totalMoney = RD.OUTPUT().MONEY.boost.get(faction) * TIME.secondsPerDay() * 2;

        for (RESOURCE resource : RESOURCES.ALL()) {
            double price = faction.stockpile.price.get(resource);

            double productionAmount = KingLevels.getInstance().getDailyProductionRate(faction, resource);

            totalMoney += productionAmount * price;
        }

        return totalMoney;
    }
}
