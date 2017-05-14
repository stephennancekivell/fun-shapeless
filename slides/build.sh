#!/bin/sh

markdown-to-slides slides.md \
  | sed "s/remark.create()/remark.create({highlightStyle: 'tomorrow-night', navigation: { scroll: false }})/g" \
  | sed "s/<\/style>/.w50 img{width:50%;} <\/style>/g" \
  | sed "s/<\/style>/.w40 img{width:40%;} <\/style>/g" \
  | sed "s/<\/style>/.w25 img{width:25%;} <\/style>/g" \
  > slides.html
