//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Graphics;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import BritefuryJ.LSpace.TableBackgroundPainter;
import BritefuryJ.LSpace.TableElement;

public class TableBackgroundCellPainter implements TableBackgroundPainter
{
	private boolean paintHeaderRow;
	private Paint headerRowPaint;
	private Paint headerColumnPaints[], bodyRowPaints[];
	
	private TableBackgroundCellPainter(boolean paintHeaderRow, Paint headerRowPaint, Paint headerColumnPaints[], Paint bodyRowPaints[])
	{
		this.paintHeaderRow = paintHeaderRow;
		this.headerRowPaint = headerRowPaint;
		this.headerColumnPaints = headerColumnPaints;
		this.bodyRowPaints = bodyRowPaints;
	}
	
	public TableBackgroundCellPainter(Paint rowPaints[])
	{
		this( false, null, null, rowPaints );
	}
	
	public TableBackgroundCellPainter(Paint background)
	{
		this( false, null, null, new Paint[] { background } );
	}
	
	public TableBackgroundCellPainter()
	{
		this( false, null, null, null );
	}
	
	
	public TableBackgroundCellPainter headerRow(Paint paint)
	{
		return new TableBackgroundCellPainter( true, paint, headerColumnPaints, bodyRowPaints );
	}
	
	public TableBackgroundCellPainter headerColumn(Paint paint)
	{
		return new TableBackgroundCellPainter( paintHeaderRow, headerRowPaint, new Paint[] { paint }, bodyRowPaints );
	}
	
	public TableBackgroundCellPainter headerColumnCycle(Paint paints[])
	{
		return new TableBackgroundCellPainter( paintHeaderRow, headerRowPaint, paints, bodyRowPaints );
	}
	
	public TableBackgroundCellPainter body(Paint paint)
	{
		return new TableBackgroundCellPainter( paintHeaderRow, headerRowPaint, headerColumnPaints, new Paint[] { paint } );
	}
	
	public TableBackgroundCellPainter bodyRowCycle(Paint paints[])
	{
		return new TableBackgroundCellPainter( paintHeaderRow, headerRowPaint, headerColumnPaints, paints );
	}
	
	
	
	
	
	@Override
	public void paintTableBackground(TableElement table, Graphics2D graphics)
	{
		Paint prevPaint = graphics.getPaint();

		// Header row and column first
		if ( paintHeaderRow  &&  headerRowPaint != null  &&
				headerColumnPaints != null  &&  headerColumnPaints.length == 1  &&
				headerRowPaint.equals( headerColumnPaints[0] ) )
		{
			// Same paint for both header row and header column; paint in one go
			int numColumns = table.getNumColumns();
			int numRows = table.getNumRows();
			
			double x1 = table.getColumnBoundaryX( 1 );
			double x2 = table.getColumnBoundaryX( numColumns );
			double y1 = table.getRowBoundaryY( 1 );
			double y2 = table.getRowBoundaryY( numRows );
			
			Path2D.Double path = new Path2D.Double();
			path.moveTo( 0.0, 0.0 );
			path.lineTo( x2, 0.0 );
			path.lineTo( x2, y1 );
			path.lineTo( x1, y1 );
			path.lineTo( x1, y2 );
			path.lineTo( 0.0, y2 );
			path.closePath();

			graphics.setPaint( headerRowPaint );
			graphics.fill( path );
		}
		else
		{
			// Paint header row
			if ( paintHeaderRow  &&  headerRowPaint != null )
			{
				int numColumns = table.getNumColumns();
				
				double x1 = table.getColumnBoundaryX( numColumns );
				double y1 = table.getRowBoundaryY( 1 );
				
				graphics.setPaint( headerRowPaint );
				graphics.fill( new Rectangle2D.Double( 0.0, 0.0, x1, y1 ) );
			}
			
			// Paint header column
			if ( headerColumnPaints != null )
			{
				double x1 = table.getColumnBoundaryX( 1 );
				cyclePaintRows( table, graphics, headerColumnPaints, 0.0, x1 );
			}
		}
		
		
		// Paint body
		if ( bodyRowPaints != null )
		{
			int numColumns = table.getNumColumns();
			double x0 = headerColumnPaints != null  ?  table.getColumnBoundaryX( 1 )  :  0.0;
			double x1 = table.getColumnBoundaryX( numColumns );

			cyclePaintRows( table, graphics, bodyRowPaints, x0, x1 );
		}
		
		
		graphics.setPaint( prevPaint );
	}
	
	
	private void cyclePaintRows(TableElement table, Graphics2D graphics, Paint paints[], double x0, double x1)
	{
		if ( paints.length == 1 )
		{
			// Single paint; use for whole body
			if ( paints[0] != null )
			{
				int numRows = table.getNumRows();
				
				double y0 = paintHeaderRow  ?  table.getRowBoundaryY( 1 )  :  0.0;
				double y1 = table.getRowBoundaryY( numRows );
				
				graphics.setPaint( paints[0] );
				graphics.fill( new Rectangle2D.Double( x0, y0, x1 - x0, y1 - y0 ) );
			}
		}
		else
		{
			// Cycle of paints; alternate
			int numRows = table.getNumRows();

			int firstRow = paintHeaderRow  ?  1 : 0;
			int paintIndex = 0;
			double y0 = table.getRowBoundaryY( firstRow );
			for (int row = firstRow; row < numRows; row++)
			{
				double y1 = table.getRowBoundaryY( row + 1 );
				
				Paint paint = paints[paintIndex];
				if ( paint != null )
				{
					graphics.setPaint( paint );
					graphics.fill( new Rectangle2D.Double( x0, y0, x1 - x0, y1 - y0 ) );
				}
				
				paintIndex++;
				if ( paintIndex == paints.length )
				{
					paintIndex = 0;
				}
				y0 = y1;
			}
		}
	}

}
