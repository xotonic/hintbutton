package xotonic.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.client.widgets.Overlay;
import com.vaadin.shared.ui.Connect;
import xotonic.HintButtonForTextField;


@Connect(HintButtonForTextField.class)
public class HintButtonConnector

        extends     AbstractExtensionConnector

        implements  AttachEvent.Handler,
                    StateChangeEvent.StateChangeHandler,
                    ClickHandler,
                    FocusHandler,
                    BlurHandler
{

    // Classnames
    private static final String SHOWED = "showed";
    private static final String BUTTON = "hintbutton-button";
    private static final String TEXTFIELD = "hintbutton-textfield";
    private static final String POPUP_ROOT = "hintbutton-popup-root";
    private static final String BOX = "box";
    private static final String BOX_HEADER = "hb-header";
    private static final String HEADER_ICON = "hb-icon";
    private static final String HEADER_CAPTION = "hb-header-caption";
    private static final String BOX_BODY = "hb-body";
    private static final String OVERLAY = "hint-button-overlay";
    private static final String FONT_AWESOME = "FontAwesome";
    private static final String DEFAULT_POS = "default-position";
    private static final String IN_BOTTOM = "in-bottom";

    // FontAwesome codes
    private static final String FA_INFO_CODE = "&#xf05a";
    private static final String FA_QUESTION_CODE = "&#xf128";

    // Textfield elements, instance for each component
    private transient Element button = null;
    private transient VTextField textField = null;

    // Overlay elements, single instance for app
    private transient static Element popupRoot = null;
    private transient static Element contentBox = null;
    private transient static Element headerBoxCaption = null;
    private transient static Overlay overlay = null;
    private transient static VTextField lastClickedField = null;

    private transient HandlerRegistration focusHandlerRegistration = null;
    private transient HandlerRegistration blurHandlerRegistration = null;

    private native static void debug(String message) /*-{
        window.console.debug(message);
    }-*/;

    @Override
    protected void extend(ServerConnector target) {


        modifyTargetField((ComponentConnector) target);
        createButton();

        if (!overlayIsCreated())
            createOverlay();


    }

    private void createButton() {

        button = DOM.createDiv();
        Button.wrap(button).addClickHandler(this);
        button.addClassName(BUTTON);
        button.addClassName(FONT_AWESOME);
        button.setTabIndex(-1);
        button.setInnerHTML(FA_QUESTION_CODE);
    }

    private void modifyTargetField(ComponentConnector target) {

        textField = (VTextField) target.getWidget();
        textField.addStyleName(TEXTFIELD);
        textField.addAttachHandler(this);
    }

    private static void setUpPopupHandlers()
    {
        Button.wrap(popupRoot).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hidePopup();
            }
        });
    }

    private void setUpTextFieldHandlers()
    {

        textField.addBlurHandler(this);
        textField.addFocusHandler(this);
    }

    private void removeTextFieldHandlers()
    {

        if (focusHandlerRegistration != null)
            focusHandlerRegistration.removeHandler();
        if (blurHandlerRegistration != null)
            blurHandlerRegistration.removeHandler();
    }

    @Override
    public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached())
            onAttach();
        else
            onDetach();
    }

    private void onAttach()
    {

        Element textFieldElement = textField.getElement();
        textFieldElement.getParentElement().insertAfter(button, textFieldElement);

        updatePosition();

        setUpTextFieldHandlers();

    }

    private  void onDetach()
    {

        if (isShowed())
            hidePopup();

        removeTextFieldHandlers();

    }


    private void updatePosition()
    {
        assert overlayIsCreated();

        // Reset position
        popupRoot.removeClassName(IN_BOTTOM);

        if (isNeedMoveToBottom()) {

            popupRoot.addClassName(IN_BOTTOM);
        }

    }

    private boolean isNeedMoveToBottom()
    {


        // Popup Bottom Right point coords
        int popupBottomRightX = popupRoot.getAbsoluteLeft() + popupRoot.getClientWidth();
        int popupBottomRightY = popupRoot.getAbsoluteTop() + popupRoot.getClientHeight();

        // Textfield Top Left point coords
        int fieldTopLeftX = textField.getAbsoluteLeft();
        int fieldTopLeftY = textField.getAbsoluteTop();

        return popupBottomRightX > fieldTopLeftX & popupBottomRightY > fieldTopLeftY;
    }

    private static void createOverlay()
    {
        assert !overlayIsCreated();


        overlay = new Overlay();
        overlay.setVisible(true);
        overlay.getElement().setId(OVERLAY);


        popupRoot = DOM.createDiv();
        popupRoot.setId(POPUP_ROOT);
        popupRoot.addClassName(DEFAULT_POS);
        setUpPopupHandlers();
        popupRoot.setTitle("Click to close popup");


        contentBox = DOM.createDiv();
        contentBox.setInnerHTML("-");
        contentBox.addClassName(BOX);
        contentBox.addClassName(BOX_BODY);

        Element headerBox = DOM.createDiv();
        headerBox.addClassName(BOX);
        headerBox.addClassName(BOX_HEADER);

        Element headerIcon = DOM.createSpan();
        headerIcon.addClassName(HEADER_ICON);
        headerIcon.addClassName(FONT_AWESOME);
        headerIcon.setInnerHTML(FA_INFO_CODE);

        headerBoxCaption = DOM.createSpan();
        headerBoxCaption.addClassName(HEADER_CAPTION);
        headerBoxCaption.setInnerHTML("Field format");


        overlay.show();


        popupRoot.appendChild(headerBox);
        headerBox.appendChild(headerIcon);
        headerBox.appendChild(headerBoxCaption);


        popupRoot.appendChild(contentBox);
        overlay.getElement().appendChild(popupRoot);
    }

    private static boolean overlayIsCreated()
    {
        return overlay != null;
    }

    @Override
    public void onClick(ClickEvent event) {


        updatePosition();
        updateText();
        updateVisibility();


    }

    private void updateVisibility()
    {


        if (isCurrentTextFieldWasLastActive())
            showOrHidePopup();
        else
        {
            /* Not matter current popup state, just showing it
               because it is another field */
            showPopup();
        }

        registerActivity();
    }

    private void registerActivity() {
        lastClickedField = textField;
    }

    private static void showOrHidePopup() {
        if (!isShowed())
            showPopup();
        else
            hidePopup();
    }

    private static boolean isShowed() {
        return popupRoot.hasClassName(SHOWED);
    }

    private static void showPopup() {
        popupRoot.addClassName(SHOWED);
    }

    private static void hidePopup() {
        popupRoot.removeClassName(SHOWED);
    }

    @Override
    public HintButtonState getState() {
        return (HintButtonState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent)
    {
        super.onStateChanged(stateChangeEvent);

        updateText();

    }

    private void updateText() {

        final String text = getState().hint;
        contentBox.setInnerHTML(text);
    }

    @Override
    public void onBlur(BlurEvent event) {

    }

    @Override
    public void onFocus(FocusEvent event) {

        updatePosition();
        updateText();
    }

    private boolean isCurrentTextFieldWasLastActive() {
        return lastClickedField == textField;
    }

    @OnStateChange("caption")
    void updateCaption()
    {

        headerBoxCaption.setInnerHTML(getState().caption);
    }

    @OnStateChange("clickToCloseTooltip")
    void updateTooltip()
    {
        
        popupRoot.setTitle(getState().clickToCloseTooltip);
    }
}
