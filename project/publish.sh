#!/usr/bin/env bash

set -Eeuo pipefail

# Travis values
echo "TRAVIS_TAG=$TRAVIS_TAG"
echo "TRAVIS_BRANCH=$TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST=$TRAVIS_PULL_REQUEST"
echo "TRAVIS_PULL_REQUEST_BRANCH=$TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_SCALA_VERSION=$TRAVIS_SCALA_VERSION"

# Commands
if [ "$TRAVIS_BRANCH" = "$TRAVIS_TAG" ]; then
  echo "Publishing release: $TRAVIS_TAG to sonatype"
  sbt publishSigned && sbt sonatypeReleaseAll
else
  echo "Nothing to publishing to sonatype"
fi
