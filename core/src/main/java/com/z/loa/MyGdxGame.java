package com.z.loa;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.z.loa.manager.FontManager;
import com.z.loa.screen.*;
import com.z.loa.screen.StartupScene;
import com.z.loa.util.AndroidDeviceOverlay;

public class MyGdxGame extends Game {
    private FadeTransition fadeTransition;
    private Stage transStage;
    private AndroidDeviceOverlay performanceOverlay;

    @Override
    public void create() {
        this.transStage = new Stage();
        this.fadeTransition = new FadeTransition(transStage);
        this.setScreen(new StartupScene(this));
        performanceOverlay = new AndroidDeviceOverlay(transStage);
        performanceOverlay.setPosition(10, 0);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
        
        transStage.act(Gdx.graphics.getDeltaTime());
        transStage.draw();
        performanceOverlay.update();
    }

    public void changeScreenWithFade(final Screen newScreen, final float duration) {

        fadeTransition.fadeOut(
                duration / 2,
                new Runnable() {
                    @Override
                    public void run() {
                        if (screen != null) {
                            screen.hide();
                        }

                        setScreen(newScreen);
                        fadeTransition.fadeIn(duration / 2, null);
                    }
                });
    }

    public void changeScreenWithFade(final float duration) {

        fadeTransition.fadeOut(
                duration / 2,
                new Runnable() {
                    @Override
                    public void run() {
                        fadeTransition.fadeIn(duration / 2, null);
                    }
                });
    }

    public FadeTransition getFadeTransition() {
        return fadeTransition;
    }

    public Stage getTransStage() {
        return transStage;
    }

    public AndroidDeviceOverlay getPerformanceOverlay() {
        return performanceOverlay;
    }

    @Override
    public void dispose() {
        if(FontManager.font != null) {
            FontManager.disposeAll();
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
