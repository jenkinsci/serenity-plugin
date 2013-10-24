/*

CHECKTREE v1.0 RC (c) 2004-2006 Angus Turnbull, http://www.twinhelix.com
Altering this notice or redistributing this file is prohibited.

 */

CheckTree.list = {};
function CheckTree(myName) {
	this.myName = myName;
	this.root = null;
	this.countAllLevels = false;
	this.checkFormat = '(%n% checked)';
	this.evtProcessed = navigator.userAgent.indexOf('Safari') > -1 ? 'safRtnVal' : 'returnValue';
	CheckTree.list[myName] = this
};
CheckTree.prototype.init = function() {
	with (this) {
		if (!document.getElementById)
			return;
		root = document.getElementById('tree-' + myName);
		if (root) {
			var lists = root.getElementsByTagName('ul');
			for ( var ul = 0; ul < lists.length; ul++) {
				lists[ul].style.display = 'none';
				lists[ul].treeObj = this;
			}
			root.treeObj = this;
			if (root.addEventListener && navigator.vendor != 'Apple Computer,Inc.') {
				root.addEventListener('click', new Function('e', myName + '.click(e)'), false)
			} else
				root.onclick = new Function('e', myName + '.click(e)');
			var nodes = root.getElementsByTagName('li');
			if (nodes != null && nodes != undefined) {
				for ( var li = 0; li < nodes.length; li++) {
					if (nodes[li].id.match(/^show-/)) {
						nodes[li].className = (nodes[li].className == 'last' ? 'plus-last' : 'plus')
					}
				}
			}
		}
	}
};
CheckTree.prototype.click = function(e) {
	with (this) {
		e = e || window.event;
		var elm = e.srcElement || e.target;
		while (elm) {
			if (elm.tagName.match(/^(input|ul)/i))
				break;
			if (elm.id && elm.id.match(/^show-(.*)/)) {
				var targ = document.getElementById('tree-' + RegExp.$1);
				if (targ.style) {
					var col = (targ.style.display == 'none');
					targ.style.display = col ? 'block' : 'none';
					elm.className = elm.className.replace(col ? 'plus' : 'minus', col ? 'minus' : 'plus')
				}
				break
			}
			elm = elm.parentNode
		}
	}
};