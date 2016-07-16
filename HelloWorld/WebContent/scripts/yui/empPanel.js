
	function togglePanel( div )
	{
		if( div == null )
			return;
		
		var id = div.id;
		var idx = id.indexOf('_');
		id =id.substring(0, idx );
		id = id + '_bd';
		var className = div.className;
		
	//	alert( className );
		if( className == 'empPanel_min' )
		{
			div.className = 'empPanel_max';
			document.getElementById(id).style.display="none";
		}
		else
		{
			div.className = 'empPanel_min';
			document.getElementById(id).style.display="";
		}
	}


	function closePanel( div )
	{
		if( div == null )
			return;
		
		var panelDiv = div.parentNode.parentNode;
		if( panelDiv != null )
			panelDiv.style.display="none";
	}
	
	function resizeIFrame( iFrame )
	{
		
		  var iframeWin = window.frames[iFrame.id];
		  var iframeEl = iFrame; //document.getElementById? document.getElementById(iframeName): document.all? document.all[iframeName]: null;
		  if ( iframeEl && iframeWin ) {
			  iframeEl.style.height = "auto"; // helps resize (for some) if new doc shorter than previous  

		  var docHt = 0, sh, oh;

  		if (iframeEl.document.height) 
			docHt = iframeEl.document.height;

	  	else if (iframeEl.document.body ) 
		{
	    	if (iframeEl.document.body.scrollHeight) 
				docHt = sh = iframeEl.document.body.scrollHeight;
	    	if (iframeEl.document.body.offsetHeight) 
				docHt = oh = iframeEl.document.body.offsetHeight;
	    	if (sh && oh) 
				docHt = Math.max(sh, oh);
	//		alert( docHt + ':' + sh + ':' + oh );
	  	}
	
	    // need to add to height to be sure it will all show
	    if (docHt) 
		{
			iframeEl.style.height = docHt + 50 + "px";
		}	
	  }
		
	}
	