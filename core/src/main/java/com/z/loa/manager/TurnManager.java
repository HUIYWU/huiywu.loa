package com.z.loa.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Timer;
import com.z.loa.entity.BaseEntity;
import com.z.loa.entity.config.BattleActionConfig;
import com.z.loa.entity.event.battle.EffectFinishEvent;
import com.z.loa.entity.event.battle.EffectTriggerEvent;
import com.z.loa.screen.BattleScene;

public class TurnManager {
    private Array<BaseEntity> participants;
    private Array<BaseEntity> players;
    private Array<BaseEntity> enemis;
    private Array<BaseEntity> defeatedParticipants;
    private Array<BaseEntity> remainPlayers;
    private BattleScene scene;
    private BattleActionManager actionManager;
    private EffectManager effectManager;
    private ObjectMap<BaseEntity, Label> floatingLabelMap;
    private int turnIndex;
    //private int independentVariable;
    private BaseEntity activeParticipant;
    private boolean waitingForOperation;
    //特效事件控制，处理单配置多特效引起的多个结束事件多次结束回合
    private boolean eventRound;
    private boolean endTurn;
    private float[] originalPosition;
    
    private boolean battleEnded;

    public TurnManager() {}

    public TurnManager(Array<BaseEntity> participants, Array<BaseEntity> players, BattleScene scene) {
        this.participants = participants;
        this.players = players;
        this.scene = scene;
        this.originalPosition = new float[2];
        this.defeatedParticipants = new Array<BaseEntity>(8);
        this.enemis = new Array<BaseEntity>();
        this.remainPlayers = new Array<BaseEntity>(3);
    }

    public void init(BattleActionManager manager_1, EffectManager manager_2, ObjectMap<BaseEntity, Label> floating) {
        this.actionManager = manager_1;
        this.effectManager = manager_2;
        this.floatingLabelMap = floating;
        for(BaseEntity entity : participants) {
        	if(!players.contains(entity, true)) {
        		enemis.add(entity);
        	}
        }
        remainPlayers.addAll(players);
        registerParticipantListener();
    }
    
    
    private void registerParticipantListener() {
        EventListener listener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.getTarget() != activeParticipant) {
                    return false;
                }
                
                if (event instanceof EffectFinishEvent) {
                    if (eventRound) {
                    	scene.recoverLowerPart();
                        scene.clearTwinText();
                        scene.enableDialog(true);
                        BaseEntity target = (BaseEntity) event.getTarget();
                        if (!isPlayer(target)) {
                            target.clearActions();
                        	target.addAction(Actions.moveTo(originalPosition[0], originalPosition[1], 0.5f, Interpolation.smooth));
                        }
                        eventRound = false;
                        endTurn = true;
                        for(BaseEntity aim : actionManager.getAims()) {
                        	addLabelAction(aim);
                        }
                        activeParticipant.resetEventFire();
                        activeParticipant.setActionConfig(null);
                        //endTurn();
                    }
                    return true;
                } else if (event instanceof EffectTriggerEvent) {
                    scene.preprocess((TextButton) scene.getButtonTable().getChild(0));
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            scene.setStateImage();
                        }
                    }, 0.1f);
                    EffectTriggerEvent trigger_event = (EffectTriggerEvent) event;
                    BaseEntity target = trigger_event.getTarget(); // target是事件发起对象
                    BattleActionConfig config = trigger_event.getActionConfig();
                    Array<BaseEntity> aims = actionManager.getAims(); // aim是选择的对象
                    if(!isPlayer(target)) {
                    	originalPosition[0] = target.getX();
                        originalPosition[1] = target.getY();
                        target.addAction(Actions.moveBy(target.getWidth() / 7, 0, 0.5f, Interpolation.smooth));
                    }
                    effectManager.postEffect(config, target, aims);
                    if (config.isFlashFollow()) {
                        Array<BaseEntity> target_1 = new Array<>();
                        target_1.add(target);
                        effectManager.postFlash(config, target_1);
                    } else {
                        effectManager.postFlash(config, aims);
                    }
                    target.resetEventFire();
                    eventRound = true;
                    return true;
                }
                return false;
            }
        };
        for (BaseEntity participant : participants) {
            participant.addListener(listener);
        }
    }
    
    private void updateParticipantsState() {
    	for(BaseEntity participant : participants) {
            int remain_hp = participant.getRemainHp();
            int max_hp = participant.getMaxHp();
            if(remain_hp <= 0 ) {
            	participant.setBattleState(BaseEntity.BattleState.DEFEATED);
                defeatedParticipants.add(participant);
            } else if (remain_hp <= max_hp * 0.3) {
            	participant.setBattleState(BaseEntity.BattleState.WEAK);
            } else {
            	participant.setBattleState(BaseEntity.BattleState.AWAIT);
            }
            	
    	}
    }
    
    private void addLabelAction(BaseEntity aim) {
        final int[] char_index = {0};
        Label label = floatingLabelMap.get(aim);
        float[] original_position = {label.getX(), label.getY()};
        String text = "" + MathUtils.random(7000, 10000);
        
        //为浮动数值标签添加数字跃动效果
        //两个平行动作，前一用于标签类斜抛移动，后一用于逐字动画及收尾
        final int[] count = {1};
        Action parallel_action = Actions.forever(Actions.sequence(
            Actions.moveBy(10 / count[0], 15 / count[0], 0.1f / count[0], Interpolation.sine), 
            Actions.moveBy(10 / count[0], -15 / count[0], 0.08f / count[0], Interpolation.sine), 
            Actions.run(() -> count[0] ++)
        ));
        label.clearActions();
        label.addAction(Actions.parallel(
            parallel_action, 
            Actions.forever(Actions.sequence(
                Actions.delay(0.04f), 
                Actions.run(new Runnable() {
            		@Override
            		public void run() {
            			if (char_index[0] < text.length()) {
            				label.setText(text.substring(0, char_index[0] + 1));
            				char_index[0]++;
            			} else {
            				label.clearActions();
                            label.addAction(Actions.sequence(
                                Actions.delay(0.4f), 
                                Actions.run(() -> {
                                    aim.decreaseRemainHp(Integer.parseInt(text));
                                    if (players.contains(aim, true)) {
                                    	scene.hpProgressTables[players.indexOf(aim, true)].progressBar.setValue(aim.getRemainHp());
                                        scene.hpProgressTables[players.indexOf(aim, true)].progressLabel.setText(aim.getRemainHp() + "/" + aim.getMaxHp());
                                    }
                                }), 
                                Actions.run(() -> {
                                    label.setText("");
                                    label.setPosition(original_position[0], original_position[1]);
//                                    updateParticipantsState();
//                                    scene.setStateImage();
                                    if(endTurn) {
                                    	endTurn();
                                        endTurn = false;
                                    }
                                    
                                })
                            ));
            			}
                    }
                })
            ))
        ));
    }

    public void startBattle() {
        for(BaseEntity player : players) {
        	player.setRemainHp(player.getMaxHp());
        }
        //independentVariable = 0;
        turnIndex = 3;
        Timer.schedule(
            new Timer.Task() {
                @Override
                public void run() {
                    startTurn();
                }
            }, 
            0.2f
        );
    }
    
    public void endBattle() {
    	this.battleEnded = true;
        this.defeatedParticipants.clear();
    }
    
    public void resetBattleEnded() {
    	this.battleEnded = false;
    }

    private void startTurn() {
        activeParticipant = participants.get(turnIndex);
        if (defeatedParticipants.contains(activeParticipant, true)) {
            if(players.contains(activeParticipant, true)) {
            	remainPlayers.removeValue(activeParticipant, true);
            }
            endTurn();
            return;
        }
        if (isPlayer(activeParticipant)) {
            waitingForOperation = true;
            scene.enablePlayerControl(activeParticipant);
        } else {
            waitingForOperation = false;
            excuteAIOperation();
        }
        
    }

    private void endTurn() {
        if (activeParticipant != null) {
            updateParticipantsState();
            scene.setStateImage();
        }
        
        if (defeatedParticipants.containsAll(enemis, true)) {
        	endBattle();
            return;
        }
        
        if (defeatedParticipants.containsAll(players, true)) {
            endBattle();
            return;
        }


        // independentVariable ++;
        // turnIndex = (independentVariable + 3) % 6;
        turnIndex++;
        if (turnIndex == participants.size) {
            turnIndex = 0;
        }
        startTurn();
    }

    private boolean isPlayer(BaseEntity participant) {
        // Array类的contains方法的boolean参数决定比较方式
        // 为true使用==运算符比较，为false使用存入对象的equals方法比较
        if (players.contains(participant, true)) {
            return true;
        }
        return false;
    }

    private void excuteAIOperation() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                BattleActionConfig[] configs = BattleActionConfig.obtainConfigs(activeParticipant.getName());
                BattleActionConfig config = configs[MathUtils.random(0, configs.length - 1)];
                actionManager.selectAim(config, remainPlayers);
                scene.enableConfig(activeParticipant, config, BaseEntity.BattleState.SKILL, true);
            }
            
        }, 1.0f);
        //...
    }

    public BaseEntity getActiveParticipant() {
        return activeParticipant;
    }
    
    public boolean isEventRound() {
    	return eventRound;
    }
    
    public boolean isBattleEnded() {
    	return battleEnded;
    }

    
}
