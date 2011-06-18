//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.Incremental.IncrementalFunctionMonitor;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;

class AVResult implements IncrementalMonitorListener
{
	private WeakReference<DMNode> node;
	private AttributeEvaluationTable attribEvalTable;
	protected IncrementalFunctionMonitor incr;
	private HashSet<AVResult> inwardDependencies, outwardDependencies;
	protected Object value;
	
	
	
	protected AVResult(DMNode node, AttributeEvaluationTable attribEvalTable)
	{
		this.node = new WeakReference<DMNode>( node );
		this.attribEvalTable = attribEvalTable;
		incr = new IncrementalFunctionMonitor( this );
		inwardDependencies = new HashSet<AVResult>();
		outwardDependencies = new HashSet<AVResult>();
		
		incr.addListener( this );
	}
	
	


	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		DMNode n = node.get();
		
		if ( n != null )
		{
			attribEvalTable.onResultInvalidated( n );
		}
		for (AVResult d: inwardDependencies)
		{
			d.outwardDependencies.remove( this );
		}
		for (AVResult d: outwardDependencies)
		{
			d.inwardDependencies.remove( this );
		}
		inwardDependencies.clear();
		outwardDependencies.clear();
	}
	
	protected void linkToInwardDependency(AVResult dep)
	{
		inwardDependencies.add( dep );
		dep.outwardDependencies.add( this );
	}
}
