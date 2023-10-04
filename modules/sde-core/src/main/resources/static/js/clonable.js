function addRows(src, levelforrowadd, keeprowininnertabel, rowNumber) {
	src = src.replace("addbutton-", "");
	var countforid = parseInt($("#hidden-" + src).val()) + 1;
	levelforrowadd = parseInt(levelforrowadd);
	try {
		var $tr = $("#tabelname-" + src).find("tbody tr").eq(rowNumber).clone();
		$tr2 = setClonedData(countforid, levelforrowadd, $tr, keeprowininnertabel);
		$("#tabelname-" + src).append($tr2);

	} catch (err) {
		txt = "There was an error on this page.\n\n";
		txt += "Error description: " + err.message + "\n\n";
		txt += "Click OK to continue.\n\n";
		alert(txt);
	}

	$("#hidden-" + src).val(countforid);
	$("#chk-" + src).attr('checked', false);

	resetSerialNoReset(src);
}
function resetSerialNoReset(src) {
	var dd = parseInt($("#hidden-" + src).val());
	var p = 1;
	for (var j = 1; j <= dd; j++) {
		$("#tabelname-" + src).find("tbody tr").eq(j).each(function() {
			$(this).find("[id^='srno-" + src + "']").val(p);
			$(this).find("[id^='srno-" + src + "']").html(p);
		});
		p = parseInt(p) + 1;
	}
}
function deleteRows(src, row) {
	var row = 0;
	if (typeof rowno == "undefined") {
		row = 2;
	}

	src = src.replace("delbutton-", "");
	var totalrowCount = parseInt($("#hidden-" + src).val());
	var rows = $("#tabelname-" + src + ">tbody>tr").length;
	var cnt = 0;
	if (rows <= row) {
		alert("At least one record should be present");
	}
	else {
		for (var i = 1; i <= totalrowCount; i++) {
			if ($("#chk-" + src + "_" + i).length > 0) {
				if ($("#chk-" + src + "_" + i).is(":checked")) {
					$("#tr-" + src + "_" + i).remove();
					cnt++;
				}
				var rowCount = $("#tabelname-" + src + ">tbody>tr").length;
				if (rowCount <= row) {
					break;
				}
			}
		}
	}
	if (cnt == 0 && rows > row) {
		alert("Please select row to delete.");
	}

	resetSerialNoReset(src);
}

function deleteAllRowexcept1st(tbalename) {
	var totalrecord = parseInt($("#hidden-" + tbalename).val());
	for (var j = totalrecord; j > 1; j--) {
		$("#tabelname-" + tbalename).find("tbody tr").eq(j).each(function() {
			$(this).remove();
		});
	}
	$("#hidden-" + tbalename).val(1);
}

function deleteSingleRow(src, level, row) {
	var rows = 0;
	if (typeof row == "undefined")
		row = 2;

	if (typeof level == "undefined")
		level = 1;

	src = src.replace("delbutton-", "");
	var arr = src.split('_');

	//  var totalrowCount =parseInt($("#hidden-"+src).val());
	if (level == '1')
		rows = $("#tabelname-" + arr[0] + ">tbody>tr").length;
	else if (level == '2')
		rows = $("#tabelname-" + arr[0] + "_" + arr[1] + ">tbody>tr").length;
	else if (level == '3')
		rows = $("#tabelname-" + arr[0] + "_" + arr[1] + "_" + arr[2] + ">tbody>tr").length;
	else if (level == '4')
		rows = $("#tabelname-" + arr[0] + "_" + arr[1] + "_" + arr[2] + "_" + arr[3] + ">tbody>tr").length;
	else if (level == '5')
		rows = $("#tabelname-" + arr[0] + "_" + arr[1] + "_" + arr[2] + "_" + arr[3] + "_" + arr[4] + ">tbody>tr").length;
	if (rows <= row) {
		alert("At least one record should be present");
	}
	else
		$("#tr-" + src).remove();

	resetSerialNoReset(arr[0]);
}
function selectAllCheckbox(src) {
	$("input[type:checkbox][id^='" + src + "_']").each(function() {
		var boo = "";
		if ($("#" + src).attr("checked"))
			boo = true;
		else
			boo = false;
		$(this).attr("checked", boo);
	});
}
function setClonedData(counter, indexofcount, $clone, delrow) {

	//alert("id ---"+delrow);
	var j = 0;
	$clone.find("table").each(function() {
		var keeprowintablearray = [delrow];
		if (isNaN(delrow))
			if (delrow.indexOf("_") != -1)
				keeprowintablearray = delrow.split("_");
		var iii = $(this).attr("id");
		var len = $("#" + iii + ">tbody>tr").length;
		var delcount = len;
		if (j < keeprowintablearray.length)
			delcount = keeprowintablearray[j];
		j++;
		for (var i = len; i > delcount; i--) {
			$clone.find("#" + iii + ">tbody>tr").eq(i).remove();
		}
	});

	$clone.find("input:radio").attr("id", function() {
		var fid = generateId(this.id, indexofcount, counter, 'radio');
		$(this).next().attr('for', fid);
		return fid;
	});
	$clone.find("input:radio").attr("name", function() {
		var fname = generateId(this.name, indexofcount, counter, 'radio');
		return fname;
	});


	$clone.find("input,select,img,a,textarea,table,tr,td,img,div,file,span,pre").attr("id", function() {
		var fid = generateId(this.id, indexofcount, counter, '');
		return fid;
	});

	$clone.find("label").attr("for", function() {
		var fid = generateId(this.id, indexofcount, counter, '');
		return fid;
	});


	$clone.find("input,select,img,a,textarea,div").attr("value", function() {
		if ($(this).is(':radio')) {
			fid = $(this).val();
		}
		else if (this.id.indexOf('hidden-') != -1 || this.id.indexOf('srno-') != -1)
			fid = 1;
		else
			fid = '';
		return fid;
	});
	$clone.find("input:checked").attr('checked', false);
	$clone.find("span.validationmsg").text('');
	$clone.attr("id",
		function() {
			var fid = generateId(this.id, indexofcount, counter);
			return fid;
		});

	return $clone;
}
function generateId(id, indexofcount, counter, src) {
	// alert("id: "+id); 

	var index = indexofcount;
	if (id != null && id != '') {
		var arr = (id).split('_');
		var len = arr.length;
		arr[index] = counter;
		for (var i = index + 1; i < len; i++) {
			if (!isNaN(arr[i]))
				arr[i] = 1;
		}
		var fid = arr.join("_");
		return fid;
	}
}

