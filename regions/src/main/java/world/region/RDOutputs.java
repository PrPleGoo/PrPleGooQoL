package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.time.TIME;
import init.resources.Growable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.trade.TR;
import init.trade.TRADABLE;
import init.trade.TRADE_TYPE;
import snake2d.util.MATH;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.pop.RDRace;
import world.region.updating.Shipper;

public final class RDOutputs {

    private static CharSequence ¤¤taxes = "¤Taxes";
    public static CharSequence ¤¤Squeeze = "¤Squeeze";
    public static CharSequence ¤¤SqueezeD = "¤Squeeze this settlement for what it's got. Instantly delivering goods and some extra denari, but upsetting the populace.";
    private static CharSequence ¤¤taxD = "¤Taxes are generated from your subjects. Higher tax rate increases taxes, but decreases loyalty.";

    static {
        D.ts(RDOutputs.class);
    }

    private INT_OE<Region> squeeze;

    public final LIST<RDOutput> ALL;
    public final RDOutput MONEY;
    public final LIST<RDResource> RES;

    public final double sqeezeAmountDays = 4.0;

    public RDOutputs(RDInit init) {

        new RD.RDOwnerChanger() {

            @Override
            public void change(Region reg, Faction oldOwner, Faction newOwner) {
                for (RDOutput r : ALL) {
                    r.yearlyAccumilation.set(reg, 0);
                }

            }
        };

        ArrayList<RDResource> rr = new ArrayList<>(TR.ALL().size());

        for (TRADABLE res : TR.ALL()) {
            rr.add(new RDResource(init, res));
        }
        this.RES = rr;

        squeeze = init.count.new DataShort("TAX_RATE", ¤¤Squeeze, ¤¤SqueezeD, 10);
        Boostable boost = BOOSTING.push("TAX_INCOME", 0, ¤¤taxes, ¤¤taxD, UI.icons().m.coins, BoostableCat.ALL().WORLD);
        MONEY = new RDOutput(boost, init);

        ALL = new ArrayList<RDOutputs.RDOutput>(0).join(MONEY).join(rr);

        ACTION a = new ACTION() {

            @Override
            public void exe() {
                RBooster b = new RBooster(new BSourceInfo(¤¤Squeeze, UI.icons().s.money), 1, 0.25, true) {

                    @Override
                    public double get(Region t) {
                        return squeeze.getD(t);
                    }
                };
                for (RDRace r : RD.RACES().all) {
                    b.add(r.loyalty.target);
                }
            }
        };

        BOOSTING.connecter(a);

        init.upers.add(new RDUpdatable() {

            @Override
            public void update(Region reg, double time) {
                time*= TIME.secondsPerDayI();
                int t = (int) time;
                if (RND.rFloat() < time-t)
                    t++;
                squeeze.inc(reg, -t);

            }

            @Override
            public void init(Region reg) {
                // TODO Auto-generated method stub

            }
        });

    }

    void init() {

    }

    public static class RDOutput {

        public final Boostable boost;
        public final Boostable boostYearlyPart;
        public final INT_OE<Region> yearlyAccumilation;

        RDOutput(Boostable boost, RDInit init) {
            this.boost = boost;
            this.boostYearlyPart = BOOSTING.push(boost.key.split("WORLD")[1]+"_YEARLY", 0, boost.name, ¤¤taxD, boost.icon, BoostableCat.ALL().WORLD_DUMP);
            yearlyAccumilation = init.count.new DataInt(boost.key + "_" + "ACC");
        }

        public int getDelivery(Region reg) {
            return (int) (boost.get(reg) + boostYearlyPart.get(reg));
        }

        public int loot(Region reg) {
            double d = 1.0-RD.DEVASTATION().current.getD(reg);

            return (int) (d* (boost.get(reg)+yearlyAccumilation.get(reg)));
        }

        public int daysUntilDailydelivery() {
            int d = 0;
            int now = TIME.days().bitsSinceStart()%(int)TIME.years().bitConversion(TIME.days());
            int remain = (int) MATH.ETA(now, d, (int)TIME.years().bitConversion(TIME.days()));
            return remain;
        }

    }

    public static class RDResource extends RDOutput{

        public final TRADABLE res;
        private final Growable g;

        RDResource(RDInit init, TRADABLE res) {
            super(BOOSTING.push("PRODUCTION_" + res.key(), 0, Dic.¤¤Production + ": " + res.names, res.desc, res.icon(),  BoostableCat.ALL().WORLD_PRODUCTION), init);
            this.res = res;

            g = g(res);


        }

        private Growable g(TRADABLE t) {
            for (Growable g : RESOURCES.growable().all()) {
                if (TR.get(g.resource) == t)
                    return g;
            }
            return null;
        }

        @Override
        public int daysUntilDailydelivery() {

            if (g != null) {
                int d = (int) (g.seasonalOffset*TIME.years().bitConversion(TIME.days()));
                int now = TIME.days().bitsSinceStart()%(int)TIME.years().bitConversion(TIME.days());
                int remain = (int) MATH.ETA(now, d, (int)TIME.years().bitConversion(TIME.days()));
                return remain;

            }

            return super.daysUntilDailydelivery();
        }

    }

    public RDResource get(TRADABLE res){
        return RES.get(res.index());
    }

    public TRADABLE fromBoost(Boostable bo) {
        if (bo.index() >= RES.get(0).boost.index() && bo.index() < RES.get(RES.size()-1).boostYearlyPart.index()) {

            return TR.ALL().get((bo.index()-RES.get(0).boost.index())/2);
        }
        return null;
    }

    public void squeze(Region reg) {
        Faction f = reg.faction();
        if (f == null)
            return;


        f.credits().inc(RD.OUTPUT().MONEY.boost.get(reg)*sqeezeAmountDays, CTYPE.TAX);
        squeeze.incD(reg, 0.5);

        Shipper shipper = RD.UPDATER().getShipper();
        shipper.setShipOnNextUpdate(reg);
    }


}
