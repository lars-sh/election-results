<?php
function data_2018_get_meta() {
	return array(
		'wahlberechtigte' => 990,
		'stimmzettel' => 601,
		'davon-briefwaehler' => 0,
		'ungueltige-stimmen' => 3
	);
}

function data_2018_get_data() {
	$return = array(
		'klein-boden' => array(
			'poppinga-jens' => 106,
			'eggers-dirk' => 71,
			'behnk-soenke' => 69,
			'breede-rolf' => 64,
			'beck-karsten' => 57,
			'motzkus-dietrich' => 57,
			'weger-marcel' => 57,
			'koenig-eva-maria' => 46,
			'kuehn-steffen' => 41,
			'wahl-joachim' => 37,
			'kraus-michael' => 34,
			'kroeger-dirk' => 29,
			'eick-ernst' => 28,
			'schoening-mathias' => 27,
			'boettger-volker' => 26,
			'gaede-jan-hendrik' => 24,
			'ziebarth-angelika' => 20,
			'gaede-henning' => 18,
			'boettger-johannes' => 16,
			'winter-martin' => 14,
			'ehlert-armin' => 12,
			'sauer-joachim' => 11,
			'joegimar-helga' => 10,
			'stapelfeldt-albert' => 10
		),
		'rethwischdorf' => array(
			'poppinga-jens' => 222,
			'boettger-volker' => 169,
			'behnk-soenke' => 151,
			'eggers-dirk' => 148,
			'gaede-jan-hendrik' => 136,
			'motzkus-dietrich' => 134,
			'gaede-henning' => 133,
			'weger-marcel' => 126,
			'beck-karsten' => 124,
			'kuehn-steffen' => 109,
			'boettger-johannes' => 105,
			'eick-ernst' => 84,
			'kraus-michael' => 82,
			'breede-rolf' => 82,
			'winter-martin' => 73,
			'koenig-eva-maria' => 69,
			'wahl-joachim' => 68,
			'stapelfeldt-albert' => 63,
			'schoening-mathias' => 58,
			'ehlert-armin' => 52,
			'sauer-joachim' => 48,
			'kroeger-dirk' => 46,
			'ziebarth-angelika' => 33,
			'joegimar-helga' => 27
		)
	);
	
	$return['gesamt'] = array();
	foreach ($return['klein-boden'] as $key => $value) {
		$return['gesamt'][$key] = $value + $return['rethwischdorf'][$key];
	}
	
	return $return;
}

function data_2018_get_personen() {
	return array(
		'poppinga-jens' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Poppinga',
			'vorname' => 'Jens'
		),
		'eggers-dirk' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Eggers',
			'vorname' => 'Dirk'
		),
		'beck-karsten' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Beck',
			'vorname' => 'Karsten'
		),
		'motzkus-dietrich' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Motzkus',
			'vorname' => 'Dietrich'
		),
		'behnk-soenke' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Behnk',
			'vorname' => 'Sönke'
		),
		'weger-marcel' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Weger',
			'vorname' => 'Marcel'
		),
		'graepel-carola' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Gräpel',
			'vorname' => 'Carola'
		),
		'schwarz-rupert' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Schwarz',
			'vorname' => 'Rupert'
		),
		'klein-erik' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Klein',
			'vorname' => 'Erik'
		),
		'dohrendorf-martina' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Dohrendorf',
			'vorname' => 'Martina'
		),
		'bernhardt-christian' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Bernhardt',
			'vorname' => 'Christian'
		),
		'topel-andreas' => array(
			'gruppierung' => 'cdu',
			'nachname' => 'Topel',
			'vorname' => 'Andreas'
		),
		'kroeger-dirk' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Kröger',
			'vorname' => 'Dirk'
		),
		'eick-ernst' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Eick',
			'vorname' => 'Ernst'
		),
		'joegimar-helga' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Jögimar',
			'vorname' => 'Helga'
		),
		'ehlert-armin' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Ehlert',
			'vorname' => 'Armin'
		),
		'ziebarth-angelika' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Ziebarth',
			'vorname' => 'Angelika'
		),
		'sauer-joachim' => array(
			'gruppierung' => 'spd',
			'nachname' => 'Sauer',
			'vorname' => 'Joachim'
		),
		'gaede-jan-hendrik' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Gäde',
			'vorname' => 'Jan-Hendrik'
		),
		'boettger-johannes' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Böttger',
			'vorname' => 'Johannes'
		),
		'boettger-volker' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Böttger',
			'vorname' => 'Volker'
		),
		'winter-martin' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Winter',
			'vorname' => 'Martin'
		),
		'gaede-henning' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Gäde',
			'vorname' => 'Henning'
		),
		'stapelfeldt-albert' => array(
			'gruppierung' => 'awg',
			'nachname' => 'Stapelfeldt',
			'vorname' => 'Albert'
		),
		'kuehn-steffen' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Kühn',
			'vorname' => 'Steffen'
		),
		'wahl-joachim' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Wahl',
			'vorname' => 'Joachim'
		),
		'kraus-michael' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Kraus',
			'vorname' => 'Michael'
		),
		'breede-rolf' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Breede',
			'vorname' => 'Rolf'
		),
		'schoening-mathias' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Schöning',
			'vorname' => 'Mathias'
		),
		'koenig-eva-maria' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'König',
			'vorname' => 'Eva-Maria'
		),
		'hartz-catrin' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Hartz',
			'vorname' => 'Catrin'
		),
		'dohrendorf-thomas' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Dohrendorf',
			'vorname' => 'Thomas'
		),
		'efrom-joachim' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Efrom',
			'vorname' => 'Joachim'
		),
		'feddern-axel' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Feddern',
			'vorname' => 'Axel'
		),
		'feddern-hartmut' => array(
			'gruppierung' => 'fwr',
			'nachname' => 'Feddern',
			'vorname' => 'Hartmut'
		)
	);
}

function data_2018_get_seats() {
	return array(
		'cdu' => 5,
		'awg' => 3,
		'fwr' => 2,
		'spd' => 1
	);
}

function data_2018_get_types() {
	return array(
		'gesamt' => 'Gesamt',
		'klein-boden' => 'Klein Boden',
		'rethwischdorf' => 'Rethwischdorf'
	);
}
