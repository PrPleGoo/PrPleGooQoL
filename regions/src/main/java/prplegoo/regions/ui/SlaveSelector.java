package prplegoo.regions.ui;

import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.GButt;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public class SlaveSelector extends GuiSection {
    public SlaveSelector(GETTER.GETTER_IMP<Region> region){
        int i = 0;
        for (RDRace e : RD.RACES().all) {

            GButt.ButtPanel b = new GButt.ButtPanel(e.race.appearance().icon) {

                @Override
                protected void renAction() {
                    selectedSet(RD.SLAVERY().has(region.get(), e));
                }

                @Override
                protected void clickA() {
                    RD.SLAVERY().toggleSlave(region.get(), e);
                }
            };
            b.pad(4, 4);

            add(b, (i % 4) * b.body().width(), (i / 4) * b.body().height());
            i++;
        }
    }
}
