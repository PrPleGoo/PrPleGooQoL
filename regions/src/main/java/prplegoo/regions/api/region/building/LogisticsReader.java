package prplegoo.regions.api.region.building;

import game.boosting.*;
import game.faction.npc.stockpile.NPCStockpile;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.value.GVALUES;
import init.value.Lockable;
import prplegoo.regions.api.region.rd.RDLogistics;
import prplegoo.regions.api.region.rd.RDOptionalConsumption;
import prplegoo.regions.buildings.supply.ROOM_LOGISTICS;
import settlement.environment.ENVIRONMENT;
import settlement.main.SETT;
import settlement.room.industry.module.IndustryResource;
import settlement.room.infra.transport.ROOM_TRANSPORT;
import settlement.room.main.ROOMS;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.info.GFORMAT;
import util.info.INFO;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.Creator;
import world.region.building.RDBuilding;
import world.region.building.RDBuildingCat;
import world.region.building.RDBuildingLevel;
import world.region.pop.RDRace;

import static settlement.path.AVAILABILITY.ROOM;

public class LogisticsReader {
    public static LIST<RDBuilding> generate(LISTE<RDBuilding> all, RD.RDInit init, RDBuildingCat cat, Json data[]) {
        return generate(all, init, cat, data[0]);
    }

    private static LIST<RDBuilding> generate(LISTE<RDBuilding> all, RD.RDInit init, RDBuildingCat cat, Json data) {
        ArrayList<RDBuilding> count = new ArrayList<>(data.i("COUNT", 1, 5));

        double output = data.d("OUTPUT");
        double credits = data.i("CREDITS", 0, Integer.MAX_VALUE);

        ROOM_TRANSPORT blue = SETT.ROOMS().TRANSPORT;

        String desc = "The " + blue.info.name + " allows you to move a single resource from this region to others. It works by loading output of this region into a separate storage. Before regions request resources from the capital, they will use the stockpile from loading stations in your empire.";

        for (int ci = 0; ci < count.max(); ci++) {
            String kkk = blue.key + "_" + (ci + 1);
            ArrayList<RDBuildingLevel> levels = new ArrayList<>(data.i("LEVELS", 1, 10));
            for (int li = 0; li < levels.max(); li++) {
                CharSequence name = blue.info.name + ": " + GFORMAT.toNumeral(new Str(4), li + 1);
                Icon icon = blue.iconBig();

                double d = (double) (li + 1) / (levels.max());

                Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + cat.key + "_" + kkk + "_" + (li + 1), name, desc, icon);
                RDBuildingLevel l = Creator.RDBuildingLevel(name, icon, needs);
                l.cost = (int) (NPCStockpile.AVERAGE_PRICE * credits * d);

                BSourceInfo info = new BSourceInfo(blue.info.name, blue.icon);

                for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
                    RESOURCE resource = RESOURCES.ALL().get(ri);

                    BoosterValue logistics = new RDLogistics.RDEnabledShipmentBooster(BValue.VALUE1, info, d * output, ci, resource.index());

                    l.local.push(logistics, RD.LOGISTICS().get(resource).boost);
                }

                levels.add(l);
            }

            INFO info = new INFO(blue.info.name, desc);

            RDBuilding b = Creator.RDBuilding(all, init, cat, kkk, info, levels, false, false, kkk, blue);

            BoostSpecs sp = new BoostSpecs(blue.info.name, blue.icon, false);
            sp.read(data, BValue.VALUE1);
            ACTION a = new ACTION() {
                final BSourceInfo info = new BSourceInfo(blue.info.name, blue.icon);
                @Override
                public void exe() {
                    for (int i = 1; i < b.levels.size(); i++) {
                        for (BoostSpec s : sp.all()) {
                            double am = (s.booster.to()*i/(b.levels.size()-1));
                            b.levels.get(i).local.push(new BoosterValue(BValue.VALUE1, info, am, s.booster.isMul), s.boostable);
                        }
                    }
                }
            };

            BOOSTING.connecter(a);
        }
        return all;
    }
}
