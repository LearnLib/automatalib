#!/bin/bash
# Copyright (C) 2013 TU Dortmund
# This file is part of AutomataLib, http://www.automatalib.net/.
# 
# AutomataLib is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License version 3.0 as published by the Free Software Foundation.
# 
# AutomataLib is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with AutomataLib; if not, see
# http://www.gnu.de/documents/lgpl.en.html.

PROJECT_ROOT=".."
XML_HEADER='<?xml version="1.0" encoding="UTF-8" ?>'
XML_HEADER_TEST='<?xml'

PROJECT_ROOT=`readlink -f "$PROJECT_ROOT"`


if [ "$*" == "" ]; then
	git ls-files "$PROJECT_ROOT" | egrep '\.xml$' | xargs "$0"
	exit
fi

for i in "$@"; do
	xml_header=`head -n 1 "$i" | grep -c "$XML_HEADER_TEST"`
	if [ $xml_header -lt 1 ]; then
		echo File $i misses proper XML header, fixing ...
		echo $XML_HEADER >.tmp
		cat "$i" >>.tmp
		mv .tmp "$i"
	fi
done

