package com.mygdx.hulaj;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;


import jdk.nashorn.internal.runtime.regexp.joni.Config;

import static java.lang.System.*;

//TODO commit to GitHub
//todo set lvl no


public class GameClassHulaj extends ApplicationAdapter implements InputProcessor {
    //..... screen width and height
    float h;
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
        //plrSprite.setPosition(0,0);

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
        plrFixture.density = 0.1f;
        //elasticity
        plrFixture.restitution = 0.5f;
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
    }


    @Override
    public void render() {
        //update physics simulation
        world.step(1f / 30f, 6, 10);

        //background
        Gdx.gl.glClearColor((float) 0.2, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // plrSprite  - set the sprite's position(that is with texture picture)
        // from the updated physics body plrBody location
        plrSprite.setPosition((plrBody.getPosition().x) - plrSprite.getWidth() / 2,
                (plrBody.getPosition().y) - plrSprite.getHeight() / 2);
        plrSprite.setRotation((float) Math.toDegrees(plrBody.getAngle()));


        // set camera to follow player
        camera.position.set(
                plrBody.getPosition().x,
                h / 2,
                0
        );
        camera.update();
        renderer.setView(camera);
        renderer.render();


        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        //plrSprite draw
        batch.draw(plrSprite,
                plrSprite.getX(), plrSprite.getY(),
                plrSprite.getOriginX(), plrSprite.getOriginY(),
                plrSprite.getWidth(), plrSprite.getHeight(),
                plrSprite.getScaleX(), plrSprite.getScaleY(),
                plrSprite.getRotation());
        batch.end();


        update();


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


    //  **************    UPDATE
    public void update() {


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
    float dragX;
    float dragY;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log("myTag", "touchDown");
        //  plrBody.applyForce(100000f, 100f, screenX, screenY, true);
        // plrBody.applyTorque(400000f, true);
        //force in pixels/s
        // plrBody.applyForceToCenter(000f, 1000000000f, true);
        dragX = Gdx.input.getX();
        dragY = Gdx.input.getY();


        Vector2 vel = this.plrBody.getLinearVelocity();
        Vector2 pos = this.plrBody.getPosition();

        //todo this control has only 8 fixed directions if done right - draw proper vector
// apply left impulse, but only if max velocity is not reached yet
        if (dragX < pos.x) {
            this.plrBody.applyLinearImpulse(-800f, 0, pos.x, pos.y, true);
            // apply up impulse, but only if max velocity is not reached yet
            if ((dragY < pos.y)) {
                this.plrBody.applyLinearImpulse(0, -800f, pos.x, pos.y, true);
            }

            // apply down impulse, but only if max velocity is not reached yet
            if ((dragY > pos.y)) {
                this.plrBody.applyLinearImpulse(0, 800f, pos.x, pos.y, true);
            }
        }

// apply right impulse, but only if max velocity is not reached yet
        // if ((dragX > pos.x) && vel.x < MAX_PLR_VELOCITY) {
        if ((dragX > pos.x)) {
            this.plrBody.applyLinearImpulse(800f, 0, pos.x, pos.y, true);
            // apply up impulse, but only if max velocity is not reached yet
            if ((dragY < pos.y)) {
                this.plrBody.applyLinearImpulse(0, -800f, pos.x, pos.y, true);
                Gdx.app.log("tag", "prghi and up");
            }

            // apply down impulse, but only if max velocity is not reached yet
            if ((dragY > pos.y)) {
                this.plrBody.applyLinearImpulse(0, 800f, pos.x, pos.y, true);
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
//todo draw Dragg point

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


}
