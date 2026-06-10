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
import com.z.loa.manager.*;
import java.time.*;
import java.time.format.*;

public class CharacterInfomation {
	private Stage infomationStage;
	private MyGdxGame game;
	private TitleScreen screen;
	private boolean isVisible;
	private static InfomationInterface activeInterface;
	private static SystemInterface activeSystemInterface;

	private TextureRegionDrawable[] drawable;
	private Table table;
	private Table lowerTable;
	private ImageButton returnButton;

	private static ObjectMap<String, TextureRegionDrawable> textureRegionDrawableMap;
	private static ObjectMap<TextButton, String> stringMap;
	private static ObjectMap<TextButton, InfomationInterface> infomationInterfaceMap;
	private String[] temp;
	private TextButton[] topBarButton;
	private TextButton[] systemOptionButton;
	private TextButton[] temp_4;
	private ImageButton switchButton;
	private Label.LabelStyle labelStyle;
	private Label.LabelStyle markupedLabelStyle;
	private Dialog.WindowStyle windowStyle;

	private Table leftTopSaveTable;
	private Table lowerSaveTable;
	private Table rightTopSaveTable;
	private Table overlayTable;

	private TextureRegion[][] split;
	private ImageButton[][] saveButtonsArrays;

	private Dialog dialog;
	private Label tips;
	private TextButton confirmButton;
	private TextButton cancelButton;

	private DateTimeFormatter formatter;

	static {
		activeInterface = InfomationInterface.INIT;
	}

	private enum InfomationInterface {
		INIT, SKILL, ITEM, EQUIP, SYSTEM;
	}

	private enum SystemInterface {
		GANE_INFO, SAVE_LOAD, EXIT_GAME, TEST_BATTLE;
	}

	public CharacterInfomation(TextureRegion[][] split, MyGdxGame game, TitleScreen screen) {
		this.game = game;
		this.screen = screen;

		infomationStage = new Stage();
		textureRegionDrawableMap = new ObjectMap<>();
		stringMap = new ObjectMap<>();
		infomationInterfaceMap = new ObjectMap<>();
		ImageButton.ImageButtonStyle return_button_style = new ImageButton.ImageButtonStyle();
		Drawable d_u = new TextureRegionDrawable(split[0][0]);
		d_u.setMinSize(72, 72);
		return_button_style.up = d_u;
		Drawable d_d = new TextureRegionDrawable(split[0][1]);
		d_d.setMinSize(72, 72);
		return_button_style.down = d_d;
		returnButton = new ImageButton(return_button_style);
		returnButton.setPosition(Constants.WIDTH * 13 / 15 - 72.0f, Constants.BELOW_Y);
		createTextureRegionDrawable();
		create();

	}

	private void createTextureRegionDrawable() {
		temp = new String[]{"info_initbg", "info_itembg", "info_eqbg", "info_moneybar", "info_sub", "save_load"};
		drawable = new TextureRegionDrawable[temp.length];
		for (int i = 0; i < temp.length; i++) {
			drawable[i] = new TextureRegionDrawable(new Texture(Gdx.files.internal("info/" + temp[i] + ".png")));
			if (i == 4) {
				drawable[i].setMinSize(457.8f, 373.5f);
			} else {
				drawable[i].setMinSize(790.0f, 990.0f);
			}

			textureRegionDrawableMap.put(temp[i], drawable[i]);
		}

	}

	private void create() {
		returnButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (activeSystemInterface != null) {
					table.setBackground(drawable[1]);
					if (activeSystemInterface == SystemInterface.GANE_INFO) {
						setSystemLayout();
					} else if (activeSystemInterface == SystemInterface.SAVE_LOAD) {
						table.reset();
						setTop();
						setLower();
					}
					activeSystemInterface = null;
				} else {
					hide();
				}

				return true;
			}

		});
		table = new Table();
		lowerTable = new Table();
		overlayTable = new Table();
		table.setBackground(drawable[0]);
		table.setPosition(Constants.WIDTH * 2 / 15, (Constants.HEIGHT - 930.0f) / 2);
		table.setSize(Constants.VISION_WIDTH, 990.0f);
		//overlayTable.setPosition(Constants.WIDTH * 2 / 15, Constants.HEIGHT * 45 / 152);
		//overlayTable.setSize(790.0f, 990.0f);
		windowStyle = new Dialog.WindowStyle(FontManager.getFont(), Color.WHITE, drawable[4]);
		dialog = new Dialog("", windowStyle);

		createTopBarButton();
		setTop();
		createLowerBar();
		setLower();

		infomationStage.addActor(table);
		infomationStage.addActor(returnButton);
		//infomationStage.addActor(overlayTable);

	}

	private void setLower() {
		table.left().bottom().add(lowerTable).colspan(5).expand().fill();
		//table.debugAll();
	}

	private void setTop() {
		table.left().bottom().add(topBarButton).row();
		for (Cell<?> c : table.getCells()) {
			c.padBottom(22.5f);
			c.expandX();
		}
	}

	private void createTopBarButton() {
		String[] temp_1 = {"[GREEN]状态", "[BLACK]技能", "[BLACK]物品", "[BLACK]装备", "[BLACK]系统"};
		String[] temp_5 = {"info_initbg", "info_initbg", "info_itembg", "info_eqbg", "info_itembg"};
		InfomationInterface[] values = InfomationInterface.values();
		topBarButton = new TextButton[5];
		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
		style.font = FontManager.getFont();
		style.fontColor = Color.WHITE;
		for (int i = 0; i < 5; i++) {
			topBarButton[i] = new TextButton(temp_1[i], style);
			stringMap.put(topBarButton[i], temp_5[i]);
			infomationInterfaceMap.put(topBarButton[i], values[i]);
		}
		addTopBarButtonListener();

	}

	private void addTopBarButtonListener() {
		for (final TextButton t_b : topBarButton) {
			final String v = stringMap.get(t_b);
			t_b.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					markupText(t_b, v);
					activeInterface = infomationInterfaceMap.get(t_b);
					createLowerBar();
					//lowerTable.debugAll();
					return true;
				}

			});
		}

	}

	private void markupText(TextButton t_b, String v) {
		table.setBackground(textureRegionDrawableMap.get(v));
		for (TextButton t_b_1 : topBarButton) {
			String old_string = t_b_1.getText().toString();
			if (t_b_1 != t_b && old_string.contains("[GREEN]")) {
				String new_string = old_string.replace("[GREEN]", "[BLACK]");
				t_b_1.setText(new_string);
				t_b.setText(t_b.getText().toString().replace("[BLACK]", "[GREEN]"));
				break;
			}
		}	
	}

	private void createLowerBar() {
		activeSystemInterface = null;
		switch (activeInterface) {
			case INIT :
				setInitLayout();
				break;
			case SKILL :
				setSkillLayout();
				break;
			case ITEM :
				setItemLayout();
				break;
			case EQUIP :
				setEquipLayout();
				break;
			case SYSTEM :
				setSystemLayout();
				break;
		}

	}

	private void setInitLayout() {
		lowerTable.reset();
		String[] temp_3 = {"角色1", "角色2", "角色3"};
		temp_4 = new TextButton[3];
		for (int i = 0; i < 3; i++) {
			TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
			style.font = FontManager.getFont();
			style.fontColor = Color.BLACK;
			temp_4[i] = new TextButton(temp_3[i], style);
			temp_4[i].addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

					return true;

				}
			});
		}
		for (TextButton t_b : temp_4) {
			lowerTable.left().bottom().add(t_b).padBottom(22.5f).expand().row();
		}

		if (labelStyle == null || markupedLabelStyle == null) {
			labelStyle = new Label.LabelStyle(FontManager.font, Color.BLACK);
			markupedLabelStyle = new Label.LabelStyle(FontManager.font, Color.WHITE);
		}

		Drawable d = drawable[3];
		d.setMinSize(201.1f, 76.5f);
		labelStyle.background = d;

		Label label = new Label(" 金", labelStyle);
		label.setFontScale(0.9f);
		lowerTable.add(label).right().padTop(-90.0f).padRight(72.0f).expandX();

	}

	private void setSkillLayout() {
		setInitLayout();

	}

	private void setItemLayout() {
		lowerTable.reset();
		labelStyle.background = null;
		Label label = new Label("上", labelStyle);
		Label label_1 = new Label("中", labelStyle);
		Label label_2 = new Label("下", labelStyle);
		Label label_3 = new Label("底部", labelStyle);
		lowerTable.add(label).expandX().row();
		lowerTable.add(label_1).height(675.0f).expandX().row();
		lowerTable.add(label_2).expandX().row();
		lowerTable.add(label_3).padTop(11.25f).height(101.25f).expandX();

	}

	private void setEquipLayout() {
		lowerTable.reset();

	}

	private void setSystemLayout() {
		lowerTable.reset();
		labelStyle.background = null;
		Label label = new Label("音乐设置", labelStyle);
		lowerTable.left().bottom().padTop(54.0f).add(label).right().expand();
		if (systemOptionButton == null) {
			createSystemTableSubassembly();
		}
		lowerTable.left().bottom().add(switchButton).left().expand().row();
		for (TextButton t_b : systemOptionButton) {
			lowerTable.left().bottom().add(t_b).right().expand().row();
		}
		lowerTable.padBottom(171.0f);

	}

	private void createSystemTableSubassembly() {
		ImageButton.ImageButtonStyle i_l = new ImageButton.ImageButtonStyle();
		split = TextureCache.getSplit("info/switch", 3, 5);
		i_l.checked = new TextureRegionDrawable(split[4][2]);
		i_l.up = new TextureRegionDrawable(split[4][0]);
		switchButton = new ImageButton(i_l);
		switchButton.setChecked(true);
		switchButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

				if (GameScreen.GAME_MUSIC.isPlaying()) {
					GameScreen.GAME_MUSIC.pause();
				} else {
					GameScreen.GAME_MUSIC.play();
				}
				return true;

			}
		});
		String[] temp_6 = {"游戏信息", "储存进度", "离开游戏"};
		systemOptionButton = new TextButton[temp_6.length];
		for (int i = 0; i < temp_6.length; i++) {
			final int j = i;
			TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
			style.font = FontManager.getFont();
			style.fontColor = Color.BLACK;
			systemOptionButton[i] = new TextButton(temp_6[i], style);
			systemOptionButton[i].addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					updateOptionInterface(j);
					return true;
				}
			});
		}
	}

	private void updateOptionInterface(int j) {
		switch (j) {
			case 0 :
				activeSystemInterface = SystemInterface.GANE_INFO;
				String temp = "苍神录\n游戏版本：1.0\n制作者：万恶的藏宝图\n开发框架：libGDX 框架版本：1.14.0\nlibGDX官方网站：https://libgdx.com";
				Label label = new Label(temp, labelStyle);
				lowerTable.clear();
				lowerTable.left().bottom().add(label).expand();
				break;
			case 1 :
				activeSystemInterface = SystemInterface.SAVE_LOAD;
				table.reset();
				table.setBackground(drawable[5]);

				leftTopSaveTable = new Table();
				rightTopSaveTable = new Table();
				lowerSaveTable = new Table();
				if (saveButtonsArrays == null) {
					saveButtonsArrays = new ImageButton[3][3];
				}
				formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH:mm");
				table.padTop(27.0f).padBottom(81.0f);
				rightTopSaveTable.padTop(40.0f).padBottom(40.0f);
				Label label_4 = new Label("角色1", labelStyle);
				Label label_5 = new Label("角色2", labelStyle);
				Label label_6 = new Label("角色3", labelStyle);
				Label label_7 = new Label("金：999999    ", labelStyle);
				label_7.setFontScale(0.8f);
				Label[] right_label = {label_4, label_5, label_6, label_7};
				for (int i = 0; i < 3; i++) {
					Label l = new Label("盘" + i, labelStyle);
					Label l_1 = new Label(LocalDateTime.now().format(formatter), labelStyle);
					leftTopSaveTable.left().bottom().add(l).expandX().fill();
					for (int k = 0; k < 4; k++) {
						ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
						style.imageDisabled = new TextureRegionDrawable(split[k][0]);
						style.imageUp = new TextureRegionDrawable(split[k][1]);
						style.imageDown = new TextureRegionDrawable(split[k][2]);
						style.imageChecked = new TextureRegionDrawable(split[k][2]);

						ImageButton i_b = new ImageButton(style);

						if (k != 0) {
							i_b.setDisabled(true);
							i_b.setTouchable(Touchable.disabled);
							saveButtonsArrays[i][k - 1] = i_b;
						}
						addSaveButtonListener(i_b, l_1, k, i, right_label);
						leftTopSaveTable.left().bottom().add(i_b).expandX();
					}

					leftTopSaveTable.row();
					leftTopSaveTable.left().bottom().add(l_1).colspan(5).expand().fill().row();
				}
				for (int i = 0; i < 4; i++) {
					if (i == 3) {
						rightTopSaveTable.left().bottom().add(label_7).bottom().expand().fill();
					} else {
						rightTopSaveTable.left().bottom().add(right_label[i]).expand().fill().row();
					}
				}

				labelStyle.font = FontManager.font;
				Label label_8 = new Label("选择要进行的操作", labelStyle);
				lowerSaveTable.left().bottom().add(label_8).expand();

				table.left().bottom().add(leftTopSaveTable).padLeft(18.0f).padRight(36.0f).expand().fill();
				table.left().bottom().add(rightTopSaveTable).padRight(36.0f).expand().fill().row();
				table.left().bottom().add(lowerSaveTable).padTop(18.0f).colspan(2).expand().fill();
				//table.debugAll();
				break;
			case 2 :
				activeSystemInterface = SystemInterface.EXIT_GAME;
				setDialog("[BLACK]确定要离开游戏并返回标题界面吗？[RED]未保存的进度将丢失！", null, -1, -1, null);
				dialog.show(infomationStage);
				break;
		}
	}

	private void addSaveButtonListener(final ImageButton image_button, final Label label, final int k, final int l,
			final Label[] right_label) {
		switch (k) {
			case 0 :
				image_button.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
						if (label.getText().charAt(0) == '空') {
							image_button.getStyle().imageChecked = null;
							writeSave(label, l);
						} else {
							//l各行存档索引，k各个按钮索引
							image_button.getStyle().imageChecked = new TextureRegionDrawable(split[0][2]);
							image_button.setChecked(true);
							setDialog("[GREEN]覆盖[BLACK]盘" + l + "数据？", label, k, l, image_button);
							dialog.show(infomationStage);
						}

						return true;
					}
				});
				break;
			case 2 :
				image_button.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
						image_button.setChecked(true);
						setDialog("[RED]删除[BLACK]盘" + l + "数据？", label, k, l, image_button);
						dialog.show(infomationStage, Actions.fadeIn(0.2f));
						return true;
					}
				});
				break;
			case 3 :
				image_button.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
						for (int i = 0; i < 3; i++) {
							if (i != l) {
								saveButtonsArrays[i][k - 1].setChecked(false);
							}
						}
						for (int i = 0; i < 4; i++) {
							if (i == 3) {
								String money = "" + MathUtils.random(5, 99999999);
								if (money.length() < 8) {
									money += "  ".repeat(8 - money.length());
								}
								right_label[i].setText("金：" + money);
							} else {
								right_label[i].setText("角色" + MathUtils.random(0, 10));
							}
						}
						return true;
					}
				});
				break;

		}
	}

	private void writeSave(Label label, int l) {
		label.setText("");
		label.setText(LocalDateTime.now().format(formatter));
		for (int i = 0; i < 3; i++) {
			if (!saveButtonsArrays[l][i].isDisabled()) {
				break;
			}
			saveButtonsArrays[l][i].setDisabled(false);
			saveButtonsArrays[l][i].setTouchable(Touchable.enabled);
		}
	}

	private void deleteSave(Label label, int l) {
		label.setText("空" + " ".repeat(26));
		for (int i = 0; i < 3; i++) {
			saveButtonsArrays[l][i].setDisabled(true);
			saveButtonsArrays[l][i].setTouchable(Touchable.disabled);
		}
	}

	private void matchButtonOperation(Label label, DateTimeFormatter formatter, int l, int k) {
		switch (k) {
			case 0 :
				writeSave(label, l);
				break;
			case 1 :

				break;
			case 2 :
				deleteSave(label, l);
				break;
		}
	}

	private void setDialog(String text, final Label label, final int k, final int l, final ImageButton image_button) {
		if (tips == null) {		
			tips = new Label(text, markupedLabelStyle);
			tips.setFontScale(1.1f);
			tips.setWrap(true);
			tips.setAlignment(Align.center);
			TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
			style.font = FontManager.getFont();
			style.fontColor = Color.BLACK;
			confirmButton = new TextButton("是", style);
			cancelButton = new TextButton("否", style);
			Table content = dialog.getContentTable();
			content.pad(18.0f).add(tips).colspan(2).expand().fill().row();
			content.add(confirmButton, cancelButton).center();
			//content.setDebug(true);
		}
		//tips.setStyle(markupedLabelStyle);
		confirmButton.clearListeners();
		cancelButton.clearListeners();
		if (activeSystemInterface == SystemInterface.EXIT_GAME) {
			confirmButton.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					activeInterface = InfomationInterface.INIT;				
                    Array<Actor> actors = game.getTransStage().getActors();
					for (int i = actors.size - 1; i >= 2; i --) {
						actors.get(i).remove();
					}
					hide();
					game.setScreen(screen);
					dialog.hide(Actions.fadeOut(0.2f));
					activeSystemInterface = null;
					return true;
				}
			});
			cancelButton.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					dialog.hide(Actions.fadeOut(0.2f));
					activeSystemInterface = null;
					return true;
				}
			});
		} else {
			confirmButton.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					matchButtonOperation(label, formatter, l, k);
					dialog.hide(Actions.fadeOut(0.2f));
					image_button.setChecked(false);
					return true;
				}
			});
			cancelButton.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					dialog.hide(Actions.fadeOut(0.2f));
					image_button.setChecked(false);
					return true;
				}
			});
		}
		tips.setStyle(markupedLabelStyle);
		tips.setText(text);

		dialog.pack();

	}

	public void show() {
		isVisible = true;
		Gdx.input.setInputProcessor(infomationStage);

	}

	public void render() {
		infomationStage.act();
		infomationStage.draw();

	}

	public void hide() {
		isVisible = false;
		Gdx.input.setInputProcessor(game.getTransStage());
	}

	public boolean isVisible() {
		return isVisible;
	}

}

