#!/bin/bash

set -ex

# Generate documentation
./gradlew dokka

mkdir tmp_docs
cd tmp_docs

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
cp -r ../build/docs/smartlock smartlock/docs
cp -r ../build/docs/style.css smartlock/
