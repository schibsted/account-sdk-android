#!/bin/bash
set -e

openssl aes-256-cbc -K $encrypted_de87219daf1c_key -iv $encrypted_de87219daf1c_iv -in deploy_key.enc -out deploy_key -d
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

./gradlew :common:bintrayUpload
./gradlew :core:bintrayUpload
./gradlew :ui:bintrayUpload
./gradlew :smartlock:bintrayUpload
./deploy_docs.sh
