#!/bin/fish

# Convert all *.wav files in directory to wav format with proper parameters for LibGDX library

for f in assets/sfx/*.wav
    set out (string replace -r '\.wav$' '_fixed.wav' "$f")
    ffmpeg -i "$f" -acodec pcm_s16le -ar 44100 -ac 1 "$out"
end
