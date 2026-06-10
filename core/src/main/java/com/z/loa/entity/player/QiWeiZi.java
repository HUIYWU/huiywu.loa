package com.z.loa.entity.player;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.z.loa.Constants;
import com.z.loa.entity.BaseEntity;

public class QiWeiZi extends BaseEntity {
    private TextureAtlas atlas;

	public QiWeiZi() {
		this(0, 0);
	}

	public QiWeiZi(float x, float y) {
		super();
		this.setPosition(x, y);
		this.orientation = Orientation.LEFT;
        super.name = "qi_wei_zi";
		super.maxHp = 3200;
		super.maxMp = 2100;
		super.remainHp = 3200;
		super.remainMp = 2100;
	}

	public void init() {
		atlas = new TextureAtlas("battle/player/packer-9.atlas");
		battleAnimation = new Animation[7];
		String[] temp = new String[]{"0_await", "1_attack", "2_skill", "3_defend", "4_weak", "5_defeated", "6_won"};
		for (int i = 0; i < temp.length; i ++) {
			Array<TextureAtlas.AtlasRegion> atlas_region = atlas.findRegions(temp[i]);
            float frame_time = 0.2f;
            if(i == 4) {
            	frame_time = 0.5f;
            }
			battleAnimation[i] = new Animation<TextureRegion>(frame_time, atlas_region);
			if (i != 1 && i != 2) {
				battleAnimation[i].setPlayMode(Animation.PlayMode.LOOP);
			}					
		}
		this.setSize(Constants.WIDTH * 79 / 108 / 176 * 57, Constants.HEIGHT * 33 / 76 / 220 * 59);
	}

    @Override
    public void decreaseRemainHp(int value) {
        super.remainHp -= 1;
    }
    
}
