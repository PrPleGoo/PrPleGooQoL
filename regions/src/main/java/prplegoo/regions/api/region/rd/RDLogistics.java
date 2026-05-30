package prplegoo.regions.api.region.rd;

import game.boosting.*;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.RDLogisticsData;
import prplegoo.regions.persistence.data.RDLogisticsData;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.Arrays;

public class RDLogistics implements IDataPersistence<RDLogisticsData> {
    private int[][] electedResources;
    private double[][] logisticsCapacity;
    private int[] register;

    private final RDShipping[] shippings;

    public RDLogistics() {
        initialize();

        shippings = new RDShipping[RESOURCES.ALL().size()];
        for (RESOURCE resource : RESOURCES.ALL()) {
            shippings[resource.index()] = new RDShipping(resource);
        }
    }

    private void initialize() {
        logisticsCapacity = new double[RESOURCES.ALL().size()][WORLD.REGIONS().all().size()];
        register = new int[RESOURCES.ALL().size()];

        electedResources = new int[WORLD.REGIONS().all().size()][5];
        for (int[] electedResource : electedResources) {
            Arrays.fill(electedResource, -1);
        }
    }

    @Override
    public String getKey() {
        return RDLogisticsData.class.toString();
    }

    @Override
    public RDLogisticsData getData() {
        return new RDLogisticsData(electedResources, logisticsCapacity, register);
    }

    @Override
    public void putData(RDLogisticsData data) {
        if (data == null) {
            LOG.ln("RDLogistics.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDLogistics.onGameSaveLoaded: data found");
        if (data.electedResources == null
                || electedResources.length != data.electedResources.length
                || data.electedResources[0] == null
                || electedResources[0].length != data.electedResources[0].length
                || data.logisticsCapacity == null
                || logisticsCapacity.length != data.logisticsCapacity.length
                || data.logisticsCapacity[0] == null
                || logisticsCapacity[0].length != data.logisticsCapacity[0].length
                || data.register == null
                || register.length != data.register.length)
        {
            LOG.ln("RDLogistics.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDLogistics.onGameSaveLoaded: data found, writing");
        electedResources = data.electedResources;
        logisticsCapacity = data.logisticsCapacity;
        register = data.register;
    }

    @Override
    public Class<RDLogisticsData> getDataClass() {
        return RDLogisticsData.class;
    }

    public double handleLogistics(RESOURCE resource, double negativeAmount) {
        double positiveAmount = negativeAmount * -1;

        double[] logisticsCapacityForResource = logisticsCapacity[resource.index()];
        int currentRegister = register[resource.index()];

        for(int i = 0; i < logisticsCapacityForResource.length; i++) {
            int actualIndex = (i + currentRegister) % logisticsCapacityForResource.length;
            register[resource.index()] = actualIndex;

            if (positiveAmount <= logisticsCapacityForResource[actualIndex])
            {
                logisticsCapacityForResource[actualIndex] -= positiveAmount;

                return 0;
            } else {
                double canShip = logisticsCapacityForResource[actualIndex];
                logisticsCapacityForResource[actualIndex] -= canShip;
                positiveAmount -= canShip;
            }
        }

        return positiveAmount * -1.0;
    }

    public double getCappedLogistics(Region region, RESOURCE resource, double desiredShipment) {
        return desiredShipment - logisticsCapacity[resource.index()][region.index()];
    }

    public void addLogistics(Region region, RESOURCE resource, double amount) {
        logisticsCapacity[resource.index()][region.index()] += amount;
    }

    public RDShipping get(RESOURCE resource) {
        return shippings[resource.index()];
    }

    public boolean isElected(int regionIndex, int logisticsBuildingIndex, int resourceIndex) {
        return electedResources[regionIndex][logisticsBuildingIndex] == resourceIndex;
    }

    public RESOURCE findElected(int regionIndex, int logisticsBuildingIndex) {
        if (electedResources[regionIndex][logisticsBuildingIndex] == -1) {
            return null;
        }

        return RESOURCES.ALL().get(electedResources[regionIndex][logisticsBuildingIndex]);
    }

    public void elect(int regionIndex, int logisticsBuildingIndex, int resourceIndex) {
        electedResources[regionIndex][logisticsBuildingIndex] = resourceIndex;
    }

    public long getValue(RESOURCE res) {
        long sum = 0;
        for (int i = 0; i < logisticsCapacity[res.index()].length; i++) {
            sum += logisticsCapacity[res.index()][i];
        }

        return sum;
    }

    public static class RDShipping {

        public final Boostable boost;

        public final RESOURCE res;

        RDShipping(RESOURCE res) {
            this.boost = BOOSTING.push("RESOURCE_LOGISTICS_" + res.key, 0, "Logistics: " + res.names, res.desc, res.icon(),  BoostableCat.ALL().WORLD_PRODUCTION);
            this.res = res;
        }

        public double getDelivery(Region reg) {
            return Math.max(0, RD.LOGISTICS().getCappedLogistics(reg, res, boost.get(reg)));
        }
    }

    public static class RDEnabledShipmentBooster extends BoosterValue {
        private final int buildingIndex;
        private final int resourceIndex;

        public RDEnabledShipmentBooster(BValue v, BSourceInfo info, double to, int logisticsBuilding, int resourceIndex) {
            super(v, info, 0, to, false);

            this.buildingIndex = logisticsBuilding;
            this.resourceIndex = resourceIndex;
        }

        @Override
        public double getValue(double input){
            return input;
        }

        private double getIfEnabled(Region reg) {
            return RD.LOGISTICS().isElected(reg.index(), buildingIndex, resourceIndex) ? to() : 0;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            if(!(o instanceof Region)){
                return 0;
            }

            return getIfEnabled((Region) o);
        }
    }

    public static int getIndex(RDBuilding building) {
        String[] exploded = building.key().split("_");
        String ci = exploded[exploded.length - 1];

        return Integer.parseInt(ci) - 1;
    }
}
