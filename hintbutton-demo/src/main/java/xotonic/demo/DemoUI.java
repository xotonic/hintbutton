package xotonic.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import xotonic.HintButtonForTextField;

import javax.servlet.annotation.WebServlet;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        TextField name = new TextField();
        TextField tf2 = new TextField();
        name.setCaption("Type your name here:");
        Button button = new Button("Click Me");
        new HintButtonForTextField().addTo(name);
        new HintButtonForTextField().addTo(tf2);
        button.addClickListener(e -> {
            Window w = new Window("Test");
            w.setModal(true);
            TextField tf = new TextField("Caption 1");
            TextField tf1 = new TextField("Caption 2");
            new HintButtonForTextField().addTo(tf);
            new HintButtonForTextField().addTo(tf1);
            VerticalLayout l = new VerticalLayout();
            l.addComponent(tf);
            l.addComponent(tf1);

            w.setContent(l);
            UI.getCurrent().addWindow(w);
        });


        // This is a component from the popup-addon module
        //layout.addComponent(new MyComponent());
        layout.addComponents(name, button, tf2);
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        setContent(layout);
    }
}
