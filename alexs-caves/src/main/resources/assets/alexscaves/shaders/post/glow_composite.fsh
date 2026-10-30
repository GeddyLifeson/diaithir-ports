#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D GlowSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;

out vec4 fragColor;

// Glow layers are drawn with premultiplied alpha into their own target; add the blurred copy on top for the bloom.
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 glow = texture(GlowSampler, texCoord);
    vec4 blur = texture(BlurSampler, texCoord);
    vec3 rgb = scene.rgb * (1.0 - clamp(glow.a, 0.0, 1.0)) + glow.rgb + blur.rgb * 1.25;
    fragColor = vec4(min(rgb, vec3(1.0)), 1.0);
}
