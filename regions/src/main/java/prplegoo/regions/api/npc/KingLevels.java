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
import prplegoo.regions.api.RDSlavery;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

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
    private double playerScaling = 1;

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
        factionBuildingBonus = new double[FACTIONS.MAX()][RD.BUILDINGS().all.size()];
        Arrays.stream(factionBuildingBonus).forEach(x -> Arrays.fill(x, -1));

        getDesiredStockpileAtLevelCache = new double[FACTIONS.MAX()][kingLevels.length][RESOURCES.ALL().size()];
        getDesiredStockpileAtLevelCacheTimers = new double[FACTIONS.MAX()][kingLevels.length][RESOURCES.ALL().size()];
        Arrays.stream(getDesiredStockpileAtLevelCacheTimers).forEach(x -> Arrays.stream(x).forEach(y -> Arrays.fill(y, -61)));
    }

    public static void setActive(boolean active) {
        isActive = active;
    }

    public void reset(int index) {
        stockpileSmoothing.reset(index);
        soldGoodsTracker.reset(index);
        kingLevelIndexes.reset(index);
        Arrays.fill(factionBuildingBonus[index], -1);
    }

    public KingLevel getKingLevel(FactionNPC faction) {
        return kingLevels[getLevel(faction)];
    }

    public double getModifiedTechMul(RDBuilding building, FactionNPC faction) {
        return 1.0 + getModifiedTechD(building, faction);
    }

    public double getModifiedTechD(RDBuilding building, FactionNPC faction) {
        return (getCachedModifiedTechD(faction, building) - 1.0) * getKingLevel(faction).getTechApplied();
    }

    private final double[][] factionBuildingBonus;
    private double getCachedModifiedTechD(FactionNPC faction, RDBuilding building) {
        if (factionBuildingBonus[faction.index()][building.index()] == -1) {
            factionBuildingBonus[faction.index()][building.index()] = building.getBlue().bonus().get(faction);
        }

        return factionBuildingBonus[faction.index()][building.index()];
    }

    public KingLevel getDesiredKingLevel(FactionNPC faction) {
        return kingLevels[Math.min(getLevel(faction) + 1, kingLevels.length - 1)];
    }

    public void consumeResources(FactionNPC faction, NPCStockpile npcStockpile, double deltaDays) {
        if (!isActive || !faction.isActive()) {
            return;
        }

        // Can't do this on the ctor, and there's not really an update method besides this one.
        playerScaling = calculatePlayerScaling();

        KingLevel kingLevel = getKingLevel(faction);

        for (RESOURCE resource : RESOURCES.ALL()) {
            double amount = deltaDays * (getDailyProductionRate(faction, resource) - getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource));

            npcStockpile.inc(resource, amount);
            double actualStorage = npcStockpile.offset(resource);

            for (ADSupply supply : AD.supplies().get(resource)) {
                if (actualStorage <= 0) {
                    continue;
                }

                for (int armyIndex = 0; armyIndex < faction.armies().all().size(); armyIndex++) {
                    WArmy army = faction.armies().all().get(armyIndex);

                    int supplyAmountToMove = (int) Math.min(supply.needed(army), actualStorage);

                    npcStockpile.inc(resource, -supplyAmountToMove);
                    supply.current().inc(army, supplyAmountToMove);
                }
            }

            if (actualStorage > 0) {
                double spoilageAmount = actualStorage
                        // Compensated for year
                        * -resource.degradeSpeed() / 16
                        / 2 // Compensated for warehouse
                        // Compensated for tech
                        / BOOSTABLES.CIVICS().SPOILAGE.get(faction)
                        // Compensated for needing extra resources
                        / getPlayerScalingMul();

                npcStockpile.inc(resource, spoilageAmount);
            }
        }

        for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
            int a = rdSlave.getDelivery(faction, deltaDays);
            faction.slaves().trade(rdSlave.rdRace.race, a, 0);
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


        return amount * getPlayerScalingMul();
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
        int currentSecond = (int) (TIME.currentSecond() / 60);
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

    public double getDesiredStockpileAtNextLevel(FactionNPC faction, RESOURCE resource) {
        return getDesiredStockpileAtLevel(faction, getDesiredKingLevel(faction), resource);
    }

    private final double[][][] getDesiredStockpileAtLevelCache;
    private final double[][][] getDesiredStockpileAtLevelCacheTimers;

    public double getDesiredStockpileAtLevel(FactionNPC faction, KingLevel kingLevel, RESOURCE resource) {
        if (getDesiredStockpileAtLevelCacheTimers[faction.index()][kingLevel.getIndex()][resource.index()] + 60 > TIME.currentSecond()) {
            return getDesiredStockpileAtLevelCache[faction.index()][kingLevel.getIndex()][resource.index()];
        }

        double amount = getDailyConsumptionRateNotHandledElseWhere(faction, kingLevel, resource)
                // Stock two years' worth.
                * 32
                // Higher levels want bigger stocks.
                // * (kingLevel.getIndex() + 1) / 2
                // Scale for the player's performance, military equipment desire scales off fielded conscripts.
                * Math.min(KingLevels.getInstance().getPlayerScalingMul(), 40);

        for (ADSupply supply : AD.supplies().get(resource)) {
            for (WArmy army : faction.armies().all()) {
                // Keep enough stock to plop down another army
                amount += supply.targetAmount(army);

                // Add two years' worth consumption
                amount += supply.consumedPerDayTarget(army) * 32;
            }
        }

        getDesiredStockpileAtLevelCache[faction.index()][kingLevel.getIndex()][resource.index()] = amount;
        getDesiredStockpileAtLevelCacheTimers[faction.index()][kingLevel.getIndex()][resource.index()] = TIME.currentSecond();

        return getDesiredStockpileAtLevelCache[faction.index()][kingLevel.getIndex()][resource.index()];
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

    private static final int maxMissingResourcesForRankUp = 5;
    public void pickMaxLevel(FactionNPC faction, boolean force) {
        if (!isActive) {
            return;
        }

        int currentYear = getCurrentYear();
        if (!force && kingLevelIndexes.getNextPickYear(faction) > currentYear) {
            return;
        }

        kingLevelIndexes.setNextPickYear(faction, currentYear + 3 + RND.rInt(4));

        int desiredLevel = getDesiredKingLevel(faction).getIndex();

        double pride = BOOSTABLES.NOBLE().PRIDE.get(faction.king().induvidual);

        for (int i = desiredLevel; i > 0; i--) {
            int missingResourceCount = 0;
            for (RESOURCE resource : RESOURCES.ALL()) {
                double amountConsumedBeforeNextCycle = getDesiredStockpileAtLevel(faction, kingLevels[i], resource);

                if (amountConsumedBeforeNextCycle > 0
                        // Prideful kings will be riskier with their ambition.
                        && faction.stockpile.offset(resource) < amountConsumedBeforeNextCycle / pride) {
                    missingResourceCount++;

                    if (missingResourceCount >= maxMissingResourcesForRankUp) {
                        break;
                    }
                }
            }

            if (missingResourceCount < maxMissingResourcesForRankUp) {
                kingLevelIndexes.setLevel(faction, i);

                return;
            }
        }

        kingLevelIndexes.setNextPickYear(faction, currentYear + 1);
        kingLevelIndexes.setLevel(faction, 0);
    }

    private static int getCurrentYear() {
        // FROM: public class DicTime
        return ((int) TIME.currentSecond()) / (int) TIME.years().bitSeconds();
    }

    public double getPlayerScalingD() {
        return playerScaling;
    }

    public double getPlayerScalingMul() {
        return playerScaling + 1.0;
    }

    private double calculatePlayerScaling() {
        int scalePercentage = 0;
        int playerRegionCount = FACTIONS.player().realm().regions() - 1;
        while (playerRegionCount > 0) {
            scalePercentage += playerRegionCount;
            playerRegionCount -= 5;
        }

        return (double) scalePercentage / 100.0;
    }
}
