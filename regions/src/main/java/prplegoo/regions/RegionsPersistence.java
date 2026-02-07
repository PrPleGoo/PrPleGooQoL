package prplegoo.regions;

import prplegoo.regions.api.gen.ProspectCache;
import prplegoo.regions.api.gen.RacePreferenceCache;
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

        putter.put(RD.SLAVERY().getKey(), RD.SLAVERY().getData());
        putter.put(RD.RECIPES().getKey(), RD.RECIPES().getData());
        putter.put(RD.DEFICITS().getKey(), RD.DEFICITS().getData());
        putter.put(RD.UPDATER().getShipper().getKey(), RD.UPDATER().getShipper().getData());

        putter.onGameSaved(file);
    }

    @Override
    public void load(FileGetter file) throws IOException {
        FileGetterApi getter = new FileGetterApi();
        getter.onGameLoaded(file);

        RD.SLAVERY().putData(getter.get(RD.SLAVERY()));
        RD.RECIPES().putData(getter.get(RD.RECIPES()));
        RD.DEFICITS().putData(getter.get(RD.DEFICITS()));
        RD.UPDATER().getShipper().putData(getter.get(RD.UPDATER().getShipper()));

        RacePreferenceCache.Reset();
        ProspectCache.Reset();
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

