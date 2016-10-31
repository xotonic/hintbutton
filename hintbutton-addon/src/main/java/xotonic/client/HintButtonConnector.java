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
               StateChangeEvent.StateChangeHandler
{

    private transient Element button = null;
    private transient Element popupRoot = null;
    private transient Element contentBox = null;
    private transient VTextField textField = null;
    private transient Overlay overlay = null;

    private native void debug(String message) /*-{
        window.console.debug(message);
    }-*/;

    @Override
    protected void extend(ServerConnector target) {
        debug("Extending : start");

        debug("Getting component widget and set style for it");
        textField = (VTextField) ((ComponentConnector) target).getWidget();
        textField.addStyleName("hintbutton-textfield");


        debug("Creating button");
        button = DOM.createDiv();
        Button.wrap(button).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                debug("Button click : start");

                Element tf = textField.getElement();

                //debug("Getting content box arrow");
                //Element arrow = getContentBoxArrow();
                final int boxArrowHeightHalf = 21;


                int top = tf.getOffsetTop() + tf.getClientHeight() + boxArrowHeightHalf;
                int left = tf.getOffsetLeft() + ( tf.getClientWidth() - contentBox.getClientWidth() ) / 2;

                popupRoot.getStyle().setTop(top, Style.Unit.PX);
                popupRoot.getStyle().setLeft(left, Style.Unit.PX);

                debug("Button click : finish");
            }
        });
        button.addClassName("hintbutton-button");

        debug("Creating overlay");
        overlay = new Overlay();
        overlay.setVisible(true);
        overlay.getElement().setId("hint-button-overlay");

        debug("Creating popup root");
        popupRoot = DOM.createDiv();
        popupRoot.setId("hintbutton-popup-root");

        debug("Creating content box");
        contentBox = DOM.createDiv();
        contentBox.setInnerHTML("Note<br>Next note");
        contentBox.addClassName("box");

        debug("Showing overlay");
        overlay.show();

        debug("Adding content box to popup root");
        popupRoot.appendChild(contentBox);
        overlay.getElement().appendChild(popupRoot);

        textField.addAttachHandler(this);

        debug("Extending : finish");
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

        debug("Attaching : finish");
    }

    private  void onDetach()
    {
        debug("Detaching : start");
        debug("Detaching : finish");
    }

    private native Element getContentBoxArrow()
        /*-{
        var arrow = $doc.querySelector(".box:before");
        return arrow;
        }-*/;
}
