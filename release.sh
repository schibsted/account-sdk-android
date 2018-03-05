#!/bin/bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_BRANCH" = "master" ]; then
    echo "We are on the master branch, releasing snapshot."
    ./gradlew publish
    ./hockey_app.sh "$hockey_app_token" "$hockey_app_id"
elif [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ ! -z "$TRAVIS_TAG" ]; then
    echo "We are on tag $TRAVIS_TAG, publishing release."
    ./gradlew publish
    ./hockey_app.sh "$hockey_app_token" "$hockey_app_id_release"
else
    echo "Not publishing any releases."
    echo "Branch is $TRAVIS_BRANCH and tag is $TRAVIS_TAG"
fi
