#!/bin/bash

file_env() {
	local var="$1"
	local fileVar="${var}_FILE"
	local def="${2:-}"
	if [ "${!var:-}" ] && [ "${!fileVar:-}" ]; then
		echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
		exit 1
	fi
	local val="$def"
	if [ "${!var:-}" ]; then
		val="${!var}"
	elif [ "${!fileVar:-}" ]; then
		val="$(< "${!fileVar}")"
	fi

	export "$var"="$val"

	unset "$fileVar"
}

envs=(
  DATASOURCE_URL
  DATASOURCE_USERNAME
  DATASOURCE_PASSWORD
  AWS_ACCESS_KEY
  AWS_SECRET_KEY
  JWT_SECRET
  ACTUATOR_BASE_PATH
)

for e in "${envs[@]}"; do
  file_env "$e"
done

exec "$@"
