package com.z.loa;

import com.badlogic.gdx.Gdx;

public class Constants {
	public static final float WIDTH;
	public static final float HEIGHT;
    public static final float VISION_WIDTH;
    public static final float VISION_HEIGHT;
    public static final float WIDTH_RATIO;
    public static final float HEIGHT_RATIO;
    public static final float LEFT_SIDE_X;
    public static final float BELOW_Y;
    public static final float RIGHT_SIDE_X;
    public static final float TOP_Y;

	static {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
        VISION_WIDTH = 790.0f;
        VISION_HEIGHT = 990.0f;
        WIDTH_RATIO = VISION_WIDTH / 176;
        HEIGHT_RATIO = VISION_HEIGHT / 220;
        LEFT_SIDE_X = (WIDTH - VISION_WIDTH) / 2;
        BELOW_Y = (HEIGHT - 930.0f) / 2;
        RIGHT_SIDE_X = (WIDTH + VISION_WIDTH) / 2;
        TOP_Y = BELOW_Y + 990.0f;
	}

}