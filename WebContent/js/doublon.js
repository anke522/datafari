
$(function($) {

	if ($(window).width()>799){
		Manager.addWidget(new AjaxFranceLabs.FacetDuplicates({
			elm : $('#facet_signature'),
			id : 'facet_signature',
			field : 'signature',
			name : "Doublons",
			mincount : 2,
			pagination : true,
			selectionType : 'OR',
			returnUnselectedFacetValues : true
		}));
	}
});