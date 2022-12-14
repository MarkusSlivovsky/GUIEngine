package org.vnna.core.example.ui.media;

import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.media_manager.media.CMediaAnimation;
import org.vnna.core.engine.media_manager.media.CMediaArray;
import org.vnna.core.engine.media_manager.media.CMediaImage;
import org.vnna.core.engine.media_manager.media.CMediaMusic;
import org.vnna.core.engine.ui_engine.UIEngine;

public class ExampleBaseMedia {

    private static final String exampleDirectory = "example/";
    
    public static CMediaAnimation GUI_ICON_BUTTON_ANIM_EXAMPLE = MediaManager.create_CMediaAnimation(exampleDirectory +"example_animation.png",UIEngine.TILE_SIZE*2,UIEngine.TILE_SIZE,0.1f);
    public static CMediaArray GUI_ICON_BUTTON_ANIM_EXAMPLE_ARRAY = MediaManager.create_CMediaArray(exampleDirectory +"example_array.png",UIEngine.TILE_SIZE*2,UIEngine.TILE_SIZE);

    public static CMediaImage GUI_SCREEN_BG = MediaManager.create_CMediaImage(exampleDirectory +"screen_bg.png");
    public static CMediaImage GUI_ICON_EXAMPLE_1 = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_1.png");
    public static CMediaImage GUI_ICON_EXAMPLE_2 = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_2.png");
    public static CMediaImage GUI_ICON_EXAMPLE_3 = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_3.png");
    public static CMediaImage GUI_ICON_EXAMPLE_4 = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_4.png");
    public static CMediaImage GUI_ICON_EXAMPLE_DOUBLE = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_double.png");

    public static CMediaImage GUI_ICON_EXAMPLE_BULLET_GREEN = MediaManager.create_CMediaImage(exampleDirectory +"example_bullet_green.png");
    public static CMediaImage GUI_ICON_EXAMPLE_BULLET_BLUE = MediaManager.create_CMediaImage(exampleDirectory +"example_bullet_blue.png");
    public static CMediaImage GUI_ICON_EXAMPLE_BULLET_ORANGE = MediaManager.create_CMediaImage(exampleDirectory +"example_bullet_orange.png");
    public static CMediaImage GUI_ICON_EXAMPLE_WINDOW = MediaManager.create_CMediaImage(exampleDirectory +"example_icon_window.png");

    public static CMediaAnimation GUI_ICON_EXAMPLE_ANIMATION_2 = MediaManager.create_CMediaAnimation(exampleDirectory +"example_animation_2.png", UIEngine.TILE_SIZE*8,UIEngine.TILE_SIZE*8,0.1f);
    public static CMediaAnimation GUI_BACKGROUND = MediaManager.create_CMediaAnimation(exampleDirectory +"background.png",32,32,0.2f);


}
