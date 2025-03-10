package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import settlement.stats.STATS;
import settlement.stats.colls.StatsReligion;
import world.map.regions.Region;
import world.region.RD;

public class FactionGenetic {
    public final RegionGenetic[] regionGenetics;
    public FitnessRecord[] fitnessRecords;

    public FactionGenetic(FactionNPC faction) {
        regionGenetics = new RegionGenetic[faction.realm().regions()];
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i] = new RegionGenetic(faction.realm().all().get(i).index());
        }
    }

    public FactionGenetic loadFitness(FactionNPC faction) {
        fitnessRecords = loadDefault(faction);

        return this;
    }

    public static FitnessRecord[] loadDefault(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[7];
        fitnessRecords[0] = new GovPoints(faction, 0);
        fitnessRecords[1] = new Health(faction, 1);
        // Work force;
        fitnessRecords[2] = new FitnessRecord(faction, 2) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return RD.SLAVERY().getWorkforce().bo.get(region);
            }
            @Override
            public double getRegionDeficitMax(FactionNPC faction) { return -50; }
        };
        // Raiders;
        fitnessRecords[3] = new FitnessRecord(faction, 3) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return -GAME.raiders().entry.get(region).probabilityRaw();
            }
        };
        // Money;
        fitnessRecords[4] = new FitnessRecord(faction, 4) {
            @Override
            public double determineValue(FactionNPC faction) {
                double totalMoney = RD.OUTPUT().MONEY.boost.get(faction) * TIME.secondsPerDay * 2;

                for (RESOURCE resource : RESOURCES.ALL()) {
                    double price = faction.stockpile.price.get(resource);

                    double productionAmount = KingLevels.getInstance().getDailyProductionRate(faction, resource);
                    if (productionAmount < 0) {
                        totalMoney += productionAmount * price;
                    } else if (productionAmount > 0) {
                        totalMoney += productionAmount * price;
                    }
                }

                return totalMoney;
            }

            @Override
            public double getFactionDeficitMax(FactionNPC faction) {
                return -1; //(KingLevels.getInstance().getLevel(faction) + 1) * faction.realm().regions() * -25000;
            }
        };
        // Loyalty;
        fitnessRecords[5] = new Loyalty(faction, 5);
        // Religion;
        fitnessRecords[6] = new FitnessRecord(faction, 6) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                double amount = 0;

                double tolerance = BOOSTABLES.NOBLE().TOLERANCE.get(faction.king().induvidual);
                StatsReligion.StatReligion religiousLikings = STATS.RELIGION().getter.get(faction.king().induvidual);

                for (int i = 0; i < RD.RACES().all.size(); i++) {
                    for (int j = 0; j < RD.RELIGION().all().size(); j++) {
                        amount += religiousLikings.opposition(STATS.RELIGION().ALL.get(j)) * RD.RELIGION().all().get(j).target(region);
                    }
                }

                return amount / tolerance;
            }
        };
        // TODO: add slaves to money

        return fitnessRecords;
    }

    public void calculateFitness(FactionNPC faction) {
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            fitnessRecord.addValue(faction);

            for (int i = 0; i < faction.realm().all().size(); i++) {
                fitnessRecord.addValue(faction, i);
            }
        }
    }

    public boolean shouldAdopt(FactionNPC faction, FactionGenetic mutant) {
//        LOG.ln("CHECKING mutant.anyFitnessExceedsDeficit IN FactionGenetic;");
        if (anyFitnessExceedsDeficit(faction)) {
//            LOG.ln("CHECKING mutant.anyFitnessExceedsDeficit IN FactionGenetic;");
            return false;
        }

        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.willIncreaseDeficit(faction, mutant)) {
                return false;
            }
        }

        // we don't have a deficit and neither does the mutant
        double random = GeneticVariables.random();
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.tryMutation(faction, mutant, random)) {
                return true;
            }
        }

        return false;
    }

    public void commit() {
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i].commit();
        }
    }

    public boolean anyFitnessExceedsDeficit(FactionNPC faction) {
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.exceedsDeficit(faction)) {
                return true;
            }
        }

        return false;
    }
}

