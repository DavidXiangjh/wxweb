	
	
	
/**
	change user's locale settings
*/
function setLocale( url )
{
	var obj = YAHOO.util.Connect.createXhrObject( -1 );
	//first get the url to set the locale value
	var httpReq = obj.conn;
	httpReq.open("GET", url, false);
	httpReq.send(null);
	var content = httpReq.responseText;

	var state = curState;
	var tmp = state.split(".");
					
	//do the menu initialize
	EMP.widget.EMPMenu.menus[tmp[0]].reset();
	initEMPMenu( EMP.widget.EMPMenu.menus[tmp[0] ] );
	
}

	