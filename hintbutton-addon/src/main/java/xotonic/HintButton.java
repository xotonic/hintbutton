package xotonic;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractField;


public class HintButton extends AbstractExtension {

    public static void addTo(AbstractField field)
    {
        System.out.println("Adding To Field");
        HintButton hb = new HintButton();
        hb.extend(field);
    }

    private void extend(AbstractField field)
    {
        super.extend(field);
    }
}
