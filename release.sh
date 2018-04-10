#!/bin/bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ ! -z "$TRAVIS_TAG" ]; then
    echo "We are on tag $TRAVIS_TAG, publishing releases."
    ./gradlew :common:bintrayUpload
    ./gradlew :core:bintrayUpload
    ./gradlew :ui:bintrayUpload
    ./gradlew :smartlock:bintrayUpload
    ./deploy_docs.sh
else
    echo "Not publishing any releases."
    echo "Branch is $TRAVIS_BRANCH and tag is $TRAVIS_TAG"
fi
