package com.z.loa.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import com.z.loa.*;
import com.z.loa.entity.npc.*;
import com.z.loa.entity.player.*;
import com.z.loa.manager.*;
import java.util.HashMap;
import java.util.Comparator;

public class GameScreen implements Screen {
	private MyGdxGame game;
	private TitleScreen screen;
	public static final Music GAME_MUSIC;

	public static float scale;
	private static TiledMap map;
	private TiledMapRenderer renderer;
	//private ShapeRenderer debugRenderer;
	private static boolean isSorted;
	private int[][] layersIndex;
	private static TiledMapTileLayer collision;
	public static HashMap<NPC, Rectangle> NPCMap;
	public static HashMap<ZhanTingyun, Rectangle> playerMap;
	private static MapObjects objects;
	private static ZhanTingyun zhanTingyun;

	private OrthographicCamera camera;
    private float[] cameraPosition;
	private ScreenViewport mainViewport;
	private ScreenViewport uiViewport;
	private Stage stage;
	private Stage uiStage;
	private CharacterInfomation characterInfomation;
	private BattleScene battleScene;
	private MapsManager mapsManager;
	public static DialogManager dialogManager;

	private ImageButton detailButton;
	private boolean initialized;

	static {
		GAME_MUSIC = Gdx.audio.newMusic(Gdx.files.internal("map2.mid"));
	}

	public GameScreen(final MyGdxGame game, TitleScreen previous_screen) {
		this.game = game;
		this.screen = previous_screen;

	}

	public void init() {
		mapsManager = new MapsManager();
		dialogManager = new DialogManager();
		NPCMap = new HashMap<NPC, Rectangle>();
		playerMap = new HashMap<ZhanTingyun, Rectangle>();
		layersIndex = new int[][]{{0}, {2}};
        renderer = mapsManager.getRenderer("map01006");
        map = MapsManager.map;
        scale = MapsManager.scale;
		objects = MapsManager.objects;
		collision = MapsManager.collision;

		TitleScreen.TITLEMUSIC.stop();
		GAME_MUSIC.play();

		TextureRegion[][] split = TextureCache.getSplit("service", 2, 2);
		ImageButton.ImageButtonStyle detail_button_style = new ImageButton.ImageButtonStyle();
		detail_button_style.up = new TextureRegionDrawable(split[1][0]);
		detail_button_style.up.setMinSize(72, 72);
		detail_button_style.down = new TextureRegionDrawable(split[1][1]);
		detail_button_style.down.setMinSize(72, 72);
		detailButton = new ImageButton(detail_button_style);
		detailButton.setScale(1.5f);
		detailButton.setPosition(Constants.LEFT_SIDE_X, Constants.BELOW_Y);
		characterInfomation = new CharacterInfomation(split, game, screen);
		detailButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				characterInfomation.show();
				return true;
			}
		});

		camera = new OrthographicCamera();
        cameraPosition = new float[2];
		mainViewport = new ScreenViewport(camera);
		uiViewport = new ScreenViewport();
		stage = new Stage(mainViewport);
		uiStage = new Stage(uiViewport);
		updateStage(scale);
		Stage trans_satge = this.game.getTransStage();
		for (int i = 0; i < 5; i++) {
			trans_satge.addActor(zhanTingyun.buttons[i]);
		}

		ImageButton subButton = new ImageButton(detail_button_style);
		subButton.setPosition(Constants.WIDTH * 13 / 15 - 72.0f, Constants.BELOW_Y);
		battleScene = new BattleScene(game);
		battleScene.init(zhanTingyun);
		subButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				battleScene.show();
				return true;
			}
		});

		trans_satge.addActor(detailButton);
		trans_satge.addActor(subButton);
		DialogManager.zhanTingyun = zhanTingyun;
		initialized = true;
	}

	public boolean isInitializad() {
		return initialized;
	}

	private void updateStage(float previous_scale) {
		stage.clear();
		NPCMap.clear();
		for (MapObject object : objects) {
			MapProperties properties = object.getProperties();
			String type = properties.get("type", String.class);
			if (type.equals("teleport")) {
				continue;
			}
			if (type.equals("role")) {
				Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
				if (zhanTingyun == null) {
					zhanTingyun = new ZhanTingyun(rectangle.x * scale, rectangle.y * scale);
					zhanTingyun.buttons[4].addListener(new InputListener() {
						@Override
						public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
							if (!NPC.isNotCharacterCollision(zhanTingyun, null, ZhanTingyun.expectX,
									ZhanTingyun.expectY)) {
								String current_dialogue = DialogManager.activeNPC.initialConversation;
								Dialog dialog = dialogManager.getDialog(current_dialogue);
								dialogManager.show(dialog, game.getTransStage());
							}
							return true;
						}

					});
				} else {
					zhanTingyun.setPosition(rectangle.x * scale, rectangle.y * scale);
					zhanTingyun.setSize(zhanTingyun.getWidth() / previous_scale * scale,
							zhanTingyun.getHeight() / previous_scale * scale);
				}
				playerMap.put(zhanTingyun, rectangle);
				stage.addActor(zhanTingyun);
				continue;
			}
			String npc_name = object.getName();
			boolean collision = properties.get("collision", true, Boolean.class);
			String default_orientation = properties.get("defaultorientation", String.class);
			int idle_index = properties.get("idleindex", 1, Integer.class);
			String initial_conversation = properties.get("initialconversation", String.class);
			String sub_conversation = properties.get("subconversation", "", String.class);
			boolean is_wander = properties.get("iswander", false, Boolean.class);

			Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
			NPC npc = new NPC(rectangle.x * scale, rectangle.y * scale, zhanTingyun, is_wander);
			npc.create(npc_name, idle_index);
			npc.initializeProperties(initial_conversation, sub_conversation, default_orientation, collision);
			stage.addActor(npc);
			NPCMap.put(npc, rectangle);
		}
        updateCamera1();
	}

	public static boolean isNotTerrainCollision(float x, float y) {
		if (collision == null) {
			return false;
		}
		int tile_width = map.getProperties().get("tilewidth", Integer.class);
		int tile_height = map.getProperties().get("tileheight", Integer.class);
		int start_x = (int) Math.floor(x / scale / tile_width);
		int start_y = (int) Math.floor(y / scale / tile_height);
		//8是人物的像素宽
		int end_x = (int) Math.floor(((x + playerMap.get(zhanTingyun).getWidth()) / scale + 8) / tile_width);
		int end_y = (int) Math.floor(((y + playerMap.get(zhanTingyun).getHeight()) / scale) / tile_width);
		for (int i = start_x; i <= end_x; i++) {
			for (int j = start_y; j <= end_y; j++) {
				if (i < 0 || i >= collision.getWidth() || j < 0 || j >= collision.getHeight()) {
					return false;
				}
				TiledMapTileLayer.Cell cell = collision.getCell(i, j);
				if (cell != null) {
					return false;
				}

			}
		}
		return true;
	}

	//获取y来决定渲染顺序
	private float getDepth(Actor a) {
		float footOffset = 0f;
		return a.getY() + footOffset;
	}

	public static void onMoved() {
		isSorted = true;
	}

	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
		}
		if (uiStage != null) {
			uiStage.dispose();
		}
		if (map != null) {
			map.dispose();
		}

	}

	@Override
	public void hide() {
		// TODO: Implement this method
	}

	@Override
	public void pause() {
		// TODO: Implement this method
	}

	//测试actor边界
	//	private void drawDebugBounds() {
	//		if (debugRenderer == null) {
	//			debugRenderer = new ShapeRenderer();
	//		}
	//		debugRenderer.setProjectionMatrix(camera.combined);
	//		debugRenderer.begin(ShapeRenderer.ShapeType.Line);
	//		debugRenderer.setColor(Color.RED);
	//		if (zhanTingyun != null) {
	//			debugRenderer.rect(zhanTingyun.getX(), zhanTingyun.getY(), zhanTingyun.getWidth(), zhanTingyun.getHeight());
	//			debugRenderer.circle(zhanTingyun.getX() + zhanTingyun.getWidth() / 2,
	//					zhanTingyun.getY() + zhanTingyun.getHeight() / 2, 3);
	//		}
	//		debugRenderer.setColor(Color.YELLOW);
	//		for (Actor actor : stage.getActors()) {
	//			if (actor instanceof NPC) {
	//				debugRenderer.rect(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
	//			}
	//		}
	//
	//		debugRenderer.end();
	//	}

	@Override
	public void render(float p) {
        //剪刀测试用于限制内容显示区域
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor((int) Constants.WIDTH * 2 / 15, (int) (Constants.HEIGHT - 930) / 2 ,//45 / 152
				MathUtils.ceil(Constants.VISION_WIDTH), MathUtils.ceil(Constants.VISION_HEIGHT));
		if (characterInfomation.isVisible()) {
			characterInfomation.render();
		} else if (battleScene.isVisible()) {
			battleScene.render();
		} else {
			mainViewport.apply();
			if (isSorted) {
				Array<Actor> actors = stage.getActors();
				actors.sort(new Comparator<Actor>() {
					@Override
					public int compare(Actor a, Actor b) {
						return Float.compare(getDepth(b), getDepth(a));
					}
				});
				isSorted = false;
                teleportCheck();
			}
            if(initialized) {
            	updateCamera1();
                initialized = false;
            }
            updateCamera();
			renderer.setView(camera);
			renderer.render(layersIndex[0]);
			stage.act();
			stage.draw();
			//drawDebugBounds();
			renderer.render(layersIndex[1]);

			uiViewport.apply();
			uiStage.act();
			uiStage.draw();
		}
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

	}

	private void teleportCheck() {
		for (MapObject object : objects) {
			String type = object.getProperties().get("type", String.class);
			String map_name = object.getName();
			if (type == null) {
				continue;
			}
			if (type.equals("teleport")) {
				Rectangle rectangle_teleport_region = ((RectangleMapObject) object).getRectangle();
				if (zhanTingyun.getRectangle().overlaps(rectangle_teleport_region)) {
					//game.getFadeTransition().setOverlay(Color.BLACK);
					//game.changeScreenWithFade(0.6f);
					//try {
					//	Thread.sleep(150);
					//} catch (InterruptedException e) {
					//	e.printStackTrace();
					//}
					float previous_scale = scale;
					updateMapData(map_name, object, previous_scale);
					break;

				}
			}

		}
	}

	private void updateMapData(String map_name, MapObject object, float previous_scale) {
		DialogManager.loadDialogues(map_name.substring(3));
		renderer = mapsManager.getRenderer(object.getName());
		scale = MapsManager.scale;
		
		map = MapsManager.map;
		objects = MapsManager.objects;
		updateStage(previous_scale);
		collision = MapsManager.collision;
	}

    //主要用于单一场景
	private void updateCamera() {
		calculateCameraPosition();
        float x = Interpolation.smooth.apply(camera.position.x, cameraPosition[0], 0.3f);
        float y = Interpolation.smooth.apply(camera.position.y, cameraPosition[1], 0.3f);
        camera.position.set(x, y, 0);
		camera.update();
	}
    
    //为大幅度画面转换使用
    private void updateCamera1() {
        calculateCameraPosition();
        camera.position.set(cameraPosition[0], cameraPosition[1], 0);
		camera.update();
	}
    
    private void calculateCameraPosition() {
    	float min_x = Constants.VISION_WIDTH / 2f;
		float max_x = MapsManager.mapPixelWidth * scale - min_x;
		float min_y = 930 / 2f;
		float max_y = MapsManager.mapPixelHeight * scale - 1050 / 2f;
		float camera_x = MathUtils.clamp(zhanTingyun.getX(), min_x, max_x);
		float camera_y = MathUtils.clamp(zhanTingyun.getY(), min_y, max_y);
        cameraPosition[0] = camera_x;
        cameraPosition[1] = camera_y;
    }
	@Override
	public void resize(int p, int p1) {
		mainViewport.update(p, p1, true);
		uiViewport.update(p, p1);
	}

	@Override
	public void resume() {

	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(game.getTransStage());
	}

}

