package prplegoo.regions.api.region.rd;

import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.RDOptionalConsumptionData;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class RDOptionalConsumption implements IDataPersistence<RDOptionalConsumptionData> {
    private final double[][] rates;
    private boolean[][][] enabled;

    public RDOptionalConsumption(){
        rates = new double[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS][RESOURCES.ALL().size()];
        initialize();
    }

    @Override
    public String getKey() {
        return RDOptionalConsumptionData.class.toString();
    }

    @Override
    public RDOptionalConsumptionData getData() {
        return new RDOptionalConsumptionData(enabled);
    }

    @Override
    public void putData(RDOptionalConsumptionData data) {
        if (data == null) {
            LOG.ln("RDOptionalConsumption.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDOptionalConsumption.onGameSaveLoaded: data found");
        if (data.enabled == null
                || enabled.length != data.enabled.length
                || data.enabled[0] == null
                || enabled[0].length != data.enabled[0].length
                || data.enabled[0][0] == null
                || enabled[0][0].length != data.enabled[0][0].length)
        {
            LOG.ln("RDOptionalConsumption.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDOptionalConsumption.onGameSaveLoaded: data found, writing");
        enabled = data.enabled;
    }

    @Override
    public Class<RDOptionalConsumptionData> getDataClass() {
        return RDOptionalConsumptionData.class;
    }

    private void initialize() {
        enabled = new boolean[WORLD.REGIONS().all().size()][RD.BUILDINGS().all.size()][RESOURCES.ALL().size()];
    }

    public boolean isEnabled(Region region, int buildingIndex, int resourceIndex){
        return enabled[region.index()][buildingIndex][resourceIndex];
    }

    public void flip(Region region, int buildingIndex, int resourceIndex) {
        enabled[region.index()][buildingIndex][resourceIndex] = !enabled[region.index()][buildingIndex][resourceIndex];
    }

    public double getRate(int buildingIndex, int resourceIndex) {
        return rates[buildingIndex][resourceIndex];
    }

    public void putRate(int buildingIndex, int resourceIndex, double rate) {
        rates[buildingIndex][resourceIndex] = rate;
    }

    public boolean hasRate(RDBuilding building) {
        double[] buildingRates = rates[building.index()];
        for (double buildingRate : buildingRates) {
            if (buildingRate > 0) {
                return true;
            }
        }

        return false;
    }

    public static class RDOptionalConsumptionBooster extends BoosterValue {
        private final int buildingIndex;
        private final int resourceIndex;

        public RDOptionalConsumptionBooster(BValue v, BSourceInfo info, double to, int buildingIndex, int resourceIndex) {
            super(v, info, to, false);

            this.buildingIndex = buildingIndex;
            this.resourceIndex = resourceIndex;
        }

        @Override
        public double getValue(double input){
            return input;
        }

        public double getIfEnabled(Region reg, int buildingIndex, int resourceIndex){
            return RD.OPTIONAL_CONSUMPTION().isEnabled(reg, buildingIndex, resourceIndex) ? to() : 0;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return getIfEnabled((Region) o, buildingIndex, resourceIndex);
        }
    }
}
