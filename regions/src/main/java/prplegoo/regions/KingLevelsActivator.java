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
        return "Activates a set of rules for the AI empires. They will use regions like the player does and trade only with actual goods and money." +
                "\nIt allows the player to trade for profit. The AI can adapt to changing markets and will dynamically maximize profit, to a degree." +
                "\nThere will be no such thing as a stable price and the AI will have to develop their economies at the start of the game." +
                "\n" +
                "\nThis is an experimental feature and can not be turned off once you start a game. You can expect weird behavior from the AI and trade will be chaotic." +
                "\nPlease use this only if you are sure you want to risk wacky bugs although I am confident in the system's ability to provide content." +
                "\nAlso comes at a significant cost to performance.";
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
}
