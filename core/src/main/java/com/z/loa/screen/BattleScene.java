package com.z.loa.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.z.loa.*;
import com.z.loa.entity.*;
import com.z.loa.entity.config.BattleActionConfig;
import com.z.loa.entity.enemy.*;
import com.z.loa.entity.event.battle.EffectFinishEvent;
import com.z.loa.entity.event.battle.EffectTriggerEvent;
import com.z.loa.entity.player.*;
import com.z.loa.manager.*;
import com.z.loa.ui.StateProgressTable;
import com.z.loa.ui.TwinLabelMarquee;

public class BattleScene {
	private Stage battleStage;
	private boolean visible;
	private MyGdxGame game;
    private ZhanTingyun zhanTingyun;
	private Table table;
	private Stack battleStack;
    private float battleStackWidth;
    private float battleStackHeight;
	private Table buttonTable;
    //private TextButton activeButton;
    private Stack bottomStack;
    private Table stateTable;
    private Stack avatarStack;
    
    private Table[] skillTables;
    //private Table itemTable;
	private BottomBar activeBar;
	private TextureRegionDrawable[] drawable;//通常用于各部分背景
    private TextureRegionDrawable[][] avatarDrawables;
	private TextButton[] buttons;
	private ObjectMap<String, Animation<TextureRegion>> effectAnimationMap;
	private ObjectMap<TextButton, String> stringMap;
	private ObjectMap<TextButton, BottomBar> barMap;
    private ObjectMap<BaseEntity, Table> playerSkillTableMap;
    private ObjectMap<BaseEntity, Image> avatarMap;
    private ObjectMap<CheckBox, BaseEntity> checkEntityMap;
    private ObjectMap<BaseEntity, Label> floatingLabelMap;//伤害或回复数值显示
    
	private Group characterGroup;
	private Array<BaseEntity> characterArray;
    private Array<BaseEntity> playerArray;
    private Array<EffectActor> pendingAddEffects;
    private Array<EffectActor> pendingRemoveEffects;
    private ButtonGroup<CheckBox> playerCheckBoxGroup;
    private ButtonGroup<CheckBox> enemyCheckBoxGroup;
	private Dialog dialog;
    private Dialog checkDialog;
    private Dialog endedDialog;
	private Label.LabelStyle messageStyle;

	private Image buttonMask;
	private Stack buttonStack;

	private TwinLabelMarquee twin;
	private Container<TwinLabelMarquee> container;
    public StateProgressTable[] hpProgressTables;
    private StateProgressTable[] mpProgressTables;
    private Table[] characters;
    
    private BattleActionManager actionManager;
    private EffectManager effectManager;
    private TurnManager turnManager;
    
    private String mark;//标记之前改动地方，不参与游戏逻辑

    private enum BottomBar {
        STATE,
        ATTACK,
        SKILL,
        ITEM,
        ESCAPE,
        AUTO;
    }

	public BattleScene(MyGdxGame game) {
		this.game = game;
		this.battleStage = new Stage();
		this.table = new Table();
		this.battleStack = new Stack();
		this.buttonTable = new Table();
        this.bottomStack = new Stack();
		this.drawable = new TextureRegionDrawable[3];
		this.buttons = new TextButton[6];
        this.buttonStack = new Stack();
		this.effectAnimationMap = new ObjectMap<>();
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("battle/effect/packer-1.atlas"));
		TextureAtlas atlas_1 = new TextureAtlas(Gdx.files.internal("battle/effect/packer-2.atlas"));
		TextureAtlas atlas_2 = new TextureAtlas(Gdx.files.internal("battle/effect/packer-3.atlas"));
		TextureAtlas atlas_3 = new TextureAtlas(Gdx.files.internal("battle/effect/packer-0.atlas"));
		effectAnimationMap.put("animation07001",new Animation<TextureRegion>(0.2f, atlas.findRegions("animation07001")));
		effectAnimationMap.put("animation01003",new Animation<TextureRegion>(0.2f, atlas_1.findRegions("animation01003")));
		effectAnimationMap.put("animation07002",new Animation<TextureRegion>(0.2f, atlas_2.findRegions("animation07002")));
		effectAnimationMap.put("animation07003",new Animation<TextureRegion>(0.2f, atlas_3.findRegions("animation07003")));
        String[] temp = {"01009", "12080", "12081", "12026", "12025", "12077", "12078", "12079", "09006", "12033", "05009", "08004", "12082", "12083"};
        for (int i = 12; i <= 25; i++) {
            TextureAtlas atlas_t =new TextureAtlas(Gdx.files.internal(String.format("battle/effect/packer-%d.atlas", i)));
            String region_name = "animation" + temp[i - 12];
            Animation<TextureRegion> animation = new Animation<TextureRegion>(0.2f, atlas_t.findRegions(region_name));
            effectAnimationMap.put(region_name, animation);
        }
        BattleActionConfig.loadConfig();
	}

	public void init(ZhanTingyun zhan_tingyun) {
        this.zhanTingyun = zhan_tingyun;
		this.activeBar = BottomBar.STATE;
		this.table.setPosition(Constants.LEFT_SIDE_X, Constants.BELOW_Y);
		this.table.setSize(790.0f, 990.0f);
		String[] temp = {"background/background01001", "state/button_bg", "state/state_bg",};
		for (int i = 0; i < drawable.length; i ++) {
			Texture texture = new Texture(Gdx.files.internal("battle/" + temp[i] + ".png"));
			drawable[i] = new TextureRegionDrawable(texture);
		}
		createBattleStack();
		createButtons();
		buttonStack.add(buttonTable);
        Image bottom_background = new Image(drawable[2]);
        bottomStack.add(bottom_background);
        
        this.table.left().bottom().add(battleStack).height(Constants.HEIGHT_RATIO * 145.0f).fill().row();
		this.table.left().bottom().add(buttonStack).expandX().fill().row();
		this.table.left().bottom().add(bottomStack).width(Constants.VISION_WIDTH * 177 / 176).height(Constants.HEIGHT_RATIO * 59).expandX().fill().row();
		//table.debugAll();
		this.battleStage.addActor(table);
		setDialog();
        
	}

	private void createBattleStack() {
		battleStack.setSize(Constants.VISION_WIDTH, Constants.HEIGHT_RATIO * 145);
		battleStackWidth = battleStack.getWidth();
		battleStackHeight = battleStack.getHeight();
		Image battle_background = new Image(drawable[0]);
		battleStack.add(battle_background);
        
        characterGroup = new Group();
		characterArray = new Array<BaseEntity>();
        playerArray = new Array<BaseEntity>();
        pendingAddEffects = new Array<EffectActor>();
        pendingRemoveEffects = new Array<EffectActor>();
        floatingLabelMap = new ObjectMap<BaseEntity, Label>();
		battleStack.add(characterGroup);
        actionManager = new BattleActionManager(characterArray, this);
        effectManager = new EffectManager(this, characterGroup, pendingAddEffects);
    }
    
    public void placeEntity() {
        this.zhanTingyun.recordPosition();
        zhanTingyun.init();
        zhanTingyun.setPosition(battleStack.getWidth() - zhanTingyun.getWidth(), battleStack.getHeight() * 1 / 16);
        characterGroup.clear();
        if (!characterArray.isEmpty()) {//一般用于二次进入场景
        	for(BaseEntity e : characterArray) {
        		characterGroup.addActor(e);
                characterGroup.addActor(floatingLabelMap.get((e)));
        	}
            return;
        }
        QiWeiZi qi_wei_zi = new QiWeiZi();
        qi_wei_zi.init();
        qi_wei_zi.setPosition(battleStackWidth - qi_wei_zi.getWidth(), battleStackHeight * 5 / 16);
		ZhanQianren zhan_qianren = new ZhanQianren();
		zhan_qianren.init();
		zhan_qianren.setPosition(battleStackWidth - zhan_qianren.getWidth() * 1.5f, battleStackHeight * 3 / 16);
        ZhanTingyi zhan_tingyi = new ZhanTingyi();
        zhan_tingyi.init();
        zhan_tingyi.setPosition(zhan_tingyi.getWidth() / 2, battleStackHeight * 5 / 16);
        XuanyuanYin xuanyuan_yin = new XuanyuanYin();
		xuanyuan_yin.init();
		xuanyuan_yin.setPosition(xuanyuan_yin.getWidth() * 3 / 4, battleStackHeight * 3 / 16);
		Kui kui = new Kui();
        kui.init();
        kui.setPosition(0.0f, battleStackHeight * 1 / 16);
        characterGroup.addActor(zhan_tingyi);
		characterGroup.addActor(xuanyuan_yin);
        characterGroup.addActor(kui);
        characterGroup.addActor(qi_wei_zi);
        characterGroup.addActor(zhan_qianren);
		characterGroup.addActor(zhanTingyun);
        if (characterArray.isEmpty()) {
            for (int i = 0; i < 6; i ++) {//通常用于首次进入
                BaseEntity entity = (BaseEntity) characterGroup.getChild(i);
                Label.LabelStyle style = new Label.LabelStyle(FontManager.getFont(), Color.WHITE);
                Label label = new Label("", style);
                label.setPosition(entity.getX() + entity.getWidth()/ 2, entity.getY() + entity.getHeight() / 2);
                characterGroup.addActor(label);
                characterArray.add(entity);
                floatingLabelMap.put(entity, label);
            }
            playerArray.add(qi_wei_zi, zhan_qianren, zhanTingyun);
        }
        turnManager = new TurnManager(characterArray, playerArray, this);
        turnManager.init(actionManager, effectManager, floatingLabelMap);
        setCheckDialog();
        setEndedDialog();
    }
    

	public void show() {
		visible = true;
        placeEntity();
        turnManager.startBattle();
        showStateTable();
		Gdx.input.setInputProcessor(battleStage);
	}

	public class EffectActor extends Actor {
        public static EffectManager.EffectPool pool;
        private String id;
        private BaseEntity target;
        public BaseEntity aim;
		private float stateTime;
		private float x, y, width, height;
        private TextureRegion currentFrame;
        
        public EffectActor() {}
        
        public void init(String id, BaseEntity target,BaseEntity aim,  float x, float y, float width, float height) {
            this.id = id;
            this.target = target;
            this.aim = aim;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
            this.stateTime = 0.0f;
        }
        
        public void reset() {
            this.id = null;
        	this.target = this.aim = null;
			this.x = this.y = this.width = this.height = 0.0f;
            this.currentFrame = null;
        }

		@Override
		public void act(float delta) {
            super.act(delta);
			stateTime += delta;
            Animation<TextureRegion> animation = effectAnimationMap.get(id);
			currentFrame = animation.getKeyFrame(stateTime);
			if (animation.isAnimationFinished(stateTime)) {
                pendingRemoveEffects.add(this);
                target.fire(new EffectFinishEvent());
			}
		}

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.draw(currentFrame, x, y, width, height);
        }
	}
    
	private void createButtons() {
        buttonMask = new Image(drawable[2]);
		buttonMask.setColor(0, 0, 0, 0.4f);
        buttonTable.setBackground(drawable[1]);
		String[] temp = {"[GOLD]状态", "[BLACK]攻击", "[BLACK]技能", "[BLACK]道具", "[BLACK]逃离", "[BLACK]自动"};
		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
		style.font = FontManager.getFont();
		stringMap = new ObjectMap<TextButton, String>();
		barMap = new ObjectMap<TextButton, BottomBar>();
		BottomBar[] values = BottomBar.values();
		for (int i = 0; i < temp.length; i++) {
			buttons[i] = new TextButton(temp[i], style);
			stringMap.put(buttons[i], temp[i].replace("\\[([A-Z]+)\\]", ""));
			barMap.put(buttons[i], values[i]);
			buttonTable.left().bottom().add(buttons[i]).padTop(15f).padBottom(15f).expand().fill();
		}
		addButtonListener();
	}
	private void addButtonListener() {
		for (final TextButton t_b : buttons) {
			t_b.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					preprocess(t_b);
					return true;
				}

			});
		}

	}

	private void markupText(TextButton t_b) {
		for (TextButton t_b_1 : buttons) {
			String old_string = t_b_1.getText().toString();
			if (t_b_1 != t_b && old_string.contains("[GOLD]")) {
				String new_string = old_string.replace("[GOLD]", "[BLACK]");
				t_b_1.setText(new_string);
				t_b.setText(t_b.getText().toString().replace("[BLACK]", "[GOLD]"));
				break;
			}
		}

	}
    
    public void preprocess(TextButton button) {
    	markupText(button);
		activeBar = barMap.get(button);
		setActiveBar();
    }

	private void setActiveBar() {
		switch (activeBar) {
			case STATE :
				showStateTable();
				break;
			case ATTACK :
                checkDialog.show(battleStage);
                BaseEntity entity = turnManager.getActiveParticipant();
                BattleActionConfig config = BattleActionConfig.obtainConfigs(entity.getName())[0];
                enemyCheckBoxGroup.setMaxCheckCount(1);
                enemyCheckBoxGroup.setMaxCheckCount(1);
                Label tip = (Label) checkDialog.getContentTable().getChild(0);
                tip.setText("选择一个目标");
                enemyCheckBoxGroup.getButtons().get(0).setChecked(true);
                ResultParameter parameter = new ResultParameter(entity, BaseEntity.BattleState.ATTACK, config, false);
                checkDialog.setObject(checkDialog.getButtonTable().getChild(0), parameter);
				break;
			case SKILL :
				showSkillList();
				break;
			case ITEM :
                //endedDialog.show(battleStage);
				break;
			case ESCAPE :
                zhanTingyun.recoverPosition();
				hide();
				break;
			case AUTO :
				break;
		}

	}
    
    private class ResultParameter {
        public BaseEntity entity;
        public BaseEntity.BattleState state;
        public BattleActionConfig config;
        public boolean visible;
        
        public ResultParameter(BaseEntity entity, BaseEntity.BattleState state, BattleActionConfig config, boolean visible) {
            this.entity = entity;
            this.state = state;
            this.config = config;
            this.visible = visible;
        }
        
        
    }

    public void enableConfig(BaseEntity entity, BattleActionConfig config, BaseEntity.BattleState state, boolean visiable) {
        entity.setBattleState(state);
        dialog.setVisible(visiable);
        disableLowerPart();
        entity.setActionConfig(config);
        if (entity.getBattleState() == BaseEntity.BattleState.SKILL) {
            if(messageStyle.font.getCache() != FontManager.getFont().getCache()) {
            	messageStyle.font = FontManager.getFont();
            }
            twin.setText(config.getActionName(), true);
        }
    }

	private void disableLowerPart() {
		dialog.setModal(true);
		buttonStack.add(buttonMask);
	}

	public void recoverLowerPart() {
		dialog.setModal(false);
		buttonStack.removeActor(buttonMask);
	}
    
    public void clearTwinText() {
    	twin.setText("");
    }
    
    public void enableDialog(boolean enable) {
    	dialog.setVisible(enable);
    }
    
    public void enablePlayerControl(BaseEntity entity) {
            String cn_name = BattleActionConfig.getCarrierData(entity.getName()).getName();
            
            twin.setText(cn_name + "行动中", true);
            recoverLowerPart();
    }

	private void showSkillList() {
        if (skillTables == null) {
            skillTables = new Table[playerArray.size];
            createSkillGroup();
        }
        for(BaseEntity e : playerArray) {
            Table t = playerSkillTableMap.get(e);
        	if(turnManager.getActiveParticipant() == e) {
        		t.setVisible(true);
                break;
        	} else {
                t.setVisible(false);
            }
        }
        stateTable.setVisible(false);
	}
    
    

    private void createSkillGroup() {
        playerSkillTableMap = new ObjectMap<BaseEntity, Table>();
        TextureRegion[][] split = TextureCache.getSplit("battle/state/skill_icon", 6, 1);
        Drawable check =new TextureRegionDrawable(new Texture(Gdx.files.internal("battle/state/systemcursor.png")));
        Label.LabelStyle label_style = new Label.LabelStyle();
        label_style.font = FontManager.getFont();
        
        for (int i = 0; i < playerArray.size; i ++) {
            BaseEntity entity = playerArray.get(i);
            BattleActionConfig[] configs = BattleActionConfig.obtainConfigs(entity.getName());

            ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle();
            style.checked = check;
            style.font = FontManager.getFont();
            Table skill_table = new Table();
            skillTables[i] = skill_table;
            ButtonGroup<ImageTextButton> skill_group = new ButtonGroup<ImageTextButton>();
            skill_group.setMinCheckCount(0);
            
            for (BattleActionConfig config : configs) {
                if (config.getMpCost() == 0) {
                    continue;
                }

                int icon_index = getSkillIconIndex(config);
                Drawable skill_icon_d = new TextureRegionDrawable(split[0][icon_index]);
                skill_icon_d.setMinSize(13 * Constants.WIDTH_RATIO, 13 * Constants.HEIGHT_RATIO);
                Image skill_icon = new Image(skill_icon_d);
                final ImageTextButton skill_button = new ImageTextButton(config.getActionName(), style);

                skill_button.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (skill_button.isChecked()) {
                            if(actionManager.isAll(config)) {
                            	enemyCheckBoxGroup.setMinCheckCount(3);
                                enemyCheckBoxGroup.setMaxCheckCount(3);
                                Label tip = (Label) checkDialog.getContentTable().getChild(0);
                                tip.setText("此技能默认全选");
                                for(CheckBox box : enemyCheckBoxGroup.getButtons()) {
                                	box.setChecked(true);
                                }
                            } else {
                                enemyCheckBoxGroup.setMinCheckCount(1);
                                enemyCheckBoxGroup.setMaxCheckCount(1);
                                Label tip = (Label) checkDialog.getContentTable().getChild(0);
                                tip.setText("选择一个目标");
                                enemyCheckBoxGroup.getButtons().get(0).setChecked(true);
                            }
                            ResultParameter parameter = new ResultParameter(entity, BaseEntity.BattleState.SKILL, config, true);
                            checkDialog.setObject(checkDialog.getButtonTable().getChild(0), parameter);
                            skill_button.setChecked(false);
                            checkDialog.show(battleStage);
                        } else {
                            String temp = config.getTips();
                            
                            twin.setIsPaused();
                            twin.setText(temp, false);
                        }

                        return true;
                    }
                });
                
                skill_group.add(skill_button);
                Label label = new Label("[BLACK]气：[BLUE]" + config.getMpCost(), label_style);
                label.setFontScale(0.5f);

                HorizontalGroup hor_group = new HorizontalGroup();
                hor_group.addActor(skill_icon);
                hor_group.addActor(skill_button);
                hor_group.addActor(label);
                SnapshotArray children = skillTables[i].getChildren();
                if (children != null && children.size % 2 == 1) {
                	skillTables[i].left().top().add(hor_group).expand().row();
                } else {
                    skillTables[i].left().top().add(hor_group).expand();
                }
                skillTables[i].setVisible(false);
            }
            skill_group.setMinCheckCount(0);
            playerSkillTableMap.put(entity, skillTables[i]);
            bottomStack.add(skillTables[i]);
        }
    }

    private int getSkillIconIndex(BattleActionConfig config) {
        int index = 0;
        BattleActionConfig.AimType aim_type = config.getAimType();
        switch (config.getActionType()) {
            case DAMAGE:
                switch (aim_type) {
                    case ENEMY_SINGLE:
                    case FRIEND_SINGLE:
                        index = 0;
                        break;
                    case ENEMY_ALL:
                    case FRIEND_ALL:
                        index = 3;
                        break;
                }
                break;
            case BUFF:
                switch (aim_type) {
                    case ENEMY_SINGLE:
                    case FRIEND_SINGLE:
                        index = 1;
                        break;
                    case ENEMY_ALL:
                    case FRIEND_ALL:
                        index = 4;
                        break;
                }
                break;
            case HEAL:
                switch (aim_type) {
                    case ENEMY_SINGLE:
                    case FRIEND_SINGLE:
                        index = 2;
                        break;
                    case ENEMY_ALL:
                    case FRIEND_ALL:
                        index = 5;
                        break;
                }
                break;
            default:
                throw new IllegalArgumentException("actionType没有值对应，json中可能未声明");
        }
        return index;
    }

     public boolean ena = false;

    public void render() {
        mark = "";
        //辅助特效事件，统一添加与移除特效
        if (turnManager != null) {
            if (turnManager.isEventRound()) {
                for (int i = 0; i < pendingAddEffects.size; i++) {
                    EffectActor effect_actor = pendingAddEffects.get(i);
                    characterGroup.addActorAfter(effect_actor.aim, effect_actor);
                }
            } else {
                for (int i = 0; i < pendingRemoveEffects.size; i++) {
                    EffectActor effect = pendingRemoveEffects.get(i);
                    characterGroup.removeActor(effect);
                    EffectActor.pool.free(effect);
                }
                pendingRemoveEffects.clear();
            }
            if(turnManager.isBattleEnded()) {
                endedDialog.show(battleStage);
                turnManager.resetBattleEnded();
            }
        }
        
        
		battleStage.act();
		battleStage.draw();
	}
    

	private class OffsetKnobDrawable extends TextureRegionDrawable {
		public OffsetKnobDrawable(TextureRegion region) {
			super(region);
		}

		@Override
		public void draw(Batch batch, float x, float y, float width, float height) {
            float offset_x =  x + Constants.WIDTH_RATIO;
            float temp = width - 2 * Constants.WIDTH_RATIO;
            float offset_width = temp >= 0 ? temp : 0;
			super.draw(batch, offset_x, y, offset_width, height);
		}
	}

	public void showStateTable() {
		if (characters == null) {
            createStateTable();
		}
        stateTable.setVisible(true);
        if (skillTables != null) {
            for(Table t : skillTables) {
            	t.setVisible(false);
            }
        }
        //Gdx.app.error("Table大小为：", "宽：" + table.getWidth() + " 高：" + table.getHeight());
        
	}
    
    private void createStateTable() {
    	stateTable = new Table();
        avatarMap = new ObjectMap<BaseEntity, Image>();
		TextureAtlas texture_atlas = new TextureAtlas("battle/state/packer-5.atlas");
        characters = new Table[3];
        hpProgressTables = new StateProgressTable[3];
        mpProgressTables = new StateProgressTable[3];
        String[] temp = {"1001006", "1001003", "1001002"};
        avatarDrawables = new TextureRegionDrawable[temp.length][7];
        for(int i = 0; i < temp.length; i ++) {
        	characters[i] = new Table();
            
            avatarStack = new Stack();
            TextureAtlas atlas = new TextureAtlas("battle/state/face" + temp[i] + ".atlas");
            String[] temp_1 = {"0_await","1_skill","2_weak","3_defeated"};
            for(int j = 0; j < temp_1.length; j ++) {
                int k = j * 2;
            	Array<TextureAtlas.AtlasRegion> atlas_regions = atlas.findRegions(temp_1[j]);
                for(TextureAtlas.AtlasRegion region : atlas_regions) {
                	avatarDrawables[i][k] = new TextureRegionDrawable(region);
                    k ++;
                }
            }
            BaseEntity entity = playerArray.get(i);
            Image avatar = new Image();
            avatarMap.put(entity, avatar);
            avatarStack.add(avatar);
            setAvatarDrawable(entity, avatar, i);
            
            hpProgressTables[i] = setHpProgress(texture_atlas, playerArray.get(i).getMaxHp());
            mpProgressTables[i] = setMpProgress(texture_atlas, playerArray.get(i).getMaxMp());
		    characters[i].left().bottom().add(avatarStack).padTop(9.0f).height(Constants.HEIGHT_RATIO * 29.0f).fill().row();
		    characters[i].left().bottom().add(hpProgressTables[i]).expand().fill().row();
		    characters[i].left().bottom().add(mpProgressTables[i]).expand().fill();
        }
        stateTable.left().bottom().add(characters[0]).padBottom(9.0f).padLeft(15.71f).width(Constants.WIDTH_RATIO * 54.0f).expandY().fill();
	    stateTable.left().bottom().add(characters[1]).padBottom(9.0f).padLeft(15.71f).width(Constants.WIDTH_RATIO * 54.0f).expandY().fill();
	    stateTable.left().bottom().add(characters[2]).padBottom(9.0f).padLeft(15.71f).width(Constants.WIDTH_RATIO * 54.0f).padRight(15.71f).expandY().fill();
        bottomStack.add(stateTable);
    }
    
    public void setStateImage() {
        int i = -1;
    	for(BaseEntity entity : playerArray) {
    		Image avatar = avatarMap.get(entity);
            i ++;
            setAvatarDrawable(entity, avatar, i);
    	}
    }
    
    private void setAvatarDrawable(BaseEntity entity, Image avatar, int i) {
        boolean turn = entity == turnManager.getActiveParticipant();
    	switch (entity.getBattleState()) {
            case AWAIT:
                if (turn) {
                    avatar.setDrawable(avatarDrawables[i][1]);
                } else {
                    avatar.setDrawable(avatarDrawables[i][0]);
                }
                break;
            case SKILL:
                if (turn) {
                    avatar.setDrawable(avatarDrawables[i][3]);
                } else {
                    avatar.setDrawable(avatarDrawables[i][2]);
                }
                break;
            case WEAK:
                if (turn) {
                    avatar.setDrawable(avatarDrawables[i][5]);
                } else {
                    avatar.setDrawable(avatarDrawables[i][4]);
                }
                break;
            case DEFEATED:
                avatar.setDrawable(avatarDrawables[i][6]);
                break;
        }
    }
    
    private StateProgressTable setHpProgress(TextureAtlas atlas, float max) {
    	ProgressBar.ProgressBarStyle p_style = new ProgressBar.ProgressBarStyle();
		p_style.background = new TextureRegionDrawable(atlas.findRegion("bar", 1));
		p_style.background.setMinSize(243.0f, 22.4f);
        //ProgressBar的konbBefore konb knobAfter (假设从左向右填充)
        //以可供拖动的knob为分界(可选)，之前为已完成填充，之后为未完成填充(可选)
		p_style.knobBefore = new OffsetKnobDrawable(atlas.findRegion("bar_step", 1));
		p_style.knobBefore.setMinSize(13.5f, 13.5f);
        Label.LabelStyle l_style = new Label.LabelStyle();
		l_style.font = FontManager.getFont();
        StateProgressTable state_progress = new StateProgressTable(0, max, 1, false, p_style, l_style);
        state_progress.progressLabel.setFontScaleY(0.5f);
        return state_progress;
    }
    
    private StateProgressTable setMpProgress(TextureAtlas atlas, float max) {
    	ProgressBar.ProgressBarStyle p_style = new ProgressBar.ProgressBarStyle();
		p_style.background = new TextureRegionDrawable(atlas.findRegion("bar", 2));
		p_style.background.setMinSize(243.0f, 22.4f);
		p_style.knobBefore = new OffsetKnobDrawable(atlas.findRegion("bar_step", 2));
		p_style.knobBefore.setMinSize(13.5f, 13.5f);
        Label.LabelStyle l_style = new Label.LabelStyle();
		l_style.font = FontManager.getFont();
        StateProgressTable state_progress = new StateProgressTable(0, max, 1, false, p_style, l_style);
        state_progress.progressLabel.setFontScaleY(0.5f);
        return state_progress;
    }

	public void hide() {
		visible = false;
		Gdx.input.setInputProcessor(game.getTransStage());
	}

	private void setDialog() {
		Drawable d = new TextureRegionDrawable(new Texture(Gdx.files.internal("battle/state/message_bg.png")));
		d.setMinSize(Constants.VISION_WIDTH, 23.0f * Constants.HEIGHT_RATIO);
		Dialog.WindowStyle style = new Dialog.WindowStyle(TitleScreen.font, Color.BLACK, d);
		dialog = new Dialog("", style);
		messageStyle = new Label.LabelStyle();
		messageStyle.font = FontManager.getFont();

		twin = new TwinLabelMarquee(messageStyle, "");
		twin.setSize(750, 103.5f);
		container = new Container<TwinLabelMarquee>(twin);
		container.setSize(750, 103.5f);
		container.setClip(true);
		container.center();

		dialog.getContentTable().add(container).padLeft(20).padRight(20);
		dialog.setModal(false);

		dialog.pack();
		dialog.setPosition(Constants.LEFT_SIDE_X, Constants.TOP_Y - dialog.getHeight());
		dialog.show(battleStage, Actions.fadeIn(0.3f));
	}
    
    private void setCheckDialog() {
    	Drawable d = new TextureRegionDrawable(new Texture(Gdx.files.internal("battle/state/select_bg.png")));
		d.setMinSize(Constants.WIDTH_RATIO * 99.0f, Constants.HEIGHT_RATIO * 81.5f);
        TextureRegion[][] split = TextureCache.getSplit("battle/state/check_icon", 2, 1);
        checkEntityMap = new ObjectMap<CheckBox, BaseEntity>();
        Dialog.WindowStyle style = new Dialog.WindowStyle(TitleScreen.font, Color.BLACK, d);
        checkDialog = new Dialog("", style) {
            @Override
            protected void result(Object o) {
                if (o instanceof ResultParameter) {
                	ResultParameter rst = (ResultParameter) o;
                    actionManager.checkAim(enemyCheckBoxGroup.getAllChecked());
                    enemyCheckBoxGroup.uncheckAll();
                    enableConfig(rst.entity, rst.config, rst.state, rst.visible);
                } else if (o instanceof Boolean) {
                    boolean rst = (boolean) o;
                    if (rst == false) {
                        BaseEntity entity = turnManager.getActiveParticipant();
                        String cn_name = BattleActionConfig.getCarrierData(entity.getName()).getName();
                    	twin.setText(cn_name + "行动中", true);
                    }
                }
            }
        };
        messageStyle.font = FontManager.getFont();
        Label tip = new Label("", messageStyle);
        Drawable check_box_off = new TextureRegionDrawable(split[0][0]);
        check_box_off.setMinSize(13.0f * Constants.WIDTH_RATIO, 13.0f * Constants.HEIGHT_RATIO);
        Drawable check_box_on = new TextureRegionDrawable(split[0][1]);
        check_box_on.setMinSize(13.0f * Constants.WIDTH_RATIO, 13.0f * Constants.HEIGHT_RATIO);
        CheckBox.CheckBoxStyle check_box_style = new CheckBox.CheckBoxStyle();
        check_box_style.checkboxOff = check_box_off;
        check_box_style.checkboxOn = check_box_on;
		check_box_style.font = FontManager.getFont();
        check_box_style.fontColor = Color.BLACK;
        //ButtonGroup为逻辑控件，不负责视觉显示
        playerCheckBoxGroup = new ButtonGroup<CheckBox>();
        VerticalGroup player_vertical_group = new VerticalGroup();
        enemyCheckBoxGroup = new ButtonGroup<CheckBox>();
        VerticalGroup enemy_vertical_group = new VerticalGroup();
        for (BaseEntity entity : characterArray) {
            String cn_name = BattleActionConfig.getCarrierData(entity.getName()).getName();
            CheckBox check_box = new CheckBox(cn_name, check_box_style);
            if (playerArray.contains(entity, true)) {
                playerCheckBoxGroup.add(check_box);
                player_vertical_group.addActor(check_box);
                continue;
            }
            enemyCheckBoxGroup.add(check_box);
            enemy_vertical_group.addActor(check_box);
            checkEntityMap.put(check_box, entity);
        }

        player_vertical_group.columnLeft();
        enemy_vertical_group.columnLeft();
        TextButton.TextButtonStyle text_button_style = new TextButton.TextButtonStyle();
        text_button_style.font = FontManager.getFont();
        text_button_style.fontColor = Color.BLACK;
        TextButton confirm = new TextButton("确定", text_button_style);
        TextButton cancel = new TextButton("取消", text_button_style);
        
        Table content_table = checkDialog.getContentTable();
        content_table.left().top().add(tip).padLeft(6.0f * Constants.WIDTH_RATIO).padTop(5.0f * Constants.HEIGHT_RATIO).expandX().fill().row();
        content_table.left().top().add(enemy_vertical_group).expand().fill();
        Table button_table = checkDialog.getButtonTable();
        button_table.add(confirm).expand().fill();
        button_table.add(cancel).expand().fill();
        checkDialog.setObject(cancel, false);
        button_table.padBottom(5.0f * Constants.HEIGHT_RATIO);
        
    }
    
    private void setEndedDialog() {
    	Drawable d = new TextureRegionDrawable(new Texture(Gdx.files.internal("battle/state/info_ended.png")));
		d.setMinSize(Constants.WIDTH_RATIO * 130.0f, Constants.HEIGHT_RATIO * 28.0f);
        Dialog.WindowStyle style = new Dialog.WindowStyle(TitleScreen.font, Color.BLACK, d);;
        endedDialog = new Dialog("", style);
        Label tip = new Label("战斗结束", messageStyle);
        endedDialog.getContentTable().add(tip);
        endedDialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                zhanTingyun.recoverPosition();
                endedDialog.hide(Actions.fadeOut(0.2f));
                hide();
            }
            
        });
        endedDialog.pack();
    }

	public boolean isVisible() {
		return visible;
	}
    
    public Table getButtonTable() {
    	return buttonTable;
    }
    
    public ObjectMap<CheckBox, BaseEntity> getCheckEntityMap() {
        return checkEntityMap;
    }
    public Array<BaseEntity> getPlayerArray() {
    	return playerArray;
    }
}


