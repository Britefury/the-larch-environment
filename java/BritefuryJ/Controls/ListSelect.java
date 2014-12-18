//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.ApplyStyleSheetFromAttribute;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleValues;

public class ListSelect extends ControlPres
{
    public static interface ListSelectListener
    {
        public void onChoice(ListSelectControl listSelect, Object choice);
    }


    private static class ChoiceToggleListener implements ToggleButton.ToggleButtonListener
    {
        private Object choice;
        private ListSelectControl control;

        private ChoiceToggleListener(Object choice)
        {
            this.choice = choice;
        }

        @Override
        public void onToggle(ToggleButton.ToggleButtonControl toggle, boolean state)
        {
            if (state)
            {
                control.setChoice(choice);
            }
        }
    }


    public static class ListSelectControl extends Control
    {
        private LSElement element;
        private LiveInterface choice;
        private ListSelectListener listener;


        protected ListSelectControl(PresentationContext ctx, StyleValues style, LSElement element, LiveInterface choice, ListSelectListener listener)
        {
            super( ctx, style );

            this.element = element;
            this.choice = choice;
            this.listener = listener;
        }




        @Override
        public LSElement getElement()
        {
            return element;
        }


        public Object getChoice()
        {
            return choice.getStaticValue();
        }

        public void setChoice(Object newChoice)
        {
            if ( listener != null )
            {
                listener.onChoice(this, newChoice);
            }
            choice.setLiteralValue(newChoice);
        }
    }




    private static class CommitListener implements ListSelectListener
    {
        private LiveValue value;
        private ListSelectListener listener;

        public CommitListener(LiveValue value, ListSelectListener listener)
        {
            this.value = value;
            this.listener = listener;
        }

        @Override
        public void onChoice(ListSelectControl listSelect, Object choice)
        {
            if ( listener != null )
            {
                listener.onChoice(listSelect, choice);
            }
            value.setLiteralValue( choice );
        }
    }





    private Pres contents[];
    private Object choiceValues[];
    protected LiveSource choiceSource;
    private ListSelectListener listener;


    private ListSelect(Object contents[], Object values[], LiveSource choiceSource, ListSelectListener listener)
    {
        super();

        this.contents = Pres.mapCoerce(contents);
        this.choiceValues = values;
        this.choiceSource = choiceSource;
        this.listener = listener;
    }

    public ListSelect(Object contents[], Object values[], LiveInterface choice, ListSelectListener listener)
    {
        this(contents, values, new LiveSourceRef(choice), listener);
    }

    public ListSelect(Object contents[], Object values[], LiveValue choice)
    {
        this(contents, values, new LiveSourceRef(choice), new CommitListener(choice, null));
    }

    public ListSelect(Object contents[], Object values[], Object initialChoice, ListSelectListener listener)
    {
        this(contents, values, new LiveSourceValue(initialChoice), listener);
    }

    public ListSelect(Object contents[], Object values[], ListSelectListener listener)
    {
        this( contents, values, values[0], listener );
    }


    private static Object[] labelTextsToLabels(String labelTexts[])
    {
        Object labels[] = new Object[labelTexts.length];
        for (int i = 0; i < labelTexts.length; i++) {
            labels[i] = new Label(labelTexts[i]);
        }
        return labels;
    }

    public static ListSelect listSelectWithLabels(String labelTexts[], Object values[], LiveInterface choice, ListSelectListener listener)
    {
        return new ListSelect(labelTextsToLabels(labelTexts), values, choice, listener);
    }

    public static ListSelect listSelectWithLabels(String labelTexts[], Object values[], LiveValue choice)
    {
        return new ListSelect(labelTextsToLabels(labelTexts), values, choice);
    }

    public static ListSelect listSelectWithLabels(String labelTexts[], Object values[], Object initialChoice, ListSelectListener listener)
    {
        return new ListSelect(labelTextsToLabels(labelTexts), values, initialChoice, listener);
    }

    public static ListSelect listSelectWithLabels(String labelTexts[], Object values[], ListSelectListener listener)
    {
        return new ListSelect(labelTextsToLabels(labelTexts), values, listener);
    }




    @Override
    public Control createControl(PresentationContext ctx, StyleValues style)
    {
        StyleValues usedStyle = Controls.useListSelectAttrs(style);

        double itemPaddingX = style.get(Controls.listSelectItemPaddingX, double.class);
        double itemPaddingY = style.get(Controls.listSelectItemPaddingY, double.class);

        final LiveInterface choice = choiceSource.getLive();

        ChoiceToggleListener choiceToggleListeners[] = new ChoiceToggleListener[contents.length];
        Pres choiceButtons[] = new Pres[contents.length];

        for (int i = 0; i < contents.length; i++)
        {
            final Object thisChoice = choiceValues[i];
            LiveFunction isHighlighted = new LiveFunction(new LiveFunction.Function() {

                @Override
                public Object evaluate() {
                    return choice.getValue().equals(thisChoice);
                }
            });

            choiceToggleListeners[i] = new ChoiceToggleListener(thisChoice);

            Pres toggleContents = contents[i].alignHPack().pad(itemPaddingX, itemPaddingY);
            toggleContents = presentAsCombinator(ctx, usedStyle, toggleContents);
            Pres toggle = new ToggleButton(toggleContents, toggleContents, isHighlighted, choiceToggleListeners[i]);
            toggle = new ApplyStyleSheetFromAttribute(Controls.listSelectToggleStyle, toggle);
            choiceButtons[i] = toggle.alignHExpand();
        }

        Pres listSelectColumn = new Column(choiceButtons);

        AbstractBorder listSelectBorder = style.get(Controls.listSelectBorder, AbstractBorder.class);
        Pres listSelect = listSelectBorder.surround(listSelectColumn);

        LSElement element = listSelect.present( ctx, style );


        // Control
        ListSelectControl control = new ListSelectControl( ctx, usedStyle, element, choice, listener );

        for (ChoiceToggleListener choiceToggleListener : choiceToggleListeners)
        {
            choiceToggleListener.control = control;
        }

        return control;
    }
}
