package com.z.loa.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.z.loa.entity.BaseEntity;
import com.z.loa.entity.config.BattleActionConfig;
import com.z.loa.entity.enemy.XuanyuanYin;
import com.z.loa.screen.BattleScene;

public class BattleActionManager {
    private Array<BaseEntity> entities;
    private BattleScene scene;
    private Array<BaseEntity> aims;
    
    public BattleActionManager(Array<BaseEntity> entities, BattleScene scene) {
        this.entities = entities;
        this.scene = scene;
        this.aims = new Array<BaseEntity>();
    }
    
    public Array<BaseEntity> getAims() {
    	return aims;
    }

    @Deprecated
    public Array<BaseEntity> selectAim(BaseEntity target, BattleActionConfig config) {
        Array<BaseEntity> aims = new Array<>();
        BattleActionConfig.AimType type = config.getAimType();
        for (BaseEntity entity : entities) {
            switch (type) {
                case FRIEND_SINGLE:
                    break;
                case FRIEND_ALL:
                    break;
                case ENEMY_SINGLE:
                    if (entity instanceof XuanyuanYin) {
                        aims.add(entity);
                    }
                    break;
                case ENEMY_ALL:
                    if (entity instanceof XuanyuanYin) {
                        aims.add(entity);
                    }
                    break;
                default:
                    break;
            }
        }
        return aims;
    }

    public boolean isAll(BattleActionConfig config) {
        BattleActionConfig.AimType type = config.getAimType();
        switch (type) {
            case ENEMY_SINGLE:
            case FRIEND_SINGLE:
                return false;
            case ENEMY_ALL:
            case FRIEND_ALL:
                return true;
            default:
                throw new IllegalArgumentException("config的aimType参数异常");
        }
    }
    
    //通常用于玩家选择目标
    public Array<BaseEntity> checkAim(Array<CheckBox> boxes) {
        aims.clear();
        for(CheckBox box : boxes) {
        	aims.add(scene.getCheckEntityMap().get(box));
        }
    	return aims;
    }
    
    //通常用于敌方选择
    public Array<BaseEntity> selectAim(BattleActionConfig config, Array<BaseEntity> players) {
        aims.clear();
        BattleActionConfig.AimType type = config.getAimType();
        switch (type) {
            case ENEMY_SINGLE:
            case FRIEND_SINGLE:
                aims.add(players.get(MathUtils.random(0, players.size - 1)));
                break;
            case ENEMY_ALL:
            case FRIEND_ALL:
                for(BaseEntity player_1 : players) {
                	if(player_1.getBattleState() != BaseEntity.BattleState.DEFEATED) {
                		aims.add(player_1);
                	}
                }
                return null;
            default:
                throw new IllegalArgumentException("config的aimType参数异常");
        }
        
    	return aims;
    }
    
    @Deprecated
    public void triggerAction(BaseEntity target, BattleActionConfig config) {
    	target.setBattleState(BaseEntity.BattleState.values()[config.getStateIndex()]);
        target.setStateTime(0);
    }
    
    
    
}
