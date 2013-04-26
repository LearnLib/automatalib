#!/bin/bash

if [ "$*" == "" ]; then
	find .. -name '*.java' | xargs "$0"
	exit
fi

for i in "$@"; do
	license=`head -n 1 "$i" | grep -c "/* Copyright (C)"`
	if [ "$license" != "1" ]; then
		echo "File $i misses license header, fixing"
		cat license-header.txt "$i" >.tmp
		mv .tmp "$i"
	fi
done

