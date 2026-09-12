#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float Retention;

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);
    vec4 previous = texture(PrevSampler, texCoord);
    fragColor = mix(current, previous, clamp(Retention, 0.0, 0.95));
    fragColor.a = 1.0;
}
