function getDate(n, min, max) {
    var yesterday = new Date();
    var diff = max + min - n;
    yesterday.setDate(yesterday.getDate() - diff);
    return yesterday;
}

var max = parseInt(document.getElementById("slider").max);
var min = parseInt(document.getElementById("slider").min);
var slideSq = document.getElementById("slider");
var y = document.getElementById("date");

y.innerHTML = getDate(slideSq.value, min, max).toISOString().slice(0,10);

slideSq.oninput = function() {
    y.innerHTML = getDate(slideSq.value, min, max).toISOString().slice(0,10);
}