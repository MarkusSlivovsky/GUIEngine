package org.vnna.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.vnna.core.engine.media_manager.media.CMediaCursor;
import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.actions.UpdateAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;
import org.vnna.core.engine.ui_engine.gui.components.button.Button;
import org.vnna.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.knob.Knob;
import org.vnna.core.engine.ui_engine.gui.components.list.List;
import org.vnna.core.engine.ui_engine.gui.components.map.Map;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.vnna.core.engine.ui_engine.gui.components.textfield.TextField;
import org.vnna.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.vnna.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.vnna.core.engine.ui_engine.gui.hotkeys.HotKey;
import org.vnna.core.engine.ui_engine.gui.notification.Notification;
import org.vnna.core.engine.ui_engine.gui.tool.MouseTool;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.vnna.core.engine.ui_engine.input_processor.InputEvents;
import org.vnna.core.engine.ui_engine.input_processor.UIEngineInputProcessor;
import org.vnna.core.engine.ui_engine.misc.ViewportMode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class InputState {

    /* Parameters */

    public int internalResolutionWidth, internalResolutionHeight;

    public ViewportMode viewportMode;


    /* #################### Graphics: Game #################### */
    public SpriteBatch spriteBatch_game;

    public TextureRegion texture_game;

    public OrthographicCamera camera_game;

    public float camera_x, camera_y, camera_z, camera_zoom;

    public int camera_width, camera_height;

    public FrameBuffer frameBuffer_game;


    /* #################### Graphics: GUI #################### */

    public SpriteBatch spriteBatch_gui;
    public TextureRegion texture_gui;

    public OrthographicCamera camera_gui;

    public FrameBuffer frameBuffer_gui;

    /* #################### Graphics: Upscaling #################### */

    public TextureRegion texture_upScale;
    public int factor_upScale;

    public FrameBuffer frameBuffer_upScale;


    /* #################### Graphics: Screen #################### */

    public SpriteBatch spriteBatch_screen;

    public Viewport viewport_screen;

    public OrthographicCamera camera_screen;

    /* #################### GUI: Added Elements #################### */

    public ArrayList<Window> windows;

    public Deque<Window> addWindowQueue;

    public Deque<Window> removeWindowQueue;

    public ArrayList<Component> screenComponents;

    public Deque<Component> addScreenComponentsQueue;

    public Deque<Component> removeScreenComponentsQueue;

    public Deque<HotKey> addHotKeyQueue;

    public Deque<HotKey> removeHotKeyQueue;

    public Window modalWindow;

    public ArrayDeque<Window> modalWindowQueue;

    public ArrayList<Notification> notifications;

    public ArrayList<HotKey> hotKeys;

    public boolean guiFrozen;

    public ArrayList<GameViewPort> gameViewPorts;

    public ArrayList<UpdateAction> delayedOneshotActions;



    /* #################### GUI: Temporary Switches #################### */

    public Window draggedWindow;

    public int draggedWindow_x_offset, draggedWindow_y_offset;

    public Button pressedButton;

    public int pressedButton_timer_hold;

    public ScrollBarVertical scrolledScrollBarVertical;

    public ScrollBarHorizontal scrolledScrollBarHorizontal;

    public ToolTip tooltip;
    public Object tooltip_lastHoverObject;

    public Knob turnedKnob;

    public Map pressedMap;

    public GameViewPort pressedGameViewPort;

    public TextField focusedTextField;

    public Object inventoryDrag_Item;

    public Inventory inventoryDrag_Inventory;

    public int inventoryDrag_from_x;

    public int inventoryDrag_from_y;

    public int InventoryDrag_offset_x;

    public int InventoryDrag_offset_y;

    public Object listDrag_Item;

    public List listDrag_List;

    public int listDrag_from_index;

    public int listDrag_offset_x;

    public int listDrag_offset_y;

    public float tooltip_fadeIn_pct;

    public boolean tooltip_wait_delay;

    public long tooltip_delay_timer, tooltip_fadeIn_timer;

    public boolean[] hotKeyPressedKeys;

    public Window lastActiveWindow;
    public MouseTool pressedMouseTool;

    public ComboBox openComboBox;

    public ContextMenu displayedContextMenu;

    public int displayedContextMenuWidth;

    /* #################### Mouse #################### */

    public int mouse_x;

    public int mouse_y;

    public int mouse_x_gui;

    public int mouse_y_gui;

    public int mouse_x_delta;

    public int mouse_y_delta;

    public Object lastGUIMouseHover; // Last GUI Element the mouse hovered over

    public CMediaCursor cursor_setNext;

    public CMediaCursor cursor_current;

    public MouseTool mouseTool;

    public CMediaCursor temporaryCursor;

    public boolean displayTemporaryCursor;

    public Vector3 vector_fboCursor;

    public Vector2 vector2_unproject;

    /* #################### Misc. ####################  */

    public float animation_timer_gui;

    public Color[] tempColorStack;

    public int tempColorStackPointer;

    public ShaderProgram grayScaleShader;

    public OrthographicCamera camera_frustum; // camera for frustum testing

    public UIEngineInputProcessor inputProcessor;

    public InputEvents inputEvents;
}
