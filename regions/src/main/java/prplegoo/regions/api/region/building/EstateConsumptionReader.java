package prplegoo.regions.api.region.building;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.api.region.rd.RDEstates;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.building.RDBuildingLevel;

public class EstateConsumptionReader {
    private static final String key = "ESTATE_CONSUMPTION";
    public static void read(Json[] jsons, BValue value, RDBuilding b) {
        ACTION a = new ACTION() {
            @Override
            public void exe() {
                for (RESOURCE resource : RESOURCES.ALL()) {
                    ConsumptionEfficiencyBo efficiencyBo = new ConsumptionEfficiencyBo(b, resource.index());
                    boolean add = false;
                    for (int i = 0; i < jsons.length; i++) {
                        int level = i+1;
                        RDBuildingLevel l = b.levels.get(level);
                        Json j = jsons[i];

                        if (!j.has(key)) {
                            return;
                        }

                        Json estateConsumption = j.json(key);
                        BSourceInfo info = new BSourceInfo(b.info.name, b.icon());

                        for (String key : estateConsumption.keys()) {
                            if (!resource.key.equals(key)) {
                                continue;
                            }

                            add = true;
                            efficiencyBo.register(level);
                            BoosterValue consumption = new RDEstates.RDEstateBooster(value, info, estateConsumption.d(key));

                            l.local.push(consumption, RD.INPUTS().get(resource));
                        }
                    }

                    if (add) {
                        efficiencyBo.add(b.efficiency);
                    }
                }
            }
        };

        BOOSTING.connecter(a);
    }
}

