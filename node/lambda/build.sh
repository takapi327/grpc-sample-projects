#!/bin/sh

echo remove dir and zip
rm -rf dist/

echo compile ts
tsc -p tsconfig.json

echo copy package.json
cp -f ./package.json ./dist

echo install module
cd dist
pnpm install

echo zip
zip -r dist.zip ./
