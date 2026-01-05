package prplegoo.regions;

import prplegoo.regions.api.npc.KingLevels;
import script.SCRIPT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

import java.io.IOException;

public class KingLevelsDeactivator implements SCRIPT, SCRIPT.SCRIPT_INSTANCE {
    @Override
    public CharSequence name() {
        return "Vanilla AI";
    }

    @Override
    public CharSequence desc() {
        return "AI empires will use default logic for trading, economy and army.";
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new KingLevelsDeactivator();
    }

    @Override
    public void initBeforeGameCreated() {
        KingLevels.setActive(false);
    }

    @Override
    public void update(double ds) {
        // NOP
    }

    @Override
    public void save(FilePutter file) {
        // NOP
    }

    @Override
    public void load(FileGetter file) throws IOException {
        // NOP
    }
}