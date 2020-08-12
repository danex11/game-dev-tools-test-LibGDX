package com.mygdx.hulaj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

public class controller8directions {

    float touchX;
    float touchY;
    Vector3 touchWorldPos;
    Vector2 touchScreenPosV2;


    public static Vector2 moveVector(int h, Camera camera, Vector3 touchScreenPosGdx, Vector2 plrBodyScreenPosV2, Sprite plrSprite) {
        //  plrBody.applyForce(100000f, 100f, screenX, screenY, true);
        // plrBody.applyTorque(400000f, true);
        //force in pixels/s
        // plrBody.applyForceToCenter(000f, 1000000000f, true);
        //

        //d>  Gdx.app.log("tagGdx", "Vector3touchScreenPosXY " + touchScreenPos.x + touchScreenPos.y);
        //d>Gdx.app.log("tag", "sprite get xy: "+ plrSprite.getX()+ " "+plrSprite.getY());
        //camera.unproject(touchScreenPos);
        // Vector3 touchWorldPos = stage.getCamera().unproject(touchScreenPos);
        /////////////////////Vector3 touchScreenPosTemp = new Vector3(touchX, touchY, 0);
       ///////////////////// Vector3 touchWorldPos = new Vector3(camera.unproject(touchScreenPosTemp));
        //d> Gdx.app.log("tagGdx", "Vector3touchScreenPosXYafterUnproj " + touchScreenPos.x + touchScreenPos.y);
//https://badlogicgames.com/forum/viewtopic.php?f=11&t=20536


        //todo get either sprite or body position
        //Vector2 plrBody = this.plrBody.getPosition();
        //////////////Vector3 plrBodyWorldPos = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        //////////////Vector3 plrBodyWorldPos = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        //project body coordinates in the world to coordinates on the screen
        Vector3 plrSpriteWorldPosTemp = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        //////////////////Vector3 plrBodyWorldPosTemp = new Vector3(plrSprite.getX(), plrSprite.getY(), 0);
        Vector3 plrSpriteScreenPos = new Vector3(camera.project(plrSpriteWorldPosTemp));
        /////////////////Vector3 plrBodyScreenPos = new Vector3(camera.project(plrBodyWorldPosTemp));
        // Gdx.app.log("tag", "touchScreenPos=plrBodyScreenPos.y  " + touchScreenPos.y + "=" + plrBodyScreenPos.y);

        int straightMargin = (int) plrSprite.getHeight() + 20;
        ///////////////////plrBodyScreenPosV2 = new Vector2(plrSpriteScreenPos.x, plrSpriteScreenPos.y);
        Vector2 touchScreenPosV2 = new Vector2(0, 0);
        // CCC  ---  down Y
        if ((touchScreenPosGdx.y < plrSpriteScreenPos.y - (straightMargin / 2))) {
            ////////////////////////////Gdx.app.log("tag", "down Y  ");
            touchScreenPosV2.y = -((h - touchScreenPosGdx.y));
            if ((touchScreenPosGdx.x < plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPosGdx.x;
            } else if ((touchScreenPosGdx.x < plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPosGdx.x;
            }
            // CCC  ---  up Y
        } else if (touchScreenPosGdx.y > plrSpriteScreenPos.y + (straightMargin / 2)) {
            /////////////////////////Gdx.app.log("tag", "up Y  ");
            touchScreenPosV2.y = (touchScreenPosGdx.y);
            if ((touchScreenPosGdx.x < plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPosGdx.x;
            } else if ((touchScreenPosGdx.x > plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPosGdx.x;
            }
        }
        // CCC  ---  constant Y
        else if (touchScreenPosGdx.y >= plrSpriteScreenPos.y - (straightMargin / 2) && touchScreenPosGdx.y <= plrSpriteScreenPos.y + (straightMargin / 2)) {
            Gdx.app.log("tag", "constant Y  ");
            touchScreenPosV2.y = (0);
            if ((touchScreenPosGdx.x < plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = -touchScreenPosGdx.x;
            } else if ((touchScreenPosGdx.x > plrSpriteScreenPos.x)) {
                touchScreenPosV2.x = touchScreenPosGdx.x;
            }
        }
        //////////////////Vector2 plrBodyWorldPosV2 = new Vector2(plrBodyWorldPos.x, plrBodyWorldPos.y);
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
        return touchScreenPosV2;
    }
}