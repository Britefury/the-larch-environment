//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.Controls.TextEntry.TextEntryControl;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.IncrementalUnit.LiteralUnit;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleValues;

public class EditableLabel extends ControlPres
{
	public static interface EditableLabelListener
	{
		public void onTextChanged(EditableLabelControl editableLabel, String text);
	}
	
	
	
	public static class EditableLabelControl extends Control
	{
		private DPElement element;
		private LiteralUnit unit;
		private String text;
		private Pres notSet;
		private EditableLabelListener listener;
		private TextEntry.TextEntryValidator validator;
		
		
		private PushElementInteractor labelInteractor = new PushElementInteractor()
		{
			@Override
			public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
			{
				showTextEntry();
			}
		};
		
		private TextEntry.TextEntryListener entryListener = new TextEntry.TextEntryListener()
		{
			public void onAccept(TextEntryControl textEntry, String text)
			{
				EditableLabelControl.this.text = text;
				showLabel();
				if ( listener != null )
				{
					listener.onTextChanged( EditableLabelControl.this, text );
				}
			}
		};

		
		
		public EditableLabelControl(PresentationContext ctx, StyleValues style, DPElement element, LiteralUnit unit, String initialText, Pres notSet,
				EditableLabelListener listener, TextEntry.TextEntryValidator validator)
		{
			super( ctx, style );
			
			this.element = element;
			this.unit = unit;
			this.text = initialText;
			this.notSet = notSet;
			this.listener = listener;
			this.validator = validator;
			
			showLabel();
		}



		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		
		private void showLabel()
		{
			Pres label;
			if ( text == null )
			{
				label = notSet;
			}
			else
			{
				label = new Label( text ).withStyleSheetFromAttr( Controls.editableLabelTextAttrs );
			}
			unit.setLiteralValue( label.withStyleSheetFromAttr( Controls.editableLabelHoverAttrs ).withElementInteractor( labelInteractor ) );
		}
		
		private void showTextEntry()
		{
			TextEntry entry = new TextEntry( text != null  ?  text  :  "", entryListener, validator );
			entry.grabCaretOnRealise();
			unit.setLiteralValue( entry );
		}
	}

	
	
	private String initialText;
	private Pres notSet;
	private EditableLabelListener listener;
	private TextEntry.TextEntryValidator validator;

	
	public EditableLabel(String initialText, Object notSet, EditableLabelListener listener)
	{
		this( initialText, notSet, listener, null );
	}
	
	public EditableLabel(String initialText, Object notSet, EditableLabelListener listener, TextEntry.TextEntryValidator validator)
	{
		this.initialText = initialText;
		this.notSet = Pres.coerce( notSet );
		this.listener = listener;
		this.validator = validator;
	}
	
	public EditableLabel(String initialText, Object notSet, EditableLabelListener listener, Pattern validatorRegex, String validationFailMessage)
	{
		this( initialText, notSet, listener, new TextEntry.RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useEditableLabelAttrs( style );
		
		LiteralUnit unit = new LiteralUnit( new Blank() );
		
		Pres unitPres = DefaultPerspective.instance.applyTo( unit.valuePresInFragment() );
		DPElement element = unitPres.present( ctx, usedStyle );
		return new EditableLabelControl( ctx, usedStyle, element, unit, initialText, notSet, listener, validator );
	}
}
