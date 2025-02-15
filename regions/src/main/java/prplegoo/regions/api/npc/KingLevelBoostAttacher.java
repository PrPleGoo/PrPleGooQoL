package prplegoo.regions.api.npc;

import game.boosting.BSourceInfo;
import game.faction.npc.FactionNPC;
import init.sprite.SPRITES;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;

public class KingLevelBoostAttacher {
    public static void attachKingLevelBoosts() {
        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 40000, false) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 0;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 0;
                }

                FactionNPC faction = (FactionNPC) t.faction();

                return (KingLevels.getInstance().getKingLevel(faction).getGovPoints()
                        + KingLevels.getInstance().getKingLevel(faction).getGovPointsPerRegion() * faction.realm().regions())
                        / 40000;
            }
        }.add(RD.BUILDINGS().costs.GOV.bo);

        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 10, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 1;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 1;
                }

                FactionNPC faction = (FactionNPC) t.faction();

                return KingLevels.getInstance().getKingLevel(faction).getCapitalPopulationCapacityMul() / 10.0;
            }
        }.add(RD.RACES().capacity);

        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 1000000, false) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 0;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 0;
                }

                FactionNPC faction = (FactionNPC) t.faction();

                return KingLevels.getInstance().getKingLevel(faction).getIncome() / 1000000;
            }
        }.add(RD.OUTPUT().MONEY.boost);
    }
}
