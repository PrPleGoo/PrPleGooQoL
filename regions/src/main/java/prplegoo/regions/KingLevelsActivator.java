package prplegoo.regions;

import prplegoo.regions.api.npc.KingLevels;
import script.SCRIPT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

import java.io.IOException;

public class KingLevelsActivator implements SCRIPT, SCRIPT.SCRIPT_INSTANCE {
    @Override
    public CharSequence name() {
        return "Region rework: Faction AI";
    }

    @Override
    public CharSequence desc() {
        return "AI empires will use regions like the player does and trade only with actual goods and money." +
                "\nIt allows the player to trade for profit. The AI can adapt to changing markets and will dynamically maximize profit, to a degree." +
                "\nThere will be no such thing as a stable price and the AI will have to develop their economies at the start of the game." +
                "\n" +
                "\nNo matter what you do, this will be enabled.";
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new KingLevelsActivator();
    }

    @Override
    public void initBeforeGameCreated() {
        KingLevels.setActive(true);
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

    @Override
    public boolean forceInit() {
        return true;
    }
}
