package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
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

import java.util.Arrays;

public class KingLevels {
    @Getter
    private static KingLevels instance;
    @Getter
    private static final boolean isActive = true;

    private final KingLevel[] kingLevels;
    private final int[] npcLevels;
    @Getter
    private final KingLevelRealmBuilder builder;

    private final int[][] productionCacheTick;
    private final double[][] productionCache;
    private final int[] lastYearPicked;

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
        productionCacheTick = new int[FACTIONS.MAX][RESOURCES.ALL().size()];
        productionCache = new double[FACTIONS.MAX][RESOURCES.ALL().size()];
        lastYearPicked = new int[FACTIONS.MAX];
    }

    public KingLevel getKingLevel(FactionNPC faction) {
        return kingLevels[getLevel(faction)];
    }

    public KingLevel getDesiredKingLevel(FactionNPC faction) {
        return kingLevels[Math.min(getLevel(faction) + 1, kingLevels.length - 1)];
    }

    public void consumeResources(FactionNPC faction, NPCStockpile npcStockpile, double deltaDays) {
        if (!isActive) {
            return;
        }

        KingLevel kingLevel = getKingLevel(faction);

        for (RESOURCE resource : RESOURCES.ALL()) {
            double amount = deltaDays * (getDailyProductionRate(faction, resource) - getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource));

            npcStockpile.inc(resource, amount);

            for (ADSupply supply : AD.supplies().get(resource)) {
                if (npcStockpile.amount(resource) <= 0) {
                    continue;
                }

                for (WArmy army : faction.armies().all()) {
                    if (!army.acceptsSupplies()
                        || npcStockpile.amount(resource) <= 0) {
                        continue;
                    }

                    int armySupplyAmount = Math.min(supply.needed(army), npcStockpile.amount(resource));
                    npcStockpile.inc(resource, -armySupplyAmount);
                    supply.current().inc(army, armySupplyAmount);
                }
            }

            npcStockpile.inc(resource, npcStockpile.amount(resource.index()) * -resource.degradeSpeed() / 16 / 2 / BOOSTABLES.CIVICS().SPOILAGE.get(faction));
        }
    }

    // For getting amounts that KingLevels actually needs to handle consuming;
    private double getDailyConsumptionRateNotHandledElseWhere(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        double amount = 0;

        amount += kingLevel.getConsumption()[resource.index()];
        amount += kingLevel.getConsumptionCapitalPop()[resource.index()] * RD.RACES().population.get(faction.realm().capitol());

        for (int i = 0; i < RD.RACES().all.size(); i ++) {
            amount += kingLevel.getConsumptionPreferredCapitalPop()[i][resource.index()] * RD.RACES().all.get(i).pop.get(faction.realm().capitol());
        }

        // TODO: ConsumptionPreferredDrink

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

        return amount;
    }

    public void resetDailyProductionRateCache(FactionNPC faction) {
        Arrays.fill(productionCacheTick[faction.index()], -1);
    }

    public double getDailyProductionRate(FactionNPC faction, RESOURCE resource) {
        int currentSecond = (int) TIME.currentSecond();
        if (productionCacheTick[faction.index()][resource.index()] == currentSecond) {
            return productionCache[faction.index()][resource.index()];
        }

        double amount = 0;

        for (Region region : faction.realm().all()) {
            amount += RD.OUTPUT().get(resource).boost.get(region);
        }

        productionCacheTick[faction.index()][resource.index()] = currentSecond;
        productionCache[faction.index()][resource.index()] = amount;

        return amount;
    }

    private double getDesiredStockpileAtLevel(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        return getDailyConsumptionRate(faction, kingLevel, resource) * FACTIONS.MAX;
    }

    public int getLevel(FactionNPC faction) {
        return npcLevels[faction.index()];
    }

    public void pickMaxLevel(FactionNPC faction) {
        pickMaxLevel(faction, false);
    }
 
    public void pickMaxLevel(FactionNPC faction, boolean force) {
        if (!isActive) {
            return;
        }

        int currentYear = getCurrentYear();
        if (!force && (currentYear % 4 != faction.index() % 4 || lastYearPicked[faction.index()] == currentYear) ) {
            return;
        }

        lastYearPicked[faction.index()] = currentYear;
        double pride = BOOSTABLES.NOBLE().PRIDE.get(faction.king().induvidual);

        for (int i = kingLevels.length - 1; i > 0; i--) {
            boolean skipped = false;
            for (RESOURCE resource : RESOURCES.ALL()) {
                double amountConsumedBeforeNextCycle = getDesiredStockpileAtLevel(faction, kingLevels[i], resource);

                if (amountConsumedBeforeNextCycle > 0
                        // Prideful kings will be riskier with their ambition.
                        && faction.stockpile.amount(resource.index()) < amountConsumedBeforeNextCycle / pride) {
                    skipped = true;
                    break;
                }
            }

            if (!skipped) {
                this.npcLevels[faction.index()] = i;
                return;
            }
        }

        this.npcLevels[faction.index()] = 0;
    }

    private static int getCurrentYear() {
        // FROM: public class DicTime
        return ((int) TIME.currentSecond()) / (int)TIME.years().bitSeconds();
    }
}
