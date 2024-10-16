package prplegoo.qol.ui;

import snake2d.SPRITE_RENDERER;
import util.gui.misc.GBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class RealClock extends GBox {
    public static final DateTimeFormatter HOUR_MINUTE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter();

    public RealClock(){
        setTime();
    }

    private void setTime(){
        text(LocalDateTime.now().format(HOUR_MINUTE));
    }

    @Override
    public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
        clear();
        setTime();
        super.render(r, X1, X2, Y1, Y2);
    }
}
