#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;

out vec4 fragColor;

// upstream read the real FOV from a custom uniform; post uniforms are static in 26.1 so the default is used
const float FOV = 70.0;

void main() {
    vec4 scaled = texture(DiffuseSampler, texCoord);
    vec4 blurred = texture(BlurSampler, texCoord);
    float dist = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(OutSize.x / OutSize.y, 1.0) * tan(radians(FOV / 2.0))));
    float circleDistance = dist * dist;
    vec4 outTexel = scaled - blurred + blurred * circleDistance;
    fragColor = vec4(outTexel.rgb, 1.0);
}
