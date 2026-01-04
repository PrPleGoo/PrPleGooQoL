package world.entity.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.constant.C;
import init.constant.Config;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.HTYPES;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import util.colors.GCOLOR;
import util.gui.misc.GText;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.WORLD;
import world.army.AD;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public abstract class WArmyState implements INDEXED{

	private static LIST<WArmyState> all = new ArrayList<>(0); 
	
	private static CharSequence ¤¤siege = "Are you sure you wish to besiege {0} and declare war on the faction of {0}?"; 
	
	static {
		D.ts(WArmyState.class);
	}

	public static LIST<WArmyState> all(){
		return all;
	}
	
	public final static WArmyState fortified = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			checkTile(a);
			return this;
		}
		
		@Override
		public GText info(WArmy a, GText box) {
			box.normalify();
			box.set(Dic.¤¤Fortified);
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Fortified;
		}
	}; 
	
	public final static WArmyState fortifying = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			checkTile(a);
			a.stateFloat += ds;
			if (a.stateFloat > TIME.secondsPerDay()/2)
				return fortified;
			return this;
				
		}
		
		@Override
		public GText info(WArmy a, GText box) {
			box.normalify();
			box.set(Dic.¤¤Fortifying);
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Fortifying;
		}
	}; 
	
	
	private static void checkTile(WArmy a) {
		if (!WORLD.PATH().map.is.is(a.ctx(), a.cty())) {
			for (int i = 0; i < GUTIL.circle().length(); i++) {
				COORDINATE c = GUTIL.circle().get(i);
				int x = a.body().cX()+c.x();
				int y = a.body().cY()+c.y();
				int tx = x >> C.T_SCROLL;
				int ty = y >> C.T_SCROLL;
				if (WORLD.PATH().map.is.is(tx, ty)) {
					a.teleport(tx, ty);
					return;
				}
			}
		}
	}
	
	public final static WArmyState raiding = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			
			Region reg = a.region();
			if (reg == null) {
				a.stateFloat = 0;
				return WArmyState.fortifying;
			}
			
			a.stateFloat += ds;
			
			if (a.stateFloat < 120)
				return this;
			
			a.stateFloat -= 120;
			
			double rd = RD.RACES().popSizeTarget(reg);
			double ad = (double)AD.men(null).get(a)/(Config.battle().MEN_PER_ARMY);
			double dd = ad/rd;
			
			double d = 120*dd/(TIME.secondsPerDay()*4);
			
			double inc = RD.DEVASTATION().current.max(reg)*d;
			int iinc = (int) inc;
			if (inc-iinc > RND.rFloat()) {
				iinc ++;
			}
			int now = RD.DEVASTATION().current.get(reg);
			if (now + iinc >= RD.DEVASTATION().current.max(reg)) {
				iinc = RD.DEVASTATION().current.max(reg)-now;
			}
			
			
			
			if (iinc > 0) {
				
				
				Shipment s = null;
				Faction to = a.faction();
				
				if (to != null) {
					for (RESOURCE res : RESOURCES.ALL()) {

						double output = RD.OUTPUT().get(res).loot(reg)*d*10.0;
						if (KingLevels.isActive() && reg.faction() instanceof FactionNPC) {
							output /= KingLevels.getInstance().getPlayerScalingMul();
						}

						int am = (int) (KingLevels.isActive()
								? Math.round(output)
								: Math.ceil(output));
						
						if (am > 0 && to != null) {
							if (s == null) {
								s = WORLD.ENTITIES().caravans.create(a.ctx(), a.cty(), to.capitolRegion(), ITYPE.spoils);
							}
							if (s != null) {
								s.loadAndReserve(res, am);
							}
							
						}
					}
					
					for (RDRace ra : RD.RACES().all) {
						int pop = (int) Math.ceil(ra.pop.get(reg)*d*0.5);
						if (pop > 0) {
							
							ra.pop.inc(reg, -pop);
							if (s == null) {
								s = WORLD.ENTITIES().caravans.create(a.ctx(), a.cty(), to.capitolRegion(), ITYPE.spoils);
							}
							if (s != null) {
								s.load(ra.race, (int) Math.ceil(pop/2.0), HTYPES.PRISONER());
							}
							
						}
					}
				}
				
				
				
				if (a.faction() == FACTIONS.player() && reg.faction() instanceof FactionNPC) {
					FactionNPC ff = (FactionNPC) reg.faction();
					ROPINIONS.STANCE().tresPass(ff, 1);
				}
				
				RD.DEVASTATION().current.inc(reg, iinc);
			}
			
			
			
			return this;
		}
		
		@Override
		public GText info(WArmy a, GText box) {
			box.warnify();
			box.set(Dic.¤¤Raiding);
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Raiding;
		}
	}; 
	
	public final static WArmyState moving = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			if (!a.path().move(a, WArmy.speed*ds)) {
				a.stateFloat = 0;
				
				return fortifying;
			}
			
			return this;
		}
		


		@Override
		public GText info(WArmy a, GText box) {
			Region reg = WORLD.REGIONS().map.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLOR.MAP().get(reg.faction()));
				text.add(Dic.¤¤MarchingTo).insert(0, reg.info.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Moving;
		}
	};
	
	public final static WArmyState intercepting = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			WArmy other = intercepting(a);
			
			
			
			if (other == null || !a.path().isValid()) {
				a.stateFloat = 0;

				
				return fortifying;
			}
			
			if (a.path().destX() == other.ctx() && a.path().destY() == other.cty()) {
				if (a.path().remaining() > 1) {
					a.path().move(a, WArmy.speed*ds);
					return this;
				}
				
				if (other.state() == fortifying || other.state() == fortified) {
					a.stateFloat = 0;
					return fortifying;
				}
				
				
				return this;
			}
			
			double dist = COORDINATE.tileDistance(a.path().destX(), a.path().destY(), other.ctx(), other.cty());
			
			
			if (dist*10 > a.path().remaining())
				if (!a.path().find(a.ctx(), a.cty(), other.ctx(), other.cty())) {
					a.stateFloat = 0;
					return fortifying;
				}
			
		
			return this;
		}
		
		private WArmy intercepting(WArmy a) {
			if (a.stateShort != -1) {
				WArmy aa = WORLD.ENTITIES().armies.get(a.stateShort);
				if (aa == null || !aa.added()) {
					a.stateShort = -1;
					return null;
				}
				return aa;
			}
			return null;
		}

		@Override
		public GText info(WArmy a, GText box) {
			WArmy aa = intercepting(a);
			if (aa == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLOR.MAP().get(aa.faction()));
				text.add(Dic.¤¤Intercepting).insert(0, aa.name);
			}
			return box;
			
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Moving;
		}
	};
	
	public final static WArmyState besieging = new WArmyState() {
		
		Region aReg;
		WArmy aa;
		private ACTION besiege = new ACTION() {
			
			@Override
			public void exe() {
				if (aReg.faction() != null && aReg.faction() instanceof FactionNPC) {
					ROPINIONS.STANCE().setNewStance((FactionNPC) aReg.faction(), DIP.WAR(), aa.faction() == FACTIONS.player());
					aa.besiege(aReg);
				}
				
			}
		};
		
		@Override
		WArmyState update(WArmy a, double ds) {
			
			Region reg = WORLD.REGIONS().getByIndex(a.stateShort);
			
		
			if (a.faction() == reg.faction() || !a.path().isValid()) {
				if (!a.besieging(reg)) {
					a.path().clear();
					a.stateFloat = 0;
					return fortifying;
				}
				return this;
			}
			
			if (a.path().move(a, WArmy.speed*ds)) {
				return this;
			}else {
				a.path().clear();
				if (a.faction() == FACTIONS.player() && reg.faction() != a.faction() && !DIP.WAR().is(reg.faction(), a.faction()) && reg.faction() != null) {
					aReg = reg;
					aa = a;
					VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤siege).insert(0, reg.info.name()).insert(0, reg.faction().name), besiege, ACTION.NOP, true);
					return this;
				}else {
					WORLD.BATTLES().besige(a, reg);
				}
			}
			
			return this;
			
		}

		@Override
		public GText info(WArmy a, GText box) {
			Region reg = WORLD.REGIONS().getByIndex(a.stateShort);
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLOR.MAP().get(reg.faction()));
				text.add(Dic.¤¤BesiegingSomething).insert(0, reg.info.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Besieging;
		}
	};
	
	public final static WArmyState movingRaid = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {

			if (!a.path().move(a, WArmy.speed*ds)) {
				
				a.stateFloat = 0;
				if (a.canRaid())
					return raiding;
				return fortifying;
			}
			
			return this;
		}
		


		@Override
		public GText info(WArmy a, GText box) {
			Region reg = WORLD.REGIONS().map.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLOR.MAP().get(reg.faction()));
				text.add(Dic.¤¤MarchingTo).insert(0, reg.info.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return Dic.¤¤Moving;
		}
	};
	
	public static boolean canBesiege(WArmy a, Region reg) {
		
		
		return (reg != null && a.faction() != reg.faction());
	}
	
	private final int index;
	
	private WArmyState() {
		all = all.join(this);
		index = all.size()-1;
	}
	
	abstract WArmyState update(WArmy a, double ds);
	
	public abstract GText info(WArmy a, GText text);
	public abstract CharSequence name(WArmy a);
	
	@Override
	public int index() {
		return index;
	}
	
}
