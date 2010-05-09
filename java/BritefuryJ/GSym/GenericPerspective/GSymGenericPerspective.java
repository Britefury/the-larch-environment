//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;
import BritefuryJ.DocPresent.ListView.SpanListViewLayoutStyleSheet;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymLocationResolver;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericPerspective extends GSymAbstractPerspective
{
	private static class GenericPerspectiveLocationResolver implements GSymLocationResolver
	{
		private GSymGenericPerspective perspective;
		
		
		public GenericPerspectiveLocationResolver(GSymGenericPerspective perspective)
		{
			this.perspective = perspective;
		}
		
		
		@Override
		public GSymSubject resolveLocationAsSubject(Location location)
		{
			return perspective.resolveRelativeLocation( null, location.iterator() );
		}
	}
	
	
	private GSymObjectViewLocationTable locationTable = new GSymObjectViewLocationTable();
	private GenericPerspectiveLocationResolver locationResolver = new GenericPerspectiveLocationResolver( this );
	private HashMap<Class<?>, ObjectPresenter> registeredJavaObjectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<Class<?>, ObjectPresenter> javaObjectPresenters = new HashMap<Class<?>, ObjectPresenter>();
	private HashMap<PyType, PyObjectPresenter> registeredPythonObjectPresenters = new HashMap<PyType, PyObjectPresenter>();
	private HashMap<PyType, PyObjectPresenter> pythonObjectPresenters = new HashMap<PyType, PyObjectPresenter>();
	
	
	public GSymGenericPerspective()
	{
		registerDefaultObjectPresenters();
	}

	


	@Override
	public DPElement present(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
	{
		DPElement result = null;
		GenericPerspectiveStyleSheet genericStyleSheet = null;
		if ( styleSheet instanceof GenericPerspectiveStyleSheet )
		{
			genericStyleSheet = (GenericPerspectiveStyleSheet)styleSheet;
		}
		else
		{
			genericStyleSheet = GenericPerspectiveStyleSheet.instance;
		}
		
		
		PyObject pyX = null;

		// Java object presentation protocol - Presentable interface
		if ( x instanceof Presentable )
		{
			// @x is an instance of @Presentable; use Presentable#present()
			Presentable p = (Presentable)x;
			result = p.present( ctx, genericStyleSheet, state );
		}
		
		// Python object presentation protocol
		if ( result == null  &&  x instanceof PyObject )
		{
			// @x is a Python object - if it offers a __present__ method, use that
			pyX = (PyObject)x;
			PyObject __present__ = null;
			try
			{
				__present__ = pyX.__getattr__( "__present__" );
			}
			catch (PyException e)
			{
				__present__ = null;
			}
			
			if ( __present__ != null  &&  __present__.isCallable() )
			{
				result = Py.tojava( __present__.__call__( Py.java2py( ctx ), Py.java2py( styleSheet ), Py.java2py( state ) ),  DPElement.class );
			}
			
			
			// __present__ did not succeed. Try the registered presenters.
			if ( result == null )
			{
				// Now try Python object presenters
				PyType typeX = pyX.getType();
				
				PyObjectPresenter presenter = getPresenterForPythonType( typeX );
				if ( presenter != null )
				{
					result = presenter.presentObject( pyX, ctx, genericStyleSheet, state );
				}
			}
		}
		
		// Java object presentation protocol - registered presenters
		if ( result == null )
		{
			ObjectPresenter presenter = getPresenterForJavaObject( x );
			if ( presenter != null )
			{
				result = presenter.presentObject( x, ctx, genericStyleSheet, state );
			}
		}
		
		// Fallback - use Java or Python toString() / __str__() methods
		if ( result == null )
		{
			if ( pyX != null )
			{
				result = presentPythonObjectAsString( pyX, ctx, genericStyleSheet, state );
			}
			else
			{
				result = presentJavaObjectAsString( x, ctx, genericStyleSheet, state );
			}
		}
		
		result.setDebugName( x.getClass().getName() );
		return result;
	}

	
	
	@Override
	public StyleSheet getStyleSheet()
	{
		return GenericPerspectiveStyleSheet.instance;
	}
	
	@Override
	public AttributeTable getInitialInheritedState()
	{
		return AttributeTable.instance;
	}
	
	@Override
	public EditHandler getEditHandler()
	{
		return null;
	}


	public GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, Location.TokenIterator relativeLocation)
	{
		Object x = locationTable.getObjectAtLocation( relativeLocation );
		if ( x != null )
		{
			String title = x != null  ?  x.getClass().getName()  :  "<null>";
			return new GSymSubject( x, this, title, AttributeTable.instance, null );
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
	
	
	
	public void registerJavaObjectPresenter(Class<?> cls, ObjectPresenter presenter)
	{
		registeredJavaObjectPresenters.put( cls, presenter );
		javaObjectPresenters.put( cls, presenter );
	}
	
	public void registerPythonObjectPresenter(PyType type, PyObjectPresenter presenter)
	{
		registeredPythonObjectPresenters.put( type, presenter );
		pythonObjectPresenters.put( type, presenter );
	}
	
	
	private ObjectPresenter getPresenterForJavaObject(Object x)
	{
		Class<?> xClass = x.getClass();
		
		// See if we have a presenter
		ObjectPresenter presenter = registeredJavaObjectPresenters.get( xClass );
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
				presenter = javaObjectPresenters.get( superClass );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
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
				presenter = javaObjectPresenters.get( iface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
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
				presenter = javaObjectPresenters.get( superInterface );
				if ( presenter != null )
				{
					// Yes - cache it for future tests
					javaObjectPresenters.put( xClass, presenter );
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
	
	
	private PyObjectPresenter getPresenterForPythonType(PyType typeX)
	{
		// See if we have a presenter
		PyObjectPresenter presenter = registeredPythonObjectPresenters.get( typeX );
		if ( presenter != null )
		{
			return presenter;
		}
		
		// No, we don't
		PyTuple mro = typeX.getMro();
		
		for (PyObject t: mro.getArray())
		{
			PyType superType = (PyType)t;
			
			// See if we can get a presenter for this superclass
			presenter = pythonObjectPresenters.get( superType );
			if ( presenter != null )
			{
				// Yes - cache it for future tests
				pythonObjectPresenters.put( typeX, presenter );
				return presenter;
			}
		}

		return null;
	}
	
	
	private void registerDefaultObjectPresenters()
	{
		registerJavaObjectPresenter( Character.class, BasicPresenters.presenter_Character );
		registerJavaObjectPresenter( String.class,  BasicPresenters.presenter_String );
		registerJavaObjectPresenter( Integer.class,  BasicPresenters.presenter_Integer );
		registerJavaObjectPresenter( Short.class,  BasicPresenters.presenter_Short );
		registerJavaObjectPresenter( Long.class,  BasicPresenters.presenter_Long );
		registerJavaObjectPresenter( Byte.class,  BasicPresenters.presenter_Byte );
		registerJavaObjectPresenter( Float.class,  BasicPresenters.presenter_Float );
		registerJavaObjectPresenter( Double.class,  BasicPresenters.presenter_Double );
		registerJavaObjectPresenter( Boolean.class,  BasicPresenters.presenter_Boolean );

		registerPythonObjectPresenter( PyTuple.TYPE,  BasicPresenters.presenter_PyTuple );
		
		registerJavaObjectPresenter( List.class,  BasicPresenters.presenter_List );
		registerJavaObjectPresenter( BufferedImage.class,  BasicPresenters.presenter_BufferedImage );
		registerJavaObjectPresenter( Shape.class,  BasicPresenters.presenter_Shape );
		registerJavaObjectPresenter( Color.class,  BasicPresenters.presenter_Color );
	}
	
	
	private static DPElement presentJavaObjectAsString(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		return styleSheet.objectBox( x.getClass().getName(), asStringStyle.staticText( x.toString() ) );
	}
	
	private static DPElement presentPythonObjectAsString(PyObject pyX, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
	{
		PyType typeX = pyX.getType();
		return styleSheet.objectBox( typeX.getName(), asStringStyle.staticText( pyX.toString() ) );
	}
	
	
	
	private static class BasicPresenters
	{
		public static final ObjectPresenter presenter_Boolean = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentBoolean( (Boolean)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Byte = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentByte( (Byte)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Character = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentChar( (Character)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Short = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentShort( (Short)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Integer = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentInt( (Integer)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Long = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentLong( (Long)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Float = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentDouble( ((Float)x).doubleValue(), ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_Double = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentDouble( (Double)x, ctx, styleSheet, state );
			}
		};
		
		public static final ObjectPresenter presenter_String = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				return GSymPrimitivePresenter.presentString( (String)x, ctx, styleSheet, state );
			}
		};
		
		
		

		public static final PyObjectPresenter presenter_PyTuple = new PyObjectPresenter()
		{
			public DPElement presentObject(PyObject x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				PyTuple tuple = (PyTuple)x;
				
				ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
				for (Object item: tuple)
				{
					itemViews.add( ctx.presentFragmentWithGenericPerspective( item ) );
				}
				
				return tupleListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
			}
		};

		

		public static final ObjectPresenter presenter_List = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				List<?> list = (List<?>)x;
				
				ArrayList<DPElement> itemViews = new ArrayList<DPElement>();
				for (Object item: list)
				{
					itemViews.add( ctx.presentFragmentWithGenericPerspective( item ) );
				}
				
				return listListViewStyle.createListElement( itemViews, TrailingSeparator.NEVER );
			}
		};
		
		public static final ObjectPresenter presenter_Shape = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				Shape shape = (Shape)x;
//				Rectangle2D bounds = shape.getBounds2D();
//				double offsetX = -bounds.getMinX(), offsetY = -bounds.getMinY();
//				double width = bounds.getWidth(), height = bounds.getHeight();
//				
//				double scale = 1.0;
//				if ( width > height  &&  width > 96.0 )
//				{
//					scale = 96.0 / width;
//				}
//				else if ( height > width  &&  height > 96.0 )
//				{
//					scale = 96.0 / height;
//				}
				
				return styleSheet.objectBox( x.getClass().getName(), PrimitiveStyleSheet.instance.shape( shape ) );
			}
		};

		public static final ObjectPresenter presenter_BufferedImage = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
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

		public static final ObjectPresenter presenter_Color = new ObjectPresenter()
		{
			public DPElement presentObject(Object x, GSymFragmentView ctx, GenericPerspectiveStyleSheet styleSheet, AttributeTable state)
			{
				Color colour = (Color)x;
				
				DPElement title = colourObjectBoxStyle.objectTitle( "java.awt.Color" );
				
				DPElement red = colourRedStyle.staticText( "R=" + String.valueOf( colour.getRed() ) );
				DPElement green = colourGreenStyle.staticText( "G=" + String.valueOf( colour.getGreen() ) );
				DPElement blue = colourBlueStyle.staticText( "B=" + String.valueOf( colour.getBlue() ) );
				DPElement alpha = colourAlphaStyle.staticText( "A=" + String.valueOf( colour.getAlpha() ) );
				
				DPElement components = colourBoxStyle.hbox( new DPElement[] { red, green, blue, alpha } );
				
				DPElement textBox = PrimitiveStyleSheet.instance.vbox( new DPElement[] { title, components } );
				
				DPElement swatch = PrimitiveStyleSheet.instance.withShapePainter( new FillPainter( colour ) ).box( 50.0, 20.0 ).alignVExpand();
				
				DPElement contents = colourBoxStyle.hbox( new DPElement[] { textBox, swatch } );
				
				return colourObjectBoxStyle.objectBorder( contents );
			}
		};
	}
	
	
	
	private static final PrimitiveStyleSheet punctuationStyle = PrimitiveStyleSheet.instance.withForeground( Color.blue );
	private static final PrimitiveStyleSheet delimStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.1f, 0.3f, 0.4f ) ).withFontBold( true ).withFontSize( 14 );
	
	
	private static final GenericPerspectiveStyleSheet colourObjectBoxStyle = GenericPerspectiveStyleSheet.instance.withObjectBorderAndTitlePaint( new Color( 0.0f, 0.1f, 0.4f ) );
	private static final PrimitiveStyleSheet colourRedStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.75f, 0.0f, 0.0f ) );
	private static final PrimitiveStyleSheet colourGreenStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.75f, 0.0f ) );
	private static final PrimitiveStyleSheet colourBlueStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.0f, 0.0f, 0.75f ) );
	private static final PrimitiveStyleSheet colourAlphaStyle = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( new Color( 0.3f, 0.3f, 0.3f ) );
	private static final PrimitiveStyleSheet colourBoxStyle = PrimitiveStyleSheet.instance.withHBoxSpacing( 5.0 );

	
	private static final PrimitiveStyleSheet asStringStyle = PrimitiveStyleSheet.instance.withFontItalic( true ).withFontSize( 14 );


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
	
	private static final ElementFactory openParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( "(" );
		}
	};
	
	private static final ElementFactory closeParenFactory = new ElementFactory()
	{
		public DPElement createElement(StyleSheet styleSheet)
		{
			return delimStyle.staticText( ")" );
		}
	};
	
	
	private static SpanListViewLayoutStyleSheet span_listViewLayout = SpanListViewLayoutStyleSheet.instance.withAddLineBreaks( true ).withAddParagraphIndentMarkers( true ).withAddLineBreakCost( true );
	private static ListViewStyleSheet listListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openBracketFactory ).withEndDelimFactory( closeBracketFactory ).withListLayout( span_listViewLayout );
	private static ListViewStyleSheet tupleListViewStyle = ListViewStyleSheet.instance.withSeparatorFactory( commaFactory ).withSpacingFactory( spaceFactory )
		.withBeginDelimFactory( openParenFactory ).withEndDelimFactory( closeParenFactory ).withListLayout( span_listViewLayout );
}
