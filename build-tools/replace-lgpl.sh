#!/bin/bash

LICENSE_BODY_TPL='license-body.txt'

IFS=$'\n'


	mod_license_body=`gmktemp`
	gsed -re 's@^(.*)$@'"$pre"'\1'"$post"'@g' <"$LICENSE_BODY_TPL" >"$mod_license_body"
	old_head=`gmktemp`
	ghead -n $((line_start - 1)) <"$file" >"$old_head"
	old_tail=`gmktemp`
	gtail -n +$((line_end + 1)) <"$file" >"$old_tail"
	cat "$old_head" "$mod_license_body" "$old_tail" >"$file"
done
