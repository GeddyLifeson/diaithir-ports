#!/bin/sh
# Synthesised three-note chime for the research-finished toast (own audio). Usage: gen_research_chime.sh <out.ogg>
ffmpeg -y -loglevel error -f lavfi -i "aevalsrc='0.35*sin(2*PI*523.25*t)*exp(-6*t)+0.3*sin(2*PI*783.99*(t-0.12))*exp(-5*(t-0.12))*gt(t,0.12)+0.25*sin(2*PI*1046.5*(t-0.24))*exp(-4*(t-0.24))*gt(t,0.24)':d=1.0:s=44100" -c:a libvorbis -q:a 4 "$1"
