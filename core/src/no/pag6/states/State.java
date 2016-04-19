package no.pag6.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import no.pag6.game.PAG6Game;
import no.pag6.helpers.AssetLoader;
import no.pag6.helpers.Constants;

public class State implements Screen, InputProcessor, Constants {

    public static final String TAG = "State";

    protected PAG6Game game;
    protected OrthographicCamera cam;
    protected Viewport viewPort;
    protected Vector3 projected, touchPoint = new Vector3(0, 0, 0);

    // Background
    private Sprite background;

    public State(PAG6Game game) {
        this.game = game;
        cam = new OrthographicCamera();
        viewPort = new FitViewport(V_WIDTH, V_HEIGHT, cam);
        cam.position.set(V_WIDTH/2, V_HEIGHT/2, 0);

        initBackground();
    }

    @Override
    public void show() {
        Gdx.app.log(TAG, "show called");

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.app.log(TAG, "render called");

        // Render background
        drawBackground();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log(TAG, "resize called");
        viewPort.update(width, height);
    }

    @Override
    public void pause() {
        Gdx.app.log(TAG, "pause called");
    }

    @Override
    public void resume() {
        Gdx.app.log(TAG, "resume called");
    }

    @Override
    public void hide() {
        Gdx.app.log(TAG, "hide called");
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose called");
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void update(float delta) {
        Gdx.app.log(TAG, "update called");
    }

    private void initBackground() {
        TextureRegion region = AssetLoader.background;
        float regionWidth = region.getRegionWidth();
        float regionHeight = region.getRegionHeight();
        background = new Sprite(region);
        background.setSize(V_WIDTH, V_HEIGHT);
        background.setPosition(0, 0);
    }

    private void drawBackground(){
        background.draw(game.spriteBatch);
    }

}
