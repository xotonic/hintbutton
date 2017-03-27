package xotonic;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractField;
import xotonic.client.HintButtonState;

import java.util.ArrayList;


/**
 * Component Extension для полей ввода, добавляет кнопку справа, при нажатии на
 * которую выводится текст (обычно подсказки для паролей)
 * !!!! Для добавления функционала используйте наследника Hint в ui модуле
 * Created by xotonic on 03.08.16.
 */
public class HintButtonForTextField extends AbstractExtension  {

    /** Так как невозможно использовать локализацию в данном модуле (циклическая зависимость)
     *  Предоставим загрузку строк локализации потомкам в ui модуле
     */
    protected enum LocalizedPattern
    {
        FIXED_LENGTH("%d"),
        RANGE_LENGTH("%d %d"),
        ONLY_MAX_LENGTH("%d"),
        ONLY_LATIN(""),
        UPPERCASE(""),
        LOWERCASE(""),
        DIGITS(""),
        SPECIALS(""),
        SPACE(""),
        IPV4(""),
        VALUE_RANGE("%s %s"),
        NATURAL_MORE("%d"),
        VALUE_UNITS(""),
        ;
        private String pattern;

        LocalizedPattern(String defaultPattern)
        {
            this.pattern = defaultPattern;
        }

        public void set(String pattern)
        {
            this.pattern = pattern;
        }

        public String get()
        {
            return pattern;
        }


        @Override
        public String toString() {
            return pattern;
        }
    }

    private static final String NEW_LINE = "</br>";
    private static final String ITEM = "- ";

    
    // Содержимое подсказки с HTML форматированием
    private String hintHTML;
    private ArrayList<String> notes = new ArrayList<>();
    private int minLength = -1;
    private int maxLength = -1;
    private boolean onlyLatin = false;
    private boolean lowercase = false;
    private boolean uppercase = false;
    private boolean digits = false;
    private String specials = "";
    private String minValue = "";
    private  String maxValue = "";
    private int moreThanNatural = 0;




    public HintButtonForTextField() {}

    private void extend(AbstractField field) {

        super.extend(field);
        getState().hint = hintHTML;
    }
    @Override
    protected HintButtonState getState() {
        return (HintButtonState) super.getState();
    }


    public AbstractField addTo(AbstractField field)
    {
        hintHTML = generateHtml();
        extend(field);
        return field;
    }


    /** Превращает набор параметров в HTML на вывод
     * @return HTML for element.innerHTML
     */
    private String generateHtml()
    {
        StringBuilder builder = new StringBuilder();
        if (minLength > -1 && maxLength > -1)
        {
            if (minLength == maxLength)
            {
                builder.append(ITEM).append(String.format(LocalizedPattern.FIXED_LENGTH.get(), minLength)).append(NEW_LINE);
            }
            else
            {
                builder.append(ITEM).append(String.format(LocalizedPattern.RANGE_LENGTH.get(), minLength, maxLength)).append(NEW_LINE);
            }
        }
        else
        {
            if (maxLength > -1)
            {
                builder.append(ITEM).append(String.format(LocalizedPattern.ONLY_MAX_LENGTH.get(), maxLength)).append(NEW_LINE);
            }
        }

        if (!minValue.isEmpty() && !maxValue.isEmpty())
        {
            builder.append(ITEM).append(String.format(LocalizedPattern.VALUE_RANGE.get(), minValue, maxValue)).append(NEW_LINE);
        }

        if (onlyLatin)
        {
            builder.append(ITEM).append(LocalizedPattern.ONLY_LATIN).append(NEW_LINE);
        }

        if (lowercase)
            builder.append(ITEM).append(LocalizedPattern.LOWERCASE ).append(NEW_LINE);
        if (uppercase)
            builder.append(ITEM).append(LocalizedPattern.UPPERCASE ).append(NEW_LINE);
        if (digits)
            builder.append(ITEM).append(LocalizedPattern.DIGITS ).append(NEW_LINE);
        if(moreThanNatural >0)
            builder.append(ITEM).append(String.format(LocalizedPattern.NATURAL_MORE.get(), moreThanNatural)).append(NEW_LINE);
        if (!specials.isEmpty())
        {
            builder.append(ITEM).append(LocalizedPattern.SPECIALS ).append(NEW_LINE);
            for (char c : specials.toCharArray())
            {
                if (c != ' ')
                    builder.append(String.format(" '<b>%c</b>'",c));
                else
                    builder.append(" <b>").append(LocalizedPattern.SPACE).append("</b>");

                if (specials.charAt(specials.length() - 1) != c )
                    builder.append(',');
                else
                    builder.append(NEW_LINE);
            }
        }

        for (String note : notes)
        {
            final String noteHtml = note.replaceAll("\n", NEW_LINE);
            builder.append(ITEM).append(noteHtml).append(NEW_LINE);
        }
        return builder.toString();
    }

    /** Фиксированная длина значения */
    public HintButtonForTextField length(int lenght)
    {
        minLength = maxLength = lenght;
        return this;
    }
    /** Диапазон значений длины */
    public HintButtonForTextField length(int min, int max)
    {
        minLength = min;
        maxLength = max;
        return this;
    }

    /** Только латинские символы <br>
     *  NOTE:<br>
     *   Выведется, если только будут истинны
     *  {@link #lowercase(boolean) } или  {@link #uppercase(boolean) }
     * */
    public HintButtonForTextField onlyLatin(boolean on)
    {
        onlyLatin = on;
        return this;
    }
    /** Допускаются прописные буквы */
    public HintButtonForTextField lowercase(boolean on)
    {
        lowercase = on;
        return this;
    }
    /** Допускаются заглавные буквы */
    public HintButtonForTextField uppercase(boolean on)
    {
        uppercase = on;
        return this;
    }
    /** Допускаются цифры */
    public HintButtonForTextField digits(boolean on)
    {
        digits = on;
        return this;
    }
    /** Специальные символы
     * @param symbols спецсимволы слитно без разделителей, пробел тоже спецсимвол
     *                (будет преобразован в слово для видимости)
     * */
    public HintButtonForTextField special(String symbols)
    {
        specials = symbols;
        return this;
    }
    /**
     * Добавить кастомный текст в конце основных параметров
     * @param text Текст, можно в HTML
     *  **/
    public HintButtonForTextField note(String text)
    {
        notes.add(text);
        return this;
    }

    public HintButtonForTextField valueRange(Object min, Object max)
    {
        maxValue = max.toString();
        minValue = min.toString();
        return this;
    }


    public HintButtonForTextField maxLength(int max)
    {
        maxLength = max;
        return this;
    }
    public HintButtonForTextField naturalMore(int moreThan)
    {
        this.moreThanNatural = moreThan;
        return this;
    }

    public void setCaption(String caption)
    {
        getState().caption = caption;
    }

    public void setCloseTooltip(String tooltipText) {
        getState().clickToCloseTooltip = tooltipText;
    }

}
