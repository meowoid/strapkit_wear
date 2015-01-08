

var stringify = function(obj) {
	var cache = [];
	  var json = JSON.stringify(obj, function(key, value) {
	    if (typeof value === 'function') {
	      return value.toString();
	    } else if (typeof value === 'object' && value !== null) {
	    	if (cache.indexOf(value) !== -1) {
	            // Circular reference found, discard key
	            return;
	        } else {
	        	cache.push(value);
	        	return value;
	        }
		} else {
			return value;
		}
	  });
	  cache = null;
	  return json;
};
var AndroidPage = Page.extend({
	id: -1,
	setId: function(idx) {
		if (this.id == null) {
			this.id = idx;
		}
	},
	getId: function() {
		return this.id;
	},
	show: function() {
		this.pageOpen = true;
		var viewString = [];
		for (var i = 0; i < this.getViews().length; i++) {
			var view = this.getViews()[i];
			viewString.push(view.getJSON());
		}
		var json = {
			views: viewString,
			pageOpen: this.pageOpen
		};	
		window.strapkit_bridge.showPage(stringify(json));
	},
	hide: function() {
		console.log('hide');
		this.pageOpen = false;
		window.strapkit_bridge.hidePage(this);
	}
});

var httpClient = function(opts, success, error) {
	if (success != null) {
		success = success.toString();
	}
	if (error != null) {
		error = error.toString();
	}
	window.strapkit_bridge.httpClient(opts, success, error);
};


var StrapKit = {
	UI: {
	    ListView : function(config){ return ListView(config); },
	    Card : function(config){ return new Card(config); },
	    TextView : function(config) { return new TextView(config); },
	    Page : function(config) { return new AndroidPage(config); }
	}
}
