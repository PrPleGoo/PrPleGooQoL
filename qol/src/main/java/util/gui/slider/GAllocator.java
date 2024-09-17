package util.gui.slider;

import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.INTE;

public class GAllocator extends ClickableAbs{

	private final SPRITE minus,plus;
	private final COLOR color;
	private final INTE target;
	private byte hoverI = 0;
	private final int width;
	private SPRITE icon;
	
	public GAllocator(SPRITE minus, SPRITE plus, COLOR color, INTE target, int width, int height){
		this.minus = minus;
		this.plus = plus;
		this.color = color;
		this.target = target;
		this.width = width;
		body.setDim(width*target.max() + minus.width() + plus.width() + 4, height);
	}
	
	public GAllocator(COLOR color, INTE target, int width, int height){
		this(SPRITES.icons().s.minifier, SPRITES.icons().s.magnifier, color, target, width, height);
	}
	
	public GAllocator(COLOR color, INTE target, int width, int height, int mmax){
		this(SPRITES.icons().s.minifier, SPRITES.icons().s.magnifier, color, target, width, height);
		body.setDim(width*mmax + minus.width() + plus.width() + 4, height);
	}
	
	public GAllocator setIcon(SPRITE icon) {
		this.icon = icon;
		body.setDim(icon.width()+8+ width*target.max() + minus.width() + plus.width() + 4, body().height());
		return this;
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (super.hover(mCoo)) {
			int ix = 0;
			if (icon != null) {
				ix = icon.width()+4;
			}
			if (mCoo.x() <= body().x1() + ix + minus.width() && mCoo.x() > body().x1()+ix) {
				hoverI = -1;
			}else if (mCoo.x() >= body().x2()-plus.width())
				hoverI = 1;
			else
				hoverI = 0;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean click() {
		if (!activeIs())
			return super.click();
		if (hoverI == 1 && target.get() < target.max()) {
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
			{
				target.set(target.max());
			}
			else
			{
				target.inc(1);
			}
		}
		else if (hoverI == -1 && target.get() > target.min()) {
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
			{
				target.set(target.min());
			}
			else
			{
				target.inc(-1);
			}
		}
		return super.click();
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		int ix = 0;
		if (icon != null) {
			icon.renderCY(r, body().x1(), body().cY());
			ix = icon.width()+8;
		}
		
		if (!activeIs() || target.get() <= target.min()) {
			GCOLOR.T().INACTIVE.bind();
		}else if(hoverI == -1) {
			GCOLOR.T().HOVERED.bind();
		}
		
		
		minus.renderCY(r, body().x1()+ix, body().cY());
		
		ColorImp.TMP.set(color).shadeSelf(0.5);
		
		for (int i = 0; i < target.max(); i++) {
			int x1 = body().x1()+minus.width()+2+i*width + ix;
			ColorImp.TMP.render(r, x1, x1+width-2, body().y1(), body().y2());
			if (i < target.get())
				color.render(r, x1+1, x1+width-2, body().y1(), body().y2()-1);
		}
		
		if (!activeIs() || target.get() >= target.max()) {
			GCOLOR.T().INACTIVE.bind();
		}else if(hoverI == 1) {
			GCOLOR.T().HOVERED.bind();
		}else
			COLOR.unbind();
		
		plus.renderCY(r, body().x2()-plus.width(), body().cY());
		
		COLOR.unbind();
		
		hoverI = 0;
		
	}
	
	

}
