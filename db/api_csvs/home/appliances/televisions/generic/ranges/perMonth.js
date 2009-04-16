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

//the standby profile item values default to zero
standbyHours=30.4*24-hoursPerMonth;
if(standbyHours>0){
	profileFinder.setProfileItemValue('standbyAlwaysCO2'
	,kWStandby * standbyHours * countryElecValue);
}

standbyHours-=30.4*3; //mostly is 3 hours per day less standby
if(standbyHours>0){
	profileFinder.setProfileItemValue('standbyMostlyCO2'
	,kWStandby * standbyHours * countryElecValue);
}

standbyHours-=30.4*4;//sometimes is 7 hours per day less standby
if(standbyHours>0){
	profileFinder.setProfileItemValue('standbySometimesCO2'
	,kWStandby * standbyHours * countryElecValue);
}

// now calculate the monthly kgCO2 emissions
if (countryElecValue != null) {
    kWOn * hoursPerMonth * countryElecValue; 
} else {
	0;
}