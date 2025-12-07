package game.events.faction;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;

public class EventFactionExpand extends EventResource{

	private static final double dtime() {
        return TIME.secondsPerDay()*2;
    }
    private double timer;
    private int nextFaction = RND.rInt(FACTIONS.MAX());


    EventFactionExpand(){
        super("FACTION_EXPAND");

        timer = dtime();
    }

    @Override
    protected void update(double ds) {
        double min = KingLevels.isActive() ? 1 : 0;

        timer -= ds*CLAMP.d(FACTIONS.player().realm().regions()/8.0, min, 1);
        if (timer > 0)
            return;

        int expansions = KingLevels.isActive() ? 20 : 1;

        for (int i = 0; i < expansions; i ++) {
            Faction f = FACTIONS.getByIndex(nextFaction);
            if (f != null && f.isActive() && f instanceof FactionNPC) {
                trigger((FactionNPC) f);

            }
            clear();
        }
    }

    public boolean trigger(FactionNPC f) {
        if (KingLevels.isActive()
                && KingLevels.getInstance().getKingLevel(f).getMaxRegions() < f.realm().regions()) {
            return false;
        }


        if (DIP.WAR().all(f).size() > 0)
            return false;

        Region best = null;
        double bv = 0;

        for (RegDist d : WORLD.PATH().regFinder.all(f, Treaty.FACTION_BORDERS, WRegSel.FACTION(null))) {
            if (FACTIONS.player().realm().regions() == 1 && RD.DIST().reachable(d.reg))
                continue;
            double v = RD.OWNER().prevOwner(d.reg) == f ? 5.0 : 1.0;
            v /= d.distance;
            if (v > bv) {
                bv = v;
                best = d.reg;
            }
        }

        if (best == null)
            return false;

        for (WArmy a : f.armies().all()) {
            if (AD.power().get(a) > RD.MILITARY().power.getD(best)*1.5) {
                a.besiege(best);
                return true;
            }
        }

        return false;
    }


    @Override
    protected void save(FilePutter file) {
        file.d(timer);
        file.i(nextFaction);
    }

    @Override
    protected void load(FileGetter file) throws IOException {
        timer = file.d();
        nextFaction = file.i();
    }

    @Override
    protected void clear() {
        timer = RND.rFloat()*dtime();
        nextFaction = RND.rInt(FACTIONS.MAX());
    }

}
