#!/usr/bin/env bash

protoc --plugin=$(pnpm root)/.bin/protoc-gen-ts_proto \
  --ts_proto_out=src/lib/generated \
  --ts_proto_opt=outputServices=grpc-js \
  --ts_proto_opt=esModuleInterop=true \
  -I=src/ src/**/*.proto
