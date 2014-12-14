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
import BritefuryJ.StyleSheet.StyleValues;

public class AccordionContainer extends ControlPres
{
	public static interface AccordionContainerListener
	{
		public void onChoice(AccordionContainerControl expander, int choice);
	}


	private static class SectionListener implements DropDownExpander.ExpanderListener
	{
		private int choice;
		private AccordionContainerControl control;

		private SectionListener(int choice)
		{
			this.choice = choice;
		}

		@Override
		public void onExpander(Expander.ExpanderControl expander, boolean expanded)
		{
			if (expanded)
			{
				control.setChoice(choice);
			}
		}
	}


	public static class AccordionContainerControl extends Control
	{
		private LSElement element;
		private LiveInterface choice;
		private AccordionContainerListener listener;


		protected AccordionContainerControl(PresentationContext ctx, StyleValues style, LSElement element, LiveInterface choice, AccordionContainerListener listener)
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


		public int getChoice()
		{
			return (Integer)choice.getStaticValue();
		}

		public void setChoice(int newChoice)
		{
			if ( listener != null )
			{
				listener.onChoice(this, newChoice);
			}
			choice.setLiteralValue(newChoice);
		}
	}




	private static class CommitListener implements AccordionContainerListener
	{
		private LiveValue value;
		private AccordionContainerListener listener;

		public CommitListener(LiveValue value, AccordionContainerListener listener)
		{
			this.value = value;
			this.listener = listener;
		}

		@Override
		public void onChoice(AccordionContainerControl expander, int choice)
		{
			if ( listener != null )
			{
				listener.onChoice(expander, choice);
			}
			value.setLiteralValue( choice );
		}
	}





	private Pres contents[][];
	protected LiveSource choiceSource;
	private AccordionContainerListener listener;


	private AccordionContainer(Object contents[][], LiveSource choiceSource, AccordionContainerListener listener)
	{
		super();

		this.contents = new Pres[contents.length][];
		int i = 0;
		for (Object tab[]: contents)
		{
			this.contents[i] = new Pres[2];
			this.contents[i][0] = Pres.coerce( tab[0] );
			this.contents[i][1] = Pres.coerce( tab[1] );
			i++;
		}
		this.choiceSource = choiceSource;
		this.listener = listener;
	}

	public AccordionContainer(Object contents[][], int initialChoice, AccordionContainerListener listener)
	{
		this(contents, new LiveSourceValue(initialChoice), listener);
	}

	public AccordionContainer(Object contents[][], LiveValue choice, AccordionContainerListener listener)
	{
		this(contents, new LiveSourceRef(choice), listener);
	}

	public AccordionContainer(Object contents[][], LiveValue choice)
	{
		this(contents, new LiveSourceRef(choice), new CommitListener(choice, null));
	}

	public AccordionContainer(Object contents[][], AccordionContainerListener listener)
	{
		this( contents, 0, listener );
	}




	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useAccordionContainerAttrs(style);

		final LiveInterface choice = choiceSource.getLive();

		SectionListener sectionListeners[] = new SectionListener[contents.length];
		Pres expanders[] = new Pres[contents.length];

		for (int i = 0; i < contents.length; i++)
		{
			final int thisChoice = i;
			LiveFunction isHighlighted = new LiveFunction(new LiveFunction.Function() {

				@Override
				public Object evaluate() {
					return (Integer)choice.getValue() == thisChoice;
				}
			});

			sectionListeners[i] = new SectionListener(i);
			expanders[i] = presentAsCombinator(ctx, usedStyle, new DropDownExpander(contents[i][0], contents[i][1], isHighlighted, sectionListeners[i]));
		}

		Pres accordionColumn = new ApplyStyleSheetFromAttribute(Controls.accordionContainerSpacingColumnStyle, new Column(expanders));

		AbstractBorder accordionBorder = style.get(Controls.accordionContainerBorder, AbstractBorder.class);
		Pres accordion = accordionBorder.surround(accordionColumn);

		LSElement element = accordion.present( ctx, style );


		// Control
		AccordionContainerControl control = new AccordionContainerControl( ctx, usedStyle, element, choice, listener );

		for (SectionListener sectionListener : sectionListeners)
		{
			sectionListener.control = control;
		}

		return control;
	}
}
