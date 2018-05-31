#!/bin/bash

set -ex

REPO="git@github.com:schibsted/account-sdk-android.git"
DIR=temp

# Delete any existing temporary website clone
rm -rf ${DIR}

# Generate documentation
./gradlew dokka

# Clone the current repo into temp folder
git clone ${REPO} ${DIR}

# Move working directory into temp folder
cd ${DIR}

# Checkout the gh-pages branch
git checkout gh-pages

# Remove old API docs
rm -rf */docs

# Copy website files
cp ../README.md .

[ -d common ] || mkdir common
cp -r ../build/docs/common common/docs
cp -r ../build/docs/style.css common/

[ -d core ] || mkdir core
cp ../core/README.md core/
cp -r ../build/docs/core core/docs
cp -r ../build/docs/style.css core/

[ -d ui ] || mkdir ui
cp ../ui/README.md ui/
cp -r ../build/docs/ui ui/docs
cp -r ../build/docs/style.css ui/

[ -d smartlock ] || mkdir smartlock
cp ../smartlock/README.md smartlock/
cp -r ../build/docs/ui ui/docs
cp -r ../build/docs/style.css ui/

# Stage all files in git and create a commit
git add .
git commit -m "Docs generated at $(date)"

# Push the new files up to GitHub
git push origin gh-pages

# Delete our temp folder
cd ..
rm -rf ${DIR}
