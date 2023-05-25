#!/usr/bin/env bash

protoc --ts_proto_opt=nestJs=true \
  --plugin=./node_modules/.bin/protoc-gen-ts_proto \
  --ts_proto_out=src/lib/generated \
  -Isrc/proto \
  src/proto/*
