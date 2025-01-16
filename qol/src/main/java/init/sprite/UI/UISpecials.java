package init.sprite.UI;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.paths.PATHS;
import prplegoo.qol.ui.RealClock;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.dic.DicTime;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSprite;
import util.spritecomposer.ComposerUtil;
import view.keyboard.KEYS;
import view.keyboard.Key;
import view.keyboard.KeyButt;
import view.main.VIEW;

public final class UISpecials {


    private final TILE_SHEET clockwork = new ITileSheet(PATHS.SPRITE_UI().get("Specials"), 1320, 208) {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.init(0, 0, 1, 1, 3, 1, d.s32);
            s.full.paste(true);
            return d.s32.saveGui();
        }
    }.get();

    private final SPRITE background = new ITileSprite(6*32,2*32,1) {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.init(s.full.body().x2(), 0, 1, 1, 6, 2, d.s32);
            s.full.paste(true);
            return d.s32.saveGui();
        }
    };

    private final TILE_SHEET selest = new ITileSheet() {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.init(0, s.full.body().y2(), 1, 1, 16, 1, d.s16);
            s.full.setVar(0).setSkip(2, 0);
            s.full.paste(true);
            return d.s16.saveGui();
        }
    }.get();


    private final TILE_SHEET buttons = new ITileSheet() {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.setSkip(10, 2);
            s.full.paste(true);
            return d.s16.saveGui();
        }
    }.get();

    private final TILE_SHEET upperPanel = new ITileSheet() {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.init(0, s.full.body().y2(), 1, 1, 27, 2, d.s24);
            s.full.pasteRotated(2, true);
            return d.s24.saveGui();
        }
    }.get();

    private final TILE_SHEET seasons = new ITileSheet() {

        @Override
        protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
            s.full.init(0, s.full.body().y2(), 1, 1, 8, 1, d.s32);
            s.full.paste(true);
            return d.s32.saveGui();
        }
    }.get();




    public UISpecials() throws IOException {

    }

    public SPRITE lowerPanel() {
        return new SPRITE() {

            @Override
            public int width() {
                return upperPanel.size()*27;
            }

            @Override
            public int height() {
                return upperPanel.size()*2;
            }

            @Override
            public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                int startTile = 0;
                int endTile = 27;
                for (int y = 0; y < 2; y++) {
                    for (int x = startTile; x < endTile; x++) {
                        upperPanel.render(r, (26-x)+(1-y)*27, X1+(x-startTile)*upperPanel.size(), Y1+y*upperPanel.size());
                    }
                }

            }
        };
    }

    public GuiSection buildTimeThing(boolean simplified) {

        GuiSection s = new GuiSection() {
            @Override
            public void render(SPRITE_RENDERER r, float ds) {
                GAME.SPEED.poll();
                super.render(r, ds);
            };
        };
        if (!simplified)
            s.add(background, 0, 0);

        {
            CLICKABLE c;
            c = speedButt(KEYS.MAIN().SPEED0, 0, GAME.SPEED.speed0);
            s.add(c, 32, 4);
            c = speedButt(KEYS.MAIN().SPEED1, 1, GAME.SPEED.speed1);
            s.addRightC(0, c);
            c = speedButt(KEYS.MAIN().SPEED2, 2, GAME.SPEED.speed2);
            s.addRightC(0, c);
            c = speedButt(KEYS.MAIN().SPEED3, 3, GAME.SPEED.speed3);
            s.addRightC(0, c);

        }

        if (!simplified){
            s.add(new ClockWork(), 48,29);
            s.addDownC(10, new RealClock());
            s.moveLastToBack();
            s.moveLastToBack();
        }


        return s;
    }

    private CLICKABLE speedButt(Key key, int i, int speed) {

        CLICKABLE c = new ClickableAbs(2*buttons.size(), buttons.size()) {

            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
                isSelected = GAME.SPEED.speedTarget() == speed;

                boolean sspeed = speed == GAME.SPEED.speed3 && GAME.SPEED.speedTarget() == GAME.SPEED.speed4;
                sspeed |= speed == GAME.SPEED.speed1 && GAME.SPEED.speedTarget() == GAME.SPEED.speed05;
                isSelected |= sspeed;

                if (isHovered || isSelected) {
                    OPACITY.O99.bind();
                    buttons.render(r, 8, body().x1(), body().y1());
                    buttons.render(r, 9, body().x1()+buttons.size(), body().y1());
                    OPACITY.unbind();
                }

                if (!isActive)
                    GCOLOR.T().INACTIVE.bind();
                else if (isSelected && isHovered)
                    GCOLOR.T().HOVER_SELECTED.bind();
                else if (isSelected)
                    GCOLOR.T().SELECTED.bind();
                else if (isHovered)
                    GCOLOR.T().HOVERED.bind();
                else
                    COLOR.WHITE100.bind();
                buttons.render(r, i*2, body().x1(), body().y1());
                buttons.render(r, i*2+1, body().x1()+buttons.size(), body().y1());
                COLOR.unbind();

            }

            @Override
            protected void clickA() {
                if (speed == GAME.SPEED.speed1 && GAME.SPEED.speedTarget() == GAME.SPEED.speed1)
                    GAME.SPEED.speedSet(GAME.SPEED.speed05);
                if (speed == GAME.SPEED.speed3 && GAME.SPEED.speedTarget() == GAME.SPEED.speed3)
                    GAME.SPEED.speedSet(GAME.SPEED.speed4);
                else
                    GAME.SPEED.speedSet(speed);
            }
        };

        return KeyButt.wrap(c, key);

    }



    private class ClockWork extends HoverableAbs {

        private final TextureCoords text = new TextureCoords();
        private final GuiSection hover = new GuiSection();

        public ClockWork() {
            body().setWidth(clockwork.size()*clockwork.tiles()).setHeight(clockwork.size());



            RENDEROBJ h;

            h = new GStat() {

                @Override
                public void update(GText text) {
                    DicTime.setTime(text, TIME.currentSecond());
                    text.lablify();
                }
            }.r(DIR.N);
            hover.addDownC(2, h);


            h = new GStat() {

                @Override
                public void update(GText text) {
                    DicTime.setDate(text, (int) TIME.currentSecond());
                    text.lablifySub();
                }
            }.r(DIR.N);
            hover.addDownC(2, h);

            hover.pad(200, 10);

            GuiSection ss = new GuiSection() {

                @Override
                public void render(SPRITE_RENDERER r, float ds) {
                    if (!VIEW.s().isActive() && !VIEW.b().isActive()) {
                        return;
                    }
                    super.render(r, ds);
                }
            };

            h = new GStat() {

                @Override
                public void update(GText text) {
                    SETT.WEATHER().temp.format(text);
                    if (SETT.WEATHER().temp.cold() > 0)
                        GFORMAT.colorInter(text, SETT.WEATHER().temp.cold(), 1);
                    else
                        GFORMAT.colorInterInv(text, SETT.WEATHER().temp.heat(), 1);
                }
            }.hv(SETT.WEATHER().temp.info.name);
            ss.addC(h, -100, 0);

            h = new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.perc(text, SETT.WEATHER().moisture.getD());
                }
            }.hv(SETT.WEATHER().moisture.info.name);
            ss.addC(h, 100, 0);
            hover.addRelBody(8, DIR.S, ss);
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

            {
                if (isHovered)
                    COLOR.WHITE150.bind();
                int off = (int) (TIME.currentSecond()*2.0) % clockwork.size();
                int width = clockwork.size()-off;
                int x1 = body().x1();
                if (off != 0) {
                    TextureCoords c = clockwork.getTexture(0);
                    text.get(c.x1+off, c.y1, width, c.y2-c.y1);
                    CORE.renderer().renderSprite(body().x1(), body().x1()+width, body().y1()-0, body().y2()-0, text);
                    x1 += width;
                }else {
                    clockwork.render(r, 0, body().x1(), body().y1()-0);
                    x1 += clockwork.size();
                }
                for (int i = 1; i < clockwork.tiles(); i++) {
                    clockwork.render(r, i, x1+(i-1)*clockwork.size(), body().y1()-0);
                }
                if (off != 0) {
                    TextureCoords c = clockwork.getTexture(0);
                    text.get(c.x1, c.y1, off, c.y2-c.y1);
                    CORE.renderer().renderSprite(body().x2()-off, body().x2(), body().y1()-0, body().y2()-0, text);
                }

            }


            renderSeasons();
            COLOR.unbind();

            {
                int sI = TIME.light().nightIs() ? 1 : 0;
                int w = body().width() + selest.size()-8;
                int x1 = (int) (body().x1()+4-selest.size() + TIME.light().partOf()*w);

                if (!render(selest.size(), selest.getTexture(sI), x1, body().y1()+8))
                    selest.render(r, sI, x1, body().y1()+8);
            }
        }

        private void renderSeasons() {

            int pw = UISpecials.this.seasons.size();
            int width = UISpecials.this.seasons.tiles()*pw;

            int x1 = body().x1();
            int x2 = body().x2();

            int start = x1 - (int) (TIME.years().bitPartOf()*width)-pw/2;


            int t = 2*4-2;
            while (start < x2) {

                TextureCoords coo = UISpecials.this.seasons.getTexture(t);

                int offX1 = 0;
                int offX2 = 0;


                if (start < x1) {
                    offX1 = x1-start;

                }

                if (start + pw > x2) {
                    offX2 = start+pw - x2;
                }

                if (offX1 < pw && offX2 < pw) {
                    text.get(coo.x1+offX1, coo.y1, pw-(offX2+offX1), coo.y2-coo.y1);
                    CORE.renderer().renderSprite(start+offX1, start + pw - offX2, body().y1(), body().y1()+text.height(), text);
                }


                start += pw;
                t++;
                t %= UISpecials.this.seasons.tiles();


            }
        }

        private boolean render(int size, TextureCoords c, int x1, int y1) {
            if (x1 + size <= body().x1())
                return true;
            if (x1 >= body().x2())
                return true;
            if (x1 < body().x1()) {

                int off = body().x1()-x1;
                text.get(c.x1+off, c.y1, size-off, c.y2-c.y1);
                CORE.renderer().renderSprite(body().x1(), body().x1()+(size-off), y1, y1+size, text);
                return true;
            }else if (x1 + size > body().x2()) {
                int width = body().x2()-x1;
                text.get(c.x1, c.y1, width, c.y2-c.y1);
                CORE.renderer().renderSprite(x1, x1+width, y1, y1+size, text);
                return true;
            }
            return false;
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            GBox b = (GBox) text;
            b.add(hover);
        }

    }

}