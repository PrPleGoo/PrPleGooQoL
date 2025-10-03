package prplegoo.regions.api.gen;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.ResSupply;
import prplegoo.regions.api.npc.KingLevels;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.army.AD;
import world.army.ADSupplies;
import world.army.ADSupply;
import world.army.WDivRegional;
import world.entity.army.WArmy;
import world.map.regions.Region;

public class KingLevelRecruiter {
    private static final Tree<WArmy> tree = new Tree<WArmy>(100) {

        @Override
        protected boolean isGreaterThan(WArmy current, WArmy cmp) {
            if (AD.menTarget(null).get(current) > AD.menTarget(null).get(cmp))
                return true;
            return current.armyIndex() > cmp.armyIndex();
        }

    };

    public static void recruit(FactionNPC f) {
        if (f.realm().all().size() == 0)
            return;

        int menTarget = AD.conscripts().total(null).get(f);
        // increasing the max needs really good testing, maybe it causes a save issue
        int armies = CLAMP.i(1 + menTarget/5000, 1, 3);

        while(f.armies().all().size() < armies && f.armies().canCreate()) {
            Region r = f.realm().all().rnd();
            COORDINATE c = WORLD.PATH().rnd(r);
            WORLD.ENTITIES().armies.create(c.x(), c.y(), f);
        }

        tree.clear();
        for (int ai = 0; ai < f.armies().all().size(); ai++) {
            WArmy a = f.armies().all().get(ai);
            tree.add(a);
        }

        while(tree.hasMore()) {
            WArmy army = tree.pollGreatest();
            for (int i = 0; i < army.divs().size(); i++) {
                WDivRegional division = (WDivRegional) army.divs().get(i);

                int divisionSize = division.menTarget();
                if (AD.supplies().supplyEquip(army) < 0.9
                        || AD.conscripts().available(division.race()).get(f) <= 0) {
                    division.menTargetSet(divisionSize - 15);
                    if (division.menTarget() == 0) {
                        division.disband();
                        i--;

                        continue;
                    }
                } else if (divisionSize < Config.BATTLE.MEN_PER_DIVISION){
                    int availableConscripts = AD.conscripts().available(division.race()).get(f);
                    int totalPips = (availableConscripts + divisionSize) / 15;

                    int maxSizeToSet = Math.min(totalPips * 15, Config.BATTLE.MEN_PER_DIVISION);

                    if (maxSizeToSet != divisionSize) {
                        division.menTargetSet(maxSizeToSet);
                    }
                }

                double trainingTarget = 0.4 * BOOSTABLES.NOBLE().COMPETANCE.get(f.court().king().roy().induvidual);
                for (StatsBattle.StatTraining trainingType : STATS.BATTLE().TRAINING_ALL) {
                    division.trainingTargetSet(trainingType, trainingTarget);
                }

                for (EquipBattle equipment : STATS.EQUIP().BATTLE_ALL()) {
                    int currentPips = division.equipTarget(equipment);
                    if (currentPips == 0) {
                        continue;
                    }

                    if (AD.supplies().get(equipment).amountValue(army) < 0.75) {
                        division.equipTargetset(equipment, currentPips - 1);

                        continue;
                    }

                    if (f.stockpile.amount(equipment.resource) > 0
                            && equipment.equipMax > division.equipTarget(equipment)) {
                        division.equipTargetset(equipment, currentPips + 1);
                    }
                }

                fillOpenSlots(division);
            }

            for (Race race : RACES.all()) {
                int availableConscripts = AD.conscripts().available(race).get(f);

                if (availableConscripts < 16) {
                    continue;
                }

                if (AD.supplies().supplyEquip(army) < 0.9 && army.divs().size() > 0) {
                    continue;
                }

                AD.regional().create(race, 15.0 /Config.BATTLE.MEN_PER_DIVISION, army);
            }

            ADSupplies.ADArtillery arts = AD.supplies().arts().rnd();
            if (AD.supplies().isMissingArtsEquip(arts, army)) {
                arts.target.inc(army, -1);
            } else {
                int artsTargetPerArmy = (int) (menTarget * BOOSTABLES.NOBLE().AGRESSION.get(f.court().king().roy().induvidual)) / 250;

                if (arts.target.get(army) < artsTargetPerArmy) {
                    boolean shouldAdd = true;
                    for (ADSupply supply : arts.sups()) {
                        if (f.stockpile.amount(supply.res) <= 0) {
                            shouldAdd = false;
                            break;
                        }
                    }

                    if (shouldAdd) {
                        arts.target.inc(army, 1);
                    }
                }
            }
        }
    }

    private static void fillOpenSlots(WDivRegional division) {
        double[] slotsUsed = new double[EquipBattle.SLOTS];
        for (EquipBattle equipment : STATS.EQUIP().BATTLE_ALL()) {
            if (division.equip(equipment) > 0) {
                for (int i = 0; i < EquipBattle.SLOTS; i ++) {
                    slotsUsed[i] += equipment.slotUse(i);
                }
            }
        }

        int randomIndex = RND.rInt(STATS.EQUIP().BATTLE_ALL().size());
        nextEquipment:
        for (int e = 0; e < STATS.EQUIP().BATTLE_ALL().size(); e++) {
            EquipBattle equipment = STATS.EQUIP().BATTLE_ALL().get((e + randomIndex) % STATS.EQUIP().BATTLE_ALL().size());

            for (int i = 0; i < EquipBattle.SLOTS; i ++) {
                if (slotsUsed[i] + equipment.slotUse(i) > 1) {
                    continue nextEquipment;
                }
            }

            division.equipTargetset(equipment, 1);
            break;
        }
    }
}
