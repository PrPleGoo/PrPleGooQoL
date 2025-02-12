package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.sprite.UI.UI;
import init.text.D;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.ui.message.MessageText;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.RDOutputs.RDOutput;
import world.region.RData.RDataE;

public class RDHealth extends RDataE {

	public static CharSequence ¤¤name = "¤Health";
	public static CharSequence ¤¤desc = "¤Health must be maintained at over 50% in a region, else there is a risk of disease.";
	
	private static CharSequence ¤¤pos = "¤You have health moving towards more than 50% and need not fear an outbreak of disease.";
	private static CharSequence ¤¤neg = "¤You have health plummeting  below 50%, and an outbreak of disease is to be expected.";
	private static CharSequence ¤¤out = "¤This region is currently suffering from an outbreak of disease. You must increase health quickly.";
	
	static CharSequence ¤¤epidemic = "¤Outbreak";
	private static CharSequence ¤¤epidemicD = "¤The region of {0} has suffered from low health and as a result there has been an outbreak of disease. While the epidemic is lasting, the region will suffer big penalties across the board. You must increase the health to over 50% in order to save the settlement.";
	
	
	static {
		D.ts(RDHealth.class);
	}
	
	public final Boostable boostablee;
	public final RDataE outbreak;
	private boolean btoggle = true;
	
	private static double dTime = 1.0/(TIME.secondsPerDay*2);
	
	private CharSequence eDesc(Region reg) {
		Str.TMP.clear().add(¤¤epidemicD).insert(0, reg.info.name());
		return Str.TMP;
	}
	
	public void hover(GBox b, Region reg) {
		b.title(RD.HEALTH().boostablee.name);
		b.text(RD.HEALTH().boostablee.desc);
		b.NL(4);
		
		if (outbreak.isMax(reg)) {
			b.error(b.text().clear().add(¤¤out).insert(0, reg.info.name()));
		}else if (boostablee.get(reg) < 0.5) {
			b.add(b.text().warnify().add(¤¤neg));
		}else {
			b.add(b.text().normalify2().add(¤¤pos));
		}
		b.NL(4);
		
		b.textLL(Dic.¤¤Current);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), getD(reg)));
		b.NL();
		b.textLL(Dic.¤¤Target);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), CLAMP.d(boostablee.get(reg), 0, 1)));
		b.NL();
		b.sep();
		boostablee.hover(b, reg, true);
		
	}
	
	public void problem(GBox b, Region reg) {
		if (outbreak.isMax(reg)) {
			b.NL();
			b.error(b.text().clear().add(¤¤out).insert(0, reg.info.name()));
			b.NL();
		}
		
	}
	
	
	RDHealth(RDInit init) {
		super("HEALTH", init.count.new DataByte("HEALTH"), init, ¤¤name);
		boostablee = BOOSTING.push("HEALTH", 1, ¤¤name, ¤¤desc, UI.icons().s.heart,  BoostableCat.ALL().WORLD);
		
		new RBooster(new BSourceInfo(Dic.¤¤Population, UI.icons().s.human), 0, -10, false) {

			@Override
			public double get(Region t) {
				if (btoggle)
					return RD.RACES().popSize(t);
				return RD.RACES().popSizeTarget(t);
			}
			
			
		}.add(boostablee);
		
		outbreak = new RDataE("OUTBREAK", init.count.new DataBit("OUTBREAK"), init, ¤¤epidemic);
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				RBooster bo = new RBooster(new BSourceInfo(¤¤epidemic, UI.icons().s.death), 1, 0, true) {
					
					@Override
					protected double get(Region reg) {
						return outbreak.get(reg);
					}
				};
				for (RDOutput o : RD.OUTPUT().ALL) {
					bo.add(o.boost);
				}
				bo = new RBooster(new BSourceInfo(¤¤epidemic, UI.icons().s.death), 1, 0, true) {
					
					@Override
					protected double get(Region reg) {
						return outbreak.get(reg);
					}
				};
				bo.add(RD.RACES().capacity);
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			@Override
			public void update(Region reg, double time) {
				


				double d = increase(reg)*dTime*time;
				moveTo(reg, Math.abs(d), d < 0 ? 0 : 255);
				int target = (int) (255*(boostablee.get(reg)));
				
				if (reg.faction() == FACTIONS.player() && !reg.capitol()) {
					if (get(reg)  < 120 && outbreak.get(reg) == 0 && target < 120) {
						outbreak.set(reg, 1);
						new MessageText(¤¤epidemic).paragraph(eDesc(reg)).send();
					}else if (outbreak.get(reg) == 1 && (get(reg) > 128) && target > 128) {
						outbreak.set(reg, 0);
					}
					
					
				}
					
				
			}
			
			public double increase(Region reg) {
//				if (reg.faction() == null || reg.faction() instanceof FactionNPC)
//					return 255;
				
				
				//btoggle = true;
				double target = 255*(boostablee.get(reg)*10)/10.0;
				//btoggle = false;
				return target;
			}
			
			@Override
			public void init(Region reg) {
				setD(reg, 1.0);
			}
		});
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player())
					setD(reg, 1.0);
			}
		};
	}

}
