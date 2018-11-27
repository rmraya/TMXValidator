#!/bin/bash

cd "$(dirname "$0")/"

bin/java --module-path lib -m tmxvalidator/com.maxprograms.tmxvalidation.TMXValidator $@