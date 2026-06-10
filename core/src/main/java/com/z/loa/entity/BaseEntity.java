package com.z.loa.entity;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.z.loa.entity.config.BattleActionConfig;
import com.z.loa.entity.event.battle.EffectFinishEvent;
import com.z.loa.entity.event.battle.EffectTriggerEvent;
import com.z.loa.screen.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;
import com.z.loa.manager.*;

public class BaseEntity extends Actor {
    protected TextureRegion currentFrame;
    protected TextureRegion currentMoodFrame;

    protected EntityState entityState;
    protected BattleState battleState;
    protected Orientation orientation;

    protected float stateTime;
    protected float elapsedTime;
    protected float moodTime;
    protected String name;
    protected int maxHp;
    protected int maxMp;
    protected int remainHp;
    protected int remainMp;

    protected Animation<TextureRegion>[] moveAnimation;
    protected Animation<TextureRegion>[] battleAnimation;
    protected Animation<TextureRegion> moodAnimation;
    protected TextureRegion[] idleTextureArray;

    protected BattleActionConfig actionConfig;
    protected boolean eventFire;
    private Runnable EffectTime;
    private ShaderProgram shader;
    private boolean enabled;
    private float flashDuration;
    private float flashCount;
    private Vector3 flashColor;

    protected enum EntityState {
        IDLE,
        BATTLE,
        MOVE;
    }

    public enum BattleState {
        AWAIT,
        ATTACK,
        SKILL,
        DEFEND,
        WEAK,
        DEFEATED,
        WON;
    }

    protected enum Orientation {
        DOWN,
        UP,
        LEFT,
        RIGHT;
    }

    public BaseEntity() {
        String vertex = Gdx.files.internal("shaders/hit_flash.vert").readString();
        String fragment = Gdx.files.internal("shaders/hit_flash.frag").readString();

        shader = new ShaderProgram(vertex, fragment);
        if (!shader.isCompiled()) {
            Gdx.app.error("Shader", shader.getLog());
        }
        this.entityState = EntityState.BATTLE;
        this.battleState = BattleState.AWAIT;
        moveAnimation = new Animation[4];
    }

    public void setBattleState(BattleState state) {
        setStateTime(0);
        this.battleState = state;
    }

    public BattleState getBattleState() {
        return battleState;
    }
    
    public String getName() {
    	return name;
    }

    public void setMaxHp(int hp) {
        this.maxHp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxMp(int mp) {
        this.maxMp = mp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public void setRemainHp(int hp) {
        this.remainHp = hp;
    }
    
    public void decreaseRemainHp(int value) {
    	this.remainHp -= value;
        if(remainHp < 0) {
        	remainHp = 0;
        }
    }
    
    public void increaseRemainHp(int value) {
    	this.remainHp += value;
        if(remainHp > maxHp) {
        	remainHp = maxHp;
        }
    }

    public int getRemainHp() {
        return remainHp;
    }

    public void setRemainMp(int mp) {
        this.remainMp = mp;
    }
    
    public void decreaseRemainMp(int value) {
    	this.remainMp -= value;
        if(remainMp < 0) {
        	remainMp = 0;
        }
    }
    
    public void increaseRemainMp(int value) {
    	this.remainMp += value;
        if(remainMp > maxMp) {
        	remainMp = maxMp;
        }
    }

    public int getRemainMp() {
        return remainMp;
    }

    public void setStateTime(float time) {
        this.stateTime = time;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void postEffect(Runnable runnable) {
        this.EffectTime = runnable;
    }

    public BattleActionConfig getActionConfig() {
        return this.actionConfig;
    }

    public void setActionConfig(BattleActionConfig config) {
        this.actionConfig = config;
    }
    
    public boolean isEventFire() {
        return this.eventFire;
    }

    public void resetEventFire() {
        this.eventFire = false;
    }

    @Override
    public void act(float delta) {
        //为Actor附加Action需调用父类act方法更新
        super.act(delta);
        stateTime += delta;
        if (enabled) {
            elapsedTime += delta;
            if (elapsedTime >= flashDuration) {
                elapsedTime = flashDuration;
                enabled = false;
            }
        }
        matchAnimation();
    }

    public void hitFlash(int count, float duration, Vector3 rgb) {
        this.flashCount = count;
        this.flashDuration = duration;
        this.flashColor = rgb;
        elapsedTime = 0;
        enabled = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (enabled) {
            ShaderProgram original_shader = batch.getShader();
            batch.setShader(shader);
            float progress = enabled ? (elapsedTime / flashDuration) : 0f;
            shader.setUniformf("u_progress", progress);
            shader.setUniformf("u_flashCount", flashCount);
            shader.setUniformf("u_rgb", flashColor);
            batch.draw(currentFrame, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            batch.setShader(original_shader);
        } else {
            batch.draw(currentFrame, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    protected void matchAnimation() {
        switch (entityState) {
            case IDLE:
                switch (orientation) {
                    case DOWN:
                        currentFrame = idleTextureArray[0];
                        break;
                    case UP:
                        currentFrame = idleTextureArray[1];
                        break;
                    case LEFT:
                        currentFrame = idleTextureArray[2];
                        break;
                    case RIGHT:
                        currentFrame = idleTextureArray[3];
                        break;
                    default:
                        currentFrame = idleTextureArray[0];
                }
                break;
            case BATTLE:
                matchBattleAnimation();
                break;
            case MOVE:
                GameScreen.onMoved();
                switch (orientation) {
                    case DOWN:
                        currentFrame = moveAnimation[0].getKeyFrame(stateTime);
                        break;
                    case UP:
                        currentFrame = moveAnimation[1].getKeyFrame(stateTime);
                        break;
                    case LEFT:
                        currentFrame = moveAnimation[2].getKeyFrame(stateTime);
                        break;
                    case RIGHT:
                        currentFrame = moveAnimation[3].getKeyFrame(stateTime);
                        break;
                    default:
                        currentFrame = moveAnimation[0].getKeyFrame(stateTime);
                }
                break;
        }
    }

    protected void matchBattleAnimation() {
        switch (battleState) {
            case AWAIT:
                currentFrame = battleAnimation[0].getKeyFrame(stateTime);
                break;
            case ATTACK:
                Animation<TextureRegion> animation = battleAnimation[1];
                int index = animation.getKeyFrameIndex(stateTime);
                if (actionConfig != null) {
                    if (index == actionConfig.getEffectTriggerIndex() && !eventFire) {
                        this.fire(new EffectTriggerEvent(actionConfig));
                        eventFire = true;
                    }
                }
                if (animation.isAnimationFinished(stateTime)) {
                    battleState = BattleState.AWAIT;
                }
                currentFrame = animation.getKeyFrame(stateTime);

                break;
            case SKILL:
                Animation<TextureRegion> animation_1 = battleAnimation[2];
                int index_1 = animation_1.getKeyFrameIndex(stateTime);
                if (actionConfig != null) {
                    if (index_1 == actionConfig.getEffectTriggerIndex() && !eventFire) {
                        this.fire(new EffectTriggerEvent(actionConfig));
                        eventFire = true;
                    }
                }
                currentFrame = battleAnimation[2].getKeyFrame(stateTime);

                break;
            case DEFEND:
                currentFrame = battleAnimation[3].getKeyFrame(stateTime);

                break;
            case WEAK:
                currentFrame = battleAnimation[4].getKeyFrame(stateTime);

                break;
            case DEFEATED:
                currentFrame = battleAnimation[5].getKeyFrame(stateTime);

                break;
            case WON:
                currentFrame = battleAnimation[6].getKeyFrame(stateTime);

                break;
        }
    }
}
