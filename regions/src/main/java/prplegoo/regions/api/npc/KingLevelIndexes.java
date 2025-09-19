package prplegoo.regions.api.npc;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.KingLevelIndexData;
import snake2d.LOG;

public class KingLevelIndexes implements IDataPersistence<KingLevelIndexData> {
    private byte[] npcLevels;
    private int[] nextPickYear;

    public KingLevelIndexes() {
        initialize();
    }

    public void initialize() {
        npcLevels = new byte[FACTIONS.MAX()];
        nextPickYear = new int[FACTIONS.MAX()];
    }

    @Override
    public String getKey() {
        return KingLevelIndexes.class.toString();
    }

    @Override
    public KingLevelIndexData getData() {
        return new KingLevelIndexData(npcLevels, nextPickYear);
    }

    @Override
    public void putData(KingLevelIndexData data) {
        if (data == null) {
            LOG.ln("KingLevelIndexes.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("KingLevelIndexes.onGameSaveLoaded: data found");
        if (npcLevels.length != data.npcLevels.length
                || nextPickYear.length != data.nextPickYear.length)
        {
            LOG.ln("KingLevelIndexes.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("KingLevelIndexes.onGameSaveLoaded: data found, writing");
        npcLevels = data.npcLevels;
        nextPickYear = data.nextPickYear;
    }

    @Override
    public Class<KingLevelIndexData> getDataClass() {
        return KingLevelIndexData.class;
    }

    public void reset(int index) {
        npcLevels[index] = 0;
        nextPickYear[index] = 0;
    }

    public int getLevel(FactionNPC faction) {
        return npcLevels[faction.index()];
    }

    public void setLevel(FactionNPC faction, int newLevel) {
        npcLevels[faction.index()] = (byte) newLevel;
    }

    public int getNextPickYear(FactionNPC faction) {
        return nextPickYear[faction.index()];
    }

    public void setNextPickYear(FactionNPC faction, int newNextPickYear) {
        nextPickYear[faction.index()] = newNextPickYear;
    }
}
