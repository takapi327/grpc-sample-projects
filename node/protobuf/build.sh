#!/bin/sh

echo remove dist dir
rm -rf dist/

echo compile ts
pnpm tsc -p tsconfig.json

echo copy package.json
cp -f ./package.json ./dist

echo install module
cd dist
pnpm install
