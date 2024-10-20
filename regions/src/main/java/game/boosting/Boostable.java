package game.boosting;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.POP_CL;
import prplegoo.regions.api.MagicStringChecker;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.MAPPED;

public final class Boostable extends INFO implements MAPPED {

    final ArrayListGrower<Booster> all = new ArrayListGrower<>();

    private final int index;
    private byte deadlockCheck;

    public final double baseValue;

    public final String key;
    public final Icon icon;
    public final BoostableCat cat;
    public final double minValue;
    private final boolean isResourceProductionBooster;

    public Boostable(int index, String key, double baseValue, CharSequence name, CharSequence desc, SPRITE icon, BoostableCat category, double minValue) {
        super(name, desc);
        this.index = index;
        this.baseValue = baseValue;
        this.key = key;
        this.isResourceProductionBooster = MagicStringChecker.isResourceProductionBooster(key);
        this.icon = icon != null ? new Icon(Icon.S, icon) : UI.icons().s.DUMMY;
        this.cat = category;
        this.minValue = minValue;
        cat.all.add(this);
    }

    public Boostable copy() {
        Boostable b = new Boostable(index, key, baseValue, name, desc, icon, cat, minValue);
        b.all.add(all);
        b.all.add(all);
        return b;
    }

//	public LIST<Booster> adds() {
//		return consumers;
//	}
//
//	public LIST<Booster> muls() {
//		return factors;
//	}

    public LIST<Booster> all() {
        return all;
    }

    public double min(Class<? extends BOOSTABLE_O> b) {
        return BUtil.min(all, b, baseValue);
    }

    public double max(Class<? extends BOOSTABLE_O> b) {
        return BUtil.max(all, b, baseValue);
    }

    public double added(BOOSTABLE_O t) {
        double padd = baseValue;
        double mul = 1;
        for (Booster s : all) {
            if (s.has(t.getClass())) {
                if (s.isMul && s.get(t) > 1)
                    mul *= s.get(t);
                else {
                    double a = s.get(t);
                    if (a > 0 || (a != 0 && isResourceProductionBooster))
                        padd += a;

                }
            }

        }

        return CLAMP.d(padd * mul, minValue, Double.MAX_VALUE);
    }

    public double progress(BOOSTABLE_O b) {
        double min = min(b.getClass());
        double max = max(b.getClass());

        double delta = max - min;
        return CLAMP.d(get(b) / delta, 0, 1);

    }

    public double get(BOOSTABLE_O t) {
        if (deadlockCheck > 1) {
            throw new RuntimeException(
                    "boostable " + key + "seems to be deadlocked. Make sure it's not a factor in its own factors");
        }

        deadlockCheck++;
        double res;
        if (isResourceProductionBooster) {
            double padd = baseValue > 0 ? baseValue : 0;
            double sub = baseValue < 0 ? baseValue : 0;
            double mul = 1;
            for (BoosterAbs<BOOSTABLE_O> s : all) {
                if (s.has(t.getClass())) {
                    if (s.isMul)
                        mul *= s.get(t);
                    else {
                        double a = s.get(t);
                        if (a == 0) {
                            continue;
                        }

                        padd += a;
                    }
                }
            }
            res = CLAMP.d(padd * mul + sub, minValue, Double.MAX_VALUE);
        } else {
            res = BUtil.value(all, t, baseValue, 1, minValue);
        }
        deadlockCheck--;
        return res;
    }

    public double get(POP_CL o, int daysBack) {

        double add = baseValue;
        double sub = 0;
        double mul = 1;

        for (Booster l : all) {
            double d = l.getValue(l.vGet(o, daysBack));
            if (!l.isMul && d != 0) {
                if (d > 0 || isResourceProductionBooster)
                    add += d;
                else
                    sub += d;
            } else if (l.isMul && d != 1) {
                mul *= d;
            }
        }
        return Math.max(mul * add + sub, minValue);

    }

    public void addFactor(BoostSpec f) {
        all.add(f.booster);

    }

    public void removeFactor(BoostSpec f) {
        all.remove(f.booster);
    }

    public void hover(GUI_BOX box, BOOSTABLE_O f, boolean keepNops) {
        hover(box, f, name, keepNops);
    }

    static final int htab = 7;

    public void hoverDetailed(GUI_BOX box, BOOSTABLE_O f, CharSequence name, boolean keepNops) {
        hoverDetailed(box, f, name, keepNops, false);
    }

    public void hoverDetailed(GUI_BOX box, BOOSTABLE_O f, CharSequence name, boolean keepNops, boolean isResourceProductionBooster) {
        BHoverer.hoverDetailed(box, all, f, name, baseValue, keepNops, isResourceProductionBooster);
    }

    public void hoverDetailedHistoric(GUI_BOX box, POP_CL o, CharSequence name, boolean keepNops, int daysBack) {

        GBox b = (GBox) box;
        if (name != null)
            b.textLL(name);
        b.NL();
        double add = baseValue;
        double sub = 0;
        double mul = 1;

        for (Booster l : all) {
            double d = l.getValue(l.vGet(o, daysBack));
            if (!l.isMul && d != 0) {
                l.hoverDetailed(box, d);
                if (d > 0)
                    add += d;
                else
                    sub += d;
            }
        }

        b.NL(4);

        for (Booster l : all) {
            double d = l.getValue(l.vGet(o, daysBack));
            if (l.isMul && d != 1) {
                l.hoverDetailed(box, d);
                mul *= d;
            }
        }

        {
            b.NL(8);
            b.tab(1);
            b.textL(Dic.¤¤Total);
            b.tab(5);

            b.add(GFORMAT.f0(b.text(), add));
            b.add(b.text().add('*'));
            b.add(GFORMAT.f1(b.text(), mul));
            if (sub != 0)
                b.add(GFORMAT.f0(b.text(), sub));
            b.add(b.text().add('='));
            b.add(GFORMAT.fRel(b.text(), Math.max(mul * add + sub, minValue), baseValue));
        }

        b.NL();

        if (keepNops) {
            b.NL(4);

            for (Booster l : all) {
                double d = l.getValue(l.vGet(o, daysBack));
                if (!l.isMul && d == 0) {
                    l.hoverDetailed(box, d);
                }
            }
            for (Booster l : all) {
                double d = l.getValue(l.vGet(o, daysBack));
                if (l.isMul && d == 1) {
                    l.hoverDetailed(box, d);
                }
            }

        }

        b.NL();


    }

    public void hover(GUI_BOX box, BOOSTABLE_O f, CharSequence name, boolean keepNops) {
        BHoverer.hover(box, all, f, name, baseValue, keepNops, isResourceProductionBooster);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public String key() {
        return key;
    }

}