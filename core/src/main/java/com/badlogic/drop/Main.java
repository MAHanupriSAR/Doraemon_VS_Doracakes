package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Vector2 previousTouchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    boolean isTouching;

    @Override
    public void create() {

        backgroundTexture = new Texture("room2.jpg");
        bucketTexture = new Texture("doraemonLeft.png");
        dropTexture = new Texture("doracake.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1.5f,2);
        touchPos = new Vector2();
        previousTouchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        music.setVolume(0.5f);
        music.play();
        isTouching = false;
    }

    @Override
    public void resize(int width, int height) {
        // Resize your application here. The parameters represent the new window size.
        viewport.update(width,height,true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    private void input(){
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            bucketSprite.setFlip(true,false);
            bucketSprite.translateX(speed*delta);
        }
        else if(Gdx.input.isKeyPressed((Input.Keys.LEFT))){
            bucketSprite.setFlip(false,false);
            bucketSprite.translateX(-speed*delta);
        }

        if (Gdx.input.isTouched()) {
            // Update current touch position
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            // If this is the first frame of touching
            if (!isTouching) {
                previousTouchPos.set(touchPos); // Set previous position to the current touch position
                isTouching = true; // Update touch state
            } else {
                // Determine the drag direction
                if (touchPos.x > previousTouchPos.x) {
                    bucketSprite.setFlip(true, false); // Flip to right
                } else if (touchPos.x < previousTouchPos.x) {
                    bucketSprite.setFlip(false, false); // Flip to left
                }
            }

            // Update bucket position
            bucketSprite.setCenterX(touchPos.x);
            previousTouchPos.set(touchPos); // Update previous position for the next frame
        } else {
            // Reset when not touching
            isTouching = false;
        }
    }

    private void logic(){
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth-bucketWidth));

        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        float dropSpeed = 4f;

//        for(Sprite dropSprite:dropSprites){
//            dropSprite.translateY(-speed*delta);
//        }
        for(int i=dropSprites.size-1; i>=0; i--){
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();
            dropSprite.translateY(-dropSpeed*delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);
            if(dropSprite.getY() < -dropHeight){
                dropSprites.removeIndex(i);
            }
            else if(bucketRectangle.overlaps(dropRectangle)){
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer+=delta;
        if(dropTimer>=1f){
            dropTimer=0;
            createDroplet();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture,0,0,worldWidth,worldHeight);
        bucketSprite.draw(spriteBatch);

        for(Sprite dropSprite:dropSprites){
            dropSprite.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    private void createDroplet(){
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }
    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}
