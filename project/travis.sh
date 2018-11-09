#!/usr/bin/env bash

set -Eeuo pipefail

# Travis values
echo "TRAVIS_TAG=$TRAVIS_TAG"
echo "TRAVIS_BRANCH=$TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST=$TRAVIS_PULL_REQUEST"
echo "TRAVIS_PULL_REQUEST_BRANCH=$TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_SCALA_VERSION=$TRAVIS_SCALA_VERSION"

function compile_test {
  sbt ++$TRAVIS_SCALA_VERSION compile test
}

# Commands
if [ "$TRAVIS_BRANCH" = "$TRAVIS_TAG" ]; then
  compile_test \
  && sbt ++$TRAVIS_SCALA_VERSION publishSigned \
  && sbt ++$TRAVIS_SCALA_VERSION sonatypeRelease
else
  compile_test
fi
