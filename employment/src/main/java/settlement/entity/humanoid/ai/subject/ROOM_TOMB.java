package settlement.room.spirit.grave;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_TOMB extends RoomBlueprintIns<GraveInstance> implements GraveData.GRAVE_DATA_HOLDER{

    private final GraveData data;
    private final CTomb constructor;
    private final SFinderRoomService finder;

    public ROOM_TOMB(int typeIndex, String key, RoomInitData init, RoomCategorySub block, SFinderRoomService finder) throws IOException {
        super(typeIndex, init, key, block);
        data = new GraveData(this, init, 40) {

            @Override
            public double respect(GraveInstance grave) {
                return constructor.respekk.get(grave);
            }

        };

        constructor = new CTomb(this, init);

        this.finder = finder;
    }

    @Override
    protected void update(double ds) {
        data.update(ds);
    }

    @Override
    public Furnisher constructor() {
        return constructor;
    }

    @Override
    public SFinderRoomService service(int tx, int ty) {
        return finder;
    }

    @Override
    protected void saveP(FilePutter file){
        data.save(file);
    }

    @Override
    protected void loadP(FileGetter file) throws IOException{
        data.load(file);
    }

    @Override
    protected void clearP() {
        data.clear();
    }

    @Override
    public void appendView(LISTE<UIRoomModule> mm) {
        //mm.add(new Gui(this).make());
    }

    @Override
    public GraveData graveData() {
        return data;
    }


    public boolean isGraveHead(int tx, int ty) {
        return is(tx, ty) && SETT.ROOMS().fData.tileData.get(tx, ty) == Grave.DIG_MARK;
    }

}
