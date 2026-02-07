package prplegoo.regions.api.resettlement;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpecs;
import game.boosting.BoosterImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.misc.CLAMP;
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

public class RDRaceEdictMassacre extends RDEdicts.RDRaceEdict {
    public RDRaceEdictMassacre(String key, RD.RDInit init, INFO info, SPRITE icon, LIST<RDRace> races, double loyalty, double growth) {
        super(key, init, info, icon, races);

        for (RDRace r : races) {
            boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-loyalty, true) {

                @Override
                public double get(Region t) {
                    return toggled.get(r.index()).get(t);
                }

            }, r.loyalty.target);

            boosts.push(new BoosterImp(new BSourceInfo(RDEdicts.get¤¤Distant() + ": " + info.name, icon), 1, 1.0-loyalty, true) {

                @Override
                public double vGet(Region t) {
                    if (t.faction() != null && realm.get(r.index()).get(t.faction()) > 0)
                        return CLAMP.d(realm.get(r.index()).getD(t.faction()), 0, 1);
                    return 0;
                }

                @Override
                public double vGet(Faction f) {
                    return CLAMP.d(realm.get(r.index()).getD(f), 0, 1);
                }


            }, r.loyalty.target);

            boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-growth, true) {

                @Override
                public double get(Region t) {
                    return toggled.get(r.index()).get(t);
                }

            }, r.pop.dtarget);
        }
    }
}

