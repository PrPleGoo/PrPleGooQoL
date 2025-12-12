package prplegoo.regions.api.gen;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
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
        int armies = CLAMP.i(1 + menTarget/10000, 1, 3);

        while(f.armies().all().size() < armies && f.armies().canCreate()) {
            Region r = f.realm().all().rnd();
            COORDINATE c = WORLD.PATH().rnd(r);
            WORLD.ENTITIES().armies.create(c.x(), c.y(), f);
        }

        if (f.armies().all().isEmpty()) {
            return;
        }

        int menPerArmy = menTarget / f.armies().all().size();

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
                    division.target.menSet(divisionSize - 10);
                    if (division.menTarget() == 0) {
                        division.disband();
                        i--;

                        continue;
                    }
                } else if (divisionSize < Config.battle().MEN_PER_DIVISION
                        && getArmySize(army) < menPerArmy){
                    int availableConscripts = AD.conscripts().available(division.race()).get(f);
                    int totalPips = (availableConscripts + divisionSize) / 10;

                    int maxSizeToSet = Math.min(totalPips * 10, Config.battle().MEN_PER_DIVISION);

                    if (maxSizeToSet != divisionSize) {
                        division.target.menSet(maxSizeToSet);
                    }
                }

                double trainingTarget = 0.4 * BOOSTABLES.NOBLE().COMPETANCE.get(f.court().king().roy().induvidual);
                for (StatsBattle.StatTraining trainingType : STATS.BATTLE().TRAINING_ALL) {
                    division.target.trainingSet(trainingType, trainingTarget);
                }

                for (EquipBattle equipment : STATS.EQUIP().BATTLE_ALL()) {
                    double currentPips = division.target.equip(equipment);

                    if (currentPips == 0) {
                        continue;
                    }

                    if (AD.supplies().get(equipment).amountValue(army) < 0.75) {
                        division.target.equipSet(equipment, 0.0);

                        continue;
                    }

//                    if (f.stockpile.amount(equipment.resource) > division.menTarget()
//                            && division.target.equip(equipment) < 1.0) {
//                        division.target.equipSet(equipment, 1.0);
//                    }
                }

                fillOpenSlots(division, f, army);
            }

            for (Race race : RACES.all()) {
                if (army.divs().size() >= Config.battle().DIVISIONS_PER_ARMY) {
                    break;
                }

                int availableConscripts = AD.conscripts().available(race).get(f);

                if (availableConscripts < 11) {
                    continue;
                }

                if (AD.supplies().supplyEquip(army) < 0.9 && army.divs().size() > 0) {
                    continue;
                }

                AD.regional().create(race, 10.0 /Config.battle().MEN_PER_DIVISION, army);
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

    private static int getArmySize(WArmy army) {
        int total = 0;
        for(int i = 0; i < army.divs().size(); i++) {
            total += army.divs().get(i).menTarget();
        }

        return total;
    }

    private static void fillOpenSlots(WDivRegional division, FactionNPC f, WArmy army) {
        double[] slotsUsed = new double[EquipBattle.SLOTS];
        for (EquipBattle equipment : STATS.EQUIP().BATTLE_ALL()) {
            if (division.target.equip(equipment) > 0) {
                for (int i = 0; i < EquipBattle.SLOTS; i ++) {
                    slotsUsed[i] += equipment.slotUse(i);
                }
            }
        }

        int randomIndex = RND.rInt(STATS.EQUIP().BATTLE_ALL().size());
        nextEquipment:
        for (int e = 0; e < STATS.EQUIP().BATTLE_ALL().size(); e++) {
            EquipBattle equipment = STATS.EQUIP().BATTLE_ALL().get((e + randomIndex) % STATS.EQUIP().BATTLE_ALL().size());

            if (!equipment.allowed(division.race())) {
               continue;
            }

            for (int i = 0; i < EquipBattle.SLOTS; i ++) {
                if (slotsUsed[i] + equipment.slotUse(i) > 1) {
                    continue nextEquipment;
                }
            }

            division.target.equipSet(equipment, 1.0);

            break;
        }
    }
}
