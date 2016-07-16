
EMP.util.closeOnBlur=
{ 
	sNode:'',sTrigger:'',fAction:'',
	update:function(sNode,sTrigger,fAction)
	{
		this.sNode=sNode;
		this.sTrigger=sTrigger;
		this.fAction=fAction;},
	
		clear:function()
		{this.sNode='';
		this.sTrigger='';
		this.fAction='';},
	
		close:function(dNode)
		{
			if(dNode.nodeType==3)
				targ=targ.parentNode;
			var n=dNode;
			if(n.id==this.sTrigger)
			return;
			do
			{
				if(n.nodeName=="DIV")
				{
					if(n.id&&n.id==this.sNode)
					{
						return;
					}
				}
				n=n.parentNode;
			}while(n.nodeName!="HTML")
			
			this.fAction();
			this.clear();
		},
		
		fire:function(e,oSelf)
		{
			if(oSelf.fAction!='')
			{
				var dNode=(e&&e.target)||(window.event&&window.event.srcElement);
				if(dNode.tagName!='HTML')
				{
					oSelf.close(dNode);
				}
			}
		}
		
	};

	YAHOO.util.Event.addListener(document,"mousedown", EMP.util.closeOnBlur.fire, EMP.util.closeOnBlur);


EMP.util.oPageSettings  = function( divId, triggerId, curTheme ){

	this.dOverlay=document.getElementById(divId);
	this.sCurrentTheme = curTheme;
	this.triggerId = triggerId;
};


EMP.util.oPageSettings.prototype = {

	dOverlay:null,
	triggerId:null,
	sCurrentTheme:null,
		
	toggle:function()
	{
		var oSelf=this;
		EMP.util.closeOnBlur.update(this.dOverlay.id, this.triggerId, function(){oSelf.showHide();});
		this.showHide();
	},
	
	showHide:function()
	{
		this.dOverlay.style.display=this.dOverlay.style.display!='block'?'block':'none';
		
		document.getElementById(this.triggerId).className=this.dOverlay.style.display!='block'?'':'on';
		if(this.dOverlay.style.display!='block' )
		{
			EMP.util.closeOnBlur.clear();
		}
		
	},

	applyTheme:function(e,oSelf)
	{
		var src=document.getElementById(e)?document.getElementById(e):(e&&e.target)||window.event.srcElement;
		var sTheme=src.id;
	
		if(document.getElementById(oSelf.sCurrentTheme))
		{
			YAHOO.util.Dom.removeClass(document.getElementById(oSelf.sCurrentTheme),"on");
		}
	
		YAHOO.util.Dom.addClass(src,"on");
		oSelf.sCurrentTheme=sTheme;
		
		var sUrl = 'ccb-';
		sUrl+= sTheme;
		sUrl+='.css';
		
		var d = document;
		var dNode = d.createElement( 'link' );
		dNode.setAttribute('type', 'text/css');
		dNode.setAttribute('rel', 'stylesheet');
		dNode.setAttribute('href', sUrl);
	//	dNode.setAttribute('id', 'css');
		
		var dHead = d.getElementsByTagName('head')[0];
		dHead.appendChild(dNode);
		
	
	},
	
	setTheme:function(theme)
	{
		this.sCurrentTheme=theme;
		
		var sUrl = 'ccb-';
		sUrl+= theme;
		sUrl+='.css';

	
		YAHOO.util.Dom.addClass(document.getElementById(theme),"on");
		
		var d = document;
		var dNode = d.createElement( 'link' );
		dNode.setAttribute('type', 'text/css');
		dNode.setAttribute('rel', 'stylesheet');
		dNode.setAttribute('href', sUrl);
		
		var dHead = d.getElementsByTagName('head')[0];
		dHead.appendChild(dNode);
	}
	
		
};

