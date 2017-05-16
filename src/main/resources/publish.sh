#!/bin/bash
tar -xzf /temp/portal.web.tar.gz;
rm -rf /temp/portal.web/WEB-INF/server.properties;
rm -rf /temp/portal.web/WEB-INF/web.xml;
fileList=`ls /opt`
for file in $fileList
do
 webapp="/opt/${file}/webapps";
 if [ -d "$webapp" ];then 
	echo "${webapp} begin cp";
	cp -rf /temp/portal.web $webapp
	/bin/bash "${webapp}/../bin/shutdown.sh";
	/bin/bash "${webapp}/../bin/startup.sh"
	echo "${webapp} execute complete";
 fi
done

