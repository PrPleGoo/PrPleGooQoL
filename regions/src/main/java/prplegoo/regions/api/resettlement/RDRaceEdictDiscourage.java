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
import world.region.pop.RDRace;

public class RDRaceEdictDiscourage extends RDEdicts.RDRaceEdict {
    public RDRaceEdictDiscourage(String key, RD.RDInit init, INFO info, SPRITE icon, LIST<RDRace> races) {
        super(key, init, info, icon, races);
        for (RDRace r : races) {
            boosts.push(new RBooster(new BSourceInfo(info.name, icon), 0, 1, true) {

                @Override
                public double get(Region t) {
                    return RDResettlement.GetDiscouragedLoyaltyMultiplier(t, r);
                }

            }, r.loyalty.target);

            boosts.push(new RBooster(new BSourceInfo(info.name, icon), 0.5, 1.0, true) {

                @Override
                public double get(Region t) {
                    return RDResettlement.GetDiscouragedGrowthTargetMultiplier(t, r);
                }

            }, r.pop.dtarget);
        }
    }

    public INT_O.INT_OE<Region> toggled(RDRace r){
        return toggled.get(r.index());
    }

    public INT_O.INT_OE<Faction> realm(RDRace r){
        return realm.get(r.index());
    }
}
