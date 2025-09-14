package world.region;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.RES;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.pathing.WRegFinder;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD.RDInit;

class Gen {

    private final WRegFinder rr = new WRegFinder();
    private static final int aveSize = 8;
    private Bitmap1D handsOff = new Bitmap1D(WREGIONS.MAX, false);

    Gen(RDInit init, ACTION loadprint){
        loadprint.exe();

        if (!WORLD.REGIONS().player.active())
            return;

        WORLD.RD().saver().clear();

        while(FACTIONS.NPCs().size() > 0) {
            FACTIONS.remove(FACTIONS.NPCs().get(0), false);
        }

        RD.PROSPECT().generate();
        loadprint.exe();
        generatePlayer(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy());
        generateKingdoms();

    }

    private void generatePlayer(int playerX, int playerY) {
        Region r = WORLD.REGIONS().map.get(playerX, playerY);
        r.fationSet(FACTIONS.player(), false);
        r.setCapitol();
        r.info.name().clear().add(FACTIONS.player().name);



        {
            LIST<RegDist> dd  = rr.all(r, treaty, WRegSel.DUMMY(r));
            if (dd.size() > 2) {
                handsOff.set(dd.rnd().reg.index(), true);
            }
        }

        int[] size = new int[] {
                1,
                3,
                5,
        };

        for (int s : size) {
            LIST<RegDist> ddd  = rr. all(r, treaty, WRegSel.DUMMY(r));

            int am = 0;
            for (RegDist d : ddd) {
                if (d.reg.faction() != null)
                    continue;
                if (handsOff.get(d.reg.index()))
                    continue;
                if (!create(d.reg)) {
                    break;
                }
                ((FactionNPC)d.reg.faction()).sanctified = true;
                spread(d.reg, RND.rInt(s));
                am++;
                if (am >= 2 + s/2)
                    break;
            }
        }



    }

    private boolean create(Region reg) {
        FactionNPC f = FACTIONS.activateNext(reg, null, false);
        return f != null;
    }


    private void generateKingdoms() {
        RES.loader().init();
        ArrayList<Region> regs = new ArrayList<>(WORLD.REGIONS().active());
        regs.shuffle();

        int amount = 3*regs.size()/4;

        while(amount > 0 && regs.size() > 0) {
            Region r = regs.removeLast();
            if (r.faction() != null)
                continue;
            if (handsOff.get(r.index()))
                continue;
            FactionNPC f = FACTIONS.activateNext(r, null, false);
            if (f == null) {
                break;
            }
            amount -= spread(r);

        }



    }



    private int spread(Region home) {

        int amount = (int) (RND.rInt(aveSize*2));
        home.fationSet(home.faction(), false);
        LIST<RegDist> ddd  = rr.all(home, treaty, WRegSel.DUMMY(home));

        int k = 1;
        for (int i = 0; i < amount && i < ddd.size(); i++) {
            ddd.get(i).reg.fationSet(home.faction(), false);
            k++;
        }
        return k;
    }

    private int spread(Region home, int amount) {
        home.fationSet(home.faction(), false);
        LIST<RegDist> ddd  = rr.all(home, treaty, WRegSel.DUMMY(home));
        int k = 1;
        for (int i = 0; i < amount && i < ddd.size(); i++) {
            ddd.get(i).reg.fationSet(home.faction(), false);
            k++;
        }
        return k;
    }

    private final Treaty treaty = new Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (to == null)
                return true;
            if (to.faction() == null)
                return true;
            if (to != null && to.faction() != origin.faction())
                return false;
            if (handsOff.get(to.index()))
                return false;
            return true;
        }

    };


//	private final Treaty tr2eaty = new Treaty();
//
//	private final class Tre2aty extends WTREATY {
//
//		private Faction f;
//
//		@Override
//		public boolean can(int fx, int fy, int tx, int ty, double dist) {
//			return test(WORLD.REGIONS().map.get(fx, fy)) && test(WORLD.REGIONS().map.get(tx, ty));
//
//		}
//
//		private boolean test(Region r) {
//			if (r == null)
//				return true;
//			if (r.faction() == f)
//				return true;
//			if (handsOff.get(r.index()))
//				return false;
//			if (r.faction() == null)
//				return true;
//			return false;
//		}
//
//	}

}
