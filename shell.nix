# Dev shell for launching NeoForge runClient on NixOS.
# LWJGL/GLFW dlopens libGLX.so.0 / libGL.so.1 / libX11 etc. at runtime;
# on NixOS these aren't on the default search path, so we assemble them here.
{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    jdk21
    libglvnd            # libGL, libGLX (dispatcher)
    libGL
    xorg.libX11
    xorg.libXcursor
    xorg.libXrandr
    xorg.libXinerama
    xorg.libXi
    xorg.libXxf86vm
    xorg.libXext
    libpulseaudio       # sound
    alsa-lib
    glfw
    stdenv.cc.cc.lib    # libstdc++
  ];

  shellHook = ''
    export JAVA_HOME="${pkgs.jdk21}/lib/openjdk"
    export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [
      pkgs.libglvnd
      pkgs.libGL
      pkgs.xorg.libX11
      pkgs.xorg.libXcursor
      pkgs.xorg.libXrandr
      pkgs.xorg.libXinerama
      pkgs.xorg.libXi
      pkgs.xorg.libXxf86vm
      pkgs.xorg.libXext
      pkgs.libpulseaudio
      pkgs.alsa-lib
      pkgs.glfw
      pkgs.stdenv.cc.cc.lib
    ]}:/run/opengl-driver/lib''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
  '';
}
