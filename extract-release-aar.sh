#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./extract-proguard-aar <path-to-identity-sdk-android-demo/app>"
    exit
fi

projectFolderPath=$1
targetDir=$1/src/main/libs

uiAar=ui-multidex-release.aar
coreAar=core-release.aar
smartlockAar=smartlock-release.aar
commonAar=common-release.aar
echo "Cleaning cache."
./gradlew clean
echo "Creating UI aar."
./gradlew ui:assembleRelease
echo "Creating CORE aar."
./gradlew core:assembleRelease
echo "Creating SMARTLOCK aar."
./gradlew smartlock:assembleRelease
echo "Creating COMMON aar."
./gradlew common:assembleRelease

if [ ! -d "$targetDir" ]; then
  echo "$targetDir doesn't exist, creating $targetDir."
  mkdir $targetDir
fi

if [ -e "$targetDir/$uiAar" ]; then
	echo "old UI aar found, removing it."
	rm "$targetDir/$uiAar"
fi
if [ -e "$targetDir/$coreAar" ]; then
	echo "old CORE aar found, removing it."
	rm "$targetDir/$coreAar"
fi

if [ -e "$targetDir/$commonAar" ]; then
	echo "old COMMON aar found, removing it."
	rm "$targetDir/$commonAar"
fi

if [ -e "$targetDir/$smartlockAar" ]; then
	echo "old SMARTLOCK aar found, removing it."
	rm "$targetDir/$smartlockAar"
fi	
echo "Moving created aars to $targetDir."

mv ./ui/build/outputs/aar/$uiAar $targetDir
mv ./core/build/outputs/aar/$coreAar $targetDir
mv ./smartlock/build/outputs/aar/$smartlockAar $targetDir
mv ./common/build/outputs/aar/$commonAar $targetDir
