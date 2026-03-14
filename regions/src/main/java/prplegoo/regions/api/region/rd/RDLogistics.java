package prplegoo.regions.api.region.rd;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import world.WORLD;
import world.map.regions.Region;

public class RDLogistics {
    private double[][] logisticsCapacity;
    private int[] register;

    private final RDShipping[] shippings;

    public RDLogistics() {
        initialize();

        shippings = new RDShipping[RESOURCES.ALL().size()];
        for(RESOURCE resource : RESOURCES.ALL()) {
            shippings[resource.index()] = new RDShipping(resource);
        }
    }

    private void initialize() {
        logisticsCapacity = new double[RESOURCES.ALL().size()][WORLD.REGIONS().all().size()];
        register = new int[RESOURCES.ALL().size()];
    }

    public int handleLogistics(RESOURCE resource, int negativeAmount) {
        int positiveAmount = negativeAmount * -1;

        double[] logisticsCapacityForResource = logisticsCapacity[resource.index()];
        int currentRegister = register[resource.index()];

        for(int i = 0; i < logisticsCapacityForResource.length; i++) {
            int actualIndex = (i + currentRegister) % logisticsCapacityForResource.length;
            register[resource.index()] = i;

            if (positiveAmount <= logisticsCapacityForResource[actualIndex])
            {
                logisticsCapacityForResource[actualIndex] -= positiveAmount;

                return 0;
            } else {
                int canShip = (int) logisticsCapacityForResource[actualIndex];
                logisticsCapacityForResource[actualIndex] -= canShip;
                positiveAmount -= canShip;
            }
        }

        return positiveAmount * -1;
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

    public static class RDShipping {

        public final Boostable boost;

        public final RESOURCE res;

        RDShipping(RESOURCE res) {
            this.boost = BOOSTING.push("RESOURCE_LOGISTICS_" + res.key, 0, "Logistics: " + res.names, res.desc, res.icon(),  BoostableCat.ALL().WORLD_PRODUCTION);
            this.res = res;
        }

        public double getDelivery(Region reg) {
            return boost.get(reg);
        }
    }
}
