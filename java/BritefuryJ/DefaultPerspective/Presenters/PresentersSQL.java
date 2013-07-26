//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.Pres.UI.SectionHeading3;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersSQL extends ObjectPresenterRegistry
{
	public PresentersSQL()
	{
		registerJavaObjectPresenter( Connection.class,  presenter_Connection );
		registerJavaObjectPresenter( ResultSet.class,  presenter_ResultSet );
	}



	public static final ObjectPresenter presenter_Connection = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Connection connection = (Connection)x;
			
			try
			{
				DatabaseMetaData dbMeta = connection.getMetaData();
				ResultSet results = dbMeta.getTables( null, null, null, null );
				
				Pres url = new HorizontalField( "URL", new Label( dbMeta.getURL() ) );
				
				Pres tablesPres = null;
				try
				{
					tablesPres = present_ResultSet( results );
				}
				catch (SQLException e)
				{
					tablesPres = Pres.coercePresentingNull(e);
				}
				Pres tables = new DropDownExpander( new SectionHeading3( "Tables" ), tablesPres );
				
				return new ObjectBox( "Connection", new Column( new Object[] { url, tables } ) );
			}
			catch (SQLException e)
			{
				return new ErrorBox( "SQL: Error presenting ResultSet", Pres.coercePresentingNull(e) );
			}
		}
	};

	
	private static Pres present_ResultSet(ResultSet results) throws SQLException
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
		
		Object tableCells[][] = cells.toArray( new Object[cells.size()][] );
		return resultStyle.applyTo( new Table( tableCells ) );
	}
	
	
	public static final ObjectPresenter presenter_ResultSet = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			ResultSet results = (ResultSet)x;
			
			try
			{
				Pres r = present_ResultSet( results );
				
				return new ObjectBox( "ResultSet", r );
			}
			catch (SQLException e)
			{
				return new ErrorBox( "SQL: Error presenting ResultSet", Pres.coercePresentingNull(e) );
			}
		}
	};


	private static final StyleSheet staticStyle = StyleSheet.style( Primitive.editable.as( false ) );


	private static final StyleSheet sectionHeadingStyle = staticStyle.withValues( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ), Primitive.fontBold.as( true ), Primitive.fontFace.as( "Serif" ) );


	private static final StyleSheet columnStyle = staticStyle.withValues( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ), Primitive.fontBold.as( true ) );


	private static final StyleSheet resultStyle = staticStyle.withValues( Primitive.tableRowSpacing.as( 1.0 ), Primitive.tableColumnSpacing.as( 25.0 ) );
}

