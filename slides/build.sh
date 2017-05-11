#!/bin/sh

markdown-to-slides slides.md \
  | sed "s/remark.create()/remark.create({highlightStyle: 'tomorrow-night', navigation: { scroll: false }})/g" \
  > slides.html
