class Codes

  def self.country_code_to_name code
    {
    	"AD" => "Andorra",
    	"AE" => "United Arab Emirates",
    	"AF" => "Afghanistan",
    	"AG" => "Antigua and Barbuda",
    	"AI" => "Anguilla",
    	"AL" => "Albania",
    	"AM" => "Armenia",
    	"AN" => "Netherlands Antilles",
    	"AO" => "Angola",
    	"AQ" => "Antarctica",
    	"AR" => "Argentina",
    	"AS" => "American Samoa",
    	"AT" => "Austria",
    	"AU" => "Australia",
    	"AW" => "Aruba",
    	"AX" => "√Öland Islands",
    	"AZ" => "Azerbaijan",
    	"BA" => "Bosnia and Herzegovina",
    	"BB" => "Barbados",
    	"BD" => "Bangladesh",
    	"BE" => "Belgium",
    	"BF" => "Burkina Faso",
    	"BG" => "Bulgaria",
    	"BH" => "Bahrain",
    	"BI" => "Burundi",
    	"BJ" => "Benin",
    	"BL" => "Saint Barth√©lemy",
    	"BM" => "Bermuda",
    	"BN" => "Brunei Darussalam",
    	"BO" => "Bolivia, Plurinational State of",
    	"BR" => "Brazil",
    	"BS" => "Bahamas",
    	"BT" => "Bhutan",
    	"BV" => "Bouvet Island",
    	"BW" => "Botswana",
    	"BY" => "Belarus",
    	"BZ" => "Belize",
    	"CA" => "Canada",
    	"CC" => "Cocos (Keeling) Islands",
    	"CD" => "Congo, the Democratic Republic of the",
    	"CF" => "Central African Republic",
    	"CG" => "Congo",
    	"CH" => "Switzerland",
    	"CI" => "C√¥te d'Ivoire",
    	"CK" => "Cook Islands",
    	"CL" => "Chile",
    	"CM" => "Cameroon",
    	"CN" => "China",
    	"CO" => "Colombia",
    	"CR" => "Costa Rica",
    	"CU" => "Cuba",
    	"CV" => "Cape Verde",
    	"CX" => "Christmas Island",
    	"CY" => "Cyprus",
    	"CZ" => "Czech Republic",
    	"DE" => "Germany",
    	"DJ" => "Djibouti",
    	"DK" => "Denmark",
    	"DM" => "Dominica",
    	"DO" => "Dominican Republic",
    	"DZ" => "Algeria",
    	"EC" => "Ecuador",
    	"EE" => "Estonia",
    	"EG" => "Egypt",
    	"EH" => "Western Sahara",
    	"ER" => "Eritrea",
    	"ES" => "Spain",
    	"ET" => "Ethiopia",
    	"FI" => "Finland",
    	"FJ" => "Fiji",
    	"FK" => "Falkland Islands (Malvinas)",
    	"FM" => "Micronesia, Federated States of",
    	"FO" => "Faroe Islands",
    	"FR" => "France",
    	"GA" => "Gabon",
    	"GB" => "United Kingdom",
    	"GD" => "Grenada",
    	"GE" => "Georgia",
    	"GF" => "French Guiana",
    	"GG" => "Guernsey",
    	"GH" => "Ghana",
    	"GI" => "Gibraltar",
    	"GL" => "Greenland",
    	"GM" => "Gambia",
    	"GN" => "Guinea",
    	"GP" => "Guadeloupe",
    	"GQ" => "Equatorial Guinea",
    	"GR" => "Greece",
    	"GS" => "South Georgia and the South Sandwich Islands",
    	"GT" => "Guatemala",
    	"GU" => "Guam",
    	"GW" => "Guinea-Bissau",
    	"GY" => "Guyana",
    	"HK" => "Hong Kong",
    	"HM" => "Heard Island and McDonald Islands",
    	"HN" => "Honduras",
    	"HR" => "Croatia",
    	"HT" => "Haiti",
    	"HU" => "Hungary",
    	"ID" => "Indonesia",
    	"IE" => "Ireland",
    	"IL" => "Israel",
    	"IM" => "Isle of Man",
    	"IN" => "India",
    	"IO" => "British Indian Ocean Territory",
    	"IQ" => "Iraq",
    	"IR" => "Iran, Islamic Republic of",
    	"IS" => "Iceland",
    	"IT" => "Italy",
    	"JE" => "Jersey",
    	"JM" => "Jamaica",
    	"JO" => "Jordan",
    	"JP" => "Japan",
    	"KE" => "Kenya",
    	"KG" => "Kyrgyzstan",
    	"KH" => "Cambodia",
    	"KI" => "Kiribati",
    	"KM" => "Comoros",
    	"KN" => "Saint Kitts and Nevis",
    	"KP" => "Korea, Democratic People's Republic of",
    	"KR" => "Korea, Republic of",
    	"KW" => "Kuwait",
    	"KY" => "Cayman Islands",
    	"KZ" => "Kazakhstan",
    	"LA" => "Lao People's Democratic Republic",
    	"LB" => "Lebanon",
    	"LC" => "Saint Lucia",
    	"LI" => "Liechtenstein",
    	"LK" => "Sri Lanka",
    	"LR" => "Liberia",
    	"LS" => "Lesotho",
    	"LT" => "Lithuania",
    	"LU" => "Luxembourg",
    	"LV" => "Latvia",
    	"LY" => "Libyan Arab Jamahiriya",
    	"MA" => "Morocco",
    	"MC" => "Monaco",
    	"MD" => "Moldova, Republic of",
    	"ME" => "Montenegro",
    	"MF" => "Saint Martin (French part)",
    	"MG" => "Madagascar",
    	"MH" => "Marshall Islands",
    	"MK" => "Macedonia, the former Yugoslav Republic of",
    	"ML" => "Mali",
    	"MM" => "Myanmar",
    	"MN" => "Mongolia",
    	"MO" => "Macao",
    	"MP" => "Northern Mariana Islands",
    	"MQ" => "Martinique",
    	"MR" => "Mauritania",
    	"MS" => "Montserrat",
    	"MT" => "Malta",
    	"MU" => "Mauritius",
    	"MV" => "Maldives",
    	"MW" => "Malawi",
    	"MX" => "Mexico",
    	"MY" => "Malaysia",
    	"MZ" => "Mozambique",
    	"NA" => "Namibia",
    	"NC" => "New Caledonia",
    	"NE" => "Niger",
    	"NF" => "Norfolk Island",
    	"NG" => "Nigeria",
    	"NI" => "Nicaragua",
    	"NL" => "Netherlands",
    	"NO" => "Norway",
    	"NP" => "Nepal",
    	"NR" => "Nauru",
    	"NU" => "Niue",
    	"NZ" => "New Zealand",
    	"OM" => "Oman",
    	"PA" => "Panama",
    	"PE" => "Peru",
    	"PF" => "French Polynesia",
    	"PG" => "Papua New Guinea",
    	"PH" => "Philippines",
    	"PK" => "Pakistan",
    	"PL" => "Poland",
    	"PM" => "Saint Pierre and Miquelon",
    	"PN" => "Pitcairn",
    	"PR" => "Puerto Rico",
    	"PS" => "Palestinian Territory, Occupied",
    	"PT" => "Portugal",
    	"PW" => "Palau",
    	"PY" => "Paraguay",
    	"QA" => "Qatar",
    	"RE" => "R√©union",
    	"RO" => "Romania",
    	"RS" => "Serbia",
    	"RU" => "Russian Federation",
    	"RW" => "Rwanda",
    	"SA" => "Saudi Arabia",
    	"SB" => "Solomon Islands",
    	"SC" => "Seychelles",
    	"SD" => "Sudan",
    	"SE" => "Sweden",
    	"SG" => "Singapore",
    	"SH" => "Saint Helena",
    	"SI" => "Slovenia",
    	"SJ" => "Svalbard and Jan Mayen",
    	"SK" => "Slovakia",
    	"SL" => "Sierra Leone",
    	"SM" => "San Marino",
    	"SN" => "Senegal",
    	"SO" => "Somalia",
    	"SR" => "Suriname",
    	"ST" => "Sao Tome and Principe",
    	"SV" => "El Salvador",
    	"SY" => "Syrian Arab Republic",
    	"SZ" => "Swaziland",
    	"TC" => "Turks and Caicos Islands",
    	"TD" => "Chad",
    	"TF" => "French Southern Territories",
    	"TG" => "Togo",
    	"TH" => "Thailand",
    	"TJ" => "Tajikistan",
    	"TK" => "Tokelau",
    	"TL" => "Timor-Leste",
    	"TM" => "Turkmenistan",
    	"TN" => "Tunisia",
    	"TO" => "Tonga",
    	"TR" => "Turkey",
    	"TT" => "Trinidad and Tobago",
    	"TV" => "Tuvalu",
    	"TW" => "Taiwan, Province of China",
    	"TZ" => "Tanzania, United Republic of",
    	"UA" => "Ukraine",
    	"UG" => "Uganda",
    	"UM" => "United States Minor Outlying Islands",
    	"US" => "United States",
    	"UY" => "Uruguay",
    	"UZ" => "Uzbekistan",
    	"VA" => "Holy See (Vatican City State)",
    	"VC" => "Saint Vincent and the Grenadines",
    	"VE" => "Venezuela, Bolivarian Republic of",
    	"VG" => "Virgin Islands, British",
    	"VI" => "Virgin Islands, U.S.",
    	"VN" => "Viet Nam",
    	"VU" => "Vanuatu",
    	"WF" => "Wallis and Futuna",
    	"WS" => "Samoa",
    	"YE" => "Yemen",
    	"YT" => "Mayotte",
    	"ZA" => "South Africa",
    	"ZM" => "Zambia",
    	"ZW" => "Zimbabwe" 
    }[code]
  end
  
  def self.us_state_to_name code
    {
    	"AL" => "Alabama",
    	"AK" => "Alaska",
    	"AS" => "American Samoa",
    	"AZ" => "Arizona",
    	"AR" => "Arkansas",
    	"CA" => "California",
    	"CO" => "Colorado",
    	"CT" => "Connecticut",
    	"DE" => "Delaware",
    	"DC" => "District Of Columbia",
    	"FM" => "Federated States Of Micronesia",
    	"FL" => "Florida",
    	"GA" => "Georgia",
    	"GU" => "Guam",
    	"HI" => "Hawaii",
    	"ID" => "Idaho",
    	"IL" => "Illinois",
    	"IN" => "Indiana",
    	"IA" => "Iowa",
    	"KS" => "Kansas",
    	"KY" => "Kentucky",
    	"LA" => "Louisiana",
    	"ME" => "Maine",
    	"MH" => "Marshall Islands",
    	"MD" => "Maryland",
    	"MA" => "Massachusetts",
    	"MI" => "Michigan",
    	"MN" => "Minnesota",
    	"MS" => "Mississippi",
    	"MO" => "Missouri",
    	"MT" => "Montana",
    	"NE" => "Nebraska",
    	"NV" => "Nevada",
    	"NH" => "New Hampshire",
    	"NJ" => "New Jersey",
    	"NM" => "New Mexico",
    	"NY" => "New York",
    	"NC" => "North Carolina",
    	"ND" => "North Dakota",
    	"MP" => "Northern Mariana Islands",
    	"OH" => "Ohio",
    	"OK" => "Oklahoma",
    	"OR" => "Oregon",
    	"PW" => "Palau",
    	"PA" => "Pennsylvania",
    	"PR" => "Puerto Rico",
    	"RI" => "Rhode Island",
    	"SC" => "South Carolina",
    	"SD" => "South Dakota",
    	"TN" => "Tennessee",
    	"TX" => "Texas",
    	"UT" => "Utah",
    	"VT" => "Vermont",
    	"VI" => "Virgin Islands",
    	"VA" => "Virginia",
    	"WA" => "Washington",
    	"WV" => "West Virginia",
    	"WI" => "Wisconsin",
    	"WY" => "Wyoming" 
    }[code]
  end
  
  # Generated via lib/cities_to_lat_lng.rb
  def self.us_city_to_lng_lat city_name
    {
    "Atlanta" => [-84.389663, 33.754487],
    "Bellevue" => [-122.150885, 47.597543],
    "Boulder" => [-105.276843, 40.010492],
    "Chicago" => [-87.624333, 41.879535],
    "Columbus" => [-83.000676, 39.962208],
    "Dallas" => [-96.797111, 32.781078],
    "Fresno" => [-119.789382, 36.743313],
    "Houston" => [-95.362534, 29.759956],
    "Los Angeles" => [-118.243425, 34.052187],
    "Los Angeles 2" => [-118.89074, 34.142859],
    "Miami" => [-80.190262, 25.774252],
    "New York" => [-73.986951, 40.756054],
    "Newark" => [-74.172247, 40.73558],
    "Palo Alto" => [-122.159928, 37.437328],
    "Raleigh" => [-78.643386, 35.779735],
    "Richmond" => [-77.433928, 37.540778],
    "Riverside" => [-117.400412, 33.94743],
    "Roanoke" => [-79.94239, 37.272621],
    "San Diego" => [-117.163841, 32.718834],
    "San Jose" => [-121.877636, 37.320052],
    "San Jose 2" => [-122.51338, 37.545228],
    "Santa Clara" => [-121.967982, 37.352313],
    "Seattle" => [-122.347276, 47.620973],
    "Syracuse" => [-76.147375, 43.050831],
    "Tampa" => [-82.451141, 27.98141],
    "Vienna VA" => [-77.258961, 38.900495],
    }[city_name] || 0
  end
    
  
end