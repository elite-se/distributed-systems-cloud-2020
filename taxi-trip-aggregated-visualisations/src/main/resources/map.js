const START_CELL_CENTRE = [40.831164, -74.192491],
    CELL_SIZE_METRES = 500,
    EARTH_RADIUS_METRES = 6371000.0,
    CELL_LAT_LENGTH = (START_CELL_CENTRE[0] + (CELL_SIZE_METRES
        / EARTH_RADIUS_METRES) * (180 / Math.PI)) - START_CELL_CENTRE[0],
    CELL_LONG_LENGTH = (START_CELL_CENTRE[1] + (CELL_SIZE_METRES
        / EARTH_RADIUS_METRES) * (180 / Math.PI) / Math.cos(
        START_CELL_CENTRE[0] * Math.PI / 180)) - START_CELL_CENTRE[1],
    START_CELL_ORIGIN = [START_CELL_CENTRE[0] + (CELL_LAT_LENGTH / 2),
      START_CELL_CENTRE[1] - (CELL_LONG_LENGTH / 2)], // Coordinates of top-left corner of cell 1.1
    CELL_LENGTH = [CELL_LAT_LENGTH, CELL_LONG_LENGTH],
    MAX_CLAT = 45,
    MAX_CLONG = 72;

let HIGH_PROFIT_METRIC = 1500;

let map,
    layer,
    grid;
$(document).ready(function () {
  map = L.map('map'),
      layer = L.featureGroup(),
      grid = createArray(MAX_CLAT, MAX_CLONG);
  L.tileLayer(
      'https://api.mapbox.com/styles/v1/adam-cattermole/cjtzo15i32cvc1fo9dzcv3m6x/tiles/256/{z}/{x}/{y}@2x?access_token={accessToken}',
      {
        attribution: '&copy; <a href="https://www.mapbox.com/">Mapbox</a> &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a>',
        id: 'mapbox.minimo',
        accessToken: 'pk.eyJ1IjoiYWRhbS1jYXR0ZXJtb2xlIiwiYSI6ImNqdHk4MzNnazBmYXI0NHBuM3h6c3I5cmUifQ.X0CQHfyaf5_Q0odBHVo46A'
      }).addTo(map);
  map.setView(START_CELL_CENTRE, 10);

  let start = START_CELL_ORIGIN;
  for (let i = 0; i < MAX_CLONG; i++) {
    for (let j = 0; j < MAX_CLAT; j++) {
      let new_start = [start[0] - CELL_LENGTH[0], start[1]];
      grid[j][i] = L.rectangle([
        start,
        [start[0] - CELL_LENGTH[0], start[1] + CELL_LENGTH[1]]
      ], {
        weight: 0.4,
        opacity: 1,
        fillOpacity: 0
      });
      layer.addLayer(grid[j][i]);
      start = new_start;
    }
    start = [START_CELL_ORIGIN[0], start[1] + CELL_LENGTH[1]];
  }
  map.addLayer(layer).fitBounds(layer.getBounds());

  //set HIGH-Cell Metric to max of the cellData
  for (let i = 0; i < document.cellData.length; i++) {
    const profit = document.cellData[i];
    if (profit.fareSum > HIGH_PROFIT_METRIC) {
      HIGH_PROFIT_METRIC = profit.fareSum;
    }
  }

  for (let i = 0; i < document.cellData.length; i++) {
    //document.cellData is created within the kotlin dsl part
    let profit = document.cellData[i];
    let val = ((profit.fareSum > HIGH_PROFIT_METRIC) ? HIGH_PROFIT_METRIC
        : profit.fareSum);

    grid[profit.cell.clat][profit.cell.clong].setStyle(
        {fillOpacity: val / HIGH_PROFIT_METRIC});
  }
});

$('#seek').click(function () {
  $('#messages').empty();

  let offset = $('#offset').val(),
      headers = {
        "offset": offset
      };

  eb.publish("config", "seek", headers);
});

$('#clean').click(function () {
  $('#messages').empty();
  for (let i = 0; i < MAX_CLONG; i++) {
    for (let j = 0; j < MAX_CLAT; j++) {
      grid[j][i].setStyle({fillOpacity: 0});
    }
  }
});

function createArray(length) {
  let arr = new Array(length || 0),
      i = length;

  if (arguments.length > 1) {
    let args = Array.prototype.slice.call(arguments, 1);
    while (i--) {
      arr[length - 1 - i] = createArray.apply(this, args);
    }
  }

  return arr;
}
