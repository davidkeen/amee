// get country from metadata
country = profileFinder.getProfileItemValue('metadata', 'country');
if ((country == null) || (country == '')) {
	country = 'United Kingdom';
}

// get electricity value based on country
countryElecValue = dataFinder.getDataItemValue('home/energy/electricity', 'country=' + country, 'kgCO2PerKWh');

if(countryElecValue==null){//try ISO code
  countryElecValue = dataFinder.getDataItemValue('home/energy/electricityiso', 'country=' + country, 'kgCO2PerKWh');
}

// now calculate the monthly kgCO2 emissions
if (countryElecValue != null) {
	if (kWhPerYear != 0) {
		(kWhPerYear * countryElecValue);
	} else if (kWhPerCycle != 0) {
		cycleRate * kWhPerCycle * countryElecValue;
	} else {
		0;
	}
} else {
	0;
}
