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
        plrSprite.setPosition(0,h/2);

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
        plrFixture.density = 1f;
        //elasticity
        plrFixture.restitution = 0.08f;
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


    Vector2 plrBodyScreenPosV2 = new Vector2(0,0);
    Vector2 touchScreenPosV2 = new Vector2(0,0);



    //  **************    UPDATE
    public void update() {

        this.plrBody.applyLinearImpulse(touchScreenPosV2, plrBodyScreenPosV2, true);



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


    //todo calculate force vector to move in specific direction
    //   https://gamedev.stackexchange.com/questions/127640/how-to-calculate-move-forward-direction-in-libgdx-with-box2d-body
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //  plrBody.applyForce(100000f, 100f, screenX, screenY, true);
        // plrBody.applyTorque(400000f, true);
        //force in pixels/s
        // plrBody.applyForceToCenter(000f, 1000000000f, true);
        //
        touchX = Gdx.input.getX();
        touchY = h - Gdx.input.getY();
        //d>  Gdx.app.log("tagGdx", "touchXY " + touchX +" "+ touchY);
        Vector3 touchScreenPos = new Vector3(touchX, touchY, 0);
        //d>  Gdx.app.log("tagGdx", "Vector3touchScreenPosXY " + touchScreenPos.x + touchScreenPos.y);
        //d>Gdx.app.log("tag", "sprite get xy: "+ plrSprite.getX()+ " "+plrSprite.getY());
        //camera.unproject(touchScreenPos);
        // Vector3 touchWorldPos = stage.getCamera().unproject(touchScreenPos);
        Vector3 touchScreenPosTemp = new Vector3(touchX, touchY, 0);
        touchWorldPos = new Vector3(camera.unproject(touchScreenPosTemp));
        //d> Gdx.app.log("tagGdx", "Vector3touchScreenPosXYafterUnproj " + touchScreenPos.x + touchScreenPos.y);
//https://badlogicgames.com/forum/viewtopic.php?f=11&t=20536


        Vector2 vel = this.plrBody.getLinearVelocity();
        //Vector2 plrBody = this.plrBody.getPosition();
        Vector3 plrBodyWorldPos = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        //project body coordinates in the world to coordinates on the screen
        Vector3 plrBodyWorldPosTemp = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        Vector3 plrBodyScreenPos = new Vector3(camera.project(plrBodyWorldPosTemp));
        // Gdx.app.log("tag", "touchScreenPos=plrBodyScreenPos.y  " + touchScreenPos.y + "=" + plrBodyScreenPos.y);

        int straightMargin = (int) plrSprite.getHeight() + 20;
        plrBodyScreenPosV2 = new Vector2(plrBodyScreenPos.x, plrBodyScreenPos.y);
        touchScreenPosV2 = new Vector2(0, 0);
        // CCC  ---  down Y
        if ((touchScreenPos.y < plrBodyScreenPos.y-(straightMargin/2))) {
            Gdx.app.log("tag", "down Y  ");
            touchScreenPosV2.y = -((h - touchScreenPos.y));
            if ((touchScreenPos.x < plrBodyScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPos.x;
            } else if ((touchScreenPos.x < plrBodyScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPos.x;
            }
            // CCC  ---  up Y
        } else if (touchScreenPos.y > plrBodyScreenPos.y+(straightMargin/2)) {
            Gdx.app.log("tag", "up Y  ");
            touchScreenPosV2.y = (touchScreenPos.y);
            if ((touchScreenPos.x < plrBodyScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPos.x;
            } else if ((touchScreenPos.x > plrBodyScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPos.x;
            }
        }
        // CCC  ---  constant Y
        else if(touchScreenPos.y >= plrBodyScreenPos.y-(straightMargin/2) && touchScreenPos.y <= plrBodyScreenPos.y+(straightMargin/2)){
            Gdx.app.log("tag", "constant Y  ");
            touchScreenPosV2.y = (0);
            if ((touchScreenPos.x < plrBodyScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPos.x;
            } else if ((touchScreenPos.x > plrBodyScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPos.x;
            }
        }
        Vector2 plrBodyWorldPosV2 = new Vector2(plrBodyWorldPos.x, plrBodyWorldPos.y);
        Gdx.app.log("tagGdx", "Vector2touchscreenpos " + touchScreenPosV2);
        /*
        //todo this control has only 4 fixed directions if done right
        //todo: -draw proper direction vector OR -add dead zone for directions up,down,left,right
        // we are comparing in in screenCoords (project bodyPos to screenPos)
// apply left impulse, but only if max velocity is not reached yet
        if (touchScreenPos.x < plrBodyScreenPos.x) {
            // apply up impulse, but only if max velocity is not reached yet
            if ((touchScreenPos.y < plrBodyScreenPos.y)) {
                this.plrBody.applyLinearImpulse(-800f, 0, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                this.plrBody.applyLinearImpulse(0, -800f, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                Gdx.app.log("tag", "left and down");
            }
            // apply down impulse, but only if max velocity is not reached yet
            if ((touchScreenPos.y > plrBodyScreenPos.y)) {
                this.plrBody.applyLinearImpulse(-800f, 0, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                this.plrBody.applyLinearImpulse(0, 800f, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                Gdx.app.log("tag", "left and up");
            }
        }

// apply right impulse, but only if max velocity is not reached yet
        // if ((dragX > pos.x) && vel.x < MAX_PLR_VELOCITY) {
       else if ((touchScreenPos.x > plrBodyScreenPos.x)) {
            // apply up impulse, but only if max velocity is not reached yet
            if ((touchScreenPos.y < plrBodyScreenPos.y)) {
                this.plrBody.applyLinearImpulse(800f, 0, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                this.plrBody.applyLinearImpulse(0, -800f, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                Gdx.app.log("tag", "right and down");
            }

            // apply down impulse, but only if max velocity is not reached yet
            if ((touchScreenPos.y > plrBodyScreenPos.y)) {
                this.plrBody.applyLinearImpulse(800f, 0, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                this.plrBody.applyLinearImpulse(0, 800f, plrBodyScreenPos.x, plrBodyScreenPos.y, true);
                Gdx.app.log("tag", "right and up");
            }
        }

         */
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    float dragX, dragY;

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
//todo draw Dragg point
        dragX = Gdx.input.getX();
        dragY = Gdx.input.getY();
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
