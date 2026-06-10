package com.z.loa.entity.player;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.z.loa.Constants;
import com.z.loa.entity.*;
import com.z.loa.entity.npc.*;
import com.z.loa.screen.*;

public class ZhanTingyun extends BaseEntity {
	private Texture zhanTingyun, button;
	private boolean showMood;
    private Vector2 prebattlePosition;
    private Stage preBattleStage;
    private Vector2 prebattleSize;

	public ImageButton[] buttons;
	public static float expectX, expectY;
    

	private static final float SPEED = 8.0f;

	public ZhanTingyun(float x, float y) {
		this.setPosition(x, y);
        super.name = "zhan_tingyun";
        super.maxHp = 3100;
		super.maxMp = 1500;
		super.remainHp = 3100;
		super.remainMp = 1500;
		entityState = EntityState.IDLE;
		orientation = Orientation.DOWN;
		this.create();
	}

	public void create() {
		zhanTingyun = new Texture(Gdx.files.internal("man/character01003.png"));
		button = new Texture(Gdx.files.internal("button.png"));
		buttons = new ImageButton[5];

		TextureRegion[][] split = TextureRegion.split(zhanTingyun, zhanTingyun.getWidth() / 3,
				zhanTingyun.getHeight() / 4);
		TextureRegion[][] split_b = TextureRegion.split(button, button.getWidth() / 2, button.getHeight() / 5);
		idleTextureArray = new TextureRegion[]{split[0][1], split[1][1], split[2][1], split[3][1]};

		for (int i = 0; i < 4; i++) {
			TextureRegion[] temp = new TextureRegion[3];
			for (int j = 0; j < 3; j++) {
				temp[j] = split[i][j];
			}
			moveAnimation[i] = new Animation<TextureRegion>(0.30f, temp);
			moveAnimation[i].setPlayMode(Animation.PlayMode.LOOP);
		}
		this.setSize(split[0][0].getRegionWidth() * GameScreen.scale, split[0][0].getRegionHeight() * GameScreen.scale);

		for (int i = 0; i < 5; i++) {
			ImageButton.ImageButtonStyle style_d = new ImageButton.ImageButtonStyle();
			style_d.up = new TextureRegionDrawable(split_b[i][0]);
			style_d.down = new TextureRegionDrawable(split_b[i][1]);
			buttons[i] = new ImageButton(style_d);
		}

		for (int i = 0; i < 5; i++) {
			buttons[i].setSize(100, 100);
		}
		buttons[0].setPosition(Gdx.graphics.getWidth() - 200, 100);
		buttons[1].setPosition(Gdx.graphics.getWidth() - 200, 300);
		buttons[2].setPosition(100, 200);
		buttons[3].setPosition(300, 200);
		buttons[4].setPosition(200, 100);

		for (int i = 0; i < 4; i++) {
			final int j = i;
			buttons[i].addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					entityState = EntityState.MOVE;
					orientation = updateOrientation(j);
					return true;

				}
				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					entityState = EntityState.IDLE;
					super.touchUp(event, x, y, pointer, button);
				}
			});
		}

	}
    
    public void init() {
        this.entityState = EntityState.BATTLE;
        TextureAtlas atlas = new TextureAtlas("battle/player/packer-8.atlas");
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
    
    public void recordPosition() {
    	prebattlePosition = new Vector2(this.getX(), this.getY());
        preBattleStage = this.getStage();
        prebattleSize = new Vector2(this.getWidth(), this.getHeight());
        
    }
    
    public void recoverPosition() {
        entityState = EntityState.IDLE;
        this.setPosition(prebattlePosition.x, prebattlePosition.y);
        this.preBattleStage.addActor(this);
        this.setSize(prebattleSize.x, prebattleSize.y);
    }

	private Orientation updateOrientation(int i) {
		Orientation ori = null;
		switch (i) {
			case 0 :
				ori = Orientation.DOWN;
				break;
			case 1 :
				ori = Orientation.UP;
				break;
			case 2 :
				ori = Orientation.LEFT;
				break;
			case 3 :
				ori = Orientation.RIGHT;
				break;
		}
		return ori;
	}

	public Rectangle getRectangle() {
		return GameScreen.playerMap.get(this);
	}

	private void updateRectangle() {
		Rectangle rectangle = new Rectangle(this.getX() / GameScreen.scale, this.getY() / GameScreen.scale, 16, 16);
		GameScreen.playerMap.put(this, rectangle);
	}

	public void updatePosition() {
		float actor_x = this.getX();
		float actor_y = this.getY();
		float new_x = actor_x, new_y = actor_y;
		expectX = actor_x;
		expectY = actor_y;
		switch (entityState) {
			case IDLE :
				switch (orientation) {
					case DOWN :
						expectY -= SPEED;
						break;
					case UP :
						expectY += SPEED;
						break;
					case LEFT :
						expectX -= SPEED;
						break;
					case RIGHT :
						expectX += SPEED;
						break;
				}
				break;
			case BATTLE :
				break;
			case MOVE :
				switch (orientation) {
					case DOWN :
						new_y -= SPEED;
						break;
					case UP :
						new_y += SPEED;
						break;
					case LEFT :
						new_x -= SPEED;
						break;
					case RIGHT :
						new_x += SPEED;
						break;
				}
				break;

		}
		if (GameScreen.isNotTerrainCollision(new_x, new_y) && NPC.isNotCharacterCollision(this, null, new_x, new_y)) {
			this.setPosition(new_x, new_y);
			updateRectangle();
		}
	}

	public void matchMoodAnimation(Animation<TextureRegion> animation) {
		this.moodAnimation = animation;
		currentMoodFrame = animation.getKeyFrame(moodTime);
		showMood = true;

	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (moodAnimation != null) {
			moodTime += delta;
			currentMoodFrame = moodAnimation.getKeyFrame(moodTime);
			if (moodAnimation.isAnimationFinished(moodTime)) {
				showMood = false;
				moodTime = 0;
				moodAnimation = null;
			}

		}

	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		updatePosition();	
		super.draw(batch, parentAlpha);
		if (showMood) {
			batch.draw(currentMoodFrame, this.getX() + this.getWidth() / 8, this.getY() + this.getHeight(),
					this.getWidth() * 0.75f, this.getWidth() * 0.75f);
		}

	}

}

