#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo "Usage: ./draft_release_notes <version>"
    exit
fi

CHANGELOG=CHANGELOG.md
LATEST_TAG=$(git describe --abbrev=0 --tags)
LOG=$(git log --pretty=format:"%s" $LATEST_TAG..HEAD)

NOTES=""
while read -r line; do

    if [[ $line != *"[Gradle Release Plugin]"* ]] && [[ $line != *"fixup!"* ]] && [[ -n "$line" ]]; then
        NOTES+="- ${line}"$'\n'
    fi
done <<< "$LOG"

VERSION="$1 ($(date -I))"
CONTENT=$(cat $CHANGELOG 2> /dev/null)

echo "## $VERSION"$'\n'"$NOTES"$'\n\n'"$CONTENT" > $CHANGELOG

