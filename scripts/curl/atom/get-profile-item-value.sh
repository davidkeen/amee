#! /bin/sh
. ./curl.conf

type="atom+xml"
amee_url="profiles/${profileUid}/home/energy/electricity/A3BE0064FA9B/kWh"

curl http://${amee_host}/${amee_url} \
  -H "Accept:application/${type}" \
	-u admin:r41n80w \
	-v \
	-o get-profile-item-value-response.xml
	
mate get-profile-item-value-response.xml 
