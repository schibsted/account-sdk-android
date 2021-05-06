#!/bin/bash

./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
./deploy_docs.sh
