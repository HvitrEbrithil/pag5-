package no.pag6.states;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;


import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import com.badlogic.gdx.input.GestureDetector;

import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sun.prism.image.ViewPort;

import com.badlogic.gdx.utils.Array;


import no.pag6.game.PAG6Game;
import no.pag6.helpers.MyContactListener;
import no.pag6.helpers.MyGestureListener;
import no.pag6.models.Player;
import no.pag6.tweenaccessors.Value;
import no.pag6.tweenaccessors.ValueAccessor;
import no.pag6.ui.SimpleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayState extends State {

    //Input-handling
    private InputMultiplexer inputMultiplexer;

    private float playTime = 0.0f;
    private float countdownTime = 3.5f;
    private boolean startSoundPlayed = false;
    private boolean inGameMusicPlaying = false;
    private Music activeInGameMusic = al.inGameEasyMusic;

    // Player stats
    private int nofPlayers;

    private Player[] players;
    private List<String> playerNames;
    private int activePlayerIdx;

    // map stuff
    private String mapFileName;
    private TiledMap map;
    private float mapDifficulty = 1.0f;
    private OrthogonalTiledMapRenderer mapRenderer;

    // box2d stuff
    private World world;
    private Box2DDebugRenderer b2dr;
    private MyContactListener cl;

    // Renderers
    private TweenManager tweener;


    // Tween assets
    private Value opacityLayer1 = new Value();
    private Value opacityLayer2 = new Value();
    private Value cameraZoom = new Value();

    //Game assets

    private GlyphLayout gl = new GlyphLayout();
    private BitmapFont countDownNameFont;
    private BitmapFont playerStatsFont;

    // Game UI


    private Label[] scoreLabels;
    private Label[] playerNameLabels;
    private Label scoreLabel;
    private Label playerNameLabel;
    private Label numberOfLives;
    private Table UItable;
    private Stage uiStage;

    float tempUIScale = .2f/PPM;

    private Button pauseButton;




    public PlayState(PAG6Game game, int nofPlayers, List<String> playerNames, String mapFileName) {
        super(game);
        this.nofPlayers = nofPlayers;
        this.playerNames = playerNames;
        this.mapFileName = mapFileName;

        players = new Player[nofPlayers];
        activePlayerIdx = 0;

        viewport.setWorldSize(A_WIDTH, A_HEIGHT);
        // load the map
        loadMap(mapFileName);

        // set up box2d
        world = new World(GRAVITY, true);
        b2dr = new Box2DDebugRenderer();
        cl = new MyContactListener();
        world.setContactListener(cl);
        addMapBodies();
        addPlayerBodies();

        players[activePlayerIdx].active = true;
        cl.setPlayer(players[activePlayerIdx]);

        initTweenAssets();
        initGameObjects();
        initGameAssets();

        //UI view-stage
        FitViewport uiViewPort = new FitViewport(V_WIDTH, V_HEIGHT, new OrthographicCamera());
        uiStage = new Stage(uiViewPort, super.game.spriteBatch);
        initUI();

        // Set in game music
        if (al.getMusicOn()) {
            setInGameMusic(mapFileName);
        }
    }

    @Override
    public void show() {
        // Add gesture listener

        InputProcessor inputProcessorOne = new GestureDetector(new MyGestureListener(this));
        InputProcessor inputProcessorTwo = this;
        inputMultiplexer = new InputMultiplexer();
        //UI-stage receives input first when its added first to the multiplexer
        inputMultiplexer.addProcessor(uiStage);
        inputMultiplexer.addProcessor(inputProcessorOne);
        inputMultiplexer.addProcessor(inputProcessorTwo);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        update(delta);

        // Clear drawings
        Gdx.gl.glClearColor(208 / 255f, 244 / 255f, 247 / 255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawTiled();

        // Render sprites
        game.spriteBatch.setProjectionMatrix(cam.combined);
        game.spriteBatch.begin();
        game.spriteBatch.enableBlending();

        for (Player player : players) {
            if (player != players[activePlayerIdx]) {
                game.spriteBatch.setColor(1, 1, 1, .2f);
                player.draw(game.spriteBatch);
                game.spriteBatch.setColor(1, 1, 1, 1);
            } else {
                player.draw(game.spriteBatch);
            }
        }

        // This should be started when game starts and in case of player change
        if (!startSoundPlayed && al.getSoundOn()) {
            al.countdownSound.play(0.5f);
            startSoundPlayed = true;
        }

        if (!inGameMusicPlaying && al.getMusicOn()) {
            activeInGameMusic.play();
            inGameMusicPlaying = true;
        }

        if (playTime < countdownTime) {
            game.spriteBatch.draw(al.countAnimation.getKeyFrame(playTime), cam.position.x - A_WIDTH/2, cam.position.y - A_HEIGHT/2, A_WIDTH, A_HEIGHT);

            // TODO: Set playername of the active player on Countdown screen (what is correct x- and y-value?)
            gl.setText(countDownNameFont, players[activePlayerIdx].getName().toUpperCase());
            countDownNameFont.draw(game.spriteBatch, gl, 0, 0);
        }

        game.spriteBatch.end();


        game.spriteBatch.setProjectionMatrix(uiStage.getCamera().combined);
        uiStage.draw();


        // TODO: Remove before release

        b2dr.render(world, cam.combined);

    }

    @Override
    public void update(float delta) {
        super.update(delta);

        playTime += delta;

        tweener.update(delta);

        if (playTime > countdownTime) {
            world.step(TIME_STEP, 6, 2); // update physics
        }

        // update camera
        Vector2 playerPos = players[activePlayerIdx].getB2dBody().getPosition();
        if (playerPos.x < A_WIDTH / 2) {
            cam.position.x = A_WIDTH / 2;
        } else {
            cam.position.x = playerPos.x; // center the camera around the activePlayer
        }
        if (playerPos.y < A_HEIGHT * 1.8f) {
            cam.position.y = A_HEIGHT * 1.8f;
        } else {
            cam.position.y = playerPos.y; // center the camera around the activePlayer
        }
        cam.update();

        // update the players
        for (Player player : players) {
            player.update(delta);
        }




        scoreLabel.setText("   " + players[activePlayerIdx].getScore());
        numberOfLives.setText("   " + players[activePlayerIdx].getNofLives());
        playerNameLabel.setText("   " + players[activePlayerIdx].getName());




        // Update UI


        // Layer-change
        map.getLayers().get(FIRST_FIRST_GFX_LAYER_NAME).setOpacity(opacityLayer1.getValue());
        map.getLayers().get(FIRST_SECOND_GFX_LAYER_NAME).setOpacity(opacityLayer1.getValue());

        map.getLayers().get(SECOND_FIRST_GFX_LAYER_NAME).setOpacity(opacityLayer2.getValue());
        map.getLayers().get(SECOND_SECOND_GFX_LAYER_NAME).setOpacity(opacityLayer2.getValue());

        cam.zoom = cameraZoom.getValue();

        // update the Tiled map renderer
        mapRenderer.setView(cam);

        // check death
        if (players[activePlayerIdx].getB2dBody().getPosition().y < 0 && ! players[activePlayerIdx].isFinished()) {
            // TODO: Move this if-loop to where the real final death of a player occurs
            players[activePlayerIdx].kill();
            setActivePlayer();

            // Tween values
            opacityLayer1.setValue(1f);
            opacityLayer2.setValue(.5f);
            cameraZoom.setValue(1f);
        }

        // check finish
        if (players[activePlayerIdx].isFinished()) {
            System.out.println("finish");
            players[activePlayerIdx].setFinished(false);
            players[activePlayerIdx].setMap();
            setActivePlayer();
        }

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchPoint.set(screenX, screenY, 0);
        projected = viewport.unproject(touchPoint);

        //pauseButton.isTouchDown(projected.x, projected.y);

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        touchPoint.set(screenX, screenY, 0);
        projected = viewport.unproject(touchPoint);


        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Jump
        if (keycode == Input.Keys.SPACE && playTime > countdownTime) {
            players[activePlayerIdx].jump();
        }

        // Switch lanes
        if (keycode == Input.Keys.UP && players[activePlayerIdx].isOnFirstLane() && playTime > countdownTime) {
            players[activePlayerIdx].switchLanes();
            tweenLayers();
        } else if (keycode == Input.Keys.DOWN && !players[activePlayerIdx].isOnFirstLane() && playTime > countdownTime) {
            players[activePlayerIdx].switchLanes();
            tweenLayers();
        }

        // TODO: Remove before release
        // Restart PlayState
        if (keycode == Input.Keys.R) {
            game.getGameStateManager().setScreen(new PlayState(game, 3, Arrays.asList("SPILLER EN", "SPILLER TO", "SPILLER TRE"), mapFileName));
        }
        // Quit application
        if (keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE) {
            System.exit(0);
        }
        // Go to GameOver screen (should be placed where GameOverState is the argument of setScreen in PlayState)
        if (keycode == Input.Keys.G) {
            al.backgroundMusic.play();
            game.getGameStateManager().setScreen(new GameOverState(game, players));
        }

        return true;
    }

    public boolean isStarted() {
        return playTime > countdownTime;
    }

    public Player getActivePlayer() {
        return players[activePlayerIdx];
    }

    public void tweenLayers() {
        boolean playerIsOnFirstLane = players[activePlayerIdx].isOnFirstLane();
        if (!playerIsOnFirstLane) {
            Tween.to(opacityLayer1, -1, .5f)
                    .target(.5f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
            Tween.to(opacityLayer2, -1, .5f)
                    .target(1f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
            Tween.to(cameraZoom, -1, .5f)
                    .target(.9f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
        } else {
            Tween.to(opacityLayer1, -1, .5f)
                    .target(1f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
            Tween.to(opacityLayer2, -1, .5f)
                    .target(.5f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
            Tween.to(cameraZoom, -1, .5f)
                    .target(1f)
                    .ease(TweenEquations.easeOutQuad)
                    .start(tweener);
        }
    }

    private void loadMap(String mapFileName) {
        map = new TmxMapLoader().load("maps/" + mapFileName);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
    }

    private void setInGameMusic(String mapFileName) {
        activeInGameMusic.pause();

        if (mapFileName.equals(MAP_HARD_1_NAME)) {
            activeInGameMusic = al.inGameHardMusic;
        } else if (mapFileName.equals(MAP_MED_1_NAME)) {
            activeInGameMusic = al.inGameMediumMusic;
        } else {
            activeInGameMusic = al.inGameEasyMusic;
        }

        activeInGameMusic.play();
    }

    private void setActivePlayer() {
        playTime = 0.0f;

        players[activePlayerIdx].active = false;

        // Check if there are more players alive
        Boolean playersLeft = false;
        for (Player player : players) {
            if (player.getNofLives() > 0) {
                playersLeft = true;
                break;
            }
        }
        if (playersLeft) {
            activePlayerIdx = (activePlayerIdx + 1) % nofPlayers;
            Player activePlayer = players[activePlayerIdx];
            activePlayer.active = true;
            // check if needed??
            this.mapFileName = activePlayer.getMap();
            this.mapDifficulty = activePlayer.getMapDifficulty();
            map = new TmxMapLoader().load("maps/"+mapFileName);
            mapRenderer.setMap(map);
            Array<Body> bodies = new Array<Body>(world.getBodyCount());
            world.getBodies(bodies);
            for (Body body : bodies) {
                if (!(body.getUserData() instanceof Player)) {
                    world.destroyBody(body);
                }
            }

            world.destroyBody(players[activePlayerIdx].getB2dBody());
            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(INIT_PLAYER_POS_X / PPM, INIT_PLAYER_POS_Y / PPM);
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            Body playerBody = world.createBody(bodyDef);

            // body fixture
            CircleShape shape = new CircleShape();
            shape.setRadius(PLAYER_BODY_RADIUS / PPM);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.filter.maskBits = FIRST_LAYER_BITS | GOAL_LAYER_BITS; // the activePlayer starts in lane 1
            playerBody.createFixture(fixtureDef);
            shape.dispose();

            // add foot fixture
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setAsBox(13 / PPM, 3 / PPM, new Vector2(0, -13 / PPM), 0);
            fixtureDef.shape = polygonShape;
            fixtureDef.isSensor = true;
            fixtureDef.filter.maskBits = FIRST_LAYER_BITS;
            playerBody.createFixture(fixtureDef).setUserData("player" + players[activePlayerIdx].getId() + "foot");
            polygonShape.dispose();

            playerBody.setUserData(players[activePlayerIdx]);
            players[activePlayerIdx].setB2dBody(playerBody);

            addMapBodies();

            cl.setPlayer(players[activePlayerIdx]);

            if (al.getMusicOn()) {
                setInGameMusic(mapFileName);
            }

            al.countdownSound.play();

            if (nofPlayers > 1) {
                players[activePlayerIdx].incrementFootContactCount();
            }
        } else {
            activeInGameMusic.stop();
            game.getGameStateManager().setScreen(new GameOverState(game, players));
        }
    }

    private void initGameObjects() {
    }

    private void initGameAssets() {
    }

    private void initTweenAssets() {
        // Register Tween Assets
        Tween.registerAccessor(Value.class, new ValueAccessor());

        tweener = new TweenManager();

        opacityLayer1.setValue(1f);
        opacityLayer2.setValue(.5f);
        cameraZoom.setValue(1f);
    }

    private void initUI() {
        TextureRegion region;
        float regionWidth, regionHeight;

        // Buttons

        region = al.pauseButtonUp;

        regionWidth = region.getRegionWidth() * UI_SCALE;
        regionHeight = region.getRegionHeight() * UI_SCALE;
        pauseButton = new Button(new TextureRegionDrawable(al.pauseButtonUp), new TextureRegionDrawable(al.pauseButtonDown));
        //Detect input on pause-button
        pauseButton.addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                game.getGameStateManager().pushScreen(new PauseState(game));
                al.countdownSound.pause();
                activeInGameMusic.pause();
                inGameMusicPlaying = false;
                if(al.getMusicOn()){
                    al.backgroundMusic.play();
                }
            }
        });

        // Font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arialbd.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 38;
        parameter.color = Color.BLACK;
        countDownNameFont = generator.generateFont(parameter);
        generator.dispose();



        // Player score


        playerNameLabel = new Label("   " + playerNames.get(activePlayerIdx), new Label.LabelStyle(new BitmapFont(), Color.BLACK));
        scoreLabel = new Label("    " + players[activePlayerIdx].getScore(), new Label.LabelStyle(new BitmapFont(), Color.BLACK));
        numberOfLives = new Label("   " + players[activePlayerIdx].getNofLives(), new Label.LabelStyle(new BitmapFont(), Color.BLACK));

        playerNameLabel.setFontScale(FONT_SCALE);
        scoreLabel.setFontScale(FONT_SCALE);
        numberOfLives.setFontScale(FONT_SCALE);

        UItable = new Table();
        UItable.top();
        UItable.setFillParent(true);
        UItable.add(playerNameLabel, scoreLabel, numberOfLives );
        UItable.add(pauseButton).right().height(regionWidth/2).width(regionWidth/2).expandX().padTop(10).padRight(50);
        UItable.setDebug(true);
        uiStage.addActor(UItable);



    }



    private void drawTiled() {
        mapRenderer.render();
    }


    private void addMapBodies() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        // loop through the layers in the map and add box2d bodies to the box2d world
        for (int i = 0; i < LAYERS.length; i++) {
            for (PolylineMapObject polylineMapObject : map.getLayers().get(LAYERS[i]).getObjects().
                    getByType(PolylineMapObject.class)) {
                Polyline line = polylineMapObject.getPolyline();

                float[] vertices = line.getTransformedVertices();
                Vector2[] worldVertices = new Vector2[vertices.length / 2];

                for (int j = 0; j < worldVertices.length; j++) {
                    worldVertices[j] = new Vector2(vertices[j * 2] / PPM, vertices[j * 2 + 1] / PPM);
                }

                ChainShape chainShape = new ChainShape();
                chainShape.createChain(worldVertices);

                bodyDef.position.set(
                        line.getOriginX() / PPM,
                        line.getOriginY() / PPM);

                body = world.createBody(bodyDef);
                fixtureDef.shape = chainShape;
                fixtureDef.filter.categoryBits = FILTER_BITS[i];
                if (LAYERS[i].equals(GOAL_COLLISION_NAME)) {
                    fixtureDef.isSensor = true;
                }
                Fixture fixture = body.createFixture(fixtureDef);
                if (LAYERS[i].equals(GOAL_COLLISION_NAME)) {
                    fixture.setUserData("goal");
                }
            }
        }

        shape.dispose();
    }

    private void addPlayerBodies() {
        for (int i = 0; i < nofPlayers; i++) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(INIT_PLAYER_POS_X / PPM, INIT_PLAYER_POS_Y / PPM);
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            Body playerBody = world.createBody(bodyDef);

            // body fixture
            CircleShape shape = new CircleShape();
            shape.setRadius(PLAYER_BODY_RADIUS / PPM);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.filter.maskBits = FIRST_LAYER_BITS | GOAL_LAYER_BITS; // the activePlayer starts in lane 1
            playerBody.createFixture(fixtureDef);
            shape.dispose();

            // add foot fixture
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setAsBox(13 / PPM, 3 / PPM, new Vector2(0, -13 / PPM), 0);
            fixtureDef.shape = polygonShape;
            fixtureDef.isSensor = true;
            fixtureDef.filter.maskBits = FIRST_LAYER_BITS;
            playerBody.createFixture(fixtureDef).setUserData("player" + i + "foot");
            polygonShape.dispose();

            players[i] = new Player(cam, playerBody, i, playerNames.get(i), i + 1);
            playerBody.setUserData(players[i]);
        }
    }
}
