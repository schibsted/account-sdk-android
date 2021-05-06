#!/bin/bash

./gradlew publishToSonatype closeSonatypeStagingRepository
./deploy_docs.sh
