package prplegoo.regions.api.region.rd;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
import init.resources.Growable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.MATH;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RDOutputs;
import world.region.pop.RDRace;

public final class RDInputs {
    public final RDInput[] ALL;

    public RDInputs() {
        ALL = new RDInput[RESOURCES.ALL().size()];
        for (RESOURCE res : RESOURCES.ALL()) {
            ALL[res.index()] = new RDInput(res);
        }
    }

    public Boostable get(RESOURCE res){
        return ALL[res.index()].boost;
    }

    public static class RDInput {

        public final Boostable boost;

        public final RESOURCE res;

        RDInput(RESOURCE res) {
            this.boost = BOOSTING.push("RESOURCE_CONSUMPTION_" + res.key, 0, Dic.¤¤Consumed + ": " + res.names, res.desc, res.icon(),  BoostableCat.ALL().WORLD_PRODUCTION);
            this.res = res;
        }

        public int getDelivery(Region reg) {
            return (int) boost.get(reg);
        }
    }
}
