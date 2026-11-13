#!/usr/bin/env bash
# NixOS launcher: LWJGL/GLFW need libGLX/libGL/libX11 on LD_LIBRARY_PATH.
# We use `nix-shell shell.nix` to assemble them, then invoke gradle inside.
set -euo pipefail

TASK="${1:-runClient}"
exec nix-shell shell.nix --run "./gradlew $TASK --no-daemon"
