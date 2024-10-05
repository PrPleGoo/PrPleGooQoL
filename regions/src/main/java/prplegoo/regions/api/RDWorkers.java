package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.AbstractModSdkScript;
import com.github.argon.sos.mod.sdk.ModSdkModule;
import com.github.argon.sos.mod.sdk.config.json.JsonConfigStore;
import com.github.argon.sos.mod.sdk.phase.Phase;
import com.github.argon.sos.mod.sdk.phase.PhaseManager;
import com.github.argon.sos.mod.sdk.phase.Phases;
import init.paths.PATHS;
import lombok.Data;
import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.nio.file.Path;

public class RDWorkers extends AbstractModSdkScript {
    private final JsonConfigStore jsonConfigStore = ModSdkModule.jsonConfigStore();

    public static final int MIN_WORKERS = 1;
    public static final int MAX_WORKERS = 100;

    private int[][] allocatedWorkers;

    @Data
    private class JsonStore {
        private final int[][] data;

        public JsonStore(int[][] data) {
            this.data = data;
        }
    }

    public CharSequence name() {
        return "RDWorkers.Saver";
    }

    @Override
    public CharSequence desc() {
        return "Saves RDWorkers.";
    }

    @Override
    protected void registerPhases(PhaseManager phaseManager) {
        phaseManager.register(Phase.INIT_SETTLEMENT_UI_PRESENT, new Phases() {
            @Override
            public void initSettlementUiPresent() {
                jsonConfigStore.bindToSave(JsonStore.class, "RDWorkers", PATHS.local().SAVE.get().resolve("PrPleGoo"), true);
            }
        });
        phaseManager.register(Phase.ON_GAME_SAVED, new Phases() {
            @Override
            public void onGameSaved(Path saveFilePath) {
                jsonConfigStore.save(new JsonStore(allocatedWorkers));
            }
        });
        phaseManager.register(Phase.ON_GAME_SAVE_LOADED, new Phases() {
            @Override
            public void onGameSaveLoaded(Path saveFilePath) {
                JsonStore data = jsonConfigStore.get(JsonStore.class).orElse(null);
                if (data == null) {
                    initialize();
                    return;
                }

                allocatedWorkers = data.data;
            }
        });
    }

    private void initialize() {
        allocatedWorkers = new int[WORLD.REGIONS().all().size()][RD.BUILDINGS().all.size()];

        for (Region region : WORLD.REGIONS().all()) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                set(region, building, 50);
            }
        }
    }

    public int getTotal(Region r) {
        int result = 0;
        for (RDBuilding building : RD.BUILDINGS().all) {
            result += get(r, building);
        }

        return result;
    }

    public void set(Region region, RDBuilding building, int t) {
        allocatedWorkers[region.index()][building.index()] = CLAMP.i(t, MIN_WORKERS, MAX_WORKERS);
    }

    public int get(Region region, RDBuilding building) {
        if (building.isPopScaler && RD.BUILDINGS().tmp().level(building, region) != 0) {
            return allocatedWorkers[region.index()][building.index()];
        }

        return 0;
    }
}

