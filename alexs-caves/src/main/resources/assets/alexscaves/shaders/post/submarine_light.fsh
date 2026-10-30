#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;

out vec4 fragColor;

const float FOV = 70.0;
const float NEAR = 0.05;
const float FAR = 1000.0;
const float EXPOSURE = 100.1;
const float AOE = 15.0;

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (NEAR * FAR) / (FAR + NEAR - z * (FAR - NEAR));
}

void main() {
    float rawDepth = texture(DiffuseDepthSampler, texCoord).r;
    float depth = linearizeDepth(rawDepth);
    float dist = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(OutSize.x / OutSize.y, 1.0) * tan(radians(FOV / 2.0))) * depth);
    float d = length(texCoord - vec2(0.5));
    d = exp(-(d * AOE)) * EXPOSURE / (dist * 0.01);
    if (depth > 500.0 || rawDepth > 0.9999) {
        fragColor = vec4(texture(DiffuseSampler, texCoord).rgb * clamp(1.0 + d, 0.0, 10.0), 1.0);
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}
