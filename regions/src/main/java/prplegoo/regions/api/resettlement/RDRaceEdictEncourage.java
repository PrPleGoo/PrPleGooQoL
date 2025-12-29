package prplegoo.regions.api.resettlement;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpecs;
import game.faction.Faction;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O;
import util.info.INFO;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.pop.RDEdicts;
import world.region.pop.RDEdicts.RDRaceEdict;
import world.region.pop.RDRace;

public class RDRaceEdictEncourage extends RDRaceEdict {
    public RDRaceEdictEncourage(String key, RD.RDInit init, INFO info, SPRITE icon, LIST<RDRace> races) {
        super(key, init, info, icon, races);

        for (RDRace r : races) {
            boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1.0, 2.0, true) {

                @Override
                public double get(Region t) {
                    return RDResettlement.GetEncouragedGrowthTargetMultiplier(t, r);
                }

            }, r.pop.dtarget);
        }
    }
}
