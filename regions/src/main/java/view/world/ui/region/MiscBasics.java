package view.world.ui.region;

import game.boosting.BoostableCat;
import game.boosting.Booster;
import game.faction.FWorth;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import prplegoo.regions.api.gen.ProspectCache;
import settlement.stats.STATS;
import settlement.tilemap.ground.Ground;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.map.pathing.WRegFinder;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDReligions.RDReligion;
import world.region.building.Creator;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

class MiscBasics extends GuiSection{

    private static CharSequence ¤¤fertilityD = "Moisture can increase production of buildings and determines how many subjects the region can support.";
    private static CharSequence ¤¤areaD = "The size of the region along with the fertility determines how many subjects it can support.";
    private static CharSequence ¤¤worth = "Worth";
    private static CharSequence ¤¤worthD = "How much the other factions think this region is worth. This counts towards your rivalry.";

    static {
        D.ts(MiscBasics.class);
    }

    public static GuiSection info(GETTER<Region> g) {
        GuiSection sec = new GuiSection();



        int w = 140;
        sec.add(new GStat() {

            @Override
            public void update(GText text) {
                GFORMAT.i(text, g.get().info.area());
            }

            @Override
            public void hoverInfoGet(GBox b) {
                b.text(¤¤areaD);
            };

        }.hh((SPRITE) new GText(UI.FONT().H2, Dic.¤¤Area).lablify(), w));

        sec.addRightC(80, new HOVERABLE.HoverableAbs(w, 16) {

            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                double d = RD.DEVASTATION().current.getD(g.get());
                int am = (int) (d*(body().width()/16));
                int x = body().x1();
                GCOLOR.UI().BAD.hovered.bind();
                for (int i = 0; i < am; i++) {
                    UI.icons().s.degrade.render(r, x, body().y1());
                    x+= 16;
                }
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                b.add(RD.DEVASTATION().current.info());
                b.NL();
                b.add(GFORMAT.percInv(b.text(), RD.DEVASTATION().current.getD(g.get())));
            }
        });

        sec.add(new GStat() {

            @Override
            public void update(GText text) {
                GFORMAT.i(text, (int)FWorth.region(g.get()));
            }

            @Override
            public void hoverInfoGet(GBox b) {
                b.text(¤¤worthD);
            };

        }.hh((SPRITE) new GText(UI.FONT().H2, ¤¤worth).lablify(), w), 0, sec.getLastY2()+2);

        int DIM = 24;
        int AM = 10;
        SPRITE s;

        s = new SPRITE.Imp(AM*DIM, DIM) {

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

                COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
                int ff = (int) Math.ceil(g.get().info.moisture()*(AM-1));
                for (int i = 0; i < AM; i++) {
                    OPACITY.unbind();
                    if (i >= ff) {
                        OPACITY.O25.bind();
                    }
                    SPRITES.icons().m.agriculture.render(r, X1, Y1);
                    X1 += DIM;
                }
                OPACITY.unbind();

            }
        };
        sec.add(new GHeader.HeaderHorizontal(Ground.¤¤moisture, s, w) {

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                b.title(Ground.¤¤moisture);
                b.text(¤¤fertilityD);
                b.NL();
                b.textL(Dic.¤¤Current);
                b.tab(6);
                b.add(GFORMAT.perc(b.text(), g.get().info.moisture()));
            }

        }, 0, sec.getLastY2()+2);

        s = new SPRITE.Imp(AM*DIM, DIM) {

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

                COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
                int x = X1;
                for (int i = 0; i < AM/2; i++) {
                    ColorImp.TMP.interpolate(CLIMATES.COLD().color, CLIMATES.TEMP().color, i/5.0);
                    ColorImp.TMP.render(r, x, x+DIM, Y1+2, Y2-2);
                    x += DIM;
                }
                for (int i = 0; i < AM/2; i++) {
                    ColorImp.TMP.interpolate(CLIMATES.TEMP().color, CLIMATES.HOT().color, i/5.0);
                    ColorImp.TMP.render(r, x, x+DIM, Y1+2, Y2-2);
                    x += DIM;
                }


                double d = 0;
                for (CLIMATE c : CLIMATES.ALL()) {
                    d+= g.get().info.climate(c)*(c.index());
                }
                d /= (CLIMATES.ALL().size()-1);
                x = (int) (X1+d*(X2-X1));
                x -= Icon.M/2;
                COLOR.BLACK.bind();
                SPRITES.icons().m.crossair.render(r, x+2, Y1+2);
                COLOR.unbind();
                SPRITES.icons().m.crossair.render(r, x, Y1);


            }
        };
        sec.addDown(2, new GHeader.HeaderHorizontal(CLIMATES.INFO().name, s, w) {

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                for (CLIMATE c : CLIMATES.ALL()) {
                    b.text(c.name);
                    b.tab(6);
                    b.add(GFORMAT.perc(b.text(), g.get().info.climate(c)));
                    b.NL();
                }
                CLIMATES.BONUS().hover(text, g.get(), Dic.¤¤Effects, BoostableCat.TYPE_WORLD);
            }

        });

        s = new SPRITE.Imp(AM*DIM, DIM) {

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                COLOR.WHITE25.render(r, X1, X2, Y1, Y2);
                int aa = AM;
                for (TERRAIN c : TERRAINS.ALL()) {
                    if (c == TERRAINS.NONE())
                        continue;
                    int am = (int) ((aa)*g.get().info.terrain(c));
                    for (int i = 0; i < am; i++) {
                        c.icon().render(r, X1, X1+DIM, Y1, Y2);
                        X1+=DIM;
                    }
                }
            }
        };
        sec.addDown(2, new GHeader.HeaderHorizontal(Dic.¤¤Terrain, s, w) {

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                for (TERRAIN t : TERRAINS.ALL()) {
                    b.text(t.name);
                    b.tab(6);
                    b.add(GFORMAT.perc(b.text(), g.get().info.terrain(t)));
                    b.NL();
                }
            }

        });





        GuiSection hh = new GuiSection();
        hh.add(new GText(UI.FONT().H2, Dic.¤¤Neighbours).lablify(), 0, 0);

        HOVERABLE h = new HOVERABLE.HoverableAbs(AM*DIM, DIM) {

            WRegFinder wr = new WRegFinder();
            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                COLOR.WHITE25.render(r, body);
                LIST<WRegFinder.RegDist> dists = wr.all(g.get(),  Treaty.REG_NEIGHS, WRegSel.DUMMY(g.get()));

                if (dists.size() > 0) {
                    int dd = AM*DIM/dists.size();
                    dd = CLAMP.i(dd, 1, Icon.S);
                    int x1 = body.x1();
                    for (WRegFinder.RegDist d : dists) {
                        COLOR c = d.reg.faction() == null ? COLOR.WHITE50 : d.reg.faction().banner().colorBG();
                        c.bind();
                        UI.icons().s.capitol.renderCY(r, x1, body.cY());
                        x1 += dd;
                    }
                }

            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                b.text(Dic.¤¤Distance);
                b.NL();
                for (WRegFinder.RegDist r : wr.all(g.get(),  Treaty.REG_NEIGHS, WRegSel.DUMMY(g.get()))) {
                    GText n = b.text();
                    COLOR c = r.reg.faction() == null ? COLOR.WHITE50 : r.reg.faction().banner().colorBG();
                    n.color(c);
                    n.add(r.reg.info.name());

                    b.add(n);
                    b.tab(6);
                    b.add(SPRITES.icons().s.arrow_right);
                    b.add(GFORMAT.i(b.text(), r.distance));
                    if (r.water) {
                        b.tab(8);
                        b.add(SPRITES.icons().s.ship);
                    }
                    b.NL();
                }

            }
        };
        hh.addRightCAbs(w, h);

        sec.addDown(2, hh);





        sec.addRelBody(2, DIR.S, race(g, sec.body().width()));



        return sec;
    }

    private static RENDEROBJ race(GETTER<Region> g, int width) {
        int YS = 3;
        int wi = 16*((width-8)/16);

        return new HOVERABLE.HoverableAbs(wi+8, 24*YS+8) {


            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                COLOR.WHITE25.render(r, body);
                final double maxPop = (3*RD.RACES().maxPop(g.get()));
                final int maxAm = (int) ((wi/8)*YS);
                int m = (body.width()-wi)/2;
                final int x1 = body.x1()+m;
                final int y1 = body.y1()+4;



                double d = (double)maxPop/(3*RD.RACES().population.get(g.get()));
                d = CLAMP.d(d, 0, 1);

                int dx = (int) (24*d);
                dx = CLAMP.i(dx, 1, 24);

                int i = 0;

                int lineMax = (int) Math.ceil((double)(wi-20)/dx);

                for (RDRace ra : RD.RACES().all) {
                    int am = (int)Math.ceil(maxAm*ra.pop.get(g.get())/maxPop);

                    while(am > 0) {
                        am--;

                        int x = dx*(i%lineMax);
                        int y = (i/lineMax);
                        i++;
                        if (y >= YS)
                            break;

                        ra.race.appearance().icon.render(r, x1+x, y1+y*24);

                    }
                }

            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                b.title(Dic.¤¤Population);

                b.tab(6);
                b.textL(Dic.¤¤Population);
                b.tab(9);
                b.textL(Dic.¤¤Biome);
                b.NL();

                for (RDRace ra : RD.RACES().all) {

                    double v = ra.pop.base(g.get());
                    b.add(ra.race.appearance().iconBig);
                    b.text(ra.race.info.names);
                    b.tab(6);

                    b.add(GFORMAT.i(b.text(), ra.pop.get(g.get())));
                    b.tab(9);
                    b.add(GFORMAT.perc(b.text(), v));
                    b.NL();


                }


                b.textLL(Dic.¤¤Total);
                b.tab(6);
                b.add(GFORMAT.iBig(b.text(), RD.RACES().population.get(g.get())));
                b.add(UI.icons().s.arrow_right);
                b.add(GFORMAT.iBig(b.text(), (int)RD.RACES().popTarget.getD(g.get())));

            }
        };
    }

    public static RENDEROBJ rel(GETTER<Region> g) {
        int AM = 24;
        return new HOVERABLE.HoverableAbs(16*AM, 20) {

            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                COLOR.WHITE25.render(r, body);

                int a = AM;
                int X1 = body.x1();
                double tot = 1.0;
                for (RDReligion ra : RD.RELIGION().all()) {

                    double v = ra.current.getD(g.get());
                    int aa = (int) Math.round(a*ra.current.getD(g.get())/tot);

                    tot -= v;
                    a -= aa;

                    while(aa > 0) {
                        ra.religion.icon.small.renderCY(r, X1, body().cY());
                        X1 += Icon.S;
                        aa--;
                    }

                }
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;

                b.title(STATS.RELIGION().info.name);

                for (RDReligion ra : RD.RELIGION().all()) {

                    b.add(ra.religion.icon);
                    b.text(ra.religion.info.name);
                    b.tab(7);
                    b.add(GFORMAT.perc(b.text(), ra.current.getD(g.get())));
                    b.NL();


                }

            }
        };
    }

    public static RENDEROBJ prospect(GETTER<Region> g) {
        int AM = 12;
        final ArrayList<BuildSort> all = new ArrayList<BuildSort>(RD.BUILDINGS().all.size());
        {
            for (RDBuilding b : RD.BUILDINGS().all) {
                for(Booster bo : b.levelCap.all()) {
                    if (bo.info.name.equals(Creator.get¤¤prospect())) {
                        all.add(new BuildSort(b));
                        break;
                    }
                }
            }
        }
        final Tree<BuildSort> sort = new Tree<BuildSort>(all.size()) {

            @Override
            protected boolean isGreaterThan(BuildSort current, BuildSort cmp) {
                return current.value > cmp.value;
            }

        };

        return new HOVERABLE.HoverableAbs(32*AM, 40) {

            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                COLOR.WHITE25.render(r, body);
                sort.clear();
                for (BuildSort b : all) {
                    b.init(g.get());
                    sort.add(b);
                }

                int max = AM;
                int X1 = body.x1();
                while(sort.hasMore() && max > 0) {
                    BuildSort b = sort.pollGreatest();
                    max--;

                    int Y1 = body.y1();
                    b.b.levels.get(1).icon.renderCY(r, X1, body().cY());

                    COLOR col = COLOR.GREEN100;
                    SPRITE ii = UI.icons().s.plus;
                    int am = (int) Math.ceil(Math.abs(b.value / 2)) - 1;
                    am = CLAMP.i(am, 0, 4);
                    col.bind();
                    for (int i = 0; i < am; i++) {
                        ii.render(r, X1+16, Y1+10*i);
                    }
                    COLOR.unbind();
                    X1 += 32;

                }
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;
                b.title(Dic.¤¤Prospect);
                sort.clear();
                for (BuildSort bu : all) {
                    bu.init(g.get());
                    sort.add(bu);
                }
                while(sort.hasMore()) {
                    BuildSort bu = sort.pollGreatest();
                    b.add(bu.b.levels.get(1).icon);
                    b.text(bu.b.info.name);
                    b.tab(7);
                    b.add(GFORMAT.f1(b.text(), bu.value));
                    b.NL();
                }
            }
        };
    }

    private static class BuildSort {

        public final RDBuilding b;
        public double value = 0;

        public BuildSort(RDBuilding b) {
            this.b = b;
        }

        void init(Region reg) {
            value = b.levelCap.get(reg);
        }

    }

}
