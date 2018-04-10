#!/bin/bash

# This is heavily based on a script from Square at
# https://github.com/square/retrofit/blob/master/deploy_website.sh

set -e

VERSION=$1

if [ -z $VERSION ]; then
    echo "Version is not specified"
    echo "Usage: ./deploy_docs.sh <VERSION>"
    exit 1
fi

while true; do
    read -p "Version is <$VERSION>. Is this correct (y/n)? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done


REPO="git@github.com:schibsted/account-sdk-android.git"
DIR=temp

# Delete any existing temporary website clone
rm -rf $DIR

# Generate documentation
./gradlew :core:dokkaJavadoc :ui:dokkaJavadoc

# Clone the current repo into temp folder
git clone $REPO $DIR

# Move working directory into temp folder
cd $DIR

# Checkout and track the gh-pages branch
git checkout -t origin/gh-pages

# Copy website files from real repo
cp -R ../core/build/javadoc core/docs/$VERSION
cp -R ../ui/build/javadoc ui/docs/$VERSION

echo "Please update the navigation with links to the newly generated version"
read -p "Press enter to continue"
vim _data/navigation.yml

# Stage all files in git and create a commit
git add .
git commit -m "Docs version <$VERSION> generated at $(date)"

# Push the new files up to GitHub
git push origin gh-pages

# Delete our temp folder
cd ..
rm -rf $DIR
