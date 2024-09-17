package util.gui.slider;

import init.C;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;

public class GTarget extends GuiSection{

	private static final CharSequence sPrev5 = "Previous (5)";
	private static final CharSequence sPrev = "Previous";
	private static final CharSequence sNext5 = "Next (5)";
	private static final CharSequence sNext = "Next";
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, RENDEROBJ stat, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, stat, target);
	}
	
	public GTarget(int width, boolean doubleNext, boolean horizontal, INTE target){
		this(width, (RENDEROBJ)null, doubleNext, horizontal, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, target.get());
			}
		}.r(DIR.C), target);
	}
	
	public GTarget(int width, SPRITE label, boolean doubleNext, boolean horizontal, INTE target){
		this(width, new RENDEROBJ.Sprite(label), doubleNext, horizontal, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, target.get());
			}
		}.r(DIR.C), target);
	}
	
	public GTarget(int width, SPRITE label, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, new RENDEROBJ.Sprite(label), doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, RENDEROBJ label, boolean doubleNext, boolean horizontal, GStat stat, INTE target){
		this(width, label, doubleNext, horizontal, stat.r(DIR.C), target);
	}
	
	public GTarget(int width, RENDEROBJ label, boolean doubleNext, boolean horizontal, RENDEROBJ stat, INTE target){

		if (doubleNext) {
			CLICKABLE c = new GButt.Glow(SPRITES.icons().s.minifierBig) {
				@Override
				protected void clickA() {
					target.inc(-5);
				}
				
				@Override
				protected void renAction() {
					activeSet(GTarget.this.activeIs() && target.get() > target.min());
				}
			}.repetativeSet(true).hoverInfoSet(sPrev5);
			addRightC(0, c); 
		}
		CLICKABLE c = new GButt.Glow(SPRITES.icons().s.minifier) {
			@Override
			protected void clickA() {
				if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
				{
					target.set(target.min());
				}
				else
				{
					target.inc(-1);
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(GTarget.this.activeIs() && target.get() > target.min());
			}
		}.repetativeSet(true).hoverInfoSet(sPrev);
		addRightC(0, c); 
		
		addRightC(width/2, stat);
		
		body().incrW(width/2);
		c = new GButt.Glow(SPRITES.icons().s.magnifier) {
			@Override
			protected void clickA() {
				if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
				{
					target.set(target.max());
				}
				else
				{
					target.inc(1);
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(GTarget.this.activeIs() && target.get() < target.max());
			}
		}.repetativeSet(true).hoverInfoSet(sNext);
		c.body().moveX1(body().x2()).moveCY(body().cY());
		add(c); 
		
		if (doubleNext) {
			c = new GButt.Glow(SPRITES.icons().s.magnifierBig) {
				@Override
				protected void clickA() {
					target.inc(5);
				}
				
				@Override
				protected void renAction() {
					activeSet(GTarget.this.activeIs() &&  target.get() < target.max());
				}
			}.repetativeSet(true).hoverInfoSet(sNext5);
			addRightC(0, c); 
		}
		
		if (label != null) {
		if (horizontal) {
			label.body().moveX1(-label.body().width()-C.SG*8);
			label.body().moveCY(body().cY());
		
		}else {
			label.body().moveCX(body().cX());
			label.body().moveY2(body().y1()-C.SG*2);
			
		}
		
		add(label);
		}
		
	}

	
	
	
	
}
