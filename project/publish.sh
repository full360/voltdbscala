#!/usr/bin/env bash

set -Eeuo pipefail

# Travis values
echo "TRAVIS_TAG=$TRAVIS_TAG"
echo "TRAVIS_BRANCH=$TRAVIS_BRANCH"
echo "TRAVIS_PULL_REQUEST=$TRAVIS_PULL_REQUEST"
echo "TRAVIS_PULL_REQUEST_BRANCH=$TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_SCALA_VERSION=$TRAVIS_SCALA_VERSION"

# Commands
function compile_test {
  sbt ++$TRAVIS_SCALA_VERSION compile test
}

function publish {
  if [ "$TRAVIS_BRANCH" = "$TRAVIS_TAG" ]; then
    echo "Publishing $TRAVIS_TAG to sonatype"
    sbt ++$TRAVIS_SCALA_VERSION publishSigned
  else
    echo "Nothing to publishing to sonatype"
  fi
}

function release {
  if [ "$TRAVIS_BRANCH" = "$TRAVIS_TAG" ]; then
    echo "Releasing $TRAVIS_TAG to sonatype"
    sbt sonatypeReleaseAll
  else
    echo "Nothing to release to sonatype"
  fi
}

case "$1" in
  test)
    compile_test
    ;;

  publish)
    publish
    ;;

  release)
    release
    ;;

  *)
    echo $"Usage: $0 {test|publish|release}"
    exit 1
esac
