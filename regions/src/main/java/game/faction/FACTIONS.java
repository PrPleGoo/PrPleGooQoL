package game.faction;

import java.io.IOException;

import game.GAME.GameResource;
import game.debug.Profiler;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.npc.UpdaterNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.player.Player;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.ResourcePrices;
import game.faction.trade.TradeManager;
import game.time.TIME;
import init.RES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.text.D;
import prplegoo.regions.api.gen.FactionGenerator;
import prplegoo.regions.api.npc.KingLevels;
import settlement.main.SETT;
import settlement.tilemap.terrain.TAmount;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs;
import world.region.pop.RDRace;

public class FACTIONS extends GameResource {

    private static final int MAX = 64;
    private static final int NPCS_MAX = MAX()-1;
    private static FACTIONS self;


    private final IUpdater updater = new IUpdater(MAX(), TIME.days().bitSeconds()/4) {

        @Override
        protected void update(int i, double timeSinceLast) {
            if (all.get(i).isActive()) {
                all.get(i).update(timeSinceLast);
            }

        }
    };

    private final ArrayList<Faction> all = new ArrayList<>(MAX());
    private final ArrayList<FactionNPC> npcs = new ArrayList<>(MAX()-1);
    private final ArrayList<FactionNPC> npcsActive = new ArrayList<>(MAX()-1);
    private final ArrayList<Faction> active = new ArrayList<>(MAX());
    private boolean dirty = true;

    private final Player player;
    private final FactionResource npcManager;
    public final UpdaterNPC ncpUpdater;
    private FactionNPC otherFaction;
    private final ResourcePrices prices;
    private final FWorth worth = new FWorth();
    private DIP dip;

    private static CharSequence ¤¤sim = "Simulating factions";
    private static CharSequence ¤¤factionDestroyed = "The faction of {0} has been completely destroyed.";
    private static CharSequence ¤¤newFaction = "A new faction has emerged. They call themselves '{0}'.";

    static {
        D.ts(FACTIONS.class);
    }

    public FACTIONS() throws IOException{
        super("FACTIONS", false);
        self = this;
        new ROPINIONS(this);
        this.player = new Player(all);
        ncpUpdater = new UpdaterNPC();
        for (int i = 1; i < MAX(); i++) {
            npcs.add(new FactionNPC(all, ncpUpdater));

        }
        otherFaction = npcs.get(0);

        npcManager = new TradeManager(this);
        dip = new DIP(this);

        new Initer(all);
        FactionProfileFlusher.load(FACTIONS.player());

        new RD.RDOwnerChanger() {
            @Override
            public void change(Region reg, Faction oldOwner, Faction newOwner) {
                activate(oldOwner);
                activate(newOwner);
            }
        };

        IDebugPanel.add("Factions Prime", new ACTION() {

            @Override
            public void exe() {
                prime();
            }
        });

        IDebugPanel.add("KingLevel Faction reset", new ACTION() {
            @Override
            public void exe() {
                while(FACTIONS.NPCs().size() > 0) {
                    FactionNPC faction = FACTIONS.NPCs().get(0);
                    FACTIONS.remove(faction, false);
                }

                FactionGenerator.getInstance().generateKingdoms();
            }
        });

        prices = new ResourcePrices();
    }

    private void activate(Faction f) {
        if (f == null || f == player)
            return;
        boolean a = f.isActive();
        if (f.wasActive != a) {
            dirty = true;
            f.wasActive = a;
            if (a) {
                for (Faction.FactionActivityListener li: Faction.FactionActivityListener.all)
                    li.add((FactionNPC) f);
            }else {
                for (Faction.FactionActivityListener li: Faction.FactionActivityListener.all)
                    li.remove((FactionNPC) f);
            }
        }
    }

    @Override
    protected void save(FilePutter file) {
        for (Faction f : all) {
            file.mark(""+f.index());
            f.save(file);
            file.mark(""+f.index());
        }
        updater.save(file);
        npcManager.save(file);
        ((FactionResource)dip).save(file);
        file.i(otherFaction.index());
    }

    @Override
    protected void load(FileGetter file) throws IOException {

        for (Faction f : all) {
            file.check(""+f.index());
            f.load(file);
            file.check(""+f.index());
        }
        updater.load(file);
        npcManager.load(file);
        ((FactionResource)dip).load(file);

        otherFaction = (FactionNPC) all.get(file.i());
        dirty = true;
        prices.clearCache();



    }

    @Override
    protected void update(float ds, Profiler prof) {

        prof.logStart(updater.getClass());
        updater.update(ds);
        prof.logEnd(updater.getClass());

        prof.logStart(npcManager.getClass());
        npcManager.update(ds, null);
        prof.logEnd(npcManager.getClass());

        prof.logStart(player.getClass());
        player.updateSpecial(ds, prof);
        prof.logEnd(player.getClass());

        prof.logStart(dip.getClass());
        ((FactionResource)dip).update(ds, null);
        prof.logEnd(dip.getClass());
    }

    public static Player player() {
        return self.player;
    }

    public static Faction getByIndex(int index) {
        return self.all.get(index);
    }

//	public static DIP DIP() {
//		return self.dip;
//	}

//	public static TradePrices prices() {
//		return self.tradePrices;
//	}
//
//	public static TradePrices pricesP() {
//		return self.tradePricesPotential;
//	}

    public void prime() {

        RES.loader().print(¤¤sim);

        int a = 50;



        for (int i = 0; i < a; i++) {
            RES.loader().print(¤¤sim + ": " + (int)(100*((i*2+a*2)/(double)(a*4))) + "%");

            for (FactionNPC f : FACTIONS.NPCs()) {
                RD.UPDATER().shipAll(f, 1.0);
                f.stockpile.update(f, TIME.secondsPerDay);
            }

            if (i % 4 == 0) {
                ((TradeManager)self.npcManager).prime();
            }
        }
        ((TradeManager)self.npcManager).prime();
        prices.clearCache();

        for (FactionNPC f : FACTIONS.NPCs()) {
            f.slaves().init();
        }
    }

    public static FactionNPC activateNext(Region capitol, RDRace prefRace, boolean log) {

        if (capitol.realm() != null)
            throw new RuntimeException();

        FactionNPC ff = self.free();
        if (ff == null)
            return null;
        ff.clear();
        capitol.fationSet(ff, log);

        capitol.info.name().clear().add(ff.name);

        ff.generate(prefRace, true);

        for (Faction.FactionActivityListener li: Faction.FactionActivityListener.all)
            li.add(ff);
        ((Faction)ff).wasActive = true;

        if (log && SETT.exists()) {
            Str.TMP.clear().add(¤¤newFaction);
            Str.TMP.insert(0, ff.name);
            WORLD.LOG().log(null, ff, UI.icons().s.crown, Str.TMP, ff.cx(), ff.cy());
        }

        if (KingLevels.isActive()) {
            ff.credits().set(100000);
            KingLevels.getInstance().reset(ff.index());

            for (int day = 0; day < 25; day++) {
                for (RDRace rr : RD.RACES().all) {
                    rr.pop.init(capitol);
                }

                KingLevels.getInstance().getBuilder().build((FactionNPC) capitol.faction());

                KingLevels.getInstance().resetDailyProductionRateCache(ff);
                for (int resourceIndex = 0; resourceIndex < RESOURCES.ALL().size(); resourceIndex++) {
                    RESOURCE res = RESOURCES.ALL().get(resourceIndex);

                    double amount = KingLevels.getInstance().getDailyConsumptionRate(ff, res);
                    amount += KingLevels.getInstance().getDailyProductionRate(ff, res);

                    ff.stockpile.inc(res, amount);
                }
            }
        }

        return ff;

    }

    public static boolean canActivateNext() {
        return self.free() != null;
    }

    public static int frees() {
        int am = 0;
        for (FactionNPC f : self.npcs) {
            if (!f.isActive()) {
                am++;
            }
        }
        return am;
    }

    public FactionNPC free() {
        for (FactionNPC f : npcs) {
            if (!f.isActive()) {
                ((Faction) f).wasActive = false;
                return f;
            }
        }
        self.dirty = true;
        return null;
    }

    public static LIST<FactionNPC> NPCs(){
        active();
        return self.npcsActive;
    }


    public static LIST<Faction> active(){
        if (self.dirty) {
            self.active.clearSloppy();
            self.active.add(self.player);
            self.npcsActive.clearSloppy();
            self.dirty = false;
            for (int i = 0; i < self.npcs.size(); i++) {
                if (self.npcs.get(i).realm().capitol() != null) {
                    self.npcsActive.add(self.npcs.get(i));
                    self.active.add(self.npcs.get(i));
                }
            }
        }
        return self.active;
    }

    public static LIST<Faction> all(){
        return self.all;
    }


    public static void remove(FactionNPC faction, boolean log) {

        if (log) {
            Str.TMP.clear().add(¤¤factionDestroyed);
            Str.TMP.insert(0, faction.name);
            WORLD.LOG().log(null, faction, UI.icons().s.crown, Str.TMP, faction.cx(), faction.cy());
        }

        faction.armies().disbandAll();
        RD.clearFaction(faction);
        self.dirty = true;
    }

    public static void otherFactionSet(FactionNPC faction) {
        self.otherFaction = faction;
    }

    public static FactionNPC otherFaction() {
        return self.otherFaction;
    }

    public static CharSequence name(Faction f) {
        if (f == null)
            return Dic.¤¤Rebels;
        return f.name;
    }

    public static ResourcePrices PRICE() {
        return self.prices;
    }

    public static FWorth WORTH() {
        return self.worth;
    }

    public static int MAX() {
        return MAX;
    }

    public static int NPC_MAX() {
        return NPCS_MAX;
    }

}

