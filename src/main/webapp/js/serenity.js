/**
 * Loads the data in the serenity result. The URL is called that will get the
 * Stapler doDynamic invocation. The model for the class and id will be
 * generated. The frames are then forced to reload which will transfer the model
 * data to the chart and source pages.
 * 
 * @param class
 *            the class of the composite, i.e. Project, Package or Class
 * @param id
 *            the id of the composite
 * @return nothing
 */
function loadFrames(klass, id) {
	// alert('Load frames : ' + class + ', ' + id);
	var xmlhttp = null;
	// Object of the current windows
	if (window.XMLHttpRequest) {
		// Firefox, Safari, ...
		xmlhttp = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP"); // Internet Explorer
	}
	xmlhttp.onreadystatechange = function() {
		// instructions to process the response
		if (xmlhttp.readyState == 4) {
			// Received, OK
			if (xmlhttp.status == 200) {
				var d = window.document;
				// var d = window.parent.document;
				var f = d.frames ? d.frames['chart'] : d.getElementById('chart');
				var p = f.document || f.contentWindow.document;
				// alert('P : 0 : ' + p);
				p.location.reload(true);
				// window.parent.document.getElementById('chart').contentWindow.location
				// .reload(true);
				// alert('Model : ' + xmlhttp.responseText);
				if (klass == 'com.ikokoon.serenity.model.Class') {
					f = d.frames ? d.frames['source'] : d.getElementById('source');
					p = f.document || f.contentWindow.document;
					p.location.reload(true);
					// alert('P : 1 : ' + p);
					// window.parent.document.getElementById('source').contentWindow.location
					// .reload(true);
				}
			}
		}
	}
	var url = 'SerenityResult/target?class=' + klass + '&id=' + id;
	xmlhttp.open('POST', url); // , true
	xmlhttp.send(); // null
}

/**
 * This function resizes the frames on the page to fit the displayable area 
 * on the browser.
 * 
 * @return nothing
 */
function resizeFrames() {
	var usableHeight = pageHeight() * 85 / 100;
	var chart = document.frames ? document.frames['chart'] : document.getElementById('chart');
	chart.height = 180;
	var source = document.frames ? document.frames['source'] : document.getElementById('source');
	source.height = usableHeight - chart.height;
	var treeDiv = document.getElementById('treeDiv');
	treeDiv.style.width = 330;
	treeDiv.style.height = usableHeight;
	// alert('Width : ' + pageWidth() + ', height : ' + pageHeight() + ', chart : ' + chart.height + ', source : ' + source.height + ', tree div : ' + treeDiv.style.height);
}

var tree;
/**
 * This function takes the 'xmp' tags and loads the JavaScript navigation tree.
 * @return nothing
 */
function loadTree() {
	tree = dhtmlXTreeFromHTML('treeDiv');
	// alert('Load tree : ' + tree);
	tree.setOnClickHandler(onClick);
	// alert('Load tree : 0 : ' + tree);
}

/**
 * This function is the event handler for the tree.
 * 
 * @param id the id of the tree item
 * @return nothing
 */
function onClick(id) {
	// alert('On click : ' + id + ', ' + tree);
	var klassName = tree.getUserData(id, 'klass');
	loadFrames(klassName, id);
}

// Browser Window Size and Position
// copyright Stephen Chapman, 3rd Jan 2005, 8th Dec 2005
// you may copy these functions but please keep the copyright notice as well
function pageWidth() {
	return window.innerWidth != null ? window.innerWidth
			: document.documentElement && document.documentElement.clientWidth ? document.documentElement.clientWidth
					: document.body != null ? document.body.clientWidth : null;
}

function pageHeight() {
	return window.innerHeight != null ? window.innerHeight
			: document.documentElement && document.documentElement.clientHeight ? document.documentElement.clientHeight
					: document.body != null ? document.body.clientHeight : null;
}

function posLeft() {
	return typeof window.pageXOffset != 'undefined' ? window.pageXOffset
			: document.documentElement && document.documentElement.scrollLeft ? document.documentElement.scrollLeft
					: document.body.scrollLeft ? document.body.scrollLeft : 0;
}

function posTop() {
	return typeof window.pageYOffset != 'undefined' ? window.pageYOffset
			: document.documentElement && document.documentElement.scrollTop ? document.documentElement.scrollTop
					: document.body.scrollTop ? document.body.scrollTop : 0;
}

function posRight() {
	return posLeft() + pageWidth();
}

function posBottom() {
	return posTop() + pageHeight();
}