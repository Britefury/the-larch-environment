//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.Controls.TextEntry.TextEntryValidator;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
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
	
	
	
	public static class EditableLabelControl extends Control implements IncrementalMonitorListener
	{
		private ClickElementInteractor labelInteractor = new ClickElementInteractor()
		{
			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				showTextEntry();
				return true;
			}
		};
		
		private TextEntry.TextEntryListener entryListener = new TextEntry.TextEntryListener()
		{
			@Override
			public void onAccept(TextEntry.TextEntryControl textEntry, String text)
			{
				if ( listener != null )
				{
					listener.onTextChanged( EditableLabelControl.this, text );
				}
				showLabel();
			}
		};

		
		
		private LSElement element;
		private LiveValue display;
		private LiveInterface value;
		private Pres notSet;
		private EditableLabelListener listener;
		private TextEntry.TextEntryValidator validator;
		
		
		public EditableLabelControl(PresentationContext ctx, StyleValues style, LSElement element, LiveValue display, LiveInterface value, Pres notSet,
				EditableLabelListener listener, TextEntry.TextEntryValidator validator)
		{
			super( ctx, style );
			
			this.element = element;
			this.display = display;
			this.value = value;
			this.notSet = notSet;
			this.listener = listener;
			this.validator = validator;
			
			
			Runnable refresh = new Runnable()
			{
				@Override
				public void run()
				{
					showLabel();
				}
			};
			element.queueImmediateEvent( refresh );
		}



		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		
		private void buildLabel()
		{
			Pres label;
			String text = (String)value.getValue();
			if ( text == null )
			{
				label = notSet;
			}
			else
			{
				label = new Label( text ).withStyleSheetFromAttr( Controls.editableLabelTextAttrs );
			}
			display.setLiteralValue( label.withStyleSheetFromAttr( Controls.editableLabelHoverAttrs ).withElementInteractor( labelInteractor ) );
		}
		
		private void showLabel()
		{
			buildLabel();
			
			value.addListener( this );
		}
		
		private void showTextEntry()
		{
			TextEntry entry = new TextEntry( value, entryListener ).validated( validator );
			entry.grabCaretOnRealise();
			display.setLiteralValue( entry );
			
			value.removeListener( this );
		}



		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			Runnable refresh = new Runnable()
			{
				@Override
				public void run()
				{
					buildLabel();
				}
			};
			element.queueImmediateEvent( refresh );
		}
	}

	
	
	private static class CommitListener implements EditableLabelListener
	{
		private LiveInterface value;
		
		public CommitListener(LiveInterface value)
		{
			this.value = value;
		}
		
		@Override
		public void onTextChanged(EditableLabelControl editableLabel, String text)
		{
			value.setLiteralValue( text );
		}
	}
	
	

	private LiveSource valueSource;
	private Pres notSet;
	private EditableLabelListener listener;
	private TextEntry.TextEntryValidator validator;

	
	private EditableLabel(LiveSource valueSource, Object notSet, EditableLabelListener listener, TextEntry.TextEntryValidator validator)
	{
		this.valueSource = valueSource;
		this.notSet = Pres.coerce( notSet );
		this.listener = listener;
		this.validator = validator;
	}
	
	public EditableLabel(String initialText, Object notSet, EditableLabelListener listener)
	{
		this( new LiveSourceValue( initialText ), notSet, listener, null );
	}
	
	public EditableLabel(LiveInterface value, Object notSet, EditableLabelListener listener)
	{
		this( new LiveSourceRef( value ), notSet, listener, null );
	}
	
	public EditableLabel(LiveValue value, Object notSet)
	{
		this( new LiveSourceRef( value ), notSet, new CommitListener( value ), null );
	}
	
	
	
	public EditableLabel validated(TextEntryValidator v)
	{
		return new EditableLabel( valueSource, notSet, listener, v );
	}
	
	public EditableLabel regexValidated(Pattern validatorRegex, String validationFailMessage)
	{
		return validated( new TextEntry.RegexTextEntryValidator( validatorRegex, validationFailMessage ) );
	}
	
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useEditableLabelAttrs( style );
		
		LiveValue display = new LiveValue( new Blank() );
		LiveInterface value = valueSource.getLive();
		
		Pres unitPres = DefaultPerspective.instance.applyTo( display );
		LSElement element = unitPres.present( ctx, usedStyle );
		return new EditableLabelControl( ctx, usedStyle, element, display, value, notSet, listener, validator );
	}
}
