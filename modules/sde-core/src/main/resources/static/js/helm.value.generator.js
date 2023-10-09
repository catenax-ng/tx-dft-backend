let requireFieldList = [];
var $globalAssetId;
var $shellIdShortId;
var $relationalsubmodel;

var globalAssetIdControl;
var shellIdShortIdControl;
var relationalsubmodel;


let optionsWithCreate = {
	plugins: ["remove_button", "drag_drop"],
	delimiter: ",",
	persist: false,
	create: true,
	maxItems: null,
	valueField: 'name',
	labelField: 'name',
	searchField: 'name',
	onOptionAdd: addItemTolist,
	options: []
};
let optionsWithOutCreate = {
	plugins: ["remove_button", "drag_drop"],
	delimiter: ",",
	persist: false,
	maxItems: null,
	valueField: 'name',
	labelField: 'name',
	searchField: 'name',
	options: []
};

let optionsWithGlobal = {
	valueField: 'name',
	labelField: 'name',
	searchField: 'name',
	options: []
};

$(document).ready(function() {
	$globalAssetId = $('#globalAssetId').selectize(optionsWithGlobal);
	$shellIdShortId = $('#shellIdShortId').selectize(optionsWithOutCreate);
    $relationalsubmodel = $('#relationalsubmodel').selectize(optionsWithGlobal);
    
	globalAssetIdControl = $globalAssetId[0].selectize;
	shellIdShortIdControl = $shellIdShortId[0].selectize;
	relationalsubmodel = $relationalsubmodel[0].selectize;

	$(".dyanamicdiv").hide();

	$('#waitingModel').modal('show');

	$('#submodel').change(function() {
		let urn = $(this).val();
		$('#waitingModel').modal('show');
		$.getJSON('/api/hub/models/' + encodeURIComponent(urn) + '/toflatcsv', function(reposnedataSet) {
			let selectedText = $("#submodel option:selected").text();
			let nameDetails = selectedText.split("-");
			$('#title').val(nameDetails[0]);
			$('#description').val(reposnedataSet.description);
			$('#semanticId').val(urn);
			$('#submodelIdShort').val(nameDetails[0].toLowerCase());
			$('#version').val(nameDetails[1]);

			$('#examplePayload').text(JSON.stringify(reposnedataSet.examplePayload, undefined, 2));
			$('#digitalTwin').text(JSON.stringify(JSON.parse(getDigitalTwinObj()), undefined, 2));
			$('#examples').text(JSON.stringify([reposnedataSet.examples], undefined, 2));

			globalAssetIdControl.clearOptions();
			shellIdShortIdControl.clearOptions();
			createCSVFieldTable(reposnedataSet.properties);
			requireFieldList = reposnedataSet.required;

			$(".dyanamicdiv").show();
			$('#waitingModel').modal('hide');
		})
			.fail(function(ex) { alert('failed, ' + ex); });

	});


	$.getJSON('/api/readSubmodels', function(dataSet) {
		console.log(dataSet);
	})
		.fail(function(ex) { alert('failed, ' + ex); });

	$.getJSON('/api/hub/models?status=RELEASED&pageSize=1000&page=0', function(dataSet) {
		$("#submodel").append('<option value="">-- Select Option--</option>');
		$.each(dataSet.items, function(index, item) {
			$("#submodel").append('<option value="' + item.urn + '">' + item.name + '-' + item.version + '</option>');
		});
		$('#waitingModel').modal('hide');
	})
		.fail(function(ex) { alert('failed, ' + ex); });


	$.getJSON('/api/usecases', function(dataSet) {
		$("#usecases").append('<option value="">-- Select Option--</option>');
		$.each(dataSet, function(index, item) {
			$("#usecases").append('<option value="' + item.id + '">' + item.title + '</option>');
		});
		$('#usecases').selectize(optionsWithOutCreate);
	})
		.fail(function(ex) { alert('failed, ' + ex); });




});

function createCSVFieldTable(dataSet) {
	$(".csvFieldsTable_dyanmic_row").remove();
	let index = 1;
	$.each(dataSet, function(key, value) {
		let csvFValue = key.substr(key.lastIndexOf(".") + 1)
		let csvFNameUseCount = $("[id^='csvFieldName-csvFieldsTable_'][value='" + csvFValue + "']").length;
		if (csvFNameUseCount > 0) {
			csvFValue = key;
		}
		let scehmaDetials = JSON.stringify(value, undefined, 2);

		$("#tabelname-csvFieldsTable").append('<tr class="csvFieldsTable_dyanmic_row" id="tr-csvFieldsTable_' + index + '">'
			+ '<td ><span id="srno-csvFieldsTable_' + index + '">' + index + '</span>. <br/><br/>'
			+ '<img src="./img/remove.png" id="delbutton-csvFieldsTable_' + index + '" onclick="removeRow(this.id,\'1\')"/><br/><br/>'
			+ 'Preference: <input type="text" class="preference form-control" id="preference-csvFieldsTable_' + index + '" value="' + index + '" onblur="goToAddOption(this.id)"/>'
			+ '</td>'
			+ '<td>'
			+ '<label for="schemaFieldName-csvFieldsTable_' + index + '"><strong>Schema Field</strong>: </label> <br/>'
			+ '<input type="text" class="form-control" readonly id="schemaFieldName-csvFieldsTable_' + index + '" value="' + key + '" onblur="goToAddOption(this.id)"/><br/><br/>'
			+ '<label for="csvFieldName-csvFieldsTable_' + index + '"><strong>CSV Fields Title</strong>: </label>'
			+ '<input type="text" id="csvFieldName-csvFieldsTable_' + index + '" value="' + csvFValue + '" class="form-control"/>'
			+ '</td>'
			+ '<td><textarea class="descriptionview form-control" id="schemadetails-csvFieldsTable_' + index + '">' + scehmaDetials + '</textarea></td>'
			+ '</tr>');

		$("#hidden-csvFieldsTable").val(index);

		index++;
	});
	addOptionToDropDown();
	/*$("#tabelname-csvFieldsTable").sortable({
		items: 'tr:not(tr:first-child)',
		cursor: 'pointer',
		axis: 'y',
		dropOnEmpty: false,
		start: function(e, ui) {
			ui.item.addClass("selected");
		},
		stop: function(e, ui) {
			ui.item.removeClass("selected");
			$(this).find("tr").each(function(index) {
				if (index > 0) {
					$(this).find(".preference").val(index);
				}
			});
		}
	});*/

}

function goToAddOption(src) {
	let values = $("#" + src).val();
	let csvFValue = values.substr(values.lastIndexOf(".") + 1)
	let fId = src.replace('schemaFieldName', '');
	$("#csvFieldName" + fId).val(csvFValue);
	let sampleJson = { type: "string", description: "" + csvFValue + "" };
	$("#schemadetails" + fId).val(JSON.stringify(sampleJson, undefined, 2));
	addOptionToDropDown();
}

function addOptionToDropDown() {

	deleteAllRowexcept1st('specificAssetIdstbl');

	$("#tabelname-specificAssetIdstbl").find("[id^='key-specificAssetIdstbl'],[id^='value-specificAssetIdstbl']").each(function() {
		$(this).html("");
	});

	globalAssetIdControl.clearOptions();
	shellIdShortIdControl.clearOptions();

	$("[id^='schemaFieldName-csvFieldsTable_']").each(function() {
		let item = $(this).val();
		addItemTolist(item);
	});
}


function addItemTolist(item) {
	
	globalAssetIdControl.addOption({ 'id': item, name: item });

	$("#tabelname-specificAssetIdstbl").find("[id^='key-specificAssetIdstbl'],[id^='value-specificAssetIdstbl']").each(function() {
		$(this).append('<option value="' + item + '">' + item + '</option>');
	})
	shellIdShortIdControl.addOption({ 'id': item, name: item });
	
	relationalsubmodel.addOption({ 'id': item, name: item });
	
}

function getDigitalTwinObj() {
	return `{
    "description": [],
    "displayName": [],
    "globalAssetId": "urn:uuid:{globalAssetId}",
    "idShort": "{shellShortId}",
    "id": "urn:uuid:{autoGeneratedUUID}",
    "specificAssetIds": "[{specificAssetIds}]",
    "submodelDescriptors": [
        {
            "endpoints": [
                {
                    "interface": "SUBMODEL-3.0",
                    "protocolInformation": {
                        "href": "{edcConnectorHost}/submodel",
                        "endpointProtocol": "HTTP",
                        "endpointProtocolVersion": [
                            "1.1"
                        ],
                        "subprotocolBody": "id={shellId}-{submoldeId};dspEndpoint={edcConnectorHost}",
                        "subprotocolBodyEncoding": "plain",
                        "securityAttributes": [
                            {
                                "type": "NONE",
                                "key": "NONE",
                                "value": "NONE"
                            }
                        ]
                    }
                }
            ],
            "idShort": "{submodelShortId}",
            "id": "urn:uuid:{autoGeneratedUUID}",
            "semanticId": {
                "type": "ExternalReference",
                "keys": [
                    {
                        "type": "GlobalReference",
                        "value": "{sematicId}"
                    }
                ]
            },
            "supplementalSemanticId": [],
            "description": [],
            "displayName": []
        }
    ]
}`;
}
