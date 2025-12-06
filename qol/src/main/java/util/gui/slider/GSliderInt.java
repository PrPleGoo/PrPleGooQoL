package util.gui.slider;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import util.text.D;
import view.keyboard.KEYS;
import view.main.VIEW;

public class GSliderInt extends GuiSection{

    protected final INTE in;
    private static final int midWidth = 8;
    private static CharSequence ¤¤setAmount = "¤Set amount";
    private static CharSequence ¤¤setAmountD = "¤Set amount {0}-{1}";

    static {
        D.ts(GSliderInt.class);
    }

    public GSliderInt(INTE in, int width, boolean input){
        this(in, width, 24, input);


    }

    public GSliderInt(INTE in, int width, boolean buttons, boolean input){
        this(in, width, 24, buttons, input);


    }

    public GSliderInt(INTE in, int width, int height, boolean buttons, boolean input){
        this.in = in;

        if (input) {
            width -= (Icon.S+2)*3;
        }

        width -= 4;
        height -= 4;

        if (width < 0)
            width = 0;

        if (buttons) {
            addRightC(0, new GButt.ButtPanel(SPRITES.icons().s.minifier) {

                @Override
                protected void clickA() {
                    in.inc(-1);
                    if (KEYS.MAIN().MOD.isPressed())
                        in.set(in.min());
                }

                @Override
                protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
                                      boolean isHovered) {
                    if (isHovered &&  MButt.LEFT.isDown()) {
                        clickSpeed1 += ds;
                        if (clickSpeed1 > 10)
                            clickSpeed1 = 10;
                        in.inc(-(int)clickSpeed1);

                    }else {
                        clickSpeed1 = 0;
                    }
                    super.render(r, ds, isActive, isSelected, isHovered);
                }


                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GAllocator.hov(text);
                    super.hoverInfoGet(text);
                }
            });

        }

        addRightC(4, new Mid(width, height));
        pad(2, 2);

        if (buttons) {
            addRightC(4, new GButt.ButtPanel(SPRITES.icons().s.magnifier) {

                @Override
                protected void clickA() {
                    in.inc(1);
                    if (KEYS.MAIN().MOD.isPressed())
                        in.set(in.max());
                }

                @Override
                protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
                                      boolean isHovered) {
                    if (isHovered &&  MButt.LEFT.isDown()) {
                        clickSpeed2 += ds*2;
                        if (clickSpeed2 > 10)
                            clickSpeed2 = 10;
                        in.inc((int)clickSpeed2);

                    }else {
                        clickSpeed2 = 0;
                    }
                    super.render(r, ds, isActive, isSelected, isHovered);
                }


                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GAllocator.hov(text);
                    super.hoverInfoGet(text);
                }
            });



        }

        if (input) {
            addRightC(2, new GButt.ButtPanel(SPRITES.icons().s.pluses) {

                @Override
                protected void clickA() {
                    Str.TMP.clear().add(¤¤setAmountD).insert(0, in.min()).insert(1, in.max());
                    VIEW.inters().input.requestNumericInput(rec, Str.TMP);
                }


            }.hoverInfoSet(¤¤setAmount));
        }

    }

    public GSliderInt(INTE in, int width, int height, boolean input){
        this(in, width, height, input, input);


    }

    private final STRING_RECIEVER rec = new STRING_RECIEVER() {

        @Override
        public void acceptString(CharSequence string) {
            String s = ""+string;
            try {
                int i = Integer.parseInt(s);
                i = CLAMP.i(i, in.min(), in.max());
                in.set(i);
            }catch(Exception e) {

            }

        }
    };

    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        activeSet(in.max() > 0);
        super.render(r, ds);
    }

    @Override
    public void hoverInfoGet(GUI_BOX text) {
        GBox b = (GBox) text;
        b.add(GFORMAT.i(b.text(), in.get()));
        super.hoverInfoSelf(text);
    }

    protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
        COLOR.WHITE65.render(r, x1, x1+width, y1, y2);
    }
    private int RI = -2;
    private boolean clicked = false;
    private double clickSpeed1 = 0;
    private double clickSpeed2 = 0;

    public void reset() {
        clickSpeed1 = 0;
        clickSpeed2 = 0;
        clicked = false;
        RI = -1;
    }

    private class Mid extends CLICKABLE.ClickableAbs {



        Mid(int width, int height){
            super(width, height-4);
        }

        @Override
        protected void clickA() {
            clicked = true;
            double x = (VIEW.mouse().x() - body().x1())/(double)body().width();
            in.setD(CLAMP.d(x, 0, 1));
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
            clicked &= (MButt.LEFT.isDown() && Math.abs(RI-VIEW.RI()) <= 1);
            RI = VIEW.RI();
            if (clicked) {
                double x = (VIEW.mouse().x() - body().x1())/(double)body().width();
                in.setD(CLAMP.d(x, 0, 1));
            }



            GCOLOR.UI().border().render(r, body, 2);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, 1);


            int x2 = body().x1() + (int) (in.getD()*body().width());
            {
                int my = isHovered || clicked ? 0 : 2;
                renderMidColor(r, body().x1(), x2-body().x1(), body().width(), body().y1()+my, body().y2()-my);
            }



            int cx = (int) (body.x1()+midWidth/2+(body().width()-midWidth)*in.getD());

//			if (!isHovered) {
//				COLOR.WHITE85.render(r, cx-1, cx+1, body().y1()+1, body().y2()-1);
//			}else {
//
//			}
            GCOLOR.UI().border().render(r, cx-midWidth/2, cx+midWidth/2, body().y1(), body().y2());
            COLOR c = isHovered || clicked ? GCOLOR.T().H1 : GCOLOR.T().H2;
            c.render(r, cx-midWidth/2+1, cx+midWidth/2-1, body().y1()+1, body().y2()-1);
            COLOR.BLACK.render(r, cx-1, cx+2, body().y1()+2, body().y2()-2);


        }

        @Override
        public boolean hover(COORDINATE mCoo) {
            if (super.hover(mCoo)) {
                if (KEYS.MAIN().MOD.isPressed() || KEYS.MAIN().UNDO.isPressed()) {
                    double d = MButt.clearWheelSpin();
                    if (d < 0)
                        in.inc(-1);
                    else if (d > 0)
                        in.inc(1);
                }

                return true;
            }
            return false;
        }


    }

    public static void renderMid(SPRITE_RENDERER r, int x1, int x2, int y1, int y2, double d, boolean isActive, boolean isSelected, boolean isHovered) {


        GCOLOR.UI().border().render(r, x1-2, x2+2, y1-2, y2+2);
        GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, x1-1, x2+1, y1-1, y2+1);

        int width = x2-x1;

        x2 = x1 + (int) (d*width);
        COLOR.WHITE65.render(r, x1, x2, y1, y2);

    }

}
