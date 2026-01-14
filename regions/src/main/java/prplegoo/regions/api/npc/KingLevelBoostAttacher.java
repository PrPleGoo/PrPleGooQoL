package prplegoo.regions.api.npc;

import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.sprite.SPRITES;
import snake2d.util.misc.CLAMP;
import world.army.AD;
import world.army.ADConscripts;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RDHealth;

public class KingLevelBoostAttacher {
    public static void attachKingLevelBoosts() {
        if(!KingLevels.isActive()){
            return;
        }
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
                        + KingLevels.getInstance().getKingLevel(faction).getGovPointsPerRegion() * (faction.realm().regions() - 1))
                        / 40000;
            }
        }.add(RD.BUILDINGS().costs.GOV.bo);

        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 10, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 1 / max();
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 1 / max();
                }

                if (!t.capitol()) {
                    return 1 / max();
                }

                FactionNPC faction = (FactionNPC) t.faction();

                return KingLevels.getInstance().getKingLevel(faction).getCapitalPopulationCapacityMul() / max();
            }
        }.add(RD.RACES().capacity);

        new RBooster(new BSourceInfo("Bad capital compensation", SPRITES.icons().s.crown), 0, 10, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 1 / max();
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 1 / max();
                }

                if (!t.capitol()) {
                    return 1 / max();
                }

                double mul = 1.0;

                double area = t.info.area();
                if (area < RD.RACES().getMaxArea()) {
                    mul *= Math.max(1, (RD.RACES().getMaxArea() - 20) / area);
                }
                if (t.info.moisture() < 1) {
                    mul *= Math.max(1, 1.0 / ((t.info.moisture() * 1.2) + 0.2));
                }

                return mul / max();
            }
        }.add(RD.RACES().capacity);

        new RBooster(new BSourceInfo("King level, player scaling", SPRITES.icons().s.crown), 0, 40, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 1 / max();
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 1 / max();
                }

                return (1 + (KingLevels.getInstance().getPlayerScalingD() / 2)) / max();
            }
        }.add(RD.MILITARY().conscriptTarget);

        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 1, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 1;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 1;
                }

                return KingLevels.getInstance().getKingLevel((FactionNPC) t.faction()).getConscriptMul();
            }
        }.add(RD.MILITARY().conscriptTarget);

        new RBooster(new BSourceInfo("King level, player scaling", SPRITES.icons().s.crown), 1, 40, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 0;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 0;
                }

                return KingLevels.getInstance().getPlayerScalingD() / max();
            }
        }.add(RD.OUTPUT().MONEY.boost);

        new RBooster(new BSourceInfo("King level", SPRITES.icons().s.crown), 0, 1000000, false) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 0;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 0;
                }

                if (t.faction().capitolRegion().index() != t.index()) {
                    return 0;
                }

                FactionNPC faction = (FactionNPC) t.faction();

                return BOOSTABLES.NOBLE().COMPETANCE.get(faction.king().induvidual) * KingLevels.getInstance().getKingLevel(faction).getIncome() / 1000000;
            }
        }.add(RD.OUTPUT().MONEY.boost);

        new RBooster(new BSourceInfo("NPC Faction Capital boost", SPRITES.icons().s.crown), 1, 2, true) {
            @Override
            public double get(Region t) {
                if (!KingLevels.isActive()) {
                    return 0;
                }

                if (!(t.faction() instanceof FactionNPC)) {
                    return 0;
                }

                if (t.faction().capitolRegion().index() != t.index()) {
                    return 0;
                }

                return 1;
            }
        }.add(RD.SLAVERY().getWorkforce().bo);
    }
}
