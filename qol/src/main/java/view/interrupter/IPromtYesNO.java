package view.interrupter;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GTextR;
import util.gui.panel.GPanel;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;

/**
 * centered medium sized panel. Can be persistent or dismissable. Composes of a
 * question and a yes and no button.
 * 
 * @author mail__000
 *
 */
public class IPromtYesNO extends Interrupter {

	private final GTextR text = new GTextR(UI.FONT().M, 1000, DIR.C);
	private final GuiSection section = new GuiSection();
	private final GPanel box = new GPanel().setDim(800, 400);
	{}
	private final ACTION close = new ACTION() {
		@Override
		public void exe() {
			deactivate();
		}

	};

	private final GButt.ButtPanel yes = new GButt.ButtPanel(SPRITES.icons().m.ok) {
		@Override
		protected void clickA() {
			hide();
		};
	};
	private final GButt.ButtPanel no = new GButt.ButtPanel(SPRITES.icons().m.cancel) {
		@Override
		protected void clickA() {
			hide();
		};
	};
	private boolean dismissable;
	private final InterManager m;

	private ACTION yesAction;
	private ACTION noAction;

	public IPromtYesNO(InterManager manager) {
		this.m = manager;
		text.text().lablify();
		section.add(box);
		section.body().centerIn(C.DIM());
		box.setBig();
		
		yes.body.setWidth(100);
		no.body.setWidth(100);
		yes.hoverInfoSet(Dic.¤¤Yes);
		no.hoverInfoSet(Dic.¤¤No);
		
		GuiSection butts = new GuiSection();

		butts.add(yes).addRight(0, no);
		
		butts.body().centerX(section);
		butts.body().moveY2(section.body().y2()-16);
		section.add(butts);

		text.text().setMaxWidth(800);
		;
		section.add(text);

	}

	static boolean tmp = false;
	
	public void activate(CharSequence message, ACTION yesAction, ACTION noAction, boolean dismissable) {
		show(m);

		this.yesAction = yesAction;
		this.noAction = noAction;
		this.dismissable = dismissable;

		section.clear();
		text.text().set(message);
		text.adjust();
		if (text.body().width() < 600)
			section.body().setDim(600, 1);
		section.addDownC(0, text);
		yes.clickActionSet(yesAction);
		
		if (noAction != null) {
			int cx = section.body().cX();
			no.clickActionSet(noAction);
			section.add(yes, cx-yes.body.width(), section.getLastY2()+16);
			section.add(no, cx, section.getLastY1());
		}else {
			section.addDownC(16, yes);
		}
		
		section.body().centerIn(C.DIM());
		
		box.setCloseAction(dismissable ? close : null);
		box.inner().set(section.body());
		section.add(box);
		section.moveLastToBack();
		
//		text.body().centerIn(box.inner());
//		text.body().incrY(-12);
//		yes.clickActionSet(yesAction);
//		no.clickActionSet(noAction);
//		no.visableSet(noAction != null);

	}

	public void deactivate() {
		hide();
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		else if (button == MButt.RIGHT) {
			deactivate();
			if(!dismissable && noAction != null)
				noAction.exe();
		}
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.MAIN().ESCAPE.consumeClick()) {
			deactivate();
			if(!dismissable && noAction != null)
				noAction.exe();
			return true;
		}
		else if (KEYS.MAIN().ENTER.consumeClick()
				|| KEYS.MAIN().KP_ENTER.consumeClick())
		{
			deactivate();
			if(yesAction != null)
				yesAction.exe();
			return true;
		}

		KEYS.clear();
		return false;
	}

	@Override
	public boolean canSave() {
		return dismissable;
	}

}
