package com.z.loa.manager;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.z.loa.*;
import com.z.loa.data.*;
import com.z.loa.entity.npc.*;
import com.z.loa.entity.player.*;
import com.z.loa.screen.*;

public class DialogManager {
    public static boolean isTextPlaying = false;
	public static boolean isTextEnd = false;
	public static boolean isDialogueEnd = false;
	public static NPC activeNPC;
	public static ZhanTingyun zhanTingyun;

	private static DialogueData dialogueData;
	private static DialogNode dialogNode;
	private static DialogueData.Character character;
	private static DialogueData.Dialogue dialogue;

	private Dialog simpleDialog;
	private Dialog fullDialog;
	private ScrollPane scrollPane;
	private Stage uiStage;
	private TextureRegionDrawable avatarDrawable;
	private Animation<TextureRegion> moodAni;
	private Label name;
	private Label message;
	private Label dialogueWait;
	private TextButton confirm_button;
	private TextButton cancel_button;
	private Array<String> Pages;
	private int pageIndex;

	private Dialog.WindowStyle windowStyle;
	private Label.LabelStyle nameStyle;
	private Label.LabelStyle messageStyle;
	private ScrollPane.ScrollPaneStyle scrollPaneStyle;
	private Label.LabelStyle dialogueWaitStyle;
	private TextButton.TextButtonStyle optionStyle;

	public DialogManager() {
		TextureRegion dialogue_frame_region = new TextureRegion(new Texture(Gdx.files.internal("dialogueframe.png")));
		TextureRegion dialogue_wait_region = new TextureRegion(new Texture(Gdx.files.internal("dialoguewait.png")));
		TextureRegionDrawable dialogue_frame_drawable = new TextureRegionDrawable(dialogue_frame_region);
		dialogue_frame_drawable.setMinSize(Constants.VISION_WIDTH, Constants.HEIGHT_RATIO * 62);//29 / 304
		TextureRegionDrawable dialogue_wait_drawable = new TextureRegionDrawable(dialogue_wait_region);
		dialogue_wait_drawable.setMinSize(Constants.WIDTH * 7 / 108, Constants.HEIGHT * 7 / 456);
		this.windowStyle = new Dialog.WindowStyle(TitleScreen.font, Color.BLACK, dialogue_frame_drawable);
		this.nameStyle = new Label.LabelStyle(FontManager.getFont(), Color.WHITE);
		this.messageStyle = new Label.LabelStyle(FontManager.getFont(), Color.WHITE);
		this.dialogueWaitStyle = new Label.LabelStyle(FontManager.getFont(), Color.WHITE);
		this.optionStyle = new TextButton.TextButtonStyle();
        optionStyle.font = FontManager.getFont();
        optionStyle.fontColor = Color.BLACK;
		this.scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
		dialogueWaitStyle.background = dialogue_wait_drawable;
		this.simpleDialog = new Dialog("", windowStyle);
		this.fullDialog = new Dialog("", windowStyle);
		dialogNode = new DialogNode();

		loadDialogues("01006");
	}

	public static void loadDialogues(String file_name) {
		FileHandle file = Gdx.files.internal("data/dialogue/dialogue" + file_name + ".json");
		Json json = new Json();
		//使用libGDX提供的ObjectMap进行json反序列化，免去一些中间步骤
		dialogueData = json.fromJson(DialogueData.class, file);
	}

	public Dialog getDialog(String c_dialogue) {
		dialogue = dialogueData.dialogues.get(c_dialogue);
		if (!dialogNode.checkConditions(dialogue.conditions)) {
			dialogue = dialogueData.dialogues.get(dialogNode.jumpTo);
		}
		character = dialogueData.characters.get(dialogue.character);
		Pages = dialogue.getPages();
		StringBuilder temp = new StringBuilder();
		if (Pages != null) {
			for (String s : Pages) {
				temp.append(s);
			}
		}
		if (dialogue.layoutType.equals("simple")) {
			return simpleDialog();
		}
		return fullDialog();

	}

	private void setLeftColumn(String name_s, Table content) {
		TextureRegion avatar_texture_region = TextureCache.getAvatarTextureRegion(character.avatar.get(dialogue.avatarIndex));
		avatarDrawable = new TextureRegionDrawable(avatar_texture_region);
		if (name == null) {
			name = new Label(name_s, nameStyle);
			name.setFontScale(1.10f);
		}
		name.setText(character.name);
        
		Image avatar = new Image(avatarDrawable);
		avatar.setScaling(Scaling.fit);
		float imgHeight = Constants.HEIGHT_RATIO * 57;
		float imgWidth = imgHeight * (avatarDrawable.getMinWidth() / avatarDrawable.getMinHeight()); // 保证宽高
		Container<Image> container = new Container<Image>(avatar);
		container.size(imgWidth, imgHeight);
		container.fill().left().top();
		container.padLeft(10f);
        
        Table left_column = new Table();
		left_column.add(container).padBottom(-65).row();
		left_column.add(name);
		content.add(left_column).left().padLeft(18).padRight(8);
	}

	private void setRightColumn(Table content) {
		if (message == null) {
			createMessageRelated();
		} else {
			message.setText("");
		}
		dialogueWait.setVisible(false);
		
		scrollPane.setActor(message);
		Table right_column = new Table();
		right_column.left().top();

		if (dialogue.options != null) {
			right_column.add(message).colspan(2).expand().fill();
			if (confirm_button == null && cancel_button == null) {
				createOptionButton();
			}
			right_column.row();
			right_column.add(confirm_button).expandX();
			right_column.add(cancel_button).expandX();
		} else {
			right_column.add(scrollPane).expand().fill();
			right_column.add(dialogueWait).bottom();
		}
		content.add(right_column).expand().fill();

	}

	private void createMessageRelated() {
		message = new Label("", messageStyle);
		message.setAlignment(Align.center);
		message.setWrap(true);
		dialogueWait = new Label("", dialogueWaitStyle);
		dialogueWait.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 10, 1.0f, Interpolation.sine),
			Actions.moveBy(0, -10, 1.0f, Interpolation.sine)
        )));
		scrollPaneStyle.vScroll = null;
		scrollPaneStyle.vScrollKnob = null;

		scrollPane = new ScrollPane(message, scrollPaneStyle);
		scrollPane.setScrollingDisabled(true, false); //禁止水平滚动，仅允许垂直滚动
		scrollPane.setOverscroll(false, false); // 禁止超出边界回弹
		scrollPane.setSmoothScrolling(true);
	}

	private void createOptionButton() {
		confirm_button = new TextButton("是", optionStyle);
		confirm_button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Dialog n_dialog = getDialog(dialogue.options.get(0).next);
				show(n_dialog, uiStage);
			}
		});
		cancel_button = new TextButton("否", optionStyle);
		cancel_button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Dialog n_dialog = getDialog(dialogue.options.get(1).next);
				show(n_dialog, uiStage);
			}
		});
	}

	private Dialog simpleDialog() {
		Table content = simpleDialog.getContentTable();
		content.clear();
		content.left().top();
		content.defaults().pad(10f);
		setRightColumn(content);

		simpleDialog.pack();
		simpleDialog.setPosition(Constants.LEFT_SIDE_X, Constants.BELOW_Y);// 45 / 152
		return simpleDialog;
	}

	private Dialog fullDialog() {
		String name_s = character.name;
		Table content = fullDialog.getContentTable();
		content.clear();
		content.left().top();
		content.defaults().pad(10f);
		setLeftColumn(name_s, content);
		setRightColumn(content);

		//对话框打包要在最后，位置设置要在打包之后
		fullDialog.pack();
		fullDialog.setPosition(Constants.LEFT_SIDE_X, Constants.BELOW_Y);
		//fullDialog.debugAll();
		return fullDialog;
	}

	public void show(Dialog dialog, Stage stage) {
		isDialogueEnd = false;
		isTextPlaying = true;
		isTextEnd = false;
		pageIndex = 0;

		if (uiStage == null) {
			uiStage = stage;
		}
		if (activeNPC != null) {
			activeNPC.facePlayer();
			activeNPC.setFrozenFacingForDialog(true);
		}
		if (dialogue.mood != null) {
			showMoodAnimation();
		}
		showHelper(dialog, stage);

	}

	private void showHelper(final Dialog dialog, final Stage stage) {
		clearDialogListeners(dialog);
		dialog.show(stage, Actions.fadeIn(0.2f));
		showCurrentPage();
		dialog.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (isTextEnd && dialogue != null) {
					if (pageIndex < Pages.size) {
						showCurrentPage();
					} else {
						handleNextDialogue(dialog, stage);
					}
				}
			}
		});

	}

	private void showCurrentPage() {
		String current_page_text = Pages.get(pageIndex);
		addTextAnimation(current_page_text);
		pageIndex++;
	}

	private void handleNextDialogue(Dialog dialog, final Stage stage) {
		if ((!"end".equals(dialogue.next))) {
			if (dialogue.options != null) {
				return;
			}
			final Dialog n_dialog = getDialog(dialogue.next);

			dialog.hide();
			message.setColor(Color.BLACK);
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					isTextPlaying = false;
					show(n_dialog, stage);
				}
			});
		} else {
			isDialogueEnd = true;
			dialog.hide();
			if (activeNPC != null) {
				activeNPC.setFrozenFacingForDialog(false);
			}

		}

	}

	private void clearDialogListeners(Dialog dialog) {
		Array<EventListener> listeners = dialog.getListeners();
		for (int i = listeners.size - 1; i >= 0; i--) {
			if (listeners.get(i) instanceof ClickListener) {
				dialog.removeListener(listeners.get(i));
			}
		}
	}

	private void addTextAnimation(final String text) {
		final Array<int[]> markup_ranges = initialiseBeforeAnimation(text);
		final int[] char_index = {0};
        
		message.addAction(Actions.forever(Actions.sequence(
            Actions.delay(0.02f), 
            Actions.run(new Runnable() {
    			@Override
    			public void run() {
    				if (char_index[0] < text.length()) {
    					if (!markup_ranges.isEmpty()) {
    						for (int[] i : markup_ranges) {
    							if (char_index[0] >= i[0] && char_index[0] <= i[1]) {
    								char_index[0] = i[1];
    								break;
    							}
    						}
    					}
    					message.setText(text.substring(0, char_index[0] + 1));
    					activeAutoScrollY(4.0f);
    					char_index[0]++;
    
    				} else {
    					message.clearActions();
    					dialogueWait.setVisible(true);
    					isTextEnd = true;
    				}
    			}
    		}
        ))));
	}

	private void activeAutoScrollY(final float speed) {
		scrollPane.clearActions();
		scrollPane.addAction(Actions.forever(Actions.run(new Runnable() {
			@Override
			public void run() {
				float current_y = scrollPane.getScrollY();
				float new_y = current_y + speed;
				new_y = Math.min(new_y, scrollPane.getMaxY());
				new_y = Math.max(new_y, 0);
				scrollPane.setScrollY(new_y);
				if (new_y >= scrollPane.getMaxY()) {
					scrollPane.clearActions();
				}
			}
		})));
	}

	public static void activeAutoScrollX(final float speed, final ScrollPane scroll_pane) {
		scroll_pane.clearActions();
		scroll_pane.addAction(new Action() {
			private boolean isPaused = true;
			private float stateTime = 0;
			private float time = 1.0f;

			@Override
			public boolean act(float p) {
				float current_x = scroll_pane.getScrollX();
				if (isPaused) {
					stateTime += p;
				}
				if (stateTime >= time) {
					isPaused = false;
				}
				if (!isPaused) {
					float new_x = current_x + speed;
					//maxX是ScrollPane子要素宽减pane宽得到的，除非子要素宽度小于pane
					new_x = Math.min(new_x, scroll_pane.getMaxX());
					new_x = Math.max(new_x, 0);
					scroll_pane.setScrollX(new_x);
					if (new_x >= scroll_pane.getActor().getWidth() / 2) {
						scroll_pane.setScrollX(0);
						stateTime = 0f;
						isPaused = true;
					}
				}

				return false;
			}

		});
	}

	private Array<int[]> initialiseBeforeAnimation(String text) {
		message.clearActions();
		if (text.contains("[")) {
			message.setColor(Color.WHITE);
		} else {
			message.setColor(Color.BLACK);
		}
		Array<int[]> markup_ranges = new Array<>();
		String pending_text = text;
		int markup_start = pending_text.indexOf("[");
		while (markup_start != -1) {
			int markup_end = pending_text.indexOf("]");
			markup_ranges.add(new int[]{markup_start, markup_end});
			//repeat方法在安卓13才出现，可能要注意兼容性问题
			pending_text = pending_text.replaceFirst("\\[([A-Z]+)\\]", "*".repeat(markup_end - markup_start + 1));
			markup_start = pending_text.indexOf("[");
		}
		return markup_ranges;
	}

	private void showMoodAnimation() {
		moodAni = new Animation<TextureRegion>(0.15f, TextureCache.getSplit(dialogue.mood, 5)[0]);
		moodAni.setPlayMode(Animation.PlayMode.NORMAL);
		zhanTingyun.matchMoodAnimation(moodAni);
	}

}

