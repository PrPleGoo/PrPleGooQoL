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
import snake2d.util.rnd.RND;
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
    private static boolean isActive = false;

    private final KingLevel[] kingLevels;
    @Getter
    private final KingLevelRealmBuilder builder;

    private final int[][] productionCacheTick;
    private final double[][] productionCache;

    public StockpileSmoothing stockpileSmoothing;
    public SoldGoodsTracker soldGoodsTracker;
    public KingLevelIndexes kingLevelIndexes;

    public KingLevels() {
        instance = this;

        stockpileSmoothing = new StockpileSmoothing();
        soldGoodsTracker = new SoldGoodsTracker();
        kingLevelIndexes = new KingLevelIndexes();
        builder = new KingLevelRealmBuilder();

        PATHS.ResFolder prplegooResFolder = PATHS.STATS().folder("prplegoo");
        Json json = new Json(prplegooResFolder.init.get("NOBLE_LEVELS"));

        Json[] kingLevelJsons = json.jsons("LEVELS");
        kingLevels = new KingLevel[kingLevelJsons.length];

        for (int i = 0; i < kingLevelJsons.length; i++) {
            kingLevels[i] = new KingLevel(kingLevelJsons[i], i);
        }

        productionCacheTick = new int[FACTIONS.MAX()][RESOURCES.ALL().size()];
        productionCache = new double[FACTIONS.MAX()][RESOURCES.ALL().size()];
    }

    public static void setActive(boolean active) {
        isActive = active;
    }

    public void reset(int index) {
        stockpileSmoothing.reset(index);
        soldGoodsTracker.reset(index);
        kingLevelIndexes.reset(index);
    }

    public KingLevel getKingLevel(FactionNPC faction) {
        return kingLevels[getLevel(faction)];
    }

    public KingLevel getDesiredKingLevel(FactionNPC faction) {
        return kingLevels[Math.min(getLevel(faction) + 1, kingLevels.length - 1)];
    }

    public void consumeResources(FactionNPC faction, NPCStockpile npcStockpile, double deltaDays) {
        if (!isActive || !faction.isActive()) {
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

                for (int armyIndex = 0; armyIndex < faction.armies().all().size(); armyIndex++) {
                    WArmy army = faction.armies().all().get(armyIndex);
                    int armySupplyAmount = (int) Math.min(supply.needed(army), npcStockpile.amount(resource));
                    npcStockpile.inc(resource, -armySupplyAmount);
                    supply.current().inc(army, armySupplyAmount);
                }
            }

            double amountStored = npcStockpile.amount(resource.index());
            if (amountStored > 0) {
                npcStockpile.inc(resource, amountStored * -resource.degradeSpeed() / 16 / 2 / BOOSTABLES.CIVICS().SPOILAGE.get(faction));
            }
        }

        stockpileSmoothing.Update(faction, deltaDays);
        soldGoodsTracker.Update(faction, deltaDays);
    }

    // For getting amounts that KingLevels actually needs to handle consuming;
    private double getDailyConsumptionRateNotHandledElseWhere(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        double amount = 0;

        amount += kingLevel.getConsumption()[resource.index()];
        amount += kingLevel.getConsumptionCapitalPop()[resource.index()] * RD.RACES().population.get(faction.realm().capitol());

        for (int i = 0; i < RD.RACES().all.size(); i++) {
            amount += kingLevel.getConsumptionPreferredCapitalPop()[i][resource.index()] * RD.RACES().all.get(i).pop.get(faction.realm().capitol());
        }


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
                amount += supply.consumedPerDayCurrent(army);
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


    public double getDesiredStockpile(FactionNPC faction, RESOURCE resource) {
        return getDesiredStockpileAtLevel(faction, getKingLevel(faction), resource);
    }

    public double getDesiredStockpileAtLevel(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        double amount = getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource) * 16 * (kingLevel.getIndex() + 1) / 2;

        for (ADSupply supply : AD.supplies().get(resource)) {
            for (WArmy army : faction.armies().all()) {
                amount += supply.targetAmount(army) + supply.consumedPerDayTarget(army) * 16;
            }
        }

        return amount;
    }

    public int getLevel(FactionNPC faction) {
        return kingLevelIndexes.getLevel(faction);
    }

    public void resetLevels() {
        kingLevelIndexes.initialize();
    }

    public void pickMaxLevel(FactionNPC faction) {
        pickMaxLevel(faction, false);
    }

    public void pickMaxLevel(FactionNPC faction, boolean force) {
        if (!isActive) {
            return;
        }

        int currentYear = getCurrentYear();
        if (!force && kingLevelIndexes.getNextPickYear(faction) > currentYear) {
            return;
        }

        kingLevelIndexes.setNextPickYear(faction, currentYear + 1 + RND.rInt(4));

        int desiredLevel = getDesiredKingLevel(faction).getIndex();

        double pride = BOOSTABLES.NOBLE().PRIDE.get(faction.king().induvidual);

        for (int i = desiredLevel; i > 0; i--) {
            int missingResourceCount = 0;
            for (RESOURCE resource : RESOURCES.ALL()) {
                double amountConsumedBeforeNextCycle = getDesiredStockpileAtLevel(faction, kingLevels[i], resource);

                if (amountConsumedBeforeNextCycle > 0
                        // Prideful kings will be riskier with their ambition.
                        && faction.stockpile.amount(resource.index()) < amountConsumedBeforeNextCycle / pride) {
                    missingResourceCount++;

                    if (missingResourceCount >= 3) {
                        break;
                    }
                }
            }

            if (missingResourceCount < 3) {
                kingLevelIndexes.setLevel(faction, i);

                return;
            }
        }

        kingLevelIndexes.setLevel(faction, 0);
    }

    private static int getCurrentYear() {
        // FROM: public class DicTime
        return ((int) TIME.currentSecond()) / (int) TIME.years().bitSeconds();
    }
}
