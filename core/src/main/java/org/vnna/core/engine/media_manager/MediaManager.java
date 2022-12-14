package org.vnna.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Align;
import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.*;
import org.vnna.core.engine.tools.Tools;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Created by Admin on 07.02.2019.
 */
public class MediaManager {

    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND_FX = "sound/";

    private HashMap<CMediaSound, Sound> medias_sounds = new HashMap<>();

    private HashMap<CMediaMusic, Music> medias_music = new HashMap<>();

    private HashMap<CMediaImage, TextureRegion> medias_images = new HashMap<>();

    private HashMap<CMediaCursor, Cursor> medias_cursors = new HashMap<>();


    private HashMap<CMediaFont, BitmapFont> medias_fonts = new HashMap<>();

    private HashMap<CMediaArray, TextureRegion[]> medias_arrays = new HashMap<>();

    private HashMap<CMediaAnimation, Animation> medias_animations = new HashMap<>();


    private ArrayDeque<CMedia> loadStack = new ArrayDeque<>();

    public HashSet<String> loadedMedia = new HashSet<>();

    private PixmapPacker pixmapPacker;

    private TextureAtlas textureAtlas;

    public MediaManager() {
        unloadAllAndReset();
    }

    public void unloadAllAndReset() {
        for (CMediaSound cMediaSound : medias_sounds.keySet()) {
            medias_sounds.get(cMediaSound).dispose();
        }
        this.medias_sounds.clear();

        for (CMediaMusic cMediaMusic : medias_music.keySet()) {
            medias_music.get(cMediaMusic).dispose();
        }
        this.medias_music.clear();

        for (CMediaFont cMediaFont : medias_fonts.keySet()) {
            medias_fonts.get(cMediaFont).dispose();
        }
        for (CMediaCursor cMediaCursor : medias_cursors.keySet()) {
            medias_cursors.get(cMediaCursor).dispose();
        }
        this.medias_fonts.clear();
        this.medias_cursors.clear();
        this.medias_images.clear();
        this.medias_arrays.clear();
        this.medias_animations.clear();
        this.loadedMedia.clear();
        this.loadStack.clear();

        if (textureAtlas != null) {
            this.textureAtlas.dispose();
            this.textureAtlas = null;
        }
        if (pixmapPacker != null) {
            this.pixmapPacker.dispose();
            this.pixmapPacker = null;
        }
    }


    public boolean loadAssets() {
        return loadAssets(4096, 4096, null, Texture.TextureFilter.Nearest);
    }

    public boolean loadAssets(int pageWidth, int pageHeight, Consumer<Float> progress, Texture.TextureFilter textureFilter) {
        this.pixmapPacker = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 2, true);
        this.textureAtlas = new TextureAtlas();

        ArrayDeque<CMedia> prepareStack = new ArrayDeque<>();

        CMedia loadMedia = null;
        int counter = 0;
        int stackSize = loadStack.size();
        loadLoop:
        while ((loadMedia = loadStack.pollFirst()) != null) {

            if (loadMedia instanceof CMediaGFX || loadMedia.getClass() == CMediaFont.class || loadMedia.getClass() == CMediaImageCursor.class) {
                if (!loadedMedia.contains(loadMedia.file)) {
                    String textureFileName = loadMedia.getClass() == CMediaFont.class ? loadMedia.file.replace(".fnt", ".png") : loadMedia.file;
                    Texture texture = new Texture(Tools.File.findResource(DIR_GRAPHICS + textureFileName), false);
                    TextureData textureData = texture.getTextureData();
                    textureData.prepare();
                    pixmapPacker.pack(loadMedia.file, textureData.consumePixmap());
                    textureData.disposePixmap();
                    texture.dispose();
                }

                prepareStack.push(loadMedia);
            } else if (loadMedia.getClass() == CMediaSound.class) {
                if (!loadedMedia.contains(loadMedia.file)) {
                    CMediaSound cMediaSound = (CMediaSound) loadMedia;
                    Sound sound = Gdx.audio.newSound(Tools.File.findResource(DIR_SOUND_FX + cMediaSound.file));
                    medias_sounds.put(cMediaSound, sound);
                }
            } else if (loadMedia.getClass() == CMediaMusic.class) {
                if (!loadedMedia.contains(loadMedia.file)) {
                    CMediaMusic cMediaMusic = (CMediaMusic) loadMedia;
                    Music music = Gdx.audio.newMusic(Tools.File.findResource(DIR_MUSIC + loadMedia.file));
                    medias_music.put(cMediaMusic, music);
                }
            }


            loadedMedia.add(loadMedia.file);
            counter++;
            if (progress != null) {
                progress.accept(0.5F * (counter / (float) stackSize));
            }
        }
        // Create Texture Atlas
        pixmapPacker.updateTextureAtlas(textureAtlas, textureFilter, textureFilter, false);

        // PrepareLoop
        CMedia prepareMedia = null;
        counter = 0;
        stackSize = loadStack.size();
        prepareLoop:
        while ((prepareMedia = prepareStack.pollFirst()) != null) {
            if (prepareMedia.getClass() == CMediaImageCursor.class) {
                CMediaImageCursor cMediaImageCursor = (CMediaImageCursor) prepareMedia;
                TextureRegion textureRegion = textureAtlas.findRegion(prepareMedia.file);
                medias_cursors.put(cMediaImageCursor, Gdx.graphics.newCursor(extractPixmapFromTextureRegion(textureRegion), cMediaImageCursor.hotspot_x, cMediaImageCursor.hotspot_y));
                // SystemCursors dont need to be put here
            } else if (prepareMedia.getClass() == CMediaImage.class) {
                CMediaImage cMediaImage = (CMediaImage) prepareMedia;
                medias_images.put(cMediaImage, textureAtlas.findRegion(prepareMedia.file));
            }
            if (prepareMedia.getClass() == CMediaFont.class) {
                CMediaFont cMediaFont = (CMediaFont) prepareMedia;
                BitmapFont bitmapFont = new BitmapFont(Tools.File.findResource(DIR_GRAPHICS + cMediaFont.file), textureAtlas.findRegion(cMediaFont.file));
                medias_fonts.put(cMediaFont, bitmapFont);
            } else if (prepareMedia.getClass() == CMediaArray.class) {
                CMediaArray cMediaArray = (CMediaArray) prepareMedia;
                TextureRegion textureRegion = textureAtlas.findRegion(cMediaArray.file);
                int width = (textureRegion.getRegionWidth() / cMediaArray.tile_width);
                int height = (textureRegion.getRegionHeight() / cMediaArray.tile_height);
                int count = width * height;
                TextureRegion[][] tmp = textureRegion.split(cMediaArray.tile_width, cMediaArray.tile_height);
                int tmpCount = 0;
                TextureRegion[] result = new TextureRegion[count];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        result[tmpCount++] = tmp[y][x];
                    }
                }
                medias_arrays.put(cMediaArray, result);
            } else if (prepareMedia.getClass() == CMediaAnimation.class) {
                CMediaAnimation cMediaAnimation = (CMediaAnimation) prepareMedia;
                TextureRegion textureRegion = textureAtlas.findRegion(cMediaAnimation.file);
                int width = (textureRegion.getRegionWidth() / cMediaAnimation.tile_width);
                int height = (textureRegion.getRegionHeight() / cMediaAnimation.tile_height);
                int count = width * height;
                TextureRegion[][] tmp = textureRegion.split(cMediaAnimation.tile_width, cMediaAnimation.tile_height);
                TextureRegion[] result = new TextureRegion[count];
                int tmpCount = 0;
                framesLoop:
                for (int x = 0; x < tmp.length; x++) {
                    for (int y = 0; y < tmp[0].length; y++) {
                        result[tmpCount++] = tmp[x][y];
                        if (tmpCount >= cMediaAnimation.frames) break framesLoop;
                    }
                }
                Animation<TextureRegion> animation = new Animation<>(cMediaAnimation.animation_speed, result);
                medias_animations.put(cMediaAnimation, animation);
            }

            counter++;
            if (progress != null) {
                progress.accept(0.5f + (0.5F * (counter / (float) stackSize)));
            }
        }


        return true;
    }

    private Pixmap extractPixmapFromTextureRegion(TextureRegion textureRegion) {
        TextureData textureData = textureRegion.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap pixmap = new Pixmap(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureData.getFormat());
        pixmap.drawPixmap(textureData.consumePixmap(), // The other Pixmap
                0, // The target x-coordinate (top left corner)
                0, // The target y-coordinate (top left corner)
                textureRegion.getRegionX(), // The source x-coordinate (top left corner)
                textureRegion.getRegionY(), // The source y-coordinate (top left corner)
                textureRegion.getRegionWidth(), // The width of the area from the other Pixmap in pixels
                textureRegion.getRegionHeight() // The height of the area from the other Pixmap in pixels
        );
        return pixmap;
    }


    public int textWidth(CMediaFont font, String text) {
        glyphLayout.setText(getCMediaFont(font), text);
        return (int) glyphLayout.width;
    }

    public int textHeight(CMediaFont font, String text) {
        glyphLayout.setText(getCMediaFont(font), text);
        return (int) glyphLayout.height;
    }

    public int imageWidth(CMediaGFX cMedia) {
        return imageWidth(cMedia, true);
    }

    public int imageWidth(CMediaGFX cMedia, boolean tileWidth) {
        if (tileWidth) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_width;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_width;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionWidth();
    }

    public int imageHeight(CMediaGFX cMedia) {
        return imageHeight(cMedia, true);
    }


    public int imageHeight(CMediaGFX cMedia, boolean tileHeight) {
        if (tileHeight) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_height;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_height;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionHeight();
    }

    /* Draw Methods */
    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y) {
        drawCMediaGFX(batch, cMedia, x, y, 0, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float animation_timer) {
        drawCMediaGFX(batch, cMedia, x, y, animation_timer, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float animation_timer, int arrayIndex) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImage(batch, (CMediaImage) cMedia, x, y);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimation(batch, (CMediaAnimation) cMedia, x, y, animation_timer);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArray(batch, (CMediaArray) cMedia, x, y, arrayIndex);
        }
    }

    /* CMediaImage */
    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 0, 0, 0);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y) {
        TextureRegion texture = getCMediaImage(cMedia);

        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public void drawCMediaImageCut(SpriteBatch batch, CMediaImage cMedia, float x, float y, int widthCut, int heightCut) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture.getTexture(), x, y, texture.getRegionX(), texture.getRegionY(), widthCut, heightCut);
    }

    /* --- CMediaAnimation  --- */

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y, float width, float height, float rotation) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y, float scaleX, float scaleY) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);

    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);

    }

    public void drawCMediaAnimationCut(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animation_timer, int widthCut, int heightCut) {

        TextureRegion textureRegion = getCMediaAnimation(cMedia, animation_timer);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX(), textureRegion.getRegionY(), widthCut, heightCut);

    }

    /* --- CMediaArray  --- */

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y, float width, float height) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);

    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y, float width, float height, float rotation) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);

    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);

    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y, float scaleX, float scaleY) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);

    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);

    }

    public void drawCMediaArrayCut(SpriteBatch batch, CMediaArray cMedia, float x, float y, int index, int widthCut, int heightCut) {

        TextureRegion textureRegion = getCMediaArray(cMedia, index);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX(), textureRegion.getRegionY(), widthCut, heightCut);

    }

    /* --- CMediaFont  --- */

    public void drawCMediaFont(SpriteBatch batch, CMediaFont cMedia, float x, float y, String text) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y));
    }

    public void drawCMediaFont(SpriteBatch batch, CMediaFont cMedia, float x, float y, String text, int maxWidth) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y), 0, text.length(), maxWidth, Align.left, false, "");
    }

    public void setCMediaFontColorWhite(CMediaFont font) {
        setCMediaFontColor(font, 1, 1, 1, 1);
    }

    public void setCMediaFontColor(CMediaFont font, FColor fColor) {
        setCMediaFontColor(font, fColor.r, fColor.g, fColor.b, fColor.a);
    }

    public void setCMediaFontColor(CMediaFont font, float r, float g, float b, float a) {
        getCMediaFont(font).setColor(r, g, b, a);
    }


    /* CMediaSound */

    public long playCMediaSound(CMediaSound cMedia) {
        return getCMediaSound(cMedia).play(1, 1, 0);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume) {
        return getCMediaSound(cMedia).play(volume, 1, 0);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume, float pan) {
        return getCMediaSound(cMedia).play(volume, 1, pan);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume, float pan, float pitch) {
        return getCMediaSound(cMedia).play(volume, pitch, pan);
    }

    public static CMediaImage create_CMediaImage(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(" file missing");
        CMediaImage cMediaImage = new CMediaImage(file);
        return cMediaImage;
    }

    public static CMediaImageCursor create_CMediaImageCursor(String file, int hotspot_x, int hotspot_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaImageCursor cMediaImageCursor = new CMediaImageCursor(file);
        cMediaImageCursor.hotspot_x = hotspot_x;
        cMediaImageCursor.hotspot_y = hotspot_y;
        return cMediaImageCursor;
    }

    public static CMediaSystemCursor create_CMediaSystemCursor(Cursor.SystemCursor systemCursor) {
        return new CMediaSystemCursor(systemCursor);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed) {
        return create_CMediaAnimation(file, tileWidth, tileHeight, animation_speed, Integer.MAX_VALUE);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed, int frames) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaAnimation cMediaAnimation = new CMediaAnimation(file);
        cMediaAnimation.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaAnimation.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        cMediaAnimation.animation_speed = animation_speed;
        cMediaAnimation.frames = Tools.Calc.lowerBounds(frames, 1);
        return cMediaAnimation;
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaFont cMediaFont = new CMediaFont(file);
        cMediaFont.offset_x = offset_x;
        cMediaFont.offset_y = offset_y;
        return cMediaFont;
    }

    public static CMediaMusic create_CMediaMusic(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaMusic cMediaMusic = new CMediaMusic(file);
        return cMediaMusic;
    }

    public static CMediaSound create_CMediaSound(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaSound cMediaSound = new CMediaSound(file);
        return cMediaSound;
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaArray cMediaArray = new CMediaArray(file);
        cMediaArray.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaArray.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        return cMediaArray;
    }

    public boolean prepareFromStaticClass(Class loadFromClass) {
        for (Field field : loadFromClass.getFields()) {
            CMedia cMedia = null;
            try {
                if (field.getType().isArray()) {
                    CMedia[] medias = (CMedia[]) field.get(null);
                    for (CMedia media : medias) {
                        loadStack.add(media);
                    }
                } else {
                    cMedia = (CMedia) field.get(null);
                    loadStack.add(cMedia);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }

        }

        return true;
    }


    public boolean prepareCMedia(CMedia cMedia) {
        loadStack.add(cMedia);
        return true;
    }


    public boolean prepareFromObject(Object object) {
        return prepareFromObject(object, 3);
    }

    public boolean prepareFromObject(Object object, int scanDepth) {
        try {
            resolveCMedia(object, scanDepth, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private boolean resolveCMedia(Object object, int scanDepth, int level) {
        if (object == null) return true;
        if (object.getClass().getPackageName().startsWith("java")) return true;
        if (level > scanDepth) return true; // scan up to depth 3
        if (CMedia.class.isAssignableFrom(object.getClass())) {
            CMedia cMedia = (CMedia) object;
            loadStack.add(cMedia);
            return true;
        }

        for (Field field : object.getClass().getFields()) {
            Object fieldObject = null;
            try {
                fieldObject = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            if (fieldObject != null) {
                if (CMedia.class.isAssignableFrom(fieldObject.getClass())) {
                    CMedia cMedia = (CMedia) fieldObject;
                    loadStack.add(cMedia);
                } else if (fieldObject.getClass() == ArrayList.class) {
                    ArrayList arrayList = (ArrayList) fieldObject;
                    for (Object arrayListItem : arrayList) {
                        resolveCMedia(arrayListItem, scanDepth, level + 1);
                    }
                } else if (field.getType().isArray()) {
                    if (field.getType().getName().startsWith("[L") || field.getType().getName().startsWith("[[L")) {
                        Object[] arrayObjects = (Object[]) fieldObject;
                        for (Object arrayObject : arrayObjects) {
                            resolveCMedia(arrayObject, scanDepth, level + 1);
                        }
                    }
                } else {
                    resolveCMedia(fieldObject, scanDepth, level + 1);
                }
            }
        }

        return true;
    }


    public TextureRegion getCMediaImage(CMediaImage cMedia) {
        return medias_images.get(cMedia);
    }

    public TextureRegion getCMediaAnimation(CMediaAnimation cMedia, float animation_timer) {
        return (TextureRegion) medias_animations.get(cMedia).getKeyFrame(animation_timer, true);
    }


    public boolean isCMediaAnimationFinished(CMediaAnimation cMedia, float animation_timer) {
        return medias_animations.get(cMedia).isAnimationFinished(animation_timer);
    }

    public int getCMediaAnimationKeyFrameIndex(CMediaAnimation cMedia, float animation_timer) {
        return medias_animations.get(cMedia).getKeyFrameIndex(animation_timer);
    }

    public Cursor getCMediaImageCursor(CMediaImageCursor cMedia) {
        return medias_cursors.get(cMedia);
    }

    public Cursor.SystemCursor getCMediaImageSystemCursor(CMediaSystemCursor cMedia) {
        return cMedia.systemCursor;
    }

    public int getCMediaArraySize(CMediaArray cMedia) {
        return medias_arrays.get(cMedia).length;
    }

    public TextureRegion getCMediaArray(CMediaArray cMedia, int index) {
        return medias_arrays.get(cMedia)[index];
    }

    public BitmapFont getCMediaFont(CMediaFont cMedia) {
        return medias_fonts.get(cMedia);
    }

    public Sound getCMediaSound(CMediaSound cMedia) {
        return medias_sounds.get(cMedia);
    }

    public Music getCMediaMusic(CMediaMusic cMedia) {
        return medias_music.get(cMedia);
    }

    public void shutdown() {
        this.unloadAllAndReset();
        return;
    }


}
