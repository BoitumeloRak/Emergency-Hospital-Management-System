#!/bin/bash

echo "Starting Hospital Management System"

echo "Launching Resource Service..."
mvn exec:java -pl resource -Dexec.mainClass="hospital.resource.ResourceService" &
# shellcheck disable=SC2034
RESOURCE_PID=$!

echo "Launching Triage Service..."
mvn exec:java -pl triage -Dexec.mainClass="hospital.triage.TriageService" &
TRIAGE_PID=$!

echo "Launching Web Dashboard..."
mvn exec:java -pl web -Dexec.mainClass="hospital.web.WebService" &
# shellcheck disable=SC2034
WEB_PID=$!


echo "All services are running."

trap "kill $RESOURCE_PID $TRIAGE_PID $WEB_PID; echo -e '\nShutting down...'; exit " INT

wait