package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
import init.RES;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import snake2d.util.file.Json;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

public class KingLevels {
    @Getter
    private static KingLevels instance;
    @Getter
    private static final boolean isActive = true;

    private final KingLevel[] kingLevels;
    private final int[] npcLevels;
    @Getter
    private final KingLevelRealmBuilder builder;

    public KingLevels() {
        instance = this;
        builder = new KingLevelRealmBuilder();

        PATHS.ResFolder prplegooResFolder = PATHS.STATS().folder("prplegoo");
        Json json = new Json(prplegooResFolder.init.get("NOBLE_LEVELS"));

        Json[] kingLevelJsons = json.jsons("LEVELS");
        kingLevels = new KingLevel[kingLevelJsons.length];

        for (int i = 0; i < kingLevelJsons.length; i++) {
            kingLevels[i] = new KingLevel(kingLevelJsons[i]);
        }

        npcLevels = new int[FACTIONS.MAX];
    }

    public int getLevel(Faction faction) {
        if (!(faction instanceof FactionNPC)) {
            return -1;
        }

        return npcLevels[faction.index()];
    }

    public int getLevel(FactionNPC faction) {
        return npcLevels[faction.index()];
    }

    public KingLevel getKingLevel(Faction faction) {
        if (!(faction instanceof FactionNPC)) {
            return null;
        }

        return kingLevels[getLevel(faction)];
    }

    public KingLevel getKingLevel(FactionNPC faction) {
        return kingLevels[getLevel(faction)];
    }

    public void consumeResources(FactionNPC faction, NPCStockpile npcStockpile, double deltaDays) {
        if (!isActive) {
            return;
        }

        KingLevel kingLevel = getKingLevel(faction);

        for (RESOURCE resource : RESOURCES.ALL()) {
            double amountConsumed = getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource) * deltaDays;

            npcStockpile.inc(resource, -amountConsumed);

            for (ADSupply supply : AD.supplies().get(resource)) {
                if (npcStockpile.amount(resource) <= 0) {
                    continue;
                }

                for (WArmy army : faction.armies().all()) {
                    if (!army.acceptsSupplies()
                        || npcStockpile.amount(resource) <= 0) {
                        continue;
                    }

                    double amount = Math.min(supply.needed(army), npcStockpile.amount(resource));
                    npcStockpile.inc(resource, -amount);
                }
            }
        }
    }

    // For getting amounts that KingLevels actually needs to handle consuming;
    private double getDailyConsumptionRateNotHandledElseWhere(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        double amount = 0;

        amount += kingLevel.getConsumption()[resource.index()];
        amount += kingLevel.getConsumptionCapitalPop()[resource.index()] * RD.RACES().population.get(faction.realm().capitol());

        // TODO: ConsumptionPreferredFood
        // TODO: ConsumptionFurniture
        // TODO: ConsumptionPreferredDrink
        // TODO: Apply spoilage

        return amount;
    }

    // For getting amounts that the empire will consume;
    public double getDailyConsumptionRate(FactionNPC faction, RESOURCE resource) {
        return getDailyConsumptionRate(faction, getKingLevel(faction), resource);
    }


    private double getDailyConsumptionRate(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        double amount = getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource);

        for (ADSupply supply : AD.supplies().get(resource)) {
            for (WArmy army : faction.armies().all()) {
                amount += supply.usedPerDay * supply.used().get(army);
            }
        }

        for (Region region : faction.realm().all()) {
            amount -= RD.OUTPUT().get(resource).boost.get(region);
        }

        return amount;
    }

    private double getDesiredStockpileAtLevel(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        return getDailyConsumptionRate(faction, kingLevel, resource) * FACTIONS.MAX;
    }

    public double getDesiredStockpile(FactionNPC faction, RESOURCE resource) {
        return getDesiredStockpileAtLevel(faction, getKingLevel(faction), resource);
    }

    public void pickMaxLevel(FactionNPC faction) {
        pickMaxLevel(faction, false);
    }

    public void pickMaxLevel(FactionNPC faction, boolean force) {
        if (!isActive) {
            return;
        }

        if (force || getCurrentYear() % FACTIONS.MAX != faction.index()){
            return;
        }

        for (int i = kingLevels.length - 1; i >= 0; i--) {
            for (RESOURCE resource : RESOURCES.ALL()) {
                double amountConsumedBeforeNextCycle = getDesiredStockpileAtLevel(faction, kingLevels[i], resource);

                if (amountConsumedBeforeNextCycle > 0
                        // Prideful kings will be riskier with their ambition.
                        && faction.stockpile.amount(resource.index()) < amountConsumedBeforeNextCycle / BOOSTABLES.NOBLE().PRIDE.get(faction.king().induvidual)) {
                    continue;
                }

                this.npcLevels[faction.index()] = i;
                return;
            }
        }

        this.npcLevels[faction.index()] = 0;
    }

    private static int getCurrentYear() {
        // FROM: public class DicTime
        return ((int) TIME.currentSecond() % (int)TIME.years().cycleSeconds())  / (int)TIME.years().bitSeconds();
    }
}
