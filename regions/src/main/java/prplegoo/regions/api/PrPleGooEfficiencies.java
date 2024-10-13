package prplegoo.regions.api;

import game.boosting.*;
import init.resources.Minable;
import init.type.TERRAINS;
import snake2d.util.misc.CLAMP;
import util.dic.Dic;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class PrPleGooEfficiencies {
    public static RBooster POP_SCALING(RDBuilding bu) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return 1.0;
            }
        };
    }

    public static RBooster POP_SCALING_WOOD(RDBuilding bu) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return t.info.terrain(TERRAINS.FOREST());
            }

            @Override
            public int getAssignedWorkforceLimit(Region t) {
                return t.info.area() * 15;
            }
        };
    }

    public static RBooster POP_SCALING_MINABLE(RDBuilding bu, Minable minable) {
        return new PopScalerBooster(bu) {
            @Override
            public double getLimitScaler(Region t) {
                return t.info.minableD(minable);
            }

            @Override
            public int getAssignedWorkforceLimit(Region t) {
                return 150;
            }
        };
    }

    public static void MINABLE(RDBuilding bu, Minable minable, double from, double to) {
        bu.baseFactors.add(new RBooster(new BSourceInfo(Dic.¤¤Minerals, minable.resource.icon()), from, to, true) {
            @Override
            public double get(Region t) {
                return CLAMP.d(t.info.minableD(minable) / 2, from, to);
            }
        }.add(bu.efficiency));
    }

    public static void SLAVERY(RDBuilding bu, double from, double to) {
        for (RDRace rdRace : RD.RACES().all) {
            bu.boosters().push(new RBooster(new BSourceInfo(Dic.¤¤Population, rdRace.race.appearance().icon), from, to, true) {
                @Override
                public double get(Region t) {
                    return rdRace.pop.get(t) / 100.0;
                }

            }, RD.SLAVERY().boostable(rdRace));
        }
    }
}
