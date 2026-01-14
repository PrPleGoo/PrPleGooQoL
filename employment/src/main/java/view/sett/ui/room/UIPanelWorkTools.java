package view.sett.ui.room;

import init.constant.C;
import init.sprite.SPRITES;
import init.type.POP_CL;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.employment.RoomEquip;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GAllocator;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.sett.ui.room.UIPanelUtil.RoomRow;

final class UIPanelWorkTools extends ISidePanel {

	private static CharSequence ¤¤level = "Current Available Max Level:";

	static {
		D.ts(UIPanelWorkTools.class);
	}

	public UIPanelWorkTools(RoomEquip work) {

		section.add(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, work.currentTotal(), work.neededTotal());
			}
		}.increase().r(DIR.N));

		RENDEROBJ q = new UIPanelUtil.BlueprintList(HEIGHT - section.body().y2() - C.SG * 16) {

			@Override
			RENDEROBJ row(RoomBlueprintIns<?> bb) {
				if (bb.employment() == null || !work.has(bb.employment())) {
					return null;
				}

				// We can cycle through the employment to create multiple jobs
				RoomRow r = new RoomRow(bb) {
					@Override
					public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
						super.hoverInfoGet(text);
						GBox b = (GBox) text;
						b.NL(8);

						b.textL(¤¤level);
						// This is for tools
						b.add(GFORMAT.i(b.text(), work.target(bb.employment()).availableMax()));
						b.NL();
						if (work.target(bb.employment()).boost() != null) {
							work.target(bb.employment()).boost().hoverDetailed(b, POP_CL.clP(), null, true);
						}
						b.sep();

						b.textL(Dic.¤¤Boosts);
						b.NL();
						work.boost(bb.employment()).booster.hover(b, (double)work.targetI(bb.employment())/(work.target(bb.employment()).max()*bb.employment().employed()));
						work.boost(bb.employment()).booster.hoverSpan(b, (double)work.targetI(bb.employment())/(work.target(bb.employment()).max()*bb.employment().employed()));
						b.NL();
					};
				};

				r.addRelBody(8, DIR.E, new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.i(text, work.targetI(bb.employment()));
					}
				});

				r.addRelBody(48, DIR.E, new GAllocator(COLOR.ORANGE100.makeSaturated(0.7), work.target(bb.employment()), 6, 16));
				r.body().incrW(420-r.body().width());
				r.pad(16, 0);
				return r;
			}

			@Override
			protected void addToCat(GuiSection s, RoomCategoryMain cat) {


				RENDEROBJ sss = new GStat() {

					@Override
					public void update(GText text) {

						int needed = 0;
						for (RoomBlueprintImp b : cat.all())
							if (b.employment() != null)
								needed += work.targetI(b.employment());

						GFORMAT.i(text, needed);
					}
				}.r(DIR.W);
				sss.body().moveX1(s.body().x1());
				sss.body().moveY1(s.getLastY2());
				s.add(sss);

				RENDEROBJ r = new GButt.Glow(SPRITES.icons().s.magnifier) {

					@Override
					protected void clickA() {
						for (int bi = 0; bi < cat.all().size(); bi++) {
							RoomBlueprintImp b = cat.all().get(bi);
							if (b instanceof RoomBlueprintIns<?>) {

								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employment() != null) {
									work.target(bb.employment()).inc(1);
								}
							}
						}
					}

				}.hoverInfoSet(UIPanelWorkPrio.¤¤Adjust);

				r.body().moveX1(s.body().x2()+32);
				r.body().moveCY(s.body().cY()-16);
				s.add(r);

				r = new GButt.Glow(SPRITES.icons().s.minifier) {

					@Override
					protected void clickA() {
						for (int bi = 0; bi < cat.all().size(); bi++) {
							RoomBlueprintImp b = cat.all().get(bi);
							if (b instanceof RoomBlueprintIns<?>) {

								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employment() != null) {
									work.target(bb.employment()).inc(-1);
								}
							}
						}
					}

				}.hoverInfoSet(UIPanelWorkPrio.¤¤Adjust);

				s.addDownC(8, r);
			}
		};

		section.addRelBody(8, DIR.S, q);


		titleSet(work.resource.names);
	}

}
