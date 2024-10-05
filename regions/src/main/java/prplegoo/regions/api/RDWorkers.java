package prplegoo.regions.api;

import com.github.argon.sos.mod.sdk.AbstractModSdkScript;
import com.github.argon.sos.mod.sdk.ModSdkModule;
import com.github.argon.sos.mod.sdk.config.json.JsonConfigStore;
import init.paths.PATHS;
import lombok.Data;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.io.IOException;
import java.nio.file.FileSystem;

public class RDWorkers {
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

    public RDWorkers(RD.RDInit init) {
        allocatedWorkers = new int[WORLD.REGIONS().all().size()][RD.BUILDINGS().all.size()];
        jsonConfigStore.bindToSave(JsonStore.class, "RDWorkers", PATHS.local().SAVE.get().resolve("PrPleGoo"), true);

        init.savable.add(new SAVABLE() {
            @Override
            public void save(FilePutter file) {
                jsonConfigStore.save(new JsonStore(allocatedWorkers));
            }

            @Override
            public void load(FileGetter file) throws IOException {
                JsonStore data = jsonConfigStore.get(JsonStore.class).orElse(null);
                if (data == null) {
                    clear();
                    return;
                }

                allocatedWorkers = data.data;
//                clear();
//                int initialPosition = file.getPosition();
//                LOG.ln("file position guard, " + initialPosition);
//
//                file.is(allocatedWorkers);
//                if (dataInvalid())
//                {
//                    LOG.ln("data invalid, fallback executed");
//                    clear();
//                    file.setPosition(initialPosition);
//                    LOG.ln("file position set, " + file.getPosition());
//                }
            }

            @Override
            public void clear() {
                initialize();
            }
        });
    }

    private boolean dataInvalid() {
        for (int[] allocatedWorker : allocatedWorkers) {
            for (int i : allocatedWorker) {
                if (i < MIN_WORKERS || i > MAX_WORKERS) {
                    LOG.ln("data invalid");
                    return false;
                }
            }
        }

        return true;
    }

    private void initialize() {
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

