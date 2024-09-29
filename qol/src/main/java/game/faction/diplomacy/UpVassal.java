package game.faction.diplomacy;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpileOld;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.dic.Dic;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.message.MessageSection;

final class UpVassal implements SAVABLE{

    private static CharSequence ¤¤tribute = "¤Tribute";
    private static CharSequence ¤¤vassal = "¤It is time for us to pay our annual tribute to our overlord and protector. We must pay 10% of our net worth.";
    private static CharSequence ¤¤overlord = "¤Tribute from our loyal subjects has arrived. As always, they are expressing eternal gratitude for your protection. Should we want to increase their opinion of us, we can always decline this offer.";
    static {
        D.ts(UpVassal.class);
    }

    private int year = -1;

    @Override
    public void save(FilePutter file) {
        file.i(year);
    }

    @Override
    public void load(FileGetter file) throws IOException {
        year = file.i();
    }

    @Override
    public void clear() {
        year = -1;
    }

    public void update() {
        if (year == TIME.years().bitsSinceStart())
            return;
        year = TIME.years().bitsSinceStart();

        if (DIP.overlord(FACTIONS.player()) != null) {
            new MVassal(DIP.overlord(FACTIONS.player())).send();
        }



        if (DIP.VASSAL().all(FACTIONS.player()).size() > 0) {
            new MOverlord(DIP.VASSAL().all(FACTIONS.player())).send();

        }

    }

    private static class MVassal extends MessageSection{

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final int credits;
        private final int fi;

        public MVassal(Faction ff) {
            super(¤¤tribute);
            fi = ff.index();
            int credits = (int) (NPCStockpileOld.AVERAGE_PRICE*10 + FACTIONS.player().credits().getD());
            for (RESOURCE res : RESOURCES.ALL()) {
                credits += FACTIONS.PRICE().get(res)*SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res);
            }

            this.credits = (int) Math.ceil(credits*0.025);
            FACTIONS.player().credits().inc(-this.credits, CTYPE.TRIBUTE);
        }

        @Override
        protected void make(GuiSection section) {
            paragraph(¤¤vassal);

            section.addRelBody(16, DIR.S, new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.i(text, -credits);
                }
            }.hh(UI.icons().m.coins));
            Faction f = FACTIONS.getByIndex(fi);
            if (f != null) {
                section.addRelBody(16, DIR.N, f.banner().HUGE);
            }


        }

    }

    private static class MOverlord extends MessageSection{

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final int[] creds;
        private final int[] fi;
        private final int[] fii;
        private final boolean[] aa;
        private boolean accepted = false;

        private final int day = TIME.days().bitsSinceStart();

        public MOverlord(LIST<? extends Faction> all) {
            super(¤¤tribute);

            fi = new int[all.size()];
            fii = new int[all.size()];
            creds = new int[all.size()];
            aa = new boolean[all.size()];
            Arrays.fill(aa, true);
            for (int i = 0; i < all.size(); i++) {
                FactionNPC f = (FactionNPC) all.get(i);
                fi[i] = f.index();
                fii[i] = f.iteration();
                DIP.TMP().setFactionAndClear(f);
                creds[i] = (int) Math.ceil(DIP.TMP().npc.offerableWorth()*0.05);
            }
        }

        @Override
        protected void make(GuiSection section) {
            paragraph(¤¤overlord);

            LinkedList<RENDEROBJ> rows = new LinkedList<>();

            for (int k = 0; k < creds.length; k++) {
                final int i = k;
                GuiSection row = new GuiSection() {

                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        super.hoverInfoGet(text);
                        if (!text.emptyIs())
                            return;

                        FactionNPC f = f(i);
                        if (f != null) {
                            VIEW.world().UI.factions.hover(text, f);
                        }
                    }

                };

                SPRITE s = new SPRITE.Imp(Icon.L) {

                    @Override
                    public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                        FactionNPC f = f(i);
                        if (f != null) {
                            f.banner().BIG.render(r, X1, Y1);
                        }
                    }
                };

                row.add(s, 0, 0);

                row.addRightC(8, new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.iIncr(text, creds[i]);
                    }
                });
                row.addRightC(160, new GButt.Checkbox() {

                    @Override
                    protected void clickA() {
                        if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
                        {
                            boolean clickedValue = aa[i];
                            for (int t = 0; t < aa.length; t++) {
                                aa[t] = !clickedValue;
                            }
                        }
                        else
                        {
                            aa[i] = !aa[i];
                        }
                    }

                    @Override
                    protected void renAction() {
                        selectedSet(aa[i]);
                        activeSet(!accepted);
                    }

                }.hoverInfoSet(Dic.¤¤Accept));

                row.pad(8, 4);

                rows.add(row);


            }

            section.addRelBody(8, DIR.S, new GScrollRows(rows, rows.get(0).body().height()*8).view());

            section.addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤Accept) {

                @Override
                protected void clickA() {
                    if (!accepted && TIME.days().bitsSinceStart()-day < 4) {
                        accepted = true;
                        for (int i = 0; i < creds.length; i++) {
                            if (f(i) != null) {
                                if (aa[i]) {
                                    FACTIONS.player().credits().inc(creds[i], CTYPE.TRIBUTE);
                                    ROPINIONS.OTHER().acceptTribute(f(i),true);
                                }else {
                                    ROPINIONS.OTHER().acceptTribute(f(i),false);
                                }
                            }
                        }
                    }

                    super.clickA();
                }

                @Override
                protected void renAction() {
                    selectedSet(accepted && Math.abs(day - TIME.days().bitsSinceStart()) < 4);
                }

            });

        }

        private FactionNPC f(int i) {
            FactionNPC f = (FactionNPC) FACTIONS.getByIndex(fi[i]);
            if (f != null &&f.isActive() &&  f.iteration() == fii[i] && DIP.overlord(f) == FACTIONS.player()) {
                return f;
            }
            return null;
        }

    }

}
