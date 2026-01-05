#version 150

#line 0 1
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
#line 3 0

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float Time;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}
void main() {
    //vec4 color = vec4(1, 1, 1, texture(Sampler0, texCoord0).r) * ColorModulator * vertexColor;
    float dist_from_center = distance(texCoord0, vec2(.5f, .5f));
    float value;
    if (dist_from_center < .5 && dist_from_center > .4) value = .5;
    else value = 0;
    float scale = 24;
    float noise_value1 = random(vec2(int(dist_from_center * scale * 10)));
    //vec4 color = vec4(noise_value3, noise_value2, noise_value1, value) * vertexColor * variation + (vec4(1, 1, 1, 0) - variation);
    //vec4 color = vec4(mod(texCoord0.xy, 1), 0, .5);
    vec4 color = vec4(1, 1, 1, value * noise_value1) * (1 - linear_fog_fade(4000, 3900, vertexDistance));

    fragColor = color * vertexColor * ColorModulator;

    //gl_FragDepth = 1.0f;
}
