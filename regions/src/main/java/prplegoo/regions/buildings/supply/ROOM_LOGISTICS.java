package prplegoo.regions.buildings.supply;

import game.GAME;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.ROOM_RADIUS;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

import java.io.IOException;

public final class ROOM_LOGISTICS extends RoomBlueprintIns<SupplyInstance> implements ROOM_RADIUS, ROOM_EMPLOY_AUTO{

	public static final int STORAGE = 160;
	final Constructor constructor;
	final Crate crate = new Crate(this);
	final Cache cache = new Cache(this);
	public final SupplyTally tally = new SupplyTally(this);
	final RESOURCE liveStock;
	
	public ROOM_LOGISTICS(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_LOGISTICS", cat);
		constructor = new Constructor(this, init);
		liveStock = RESOURCES.map().read("LIVESTOCK", init.data());
	}

	@Override
	protected void update(float ds) {
		
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		cache.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		cache.clear();
		tally.clear();
		for (SupplyInstance ins : all()) {
			tally.init(ins);
		}
		cache.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		cache.clear();
		tally.clear();
		cache.clear();
	}
	
	@Override
	public boolean degrades() {
		return false;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((SupplyInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((SupplyInstance)r).auto = b;
	}

	
	
	public int goReserve(int mx, int my, ShortCoo coo) {
		SupplyInstance ins = get(mx, my);
		if (ins == null)
			return 0;
		if (ins.goCount >= ins.jobs.size())
			ins.goCount = 0;
		COORDINATE c = ins.jobs.get(ins.goCount);
		coo.set(c);
		ins.goCount++;
		Crate cr = crate.get(c.x(), c.y());
		int am = cr.reserveGo();
		return am;
	}
	
	public RESOURCE goCrate(COORDINATE c, int am){
		
		Crate cr = crate.get(c.x(), c.y());
		if (cr == null)
			return null;
		
		return cr.go(am);
	}
	
	public void goCancel(COORDINATE c, int am) {
		Crate cr = crate.get(c.x(), c.y());
		if (cr == null)
			return;
		cr.goCancel(am);
	}
	
	private int upI = -1;
	private RBITImp hh = new RBITImp();
	
	public boolean has(RESOURCE res) {
		if (upI == GAME.updateI()) {
			return hh.has(res);
		}
		upI = GAME.updateI();
		for (int i = 0; i < instancesSize(); i++) {
			if (getInstance(i).allowed().has(res))
				return true;
		}
		return hh.has(res);
	}
	

}
