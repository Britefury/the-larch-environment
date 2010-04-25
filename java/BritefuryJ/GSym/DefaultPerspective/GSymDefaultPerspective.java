//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.DefaultPerspective;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;
import BritefuryJ.DocPresent.ListView.SpanListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymLocationResolver;
import BritefuryJ.GSym.GSymPerspective;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.ObjectView.GSymObjectViewLocationTable;
import BritefuryJ.GSym.ObjectView.ObjectPresenter;
import BritefuryJ.GSym.ObjectView.Presentable;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymDefaultPerspective implements GSymPerspective
{
	private class GSymObjectViewFragmentFunction implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			DPElement result;
			if ( x instanceof Presentable )
			{
				Presentable p = (Presentable)x;
				result = p.present( ctx, styleSheet, state );
			}
			else
			{
				ObjectPresenter presenter = getPresenterForJavaObject( x );
				if ( presenter != null )
				{
					result = presenter.presentObject( x, ctx, styleSheet, state );
				}
				else
				{
					result = presentJavaObject( x, ctx, styleSheet, state );
				}
			}
			result.setDebugName( x.getClass().getName() );
			return result;
		}
	}

	
	
	private static class DefaultPerspectiveLocationResolver implements GSymLocationResolver
	{
		private GSymDefaultPerspective perspective;
		
		
		public DefaultPerspectiveLocationResolver(GSymDefaultPerspective perspective)
		{
			this.perspective = perspective;
		}
		
		
		@Override
		public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
		{
			GSymSubject subject = perspective.resolveLocation( null, location.iterator() );
			if ( subject != null )
			{
				GSymViewContext viewContext = new GSymViewContext( subject, perspective.browserContext, null, persistentState );
				return viewContext.getPage();
			}
			else
			{
				return null;
			}
		}

		@Override
		public GSymSubject resolveLocationAsSubject(Location location)
		{
			return perspective.resolveLocation( null, location.iterator() );
		}
	}
	
	
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private GSymObjectViewFragmentFunction fragmentViewFn = new GSymObjectViewFragmentFunction();
	private DefaultPerspectiveLocationResolver locationResolver = new DefaultPerspectiveLocationResolver( this );
	private HashMap<Class<?>, ObjectPresenter> registeredObjectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<Class<?>, ObjectPresenter> objectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private GSymBrowserContext browserContext;
	
	
	public GSymDefaultPerspective(GSymBrowserContext browserContext)
	{
		this.browserContext = browserContext;

		registerDefaultObjectPresenters();
	}

	


	public GSymViewFragmentFunction getFragmentViewFunction()
	{
		return fragmentViewFn;
	}
	
	public StyleSheet getStyleSheet()
	{
		return PrimitiveStyleSheet.instance;
	}
	
	public AttributeTable getInitialState()
	{
		return AttributeTable.instance;
	}
	
	public Object createInitialState(GSymSubject subject)
	{
		return null;
	}

	public EditHandler getEditHandler()
	{
		return null;
	}


	public GSymSubject resolveLocation(GSymSubject enclosingSubject, Location.TokenIterator relativeLocation)
	{
		Object x = locationTable.getObjectAtLocation( relativeLocation );
		if ( x != null )
		{
			String title = x != null  ?  x.getClass().getName()  :  "<null>";
			return new GSymSubject( x, this, title, AttributeTable.instance );
		}
		else
		{
			return null;
		}
	}



	public Location getLocationForObject(Object x)
	{
		return locationTable.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		return locationTable.getObjectAtLocation( location.iterator() );
	}
	
	
	public GSymLocationResolver getLocationResolver()
	{
		return locationResolver;
	}
	
	
	
	public void registerObjectPresenter(Class<?> cls, ObjectPresenter presenter)
	{
		registeredObjectPresenters.put( cls, presenter );
		objectPresenters.put( cls, presenter );
	}
	
	
	private ObjectPresenter getPresenterForJavaObject(Object x)
	{
		Class<?> xClass = x.getClass();
		
		// See if we have a presenter
		ObjectPresenter presenter = registeredObjectPresenters.get( xClass );
		if ( presenter != null )
		{
			return presenter;
		}
		
		// No, we don't
		if ( xClass != Object.class )
		{
			// The class of x is a subclass of Object
			Class<?> superClass = xClass.getSuperclass();
			
			while ( superClass != Object.class )
			{
				// See if we can get a presenter for this superclass
				presenter = objectPresenters.get( superClass );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					objectPresenters.put( xClass, presenter );
					return presenter;
				}
				
				// Try the next class up the hierarchy
				superClass = superClass.getSuperclass();
			}
		}
		
		// Now check the interfaces
		
		// First, build a list of all interfaces implemented by x, and its superclasses.
		Class<?> c = xClass;
		HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
		Stack<Class<?>> interfaceStack = new Stack<Class<?>>();
		while ( c != Object.class )
		{
			for (Class<?> iface: c.getInterfaces())
			{
				presenter = objectPresenters.get( iface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					objectPresenters.put( xClass, presenter );
					return presenter;
				}
				if ( !interfaces.contains( iface ) )
				{
					interfaces.add( iface );
					interfaceStack.add( iface );
				}
			}
			c = c.getSuperclass();
		}
		
		// Now check the super interfaces
		while ( !interfaceStack.empty() )
		{
			Class<?> interfaceFromStack = interfaceStack.pop();
			for (Class<?> superInterface: interfaceFromStack.getInterfaces())
			{
				presenter = objectPresenters.get( superInterface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					objectPresenters.put( xClass, presenter );
					return presenter;
				}
				if ( !interfaces.contains( superInterface ) )
				{
					interfaces.add( superInterface );
					interfaceStack.add( superInterface );
				}
			}
		}
		
		return null;
	}
	
	
	
	private void registerDefaultObjectPresenters()
	{
		registerObjectPresenter( Character.class, PrimitivePresenter.presenter_Character );
		registerObjectPresenter( String.class,  PrimitivePresenter.presenter_String );
		registerObjectPresenter( Integer.class,  PrimitivePresenter.presenter_Integer );
		registerObjectPresenter( Short.class,  PrimitivePresenter.presenter_Short );
		registerObjectPresenter( Long.class,  PrimitivePresenter.presenter_Long );
		registerObjectPresenter( Byte.class,  PrimitivePresenter.presenter_Byte );
		registerObjectPresenter( Float.class,  PrimitivePresenter.presenter_Float );
		registerObjectPresenter( Double.class,  PrimitivePresenter.presenter_Double );
		registerObjectPresenter( Boolean.class,  PrimitivePresenter.presenter_Boolean );

		registerObjectPresenter( PyString.class,  PrimitivePresenter.presenter_PyString );
		registerObjectPresenter( PyUnicode.class,  PrimitivePresenter.presenter_PyUnicode );
		registerObjectPresenter( PyInteger.class,  PrimitivePresenter.presenter_PyInteger );
		registerObjectPresenter( PyLong.class,  PrimitivePresenter.presenter_PyLong );
		registerObjectPresenter( PyFloat.class,  PrimitivePresenter.presenter_PyFloat );
		registerObjectPresenter( PyBoolean.class,  PrimitivePresenter.presenter_PyBoolean );

		
		registerObjectPresenter( List.class,  PrimitivePresenter.presenter_List );
		registerObjectPresenter( BufferedImage.class,  PrimitivePresenter.presenter_BufferedImage );
}
	
	
	private static DPElement presentJavaObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPElement className = javaObjectClassNameStyle.staticText( x.getClass().getName() );
		DPElement asString = asStringStyle.text( x.toString() );
		return javaObjectBorderStyle.border( javaObjectBorderStyle.vbox( Arrays.asList( new DPElement[] { className, asString.padX( 10.0 ) } ) ) );
	}
	
	
	
	private static class PrimitivePresenter
	{
		public static DPElement presentChar(char c, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			String str = Character.toString( c );
			ArrayList<DPElement> lineContent = new ArrayList<DPElement>();
			lineContent.add( punctuationStyle.staticText(  "'" ) );
			lineContent.add( stringContentStyle.staticText( str ) );
			lineContent.add( punctuationStyle.staticText(  "'" ) );
			return PrimitiveStyleSheet.instance.hbox( lineContent );
		}
		
		public static DPElement presentString(String str, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			String lines[] = str.split( "\n" );
			if ( lines.length == 1 )
			{
				ArrayList<DPElement> lineContent = new ArrayList<DPElement>();
				lineContent.add( punctuationStyle.staticText(  "\"" ) );
				unescapeString( lineContent, str );
				lineContent.add( punctuationStyle.staticText(  "\"" ) );
				return PrimitiveStyleSheet.instance.hbox( lineContent );
			}
			else
			{
				ArrayList<DPElement> lineElements = new ArrayList<DPElement>();
				int index = 0;
				for (String line: lines)
				{
					ArrayList<DPElement> lineContent = new ArrayList<DPElement>();
					if ( index == 0 )
					{
						lineContent.add( punctuationStyle.staticText(  "\"" ) );
					}
					unescapeString( lineContent, line );
					if ( index == lines.length - 1 )
					{
						lineContent.add( punctuationStyle.staticText(  "\"" ) );
					}
					lineElements.add( PrimitiveStyleSheet.instance.hbox( lineContent ) );
					index++;
				}
				return multiLineStringStyle.vbox( lineElements );
			}
		}
		
		static void unescapeString(ArrayList<DPElement> elements, String x)
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < x.length(); i++)
			{
				char c = x.charAt( i );
				
				DPElement escapeItem = null;

				if ( c == '\r' )
				{
					escapeItem = escapeStyle.staticText( "\\r" );
				}
				else if ( c == '\t' )
				{
					escapeItem = escapeStyle.staticText( "\\t" );
				}
				
				if ( escapeItem != null )
				{
					if ( builder.length() > 0 )
					{
						elements.add( stringContentStyle.staticText( builder.toString() ) );
						builder = new StringBuilder();
					}
					elements.add( escapeItem );
				}
				else
				{
					builder.append( c );
				}
			}
			
			if ( builder.length() > 0 )
			{
				elements.add( stringContentStyle.staticText( builder.toString() ) );
			}
		}
		
		public static DPElement presentByte(byte b, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			return integerStyle.staticText( Integer.toHexString( (int)b ) );
		}
		
		
		public static DPElement presentInt(int x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			return integerStyle.staticText( Integer.toString( x ) );
		}
		
		public static DPElement presentDouble(double x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			String asText = Double.toString( x );
			
			if ( asText.contains( "e" ) )
			{
				return presentSIDouble( asText, asText.indexOf( "e" ) );
			}
			else if ( asText.contains( "E" ) )
			{
				return presentSIDouble( asText, asText.indexOf( "E" ) );
			}
			else
			{
				return floatStyle.staticText( asText );
			}
		}
		
		private static DPElement presentSIDouble(String textValue, int expIndex)
		{
			DPElement mantissa = floatStyle.staticText( textValue.substring( 0, expIndex ) + "*10" );
			DPElement exponent = floatStyle.staticText( textValue.substring( expIndex + 1, textValue.length() ) );
			return PrimitiveStyleSheet.instance.scriptRSuper( mantissa, exponent );
		}
		
		public static DPElement presentBoolean(boolean b, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			if ( b )
			{
				return booleanStyle.staticText( "True" );
			}
			else
			{
				return booleanStyle.staticText( "False" );
			}
		}

	
	
		public static final ObjectPresenter presenter_Character = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentChar( (Character)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_String = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentString( (String)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Byte = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentByte( (Byte)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Short = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentInt( ((Short)x).intValue(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Integer = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentInt( (Integer)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Long = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentInt( ((Long)x).intValue(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Float = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentDouble( ((Float)x).doubleValue(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Double = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentDouble( (Double)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Boolean = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentBoolean( (Boolean)x, ctx, styleSheet, state );
			}
		};
		
		

		
		public static final ObjectPresenter presenter_PyString = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentString( ((PyString)x).asString(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_PyUnicode = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentString( ((PyUnicode)x).asString(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_PyInteger = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentInt( ((PyInteger)x).asInt(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_PyLong = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return integerStyle.staticText( x.toString() );
			}
		};
		
		public static final ObjectPresenter presenter_PyFloat = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentDouble( ((PyFloat)x).asDouble(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_PyBoolean = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				return PrimitivePresenter.presentBoolean( ((PyBoolean)x).__nonzero__(), ctx, styleSheet, state );
			}
		};
		
		
		
		
		public static final ObjectPresenter presenter_BufferedImage = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				BufferedImage image = (BufferedImage)x;
				double width = (double)image.getWidth();
				double height = (double)image.getHeight();
				
				if ( width > height  &&  width > 96.0 )
				{
					height *= ( 96.0 / width );
					width = 96.0;
				}
				else if ( height > width  &&  height > 96.0 )
				{
					width *= ( 96.0 / height );
					height = 96.0;
				}
				
				return PrimitiveStyleSheet.instance.image( image, width, height );
			}
		};

		public static final ObjectPresenter presenter_List = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
			{
				List<?> list = (List<?>)x;
				
				ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
				for (Object item: list)
				{
					itemViews.add( ctx.presentFragmentWithDefaultPerspective( item ) );
				}
				
				return listListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
			}
		};
	}
	
	
	
	private static final PrimitiveStyleSheet punctuationStyle = PrimitiveStyleSheet.instance.withForeground( Color.blue );
	private static final PrimitiveStyleSheet delimStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.1f, 0.3f, 0.4f ) ).withFont( new Font( "Sans serif", Font.BOLD, 14 ) );
	private static final PrimitiveStyleSheet escapeStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.15f, 0.35f ) ).withBackground( new FillPainter( new Color( 0.8f, 1.0f, 1.0f ) ) ); 
	private static final PrimitiveStyleSheet stringContentStyle = PrimitiveStyleSheet.instance; 
	private static final PrimitiveStyleSheet multiLineStringStyle = PrimitiveStyleSheet.instance.withBackground( new FillPainter( new Color( 0.8f, 0.8f, 1.0f ) ) );
	private static final PrimitiveStyleSheet integerStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.5f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet floatStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.25f, 0.0f, 0.5f ) );
	private static final PrimitiveStyleSheet booleanStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.0f, 0.5f, 0.0f ) ).withTextSmallCaps( true );
	
	private static final PrimitiveStyleSheet javaObjectBorderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 2.0, 2.0, 5.0, 5.0, new Color( 63, 70, 95 ), null ) );
	private static final PrimitiveStyleSheet javaObjectClassNameStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 63, 70, 95 ) ).withFont( new Font( "Sans serif", Font.BOLD, 12 ) );
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.ITALIC, 14 ) );


	private static final SeparatorElementFactory commaFactory = new SeparatorElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet, int index, DPElement child)
		{
			return punctuationStyle.staticText( "," );
		}
	};

	private static final ElementFactory spaceFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return PrimitiveStyleSheet.instance.staticText( " " );
		}
	};
	
	private static final ElementFactory openBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "[" );
		}
	};
	
	private static final ElementFactory closeBracketFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "]" );
		}
	};
	
	
	private static SpanListViewLayoutStyleSheet span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( true ).withAddParagraphIndentMarkers( true ).withAddLineBreakCost( true );
	private static ListViewStyleSheet listListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openBracketFactory ).withEndDelimFactory( closeBracketFactory ).withListLayout( span_listViewLayout );
}
