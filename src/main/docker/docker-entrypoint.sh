#!/bin/bash
set -e

SPRING_EXTERNAL_PROPERTIES=/var/opt/teamcode/config/application.yml

#if [ -f $SPRING_EXTERNAL_PROPERTIES ]; then
		# if there's an external properties file, load it
#		APP_OPTS="--spring.config.location=/opt/teamcode/config/ --spring.config.name=application"
#fi

# exec java -jar /app.jar $APP_OPTS
#exec java -jar /app.jar --spring.config.location=file:$SPRING_EXTERNAL_PROPERTIES

exec java -d64 -Xms$XMS -Xmx$XMX -jar /app.jar --spring.config.location=file:/var/opt/teamcode/config/application.yml

# run everything else
exec "$@"