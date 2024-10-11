package prplegoo.regions.api;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpec;
import init.resources.Minable;
import init.sprite.UI.UI;
import init.type.TERRAINS;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrPleGooEfficiencies {
    public static void POP_SCALING(RDBuilding bu) {
        bu.baseFactors.add(new PopScalerBooster(bu){
            @Override
            public double getLimitScaler(Region t){
                return 1.0;
            }

            @Override
            public int getAssignedWorkforceLimit(Region t){
                return -1;
            }
        }.add(bu.efficiency));
    }

    public static void POP_SCALING_WOOD(RDBuilding bu) {
        bu.baseFactors.add(new PopScalerBooster(bu){
            @Override
            public double getLimitScaler(Region t){
                return t.info.terrain(TERRAINS.FOREST());
            }

            @Override
            public int getAssignedWorkforceLimit(Region t){
                return t.info.area() * 15;
            }
        }.add(bu.efficiency));
    }

    public static void POP_SCALING_MINABLE(RDBuilding bu, Minable minable) {
        bu.baseFactors.add(new PopScalerBooster(bu){
            @Override
            public double getLimitScaler(Region t){
                return t.info.minableD(minable);
            }

            @Override
            public int getAssignedWorkforceLimit(Region t){
                return 200;
            }
        }.add(bu.efficiency));
    }
}
