/* 
 * This JavaScript file provides methods for clearing the
 * canvas and for rendering Red Box Man.
 */

var canvas;
var gc;
var canvasWidth;
var canvasHeight;
var imageLoaded;
var mousePositionRendering;
var redBoxManImage;
var mouseX;
var mouseY;
var imagesRedBoxManLocations;
var shapesRedBoxManLocations;

function Location(initX, initY) {
    this.x = initX;
    this.y = initY;
}

function init() {
    // GET THE CANVAS SO WE CAN USE IT WHEN WE LIKE
    canvas = document.getElementById("red_box_man_canvas");
    gc = canvas.getContext("2d");

    // MAKE SURE THE CANVAS DIMENSIONS ARE 1:1 FOR RENDERING
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    canvasWidth = canvas.width;
    canvasHeight = canvas.height;

    // FOR RENDERING TEXT
    gc.font = "32pt Arial";

    // LOAD THE RED BOX MAN IMAGE SO WE CAN RENDER
    // IT TO THE CANVAS WHENEVER WE LIKE
    redBoxManImage = new Image();
    redBoxManImage.onload = function () {
        imageLoaded = true;
    }
    redBoxManImage.src = "./images/RedBoxMan.png";

    // BY DEFAULT WE'LL START WITH MOUSE POSITION RENDERING ON
    mousePositionRendering = true;

    // HERE'S WHERE WE'LL PUT OUR RENDERING COORDINATES
    imagesRedBoxManLocations = new Array();
    shapesRedBoxManLocations = new Array();
}

function processMouseClick(event) {
    updateMousePosition(event);
    var location = new Location(mouseX, mouseY);
    if (event.shiftKey) {
        shapesRedBoxManLocations.push(location);
        render();
    } else if (event.ctrlKey) {
        if (imageLoaded) {
            imagesRedBoxManLocations.push(location);
            render();
        }
    } else {
        clear();
    }
}

function clearCanvas() {
    gc.clearRect(0, 0, canvasWidth, canvasHeight);
}

function clear() {
    shapesRedBoxManLocations = [];
    imagesRedBoxManLocations = [];
    clearCanvas();
}

function updateMousePosition(event) {
    var rect = canvas.getBoundingClientRect();
    mouseX = event.clientX - rect.left;
    mouseY = event.clientY - rect.top;
    render();
}

function renderShapesRedBoxMan(location) {
    var headColor = "#DD0000";
    var outlineColor = "#000000";
    var headW = 115;
    var headH = 88;

    // DRAW HIS RED HEAD
    gc.fillStyle = headColor;
    gc.fillRect(location.x, location.y, headW, headH);
    gc.beginPath();
    gc.strokeStyle = outlineColor;
    gc.lineWidth = 1;
    gc.rect(location.x, location.y, headW, headH);
    gc.stroke();

    // AND THEN DRAW THE REST OF HIM
    var headColor =     "#DD0000";
    var outlineColor =  "#000000";
    var eyeColor =      "#FFFF00";
    var pupilColor =    "#000000";
    var mouthColor =    "#000000";
    var bodyColor =     "#000000";
    var headW = 115;
    var headH = 88;
    var eyeW = 35;
    var eyeH = 25;
    var eye1offset = [10, 10];
    var eye2offset = [70, 10];
    var pupilW = 10;
    var pupilH = 10;
    var pupiloffset = [10, 10];
    var mouthW = 100;
    var mouthH = 10;
    var mouthoffset = [30, 50];
    var chestW = 70;
    var chestH = 40;
    var chestoffset = [30, 85];
    var bodyW = 60;
    var bodyH = 20;
    var bodyoffset = [35, 125];
    var footW = 10;
    var footH = 20;
    var foot1offset = [30, 145];
    var foot2offset = [90, 145];

    // AND THEN DRAW THE REST OF HIM
    //eye 1
    gc.fillStyle = eyeColor;
    gc.fillRect(location.x + eye1offset[0], location.y + eye1offset[1], eyeW, eyeH);
    gc.beginPath();
    gc.rect(location.x + eye1offset[0], location.y + eye1offset[1], eyeW, eyeH);
    gc.stroke();
    //eye2
    gc.fillRect(location.x + eye2offset[0], location.y + eye2offset[1], eyeW, eyeH);
    gc.beginPath();
    gc.rect(location.x + eye2offset[0], location.y + eye2offset[1], eyeW, eyeH);
    gc.stroke();
    //pupil1
    gc.fillStyle = pupilColor;
    gc.fillRect(location.x + eye1offset[0] + pupiloffset[0],
            location.y + eye1offset[1] + pupiloffset[1], pupilW, pupilH);
    gc.beginPath();
    //pupil2
    gc.fillRect(location.x + eye2offset[0] + pupiloffset[0],
            location.y + eye2offset[1] + pupiloffset[1], pupilW, pupilH);
    gc.beginPath();
    //mouth
    gc.fillRect(location.x + mouthoffset[0], location.y + mouthoffset[1], mouthW, mouthH);
    gc.beginPath();
    //chest
    gc.fillRect(location.x + chestoffset[0], location.y + chestoffset[1], chestW, chestH);
    gc.beginPath();
    //body
    gc.fillRect(location.x + bodyoffset[0], location.y + bodyoffset[1], bodyW, bodyH);
    gc.beginPath();
    //foot1
    gc.fillRect(location.x + foot1offset[0], location.y + foot1offset[1], footW, footH);
    gc.beginPath();
    //foot2
    gc.fillRect(location.x + foot2offset[0], location.y + foot2offset[1], footW, footH);
    gc.beginPath();
}

function renderImageRedBoxMan(location) {
    gc.drawImage(redBoxManImage, location.x, location.y);
}

function renderMousePositionInCanvas(event) {
    if (mousePositionRendering) {
        gc.strokeText("(" + mouseX + "," + mouseY + ")", 10, 50);
    }
}

function toggleMousePositionRendering() {
    mousePositionRendering = !mousePositionRendering;
}

function render() {
    clearCanvas();
    for (var i = 0; i < shapesRedBoxManLocations.length; i++) {
        var location = shapesRedBoxManLocations[i];
        renderShapesRedBoxMan(location);
    }
    for (var j = 0; j < imagesRedBoxManLocations.length; j++) {
        var location = imagesRedBoxManLocations[j];
        renderImageRedBoxMan(location);
    }
    renderMousePositionInCanvas();
}