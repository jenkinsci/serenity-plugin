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
 * @param the event so we can stop the checktree function opening the class method           
 * @return nothing
 */
function loadFrames(klass, id, evt) {
	var xmlhttp = null;
	if (window.XMLHttpRequest) {
		// Firefox, Safari, ...
		xmlhttp = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		// Internet Explorer
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				var d = window.document;
				var f = d.frames ? d.frames['chart'] : d.getElementById('chart');
				var p = f.document || f.contentWindow.document;
				p.location.reload(true);
				if (klass == 'com.ikokoon.serenity.model.Class') {
					loadSource();
				}
			}
		}
	}
	var url = 'SerenityResult/target?class=' + klass + '&id=' + id;
	xmlhttp.open('POST', url); // , true
	xmlhttp.send(); // null
	
	evt = evt || window.event;
    if (typeof evt.stopPropagation != "undefined") {
        evt.stopPropagation();
    } else {
        evt.cancelBubble = true;
    }
}

function loadSource() {
	var xmlhttp = null;
	// Object of the current windows
	if (window.XMLHttpRequest) {
		// Firefox, Safari, ...
		xmlhttp = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		 // Internet Explorer
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4) {
			if (xmlhttp.status == 200) {
				var d = window.document;
				var s = d.getElementById('source');
				s = (s.contentWindow) ? s.contentWindow : (s.contentDocument.document) ? s.contentDocument.document : s.contentDocument;
				s.document.open();
				s.document.write(xmlhttp.responseText);
				s.document.close();
			}
		}
	}
	var url = 'SerenityResult/source';
	xmlhttp.open('GET', url); // , true
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
	source.height = usableHeight - chart.height - 65;
	// alert('Width : ' + pageWidth() + ', height : ' + pageHeight() + ', chart : ' + chart.height + ', source : ' + source.height);
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