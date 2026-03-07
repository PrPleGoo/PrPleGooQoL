package prplegoo.regions.ui;

import init.settings.S;
import init.sprite.UI.UI;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
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

