##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


def _jsIndent(xs):
	return '\n'.join( [ '  ' + x   for x in xs.split( '\n' ) ] )

_pageTemplate = \
"""
<html>
  <head>
    <style type="text/css">
      %s
    </style>
    <script type="text/javascript" src="jquery-1.2.6.js"></script>
    <script type="text/javascript">
function log(message) {
    if (!log.window_ || log.window_.closed) {
        var win = window.open("", null, "width=400,height=200," +
                              "scrollbars=yes,resizable=yes,status=no," +
                              "location=no,menubar=no,toolbar=no");
        if (!win) return;
        var doc = win.document;
        doc.write("<html><head><title>Debug Log</title></head>" +
                  "<body></body></html>");
        doc.close();
        log.window_ = win;
    }
    var logLine = log.window_.document.createElement("div");
    logLine.appendChild(log.window_.document.createTextNode(message));
    log.window_.document.body.appendChild(logLine);
}

$(document).ready(function()
{
  %s
});


    </script>
  </head>
  <body>
    %s
  </body>
  </html>
"""

def testPage(context, body):
	styles = ''
	return _pageTemplate  %  ( styles, context.getOnReadyScript(), body )



import unittest

class TestCase_Page (unittest.TestCase):
	def testPage(self):
		from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
		vctx 
	