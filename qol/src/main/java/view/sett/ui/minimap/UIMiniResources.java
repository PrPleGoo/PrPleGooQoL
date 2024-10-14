package view.sett.ui.minimap;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.Icons;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;

final class UIMiniResources extends Expansion {

    private static CharSequence ¤¤desc = "¤Click to open resource details, right click to go to warehouse.";

    static {
        D.ts(UIMiniResources.class);
    }

    private GuiSection mini;
    private GuiSection full;

    public UIMiniResources(int index, int y1) {
        super(index);

        full = new Full(y1);
        mini = new Mini(y1);

        add(full);

        CLICKABLE c = new GButt.Glow(SPRITES.icons().s.arrow_left) {
            @Override
            protected void clickA() {
                int y1 = body().y1();
                clear();
                add(full);
                body().moveY1(y1);
            }
        };
        mini.add(c, mini.body().x2() - c.body().width() - 4, mini.body().y1() + 4);
        c = new GButt.Glow(SPRITES.icons().s.arrow_right) {
            @Override
            protected void clickA() {
                int y1 = body().y1();
                clear();
                add(mini);
                body().moveY1(y1);
            }
        };
        full.add(c, full.body().x2() - c.body().width() - 4, full.body().y1() + 4);


    }


    private static class Mini extends GuiSection {

        private final INTE t;

        Mini(int y1) {
            RENDEROBJ row = mini(RESOURCES.ALL().get(0));
            int width = row.body().width();
            body().moveY1(y1);
            int cats = 0;
            for (RESOURCE r : RESOURCES.ALL())
                if (r.category > cats) {
                    cats = r.category;
                }
            LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
            {
                int cat = RESOURCES.ALL().get(0).category;

                for (RESOURCE r : RESOURCES.ALL()) {
                    if (r.category != cat) {
                        rows.add(new RENDEROBJ.RenderImp(width, 16) {
                            @Override
                            public void render(SPRITE_RENDERER r, float ds) {
                                GCOLOR.UI().borderH(r, body().x1() + 4, body().x2() - 4, body().y1() + 7, body().y1() + 10);
                            }
                        });
                        cat = r.category;
                    }

                    rows.add(mini(r));

                }
            }


            body().setDim(width + 6, C.HEIGHT() - y1);

            RENDEROBJ c;
            y1 = y1 + 4;

            c = new GButt.Glow(UI.decor().up) {
                @Override
                protected void renAction() {
                    activeSet(t.get() > 0);
                }

                @Override
                protected void clickA() {
                    t.inc(-1);
                }
            };
            c.body().moveCX(body().cX() + 2);
            c.body().moveY1(y1);
            add(c);


            GScrollRows sc = new GScrollRows(rows, C.HEIGHT() - getLastY2() - c.body().height() - 8, 0, false);
            addDownC(0, sc.view());

            t = sc.target;

            c = new GButt.Glow(UI.decor().down) {
                @Override
                protected void renAction() {
                    activeSet(t.get() != t.max());
                }

                @Override
                protected void clickA() {
                    t.inc(1);
                }
            };
            addDownC(4, c);

        }


    }

    private static class Full extends GuiSection {

        private final INTE t;

        Full(int y1) {
            RENDEROBJ row = big(RESOURCES.ALL().get(0));
            int width = row.body().width() * 2;
            body().setDim(width + 6, C.HEIGHT() - y1);
            body().moveY1(y1);
            int cats = 0;
            for (RESOURCE r : RESOURCES.ALL())
                if (r.category > cats) {
                    cats = r.category;
                }
            LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
            {
                GuiSection s = null;
                int cat = RESOURCES.ALL().get(0).category;

                for (RESOURCE r : RESOURCES.ALL()) {
                    if (r.category != cat) {
                        rows.add(new RENDEROBJ.RenderImp(width, 16) {
                            @Override
                            public void render(SPRITE_RENDERER r, float ds) {
                                GCOLOR.UI().borderH(r, body().x1() + 4, body().x2() - 4, body().y1() + 7, body().y1() + 10);
                            }
                        });
                        s = new GuiSection();
                        rows.add(s);
                        cat = r.category;
                    }

                    if (s == null || s.elements().size() >= 2) {
                        s = new GuiSection();
                        rows.add(s);
                    }

                    s.addRightC(0, big(r));

                }
            }


            RENDEROBJ c;
            y1 = y1 + 4;

            c = new GButt.Glow(UI.decor().up) {
                @Override
                protected void renAction() {
                    activeSet(t.get() > 0);
                }

                @Override
                protected void clickA() {
                    t.inc(-1);
                }
            };

            c.body().centerX(this);
            c.body().moveY1(y1);
            add(c);


            GScrollRows sc = new GScrollRows(rows, C.HEIGHT() - getLastY2() - c.body().height() - 6, 0, false);
            addDownC(0, sc.view());

            t = sc.target;

            c = new GButt.Glow(UI.decor().down) {
                @Override
                protected void renAction() {
                    activeSet(t.get() != t.max());
                }

                @Override
                protected void clickA() {
                    t.inc(1);
                }
            };
            addDownC(4, c);


        }


    }

    private static GuiSection resBody(RESOURCE res) {
        return new GuiSection() {
            private final SPRITE decr = SPRITES.icons().s.arrowDown.createColored(COLOR.YELLOW100).scaled(0.7);
            int wI = 0;
            int lastDayChecked = 0;
            int productionLastYear = 0;

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                res.hoverDetailed(text);
                GBox b = (GBox) text;
                b.sep();
                b.text(¤¤desc);

                super.hoverInfoGet(text);
            }

            @Override
            public void render(SPRITE_RENDERER r, float ds) {
                int currentDay = TIME.days().bitCurrent();
                if(currentDay != lastDayChecked){
                    productionLastYear = GAME.player().res().total().history(res).getPeriodSum(-(int) TIME.years().bitConversion(TIME.days()), 0);
                    lastDayChecked = currentDay;
                }

                double a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
                double c = SETT.ROOMS().STOCKPILE.tally().space.total(res);
                double d = 0;
                if (c > 0)
                    d = a / c;
                if (d > 0.9)
                    GMeter.render(r, GMeter.C_REDPURPLE, d, body());
                else if (c > 0)
                    GMeter.render(r, GMeter.C_REDGREEN, d, body());
                else
                    GMeter.render(r, GMeter.C_INACTIVE, d, body());

                if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0) {
                    d = SETT.ROOMS().IMPORT.tally.importWhenBelow.getD(res);
                    if (d > 0) {
                        int x1 = (int) (body().x1() + d * (body().width() - 2));
                        COLOR.WHITE85.render(r, x1, x1 + 1, body().y1(), body().y2());
                    }
                }


                if (!hoveredIs()) {
                    OPACITY.O25.bind();
                    COLOR.BLACK.render(r, body(), -1);
                    OPACITY.unbind();
                }

                //COLOR.WHITE30.render(r, body());
                if (hoveredIs()) {
                    if (MButt.RIGHT.consumeClick()) {

                        for (int i = 0; i < SETT.ROOMS().STOCKPILE.instancesSize(); i++) {
                            wI++;
                            if (wI >= SETT.ROOMS().STOCKPILE.instancesSize())
                                wI = 0;

                            RoomInstance ins = SETT.ROOMS().STOCKPILE.getInstance(wI, res);

                            if (ins != null) {
                                VIEW.s().getWindow().centererTile.set(ins.body().cX(), ins.body().cY());
                                break;
                            }

                        }
                    }
                }

                super.render(r, ds);

                if (productionLastYear < 0) {
                    decr.render(r, body().x2() - decr.width(), body().y2() - decr.height());
                }
            }

            @Override
            public boolean click() {
                VIEW.UI().goods.detail(res, GAME.player());
                return super.click();

            }
        };
    }

    private static RENDEROBJ stat(RESOURCE res) {
        return new GStat() {

            @Override
            public void update(GText text) {
                text.setFont(UI.FONT().S);

                int a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
                GFORMAT.i(text, a);

                if (a == 0)
                    if (SETT.PATH().finders.resource.scattered.has(res))
                        text.normalify();
                    else
                        text.errorify();
            }

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                OPACITY.O018.bind();
                COLOR.BLACK.render(r, X1 - 1, X2 + 1, Y1 - 1, Y2 + 1);
                OPACITY.unbind();
                super.render(r, X1, X2, Y1, Y2);
            }
        }.r(DIR.NW);
    }


    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        if (visableIs()) {
            GCOLOR.UI().panBG.render(r, body());
            GCOLOR.UI().borderH(r, body(), 0);
            super.render(r, ds);
        }
    }


    private static RENDEROBJ mini(RESOURCE res) {

        GuiSection s = resBody(res);

        s.add(res.icon().small, 0, 0);
        RENDEROBJ r = stat(res);


        s.addRightC(3, r);
        s.body().incrW(40);


        s.pad(2, 4);
        return s;


    }

    private static RENDEROBJ big(RESOURCE res) {

        GuiSection s = resBody(res);

        s.add(res.icon(), 0, 0);

        RENDEROBJ r = stat(res);

        s.addRightC(1, r);
        s.body().incrW(42);


        s.pad(2, 4);

        return s;


    }

    @Override
    public void save(FilePutter file) {

    }

    @Override
    public void load(FileGetter file) throws IOException {

    }


}
