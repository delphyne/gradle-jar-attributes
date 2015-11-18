#!/bin/bash

# Fail Fast.
set -e

# Bail out if we're not running in Travis
if [ "${TRAVIS}" != "true" ]; then
    echo This script only designed to work within Travis CI
    exit 1;
fi

# Setup minimum Git environment
git config --global user.email 'delphyne+travis-ci@gmail.com'
git config --global user.name 'Travis CI'

export M2_REPO_DIRECTORY=delphyne.github.io

# clone the M2 Repository
git clone --quiet --depth 1 --branch master "https://${GH_TOKEN}@github.com/delphyne/delphyne.github.io.git" build/${M2_REPO_DIRECTORY} >/dev/null 2>&1

# Push the maven artifacts into the right part of the tree
./gradlew uploadArchives

# switch into our checked out M2 Repo clone
pushd build/${M2_REPO_DIRECTORY}

# Regenerate the site index for the .m2 directory
./reindex.sh

# commit and push our changes back to the .m2 site
git add .m2
git commit -m "Deploy ${TRAVIS_TAG} artifact to Maven2 Repository."
git push --quiet "https://${GH_TOKEN}@github.com/delphyne/delphyne.github.io.git" master:master >/dev/null 2>&1
