let fieldList = [];
var $globalAssetId;
var $localIdentifier;
var $specificAssetIds;
var $shellIdShortId;

var csvFieldsControl;
var globalAssetIdControl;
var localIdentifierControl;
var specificAssetIdsControl;
var shellIdShortIdControl;


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
	$csvFields = $('#csvFields').selectize(optionsWithCreate);
	$globalAssetId = $('#globalAssetId').selectize(optionsWithGlobal);
	$localIdentifier = $('#localIdentifier').selectize(optionsWithOutCreate);
	$specificAssetIds = $('#specificAssetIds').selectize(optionsWithOutCreate);
	$shellIdShortId = $('#shellIdShortId').selectize(optionsWithOutCreate);

	csvFieldsControl = $csvFields[0].selectize;
	globalAssetIdControl = $globalAssetId[0].selectize;
	localIdentifierControl = $localIdentifier[0].selectize;
	specificAssetIdsControl = $specificAssetIds[0].selectize;
	shellIdShortIdControl = $shellIdShortId[0].selectize;

	$(".dyanamicdiv").hide();

	//$('#waitingModel').modal('show');

	$('#submodel').change(function() {

		$.getJSON('/api/hub/models/' + encodeURIComponent($(this).val()) + '/example-payload', function(dataSet) {
			$('#examplePayloadPre').text(JSON.stringify(dataSet, undefined, 2));
			$('#digitalTwinPre').text(JSON.stringify(JSON.parse(getDigitalTwinObj()), undefined, 2));
			$('#examplePayload').val(JSON.stringify(dataSet, undefined, 2));
			$('#digitalTwin').val(JSON.stringify(JSON.parse(getDigitalTwinObj()), undefined, 2));

			csvFieldsControl.clear();
			csvFieldsControl.clearOptions();
			globalAssetIdControl.clearOptions();
			localIdentifierControl.clearOptions();
			specificAssetIdsControl.clearOptions();
			shellIdShortIdControl.clearOptions();

			getFlatCSVFieldsFromServer(dataSet);
			$(".dyanamicdiv").show();
		})
			.fail(function(ex) { alert('failed, ' + ex); });

	});

	$.getJSON('/api/hub/models?status=RELEASED&pageSize=1000&page=0', function(dataSet) {
		$("#submodel").append('<option value="">-- Select Option--</option>');
		$.each(dataSet.items, function(index, item) {
			$("#submodel").append('<option value="' + item.urn + '">' + item.name + '-' + item.version + '</option>');
		});
		//$('#waitingModel').modal('hide');
	})
		.fail(function(ex) { alert('failed, ' + ex); });


	$.getJSON('/api/usecases', function(dataSet) {
		$("#usecase").append('<option value="">-- Select Option--</option>');
		$.each(dataSet, function(index, item) {
			$("#usecase").append('<option value="' + item.id + '">' + item.title + '</option>');
		});
		$('#usecase').selectize(optionsWithOutCreate);
	})
		.fail(function(ex) { alert('failed, ' + ex); });




});

function getFlatCSVFieldsFromServer(dataSet) {
	$.ajax({
		type: "POST",
		url: '/api/hub/models/json/toflatcsv',
		data: JSON.stringify(dataSet),
		contentType: "application/json"
	})
		.done(function(data, textStatus, jqXHR) {
			$.each(data, function(index, item) {
				csvFieldsControl.addOption({ 'id': item, name: item });
			});
			fieldList = data;
			csvFieldsControl.setValue(data);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			console.log("Ajax problem: " + textStatus + ". " + errorThrown);
		});
}


function addItemTolist(item) {
	globalAssetIdControl.addOption({ 'id': item, name: item });
	localIdentifierControl.addOption({ 'id': item, name: item });
	specificAssetIdsControl.addOption({ 'id': item, name: item });
	shellIdShortIdControl.addOption({ 'id': item, name: item });
}

function getDigitalTwinObj() {
	return `{
    "description": [],
    "displayName": [],
    "globalAssetId": "urn:uuid:{uuid}",
    "idShort": "{shellShortId}",
    "id": "urn:uuid:{uuid}",
    "specificAssetIds": "{specificAssetIds}",
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
            "id": "urn:uuid:{uuid}",
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
