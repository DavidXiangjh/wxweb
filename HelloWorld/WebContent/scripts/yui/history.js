 
 
    var bookmarkedSection = YAHOO.util.History.getBookmarkedState( "emp" );
    var initSection = bookmarkedSection || "yuiIndex.do";
    var curState = null;

    try {

	    // Register our only module. Module registration MUST take
	    // place before calling YAHOO.util.History.initialize.
	    YAHOO.util.History.register( "emp", initSection, function( state ) {
	        // This is called after calling YAHOO.util.History.navigate, or after the user
        // has trigerred the back/forward button. We cannot discrminate between
        // these two situations.
  		if( state != curState )
   		{
   			//alert( 'Navagate to :' + state  + ':' + curState );
   			var tmp = state.split(".");
			try{
	  			EMP.widget.EMPMenu.getEMPMenu(tmp[0]).showMenuWithoutHistory( tmp[1]);
	  			curState = state;
	  		}catch(ee)
	  		{
	  		}
  		}
  			
		//load the state ....  		
    } );


        YAHOO.util.History.initialize( "blank.html" );
    } catch ( e ) 
    {
    	alert("failed to initialize Yahoo history!" + e);
     }
 