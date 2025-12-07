package world.region.updating;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.race.RACES;
import init.resources.RESOURCES;
import init.type.HTYPES;
import prplegoo.regions.api.RDSlavery;
import prplegoo.regions.persistence.IDataPersistence;
import prplegoo.regions.persistence.data.ShipperData;
import prplegoo.regions.persistence.data.ShipperData;
import snake2d.LOG;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDResource;

public final class Shipper implements IDataPersistence<ShipperData> {
    private double[] since;
    private double[][] resources;
    private int[][] slaves;

    private final int shipmentInterval;

    public Shipper() {
        this.shipmentInterval = TIME.secondsPerDay() * 16;

        initialize();
    }

    private void initialize(){

        since = new double[WORLD.REGIONS().all().size()];
        resources = new double[WORLD.REGIONS().all().size()][RESOURCES.ALL().size()];
        slaves = new int[WORLD.REGIONS().all().size()][RACES.all().size()];
    }

    public void ship(Region r, double seconds) {

        Faction f = r.faction();

        if (f == null)
            return;

        if (r.besieged())
            return;

        if (f.capitolRegion() == null)
            return;

		double days = seconds*TIME.secondsPerDayI();

        if (f == FACTIONS.player()) {
            if (r.capitol())
                return;
        }

        f.credits().inc(RD.OUTPUT().MONEY.boost.get(r)*days, CTYPE.TAX);

        if (f != FACTIONS.player()) {
            return;
        }

        for (RDResource res : RD.OUTPUT().RES) {
            int a = amount(f, res, r, seconds);

            if (a < 0) {
                a = RD.DEFICITS().handleDeficit(res.res, a);
            }

            if (a == 0)
            {
                continue;
            }

            resources[r.index()][res.res.index()] += a;
        }

        for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
            int a = rdSlave.getDelivery(r, days);
            slaves[r.index()][rdSlave.rdRace.race.index] += a;
        }

        since[r.index()] += seconds;

        if (!timeToShip(r) || !hasAnythingToShip(r)){
            return;
        }

        Shipment c = WORLD.ENTITIES().caravans.create(r, f.capitolRegion(), ITYPE.tax);
        if (c != null) {
            for (RDResource res : RD.OUTPUT().RES) {
                int a = (int) resources[r.index()][res.res.index()];

                c.loadAndReserve(res.res, a);

                resources[r.index()][res.res.index()] -= a;
            }

            for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
                int a = slaves[r.index()][rdSlave.rdRace.race.index()];

                c.load(rdSlave.rdRace.race, a, HTYPES.PRISONER());

                slaves[r.index()][rdSlave.rdRace.race.index()] -= a;
            }

            since[r.index()] = 0;
        }
    }

    public int daysUntilTaxes(Region region) {
        int secondsUntilShipment = shipmentInterval - (int) since[region.index()];

        int daysUntilShipment = secondsUntilShipment / TIME.secondsPerDay();

        return daysUntilShipment + 1;
    }

    private boolean timeToShip(Region region) {
        return since[region.index()] > shipmentInterval;
    }

    private boolean hasAnythingToShip(Region region) {
        for (RDResource res : RD.OUTPUT().RES) {
            if (resources[region.index()][res.res.index()] > 0) {
                return true;
            }
        }

        for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
            if (slaves[region.index()][rdSlave.rdRace.index()] > 0) {
                return true;
            }
        }

        return false;
    }

    public int getAccumulatedTaxes(Region region, RDResource resource){
        return (int) resources[region.index()][resource.res.index()];
    }

    public int getAccumulatedTaxes(Region region, RDSlavery.RDSlave rdSlave){
        return (int) slaves[region.index()][rdSlave.rdRace.index()];
    }

    private int amount(Faction f, RDResource res, Region r, double seconds) {
        return (int) Math.ceil(res.boost.get(r)*seconds*TIME.secondsPerDayI());
    }

    public void shipAll(Faction f, double days) {

        for (int ri = 0; ri < f.realm().regions(); ri++) {
            Region reg = f.realm().region(ri);
            for (RDResource res : RD.OUTPUT().RES) {
                int a = (int) Math.ceil(res.boost.get(reg)*days);
                if (a > 0) {
                    f.buyer().deliver(res.res, a, ITYPE.tax);
                }
            }

        }

    }

    @Override
    public String getKey() {
        return ShipperData.class.toString();
    }

    @Override
    public ShipperData getData() {
        return new ShipperData(since, resources, slaves);
    }

    @Override
    public void putData(ShipperData data) {
        if (data == null) {
            LOG.ln("Shipper.onGameSaveLoaded: data null, initializing");
            initialize();
            return;
        }

        LOG.ln("Shipper.onGameSaveLoaded: data found");
        if (since.length != data.since.length
                || resources.length != data.resources.length
                || slaves.length != data.slaves.length)
        {
            LOG.ln("Shipper.onGameSaveLoaded: data found, length difference detected, not writing");
            return;
        }

        LOG.ln("Shipper.onGameSaveLoaded: data found, writing");
        since = data.since;
        resources = data.resources;
        slaves = data.slaves;
    }

    @Override
    public Class<ShipperData> getDataClass() {
        return ShipperData.class;
    }
}
