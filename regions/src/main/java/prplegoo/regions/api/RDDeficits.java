package prplegoo.regions.api;

import game.faction.FACTIONS;
import game.faction.FResources;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.RDDeficitData;
import settlement.room.industry.module.Industry;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.HashMap;

public class RDDeficits implements IDataPersistence<RDDeficitData> {
    private static final double timer = TIME.secondsPerDay;
    private int[] deficits;
    private int[] oldDeficits;
    private int[] supplies;
    private int[] unresolvedDeficits;
    private double since = 0;

    public RDDeficits() {
        initialize();
    }

    private void initialize() {
        deficits = new int[RESOURCES.ALL().size()];
        oldDeficits = new int[RESOURCES.ALL().size()];
        supplies = new int[RESOURCES.ALL().size()];
        unresolvedDeficits = new int[RESOURCES.ALL().size()];
    }

    @Override
    public String getKey() {
        return RDDeficitData.class.toString();
    }

    @Override
    public RDDeficitData getData() {
        return new RDDeficitData(deficits, oldDeficits, supplies, unresolvedDeficits, since);
    }

    @Override
    public void putData(RDDeficitData data) {
        if (data == null) {
            LOG.ln("RDDeficits.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("RDDeficits.onGameSaveLoaded: data found");
        if (deficits.length != data.deficits.length
                || data.oldDeficits == null
                || oldDeficits.length != data.oldDeficits.length
                || data.supplies == null
                || supplies.length != data.supplies.length
                || unresolvedDeficits.length != data.unresolvedDeficits.length)
        {
            LOG.ln("RDDeficits.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("RDDeficits.onGameSaveLoaded: data found, writing");
        deficits = data.deficits;
        oldDeficits = data.oldDeficits;
        supplies = data.supplies;
        unresolvedDeficits = data.unresolvedDeficits;
        since = data.since;
    }

    @Override
    public Class<RDDeficitData> getDataClass() {
        return RDDeficitData.class;
    }

    public void update(double timeSinceLast) {
        since += timeSinceLast;
        if (since > timer) {
            since -= timer;

            tryResolveDeficits();
        }
    }

    private void tryResolveDeficits() {
        Faction player = FACTIONS.player();

        HashMap<Integer, Shipment> caravanByRegionIndex = new HashMap<>();
        for (int i = 0; i < RESOURCES.ALL().size(); i++) {
            RESOURCE resource = RESOURCES.ALL().get(i);

            int toResolve = Math.min(Math.abs(oldDeficits[i]), supplies[i]);

            if (toResolve > 0) {
                Region caravanDestination = findRandomRegionConsuming(resource);
                if (caravanDestination != null) {
                    if (!caravanByRegionIndex.containsKey(caravanDestination.index())) {
                        caravanByRegionIndex.put(caravanDestination.index(), WORLD.ENTITIES().caravans.createActualDest(player.capitolRegion(), caravanDestination, ITYPE.tax));
                    }

                    Shipment caravan = caravanByRegionIndex.get(caravanDestination.index());
                    caravan.loadAlreadyReserved(resource, toResolve);
                }

                player.res().dec(resource, FResources.RTYPE.TAX, toResolve);
                oldDeficits[i] += toResolve;
                supplies[i] -= toResolve;
            }

            unresolvedDeficits[i] = oldDeficits[i];

            oldDeficits[i] += deficits[i];
            deficits[i] = 0;
        }
    }

    private Region findRandomRegionConsuming(RESOURCE resource) {
        int regionCount = FACTIONS.player().realm().regions();
        int random = RND.rInt(regionCount);
        for (int i = 0; i < regionCount; i++) {
            Region region = FACTIONS.player().realm().all().get((random + i) % regionCount);
            if (region.capitol()) {
                continue;
            }

            if (RD.OUTPUT().get(resource).boost.get(region) < 0) {
                return region;
            }
        }

        return null;
    }

    public int handleDeficit(RESOURCE resource, int amount) {
        if (amount < 0) {
            deficits[resource.index()] += amount;

            return 0;
        }

        return amount;
    }

    public double getDeficitModifier(RESOURCE resource) {
        if (unresolvedDeficits[resource.index()] >= -80) {
            return 1;
        }

        double shortage = (1000 + unresolvedDeficits[resource.index()]);

        double result = CLAMP.d(shortage / 1000.0, 0, 1);

        if (result < 0.1) {
            return 0;
        }

        return result;
    }

    public double getWorstDeficit(Region region, RDBuilding building, LIST<Industry> industries) {
        Industry selectedRecipe = industries.get(RD.RECIPES().getRecipeIndex(region, building.index(), building.getBlue()));

        double worst = 1;
        for (Industry.IndustryResource resource : selectedRecipe.ins()) {
            double deficit = RD.DEFICITS().getDeficitModifier(resource.resource);
            if (deficit < worst) {
                worst = deficit;
            }
        }

        return worst;
    }

    public int getOutstandingDeficit(RESOURCE resource) {
        return -(deficits[resource.index()] + oldDeficits[resource.index()]) - supplies[resource.index()];
    }

    public int getDeficit(RESOURCE resource) {
        return deficits[resource.index()] + oldDeficits[resource.index()];
    }

    public int getSupplies(RESOURCE resource) {
        return supplies[resource.index()];
    }

    public void addSupplies(RESOURCE resource, int amount) {
        supplies[resource.index()] += amount;
    }

    public double getFoodDeficit(Region region) {
        double amTotal = 0;

        for(RESOURCE resource : RESOURCES.EDI().res()) {
            if (RD.FOOD_CONSUMPTION().has(region, resource)) {
                amTotal += getDeficitModifier(resource);
            }
        }

        return CLAMP.d(amTotal / RD.FOOD_CONSUMPTION().getFoodTypeCount(region), 0, 1);
    }
}
