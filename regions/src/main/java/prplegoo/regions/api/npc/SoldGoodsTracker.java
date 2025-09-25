package prplegoo.regions.api.npc;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCES;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.KingLevelIndexData;
import prplegoo.regions.persistence.data.SellingData;
import snake2d.LOG;

public class SoldGoodsTracker implements IDataPersistence<SellingData> {
    private double[][] sold;

    public SoldGoodsTracker() {
        initialize();
    }

    private void initialize() {
        sold = new double[FACTIONS.MAX()][RESOURCES.ALL().size()];
    }

    @Override
    public String getKey() {
        return SoldGoodsTracker.class.toString();
    }

    @Override
    public SellingData getData() {
        return new SellingData(sold);
    }

    @Override
    public void putData(SellingData data) {
        if (data == null) {
            LOG.ln("SoldGoodsTracker.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("SoldGoodsTracker.onGameSaveLoaded: data found");
        if (sold.length != data.sold.length
                || sold[0].length != data.sold[0].length)
        {
            LOG.ln("SoldGoodsTracker.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("SoldGoodsTracker.onGameSaveLoaded: data found, writing");
        sold = data.sold;
    }

    @Override
    public Class<SellingData> getDataClass() {
        return SellingData.class;
    }

    public void reset(int index) {
        for (int resourceIndex = 0; resourceIndex < RESOURCES.ALL().size(); resourceIndex++) {
            sold[index][resourceIndex] = 0;
        }
    }

    public void Update(FactionNPC faction, double deltaDays) {
        for (int resourceIndex = 0; resourceIndex < RESOURCES.ALL().size(); resourceIndex++) {
            double delta = sold[faction.index()][resourceIndex] / 32;

            double toSubtract = delta * deltaDays;

            double toSet = Math.max(0, sold[faction.index()][resourceIndex] - toSubtract);

            sold[faction.index()][resourceIndex] = toSet;
        }
    }

    public void processTraded(FactionNPC faction, int resourceIndex, int amount) {
        sold[faction.index()][resourceIndex] += amount;
    }

    public double getSold(FactionNPC faction, int resourceIndex) {
        return sold[faction.index()][resourceIndex];
    }
}

