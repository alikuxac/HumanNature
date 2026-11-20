#version 150

uniform sampler2D DiffuseSampler;
uniform float Intensity;
uniform vec2 ScreenSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 desaturated = mix(color.rgb, vec3(gray), clamp(Intensity, 0.0, 1.0) * 0.9);
    vec2 centered = (gl_FragCoord.xy / ScreenSize - 0.5) * 2.0;
    float radius = length(centered);
    float vignette = 1.0 - smoothstep(0.55, 1.25, radius) * clamp(Intensity, 0.0, 1.0);
    fragColor = vec4(desaturated * vignette, color.a);
}
