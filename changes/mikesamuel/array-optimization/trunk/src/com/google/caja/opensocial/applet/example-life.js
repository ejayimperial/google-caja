<!-- Cellular automaton gadget. This code is in the public domain. -->
<script>var handle = null;</script>
<textarea id="area">It's alive!</textarea>
<input type="button" value="go"
 onclick="if (handle === null) { handle = setInterval(update, 1000); }">
<input type="button" value="stop"
 onclick="if (handle !== null) { clearInterval(handle); handle = null; }">
<script>
// Size of the grid
var size = 40;
// Text area for displaying the CA
var ta = document.getElementById('area');
ta.setAttribute('rows', size);
ta.setAttribute('cols', size*2);

// Set up the internal version of the grid
// ca[t][x][y]
// t alternates between 0 and 1
var ca = [];
var t, x, y;
for (t = 0; t < 2; ++t) {
  ca[t] = [];
  for (x = 0; x < size; ++x) {
    ca[t][x] = new Array(size);
  }
}

// Start with a pi heptomino
var s = Math.floor(size / 2);
ca[0][s][s] = 1;
ca[0][s+1][s] = 1;
ca[0][s+2][s] = 1;
ca[0][s][s+1] = 1;
ca[0][s+2][s+1] = 1;
ca[0][s][s+2] = 1;
ca[0][s+2][s+2] = 1;


// Compute number of neighbors (on a torus)
function count(ca, t, x, y) {
  var sum = 0, i, j;
  for (i = -1; i < 2; ++i) {
    for (j = -1; j < 2; ++j) {
      if (!i && !j) continue;

      var k = x + i;
      if (k < 0) { k += size; }
      else if (k >= size) { k -= size; }

      var m = y + j;
      if (m < 0) { m += size; }
      else if (m >= size) { m -= size; }

      sum += !!(ca[t][k][m]);
    }
  }
  return sum;
}

// Display loop
function display(ca, t) {
  var x, y, str = "";
  for (y = 0; y < size; ++y) {
    for (x = 0; x < size; ++x) {
      str += ca[t][x][y] ? "()" : "  ";
    }
    str += "\n";
  }
  ta.innerHTML = str;
}

// Update loop
function update() {
  for (x = 0; x < size; ++x) {
    for (y = 0; y < size; ++y) {
      var c = count(ca, t, x, y);
      // Conway's rules
      if (c === 2) { ca[1-t][x][y] = ca[t][x][y]; }
      else if (c === 3) { ca[1-t][x][y] = 1; }
      else { ca[1-t][x][y] = 0; }
    }
  }
  t = 1 - t;
  display(ca, t);
}
t=0;
</script>
