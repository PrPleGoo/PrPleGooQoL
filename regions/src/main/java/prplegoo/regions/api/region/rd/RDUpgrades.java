package prplegoo.regions.api.region.rd;

import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.faction.FACTIONS;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.RDUpgradesData;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RDUpgrades implements IDataPersistence<RDUpgradesData> {
    public int[][] levels;

    public RDUpgrades() {
        initialize();
    }

    private void initialize(){
        levels = new int[WORLD.REGIONS().all().size()][RD.BUILDINGS().all.size()];
    }

    @Override
    public String getKey() {
        return RDUpgradesData.class.toString();
    }

    @Override
    public RDUpgradesData getData() {
        return new RDUpgradesData(levels);
    }

    @Override
    public void putData(RDUpgradesData data) {
        if (data == null) {
            LOG.ln("RDUpgrades.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDUpgrades.onGameSaveLoaded: data found");
        if (data.levels == null
                || levels.length != data.levels.length
                || data.levels[0] == null
                || levels[0].length != data.levels[0].length)
        {
            LOG.ln("RDUpgrades.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDUpgrades.onGameSaveLoaded: data found, writing");
        levels = data.levels;
    }

    @Override
    public Class<RDUpgradesData> getDataClass() {
        return RDUpgradesData.class;
    }

    public int getLevel(Region reg, int buildingIndex) {
        return levels[reg.index()][buildingIndex];
    }

    public void setLevel(Region region, int buildingIndex, int level) {
        if (level == 0 || RD.BUILDINGS().all.get(buildingIndex).getBlue().upgrades().requires(level).passes(FACTIONS.player())) {
            levels[region.index()][buildingIndex] = level;
        }
    }

    public static class RDUpgradeMaintenanceBooster extends BoosterValue {
        private final int buildingIndex;
        private final int level;

        public RDUpgradeMaintenanceBooster(BValue v, BSourceInfo info, double to, int buildingIndex, int level) {
            super(v, info, to, false);

            this.buildingIndex = buildingIndex;
            this.level = level;
        }

        @Override
        public double to(){
            return super.to();
        }

        @Override
        public double getValue(double input){
            return input;
        }

        public double getIfUpgradeLevel(Region reg, int buildingIndex){
            return RD.UPGRADES().getLevel(reg, buildingIndex) == level ? to() : 0;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return getIfUpgradeLevel((Region) o, buildingIndex);
        }
    }
}
