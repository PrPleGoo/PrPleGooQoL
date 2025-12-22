package prplegoo.regions.api.gen;

import init.race.Race;
import init.type.CLIMATES;
import init.type.TERRAINS;
import snake2d.LOG;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

import java.util.Arrays;

public class RacePreferenceCache {
    private static RacePreferenceCache _instance;

    public static RacePreferenceCache getInstance() {
        if (_instance == null) {
            _instance = new RacePreferenceCache();
        }

        return _instance;
    }

    public static void Reset() {
        _instance = null;
    }

    private final double[][] _regionRacePreference;

    private RacePreferenceCache() {
        _regionRacePreference = new double[WORLD.REGIONS().all().size()][RD.RACES().all.size()];

        Initialize();
    }

    private void Initialize() {
        for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
        {
            Region reg = WORLD.REGIONS().all().get(r);
            for(int ra = 0; ra < RD.RACES().all.size(); ra++)
            {
                Race race = RD.RACES().all.get(ra).race;
                // RDRace.biome calculation
                double c = 0;
                for (int i = 0; i < CLIMATES.ALL().size(); i++)
                    c += reg.info.climate(CLIMATES.ALL().get(i))*race.population().climate(CLIMATES.ALL().get(i));

                double t = 0;
                for (int i = 0; i < TERRAINS.ALL().size(); i++)
                    t += reg.info.terrain(TERRAINS.ALL().get(i))*race.population().terrain(TERRAINS.ALL().get(i));

                _regionRacePreference[r][ra] = c*t;
            }
        }

        double[] regionRacePreferenceSum = new double[RD.RACES().all.size()];
        Arrays.fill(regionRacePreferenceSum, 0);
        for(int ra = 0; ra < RD.RACES().all.size(); ra++)
        {
            for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
            {
                regionRacePreferenceSum[ra] += _regionRacePreference[r][ra];
            }
        }

        double totalSum = 0.0;
        double[] regionRacePreferenceSumAfterCorrection = new double[RD.RACES().all.size()];
        Arrays.fill(regionRacePreferenceSumAfterCorrection, 0);
        for(int ra = 0; ra < RD.RACES().all.size(); ra++)
        {
            double raceMultiplier = regionRacePreferenceSum[ra] / WORLD.REGIONS().all().size();
            for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
            {
                _regionRacePreference[r][ra] *= raceMultiplier;

                regionRacePreferenceSumAfterCorrection[ra] += _regionRacePreference[r][ra];
                totalSum += _regionRacePreference[r][ra];
            }
        }

        double highest = -1;
        for(int ra = 0; ra < RD.RACES().all.size(); ra++)
        {
            double raceMultiplier = regionRacePreferenceSumAfterCorrection[ra] / totalSum;
            for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
            {
                _regionRacePreference[r][ra] /= raceMultiplier;

                if (_regionRacePreference[r][ra] > highest)
                {
                    highest = _regionRacePreference[r][ra];
                }
            }
        }

        double multiplier = 1.0 / highest;
        for(int ra = 0; ra < RD.RACES().all.size(); ra++)
        {
            double rarity = RD.RACES().all.get(ra).race.getPopulation().max;
            for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
            {
                _regionRacePreference[r][ra] *= multiplier * rarity;
            }
        }

        if (false)
        {
            Arrays.fill(regionRacePreferenceSum, 0);
            for(int ra = 0; ra < RD.RACES().all.size(); ra++)
            {
                for(int r = 0; r < WORLD.REGIONS().all().size(); r++)
                {
                    regionRacePreferenceSum[ra] += _regionRacePreference[r][ra];
                }
            }

            LOG.ln("Region count:");
            LOG.ln(WORLD.REGIONS().all().size());

            for(int ra = 0; ra < RD.RACES().all.size(); ra++)
            {
                LOG.ln(RD.RACES().all.get(ra).race.info.name);
                LOG.ln(regionRacePreferenceSum[ra]);
            }
        }
    }

    public double getRacePreference(Region region, RDRace rdRace) {
        return _regionRacePreference[region.index()][rdRace.index()];
    }
}
