package game.faction.npc;

import java.io.IOException;

import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.FCredits;
import game.faction.FCredits.CTYPE;
import game.faction.FResources;
import game.faction.Faction;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import game.time.TIME;
import init.race.Race;
import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import world.army.AD;
import world.region.RD;
import world.region.pop.RDRace;

public final class FactionNPC extends Faction implements BOOSTABLE_O{

    private final ArrayListGrower<NPCResource> res = new ArrayListGrower<>();

    public final Str nameIntro = new Str(64);
    private final NPCCourt court = new NPCCourt(this, res);
    private final FBanner banner = new FBanner(this);
    private final TradeNPC trade = new TradeNPC(this);
    private final FResources stats = new FResources(4, TIME.years()) {

        @Override
        public int getAvailable(RESOURCE t) {
            return stockpile.amount(t);
        }

    };
    private final FCredits credits = new FCredits(4, TIME.years());
    public final NPCBonus bonus = new NPCBonus(this, res);
    public final NPCStockpile stockpile = new NPCStockpile(this, res, credits);
    private final NPCSlaves slaves = new NPCSlaves(this);
    public final NPCRequest request = new NPCRequest(this);
    private int iteration;
    public boolean sanctified = false;

    public FactionNPC(LISTE<Faction> all, UpdaterNPC up){
        super(all);
    }

    public void generate(RDRace pref, boolean init) {

        court.init();
        sanctified = false;
        if (pref == null) {
            pref = RD.RACES().all.rnd();
            if (capitolRegion() != null) {
                double pop = RD.RACES().popTarget.getValue(capitolRegion());
                pop *= RND.rFloat();
                for (RDRace r : RD.RACES().all) {
                    pop -= r.pop.target(capitolRegion());
                    if (pop <= 0) {
                        pref = r;
                        break;
                    }

                }
            }

        }

        nameIntro.clear().add(pref.names.intros.next());
        name.clear().add(pref.names.fNames.next());
        nameFix(pref);
        event = false;


        if (realm().capitol() != null) {
            realm().capitol().info.name().clear().add(name);
            iteration++;

            credits.inc(-credits.getD(), CTYPE.DIPLOMACY);

            for (NPCResource r : res) {
                r.generate(pref, this, init);
            }
            slaves.init();
        }

    }

    private void nameFix(RDRace pref) {
        for (int d = 0; d < 100; d++) {
            for (int i = 0; i < FACTIONS.NPCs().size(); i++) {
                FactionNPC fo = FACTIONS.NPCs().get(i);
                if (fo.isActive() && fo != this && fo.name.equals(name)) {
                    name.clear().add(pref.names.fNames.next());
                    break;
                }
            }
        }
    }

    @Override
    public Race race() {
        return court.race();
    }

    @Override
    public TradeNPC buyer() {
        return trade;
    }

    @Override
    public TradeNPC seller() {
        return trade;
    }

    @Override
    protected void save(FilePutter file) {
        nameIntro.save(file);
        for (NPCResource r : res) {
            SAVABLE s = r.saver();
            if (s != null)
                s.save(file);
        }
        trade.saver.save(file);
        file.i(iteration);
        request.save(file);
        file.bool(sanctified);
        super.save(file);
    }

    @Override
    protected void load(FileGetter file) throws IOException {
        nameIntro.load(file);
        for (NPCResource r : res) {

            SAVABLE s = r.saver();
            if (s != null)
                s.load(file);
        }
        trade.saver.load(file);
        iteration = file.i();
        request.load(file);
        sanctified = file.bool();
        super.load(file);
    }

    @Override
    public void clear() {
        for (NPCResource r : res) {
            SAVABLE s = r.saver();
            if (s != null)
                s.clear();
        }
        trade.saver.clear();
        iteration = 0;
        request.clear();
        sanctified = false;
        super.clear();
    }

    @Override
    protected void update(double ds) {
        for (NPCResource r : res) {
            r.update(this, ds);
        }
        request.update();
        super.update(ds);
    }

    public int getWorkers(RESOURCE res) {
        return (int) 0;
    }



    @Override
    public FBanner banner() {
        return banner;
    }

    @Override
    public FCredits credits() {
        return credits;
    }

    @Override
    public CharSequence rulerName() {
        return court.king().name;
    }

    public NPCCourt court() {
        return court;
    }

    public int iteration() {
        return iteration;
    }

    @Override
    public FResources res() {
        return stats;
    }

    @Override
    public NPCSlaves slaves() {
        return slaves;
    }

    public Royalty king() {
        return court.king().roy();
    }

    @Override
    public double boostableValue(BValue v) {
        return v.vGet(this);
    }

    @Override
    public double offensivePower() {
        return AD.power().get(this);
    }

    @Override
    public int citizens(Race race) {
        if (capitolRegion() == null)
            return 0;
        if (race == null)
            return RD.RACES().population.get(capitolRegion());

        RDRace r = RD.RACES().get(race);
        if (r == null)
            return 0;
        return r.pop.get(capitolRegion());

    }

}
