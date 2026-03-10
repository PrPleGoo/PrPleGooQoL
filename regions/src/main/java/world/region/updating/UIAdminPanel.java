package view.world.ui.panels;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;

public final class UIAdminPanel extends ISidePanel{

    public UIAdminPanel(ISidePanels panels){
        titleSet(BOOSTABLES.CIVICS().GOV.name);

        section.addRelBody(2, DIR.S, new GStat() {

            @Override
            public void update(GText text) {
                GFORMAT.iofk(text, (int)BOOSTABLES.CIVICS().GOV.get(FACTIONS.player()), (int)BOOSTABLES.CIVICS().GOV.added(FACTIONS.player()));
            }
        }.hv(Dic.¤¤Available));

        section.body().incrH(16);


        GTableBuilder bu = new GTableBuilder() {

            @Override
            public int nrOFEntries() {
                return FACTIONS.player().realm().regions()-1;
            }

        };

        bu.column(null, 250, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                GuiSection s = new GuiSection() {
                    @Override
                    protected void clickA() {
                        Region r = FACTIONS.player().realm().region(ier.get()+1);
                        if (r != null) {
                            ISidePanel pp = VIEW.world().UI.regions.get(r);
                            panels.clear();
                            panels.add(UIAdminPanel.this, true);
                            panels.add(pp, false);
                        }
                    }
                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        Region r = FACTIONS.player().realm().region(ier.get()+1);
                        if (r != null)
                            VIEW.world().UI.regions.hover(r, text);
                    }

                };
                s.add(new GStat() {

                    @Override
                    public void update(GText text) {
                        Region r = FACTIONS.player().realm().region(ier.get()+1);
                        if (r != null)
                            text.add(r.info.name());
                    }

                }.r(DIR.W));
                s.addRightC(180, new GStat() {

                    @Override
                    public void update(GText text) {
                        Region r = FACTIONS.player().realm().region(ier.get()+1);
                        if (r != null)
                            GFORMAT.iIncr(text, RD.BUILDINGS().costs.GOV.consumed(r));
                        text.s();
                        text.add(RD.BUILDINGS().costs.GOV.consumed(FACTIONS.player()));
                    }
                });

                s.body().setWidth(250);
                s.pad(0, 6);

                return s;
            }
        });

        section.addRelBody(16, DIR.S, bu.createHeight((HEIGHT/2)-section.body().height()-32, true));

        GuiSection estates = new GuiSection();

        estates.addRelBody(0, DIR.S, new GStat() {
            @Override
            public void update(GText text) {
                GFORMAT.iofk(text, RD.ESTATES().getCurrentEstateCount(), (int)RD.ESTATES().maxEstates.get(FACTIONS.player()));
            }
        }.hv("Used estates / Max estates"));

        estates.addRelBody(0, DIR.S, new GStat() {
            @Override
            public void update(GText text) {
                GFORMAT.perc(text, RD.ESTATES().getConsumptionMultiplier());
            }
        }.hv("Estate maintenance rate"));

        GText text = new GText(UI.FONT().S, 8);
        text.setMaxWidth(250);
        text.add("Estates can be built in your realm to generate Gov Points. You have a soft maximum for the amount of estates you can maintain. If you build too many, estates will require 10% extra resources for each estate over the limit.");
        estates.addDown(0, text);

        section.addDown(0, estates);
    }

}
