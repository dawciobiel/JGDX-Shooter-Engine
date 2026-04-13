#!/usr/bin/env fish

# -----------------------------------------------------------------------------
# Project: JGDX-Shooter-Engine
# Script:  convert-audio-file.fish
#
# Description:
#   Recursively converts all *.wav audio files in assets/sfx/ into a
#   LibGDX-compatible format (PCM 16-bit, 44.1kHz, mono).
#
#   The script:
#     - Works independently of current working directory
#     - Resolves project root based on script location
#     - Processes files recursively
#     - Skips already processed files (*_fixed.wav)
#     - Uses temporary files to safely overwrite originals (FFmpeg limitation)
#
# Author:    Dawid Bielecki "dawciobiel"
# GitHub:    https://github.com/dawciobiel/JGDX-Shooter-Engine
# License:   GNU GPL 3.0
# Date:      2026-04-13
# -----------------------------------------------------------------------------

# Resolve directory where this script is located
set script_dir (dirname (realpath (status filename)))

# Project root (assumes script is in /scripts)
set project_root (realpath "$script_dir/..")

# Target directory
set target_dir "$project_root/assets/sfx"

# Validate target directory
if not test -d "$target_dir"
    echo "Error: Directory not found: $target_dir"
    exit 1
end

# Find all wav files recursively
set files (find "$target_dir" -type f -name "*.wav")

# Handle empty result
if test (count $files) -eq 0
    echo "No .wav files found in $target_dir"
    exit 0
end

# Process files
for f in $files

    # Skip already converted files
    if string match -q '*_fixed.wav' "$f"
        continue
    end

    echo "Converting $f"

    # Temporary output file (FFmpeg cannot overwrite input directly)
    set tmp "$f.tmp.wav"

    # Convert audio to LibGDX-compatible format
    ffmpeg -loglevel error -stats -y \
        -i "$f" \
        -acodec pcm_s16le \
        -ar 44100 \
        -ac 1 \
        "$tmp"

    # Replace original file safely
    mv "$tmp" "$f"
end

echo "Conversion completed."
