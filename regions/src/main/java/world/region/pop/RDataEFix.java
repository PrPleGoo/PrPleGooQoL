package world.region.pop;

import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;
import world.region.RData;

public class RDataEFix extends RData.RDataE implements INT_O.INT_OE<Region> {

    public RDataEFix(String key, INT_OE<Region> plocal, RD.RDInit init, CharSequence name) {
        super(key, plocal, init, name);
    }


    @Override
    public void set(Region t, int s) {
        RD.RACES().pop.set(t, RD.RACES().population.get(t) - get(t));
        super.set(t, s);
        RD.RACES().pop.set(t, RD.RACES().population.get(t) + get(t));
    }
}