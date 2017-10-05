RSuite.Action({
	id: 'oxygen:editOxygen',
	icon: self.location.protocol + "//" + self.location.host + '/' + 'rsuite/rest/v1/static/@pluginId@/images/icon_oxauthor.png',
	invoke: function (context) {
		debugger;
		var moId = Ember.get(context, 'managedObject.finalManagedObject.id');
		var open = function () {
			window.open(RSuite.restUrl(1, 'api/@pluginId@?id=' + encodeURIComponent(moId)));
			RSuite.get("progressMeter").dialog("close");
		};
		RSuite.view.Menu.removeAll();
		RSuite.get("progressMeter").dialog("open");
		if (!Ember.get(context, 'managedObject.finalManagedObject.checkout')) {
			return RSuite.Action('rsuite:checkOut', context).then(open);
		} else {
			open();
		}
		
	}
});
