#!/usr/bin/env -S just --justfile
# ^ A shebang isn't required, but allows a justfile to be executed
#   like a script, with `./justfile test`, for example.

alias b := build-container
alias p := push
alias c := compile
alias pwr := push-with-restart

log := "warn"

export JUST_LOG := log

clean:
  mvn clean

build-container: compile
  docker build . -t docker.stackable.tech/stackable-experimental/hdfs:3.3.6-stackable0.0.0-authorizer

compile:
  mvn package

push: build-container
  docker push docker.stackable.tech/stackable-experimental/hdfs:3.3.6-stackable0.0.0-authorizer

push-with-restart: push
  kubectl rollout restart statefulset simple-hdfs-namenode-default