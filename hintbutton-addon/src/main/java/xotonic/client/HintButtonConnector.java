package xotonic.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.client.widgets.Overlay;
import com.vaadin.shared.ui.Connect;
import xotonic.HintButton;

@Connect(HintButton.class)
public class HintButtonConnector

    extends AbstractExtensionConnector

    implements AttachEvent.Handler,
               StateChangeEvent.StateChangeHandler,
               ClickHandler
{

    // Classnames
    private static final String SHOWED = "showed";
    private static final String BUTTON = "hintbutton-button";
    private static final String TEXTFIELD = "hintbutton-textfield";
    private static final String POPUP_ROOT = "hintbutton-popup-root";
    private static final String BOX = "box";
    private static final String OVERLAY = "hint-button-overlay";

    // Textfield elements, instance for each component
    private transient Element button = null;
    private transient VTextField textField = null;

    // Overlay elements, single instance for app
    private transient static Element popupRoot = null;
    private transient static Element contentBox = null;
    private transient static Overlay overlay = null;
    private transient static VTextField lastClickedField = null;

    private native static void debug(String message) /*-{
        window.console.debug(message);
    }-*/;

    @Override
    protected void extend(ServerConnector target) {
        debug("Extending : start");

        modifyTargetField((ComponentConnector) target);
        createButton();

        if (!overlayIsCreated())
            createOverlay();

        debug("Extending : finish");
    }

    private void createButton() {
        debug("Creating button");
        button = DOM.createDiv();
        Button.wrap(button).addClickHandler(this);
        button.addClassName(BUTTON);
    }

    private void modifyTargetField(ComponentConnector target) {
        debug("Getting component widget and modifying it");
        textField = (VTextField) target.getWidget();
        textField.addStyleName(TEXTFIELD);
        textField.addAttachHandler(this);
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
        debug("Attaching : start");

        debug("Inserting button into textfield element");
        Element textFieldElement = textField.getElement();
        textFieldElement.getParentElement().insertAfter(button, textFieldElement);

        updatePosition();

        debug("Attaching : finish");
    }

    private  void onDetach()
    {
        debug("Detaching : start");
        debug("Detaching : finish");
    }

    /* fails */
    private native Element getContentBoxArrow()
        /*-{
        var arrow = $doc.querySelector(".box:before");
        return arrow;
        }-*/;


    private void updatePosition()
    {
        assert overlayIsCreated();

        debug("Updating position");
        Element tf = textField.getElement();

        //debug("Getting content box arrow");
        //Element arrow = getContentBoxArrow();
        final int boxArrowHeightHalf = 21;

        int top = tf.getAbsoluteTop() + tf.getClientHeight() + boxArrowHeightHalf;
        int left = tf.getAbsoluteLeft() + ( tf.getClientWidth() - contentBox.getClientWidth() ) / 2;

        debug("top  = " + top);
        debug("left = " + left);

        popupRoot.getStyle().setTop(top, Style.Unit.PX);
        popupRoot.getStyle().setLeft(left, Style.Unit.PX);
    }

    private static void createOverlay()
    {
        assert !overlayIsCreated();

        debug("Creating overlay");
        overlay = new Overlay();
        overlay.setVisible(true);
        overlay.getElement().setId(OVERLAY);

        debug("Creating popup root");
        popupRoot = DOM.createDiv();
        popupRoot.setId(POPUP_ROOT);

        debug("Creating content box");
        contentBox = DOM.createDiv();
        contentBox.setInnerHTML("Note<br>Next note");
        contentBox.addClassName(BOX);

        debug("Showing overlay");
        overlay.show();

        debug("Adding content box to popup root");
        popupRoot.appendChild(contentBox);
        overlay.getElement().appendChild(popupRoot);
    }

    private static boolean overlayIsCreated()
    {
        return overlay != null;
    }

    @Override
    public void onClick(ClickEvent event) {
        debug("Button click : start");

        updatePosition();
        updateVisibility();

        debug("Button click : finish");
    }

    private void updateVisibility()
    {
        debug("Updating visibility");

        if (lastClickedField == textField)
                showOrHidePopup();
        else
        {
            /* Not matter current popup state, just showing it
               because it is another field */
            showPopup();
        }

        lastClickedField = textField;
    }

    private void showOrHidePopup() {
        if (!isShowed())
            showPopup();
        else
            hidePopup();
    }

    private boolean isShowed() {
        return popupRoot.hasClassName(SHOWED);
    }

    private void showPopup() {
        popupRoot.addClassName(SHOWED);
    }

    private void hidePopup() {
        popupRoot.removeClassName(SHOWED);
    }
}
