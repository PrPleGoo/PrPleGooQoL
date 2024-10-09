package game.boosting;

import prplegoo.regions.api.MagicStringChecker;
import snake2d.LOG;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayListInt;
import snake2d.util.sets.LIST;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.info.GFORMAT;

public final class BHoverer {

    private final static ArrayListInt sort = new ArrayListInt(1024);

    private BHoverer() {

    }

    public static <T> void hoverDetailed(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops){
        hoverDetailed(box, all, f, name, baseValue, keepNops, false);
    }

    public static <T> void hoverDetailed(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops, boolean isResourceProductionBooster) {
        GBox b = (GBox) box;
        if (name != null)
            b.textLL(name);
        b.NL();

        for (BoosterAbs<T> l : all) {
            double d = l.get(f);

            if (l.has(f.getClass()) && !l.isMul && d != 0) {
                l.hoverDetailed(box, d);

            }
        }

        b.NL(4);

        for (BoosterAbs<T> l : all) {
            double d = l.get(f);
            if (l.has(f.getClass()) && l.isMul && d != 1) {
                l.hoverDetailed(box, d);
            }
        }

        tot(box, all, f, baseValue, isResourceProductionBooster);

        if (keepNops) {
            b.NL(4);

            for (BoosterAbs<T> l : all) {
                double d = l.get(f);
                if (l.has(f.getClass()) && !l.isMul && d == 0) {
                    l.hoverDetailed(box, d);
                }
            }
            for (BoosterAbs<T> l : all) {
                double d = l.get(f);
                if (l.has(f.getClass()) && l.isMul && d == 1) {
                    l.hoverDetailed(box, d);
                }
            }

        }

        b.NL();

    }

    public static <T> void hover(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops) {
        hover(box, all, f, name, baseValue, keepNops, false);
    }

    public static <T> void hover(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops, boolean isResourceProductionBooster) {
        GBox b = (GBox) box;
        hoverNoTot(box, all, f, name, baseValue, keepNops, isResourceProductionBooster);

        b.NL(8);

        tot(box, all, f, baseValue, isResourceProductionBooster);
        b.NL(8);

        if (keepNops) {
            int t = 0;
            for (BoosterAbs<T> l : all) {
                if (!l.has(f.getClass()))
                    continue;
                if (t > 1) {
                    t = 0;
                    b.NL();
                }
                double d = l.get(f);
                if (l.isMul && d == 1) {
                    hov(f, b, l, t++);
                }else if (!l.isMul && d == 0)
                    hov(f, b, l, t++);
            }

        }

        b.NL();

    }

    public static <T> void hoverNoTot(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops) {
        hoverNoTot(box, all, f, name, baseValue, keepNops, false);
    }

    public static <T> void hoverNoTot(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T f, CharSequence name, double baseValue, boolean keepNops, boolean isResourceProductionBooster) {
        GBox b = (GBox) box;
        if (name != null)
            b.textLL(name);
        b.NL();

        sort.clear();
        int ii = 0;
        for (BoosterAbs<T> l : all) {
            double d = l.get(f);
            if (l.has(f.getClass()) && (d > 0 || (d != 0 && isResourceProductionBooster)) && !l.isMul) {
                sort.add(ii);
            }
            ii++;
        }
        int i = 0;
        for (BoosterAbs<T> l : all) {
            double d = l.get(f);
            if (l.has(f.getClass()) && (d < 0 && !isResourceProductionBooster) && !l.isMul) {
                if (i < sort.size()) {
                    hov(f, b, all.get(sort.get(i)), 0);
                    i++;
                }
                hov(f, b, l, 1);
                b.NL();
            }
        }
        for (; i < sort.size(); i++) {
            hov(f, b, all.get(sort.get(i)), 0);
            b.NL();
        }

        b.NL(4);
        sort.clear();
        ii = 0;
        for (BoosterAbs<T> l : all) {
            double d = l.get(f);
            if (l.has(f.getClass()) && d > 1 && l.isMul) {
                sort.add(ii);
            }
            ii++;
        }
        i = 0;
        for (BoosterAbs<T> l : all) {
            double d = l.get(f);
            if (l.has(f.getClass()) && d < 1 && l.isMul) {
                if (i < sort.size()) {
                    hov(f, b, all.get(sort.get(i)), 0);
                    i++;
                }
                hov(f, b, l, 1);
                b.NL();
            }

        }
        for (; i < sort.size(); i++) {
            hov(f, b, all.get(sort.get(i)), 0);
            b.NL();
        }

        b.NL();

    }

    public static <T> void tot(GUI_BOX box, LIST<? extends BoosterAbs<T>> all, T t, double baseValue, boolean isResourceProductionName) {
        double mul = 1;
        double padd = baseValue > 0 ? baseValue : 0;
        double sub = baseValue < 0 ? baseValue : 0;
        for (BoosterAbs<T> s : all) {
            if (s.has(t.getClass())) {
                if (s.isMul)
                    mul *= s.get(t);
                else {
                    double a = s.get(t);
                    if (a < 0 && !isResourceProductionName)
                        sub += a;
                    else
                        padd += a;
                }
            }

        }
        double tot =padd*mul + sub;

        GBox b = (GBox) box;
        b.tab(1);
        b.textL(Dic.¤¤Total);
        b.tab(5);

        b.add(GFORMAT.f0(b.text(), padd));
        b.add(b.text().add('*'));
        b.add(GFORMAT.f1(b.text(), mul));
        if (sub != 0)
            b.add(GFORMAT.f0(b.text(), sub));
        b.add(b.text().add('='));

        b.add(GFORMAT.fRel(b.text(), tot, baseValue));
        b.NL();

    }

    private static <T> void hov(T f, GBox b, BoosterAbs<T> l, int tab) {

        l.hover(b, l.get(f), tab);

        if (!l.has(f.getClass()))
            b.text("!");
    }

}
