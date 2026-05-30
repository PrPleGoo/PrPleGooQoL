package world.region.building;

import java.io.IOException;

import game.GAME;
import game.boosting.*;
import game.boosting.BValue.BValuePlayerOnly;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.sprite.SPRITES;
import init.value.GVALUES;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.gui.misc.GBox;
import util.info.INFO;
import util.text.Dic;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public final class RDBuildPoints {

    public final LIST<RDBuildPoint> ALL;
    private final LIST<RDBuildPoint> all;
    public final RDBuildPoint GOV;
//	private boolean dirty = true;

    RDBuildPoints() throws IOException {


        ArrayListGrower<RDBuildPoint> all = new ArrayListGrower<>();
        GOV = new RDBuildPoint(all.size(), BOOSTABLES.CIVICS().GOV);
        all.add(GOV);


        ResFolder f = PATHS.WORLD().folder("point");
        for (String key : f.init.getFiles()) {
            Json d = new Json(f.init.get(key));
            Json t = new Json(f.text.get(key));
            RDBuildPoint p = new RDBuildPoint(all.size(), key, d, t);
            all.add(p);
        }
        this.ALL = all;
        this.all = new ArrayList<RDBuildPoints.RDBuildPoint>(ALL);
    }

    public void setDirty() {
        upI = -120;

    }

    public RDBuildPoint get(Boostable bo) {

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).bo == bo)
                return all.get(i);
        }
        return null;
    }

    public RDBuildPoint get(Boostable bo, Booster b) {
        if (b.isMul) {
            if (b.to() >= 1)
                return null;
        }else if (b.to() > 0)
            return null;
        return get(bo);
    }




    private boolean calcing = false;

    private int upI = -120;


    private boolean dirty() {
        return upI != GAME.updateI();
    }

    private void calc() {
        if (calcing)
            return;
        calcing = true;

        upI = GAME.updateI();


        for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
            Region reg = FACTIONS.player().realm().region(i);
            for (RDBuildPoint co : all) {
                co.eff[reg.index()] = 1;
                co.lastValue[reg.index()] = -1;
                co.consumed[reg.index()] = -1;
            }
        }

        int deathSwitch = 100;
        boolean changed = true;
        while(changed && deathSwitch-- > 0) {
            changed = false;
            for (int ci = 0; ci < all.size(); ci++) {
                RDBuildPoint co = all.get(ci);

                for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
                    Region r = FACTIONS.player().realm().region(ri);

                    double v = co.bo.get(r);
                    if (v != co.lastValue[r.index()]){
                        co.lastValue[r.index()] = v;
                        changed = true;
                        co.consumed[r.index()] = 0;
                        for (int bi = 0; bi < co.bo.all().size(); bi++) {
                            Booster b = co.bo.all().get(bi);
                            double bv = b.get(r);
                            if (!b.isMul && (bv < 0 || (co.bo == BOOSTABLES.CIVICS().GOV && b instanceof RDBuilding.BBoost)))
                                co.consumed[r.index()] += bv;
                        }

                        if (v >= 0)
                            co.eff[r.index()] = 1;
                        else {
                            double add = co.bo.baseValue;
                            double mul = 1;
                            for (int bi = 0; bi < co.bo.all().size(); bi++) {
                                Booster b = co.bo.all().get(bi);
                                double bv = b.get(r);
                                if (b.isMul)
                                    mul *= bv;
                                else if(bv > 0)
                                    add += bv;
                            }
                            if (mul > 1)
                                add*= mul;
                            co.eff[r.index()] = CLAMP.d((add+v)/add, 0, 1);

                        }
                    }
                }




            }
        }
        calcing = false;


    }



    public final class RDBuildPoint{

        public final int index;
        public final SPRITE icon;
        public final INFO info;
        public final Boostable bo;
        private final double[] lastValue = new double[WREGIONS.MAX];
        private final int[] consumed = new int[WREGIONS.MAX];
        private final double[] eff = new double[WREGIONS.MAX];
        private final double BI = 1.0/1000000.0;
        RDBuildPoint(int index, String key, Json data, Json text) throws IOException{
            this.index = index;
            icon = SPRITES.icons().get(data);
            info = new INFO(text);
            bo = BOOSTING.push("POINT_" + key, 0, info.name, info.desc, icon, BoostableCat.ALL().WORLD);

            GVALUES.REGION.push("EFFICIENCY_" + key, info.name, icon, new DOUBLE_O<Region>() {

                @Override
                public double getD(Region t) {
                    return eff(t);
                }

            });

            new BoostSpecs(info.name, icon, true).read(data, new BValuePlayerOnly() {

                @Override
                public double vGet(Player f) {
                    int am = 0;
                    for (int i = 0; i < f.realm().regions(); i++)
                        am += lastValue[f.realm().region(i).index()];
                    return am;
                }

                @Override
                public double vGet(Region reg) {
                    calc();
                    return lastValue[reg.index()]*BI;
                }

                @Override
                public double vGet(FactionNPC f) {
                    return 0;
                }
            });

        }

        RDBuildPoint(int index, Boostable bo) throws IOException{
            this.index = index;
            icon = bo.icon.medium;
            info = new INFO(bo.name, bo.desc);
            this.bo = bo;

            GVALUES.REGION.push("EFFICIENCY_" + bo.key, info.name, icon, new DOUBLE_O<Region>() {

                @Override
                public double getD(Region t) {
                    return eff(t);
                }

            });


        }

        public double eff(Region reg) {
            if (reg.faction() != FACTIONS.player())
                return 1;

            if (dirty() || lastValue[reg.index()] != bo.get(reg)) {
                calc();
            }
            return eff[reg.index()];
        }

        public int consumed(Region reg) {
            calc();
            return consumed[reg.index()];
        }


        public int consumed(Faction f) {
            calc();
            int am = 0;
            for (int ri = 0; ri < f.realm().regions(); ri++) {
                Region r = f.realm().region(ri);
                am += consumed[r.index()];
            }
            return -am;
        }

        public void hover(GUI_BOX box, BOOSTABLE_O reg) {
            GBox b = (GBox) box;
            box.title(bo.name);
            box.text(bo.desc);
            box.NL();

            bo.hoverDetailed(box, reg, Dic.¤¤Produced, true);
            b.NL();

//			b.textLL(¤¤allocated);
//			b.tab(6);
//			b.add(GFORMAT.iIncr(b.text(), -allocated));
//			b.NL();
//
//			b.textLL(¤¤frozen);
//			b.tab(6);
//			b.add(GFORMAT.iIncr(b.text(), -frozen()));
//			b.NL();
//
//			b.sep();
//
//			b.textLL(¤¤available);
//			b.tab(6);
//			b.add(GFORMAT.iIncr(b.text(), available()));
//			b.NL();
//
//			b.textLL(¤¤penalty);
//			b.tab(6);
//			b.add(GFORMAT.percInv(b.text(), penalty));
//			b.NL();
//

        }


    }

}
