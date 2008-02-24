import sys

from Britefury.DocModel.DMIO import readSX

from Britefury.gSym.gSymEnvironment import createGSymGLispEnvironment



if len( sys.argv ) != 2:
	print 'Usage:'
	print '\t%s <script_file>'
	sys.exit()

	
env = createGSymGLispEnvironment()
env.name = '<script>'
scriptFilename = sys.argv[1]

result = env.evaluate( readSX( file( scriptFilename, 'r' ) ) )

print 'RESULT:'
print result.actions[0][1]()



