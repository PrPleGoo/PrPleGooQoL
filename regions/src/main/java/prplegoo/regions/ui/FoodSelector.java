package prplegoo.regions.ui;

import init.resources.RESOURCES;
import init.resources.ResG;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.GButt;
import world.map.regions.Region;
import world.region.RD;

public class FoodSelector extends GuiSection {
    public FoodSelector(GETTER.GETTER_IMP<Region> region){
        int i = 0;
        for (ResG e : RESOURCES.EDI().all()) {

            GButt.ButtPanel b = new GButt.ButtPanel(e.resource.icon()) {

                @Override
                protected void renAction() {
                    selectedSet(RD.FOOD_CONSUMPTION().has(region.get(), e.resource));
                }

                @Override
                protected void clickA() {
                    RD.FOOD_CONSUMPTION().toggleFood(region.get(), e.resource);
                }
            };
            b.pad(4, 4);

            add(b, (i % 4) * b.body().width(), (i / 4) * b.body().height());
            i++;
        }
    }
}
