package world.region.pop;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import lombok.Getter;
import prplegoo.regions.api.resettlement.RDRaceEdictDiscourage;
import prplegoo.regions.api.resettlement.RDRaceEdictEncourage;
import prplegoo.regions.api.resettlement.RDRaceEdictMassacre;
import settlement.stats.STATS;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.INT_O;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;

public class RDEdicts {
	@Getter
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

		sanction = new RDRaceEdictEncourage("SANCTION", init, new INFO("Encourage growth", "Encouraging growth of a species will cause members from your empire to move here."), UI.icons().m.descrimination, races);
		exile = new RDRaceEdictDiscourage("EXILE", init, new INFO("Discourage growth", "Discouraging growth of a species will cause members from your empire to move away from here. If there's no place to go they will feel exiled."), UI.icons().m.exit, races);
		massacre = new RDRaceEdictMassacre("MASSACRE", init, new INFO(¤¤Massacre, ¤¤MassacreD), UI.icons().m.skull, races, 1.0, 1.0);
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
				int am = 0;
				for (int ri = 0; ri < reg.faction().realm().regions(); ri++) {
					Region r = reg.faction().realm().region(ri);
					am+=massacre.toggled(race).get(r);
				}

				if (am > 0) {
					massacre.realm(race).incFraction(reg.faction(), am*0.5*ds*TIME.secondsPerDayI()*massacre.realm(race).max(null));
				}else {
					massacre.realm(race).incFraction(reg.faction(), -ds*dtime*massacre.realm(race).max(null));
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

	public static abstract class RDRaceEdict {

		protected final LIST<INT_O.INT_OE<Region>> toggled;
		protected final LIST<INT_O.INT_OE<Faction>> realm;
		public final INFO info;
		public final SPRITE icon;
		public final BoostSpecs boosts;

		public RDRaceEdict(String key, RD.RDInit init, INFO info, SPRITE icon, LIST<RDRace> races) {
			this.info = info;

			ArrayList<INT_O.INT_OE<Region>> toggleds = new ArrayList<INT_O.INT_OE<Region>>(races.size());
			ArrayList<INT_O.INT_OE<Faction>> realms = new ArrayList<INT_O.INT_OE<Faction>>(races.size());


			boosts = new BoostSpecs(info.name, icon, true);

			for (RDRace r : races) {

				INT_O.INT_OE<Region> toggled = init.count.new DataBit(key + "_RACE_TOGGLED" + r.race.key);
				INT_O.INT_OE<Faction> realm = init.rCount.new DataByte(key + "_RACE_REALM" + r.race.key);

				toggleds.add(toggled);
				realms.add(realm);
			}

			this.toggled = toggleds;
			this.realm = realms;

			this.icon = icon;
		}

		public INT_O.INT_OE<Region> toggled(RDRace r){
			return toggled.get(r.index());
		}

		public INT_O.INT_OE<Faction> realm(RDRace r){
			return realm.get(r.index());
		}
	}
}
