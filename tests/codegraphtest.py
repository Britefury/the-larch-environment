##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *
from Britefury.FileIO.IOXml import *


numsmgnodes = 0

class SendMessageNode (SheetGraphNode):
	def __init__(self):
		super( SendMessageNode, self ).__init__()
		global numsmgnodes
		numsmgnodes += 1

	description='Send message'
	target = SheetGraphSinkSingleField( 'target' )
	messageId = Field( str, '' )
	args = SheetGraphSinkMultipleField( 'args' )
	kwargs = SheetGraphSinkMultipleField( 'kwargs' )
	result = SheetGraphSourceField( 'result' )


class KWArgNode (SheetGraphNode):
	description='Keyword argument'
	argName = Field( str, '' )
	value = SheetGraphSinkSingleField( 'value' )
	owner = SheetGraphSourceField( 'owner' )



leafNodes = []
def makeGraph(maxNodes):
	graph = SheetGraph()
	leafNodes.append( SendMessageNode() )
	graph.nodes.append( leafNodes[0] )
	numNodes = 1
	while len( leafNodes ) > 0:
		node = leafNodes[0]
		del leafNodes[0]
		if numNodes < maxNodes:
			targetNode = SendMessageNode()
			argsNodes = [ SendMessageNode()  for i in xrange( 0, 2 ) ]
			graph.nodes.append( targetNode )
			for n in argsNodes:
				graph.nodes.append( n )
			node.target.append( targetNode.result )
			node.messageId = 'abc'
			for n in argsNodes:
				node.args.append( n.result )
			leafNodes.extend( [ targetNode ] + argsNodes )
			numNodes += 3
 	return graph




def iotest(numNodes):
	print 'Making graph'
	graph = makeGraph( numNodes )
	print 'Serialising graph out'
	docOut = OutputXmlDocument()
	docOut.getContentNode()  <<  graph

	print 'Writing graph'
	fOut = open( 'test.xml', 'w' )
	docOut.writeFile( fOut )
	fOut.close()

	print 'Reading graph'
	fIn = open( 'test.xml', 'r' )
	docIn = InputXmlDocument()
	docIn.parseFile( fIn )
	fIn.close()
	contentNode = docIn.getContentNode()
	if contentNode.isValid():
		print 'Serialising graph in'
		graph2 = SheetGraph()
		contentNode  >>  graph2

		print 'Serialising graph back out'
		docOut2 = OutputXmlDocument()
		docOut2.getContentNode()  <<  graph2

		print 'Writing graph back'
		fOut2 = open( 'test2.xml', 'w' )
		docOut2.writeFile( fOut2 )
		fOut2.close()

		if graph == graph2:
			print 'SUCCESS'
		else:
			print 'FAILURE'







if __name__ == '__main__':
	import sys

	if len( sys.argv ) == 1:
		print 'Enter a value to start making nodes'

		input()

		print 'making nodes'
		makeGraph(10000)
		print '%d nodes made, enter a value to quit' % ( numsmgnodes, )

		input()
	elif len( sys.argv ) >= 2:
		if sys.argv[1] == '--io':
			numNodes = 12
			if len( sys.argv ) >= 3:
				numNodes = int( sys.argv[2] )
			iotest( numNodes )
		if sys.argv[1] == '--ioprof':
			numNodes = 2048
			if len( sys.argv ) >= 3:
				numNodes = int( sys.argv[2] )
			import profile
			print 'Making graph'
			graph = makeGraph( numNodes )
			print 'Serialising graph out'
			docOut = OutputXmlDocument()
			profile.run( 'docOut.getContentNode()  <<  graph', 'seroutprof' )
			print 'Writing graph'
			fOut = open( 'test.xml', 'w' )
			profile.run( 'docOut.writeFile( fOut )', 'writeprof' )
			fOut.close()

			print 'Reading graph'
			fIn = open( 'test.xml', 'r' )
			docIn = InputXmlDocument()
			profile.run( 'docIn.parseFile( fIn )', 'readprof' )
			fIn.close()
			contentNode = docIn.getContentNode()
			if contentNode.isValid():
				print 'Serialising graph in'
				graph2 = DBGraph()
				profile.run( 'contentNode  >>  graph2', 'serinprof' )
		elif sys.argv[1] == '--speed':
			if len( sys.argv ) > 2:
				numNodes = int( sys.argv[2] )
			else:
				numNodes = 10240

			import datetime

			startTime = datetime.datetime.now()
			makeGraph( numNodes )
			endTime = datetime.datetime.now()

			elapsed = endTime - startTime
			seconds = elapsed.days * 86400.0  +  elapsed.seconds  +  elapsed.microseconds / 1000000.0
			print '%d nodes took %f seconds; %f nodes per second' % ( numNodes, seconds, numNodes / seconds )


