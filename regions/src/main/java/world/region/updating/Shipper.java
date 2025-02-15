package world.region.updating;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.resources.Growable;
import init.resources.RESOURCES;
import init.type.HTYPES;
import prplegoo.regions.api.RDSlavery;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDResource;

final class Shipper {


    public Shipper() {

    }



    public void ship(Region r, double seconds) {

        Faction f = r.faction();

        if (f == null)
            return;

        if (r.besieged())
            return;

        if (false) {
            //fix farm expoit.
            //fix performance stuttering
        }

        if (f.capitolRegion() == null)
            return;

        double days = seconds*TIME.secondsPerDayI;
        int am = 0;

        if (f == FACTIONS.player()) {
            if (r.capitol())
                return;
        }

        f.credits().inc(RD.OUTPUT().MONEY.boost.get(r)*days, CTYPE.TAX);

        for (RDResource res : RD.OUTPUT().RES) {
            int a = (int) Math.ceil(res.boost.get(r)*days);
            am += Math.abs(a);
        }

        for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
            int a = rdSlave.hasDelivery(r, days);
            am += Math.abs(a);
        }

        if (am <= 0)
            return;

        Shipment c = WORLD.ENTITIES().caravans.create(r, f.capitolRegion(), ITYPE.tax);
        if (c != null) {
            for (RDResource res : RD.OUTPUT().RES) {
                int a = amount(res, r, seconds);
                if (a == 0)
                {
                    continue;
                }

                if (a > 0) {
                    if (f == FACTIONS.player()) {
                        for (ADSupply s : AD.supplies().get(res.res)) {
                            for (WArmy e : FACTIONS.player().armies().all()) {
                                if (!e.acceptsSupplies()) {
                                    continue;
                                }

                                int needed = s.needed(e);
                                if (needed < a) {
                                    a -= needed;
                                    s.current().set(e, s.current().get(e) + needed);
                                } else {
                                    s.current().set(e, s.current().get(e) + a);
                                    a = 0;
                                }
                            }
                        }
                    }

                    if (a == 0)
                    {
                        continue;
                    }

                    c.loadAndReserve(res.res, a);
                    if (f instanceof FactionNPC) {
                        ((FactionNPC) f).stockpile.inc(res.res, a);
                    }
                }
                else {
                    f.seller().remove(res.res, -a, ITYPE.tax);
                    if (f instanceof FactionNPC) {
                        ((FactionNPC) f).stockpile.inc(res.res, -a);
                    }
                }
            }

            for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
                int a = rdSlave.getDelivery(r, days);
                c.load(rdSlave.rdRace.race, a, HTYPES.PRISONER());
                if (f instanceof FactionNPC) {
                    ((FactionNPC) f).slaves().trade(rdSlave.rdRace.race, a, 0);
                }
            }
        }

    }

    private int amount(RDResource res, Region r, double seconds) {
        Growable g = RESOURCES.growable().get(res.res);
        if (g != null) {
            double year = TIME.secondsPerDay*TIME.years().bitConversion(TIME.days());
            double harvest = g.seasonalOffset*TIME.secondsPerDay*TIME.years().bitConversion(TIME.days());
            double now = TIME.currentSecond()%year;
            double before = (TIME.currentSecond()-seconds)%year;
            if (now >= harvest && before <= harvest || (now < harvest && before > harvest)) {
                return(int) Math.ceil(res.boost.get(r)*TIME.years().bitConversion(TIME.days()));
            }
            return 0;

        }else {
            return (int) Math.ceil(res.boost.get(r)*seconds*TIME.secondsPerDayI);
        }
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


}
