package prplegoo.regions.api.resettlement;

import game.faction.FACTIONS;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDEdicts;
import world.region.pop.RDRace;

public class RDResettlement {

    private static RDEdicts edicts() {
        return RD.RACES().edicts;
    }

    public static int GetEncouragedCount(RDRace race) {
        int sum = 0;

        for (Region region : FACTIONS.player().realm().all()) {
            sum += IsEncouraged(region, race) ? 1 : 0;
        }

        return sum;
    }

    private static boolean IsEncouraged(Region region, RDRace race) {
        return edicts().sanction.toggled(race).get(region) == 1;
    }

    private static int GetEncourageDeficit(RDRace race) {
        return GetDiscouragedCount(race) - GetEncouragedCount(race);
    }

    public static int GetDiscouragedCount(RDRace race) {
        int sum = 0;

        for (Region region : FACTIONS.player().realm().all()) {
            sum += IsDiscouraged(region, race) ? 1 : 0;
        }

        return sum;
    }

    private static boolean IsDiscouraged(Region region, RDRace race) {
        return edicts().exile.toggled(race).get(region) == 1;
    }

    private static int GetDiscourageDeficit(RDRace race) {
        return GetEncouragedCount(race) - GetDiscouragedCount(race);
    }

    private static final int CAP = 10;
    public static double GetEncouragedGrowthTargetMultiplier(Region region, RDRace race) {
        if (!IsEncouraged(region, race)) {
            return 0.0;
        }

        double value = CLAMP.i(CAP - GetDiscourageDeficit(race), 0, CAP);

        return value / CAP;
    }

    public static double GetDiscouragedGrowthTargetMultiplier(Region region, RDRace race) {
        if (!IsDiscouraged(region, race)) {
            return 1.0;
        }

        return 0.0;
    }

    public static double GetDiscouragedLoyaltyMultiplier(Region region, RDRace race) {
        if (!IsDiscouraged(region, race)) {
            return 1.0;
        }

        double value = CLAMP.i(CAP - GetEncourageDeficit(race), 0, CAP);

        return value / CAP;
    }
}

