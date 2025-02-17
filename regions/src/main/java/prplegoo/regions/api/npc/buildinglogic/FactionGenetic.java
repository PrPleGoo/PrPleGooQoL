package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import lombok.Getter;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.LOG;
import snake2d.util.rnd.RND;
import world.map.regions.Region;
import world.region.RD;

public class FactionGenetic {
    private final RegionGenetic[] regionGenetics;
    @Getter
    private boolean isMutant = false;

    private FitnessRecord[] fitnessRecords;

    public FactionGenetic(FactionNPC faction) {
        regionGenetics = new RegionGenetic[faction.realm().regions()];
        for (int i = 0; i < regionGenetics.length; i++) {
            regionGenetics[i] = new RegionGenetic(faction.realm().all().get(i).index());
        }
    }

    public void mutate() {
        int max = GeneticVariables.maxMutations;
        while (max > 0 && !isMutant) {
            isMutant = regionGenetics[RND.rInt(regionGenetics.length)].mutate();
            max --;
        }
    }

    public void calculateFitness(FactionNPC faction, double[] buyPrice, double[] sellPrice) {
        fitnessRecords = new FitnessRecord[6];
        // GovPoints;
        fitnessRecords[0] = new FitnessRecord(faction, 0) {
            @Override
            public double determineValue(FactionNPC faction) {
                return Math.min(1, RD.BUILDINGS().costs.GOV.bo.get(faction.capitolRegion()));
            }

            @Override
            public double getFactionDeficitMax(FactionNPC faction) { return -10; }
        };
        // Health;
        fitnessRecords[1] = new FitnessRecord(faction, 1) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return Math.min(RD.HEALTH().boostablee.get(region) - 1, 1);
            }
            @Override
            public double getRegionDeficitMax(FactionNPC faction) { return -0.5; }
        };
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
                double totalMoney = RD.OUTPUT().MONEY.boost.get(faction);

                for (RESOURCE resource : RESOURCES.ALL()) {
                    double productionAmount = KingLevels.getInstance().getDailyProductionRate(faction, resource);
                    if (productionAmount < 0) {
                        if (faction.stockpile.amount(resource.index()) <= Math.abs(productionAmount) * 2) {
                            totalMoney += productionAmount * sellPrice[resource.index()] * 1000000;
                            continue;
                        }

                        totalMoney += productionAmount * sellPrice[resource.index()];
                    } else if (productionAmount > 0) {
                        totalMoney += productionAmount * buyPrice[resource.index()];
                    }
                }

                return totalMoney;
            }

            @Override
            public double getFactionDeficitMax(FactionNPC faction) {
                return (KingLevels.getInstance().getLevel(faction) + 1) * faction.realm().regions() * -50000;
            }
        };
        // Loyalty;
        fitnessRecords[5] = new FitnessRecord(faction, 5) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                double amount = 0;

                for (int i = 0; i < RD.RACES().all.size(); i++) {
                    amount += Math.min(RD.RACES().all.get(i).loyalty.target.get(region), 1);
                }

                return amount;
            }
        };
        // TODO: add slaves to money


        for (FitnessRecord fitnessRecord : fitnessRecords) {
            fitnessRecord.addValue(faction);

            for (int i = 0; i < faction.realm().all().size(); i++) {
                fitnessRecord.addValue(faction, i);
            }
        }
    }

    public boolean shouldKill(FactionNPC faction, FactionGenetic mutant) {
//        LOG.ln("CHECKING mutant.anyFitnessExceedsDeficit IN FactionGenetic;");
        if (mutant.anyFitnessExceedsDeficit(faction)) {
//            LOG.ln("CHECKING mutant.anyFitnessExceedsDeficit IN FactionGenetic;");
            return true;
        }

        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.willIncreaseDeficit(faction, mutant)) {
                return true;
            }
        }

        // we don't have a deficit and neither does the mutant
        double random = GeneticVariables.random();
        for (FitnessRecord fitnessRecord : fitnessRecords) {
            if (fitnessRecord.tryMutation(faction, mutant, random)) {
                return false;
            }
        }

        return true;
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

    private abstract class FitnessRecord {
        private final int index;
        private double factionValue = 0;
        private final double[] regionValues;

        public FitnessRecord(FactionNPC faction, int index) {
            this.index = index;
            regionValues = new double[faction.realm().all().size()];
        }

        public double determineValue(FactionNPC faction, Region region) {
            return 0;
        }

        public double determineValue(FactionNPC faction) {
            return 0;
        }

        public double getFactionDeficitMax(FactionNPC faction) { return Double.NEGATIVE_INFINITY; }

        public double getRegionDeficitMax(FactionNPC faction) { return Double.NEGATIVE_INFINITY; }

        public boolean exceedsDeficit(FactionNPC faction) {
            // Both can be negative infinity, what do
            if (factionValue <= getFactionDeficitMax(faction)) {
//                LOG.ln("exceedsDeficit: " + index + ", factionValue: " + factionValue);
                return true;
            }

            for (int i = 0; i < regionValues.length; i++) {
                if (regionValues[i] <= getRegionDeficitMax(faction)) {
//                    LOG.ln("exceedsDeficit: " + index + ", regionValue: " + regionValue);
                    return true;
                }
            }

            return false;
        }

        public void addValue(FactionNPC faction, int regionIndex) {
            regionValues[regionIndex] += determineValue(faction, faction.realm().all().get(regionIndex));
        }

        public void addValue(FactionNPC faction) {
            factionValue += determineValue(faction);
        }

        public boolean willIncreaseDeficit(FactionNPC faction, FactionGenetic mutant) {
            if (factionValue < 0) {
                return factionValue > mutant.fitnessRecords[index].factionValue;
            }
            if (mutant.fitnessRecords[index].factionValue < 0) {
                return true;
            }

            for (int i = 0; i < faction.realm().all().size(); i++) {
                if (regionValues[i] < 0) {
                    return regionValues[i] > mutant.fitnessRecords[index].regionValues[i];
                }
                if (mutant.fitnessRecords[index].regionValues[i] < 0) {
                    return true;
                }
            }

            return false;
        }

        public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
            if (mutant.fitnessRecords[index].factionValue != factionValue) {
                return (mutant.fitnessRecords[index].factionValue - factionValue) / factionValue > random;
            }
            for (int i = 0; i < faction.realm().all().size(); i++) {
                if (mutant.fitnessRecords[index].regionValues[i] != regionValues[i]
                        && (mutant.fitnessRecords[index].regionValues[i] - regionValues[i]) / regionValues[i] > random) {
                    return true;
                }
            }

            return false;
        }
    }
}
