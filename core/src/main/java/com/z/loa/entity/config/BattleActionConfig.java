package com.z.loa.entity.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.z.loa.Constants;
import com.z.loa.data.ActionCarrier;
import com.z.loa.data.ActionData;

public class BattleActionConfig {
    private static ActionData data;
    private static ObjectMap<String, BattleActionConfig[]> configsMap;
    private String effectId;
    private String actionName;
    private String tips;
    private int effectTriggerIndex;
    private int stateIndex;
    private AimType aimType;
    private ActionType actionType;
    private boolean specialPosition;
    private float[] position;
    private float[] size;
    private boolean flashFollow;
    private float flashDelayTime;
    private int flashCount;
    private float flashDuration;
    private Vector3 flashRgb;
    private int mpCost;

    public enum AimType {
        FRIEND_SINGLE,
        FRIEND_ALL,
        ENEMY_SINGLE,
        ENEMY_ALL;
    }

    public enum ActionType {
        DAMAGE,
        BUFF,
        HEAL;
    }
    
    static {
        configsMap = new ObjectMap<String, BattleActionConfig[]>();
    }

    private BattleActionConfig() {}

    public static void loadConfig() {
        FileHandle file = Gdx.files.internal("data/action/action.json");
        Json json = new Json();
        data = json.fromJson(ActionData.class, file);
    }

    public static BattleActionConfig[] obtainConfigs(String name) {
        if(configsMap.containsKey(name)) {
        	return configsMap.get(name);
        }
        
        ActionCarrier carrier = getCarrierData(name);
        Array<String> actions = carrier.getActionArray();
        BattleActionConfig[] configs = new BattleActionConfig[actions.size];
        for (int i = 0; i < configs.length; i++) {
            String id = actions.get(i);
            BattleActionConfig config = new BattleActionConfig();
            config.setParameter(config, id);
            configs[i] = config;
        }
        configsMap.put(name, configs);
        return configs;
    }
    
    public static ActionCarrier getCarrierData(String id) {
    	if(data.players.containsKey(id)) {
    		return data.players.get(id);
    	} else if (data.enemies.containsKey(id)){
    		return data.enemies.get(id);
    	}
        return null;
    }

    private void setParameter(BattleActionConfig config, String id) {
        ActionData.Action action = data.actions.get(id);
        config.setEffectId(id);
        config.setActionName(action.actionName);
        config.setTips(action.tips);
        config.setEffectTriggerIndex(action.effectTriggerIndex);
        config.setStateIndex(action.stateIndex);
        config.setAimType(AimType.valueOf(action.aimType));
        config.setActionType(ActionType.valueOf(action.actionType));
        config.setSpecialPosition(action.specialPosition);
        config.setPosition(action.position);
        config.setSize(action.size);
        config.setFlashDelayTime(action.flashDelayTime);
        config.setFlashCount(action.flashCount);
        config.setFlashRgb(action.flashRgb);
        config.setMpCost(action.mpCost);
    }

    public String getEffectId() {
        return this.effectId;
    }

    public void setEffectId(String id) {
        this.effectId = id;
    }

    public String getActionName() {
        return this.actionName;
    }

    public void setActionName(String name) {
        this.actionName = name;
    }

    public String getTips() {
        return this.tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getEffectTriggerIndex() {
        return this.effectTriggerIndex;
    }

    public void setEffectTriggerIndex(int index) {
        this.effectTriggerIndex = index;
    }

    public int getStateIndex() {
        return this.stateIndex;
    }

    public void setStateIndex(int index) {
        this.stateIndex = index;
    }

    public AimType getAimType() {
        return this.aimType;
    }

    public void setAimType(AimType type) {
        this.aimType = type;
    }

    public boolean isSpecialPosition() {
        return this.specialPosition;
    }

    public void setSpecialPosition(boolean special) {
        this.specialPosition = special;
    }

    public float[] getSize() {
        return this.size;
    }

    public void setSize(float width, float height) {
        if (size == null) {
            size = new float[2];
        }
        size[0] = width;
        size[1] = height;
    }

    public void setSize(float[] size) {
        size[0] *= Constants.WIDTH_RATIO;
        size[1] *= Constants.HEIGHT_RATIO;
        this.size = size;
    }

    public boolean isFlashFollow() {
        return this.flashFollow;
    }

    public void setFlashFollow(boolean follow) {
        this.flashFollow = follow;
    }

    public float[] getPosition() {
        return this.position;
    }

    public void setPosition(float x, float y) {
        if (position == null) {
            position = new float[2];
        }
        position[0] = x;
        position[1] = y;
    }

    public void setPosition(float[] position) {
        if (position.length == 0) {
            return;
        }
        position[0] *= Constants.WIDTH_RATIO;
        position[1] *= Constants.HEIGHT_RATIO;
        this.position = position;
    }

    public float getFlashDelayTime() {
        return this.flashDelayTime;
    }

    public void setFlashDelayTime(float time) {
        this.flashDelayTime = time;
    }

    public int getFlashCount() {
        return this.flashCount;
    }

    public void setFlashCount(int count) {
        this.flashCount = count;
    }

    public float getFlashDuration() {
        return this.flashDuration;
    }

    public void setFlashDuration(float duration) {
        this.flashDuration = duration;
    }

    public Vector3 getFlashRgb() {
        return this.flashRgb;
    }

    public void setFlashRgb(Vector3 rgb) {
        this.flashRgb = rgb;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getMpCost() {
        return this.mpCost;
    }

    public void setMpCost(int cost) {
        this.mpCost = cost;
    }
}
