package world.region.pop;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpecs;
import game.boosting.BoosterImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import prplegoo.regions.api.npc.KingLevels;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;

public class RDEdicts {

	private static CharSequence ¤¤Distant = "¤Distant";
	
	private static CharSequence ¤¤Prosecute = "¤Persecution";
	private static CharSequence ¤¤ProsecuteD = "¤Persecuting a species severely diminishes growth and decreases happiness.";
	
	private static CharSequence ¤¤Exile = "¤Exile";
	private static CharSequence ¤¤ExileD = "¤Forbid this species from immigrating and sends off any citizens to neighbouring regions where they are still welcome.";
	
	
	private static CharSequence ¤¤Massacre = "¤Massacre";
	private static CharSequence ¤¤MassacreD = "¤Commit genocide and instantly rid yourself of this species. Will cause an outrage of course, make sure you have enough military presence to handle an eventual uprising.";
	
	private static double dtime;
	
	static {
		D.ts(RDEdicts.class);
	}
	
	public final LIST<RDRaceEdict> all;
	public final RDRaceEdict sanction;
	public final RDRaceEdict exile;
	public final RDRaceEdict massacre;
	
	RDEdicts(LIST<RDRace> races, RDInit init) {
		dtime = 1.0/(TIME.secondsPerDay()*2*16);

		sanction = new RDRaceEdict("SANCTION", init, new INFO(¤¤Prosecute, ¤¤ProsecuteD), UI.icons().m.descrimination, races, 0.25, 0.5);
		exile = new RDRaceEdict("EXILE", init, new INFO(¤¤Exile, ¤¤ExileD), UI.icons().m.exit, races, 1.0, 0.6);
		massacre = new RDRaceEdict("MASSACRE", init, new INFO(¤¤Massacre, ¤¤MassacreD), UI.icons().m.skull, races, 1.0, 1.0);
		this.all = new ArrayList<RDRaceEdict>(sanction,exile,massacre);
		for (RDRace r : races)
			init.upers.add(new Up(r));
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player()) {
					for (RDRace r : RD.RACES().all) {
						for (RDRaceEdict e : all)
							e.toggled(r).set(reg, 0);
					}
				}
			}
		};
		
		for (RDRace r : races) {
			BSourceInfo ss = new BSourceInfo(STATS.MULTIPLIERS().PROSECUTION.name + " (" + Dic.¤¤Capitol + ")", UI.icons().m.descrimination);
			new RBooster(ss, 1, 0.25, true) {

				@Override
				public double get(Region t) {
					if (KingLevels.isActive() && FACTIONS.player() != t.faction()) {
						return 0;
					}

					return STATS.MULTIPLIERS().PROSECUTION.value(HCLASSES.CITIZEN(), r.race, 0);
				}
			
			}.add(r.loyalty.target);
		}
	}
	
	private class Up implements RDUpdatable {
		
		private final RDRace race;
		
		Up(RDRace r){
			this.race = r;
		}
		
		@Override
		public void update(Region reg, double ds) {
			if (reg.faction() != null && reg.capitol()) {
				for (RDRaceEdict e : all) {
					int am = 0;
					for (int ri = 0; ri < reg.faction().realm().regions(); ri++) {
						Region r = reg.faction().realm().region(ri);
						am+=e.toggled(race).get(r);
					}

					if (am > 0) {
						e.realm(race).incFraction(reg.faction(), am*0.5*ds*TIME.secondsPerDayI()*e.realm(race).max(null));
					}else {
						e.realm(race).incFraction(reg.faction(), -ds*dtime*e.realm(race).max(null));
					}
					
				}
			}
		}
		
		@Override
		public void init(Region reg) {
			
		
			
			if (reg.faction() == FACTIONS.player()) {
				
				for (RDRaceEdict e : all) {
					e.toggled(race).set(reg, 0);
					e.realm(race).setD(reg.faction(), 0);
				}
			}else if (reg.faction() != null && reg.capitol()) {
				for (RDRaceEdict e : all) {
					e.realm(race).setD(reg.faction(), 0);
					for (int ri = 0; ri < reg.faction().realm().regions(); ri++) {
						Region r = reg.faction().realm().region(ri);
						if (e.toggled(race).get(r) == 1) {
							e.realm(race).setD(reg.faction(), 1.0);
						}
					}
				}
			}
		}
	}
	
	public static final class RDRaceEdict {
		
		public final LIST<INT_OE<Region>> toggled;
		public final LIST<INT_OE<Faction>> realm;
		public final INFO info;
		public final SPRITE icon;
		public final BoostSpecs boosts;
		
		private RDRaceEdict(String key, RDInit init, INFO info, SPRITE icon, LIST<RDRace> races, double loyalty, double growth) {
			this.info = info;
			
			ArrayList<INT_OE<Region>> toggleds = new ArrayList<INT_OE<Region>>(races.size());
			ArrayList<INT_OE<Faction>> realms = new ArrayList<INT_OE<Faction>>(races.size());
			
			
			boosts = new BoostSpecs(info.name, icon, true);
			
			for (RDRace r : races) {
				
				INT_OE<Region> toggled = init.count.new DataBit(key + "_RACE_TOGGLED" + r.race.key);
				INT_OE<Faction> realm = init.rCount.new DataByte(key + "_RACE_REALM" + r.race.key);
				
				boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-loyalty, true) {

					@Override
					public double get(Region t) {
						return toggled.get(t);
					}
				
				}, r.loyalty.target);
				
				boosts.push(new BoosterImp(new BSourceInfo(¤¤Distant + ": " + info.name, icon), 1, 1.0-loyalty, true) {

					@Override
					public double vGet(Region t) {
						if (KingLevels.isActive() && FACTIONS.player() != t.faction()) {
							return 0;
						}

						if (t.faction() != null && realm.get(t.faction()) > 0)
							return CLAMP.d(realm.getD(t.faction()), 0, 1);
						return 0;
					}

					@Override
					public double vGet(Faction f) {
						return CLAMP.d(realm.getD(f), 0, 1);
					}
					

				}, r.loyalty.target);
				
				boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-growth, true) {

					@Override
					public double get(Region t) {
						return toggled.get(t);
					}
				
				}, r.pop.dtarget);	
				
				toggleds.add(toggled);
				realms.add(realm);
			}
			
			this.toggled = toggleds;
			this.realm = realms;
			
			this.icon = icon;
		}
		
		public INT_OE<Region> toggled(RDRace r){
			return toggled.get(r.index());
		}
		
		public INT_OE<Faction> realm(RDRace r){
			return realm.get(r.index());
		}
	}
	
}
