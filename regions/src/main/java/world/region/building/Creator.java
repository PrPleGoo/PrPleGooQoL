package world.region.building;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.player.Player;
import game.values.GVALUES;
import game.values.Lockable;
import game.values.Value;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import init.type.CLIMATE;
import init.type.CLIMATES;
import prplegoo.regions.api.PrPleGooEfficiencies;
import settlement.army.div.Div;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.Induvidual;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.info.GFORMAT;
import util.info.INFO;
import world.map.regions.Region;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RDOutputs.RDOutput;
import world.region.RDReligions.RDReligion;
import world.region.pop.RDRace;

import java.io.IOException;

final class Creator {

	private static CharSequence ¤¤prospect = "Prospect";
	private static CharSequence ¤¤desc = "Produces";
	private static CharSequence ¤¤small = "(Small)";
	private static CharSequence ¤¤large = "(Large)";
	private static CharSequence ¤¤awesome = "(Awesome)";
	
	static {
		D.ts(Creator.class);
	}
	

	Creator(RDBuildings buils) {
		
	}
	
	public RDBuilding read(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, String key, ResFolder f) throws IOException {
		
		Json json = new Json(f.init.get(key));
		Json text = new Json(f.text.get(key));
		INFO info = new INFO(text);
		
		ArrayListGrower<RDBuildingLevel> levels = new ArrayListGrower<>();
		
		Json[] jsons = json.jsons("LEVELS", 1, 10);
		
		int li = 0;
		String[] names = new String[0];
		if (text != null && text.has("LEVELS")) 
			names = text.texts("LEVELS");
		boolean aibuild = json.bool("AI_BUILDS", true);
		boolean noti = json.bool("NOTIFY_WHEN_UPGRADABLE", false);
		String order = "";
		if (json.has("ORDER"))
			order = json.value("ORDER");
		for (Json j : jsons) {
			CharSequence name = li < names.length ? names[li] : info.name + ": " + GFORMAT.toNumeral(new Str(4), li+1);
			Icon icon = SPRITES.icons().get(j);

			Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + cat.key + "_" + key + "_"+(li+1), name, info.desc, icon);
			needs.push(j);
			RDBuildingLevel l = new RDBuildingLevel(name, icon, needs);
			levels.add(l);
			li++;
		}
		
		RDBuilding b = new RDBuilding(all, init, cat, key, info, levels, aibuild, noti, order);
		
		
		for (int i = 0; i < jsons.length; i++) {
			RDBuildingLevel l = b.levels.get(i+1);
			Json j = jsons[i];
			l.local.read("BOOST", j, BValue.VALUE1);
			l.global.read("BOOST_GLOBAL", j, BValue.VALUE1, Dic.¤¤global, true, "ADMIN");
			l.cost = j.i("CREDITS", 0, 1000000, 0);
		}

		if (json.bool("FOOD_CONSUMER", false)) {
			BOOSTING.connecter(() -> PrPleGooEfficiencies.FOOD_CONSUMER(b));
		}

		pushEfficiency(b, json);
		
		return b;
		
	}
	
	public LIST<RDBuilding> generate(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, ResFolder f){
		
		ArrayListGrower<RDBuilding> res = new ArrayListGrower<>();
		if (!f.init.exists("_GEN"))
			return res;
		
		Json[] data = new Json(f.init.get("_GEN")).jsons("GENS");
		for (Json j : data)
			res.add(generate(all, init, cat, j));
		
		return res;
		
	}
	
	private LIST<RDBuilding> generate(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, Json data){
		
		ArrayListGrower<RDBuilding> res = new ArrayListGrower<>();

		LIST<RoomBlueprint> rooms = SETT.ROOMS().collection.readMany("INDUSTRIES", data);
		
		for (RoomBlueprint b : rooms) {
			if (b instanceof ROOM_TEMPLE) {
				ROOM_TEMPLE t = (ROOM_TEMPLE) b;
				RDBuilding bu = generate(all, init, cat, t, data);
				res.add(bu);
			}
			
			
			if (b instanceof RoomBlueprintImp && b  instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) b;
				RoomBlueprintImp blue = (RoomBlueprintImp) b;
				
				if (h.industries().size() == 0 || h.industries().get(0).outs().size() == 0) {
					LOG.err(data.errorGet(b.key + "Is not a valid room to generate", "INDUSTRIES"));
				}
				
				RDBuilding bu = generate(all, init, cat, h.industries().get(0).outs(), blue, data);
				res.add(bu);
			}
			
		}
		
		
		return res;
		
	}
	

	
	private RDBuilding generate(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, LIST<IndustryResource> is, RoomBlueprintImp blue, Json data){
		
		ArrayList<RDBuildingLevel> levels = new ArrayList<>(data.i("LEVELS", 1, 10));
		
		double output = data.d("OUTPUT");
		double credits = data.i("CREDITS", 0, Integer.MAX_VALUE);
		
		
		String kkk = blue.key.startsWith("_") ? blue.key.substring(1) : blue.key;

		String desc = ¤¤desc + ": ";
		
		for(int ri = 0; ri  < is.size(); ri++) {
			desc += is.get(0).resource.name;
			if (ri < is.size()-1)
				desc += ", ";
		}
		
		for (int li = 0; li < levels.max(); li++) {
			CharSequence name = blue.info.name + ": " + GFORMAT.toNumeral(new Str(4), li+1);
			Icon icon = blue.iconBig();

			double d = (double)(li+1) / (levels.max());
			
			Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + cat.key + "_" + kkk + "_"+(li+1), name, desc, icon);
			RDBuildingLevel l = new RDBuildingLevel(name, icon, needs);
			l.cost = (int) (NPCStockpile.AVERAGE_PRICE*credits*d);
			
			BSourceInfo info = new BSourceInfo(blue.info.name, blue.icon);
			for(int ri = 0; ri  < is.size(); ri++){
				IndustryResource i = is.get(ri);
				BoosterValue bo = new BoosterValue(BValue.VALUE1, info, output*d*i.rate, false);
				RDOutput out = RD.OUTPUT().get(i.resource);
				l.local.push(bo, out.boost);
			}
			
			levels.add(l);
		}
		
		INFO info = new INFO(blue.info.name, desc);
		
		RDBuilding b = new RDBuilding(all, init, cat, kkk, info, levels, true, false, kkk);
		

		
		pushEfficiency(b, data);
		
		BoostSpecs sp = new BoostSpecs(blue.info.name, blue.icon, false);
		sp.read(data, BValue.VALUE1);
		ACTION a = new ACTION() {
			BSourceInfo info = new BSourceInfo(blue.info.name, blue.icon);
			@Override
			public void exe() {
				for (int i = 1; i < b.levels.size(); i++) {
					for (BoostSpec s : sp.all()) {
						int am = (int) (s.booster.to()*i/(b.levels.size()-1));
						b.levels.get(i).local.push(new BoosterValue(BValue.VALUE1, info, am, s.booster.isMul), s.boostable);
					}
				}
				for (CLIMATE c : CLIMATES.ALL()) {
					for (int si = 0; si < c.boosters.all().size(); si++) {
						BoostSpec s = c.boosters.all().get(si);
						if (s.boostable == blue.bonus()) {
							BoostSpec ss = CLIMATES.pushIfDoesntExist(c, s.booster.to(), b.efficiency, s.booster.isMul);
							if (ss != null) {
								b.baseFactors.add(ss.booster);
							}
						}
					}
				}
				
				for (RDRace c : RD.RACES().all) {
					for (int si = 0; si < c.race.boosts.all().size(); si++) {
						BoostSpec s = c.race.boosts.all().get(si);
						if (s.boostable == blue.bonus()) {
							
							BoostSpec sp = RACES.boosts().pushIfDoesntExist(c.race, s.booster.to(), b.efficiency, s.booster.isMul);
							if (sp != null && !sp.boostable.contains(sp.booster))
								sp.booster.add(sp.boostable);
						}
					}
				}
				
			}
		};
		BOOSTING.connecter(a);
		
		
		return b;
		
	}
	
	private RDBuilding generate(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, ROOM_TEMPLE temple, Json data){
		
		ArrayList<RDBuildingLevel> levels = new ArrayList<>(5);
		double credits = data.i("CREDITS", 0, Integer.MAX_VALUE);
		
		ROOM_SHRINE shrine = SETT.ROOMS().TEMPLES.SHRINES.get(temple.religion.index());
		RDReligion reg = RD.RELIGION().get(temple.religion);
		double[] local = new double[] {
			1,
			2,
			3,
			4,
			5
		};
		double[] global = new double[] {
			0,
			0,
			0.1,
			0.25,
			0.5,
		};
		CharSequence[] name = new CharSequence[] {
			shrine.info.name + " " + ¤¤small,
			shrine.info.name + " " + ¤¤large,
			temple.info.name + " " + ¤¤small,
			temple.info.name + " " + ¤¤large,
			temple.info.name + " " + ¤¤awesome,
		};
		
		Icon[] icons = new Icon[] {
			shrine.icon,
			shrine.icon,
			temple.icon,
			temple.icon,
			temple.icon,
		};
		
		for (int i = 0; i < local.length; i++) {
			
			double d = (double)(i+1) / (levels.max());
			
			Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + cat.key + "_" + temple.religion.key + "_"+(i+1), name[i], temple.religion.info.desc, icons[i]);
			RDBuildingLevel l = new RDBuildingLevel(name[i], icons[i], needs);
			l.cost = (int) (NPCStockpile.AVERAGE_PRICE*credits*Math.pow(d, 2));
			
			BSourceInfo info = new BSourceInfo(name[i], icons[i]);
			{
				BoosterValue bo = new BoosterValue(BValue.VALUE1, info, local[i], false);
				l.local.push(bo, reg.boost);
				if (global[i] > 0) {
					bo = new BoosterValue(BValue.VALUE1, info, global[i], false);
					l.global.push(bo, reg.boost);
				}
				
			}
			
			levels.add(l);
			
		}
		

		
		INFO info = new INFO(shrine.info.name, temple.religion.info.desc);
		
		RDBuilding b = new RDBuilding(all, init, cat, temple.key, info, levels, true, false, temple.key);
		
		BoostSpecs sp = new BoostSpecs(shrine.info.name, shrine.icon, false);
		sp.read(data, BValue.VALUE1);
		ACTION a = new ACTION() {
			BSourceInfo info = new BSourceInfo(shrine.info.name, shrine.icon);
			@Override
			public void exe() {
				for (int i = 1; i < b.levels.size(); i++) {
					for (BoostSpec s : sp.all()) {
						int am = (int) (s.booster.to()*i/(b.levels.size()-1));
						b.levels.get(i).local.push(new BoosterValue(BValue.VALUE1, info, am, s.booster.isMul), s.boostable);
					}
				}
				
			}
		};
		BOOSTING.connecter(a);

		pushEfficiency(b, data);

		return b;
		
	}
	
	private void pushEfficiency(RDBuilding bu, Json da) {
		
		if (!da.has("EFFICIENCY"))
			return;
		
		Json data = da.json("EFFICIENCY");
		
		
		if (data.has("PROSPECT")) {
			double[] p = data.ds("PROSPECT", 2);
			double from = p[0];
			double mul = p[1];
			Bo bo = new Bo(new BSourceInfo(¤¤prospect, UI.icons().s.gift), from, from+mul, true) {

				@Override
				double get(Region reg) {
					return from +  mul*RD.RAN().get(reg, bu.index()*4, 4)/15.0;
				};
				
				
			};
			bo.add(bu.efficiency);
			bu.baseFactors.add(bo);
		}

		ACTION ca = new ACTION() {
			
			@Override
			public void exe() {
				if (data.has("BOOST")) {
					Json e = data.json("BOOST");
					for (String k : e.keys()) {
						double[] p = e.ds(k, 2);
						double from = p[0];
						double mul = p[1];
						for (Value<Region> v : GVALUES.REGION.get(k, e)) {
							Bo bo = new Bo(new BSourceInfo(v.name, v.icon), from, from+mul, true) {

								@Override
								double get(Region reg) {
									return from +  mul*v.d.getD(reg);
								};	
							};
							bo.add(bu.efficiency);
							bu.baseFactors.add(bo);
						}
					}
				}
				
			}
		};

		BOOSTING.connecter(ca);
	}
	
	static abstract class Bo extends BoosterImp {

		public Bo(BSourceInfo info, double from, double to, boolean isMul) {
			super(info, from, to, isMul);
		}
		
		abstract double get(Region reg);
		
		@Override
		public double vGet(FactionNPC f) {
			return vGet((Faction)f);
		}
		
		@Override
		public double vGet(Player f) {
			return vGet((Faction)f);
		}
		
		@Override
		public double vGet(Faction f) {
			double d = 0;
			
			for (int ri = 0; ri < f.realm().regions(); ri++) {
				d += vGet(f.realm().region(ri));
			}
			return d;
			
		};
		
		@Override
		public double vGet(PopTime t) {
			return vGet((Faction)FACTIONS.player());
		}
		
		@Override
		public double vGet(Div div) {
			return vGet((Faction)FACTIONS.player());
		}
		
		@Override
		public double vGet(Induvidual indu) {
			return  vGet(indu.faction());
		}
		
		@Override
		public double vGet(Region reg) {
			return get(reg);
		}
		
		@Override
		public double getValue(double input) {
			return input;
		}
	}
	
}
