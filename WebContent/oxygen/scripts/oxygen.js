(function (global) {
    "use strict";
 
    RSuite.Action({
    	downloadJNLP: function(text, fileName, type) {
    		var a = document.createElement('a');
    		var file = new Blob([text], {type: type});
		  	if(navigator.userAgent.toLowerCase().indexOf('firefox') > -1 || navigator.userAgent.toLowerCase().indexOf('chrome') > -1){
		  		a.setAttribute('href', URL.createObjectURL(file));
			  	a.setAttribute('download', fileName);
			  	if (document.createEvent) {
		  			var event = document.createEvent('MouseEvents');
		  		    event.initEvent('click', true, true);
		  		    a.dispatchEvent(event);
		  		}
		  		else {
		  			a.click();
		  		}
	    	}else{
	    		window.navigator.msSaveBlob(file, fileName);
	    	}
    		
    		
	  	},
	  	
    	generateJNLP: function (moId) {
    		var protocol = self.location.protocol;
    		var hostname = self.location.hostname;
    		var port = self.location.port;
    		var serviceVersion = 1;
    		var restUrl = RSuite.restUrl(serviceVersion);
    		var hostURL = protocol + "//" +  hostname + ":" + port;
    		var appletJarURL = hostURL+ '/rsuite/rest/v1/static/oxygen/SignedOxygenLauncher.jar';
    		var appletClassURL = "OxygenLauncherApplet";
    		return ['<?xml version="1.0" encoding="utf-8"?>'
    				,'<jnlp spec="6.0+" codebase="' + hostURL + '/rsuite/rest/v1/api/@pluginId@/">'
    				,'	<information>'
    				,'		<title>Oxygen Desktop launcher</title>'
    				,'		<vendor>RSI Content Solutions</vendor>'
    				,'		<description>Oxygen Desktop launcher for ' + moId + '</description>'
    				,'	</information>'
    				,'	<security><all-permissions/></security>'
    				,'	<resources>'
    				,'		<jar href="' + appletJarURL + '"/>'
    				,'	</resources>'
    				,'	<application-desc main-class="' + appletClassURL + '">'
    				,'  	<argument>'+hostURL+'</argument>'
    				,'		<argument>'+RSuite.model.session.get('user.name')+'</argument>'
    				,'		<argument>'+RSuite.model.session.get('key')+'</argument>'
    				,'		<argument>'+moId+'</argument>'
    				,' </application-desc>'
    				,'</jnlp>'].join('\n');
    			
    	},
        id: 'oxygen:editOxygen',
        icon: self.location.protocol + "//" + self.location.host + '/' + 'rsuite/rest/v1/static/oxygen/images/icon_oxauthor.gif',
        invoke: function (context) {
        	var moId = Ember.get(context, 'managedObject.finalManagedObject.id');
        	var open = function () {
    			var contentJNLP =  this.generateJNLP(moId);
    			this.downloadJNLP(contentJNLP, '@pluginId@-' + moId + '.jnlp', 'application/x-java-jnlp-file');
    		}.bind(this);
        	RSuite.view.Menu.removeAll();
        	RSuite.get("progressMeter").dialog("open");
        	if (!Ember.get(context, 'managedObject.finalManagedObject.checkout')) {
    			return RSuite.Action('rsuite:checkOut', context).then(open(), RSuite.get("progressMeter").dialog("close"));
    		} else {
    			RSuite.get("progressMeter").dialog("close");
    			open();
    		}
        	
        }
	});
}(this));
