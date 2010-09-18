//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.Presenters;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Table;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.PresCom.ErrorBox;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresenterRegistry;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GenericPresentersSQL extends GSymObjectPresenterRegistry
{
	public GenericPresentersSQL()
	{
		registerJavaObjectPresenter( ResultSet.class,  presenter_ResultSet );
	}



	public static final ObjectPresenter presenter_ResultSet = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
		{
			ResultSet results = (ResultSet)x;
			
			try
			{
				ResultSetMetaData metaData = results.getMetaData();
				int numColumns = metaData.getColumnCount();
				
				ArrayList<Object[]> cells = new ArrayList<Object[]>();
				
				Object header[] = new Object[numColumns];
				cells.add( header );
				for (int column = 0; column < numColumns; column++)
				{
					header[column] = columnStyle.applyTo( new Label( metaData.getColumnName( column + 1 ) ) );
				}
				
				
				while ( results.next() )
				{
					Object row[] = new Object[numColumns];
					cells.add( row );
					for (int column = 0; column < numColumns; column++)
					{
						row[column] = results.getObject( column + 1 );
					}
				}
				
				Object tableCells[][] = cells.toArray( new Object[0][] );
				Pres table = resultStyle.applyTo( new Table( tableCells ) );
				
				return new ObjectBox( "ResultSet", table );
			}
			catch (SQLException e)
			{
				return new ErrorBox( "SQL: Error presenting ResultSet", Pres.coerce( e ) );
			}
		}
	};



	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );
	
	
	private static final StyleSheet columnStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true );
	
	
	private static final StyleSheet resultStyle = staticStyle.withAttr( Primitive.tableRowSpacing, 2.0 ).withAttr( Primitive.tableColumnSpacing, 10.0 );
}

