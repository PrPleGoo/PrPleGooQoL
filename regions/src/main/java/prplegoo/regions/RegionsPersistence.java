package prplegoo.regions;

import prplegoo.regions.api.RDFoodConsumptionData;
import prplegoo.regions.persistence.FileGetterApi;
import prplegoo.regions.persistence.FilePutterApi;
import script.SCRIPT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.region.RD;

import java.io.IOException;

public class RegionsPersistence implements SCRIPT, SCRIPT.SCRIPT_INSTANCE {
    @Override
    public void update(double ds) {
        // nop
    }

    @Override
    public void save(FilePutter file) {
        FilePutterApi putter = new FilePutterApi();

        putter.put(RD.FOOD_CONSUMPTION().getKey(), RD.FOOD_CONSUMPTION().getData());
        putter.put(RD.SLAVERY().getKey(), RD.SLAVERY().getData());
        putter.put(RD.RECIPES().getKey(), RD.RECIPES().getData());

        putter.onGameSaved(file);
    }

    @Override
    public void load(FileGetter file) throws IOException {
        FileGetterApi getter = new FileGetterApi();
        getter.onGameLoaded(file);

        RDFoodConsumptionData gotten = getter.get(RD.FOOD_CONSUMPTION());
        RD.FOOD_CONSUMPTION().putData(gotten);
        RD.SLAVERY().putData(getter.get(RD.SLAVERY()));
        RD.RECIPES().putData(getter.get(RD.RECIPES()));
    }

    @Override
    public CharSequence name() {
        return "Region overhaul saving";
    }

    @Override
    public CharSequence desc() {
        return "No matter what you do, this will be enabled.";
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new RegionsPersistence();
    }

    @Override
    public boolean forceInit() {
        return true;
    }
}
