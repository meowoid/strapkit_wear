
//your code here

var SK = require('strapkit');
var SM = SK.metrics;
var UI = SK.ui;
var times = 0;
SM.init({
app_id:"tSGpQgSgjYZisap2o"
});

SM.log('/main/show', {data: "good"});

var cityName = 'London';
var URL = 'http://api.openweathermap.org/data/2.5/weather?q=' + cityName;

ajax(
  {
    url: URL,
    type: 'json'
  },
  function(data) {
    // Success!
    console.log('Successfully fetched weather data!');
  },
  function(error) {
    // Failure!
    console.log('Failed fetching weather data: ' + error);
  }
);

$.getJSON('http://opticontact.herokuapp.com/lenses', function(data) {
console.log(data)
var config = {sections:[{items:[]}]};

for(var i=0; i<data.length; i++) {
    config.sections[0].items.push({title: data[i].name});
}


var menu =  UI.menu(config);
      menu.show();
      menu.on('select', function(idx) {

        if(idx == 0) {
            var textvw = UI.view({
                title: "You chose Strapkit! Hell Yeah"
            });
            textvw.show();
        }

        if(idx == 1) {

            var textvw = UI.view({
                title: "Dude, why no Strapkit?"
            });

            var times = 0;

            textvw.on('touch', function() {
                times++;
               if(times == 10 && strapkit.wear) {
                 strapkit.wear.confirmActivity("You clicked 10 times!");
               }
               textvw.title = "Dude, you've clicked " + times + "times!";
               textvw.show();
            });
            textvw.show();

        }
      });
})
