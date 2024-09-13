package view.world.ui.region;

import game.boosting.BOOSTING;
import game.boosting.BoostableCat;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.dic.Dic;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDEdicts.RDRaceEdict;
import world.region.pop.RDRace;

final class PlayPop extends GuiSection{
	
	static CharSequence ¤¤eWarning = "¤Enabling an edict has a global effect in your whole kingdom. The affected race will have their loyalty decreased in all regions.";
	
	public PlayPop(GETTER_IMP<Region> g, int width, int height) {

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		{
			GuiSection h = new GuiSection();
			
			{
				
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, RD.RACES().population.get(g.get()));
					}
				};
				
				SPRITE pop = new SPRITE.Imp(140, 24) {
					
					@Override
					public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
						double n = RD.RACES().population.get(g.get());
						double nn = RD.RACES().popTarget.getD(g.get());
						double mm = Math.max(n, nn);
						n /= mm;
						nn/= mm;
						GMeter.render(rr, GMeter.C_GRAY, n, nn, X1, X2, Y1, Y2);
						s.adjust();
						X1 += 8;
						Y1 = Y1+((Y2-Y1)-s.height())/2;
						OPACITY.O50.bind();
						COLOR.BLACK.render(rr, X1-1, X1+s.width()+2, Y1+2, Y1+s.height()-2);
						OPACITY.unbind();
						s.render(rr, X1, Y1);
					}
				};
				
				h.add(new GHeader.HeaderHorizontal(UI.icons().m.citizen.resized(24), pop) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						
						b.title(RD.RACES().population.name);
						
						b.textLL(Dic.¤¤Current);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), RD.RACES().population.get(g.get())));
						b.NL();
						b.textLL(Dic.¤¤Target);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), (int)RD.RACES().popTarget.getD(g.get())));
						b.sep();
						
						
						RD.RACES().capacity.hover(b, g.get(), RD.RACES().capacity.name, true);
						
						b.NL(8);
						b.tab(1);
						b.textL(Dic.¤¤Used);
						
						double d = RD.RACES().capacity.get(g.get())*RD.RACES().population.get(g.get())/(RD.RACES().popTarget.getD(g.get()));
						
						b.tab(5);
						b.add(GFORMAT.f0(b.text(), -d));
					}
				});
				
			}
			
			
			
			h.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, RD.RACES().loyaltyAll.getD(g.get()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					RD.RACES().loyaltyAll.info().hover(b);
				};
				
				
			}.hh(UI.icons().m.rebellion), 252);
			
			h.addCentredY(new Sprite(UI.icons().m.descrimination) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					RD.RACES().edicts.sanction.info.hover(text);
				}
			},476);
			
			h.addCentredY(new Sprite(UI.icons().m.exit) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					RD.RACES().edicts.exile.info.hover(text);
				}
			},476+32);
			
			h.addCentredY(new Sprite(UI.icons().m.skull) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					RD.RACES().edicts.massacre.info.hover(text);
				}
			},476+32*2);
			
			add(h);
		}
		
		
		
		for (RDRace r : RD.RACES().all) {
			GuiSection row = new GuiSection();
			row.addRightC(0, new Sprite(r.race.appearance().icon){

				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					r.race.info.hover(b);
					b.NL(8);
					
					r.race.boosts.hover(text, 1.0, BoostableCat.TYPE_WORLD);
					
					text.NL(8);
					
					r.race.pref().hoverOther(b);
				}
				
				
			});
			
			GuiSection popGrowth = new GuiSection() {
				
				String mt = Dic.¤¤Modifiers + ": " + Dic.¤¤Target;
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(r.pop.name);
					
					b.textLL(Dic.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), r.pop.get(g.get())));
					b.NL();
					
					b.textLL(Dic.¤¤Target);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), r.pop.target(g.get())));
					b.NL();
					
					b.textLL(Dic.¤¤Growth);
					b.tab(6);
					b.add(GFORMAT.percInc(b.text(), r.pop.growth(g.get())));
					b.NL();
					
					b.textLL(Dic.¤¤Capacity);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), (int)RD.RACES().capacity.get(g.get())));
					b.NL();
					
					b.textLL(Dic.¤¤Rarity);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), r.pop.maxPopulation));
					b.NL();
					

					b.sep();
					
					r.pop.dtarget.hover(b, g.get(), mt, true);
					
					b.sep();
					
					r.pop.growth.hover(b, g.get(), Dic.¤¤Growth, true);
				}
				
			};
			
			RENDEROBJ p = new RenderImp(140, 16) {
				
				@Override
				public void render(SPRITE_RENDERER rr, float ds) {
					
					double c = r.pop.get(g.get())/(r.pop.maxPopulation*RD.RACES().capacity.get(g.get())+1.0);
					double t = r.pop.dtarget(g.get());

					GMeter.render(rr, GMeter.C_GRAY, c, t, body.x1(), body.x2(), body.y1(), body.y2());
					//GMeter.render(rr, GMeter.C_GRAY, n, body);
					
					
				}
			};
			
			popGrowth.addRightC(6, p);
			
			popGrowth.addRightC(8, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.percInc(text, r.pop.growth(g.get()));
				}
				
			});
			
			popGrowth.body().incrW(64);
			
			row.addRightC(8, popGrowth);
			
			GuiSection loyalty = new GuiSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(r.loyalty.name);
					
					b.textLL(Dic.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), r.loyalty.getD(g.get())));
					b.NL();
					b.textLL(Dic.¤¤Target);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), r.loyalty.target.get(g.get())));
					b.NL();
					b.textLL(Dic.¤¤Increase);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), r.loyaltyTarget(g.get())));
					b.NL();

					b.NL();
					b.sep();

					r.loyalty.target.hover(b, g.get(), Dic.¤¤Factors, true);
				}
				
			};
			
			HoverableAbs l = new HoverableAbs(140, 16) {
				
				@Override
				protected void render(SPRITE_RENDERER rr, float ds, boolean isHovered) {
					double c = (1 + r.loyalty.getD(g.get())/10.0)/2.0;
					double t = (1 + r.loyalty.target.get(g.get())/10.0)/2.0;
					GMeter.renderC(rr, c, t, body);
				}
			
			};
			loyalty.add(l);
			loyalty.addRightC(8, new GStat() {

				@Override
				public void update(GText text) {
					double gg = ((int)((r.loyalty.target.get(g.get()))*100))/100.0;
					GFORMAT.f0(text, gg);
				}
				
			});
			loyalty.body().incrW(66);
			row.addRightC(8, loyalty);
			
			for (RDRaceEdict e : RD.RACES().edicts.all) {
				
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						int i = (e.toggled(r).get(g.get())+1)&1;
						for (RDRaceEdict ee : RD.RACES().edicts.all)
							ee.toggled(r).set(g.get(), 0);
						e.toggled(r).set(g.get(), i);
					}
				};
				
				row.addRightC(8, new GButt.Checkbox(){
					@Override
					protected void clickA() {
						
						
						int i = (e.toggled(r).get(g.get())+1)&1;
						if (i == 1 && !(CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))) {
							VIEW.inters().yesNo.activate(¤¤eWarning, a, ACTION.NOP, true);
						}
						else {
							a.exe();
						}
					};
					
					@Override
					protected void renAction() {
						selectedSet(e.toggled(r).get(g.get()) == 1);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						e.info.hover(text);
						text.NL(8);
						e.boosts.hover(text, 1.0, BOOSTING.TYPES().REGION, BOOSTING.TYPES().FACTION, BoostableCat.TYPE_WORLD);
						e.boosts.hover(text, 1.0, BOOSTING.TYPES().FACTION, BOOSTING.TYPES().DIV, BoostableCat.TYPE_WORLD);
					}
					
					
				});
			}
			
			
			row.body().setWidth(width-24);
			
			row.add(new RenderImp(row.body().width(), 4) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GCOLOR.UI().border().render(r, body.x1(), body.x2(), body.y1()+1, body.y1()+2);
				}
			}, 0, row.body().y2());
			
			
			rows.add(row);
			
		}
		
		height-= body().height();
		height = rows.get(0).body().height()*(height/rows.get(0).body().height());
		
		add(new GScrollRows(rows, height).view(), 0, body().y2());
	}
	
	
}
