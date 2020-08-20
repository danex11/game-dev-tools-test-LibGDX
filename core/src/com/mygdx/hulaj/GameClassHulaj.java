package com.mygdx.hulaj;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import sun.rmi.runtime.Log;

//todo set lvl no


public class GameClassHulaj extends ApplicationAdapter implements InputProcessor {
    //..... screen width and height
    int hpx;
    int wpx;

    float hm, wm;

    // .. debugger
    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;
    // .. camera
    OrthographicCamera camera;
    // .. world
    World world;
    // .. physics
    //box2d world is something very small and does not depends on device screen size,
    //sprite is 32 so scaled by 16 it will be 2meters
    final static int PPM = 128;
    //final float PIXELS_TO_METERS = 16f;
    final short PLAYER = 00000001;
    final short SOLID = 00000010;
    SpriteBatch batch;

    // .. deb>
    TextureRegion backgroundTexture;
    BitmapFont font;

    //  http://www.pixnbgames.com/blog/libgdx/how-to-use-libgdx-tiled-drawing-with-libgdx/
    // .. Tiled map
    private TiledMap map;
    private AssetManager manager;
    // Map properties
    private int tileWidth, tileHeight,
            mapWidthInTiles, mapHeightInTiles,
            mapWidthInPixels, mapHeightInPixels;
    private OrthogonalTiledMapRenderer renderer;
    private ShapeRenderer shRend;
    int unitScaleMap;

    //...Sprites
    //plr
    Texture plrImg;
    Sprite plrSprite;
    Body plrBody;
    int MAX_PLR_VELOCITY = 400;

    //deb>
    //touchsprite
    Sprite touchSprite;
    //edge bottom
    Body bodyEdgeBottom;
    Body bodyEdgeTop;


    @Override
    public void create() {
        //----------------Screen
        //ScreeenSize in Units
        wpx = Gdx.graphics.getWidth();
        hpx =  (Gdx.graphics.getHeight());
        wm = wpx/PPM;
        hm = hpx/PPM;
        //-----------------Controls
        Gdx.input.setInputProcessor(this);
        //-----------------World
        Box2D.init();
        world = new World(new Vector2(0, 0f), true);
        //---------------TiledMap
        manager = new AssetManager();
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("maps/csvprawydol.tmx", TiledMap.class);
        manager.finishLoading();
        shRend = new ShapeRenderer();

        map = manager.get("maps/csvprawydol.tmx", TiledMap.class);

        // Read properties
        MapProperties properties = map.getProperties();
        tileWidth = properties.get("tilewidth", Integer.class);
        tileHeight = properties.get("tileheight", Integer.class);
        mapWidthInTiles = properties.get("width", Integer.class);
        mapHeightInTiles = properties.get("height", Integer.class);
        mapWidthInPixels = mapWidthInTiles * tileWidth;
        mapHeightInPixels = mapHeightInTiles * tileHeight;
        // Set up the camera viewport
        // >>>   CAMERA VIEWPOINT
        //we should be using this camera width and height instead of h and w
        camera = new OrthographicCamera(wpx , hpx );
        //camera.setToOrtho(false, w, h);
        ////camera.position.x = mapWidthInPixels;// * .5f;
        ////camera.position.y = mapHeightInPixels;
        //give @unitScale to map renderer
        // new OrthogonalTiledMapRenderer(map, 1 / 40f)
        unitScaleMap = 2;
        renderer = new OrthogonalTiledMapRenderer(map, (1 / (float) unitScaleMap)/PPM);


        //-----------------SpriteBatch
        batch = new SpriteBatch();


        //todo scaling to metrs
        //  https://www.codeandweb.com/texturepacker/tutorials/libgdx-physics
        /*
         *********************** PLR PLR PLR PLR PLR PLR PLR PLR
         */
        //plr Sprite
        plrImg = new Texture("sprite_actor.png");
        plrSprite = new Sprite(plrImg);
        //deb>
        Gdx.app.log("tagGdx", "spriteHeight " + plrSprite.getHeight());
        touchSprite = new Sprite(plrImg);
        plrSprite.setPosition(0, hpx / 4);

        //plr Body - and set its position to this of plrSprite
        BodyDef plrBodyDef = new BodyDef();
        plrBodyDef.bullet = true;
        plrBodyDef.type = BodyDef.BodyType.DynamicBody;
        plrBodyDef.position.set((plrSprite.getX() + plrSprite.getWidth() / 2)/PPM,
                (plrSprite.getY() + plrSprite.getHeight() / 2)/PPM);
        plrBody = world.createBody(plrBodyDef);

        //plr Fixture - and attach all this parameters to plrBody
        FixtureDef plrFixture = new FixtureDef();
        PolygonShape plrShape = new PolygonShape();
        plrShape.setAsBox((plrSprite.getWidth() / 2/PPM), (plrSprite.getHeight() / 2/PPM));
        plrFixture.shape = plrShape;
        plrFixture.density = 1f;
        //elasticity
        plrFixture.restitution = 0f;
        //what I am
        plrFixture.filter.categoryBits = PLAYER;
        //what I collide with
        plrFixture.filter.maskBits = SOLID;
        plrBody.createFixture(plrFixture);
        //no rotation
        plrBody.setFixedRotation(true);
        plrShape.dispose();

        //deb>
        //Gdx.app.log("tagGdx", "bodyHeight " + plrShape.);


        /*
         ******************************* EDGES
         */
        // --- edge shape
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(0, 0, 10 * wpx, 1);


        //--TOP Edge
        BodyDef bodyDefEdgeTop = new BodyDef();
        bodyDefEdgeTop.type = BodyDef.BodyType.StaticBody;
        bodyDefEdgeTop.position.set(0 - wpx / 2, hpx - 10);
        FixtureDef fixtureEdgeTop = new FixtureDef();
        fixtureEdgeTop.filter.categoryBits = SOLID;
        fixtureEdgeTop.filter.maskBits = PLAYER;
        fixtureEdgeTop.shape = edgeShape;
        bodyEdgeTop = world.createBody(bodyDefEdgeTop);
        bodyEdgeTop.createFixture(fixtureEdgeTop);

        //--- Bottom Edge
        BodyDef bodyDefEdgeBottom = new BodyDef();
        bodyDefEdgeBottom.type = BodyDef.BodyType.StaticBody;
        bodyDefEdgeBottom.position.set(0 - wpx / 2, 0);
        FixtureDef fixtureEdgeBottom = new FixtureDef();
        fixtureEdgeBottom.filter.categoryBits = SOLID;
        fixtureEdgeBottom.filter.maskBits = PLAYER;
        fixtureEdgeBottom.shape = edgeShape;
        bodyEdgeBottom = world.createBody(bodyDefEdgeBottom);
        bodyEdgeBottom.createFixture(fixtureEdgeBottom);


        //dispose of shape after creation
        edgeShape.dispose();

        //debugger
        debugMatrix = batch.getProjectionMatrix().cpy().scale(PPM, PPM, 1);
        debugRenderer = new Box2DDebugRenderer();
        //camera
        //camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //update camera
        //camera.update();

        //deb>
        backgroundTexture = new TextureRegion(new Texture("background.png"), 0, 0, 2048, 563);
    }



    //
    //
    /**
     * RENDERER
     */
    @Override
    public void render() {
        //update
        update();

        //background
        Gdx.gl.glClearColor((float) 0.2, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // plrSprite  - set the sprite's position(that is with texture picture)
        // from the updated physics body plrBody location
        /*
        plrSprite.setPosition((plrBody.getPosition().x - plrSprite.getWidth() / 2)*PPM,
                (plrBody.getPosition().y - plrSprite.getHeight() / 2)*PPM);

         */


        // render Tiles
        renderer.render();


        // set camera to follow  plr
        /*
        camera.position.set(
                plrSprite.getX() + wpx / 2,
                //plrSprite.getY(),
                hpx / 2,
                0
        );
         */
        camera.position.set(
          touchX,//+Gdx.input.getDeltaX(),
          touchY,//+Gdx.input.getDeltaY(),
          0
        );

        //so that this rendered ObjectTiles move with camera
        shRend.setProjectionMatrix(camera.combined);
        //render Object layers of Tiles

        for (MapObject object : map.getLayers().get("solids").getObjects()) {
            // if (object instanceof RectangleMapObject) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            shRend.begin(ShapeRenderer.ShapeType.Line);
            shRend.setColor(Color.WHITE);
            shRend.rect((rect.x) / unitScaleMap, (rect.y) / unitScaleMap, rect.width / unitScaleMap, rect.height / unitScaleMap);
            shRend.end();
            //  }
        }


        for (MapObject object : map.getLayers().get("evilground").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                shRend.begin(ShapeRenderer.ShapeType.Line);
                shRend.setColor(Color.GRAY);

                shRend.rect((rect.x) / unitScaleMap, (rect.y) / unitScaleMap, rect.width / unitScaleMap, rect.height / unitScaleMap);

                shRend.end();
            }
        }


        plrSprite.setPosition((plrBody.getPosition().x *PPM),
                (plrBody.getPosition().y *PPM));
        //plrSprite - set the sprite's rotation
        plrSprite.setRotation((float) Math.toDegrees(plrBody.getAngle()));

        batch.setProjectionMatrix(camera.combined);
        ////////////////////////
        // *************************************** BATCH BEGIN
        batch.begin();

        //deb>
        // test> batch.draw(backgroundTexture, 0, 0);
        //I believe texture region takes the upper left corner as 0,0 and batch.Draw the bottom left.
        //So you might need to do something like this:
        // test> batch.draw(backgroundTexture, 0, Gdx.graphics.getHeight());


        renderer.setView(camera);

        font = new BitmapFont(false);
        font.getData().setScale(2);
        font.draw(batch, "X zero", 0, 0);

        //plrSprite draw
        batch.draw(plrSprite,
                plrSprite.getX(), plrSprite.getY(),
                plrSprite.getOriginX(), plrSprite.getOriginY(),
                plrSprite.getWidth(), plrSprite.getHeight(),
                plrSprite.getScaleX(), plrSprite.getScaleY(),
                plrSprite.getRotation());
        if (touchWorldPos != null) {
            batch.draw(touchSprite, touchWorldPos.x, hpx - touchWorldPos.y);
        }

        batch.end();
        // ****** BATCH END
        camera.update();


        // debbuger
        debugRenderer.render(world, camera.combined);


        //update physics simulation
        //world.step(1f / 0.002f, 1, 100);
        //world.step(1 / 0.0001f, 1 / 100000000, 100000000);
        world.step(1 / 60f, 6, 2);
    }


    @Override
    public void dispose() {
        plrImg.dispose();
        batch.dispose();
        plrImg.dispose();
        manager.dispose();
        renderer.dispose();
    }


    //
    //
    //
    //
    //
    //
    Vector2 plrBodyScreenPosV2 = new Vector2(0, 0);
    Vector2 movementVector = new Vector2(0, 0);

    //  ************* UPDATE
    public void update() {
        if (goGoGo) {
            plrBodyScreenPosV2 = new Vector2(plrBody.getPosition().x, plrBody.getPosition().y);
           ///deb movementVector = new Vector2(controller8directions.moveVector(hpx, camera, touchScreenPosGdx, plrBodyScreenPosV2, plrSprite));
           ///deb Vector2 movementVectorScaled = new Vector2(movementVector.x, movementVector.y);
          ///deb  this.plrBody.applyLinearImpulse(movementVectorScaled, plrBodyScreenPosV2, true);

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
        touchY = hpx - Gdx.input.getY();
        touchWorldPos = new Vector3(touchX, touchY, 0);
        camera.unproject(touchWorldPos);

        ///////////////touchScreenPosGdx = new Vector3(touchX, touchY, 0);
        Gdx.app.log("tagGdx", "touchScreenPosGdx " + touchX + " " + touchY);
        Gdx.app.log("tagGdx", "touchWorldPos " + touchWorldPos);
         goGoGo = true;


        plrBody.setLinearVelocity(0f, 1f);

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
        dragY = hpx - Gdx.input.getY();
        touchScreenPosGdx = new Vector3(dragX, dragY, 0);
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
