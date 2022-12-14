package org.vnna.core.engine.ui_engine.gui.components.textfield;

import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.ui_engine.gui.actions.TextFieldAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;
import java.util.HashSet;

public class TextField extends Component {

    public String content;

    public CMediaFont font;

    public TextFieldAction textFieldAction;

    public boolean focused;

    public int contentMaxLength;

    public int offset;

    public HashSet<Character> allowedCharacters;

    public int markerPosition;

    public boolean contentValid;

}
