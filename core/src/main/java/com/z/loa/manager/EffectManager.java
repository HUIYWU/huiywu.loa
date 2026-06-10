package com.z.loa.manager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Timer;
import com.z.loa.entity.BaseEntity;
import com.z.loa.entity.config.BattleActionConfig;
import com.z.loa.screen.BattleScene;

public class EffectManager {
    private BattleScene battleScene;
    private Group characterGroup;
    private Array<BattleScene.EffectActor> pendingAddEffects;
    private EffectPool pool;
    
    public EffectManager(BattleScene scene, Group character, Array<BattleScene.EffectActor> pending_add) {
        this.battleScene = scene;
        this.characterGroup = character;
        this.pendingAddEffects = pending_add;
        this.pool = BattleScene.EffectActor.pool = new EffectPool();
    }
    
    /**
     * @param target  特效事件源对象，在特殊分支用于定位z层级
     */
    public void postEffect(BattleActionConfig config, BaseEntity target, Array<BaseEntity> aims) {
    	String id = config.getEffectId();
        pendingAddEffects.clear();
        for (BaseEntity aim : aims) {
            float[] size = config.getSize();
            float width = size[0];
            float height = size[1];
            float x, y;
            if (config.isSpecialPosition()) {
                float[] position = config.getPosition();
                x = position[0];
                y = position[1];
                BattleScene.EffectActor effect_actor = pool.obtainEffect(id, target, target, x, y, width, height);
                pendingAddEffects.add(effect_actor);
                for(BaseEntity aim_1 : aims) {
                	aim_1.setBattleState(BaseEntity.BattleState.DEFEND);
                }
                break;
            } else {
                x = aim.getX() + (aim.getWidth() - width) / 2;
                y = aim.getY();
            }
            
            BattleScene.EffectActor effect_actor = pool.obtainEffect(id, target, aim, x, y, width, height);
            pendingAddEffects.add(effect_actor);
            aim.setBattleState(BaseEntity.BattleState.DEFEND);
        }
    }
    /**
     * @param objectives 闪烁对象Array, 不一定包含事件发起对象
     */
    public void postFlash(BattleActionConfig config, Array<BaseEntity> objectives) {
        for (BaseEntity objective : objectives) {
            int count = config.getFlashCount();
            float duration = count * 0.2f;
            Vector3 rgb = config.getFlashRgb();
            float delay_time = config.getFlashDelayTime();
            if (delay_time == 0) {
                objective.hitFlash(count, duration, rgb);
                return;
            }
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    objective.hitFlash(count, duration, rgb);
                    if (!battleScene.getPlayerArray().contains(objective, true)) {
                    	objective.addAction(Actions.repeat(count, Actions.sequence(Actions.moveBy(15, 0, 0.15f, Interpolation.sine), Actions.moveBy(-15, 0, 0.05f, Interpolation.sine))));
                    }
                }
            },delay_time);
        }
    }

    public class EffectPool extends Pool<BattleScene.EffectActor> {
        @Override
        protected BattleScene.EffectActor newObject() {
            return battleScene.new EffectActor();
        }

        @Override
        protected void reset(BattleScene.EffectActor effect) {
            super.reset(effect);
            effect.reset();
        }
        
        public BattleScene.EffectActor obtainEffect(String id, BaseEntity target, BaseEntity aim, float x, float y, float width, float height) {
            BattleScene.EffectActor effect = super.obtain();
            effect.init(id, target, aim, x, y, width, height);
        	return effect;
        }
        
    }
}
