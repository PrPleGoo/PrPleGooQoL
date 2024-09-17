package view.interrupter;

import init.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.keyboard.KEYS;

public class IPromtScreen extends Interrupter{

	private COLOR c = COLOR.WHITE100;
	private CharSequence message;
	private final RENDEROBJ.RenderImp text = new RENDEROBJ.RenderImp(600, 100) {

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			c.bind();
			UI.FONT().H2.renderIn(r, body, DIR.N, message);
			COLOR.unbind();
		}
		
	};
	private GButt[] butts;
	private GButt hovered;
	private ACTION deactivateAction;
	private final InterManager m;
	
	public IPromtScreen(InterManager manager) {
		pin();
		this.m = manager;
		text.body().centerIn(C.DIM());
	}
	
	public void activate(CharSequence message, COLOR c, ACTION deactivateAction, GButt... butts){
		super.show(m);
		this.message = message;
		this.c = c;
		text.body().centerIn(C.DIM());
		
		this.butts = butts;
		hovered = null;
		
		this.deactivateAction = deactivateAction;
		
		if (butts.length == 0)
			return;
		
		int w = C.WIDTH()/12;
		int y = text.body().y2() + C.SCALE*10;
		int x = C.WIDTH()/2;
		x -= butts.length*w/2;
		
		for (GButt b : butts){
			b.body().moveX1Y1(x - b.body().width()/2, y);
			x+= 2*w;
		}
		
	}

	public void deactivate() {
		if (deactivateAction != null)
			deactivateAction.exe();
		hide();
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		text.render(r, ds);
		if (butts.length == 0)
			return false;
		for (GButt b : butts)
			b.render(r, ds);
		return false;
	}


	@Override
	protected void mouseClick(MButt button) {
		if (butts.length == 0){
			deactivate();
		}
		
		if (hovered != null && hovered.hoveredIs()){
			deactivate();
			hovered.click();
		}

		
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (butts.length == 0)
			return true;
		
		if (hovered != null && hovered.hover(mCoo))
			return true;
		
		for (GButt b : butts)
			if (b.hover(mCoo))
				hovered = b;
		
		return true;
	}

	@Override
	protected boolean update(float ds) {
		
		if (KEYS.MAIN().ESCAPE.consumeClick() || KEYS.MAIN().ENTER.consumeClick() || KEYS.MAIN().KP_ENTER.consumeClick())
			deactivate();
		KEYS.clear();
		return false;
	}
	
}
