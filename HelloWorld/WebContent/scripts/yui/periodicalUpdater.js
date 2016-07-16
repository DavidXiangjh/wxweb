
Function.prototype.bind = function() {
  var __method = this, args = $A(arguments), object = args.shift();
  return function() {
    return __method.apply(object, args.concat($A(arguments)));
  }
}

var $A = Array.from = function(iterable) {
  if (!iterable) return [];
  if (iterable.toArray) {
    return iterable.toArray();
  } else {
    var results = [];
    for (var i = 0; i < iterable.length; i++)
      results.push(iterable[i]);
    return results;
  }
}



/*
	class used to periodically update div's content
	dataSrc: the url used to update the div
	the dataObj is as: 
		frequency:1, parameters: 'c=status', onFailure: reportError
		frequency: frenquency of time in second
		parameters: parameter wanted to append to the GET url
	
*/

EMP.util.PeriodicalUpdater  = function(divId, dataSrc, dataObj){
		this.init(divId, dataSrc, dataObj);
}

EMP.util.PeriodicalUpdater.prototype = {

	/*
		the url used for periodical updater src request url
	*/
	dataSrc:null,
	comboxs:null,
	dataObj:null,
	
	isStop:false,
	frequency:0,
	
	timer:null,
		
//	frequency:1, parameters: 'c=status', onFailure: reportError
	
	init:function(divId, dataSrc, dataObj){
		this.divId = divId;
		this.dataSrc = dataSrc;
		this.dataObj = dataObj;
		this.frequency = dataObj.frequency;
		this.startUp();
	},

	
	startUp:function(  )
	{
		this.timer = setTimeout(this.onTimerEvent.bind(this), this.this.frequency * 1000);
	//	this.doRequest();
	},
	
	onTimerEvent:function()
	{
		this.doRequest();
	},
	

	doRequest:function()
	{
		var obj = YAHOO.util.Connect.createXhrObject( -1 );
		//first get the url to set the locale value
		var httpReq = obj.conn;
		var url = this.dataSrc;
		if( this.dataObj != null && this.dataObj.parameters != null )
		{
			var idx = url.indexOf('?');
			if( idx != -1 )
				url = url + '&' + this.dataObj.parameters;
			else
				url = url + '?' + this.dataObj.parameters;
			
		}
		
		httpReq.open("GET", url, false);
		httpReq.send(null);
		
		if( httpReq.readyState == 4 )
		{
			var divElement = document.getElementById( this.divId );
			if( divElement != null )
			{
				divElement.innerHTML = httpReq.responseText;
				if( !this.isStop )
					this.startUp();
			}
		}
		else
		{
			if( this.data.onFailure != null )
				this.data.onFailure( httpReq.status );
		}
	},

	stop:function()
	{
		this.isStop = true;
 		clearTimeout(this.timer);
 	},
 	
 	setFrequency:function( value )
 	{
 	 	this.frequency = value;
 	 }
}
