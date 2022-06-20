<?php
function data_%tY_get_meta() {
	return array(
		'wahlberechtigte' => %s,
		'stimmzettel' => %s,
		'davon-briefwaehler' => %d,
		'ungueltige-stimmen' => %d
	);
}

function data_%1$tY_get_data() {
	$return = array(%s);
	
	$return['gesamt'] = array();
	foreach ($return['klein-boden'] as $key => $value) {
		$return['gesamt'][$key] = $value + $return['rethwischdorf'][$key];
	}
	
	return $return;
}

function data_%1$tY_get_personen() {
	return array(%s);
}

function data_%1$tY_get_seats() {
	return array(%s);
}

function data_%1$tY_get_types() {
	return array(
		'gesamt' => 'Gesamt'%s
	);
}
