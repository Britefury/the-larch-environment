//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.LSpace.Interactor.RealiseElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TextEditEvent;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


public class AutoCompleteList extends ControlPres
{
	public interface AutoCompleteChoiceFn
	{
		List<AutoCompleteItem> autoCompleteChoices(LSElement element);
	}


	public interface AutoCompleteListener
	{
		void complete(Object value);
	}

	private interface ItemChooseFn
	{
		void itemChosen(Object value);
	}



	public static class AutoCompleteItem
	{
		private static final StyleSheet itemStyle = StyleSheet.style( Primitive.hoverBackground.as( new FilledOutlinePainter( new Color( 0.95f, 0.95f, 0.95f ), new Color( 0.75f, 0.75f, 0.75f ) ) ) );
		private static final StyleSheet chosenItemStyle = StyleSheet.style( Primitive.background.as( new FilledOutlinePainter( new Color( 0.925f, 0.95f, 0.975f ), new Color( 0.725f, 0.75f, 0.775f ) ) ),
				Primitive.hoverBackground.as( new FilledOutlinePainter( new Color( 0.95f, 0.95f, 0.95f ), new Color( 0.75f, 0.75f, 0.75f ) ) ) );
		private static final double itemPadding = 2.0;


		private class ItemPushInteractor implements ClickElementInteractor
		{
			private ItemChooseFn chooseFn;


			private ItemPushInteractor(ItemChooseFn chooseFn)
			{
				this.chooseFn = chooseFn;
			}


			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				chooseFn.itemChosen( value );
				return true;
			}
		}



		private Pres presentation;
		public Object value;


		public AutoCompleteItem(Object presentation, Object value)
		{
			this.presentation = Pres.coerce( presentation );
			this.value = value;
		}



		protected Pres itemPres(boolean selected, ItemChooseFn onChoose)
		{
			final ItemPushInteractor interactor = new ItemPushInteractor( onChoose );

			StyleSheet style = selected  ? chosenItemStyle : itemStyle;
			Pres p = style.applyTo( new Bin( presentation.alignHPack() ).pad( itemPadding, itemPadding ) ).alignHExpand();
			p = p.withElementInteractor( interactor );
			return p;
		}
	}









	public static class AutoCompleteListControl extends Control
	{
		private class AutoCompleteInteractor implements KeyElementInteractor, TreeEventListener
		{
			public boolean keyPressed(LSElement element, KeyEvent event)
			{
				if ( event.getKeyCode() == KeyEvent.VK_TAB )
				{
					int mods = Modifier.getKeyModifiersFromEvent( event );
					if ( ( mods & ( Modifier.CTRL | Modifier.ALT | Modifier.ALT_GRAPH ) )  ==  0 )
					{
						int delta = ( mods & Modifier.SHIFT )  ==  0  ?  1  :  -1;
						move( delta );
						return true;
					}

				}
				else if ( event.getKeyCode() == KeyEvent.VK_ENTER )
				{
					return true;
				}
				return false;
			}

			public boolean keyReleased(LSElement element, KeyEvent event)
			{
				return event.getKeyCode() == KeyEvent.VK_ENTER;
			}

			public boolean keyTyped(LSElement element, KeyEvent event)
			{
				if ( event.getKeyChar() == '\n' )
				{
					chooseSelected();
					return true;
				}
				else if ( event.getKeyChar() == '\t' )
				{
					return true;
				}
				return false;
			}

			public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
			{
				if ( event instanceof TextEditEvent )
				{
					onEdit();
				}
				return false;
			}
		}


		private class AutoCompleteDetacher implements RealiseElementInteractor
		{
			public void elementRealised(LSElement element)
			{
			}

			public void elementUnrealised(LSElement element)
			{
				notifyAutoCompleteListUnrealised();
			}
		}




		private AutoCompleteChoiceFn autoCompleteChoiceFn;
		private LiveFunction listContentsLive;
		private LiveValue selectionLive = new LiveValue( -1 );
		private LiveInterface elementToMonitorLive;
		private LSElement currentElement = null;
		private AutoCompleteListener listener;
		private AutoCompleteInteractor interactor = new AutoCompleteInteractor();
		private AutoCompleteDetacher detacher = new AutoCompleteDetacher();
		private LSElement controlElement;




		protected AutoCompleteListControl(PresentationContext ctx, StyleValues style, LiveInterface elementToMonitorLive, AutoCompleteChoiceFn completer, AutoCompleteListener listener)
		{
			super( ctx, style );

			this.autoCompleteChoiceFn = completer;
			this.elementToMonitorLive = elementToMonitorLive;

			listContentsLive = new LiveFunction( new LiveFunction.Function()
			{
				public Object evaluate()
				{
					LSElement element = (LSElement)AutoCompleteListControl.this.elementToMonitorLive.getValue();

					if ( element != currentElement )
					{
						if ( currentElement != null )
						{
							detach( currentElement );
						}

						currentElement = element;

						if ( currentElement != null )
						{
							attach( currentElement );
						}
					}

					if ( element != null )
					{
						return autoCompleteChoiceFn.autoCompleteChoices( element );
					}
					else
					{
						return new ArrayList<AutoCompleteItem>();
					}
				}
			} );

			Pres p = pres();
			controlElement = p.present( ctx, style );

			this.listener = listener;
		}


		@Override
		public LSElement getElement()
		{
			return controlElement;
		}



		private void attach(LSElement elementToMonitor)
		{
			elementToMonitor.addElementInteractor( interactor );
			elementToMonitor.addTreeEventListener( interactor );
		}

		private void detach(LSElement elementToMonitor)
		{
			elementToMonitor.removeElementInteractor( interactor );
			elementToMonitor.removeTreeEventListener( interactor );
		}


		private void notifyAutoCompleteListUnrealised()
		{
			if ( currentElement != null )
			{
				detach( currentElement );
			}
		}

		private void onEdit()
		{
			// Trigger a refresh
			listContentsLive.onChanged();
		}


		private void complete(Object value)
		{
			if ( listener != null )
			{
				listener.complete( value );
			}
		}


		private void move(int delta)
		{
			@SuppressWarnings("unchecked")
			List<AutoCompleteItem> contents = (List<AutoCompleteItem>)listContentsLive.getValue();
			int length = contents.size();
			int selection = (Integer)selectionLive.getStaticValue();
			if ( selection == -1 )
			{
				selection = delta == 1  ?  0  :  length - 1;
			}
			else
			{
				selection += delta;
				selection = selection < 0  ?  length - 1  :  selection;
				selection = selection >= length  ?  0  :  selection;
			}
			selectionLive.setLiteralValue( selection );
		}


		private void chooseSelected()
		{
			@SuppressWarnings("unchecked")
			List<AutoCompleteItem> contents = (List<AutoCompleteItem>)listContentsLive.getValue();
			if ( contents.size() > 0 )
			{
				int selection = (Integer)selectionLive.getStaticValue();
				selection = selection == -1  ?  0  :  selection;
				complete( contents.get( selection ).value );
			}
		}


		private Pres pres()
		{
			final ItemChooseFn onItemChosen = new ItemChooseFn()
			{
				public void itemChosen(Object value)
				{
					complete( value );
				}
			};

			LiveFunction ac = new LiveFunction( new LiveFunction.Function()
			{
				public Object evaluate()
				{
					@SuppressWarnings("unchecked")
					List<AutoCompleteItem> contents = (List<AutoCompleteItem>)listContentsLive.getValue();
					int selection = (Integer)selectionLive.getValue();
					Pres itemPres[] = new Pres[contents.size()];
					int i = 0;
					for (AutoCompleteItem item: contents)
					{
						itemPres[i] = item.itemPres( i == selection, onItemChosen );
						i++;
					}
					return new Column( itemPres );
				}
			} );

			return ac.withElementInteractor( detacher );
		}
	}




	private LiveSource elementToMonitorSource;
	private AutoCompleteChoiceFn autoCompleteChoiceFn;
	private AutoCompleteListener listener;


	private AutoCompleteList(LiveSource elementToMonitorSource, AutoCompleteChoiceFn autoCompleteChoiceFn, AutoCompleteListener listener)
	{
		this.autoCompleteChoiceFn = autoCompleteChoiceFn;
		this.elementToMonitorSource = elementToMonitorSource;
		this.listener = listener;
	}


	public AutoCompleteList(LSElement elementToMonitor, AutoCompleteChoiceFn completer, AutoCompleteListener listener)
	{
		this( new LiveSourceValue( elementToMonitor ), completer, listener );
	}

	public AutoCompleteList(LiveInterface elementToMonitor, AutoCompleteChoiceFn completer, AutoCompleteListener listener)
	{
		this( new LiveSourceRef( elementToMonitor ), completer, listener );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		return new AutoCompleteListControl( ctx, style, elementToMonitorSource.getLive(), autoCompleteChoiceFn, listener );
	}
}
