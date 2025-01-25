package world.region.building;

import game.boosting.BValue;
import game.faction.Faction;
import init.paths.PATHS.ResFolder;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import world.map.regions.Region;
import world.region.RD.RDInit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class RDBuildingCat {
	
	final ArrayListGrower<RDBuilding> all = new ArrayListGrower<>();
	public final COLOR color;
	public final String key;
	public final int order;
	
	
	RDBuildingCat(Creator creator, LISTE<RDBuilding> all, RDInit init, String folder, ResFolder p) throws IOException{
		this.key = folder.toUpperCase(Locale.ENGLISH);
		Json json = new Json(p.init.get("_CAT"));
		this.color = new ColorImp(json);
		order = json.i("ORDER", 0, 10000000, 0);
		addJsons(creator, all, init, p);
		creator.generate(all, init, this, p);
		
		RDBuilding[] bus = new RDBuilding[this.all.size()];
		for (int i = 0; i <  this.all.size(); i++)
			bus[i] = this.all.get(i);
		this.all.clear();
		Arrays.sort(bus, new Comparator<RDBuilding>() {

			@Override
			public int compare(RDBuilding o1, RDBuilding o2) {
				return o1.order.compareTo(o2.order);
			}
		
		});
		this.all.add(bus);
		
	}
	
	public LIST<RDBuilding> all(){
		return all;
	}
	
	private void addJsons(Creator creator, LISTE<RDBuilding> all, RDInit init, ResFolder p) throws IOException{
		
		for (String f : p.init.getFiles()) {
			creator.read(all, init, this, f, p);
		}
		
	}
	
	static final BValue lValue = new BValue.BValueNone() {
		@Override
		public double vGet(Region reg) {
			return 1.0;
		}

		@Override
		public double vGet(Faction f) {
			return 0;
		}
		

	};
	
	static final BValue lGlobal = new BValue.BValueNone() {
		@Override
		public double vGet(Region reg) {
			return 1.0;
		}

		@Override
		public double vGet(Faction f) {
			return 1.0;
		}

		
	};
	
	
	
}