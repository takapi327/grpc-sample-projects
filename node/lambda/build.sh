#!/bin/sh

echo remove dir and zip
rm -rf dist/
mkdir dist

echo compile ts
pnpm tsc -p tsconfig.json

echo copy package.json
cp -f ./package.json ./dist

echo install module
cd dist
pnpm install

echo zip
zip -r dist.zip ./
