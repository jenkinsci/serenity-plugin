/**
 * Loads the data in the serenity result. The url is called that will get the
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
				var d = window.parent.document;
				var f = d.frames ? d.frames['chart'] : d.getElementById('chart');
				var p = f.document || f.contentWindow.document;
				p.location.reload(true);
				// window.parent.document.getElementById('chart').contentWindow.location
				//		.reload(true);
				if (klass == 'com.ikokoon.serenity.model.Class') {
					f = d.frames ? d.frames['source'] : d.getElementById('source');
					p = f.document || f.contentWindow.document;
					p.location.reload(true);
					// window.parent.document.getElementById('source').contentWindow.location
					//		.reload(true);
				}
			}
		}
	}
	var url = 'SerenityResult/chart?class=' + klass + '&id=' + id;
	xmlhttp.open('GET', url, true);
	xmlhttp.send(null);
}