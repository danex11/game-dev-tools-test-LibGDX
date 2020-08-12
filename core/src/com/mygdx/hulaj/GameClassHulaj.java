package com.mygdx.hulaj;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import sun.rmi.runtime.Log;

//todo set lvl no


public class GameClassHulaj extends ApplicationAdapter implements InputProcessor {
    //..... screen width and height
    int h;
    float w;

    // .. debugger
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;
    // .. camera
    OrthographicCamera camera;
    // .. world
    World world;
    // .. physics
    final short PLAYER = 00000001;
    final short SOLID = 00000010;
    SpriteBatch batch;

    // .. deb>
    TextureRegion backgroundTexture;

    //  http://www.pixnbgames.com/blog/libgdx/how-to-use-libgdx-tiled-drawing-with-libgdx/
    // .. Tiled map
    private TiledMap map;
    private AssetManager manager;
    // Map properties
    private int tileWidth, tileHeight,
            mapWidthInTiles, mapHeightInTiles,
            mapWidthInPixels, mapHeightInPixels;
    private OrthogonalTiledMapRenderer renderer;

    //...Sprites
    //plr
    Texture plrImg;
    Sprite plrSprite;
    Body plrBody;
    int MAX_PLR_VELOCITY = 800000000;

    //deb>
    //touchsprite
    Sprite touchSprite;
    //edge bottom
    Body bodyEdgeBottom;
    Body bodyEdgeTop;


    @Override
    public void create() {
        //----------------Screen
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        //-----------------Controls
        Gdx.input.setInputProcessor(this);
        //-----------------World
        world = new World(new Vector2(0, 0), true);
        //---------------TiledMap
        manager = new AssetManager();
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("maps/lvl0.tmx", TiledMap.class);
        manager.finishLoading();

        map = manager.get("maps/lvl0.tmx", TiledMap.class);

        // Read properties
        MapProperties properties = map.getProperties();
        tileWidth = properties.get("tilewidth", Integer.class);
        tileHeight = properties.get("tileheight", Integer.class);
        mapWidthInTiles = properties.get("width", Integer.class);
        mapHeightInTiles = properties.get("height", Integer.class);
        mapWidthInPixels = mapWidthInTiles * tileWidth;
        mapHeightInPixels = mapHeightInTiles * tileHeight;
        // Set up the camera
        camera = new OrthographicCamera(320.f, 180.f);
        camera.position.x = mapWidthInPixels * .5f;
        camera.position.y = mapHeightInPixels;
        renderer = new OrthogonalTiledMapRenderer(map);


        //-----------------SpriteBatch
        batch = new SpriteBatch();

        /*
         ********************************* PLR
         */
        //plr Sprite
        plrImg = new Texture("plr2.png");
        plrSprite = new Sprite(plrImg);
        //deb>
        touchSprite = new Sprite(plrImg);
        plrSprite.setPosition(0, h / 2);

        //plr Body - and set its position to this of plrSprite
        BodyDef plrBodyDef = new BodyDef();
        plrBodyDef.type = BodyDef.BodyType.DynamicBody;
        plrBodyDef.position.set((plrSprite.getX() + plrSprite.getWidth() / 2),
                (plrSprite.getY() + plrSprite.getHeight() / 2));
        plrBody = world.createBody(plrBodyDef);

        //plr Fixture - and attach all this parameters to plrBody
        FixtureDef plrFixture = new FixtureDef();
        PolygonShape plrShape = new PolygonShape();
        plrShape.setAsBox(plrSprite.getWidth() / 2, plrSprite.getHeight() / 2);
        plrFixture.shape = plrShape;
        plrFixture.density = 10f;
        //elasticity
        plrFixture.restitution = 100f;
        //what I am
        plrFixture.filter.categoryBits = PLAYER;
        //what I collide with
        plrFixture.filter.maskBits = SOLID;

        plrBody.createFixture(plrFixture);
        plrShape.dispose();




        /*
         ******************************* EDGES
         */
        // --- edge shape
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(0, 0, 10 * w, 0);

        //--- Bottom Edge
        BodyDef bodyDefEdgeBottom = new BodyDef();
        bodyDefEdgeBottom.type = BodyDef.BodyType.StaticBody;
        bodyDefEdgeBottom.position.set(0 - w / 2, 0);
        FixtureDef fixtureEdgeBottom = new FixtureDef();
        fixtureEdgeBottom.filter.categoryBits = SOLID;
        fixtureEdgeBottom.filter.maskBits = PLAYER;
        fixtureEdgeBottom.shape = edgeShape;
        bodyEdgeBottom = world.createBody(bodyDefEdgeBottom);
        bodyEdgeBottom.createFixture(fixtureEdgeBottom);

        //--TOP Edge
        BodyDef bodyDefEdgeTop = new BodyDef();
        bodyDefEdgeTop.type = BodyDef.BodyType.StaticBody;
        bodyDefEdgeTop.position.set(0 - w / 2, h - 1);
        FixtureDef fixtureEdgeTop = new FixtureDef();
        fixtureEdgeTop.filter.categoryBits = SOLID;
        fixtureEdgeTop.filter.maskBits = PLAYER;
        fixtureEdgeTop.shape = edgeShape;
        bodyEdgeTop = world.createBody(bodyDefEdgeTop);
        bodyEdgeTop.createFixture(fixtureEdgeTop);

        //dispose of shape after creation
        edgeShape.dispose();

        //debugger
        debugMatrix = batch.getProjectionMatrix().cpy().scale(1, 1, 1);
        debugRenderer = new Box2DDebugRenderer();
        //camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //update camera
        //camera.update();

        //deb>
        backgroundTexture = new TextureRegion(new Texture("background.png"), 0, 0, 2048, 563);
    }


    @Override
    public void render() {
        //update physics simulation
        world.step(1f / 2f, 1, 1);

        //update
        update();

        //background
        Gdx.gl.glClearColor((float) 0.2, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // plrSprite  - set the sprite's position(that is with texture picture)
        // from the updated physics body plrBody location
        plrSprite.setPosition((plrBody.getPosition().x) - plrSprite.getWidth() / 2,
                (plrBody.getPosition().y) - plrSprite.getHeight() / 2);
        //plrSprite - set the sprite's rotation
        plrSprite.setRotation((float) Math.toDegrees(plrBody.getAngle()));
//deb>
/*
        // set camera to follow player
        camera.position.set(
                plrBody.getPosition().x,
                h / 2,
                0
        );


 */


        // render Tiles
        //test> renderer.render();


        batch.setProjectionMatrix(camera.combined);
        // ************************************************ BATCH BEGIN
        batch.begin();

        //deb>
        batch.draw(backgroundTexture, 0, 0);
        //I believe texture region takes the upper left corner as 0,0 and batch.Draw the bottom left.
        //So you might need to do something like this:
        batch.draw(backgroundTexture, 0, Gdx.graphics.getHeight());

        //plrSprite draw
        batch.draw(plrSprite,
                plrSprite.getX(), plrSprite.getY(),
                plrSprite.getOriginX(), plrSprite.getOriginY(),
                plrSprite.getWidth(), plrSprite.getHeight(),
                plrSprite.getScaleX(), plrSprite.getScaleY(),
                plrSprite.getRotation());
        if (touchWorldPos != null) {
            batch.draw(touchSprite, touchWorldPos.x, h - touchWorldPos.y);
        }
        //deb>
        // set camera to follow touch point
        camera.position.set(
                plrSprite.getX() + w / 2 - 200,
                h / 2,
                0
        );
        batch.end();
        // ****** BATCH END
        camera.update();
        renderer.setView(camera);


        // debbuger
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void dispose() {
        plrImg.dispose();
        batch.dispose();
        plrImg.dispose();
        manager.dispose();
    }


    Vector2 plrBodyScreenPosV2 = new Vector2(0, 0);
    Vector2 movementVector = new Vector2(0, 0);


    //  **************    UPDATE
    public void update() {
        if (goGoGo) {
            movementVector = new Vector2(controller8directions.moveVector(h, camera, touchScreenPosGdx, plrBodyScreenPosV2, plrSprite));
            this.plrBody.applyLinearImpulse(movementVector, plrBodyScreenPosV2, true);

        } else if (!goGoGo) {
            /*
            //todo if velocity !=0 apply counterforce
            Vector2 velocityPlr = new Vector2(plrBody.getLinearVelocity());
            if (velocityPlr.x > 0) {
                movementVector = new Vector2(-movementVector.x, -movementVector.y);
            } else {
                movementVector = new Vector2(0, 0);
            }
            this.plrBody.applyLinearImpulse(movementVector, plrBodyScreenPosV2, true);
            //movementVector = new Vector2(-movementVector.x / 2, -movementVector.y / 2);

             */
        }

    }


    // *********************** INPUT CONTROLLERS
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


    //ccccccccccccccccccc Android methods
    float touchX;
    float touchY;
    Vector3 touchWorldPos;
    ////////////Vector2 movementVector;
    boolean goGoGo;
    Vector3 touchScreenPosGdx;

    //todo calculate force vector to move in specific direction
    //   https://gamedev.stackexchange.com/questions/127640/how-to-calculate-move-forward-direction-in-libgdx-with-box2d-body
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchX = Gdx.input.getX();
        touchY = h - Gdx.input.getY();
        touchScreenPosGdx = new Vector3(touchX, touchY, 0);
        Gdx.app.log("tagGdx", "touchScreenPosGdx " + touchScreenPosGdx);
        //goGoGo = true;
        return true;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        goGoGo = false;
        return false;
    }


    float dragX, dragY;

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
//todo draw Dragg point
        dragX = Gdx.input.getX();
        dragY = h - Gdx.input.getY();
        ////////touchScreenPosGdx = new Vector3(dragX, dragY, 0);
        Gdx.app.log("tagGdx", "dragScreenPosGdx " + touchScreenPosGdx);
        goGoGo = true;
        return false;
    }


    //mmmmmmmmmmmmmmmmmmm mouse methods
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }


}
