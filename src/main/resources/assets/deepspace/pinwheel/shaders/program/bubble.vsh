#version 150
#line 0 1
#veil:buffer veil:camera VeilCamera
/*#version 150*/

#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
float light0 = max(0.0, dot(lightDir0, normal));
float light1 = max(0.0, dot(lightDir1, normal));
float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
return vec4(color.rgb * lightAccum, color.a);
}

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}
#line 0 2
/*#version 150*/

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
if (vertexDistance <= fogStart) {
return inColor;
}

float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
if (vertexDistance <= fogStart) {
return 1.0;
} else if (vertexDistance >= fogEnd) {
return 0.0;
}

return smoothstep(fogEnd, fogStart, vertexDistance);
}

float fog_distance(vec3 pos, int shape) {
if (shape == 0) {
return length(pos);
} else {
float distXZ = length(pos.xz);
float distY = abs(pos.y);
return max(distXZ, distY);
}
}
#line 4 0

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
layout(location = 5) in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform vec3 SunPosition;
uniform float Time;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec3 pos = Position + ChunkOffset + VeilCamera.CameraBobOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexDistance = fog_distance(pos, FogShape);
    //vertexDistance = 1.0f/0.0f;
    float len = length(SunPosition);
    vertexColor = Color * vec4(clamp(dot(normalize(SunPosition - pos), Normal) + .5f + (1.0 / (len * len)), .2f, 2));
    // vertexColor = vec4(SunPosition.xyz, 1); # visualize position

    texCoord0 = UV0 + Time / 320;
}