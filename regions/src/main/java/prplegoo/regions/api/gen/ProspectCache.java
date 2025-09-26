package prplegoo.regions.api.gen;

import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.room.food.farm.ROOM_FARM;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.food.pasture.ROOM_PASTURE;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintImp;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.Arrays;

public class ProspectCache {
    private static ProspectCache _instance;

    public static ProspectCache getInstance() {
        if (_instance == null) {
            _instance = new ProspectCache();
        }

        return _instance;
    }

    private final int[][] _regionBuildingLevelCap;

    private ProspectCache() {
        _regionBuildingLevelCap = new int[WORLD.REGIONS().all().size()][SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];

        for (int[] buildingLevelCap : _regionBuildingLevelCap) {
            Arrays.fill(buildingLevelCap, -1);
        }
    }

    public double getLevelCap(Region region, RDBuilding building) {
        if (_regionBuildingLevelCap[region.index()][building.index()] == -1) {
            _regionBuildingLevelCap[region.index()][building.index()] = CLAMP.i(calculateProspectValue(region, building), 1, 10);
        }

        return (double) _regionBuildingLevelCap[region.index()][building.index()] / 10;
    }

    private int calculateProspectValue(Region region, RDBuilding building) {
        RoomBlueprintImp blue = building.getBlue();

        if (((INDUSTRY_HASER) blue).industries().size() > 1) {
            LOG.err("RDBuilding: " + building.info.name + " calculating prospect value, but more than 1 recipe.");
        }

        if (blue.cat.name().equals("Mines")) {
            return calculateMinesProspectValue(region, blue);
        }

        if (blue.cat.name().equals("Farms")) {
            return calculateFarmProspectValue(region, blue);
        }

        if (blue.cat.name().equals("Aquaculture")) {
            return calculateAquacultureProspectValue(region);
        }

        if (blue.cat.name().equals("Husbandry")) {
            return calculateHusbandryProspectValue(region, blue);
        }

        if (building.info.name.equals("Woodcutter")) {
            return calculateWoodcutterProspectValue(region);
        }

        LOG.err("RDBuilding: " + building.info.name + " no prospect calculation done.");

        return 10;
    }

    private int calculateMinesProspectValue(Region region, RoomBlueprintImp blue) {
        ROOM_MINE mine = (ROOM_MINE) blue;

        double result = 0.0;
        for (int i = 0; i < TERRAINS.ALL().size(); i++) {
            TERRAIN terrain = TERRAINS.ALL().get(i);

            result += mine.minable.terrain(terrain) * region.info.terrain(terrain) * region.info.area();
        }


        return (int) Math.round(result / 6);
    }

    private int calculateFarmProspectValue(Region region, RoomBlueprintImp blue) {
        if (blue instanceof ROOM_ORCHARD) {
            double moistureFactor = Math.min(region.info.moisture(), 0.5) + 0.5;

            double forestedArea = region.info.area() * region.info.terrain(TERRAINS.FOREST());
            double clearArea = region.info.area() * region.info.terrain(TERRAINS.NONE());
            double countedArea = Math.min(120, (forestedArea / 2) + clearArea);

            double result = countedArea * moistureFactor;

            return (int) Math.round(result / 12);
        }

        ROOM_FARM farm = (ROOM_FARM) blue;

        if (farm.constructor().mustBeIndoors()) {
            double result = region.info.terrain(TERRAINS.MOUNTAIN()) * region.info.area();

            return (int) Math.round(result / 4);
        }

        double forestedArea = region.info.area() * region.info.terrain(TERRAINS.FOREST());
        double clearArea = region.info.area() * region.info.terrain(TERRAINS.NONE());
        double countedArea = Math.min(80, (forestedArea / 2) + clearArea);

        double result = countedArea * region.info.moisture();

        return (int) Math.round(result / 8);
    }

    private int calculateAquacultureProspectValue(Region region) {
        double ocean = region.info.terrain(TERRAINS.OCEAN());
        double wet = region.info.terrain(TERRAINS.WET());

        double result = ((ocean + wet) * region.info.area());

        return (int) Math.round(result / 8);
    }

    private int calculateHusbandryProspectValue(Region region, RoomBlueprintImp blue) {
        ROOM_PASTURE pasture = (ROOM_PASTURE) blue;

        if (pasture.constructor().mustBeIndoors()) {
            double result = region.info.terrain(TERRAINS.MOUNTAIN()) * region.info.area();

            return (int) Math.round(result / 5);
        }

        double moistureFactor = Math.min(region.info.moisture(), 0.25) + 0.75;

        double forestedArea = region.info.area() * region.info.terrain(TERRAINS.FOREST());
        double clearArea = region.info.area() * region.info.terrain(TERRAINS.NONE());
        double countedArea = Math.min(160, (forestedArea / 2) + clearArea);

        double result = countedArea * moistureFactor;

        return (int) Math.round(result / 16);
    }

    private int calculateWoodcutterProspectValue(Region region) {
        double result = region.info.terrain(TERRAINS.FOREST()) * region.info.area();

        return (int) Math.round(result / 8);
    }
}
