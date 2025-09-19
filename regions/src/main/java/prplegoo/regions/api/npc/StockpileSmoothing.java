package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.trade.TradeManager;
import init.resources.RESOURCES;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.StockpileSmoothingData;
import snake2d.LOG;

public class StockpileSmoothing implements IDataPersistence<StockpileSmoothingData> {
    private double[][] currentTarget;

    public StockpileSmoothing() {
        initialize();
    }

    private void initialize() {
        currentTarget = new double[FACTIONS.MAX()][RESOURCES.ALL().size()];
    }

    @Override
    public String getKey() {
        return StockpileSmoothing.class.toString();
    }

    @Override
    public StockpileSmoothingData getData() {
        return new StockpileSmoothingData(currentTarget);
    }

    @Override
    public void putData(StockpileSmoothingData data) {
        if (data == null) {
            LOG.ln("StockpileSmoothing.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("StockpileSmoothing.onGameSaveLoaded: data found");
        if (currentTarget.length != data.currentTarget.length
                || currentTarget[0].length != data.currentTarget[0].length)
        {
            LOG.ln("StockpileSmoothing.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("StockpileSmoothing.onGameSaveLoaded: data found, writing");
        currentTarget = data.currentTarget;
    }

    @Override
    public Class<StockpileSmoothingData> getDataClass() {
        return StockpileSmoothingData.class;
    }

    public void reset(int index) {
        for (int resourceIndex = 0; resourceIndex < RESOURCES.ALL().size(); resourceIndex++) {
            currentTarget[index][resourceIndex] = 0;
        }
    }

    public void Update(FactionNPC faction, double deltaDays) {
        for (int resourceIndex = 0; resourceIndex < RESOURCES.ALL().size(); resourceIndex++) {
            int actualTarget = (int) actualTarget(faction, resourceIndex);

            if(actualTarget == currentTarget[faction.index()][resourceIndex]) {
                continue;
            }

            double delta = actualTarget - currentTarget[faction.index()][resourceIndex];
            if (delta < 0) {
                delta = Math.min(-50, delta) / 32;
            } else {
                delta = Math.max(50, delta) / 16;
            }

            double toAdd = delta * deltaDays;

            double toSet = currentTarget[faction.index()][resourceIndex] + toAdd;
            if (toSet > actualTarget && toAdd > 0) {
                toSet = actualTarget;
            } else if (toSet < TradeManager.MIN_LOAD*4) {
                toSet = TradeManager.MIN_LOAD*4;
            }

            currentTarget[faction.index()][resourceIndex] = toSet;
        }
    }

    public double getCurrentTarget(FactionNPC faction, int resourceIndex) {
        return currentTarget[faction.index()][resourceIndex];
    }

    public double actualTarget(FactionNPC faction, int resourceIndex) {
        double amTarget = KingLevels.getInstance().getDesiredStockpileAtLevel(faction, KingLevels.getInstance().getDesiredKingLevel(faction), RESOURCES.ALL().get(resourceIndex))
                - Math.min(0, KingLevels.getInstance().getDailyProductionRate(faction, RESOURCES.ALL().get(resourceIndex))) * 2;
        if (amTarget == 0) {
            // TODO: TOLERANCE as a stand in for curiosity or hoarding or something;
            amTarget = Math.max(amTarget, BOOSTABLES.NOBLE().TOLERANCE.get(faction.king().induvidual) * 0.9 * Math.pow(10, Math.sqrt(KingLevels.getInstance().getLevel(faction))) + 5);
        }

        amTarget *= 15;
        amTarget += KingLevels.getInstance().soldGoodsTracker.getSold(faction, resourceIndex);

        int tradeSets = (int) (amTarget / TradeManager.MIN_LOAD);
        return (tradeSets + 1) * TradeManager.MIN_LOAD;
    }
}


