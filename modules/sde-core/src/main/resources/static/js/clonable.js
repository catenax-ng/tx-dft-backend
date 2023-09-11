let cloneElement = function(partId, target) {

	let number = 0;
	let htmlObject = null;

	// count existing sections
	$('[id^=' + partId + ']').each(function() {
		let localid = this.id;
		number = localid.substr(localid.lastIndexOf('_') + 1)
	});

	if (!isNaN(number)) {
		number = parseInt(number);
		htmlObject = $("#" + partId + "_" + number).clone();
		number++;
	}
	else {
		alert("Clone not possible because of non numeric idis ending")
	}

	let newId = partId + "_" + number;
	htmlObject = htmlObject.attr("id", newId);
	htmlObject.children().find('input,textarea,button,a,select,img,div,span').attr('id', function(i, val) {
		if (typeof val != 'undefined') {
			let oldid = val.substr(0, val.lastIndexOf('_'));
			let newid = oldid + "_" + number;
			htmlObject.find("#" + val).val("");
			htmlObject.find("#" + val).attr("id", newid);
		}
	});

	$("#" + target).append(htmlObject);
	return newId;
};

let removeElement = function(elementId, partId) {

	let indexId = elementId.replace(partId+"RemoveBtn_",'');
	
	let idOfRemoveElement=partId + "Div_";
	
	if ($('[id^=' + idOfRemoveElement + ']').length > 1)
		$("#" + idOfRemoveElement + indexId).remove();
	else {
		alert("Atelast one element should be there");
	}
}